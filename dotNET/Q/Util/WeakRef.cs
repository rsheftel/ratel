using System;
using System.Collections;
using System.Collections.Generic;

namespace Q.Util {
    public class WeakRef<T> : Objects where T : class {
        readonly WeakReference reference;

        public WeakRef(T t) {
            reference = new WeakReference(t);
        }

        public bool isAlive() {
            return reference.IsAlive;
        }

        public static implicit operator T(WeakRef<T> r) {
            return r.value();
        }

        public T value() {
            Bomb.unless(isAlive(), () => "tried to access GC'd value of a weakReference");
            return (T) reference.Target;
        }

        public List<T> safeValue() {
            var value = (T) reference.Target;
            return value != null ? new List<T> {value} : new List<T>();
        }

        public bool Equals(WeakRef<T> obj) {
            return reference.Target.Equals(obj.reference.Target);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (WeakRef<T>) && Equals((WeakRef<T>) obj);
        }

        public override int GetHashCode() {
            var safe = safeValue();
            return isEmpty(safe) ? 0 : safe[0].GetHashCode();
        }
    }

    public class WeakRefDictionary<K, V> : Objects where K : class {
        readonly Dictionary<WeakRef<K>, V> refs = new Dictionary<WeakRef<K>, V>();

        public bool Remove(K key) {
            lock (refs) return refs.Remove(new WeakRef<K>(key));
        }

        public V this[K key] {
            get { lock(refs) return refs[new WeakRef<K>(key)]; }
            set { lock(refs) refs[new WeakRef<K>(key)] = value; }
        }

        public ICollection<K> Keys {
            get { return collect(liveKeys(), r => r.safeValue()); }
        }

        IEnumerable<WeakRef<K>> liveKeys() {
            lock(refs) {
                each(reject(refs.Keys, k => k.isAlive()), k => refs.Remove(k));
                return list<WeakRef<K>>(refs.Keys);
            }
        }

        public void clear() {
            lock (refs) refs.Clear();
        }

        public bool ContainsKey(K key) {
            lock (refs) return refs.ContainsKey(new WeakRef<K>(key));
        }
    }

    public class WeakRefList<T> : Objects, IEnumerable<T> where T : class {
        readonly List<WeakRef<T>> refs = new List<WeakRef<T>>();

        public void Add(T t) {
            refs.Add(new WeakRef<T>(t));
        }

        IEnumerator<T> IEnumerable<T>.GetEnumerator() {
            return collect(liveRefs(), r => r.safeValue()).GetEnumerator();
        }

        List<WeakRef<T>> liveRefs() {
            refs.RemoveAll(r => !r.isAlive());
            return copy(refs);
        }

        public IEnumerator GetEnumerator() {
            return liveRefs().GetEnumerator();
        }

        public bool remove(T t) {
            var matching = matches(t);
            if(isEmpty(matching)) return false;
            refs.Remove(first(matching));
            return true;
        }

        public bool contains(T t) {
            return !isEmpty(matches(t));
        }

        List<WeakRef<T>> matches(T t) {
            return accept(liveRefs(), r => r.value().Equals(t));
        }

        public int count() {
            return liveRefs().Count;
        }

        public void clear() {
            refs.Clear();
        }
    }
}