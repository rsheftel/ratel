using NUnit.Framework;
using Q.Systems;
using Q.Systems.Examples;
using Q.Trading;
using Q.Util;
using systemdb.metadata;
using O=Q.Util.Objects;

namespace Q.Simulator {
    [TestFixture]
    public class TestPaperBroker : DbTestCase {
        static readonly Symbol SYMBOL = new Symbol("RE.TEST.TY.1C", 1000);
        LiveSystem liveSystem;
        Simulator simulator;
        TestSystem system;
        FakeBarLoader data;
        static readonly Position ONE_LONG = new Position(SYMBOL, 1, Direction.LONG);
        static readonly Position ONE_SHORT = new Position(SYMBOL, 1, Direction.SHORT);

        public override void setUp() {
            base.setUp();
            emailer.allowMessages();
            liveSystem = OneSystemTest<TestSystem>.fakeLiveSystem(new Parameters {{"LeadBars", 0.0}}, false);
            data = new FakeBarLoader(SYMBOL);
            simulator = data.simulator(new SystemArguments(SYMBOL, parameters(liveSystem)), "TEST");
            system = simulator.theSymbolSystem<TestSystem>(SYMBOL); 
            SYMBOL.setCloseTimeForTest("14:00:00", 300);
            bar(0, 0, 0, 0);
        }

        [Test]
        public void testStopSell() {
            var sshigh = sell("in", new Stop(3.5));
            bar(1, 4, 1, 3, ONE_SHORT);
            hasNewTrades(new Trade(sshigh, 1, 1, slippage(), 1));
            exit("not quite out", new Stop(1.001), OneBar.ONE);
            bar(1, 1, 1, 1, ONE_SHORT);
            noNewTrades();
            var getOut  = exit("out", new Stop(-1), OneBar.ONE);
            bar(0, 1, -1, 1);
            hasNewTrades(new Trade(getOut, 0, 1, slippage(), 1));
        }

        double slippage() {
            return system.slippage(SYMBOL);
        }

        [Test]
        public void testLimitSell() {
            var sshigh = placeOrder(SYMBOL.sell("in", new Limit(3.5), 1, OneBar.ONE));
            bar(1, 4, 1, 3, ONE_SHORT);
            hasNewTrades(new Trade(sshigh, 3.5, 1, slippage(), 1));
            exit("not quite out", new Limit(0.99), OneBar.ONE);
            bar(1, 1, 1, 1, ONE_SHORT);
            noNewTrades();
            var getOut  = exit("out", new Limit(1), OneBar.ONE);
            bar(0, 1, -1, 1);
            hasNewTrades(new Trade(getOut, 0, 1, slippage(), 1));
        }

        Order exit(string s, OrderDetails stop, OrderDuration duration) {
            return placeOrder(system.position().exit(s, stop, duration));
        }

        [Test]
        public void testStop() {
            buy("in2", new Stop(3.5), OneBar.ONE);
            var in1 = buy("in", new Stop(3), OneBar.ONE);
            bar(1, 4, 2, 3, ONE_LONG);
            hasNewTrades(new Trade(in1, 3, 1, slippage(), 1));
            bar(1, 4, 2, 3, ONE_LONG);
            noNewTrades();
            exit("out", new Stop(0), OneBar.ONE);
            bar(1, 4, 2, 3, ONE_LONG);
            noNewTrades();
            bar(-1, -1, -1, -1, ONE_LONG);
            var out1 = exit("out", new Stop(0), OneBar.ONE);
            bar(-1, -1, -1, -1);
            hasNewTrades(new Trade(out1, -1, 1, slippage(), 1));
            var in2 = buy("in", new Stop(3), OneBar.ONE);
            tick(3.5, ONE_LONG);
            hasNewTrades(new Trade(in2, 3.5, 1, slippage(), 1));
            var in3 = scaleUp("in", new Stop(4), OneBar.ONE);
            tick(3.99999, ONE_LONG);
            noNewTrades();
            tick(4.001, new Position(SYMBOL, 2, Direction.LONG));
            hasNewTrades(new Trade(in3, 4.001, 1, slippage(), 1));
        }

        [Test]
        public void testStopLimitBuyLive() {
            bar(1,1,1,1);
            var order = buy("in", new StopLimit(2, 1), OneBar.ONE);
            tick(0.99);
            tick(1.99);
            tick(2); // stop fires, turning this into a buy limit(1)
            tick(1.001);
            tick(0.99, ONE_LONG);
            hasNewTrades(new Trade(order, 0.99, 1, slippage(), 1)); // now reversed
            var out1 = exit("out", new StopLimit(1, 2), OneBar.ONE);
            tick(1.1, ONE_LONG);
            tick(2.1, ONE_LONG);
            tick(1, ONE_LONG); // stop fires, now a sell limit(2)
            tick(0.99, ONE_LONG);
            tick(1.99, ONE_LONG);
            tick(2.2);
            hasNewTrades(new Trade(out1, 2.2, 1, slippage(), 1));
        }

        [Test]
        public void testStopLimitSellLive() {
            bar(1,1,1,1);
            var order = sell("in", new StopLimit(1, 2));
            tick(1.1);
            tick(1);
            tick(2.1, ONE_SHORT);
            hasNewTrades(new Trade(order, 2.1, 1, slippage(), 1)); // now reversed
            var out1 = exit("out", new StopLimit(2, 1), OneBar.ONE);
            tick(2.1, ONE_SHORT);
            tick(2, ONE_SHORT);
            tick(2.5, ONE_SHORT);
            tick(1);
            hasNewTrades(new Trade(out1, 1, 1, slippage(), 1));
        }

        Order buy(string description, OrderDetails details, OrderDuration duration) {
            return placeOrder(SYMBOL.buy(description, details, 1, duration));
        }

        Order sell(string description, OrderDetails details) {
            return placeOrder(SYMBOL.sell(description, details, 1, OneBar.ONE));
        }

        [Test]
        public void testMarketOnClose() {
            
            var in1 = buy("in", Order.market(), OnClose.ON_CLOSE);
            bar(1, 4, 2, 3, ONE_LONG);
            hasNewTrades(new Trade(in1, 3, 1, slippage(), 1));
            var out1 = exit("out", Order.market(), OnClose.ON_CLOSE);
            O.freezeNow("2009/01/01 13:59:59");
            tick(4.5, ONE_LONG);
            noNewTrades();
            O.freezeNow("2009/01/01 14:00:00");
            tick(3.5);
            hasNewTrades(new Trade(out1, 3.5, 1, slippage(), 1));
            buy("in2", Order.market(), OnClose.ON_CLOSE);
            tick(3.5);
            noNewTrades();
        }

        [Test]
        public void testNoStopOnClose() {
            Bombs(() => buy("in", Order.stop(123), OnClose.ON_CLOSE), "can't place STOP");
            Bombs(() => buy("in", Order.stopLimit(123), OnClose.ON_CLOSE), "can't place STOP_LIMIT");
            Bombs(() => buy("in", Order.protectiveStop(123), OnClose.ON_CLOSE), "can't place PROTECTIVE_STOP");
        }

        [Test]
        public void testMarket() {
            var in1 = buy("in", Order.market(), OneBar.ONE);
            bar(1, 4, 2, 3, ONE_LONG);
            hasNewTrades(new Trade(in1, 1, 1, slippage(), 1));
            var out1 = exit("out", Order.market(), OneBar.ONE);
            tick(4.5);
            hasNewTrades(new Trade(out1, 4.5, 1, slippage(), 1));
        }

        [Test]
        public void testLimit() {
            var in1 = buy("in", new Limit(3), OneBar.ONE);
            bar(1, 4, 2, 3, ONE_LONG);
            hasNewTrades(new Trade(in1, 1, 1, slippage(), 1));
            bar(1, 4, 2, 3, ONE_LONG);
            hasNewTrades();
            exit("out", new Limit(4.0001), OneBar.ONE);
            bar(1, 4, 2, 3, ONE_LONG);
            hasNewTrades();
            bar(5, 5, 5, 5, ONE_LONG);
           exit("out2", new Limit(5.5), OneBar.ONE);
            var out1 = exit("out", new Limit(5), OneBar.ONE);
            bar(4, 7, 3, 5);
            hasNewTrades(new Trade(out1, 5, 1, slippage(), 1));
            var in2 = buy("in", new Limit(4), OneBar.ONE);
            tick(3.5, new Position(SYMBOL, 1, Direction.LONG));
            hasNewTrades(new Trade(in2, 3.5, 1, slippage(), 1));
            var in3 = scaleUp("in", new Limit(3), OneBar.ONE);
            tick(2.5, new Position(SYMBOL, 2, Direction.LONG));
            hasNewTrades(new Trade(in3, 2.5, 1, slippage(), 1));
        }

        
        [Test]
        public void testLimitOnClose() {
            var in1 = buy("in", new Limit(3.5), OnClose.ON_CLOSE);
            bar(1, 4, 2, 3, ONE_LONG);
            hasNewTrades(new Trade(in1, 3, 1, slippage(), 1));
            bar(1, 4, 2, 3, ONE_LONG);
            hasNewTrades();
            exit("out", new Limit(3.5), OnClose.ON_CLOSE);
            bar(1, 4, 2, 3, ONE_LONG);
            hasNewTrades();
            bar(5, 5, 5, 5, ONE_LONG);
            exit("out2", new Limit(5.5), OnClose.ON_CLOSE);
            var out1 = exit("out", new Limit(4.5), OnClose.ON_CLOSE);
            bar(4, 7, 3, 5);
            hasNewTrades(new Trade(out1, 5, 1, slippage(), 1));

            O.freezeNow("2009/01/01 13:59:59");
            var in2 = buy("in", new Limit(4), OnClose.ON_CLOSE);
            tick(3.5);
            noNewTrades();
            O.freezeNow("2009/01/01 14:00:00");
            tick(3.5, new Position(SYMBOL, 1, Direction.LONG));
            hasNewTrades(new Trade(in2, 3.5, 1, slippage(), 1));
            scaleUp("in", new Limit(3), OnClose.ON_CLOSE);
            tick(2.5, new Position(SYMBOL, 1, Direction.LONG));
            noNewTrades();
        }

        Order scaleUp(string description, OrderDetails details, OrderDuration duration) {
            return placeOrder(system.position().scaleUp(description, details, 1, duration));
        }

        void tick(double d, params Position[] expected) {
            simulator.processTick(new Tick(d, 1, O.now()), SYMBOL);
            hasPositions(expected);
        }

        void hasPositions(Position[] expected) {
            O.each(system.positions(SYMBOL), O.list(expected), (a, b) => a.requireMatches(b));
        }

        Order placeOrder(Order o) {
            system.placeOrder(o);
            return o;
        }

        /** the *new* here is because the simulator clears the trade list by bar */
        void hasNewTrades(params Trade[] expected) {
            AreEqual(O.list(expected), simulator.trades);
        }

        void noNewTrades() {
            IsEmpty(simulator.trades);
        }

        void bar(double open, double high, double low, double close, params Position[] expected) {
            data.add(SYMBOL, open, high, low, close);
            simulator.nextBar();
            hasPositions(expected);
        }

        static Parameters parameters(LiveSystem system) {
            return new Parameters {{"systemId", (double) system.id()}, {"RunMode", (double) RunMode.LIVE}};
        }

        class TestSystem : EmptySystem {
            public TestSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {}

            protected override void onFilled(Position position, Trade trade) {
                cancelAllOrders();
            }

            public override bool runOnClose() {
                return true;
            }
        }
    }
}
