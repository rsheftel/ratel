namespace Q.Trading {
    public abstract class SingleSystem<T> : System where T : SystemKey {
        readonly T key_;

        protected SingleSystem(QREBridgeBase bridge, T key) : base(bridge) {
            key_ = key;
        }

        public T key() {
            return key_;
        }

    }
}