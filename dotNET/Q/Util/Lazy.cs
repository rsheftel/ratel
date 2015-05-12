namespace Q.Util {
    public delegate T Creator<T>();
    public class Lazy<T> {
        readonly Creator<T> initialize;
        T value_;
        bool initialized;
        bool initializing;
        readonly object initializerLock = new object();

        public Lazy(Creator<T> initialize) {
            this.initialize = initialize;
        }

        public static implicit operator T(Lazy<T> s) {
            return s.initializedValue();
        }

        public T initializedValue() {
            lock(initializerLock) {
                requireNotInitializing();
                if (!initialized) {
                    initializing = true;
                    try {
                        value_ = initialize();
                        LogC.verbose(() => "initialized lazy to " + value_);
                    } finally {
                        initializing = false;
                    }
                    initialized = true;
                }
                return value_;
            }
        }

        void requireNotInitializing() {
            Bomb.when(initializing, () => "use of lazy in initialize would cause infinite loop");
        }

        public void clear() {
            requireNotInitializing();
            initialized = false;
            value_ = default(T);
        }

        public void overrideValueForTest(T t) {
            requireNotInitializing();
            initialized = true;
            value_ = t;
        }
    }
}