using Q.Util;

namespace Q.Spuds.Core {
    public class RootSpud<T> : Spud<T>, IRootSpud {
        public RootSpud(SpudManager manager) : base(manager) {}
        public RootSpud(SpudManager manager, T initialValue) : base(manager) {
            set(initialValue);
        }

        protected sealed override T calculate() {
            throw Bomb.toss("cannot calculate on a RootSpud");
        }

        public sealed override void updateThyself() {}

        public new void set(T value) {
            base.set(value);
        }
    }

    public interface IRootSpud {}


}