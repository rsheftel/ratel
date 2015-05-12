using System;
using System.Collections.Generic;
using file;
using Q.Messaging;
using Q.Recon;
using Q.Simulator;
using Q.Trading;
using Q.Util;
using systemdb.metadata;
using Symbol=Q.Trading.Symbol;
using O=Q.Util.Objects;

namespace Q.Systems {
    public abstract class OneSystemTest<S> : DbTestCase where S : Trading.System {

        QREBridge<S> bridge_;
        protected int systemId;
        protected LiveSystem liveSystem;
        FakeBarLoader loader;
        bool barsLoadedThisBar;
        private Dictionary<Symbol, Bar> lastBar;

        public override void setUp() {
            base.setUp();

            liveSystem = fakeLiveSystem(parameters(), runInNativeCurrency());
            systemId = liveSystem.id();
            initializeSymbols();
            bridge_ = new QREBridge<S>(arguments());
            loader = new FakeBarLoader(bridge_.arguments().symbols);
            loader.setSpudManager(bridge_.manager);
            barsLoadedThisBar = false;
            
            noOrders();
        }

        protected virtual void initializeSymbols() {}
        protected virtual bool runInNativeCurrency() {
            return false;
        }

        public static LiveSystem fakeLiveSystem(Parameters parameters, bool runInNativeCurrency) {
            var liveSystemTemp = new LiveSystem(new Siv("TestSystem1", "daily", "1.0"), new Pv("Slow"));
            liveSystemTemp.setQClassName(typeof(S).FullName);
            liveSystemTemp.populateDetailsIfNeeded(runInNativeCurrency);
            liveSystemTemp.populateTagIfNeeded("QF.Example", false);
            parameters.insertInto(liveSystemTemp);
            return liveSystemTemp;
        }

        public override void tearDown() {
            new QDirectory("temp").destroyIfExists();
            bridge_.monitor().shutdown();
            base.tearDown();
        }


        protected abstract SystemArguments arguments();
        protected abstract int leadBars();

        protected List<Order> orders(Symbol symbol) {
            return system().orders(symbol);
        }

        protected TradeMonitor monitor() {
            return bridge().monitor();
        }

        protected void fill(Symbol symbol, int index, double price) {
            fill(orders(symbol)[index], price);
        }

        protected void fill(Order order, double price) {
            bridge().fill(order, price);
        }

        protected virtual void processBar(Dictionary<Symbol, Bar> symbolBars) {
            if(!barsLoadedThisBar) {
                loader.add(symbolBars);
                barsLoadedThisBar = false;
            }
            bridge().setCurrentSlippage(loader.currentSlippages(O.first(symbolBars.Values).time));
            bridge().processBar(symbolBars);
        }
        
        protected void processClose(Converter<Dictionary<Symbol, Bar>, Dictionary<Symbol, Bar>> nextBar) {
            if(lastBar != null) processBar(lastBar);
            lastBar = nextBar(lastBar);
            processClose(lastBar);
        }

        protected void processClose(Dictionary<Symbol, Bar> bar) {
            loader.add(bar);
            barsLoadedThisBar = true;
            bridge().setCurrentSlippage(loader.currentSlippages(Objects.first(bar.Values).time));
            bridge().processClose(bar);
        }

        protected void processTick(Symbol symbol, double price, DateTime time) {
            bridge().processTick(symbol, new Tick(price, 1, time));
        }

        protected QREBridge<S> bridge() {
            return bridge_;
        }

        protected virtual DateTime nextTime() {
            return lastBar == null ? date("1978-05-08") : Objects.first(lastBar.Values).time.AddDays(1);
        }

        protected void noOrders() {
            LogC.infoOnce("expected no orders");
            IsTrue(O.isEmpty(bridge().allOrders()));
        }

        protected void hasOrders(int expected) {
            LogC.info("expected " + expected + " orders");
            HasCount(expected, O.list(bridge().allOrders()));
        }

        protected void hasOrders(Symbol symbol, params Order[] expecteds) {
            hasOrders(orders(symbol), expecteds);
        }

        protected void hasOrders(params Order[] expecteds) {
            hasOrders(O.list(bridge().allOrders()), expecteds);
        }

        public static void hasOrders(List<Order> actuals, params Order[] expecteds) {
            LogC.info("expected orders \n" + Objects.toShortString(expecteds));
            Objects.each(expecteds, o => o.placed());
            Producer<string> actualExpected = () => "\nTEST EXPECTED:\n" + Objects.toShortString(expecteds)+"\nSYSTEM PRODUCED:\n" + Objects.toShortString(actuals);
            if (actuals.Count > expecteds.GetLength(0)) Bomb.toss("system produced more orders than test expected! " + actualExpected());
            if (actuals.Count < expecteds.GetLength(0)) Bomb.toss("system did not produce enough orders! " + actualExpected());
            try {
                requireOrdersMatch(expecteds, actuals);
            } catch (Exception e) {
                Bomb.toss("order mismatch: " + actualExpected(), e);
            }
        }

        static void requireOrdersMatch(IEnumerable<Order> expecteds, IEnumerable<Order> actuals) {
            Objects.each(expecteds, actuals, (expected, actual) => {
                if (expected.Equals(Order.ANY)) return;
                if (expected.Equals(actual)) return;
                var details = "";
                if ((expected + "").Equals(actual + "")) 
                    details += "\nDETAILS MISMATCH: expected\n\t" + expected.details.ToLongString() + "\nMISMATCH\n\t" + actual.details.ToLongString();
                Bomb.toss("\nORDER MISMATCH: expected \n\t" + expected + "\nMISMATCH\n\t" + actual + details);
            });
        }

        protected S system() {
            return bridge_.system;
        }

        protected void noPositions() {
            IsTrue(O.isEmpty(system().allPositions()));
        }
        
        protected void hasPosition(Symbol symbol, double amount) {
            AreEqual(amount, position(symbol).amount);
        }

        protected Position position(Symbol symbol) {
            return system().position(symbol);
        }

        protected static OrderDetails stop(double stopLevel) { return Order.stop(stopLevel);  }
        protected static OrderDetails limit(double limitLevel) { return Order.limit(limitLevel); }
        protected static OrderDetails stopLimit(double level) { return stopLimit(level, level); }
        protected static OrderDetails stopLimit(double stopLevel, double limitLevel) { return Order.stopLimit(stopLevel, limitLevel); }
        protected static OrderDetails protectiveStop(double level) { return protectiveStop(level, level); }
        protected static OrderDetails protectiveStop(double stopLevel, double limitLevel) { return Order.protectiveStop(stopLevel, limitLevel); }
        protected static OrderDetails market() { return Order.market(); }
        protected static OrderDuration fillOrKill() { return FillOrKill.FILL_KILL; }
        protected static OrderDuration oneBar() { return OneBar.ONE; }
        protected static OrderDuration oneDay() { return new OneDay(); }
        protected static OrderDuration onTheClose() { return OnClose.ON_CLOSE; }

        protected static PublishCounter positionPublishCounter(Symbol symbol) {
            return new PublishCounter(OrderTable.DEFAULT_PREFIX + ".TestSystem1.1.0.daily.Slow." + symbol.name + ".optimalPosition");
        }

        protected virtual Parameters parameters() {
            return new Parameters {
                {"systemId", systemId},
                {"LeadBars", leadBars()},
                {"RunMode", ((int) RunMode.LIVE)}
            };
        }
    }
}