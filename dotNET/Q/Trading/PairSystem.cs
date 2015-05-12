using System;
using System.Collections.Generic;
using Q.Trading.Results;
using Q.Util;

namespace Q.Trading {
    public abstract class PairSystem : SingleSystem<Pair> {
        protected readonly Pair pair;

        protected PairSystem(QREBridgeBase bridge, Pair pair) : base(bridge, pair) {
            this.pair = pair;
        }

        public static T create<T>(string className, QREBridgeBase bridge, Pair pair) where T : PairSystem {
            var type = Type.GetType(className, true, false);
            var c = type.GetConstructor(new[] { typeof(QREBridge<T>), typeof(Pair) });
            Bomb.ifNull(c, () => "no constuctor matching qrebridge of type, symbol on class " + className);
            return (T) c.Invoke(new object[] { bridge, pair });
        }

        public override void addCollectorsTo(SystemArguments arguments, Dictionary<Collectible, StatisticsCollector> collectors) {
            collectors[this] = new StatisticsCollector(arguments);
        }

        public override string name {
            get { return pair.name(); }
        }

        internal override IEnumerable<Position> allPositions() {
            return positions();
        }

        public override IEnumerable<Order> allOrders() {
            return collect(pair.symbols(), symbol => orders(symbol));
        }

        protected IEnumerable<Position> positions() {
            return collect(pair.symbols(), symbol => positions(symbol));
        }
    }
}