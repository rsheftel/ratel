using System;

namespace Q.Spuds.Core {
    public class CrossOverSpud<T> : Spud<CrossOverStatus> where T : IComparable<T> {
        readonly Spud<T> spud1;
        readonly Spud<T> spud2;

        public CrossOverSpud(Spud<T> spud1, Spud<T> spud2) : base(spud1.manager) {
            this.spud1 = dependsOn(spud1);
            this.spud2 = dependsOn(spud2);
        }

        public CrossOverSpud(Spud<T> spud1, T t) : this(spud1, spud1.constant(t)) {}

        protected override CrossOverStatus calculate() {
            if (spud1[0].CompareTo(spud2[0]) < 0) return CrossOverStatus.BELOW;
            if (spud1[0].CompareTo(spud2[0]) > 0) return CrossOverStatus.ABOVE;
            if(spud1.count() == 1 || spud2.count() == 1) return CrossOverStatus.NONE;
            return this[1];
        }

        public bool crossedAbove() {
            if(count() < 2) return false;
            return this[0] == CrossOverStatus.ABOVE && this[1] == CrossOverStatus.BELOW;
        }

        public bool crossedBelow() {
            if(count() < 2) return false;
            return this[0] == CrossOverStatus.BELOW && this[1] == CrossOverStatus.ABOVE;
        }

        public bool crossed() {
            return crossedAbove() || crossedBelow();
        }
    }

    public enum CrossOverStatus { NONE, BELOW, ABOVE };
}