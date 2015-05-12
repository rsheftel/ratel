using System;
using System.Collections.Generic;
using Q.Util;

namespace Q.Trading {
    public abstract class PairGenerator : Objects {
        protected readonly SystemArguments arguments;
        
        protected PairGenerator(SystemArguments arguments) {
            this.arguments = arguments;
        }

        public static T create<T>(Type type, SystemArguments arguments) {
            var c = type.GetConstructor(new[] {typeof(SystemArguments)});
            return (T) c.Invoke(new object[] { arguments });
        }

        public void eachPair(Action<Pair> onPair) {
            each(pairs(), onPair);
        }

        protected internal abstract IEnumerable<Pair> pairs();
    }
}