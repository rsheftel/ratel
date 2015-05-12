using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using Q.Util;
using systemdb.metadata;
using O=Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestSystemDeactivation : OneSystemTest<IndependentSymbolSystems<DeactivationRecorder>> {
        static readonly Symbol symbol1 = new Symbol("SYM1");
        static readonly Symbol symbol2 = new Symbol("SYM2");
        static readonly Dictionary<Symbol, Bar> bars = new Dictionary<Symbol, Bar> {
            { symbol1, new Bar(1, 1, 1, 1) },
            { symbol2, new Bar(1, 1, 1, 1) }
        };
        static readonly bool[] activated = new[] {false};
        static readonly Predicate condition = (() => activated[0]);

        public override void setUp() {
            base.setUp();
            activated[0] = false;
        }

        protected override void initializeSymbols() {
            var zero = new java.lang.Integer(0);
            MarketTable.MARKET.insert("SYM1", "TEST", new java.lang.Double(0.0), "23:59:59", zero);
            MarketTable.MARKET.insert("SYM2", "TEST", new java.lang.Double(0.0), "23:59:59", zero);
            base.initializeSymbols();
        }

        protected override SystemArguments arguments() {
            return new SystemArguments(O.list(symbol1, symbol2), parameters());
        }

        protected override int leadBars() {
            return 0;
        }

        [Test]
        public void testBars() {
            var system1 = system().systems_[symbol1];
            var system2 = system().systems_[symbol2];
            bar();
            allBarMethodsCalled(system1, system2);
            Aborts(() => system2.deactivateAndStop(condition));
            bar();
            IsTrue(system1.newBarCalled());
            IsTrue(system1.onCloseCalled());
            IsFalse(system2.newBarCalled());
            IsFalse(system2.onCloseCalled());
            activate();
            bar();
            allBarMethodsCalled(system1, system2);
            Aborts(() => system2.deactivateAndStop(condition));
            IsFalse(system2.isActive());
            bar();
            IsTrue(system2.isActive());
        }

        [Test]
        public void testTicks() {
            bar();
            var system1 = system().systems_[symbol1];
            var system2 = system().systems_[symbol2];
            correctTickMethodsCalled(system1, system2);
            Aborts(() => system2.deactivateAndStop(condition));
            tick(symbol1);
            IsTrue(system1.newTickCalled());
            IsFalse(system2.newTickCalled());
            tick(symbol2);
            IsFalse(system1.newTickCalled());
            IsFalse(system2.newTickCalled());
            activate();
            // for now, if you deactivate on a tick, you need to wait until the next bar to get back in.  consider relaxing...
            bar();
            correctTickMethodsCalled(system1, system2);
        }

        static void activate() {
            activated[0] = true;
        }

        [Test]
        public void testOrders() {
            bar();
            placeOrder();
            hasOrders(1);
            system().cancelAllOrders();
            system1().deactivate(condition);
            Bombs(placeOrder, "deactivated");
        }

        [Test]
        public void testChaining() {
            IsTrue(isActive());
            var another = new[] {false};
            system1().deactivate(() => another[0]);
            IsFalse(isActive());
            system1().deactivate(condition);
            IsFalse(isActive());
            another[0] = true;// cleared hurdle one
            bar();
            IsFalse(isActive()); 
            activate(); // cleared hurdle two
            another[0] = false; // reverse hurdle one, shouldn't matter, condition was met for a bar, it's cleared
            bar();
            IsTrue(isActive());
            
        }

        static void Aborts(Action shouldAbort) {
            try {
                shouldAbort();
                Fail();
            } catch(AbortBar) {}
        }

        bool isActive() {
            return system1().isActive();
        }

        DeactivationRecorder system1() {
            return system().systems_[symbol1];
        }

        void placeOrder() {
            system().systems_[symbol1].placeOrder(symbol1.buy("asdf", market(), 1, oneBar()).placed());
        }

        void correctTickMethodsCalled(DeactivationRecorder system1, DeactivationRecorder system2) {
            tick(symbol1);
            IsTrue(system1.newTickCalled());
            IsFalse(system2.newTickCalled());
            tick(symbol2);
            IsFalse(system1.newTickCalled());
            IsTrue(system2.newTickCalled());
        }

        void tick(Symbol symbol) {
            bridge().processTick(symbol, new Tick(1, 1, O.now()));
        }

        static void allBarMethodsCalled(DeactivationRecorder system1, DeactivationRecorder system2) {
            IsTrue(system1.newBarCalled());
            IsTrue(system1.onCloseCalled());
            IsTrue(system2.newBarCalled());
            IsTrue(system2.onCloseCalled());
        }

        void bar() {
            bridge().processClose(bars);
            bridge().processBar(bars);
        }
    }
    public class GetsInAndDeactivates : EmptySystem {
        public GetsInAndDeactivates(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {}
        protected override void onNewBar() {
            placeOrder(symbol.buy("get some", market(), 10, oneBar()));
            deactivateAndStop(() => false);
        }
    }

    [TestFixture]
    public class TestSystemDeactivationWithPosition : OneSymbolSystemTest<GetsInAndDeactivates> {
        protected override int leadBars() { return 0; }
        protected override SystemArguments arguments() {
            return new SystemArguments(symbol(), parameters());
        }
        
        [Test] public void testSomething() {
            processBar(20, 20, 20, 20);
            fill(0, 19);
            Bombs(() => processBar(20, 20, 20, 20), "failed@0", "can't deactivate with existing position");
        }
    }

    public class DeactivationRecorder : SymbolSystem {
        bool newBarCalled_;
        bool newTickCalled_;
        bool onCloseCalled_;

        public DeactivationRecorder(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {}
        protected override void onFilled(Position position, Trade trade) {}

        protected override void onNewBar() {
            newBarCalled_ = true;
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            newTickCalled_ = true;
        }

        public override bool runOnClose() { return true; }
        public override bool runOnNewTick() { return true; }
        protected override void onClose() {
            onCloseCalled_ = true;
        }

        public bool newBarCalled() {
            var result = newBarCalled_;
            newBarCalled_ = false;
            return result;
        }

        public bool newTickCalled() {
            var result = newTickCalled_;
            newTickCalled_ = false;
            return result;
        }

        public bool onCloseCalled() {
            var result = onCloseCalled_;
            onCloseCalled_ = false;
            return result;
        }
    }
}
