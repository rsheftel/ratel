using System;

namespace Q.Spuds.Core {

    public abstract class ComparableSpud<T> : Spud<T> where T : IComparable<T> {

        protected ComparableSpud(SpudManager manager) : base(manager) {}

        public Spud<T> highest(int period) {
            return new MinMax<T>(this, period, MinMax<T>.MAX);
        }

        public Spud<T> lowest(int period) {
            return new MinMax<T>(this, period, MinMax<T>.MIN);
        }   
        
        public Spud<T> highest() {
            return highest(int.MaxValue);
        }

        public Spud<T> lowest() {
            return lowest(int.MaxValue);
        }

        public Spud<T> minMax(int direction) {
            return new MinMax<T>(this, int.MaxValue, direction);
        }

    }

    public class MinMax<T> : ComparableSpud<T> where T : IComparable<T> {
        public static readonly int MAX = 1;
        public static readonly int MIN = -1;

        readonly Spud<T> spud;
        readonly int period;
        readonly int direction;

        public MinMax(Spud<T> spud, int period, int direction) : base(spud.manager) {
            this.spud = dependsOn(spud);
            this.period = period;
            this.direction = direction;
        }

        protected override T calculate() {
            if(!isInitialized()) return spud[0];
            var challenger = spud[0];
            if (count() < 2) return spud[0];
            var champion = this[1];
            if(challenge(champion, challenger)) return challenger;
            if(period >= spud.count()) return champion;
            if(!champion.Equals(spud[period])) return champion;

            champion = challenger;
            for(var i = 1; i < period; i++) {
                challenger = spud[i];
                if (challenge(champion, challenger)) champion = challenger;
            }
            return champion;
        }

        bool challenge(T champion, T challenger) {
            return challenger.CompareTo(champion) * direction > 0;
        }
    }
}