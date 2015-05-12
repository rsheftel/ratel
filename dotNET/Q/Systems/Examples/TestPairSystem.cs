using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestPairSystem : OneSystemTest<MultiPairSystem<SimplePairSystem, SomeCombination>> {
        static readonly Symbol ty = new Symbol("RE.TEST.TY.1C", 1000);
        static readonly Symbol tu = new Symbol("RE.TEST.TU.1C", 1000);

        [Test]
        public void testCanCreateFromDb() {
            LogC.info("full class name = " + typeof (MultiPairSystem<SimplePairSystem, SomeCombination>).FullName);
            const string name = "Q.Trading.MultiPairSystem`2[[Q.Systems.Examples.SimplePairSystem],[Q.Systems.Examples.SomeCombination]]";
            var type = Type.GetType(name, true, false);
            AreEqual(typeof (MultiPairSystem<SimplePairSystem, SomeCombination>), type);
        }

        [Test]
        public void testBars() {
            bar(100.0, 50.0);
            hasOrders(ty.buy("go long", market(), 1, oneBar()), tu.sell("go long", market(), 1, oneBar()));
            fill(ty, 0, 90.0);
            fill(tu, 0, 60.0);
            bar(130.0, 120.0);
            AreEqual(-20 * 1000, bridge().statistics().netProfit());
        }

        [Test]
        public void testExitAndDeactivate() {
            systemTy().beTestDeactivated = true;
            bar(100, 50);
            noOrders();
            systemTy().beTestDeactivated = false;
            bar(10, 10);
            hasOrders(ty.buy("go long", market(), 1, oneBar()), tu.sell("go long", market(), 1, oneBar()));
            fill(tu, 0, 60.0);
            fill(ty, 0, 90);
            bar(130.0, 120.0);
            hasOrders(ty.sell("no holding", market(), 1, oneBar()), tu.buy("no holding", market(), 1, oneBar()));
        }

        SimplePairSystem systemTy() {
            return O.the(system().systems(ty));
        }

        protected override void initializeSymbols() {
            ty.setSlippageForTest(0);
            tu.setSlippageForTest(0);
            base.initializeSymbols();
        }

        void bar(double tyClose, double tuClose) {
            processBar(new Dictionary<Symbol, Bar>{{ty, new Bar(tyClose, tyClose, tyClose, tyClose)}, {tu, new Bar(tuClose, tuClose, tuClose, tuClose)}});
        }

        protected override SystemArguments arguments() {
            return new SystemArguments(O.list(ty, tu), parameters());
        }

        protected override int leadBars() {
            return 0;
        }
    }

    public class SimplePairSystem : PairSystem {
        internal bool beTestDeactivated;
        public SimplePairSystem(QREBridgeBase bridge, Pair pair) : base(bridge, pair) {}

        public override DateTime onCloseTime() {return default(DateTime);}
        protected override void onNewTick(Symbol symbol, Bar partialBar, Tick tick) {}
        protected override void onNewBar(Dictionary<Symbol, Bar> b) {
            if (beTestDeactivated)
                deactivateAndStop(() => !beTestDeactivated);
            if (hasPosition()) each(positions(), position => placeOrder(position.exit("no holding", market(), oneBar())));
            else placeOrders(pair.buy("go long", market(), market(), 1, oneBar() ));
        }

        protected override void onFilled(Position position, Trade trade) {}
        protected override void onClose(Dictionary<Symbol, Bar> current) {}
    }

    public class SomeCombination : PairGenerator {
        public SomeCombination(SystemArguments arguments) : base(arguments) {}

        protected internal override IEnumerable<Pair> pairs() {
            return list(new Pair(first(arguments.symbols), second(arguments.symbols)));
        }
    }
}
