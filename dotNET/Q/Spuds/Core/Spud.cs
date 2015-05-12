using System;
using System.Collections;
using System.Collections.Generic;
using Q.Util;

namespace Q.Spuds.Core {
    public abstract class Spud<T> : SpudBase, IEnumerable<T> {
        readonly List<T> t = new List<T>();
        bool initialized;
        public event Action<T> valueChanged;
        public event Action<T> valueSet;
        public event Action pushedDown;


        protected Spud(SpudManager manager) : base(manager) {
            valueChanged += doNothing;
            valueSet += doNothing;
            pushedDown += doNothing;
        }

        protected abstract T calculate();

        public override void updateThyself() {
            set(calculate());
        }

        public static implicit operator T(Spud<T> s) {
            return s[0];
        }

        public override int count() {
            if(!isInitialized() && canUpdate()) updateAndClean();
            return t.Count;
        }


        public virtual T this[int index] {
            get {
                requireAlive();
                if(index == 0 && isDirty()) updateAndClean();
                var c = t.Count;
                if(index < 0 || index >= c)
                    Bomb.toss(c == 0 ? "use of uninitialized spud" : "index must be >= 0 and < " + c + " but was " + index);
                return t[c - index - 1];
            }
        }


        public T[] last(int window) {
            return toArray(window);            
        }

        protected virtual void set(T value) {
            requireAlive();
            var changed = false;
            if (!isInitialized()) {
                t.Add(value);
                initialized = true;
                makeChildrenDirty();
                changed = true;
            } else if (!value.Equals(t[t.Count - 1])) {
                t[t.Count - 1] = value;
                makeChildrenDirty();
                changed = true;
            }
            beClean();
            valueSet(value);
            if (changed) valueChanged(value);
        }

        void makeChildrenDirty() {
            if(!isDirty())
                eachChild(child => child.beDirty());
        }

        public override void recalculate() {
            if(canUpdate())
                updateAndClean();
        }

        public override void pushDown() {
            requireAlive();
            if(manager.hasPushedDown(this)) return;
            // do children first so that they are processed while the parent is in the "correct" timestamp
            eachChild(child => child.pushDown());
            if(hasContent())
                t.Add(this[0]); // this has the valuable side-effect of updating the initializedValue of the spud before it's pushed down
            manager.pushedDown(this);
            thyselfBeDirty();
            pushedDown();
        }

        public override bool canUpdate() {
            if(isEmpty(parents)) return false;
            foreach (var parent in parents) if (!parent.hasContent()) return false;
            return true;
        }

        public bool changed() {
            if (count() == 1) return true;
            return !this[0].Equals(this[1]);
        }

        public bool isInitialized() {
            return initialized;
        }

        public Spud<U> transform<U>(Converter<T, U> transformer) {
            return new SpudTransformer<U, T>(this, transformer);
        }

        public void requireNoListeners() {
            var listeners = numDelegates(valueChanged) - 1;
            Bomb.unless(listeners == 0, () => "requireNoListeners called when there are " + listeners + " listeners on " + this);
        }

        class SpudTransformer<TARGET, SOURCE> : Spud<TARGET> {
            readonly Spud<SOURCE> spud;
            readonly Converter<SOURCE, TARGET> transformer;

            public SpudTransformer(Spud<SOURCE> spud, Converter<SOURCE, TARGET> transform) : base(spud.manager) {
                this.spud = dependsOn(spud);
                transformer = transform;
            }

            protected override TARGET calculate() {
                return transformer(spud[0]);
            }
        }

        public Spud<K> constant<K>(K value) {
            return new RootSpud<K>(manager, value);
        }

        public string last10() {
            if (count() == 0) return "EMPTYSPUD";
            string[] message = { "" };
            var lastIndex = Math.Min(10, count()) - 1;
            Converter<T, string> stringify = o => o is DateTime ? ymdHuman(date(o.ToString())) : o.ToString();
            zeroTo(lastIndex, i => message[0] += stringify(this[i]) + ", ");
            message[0] += stringify(this[lastIndex]);
            if (count() > 10) message[0] += ", ...";
            return message[0];
        }

        public void onLast(int window, Action<T> onEach) {
            if (window > count()) window = count();
            zeroTo(window, i => onEach(this[i]));                
        }

        public bool hasNaN(int window) {
            var hasNaN = false;
            onLast(window, i => hasNaN |= Equals(i, double.NaN));                
            return hasNaN;
        }

        public bool hasInfinity(int window) {
            var hasInfinity = false;
            onLast(window, d => hasInfinity |= Equals(d, double.PositiveInfinity) | Equals(d, double.NegativeInfinity) );                
            return hasInfinity;
        }


        public T[] toArray() {
            requireAlive();
            if(isDirty()) updateAndClean();
            return t.ToArray();
        }

        public T[] toArray(int size) {
            requireAlive();
            if(isDirty()) updateAndClean();
            return size >= count() ? toArray() : t.GetRange(t.Count - size, size).ToArray();
        }

        
        public Spud<T> lagged(int lag) {
            return new LaggedSpud<T>(this, lag);
        }

        public T lookupFromStartOfTime(int index) {
            return this[count() - index - 1];
        }

        public IEnumerator<T> GetEnumerator() {
            requireAlive();
            if(isDirty()) updateAndClean();
            return reverse(t).GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator() {
            return GetEnumerator();
        }

    }

    internal class WindowSpud<T>: Spud<T[]> {
        readonly int window;
        readonly Spud<T> values;

        public WindowSpud(Spud<T> values, int window) : base(values.manager) {
            this.window = window;
            this.values = dependsOn(values);
        }

        protected override T[] calculate() {
            return values.count() <= window ? null : values.toArray(window);
        }
        
    }

    public class LaggedSpud<T> : Spud<T> {
        readonly int lag;
        readonly Spud<T> values;

        public LaggedSpud(Spud<T> values, int lag) : base(values.manager) {
            this.lag = lag;
            this.values = dependsOn(values);
        }

        protected override T calculate() {
            return values.count() <= lag ? default(T) : values[lag];
        }
    }

    public class DiffSpud : Spud<double> {
        readonly Spud<double> values;
        readonly int lag;

        public DiffSpud(Spud<double> values, int lag) : base(values.manager) {
            this.lag = lag;
            this.values = dependsOn(values);            
            Bomb.unless(lag >= 1, () => "lag should be >= 1");
        }
        
        protected override double calculate() {            
            return values.count() <= lag ? double.NaN : values - values[lag];
        }
    }

    public class LogSpud : Spud<double> {
        readonly Spud<double> values;        

        public LogSpud(Spud<double> values) : base(values.manager) {            
            this.values = dependsOn(values);                        
        }
        
        protected override double calculate() {            
            return values <=0 ? double.NaN : Math.Log(values);
        }
    }
}