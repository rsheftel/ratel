using System;
using System.Collections.Generic;
using file;
using NUnit.Framework;
using Q.Recon;
using Q.Systems.Examples;
using Q.Trading;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using util;
using Bar=Q.Trading.Bar;
using Market=systemdb.metadata.Market;
using O=Q.Util.Objects;
using Symbol=Q.Trading.Symbol;
using Tick=Q.Trading.Tick;

namespace Q.Simulator {
    [TestFixture]
    public class TestSimulator : DbTestCase {
        static readonly Symbol SYMBOL = new Symbol("RE.TEST.TY.1C", 1000);
        static readonly QDirectory curvesDirectory = new QDirectory("./simulator.curves");
        static readonly Siv SIV = new Siv("TestSystem1", "daily", "1.0");
        static readonly Pv FAST = new Pv("Fast");
        new static readonly LiveSystem LIVE_SYSTEM = new LiveSystem(SIV, FAST);
        static readonly Symbol SYMBOL2 = new Symbol("RE.TEST.TU.1C", 1000);

        [Test]
        public void testCanRunOneBar() {
            var args = arguments(O.list(SYMBOL), LIVE_SYSTEM, RunMode.RIGHTEDGE, typeof(ExampleSymbolSystem));
            var simulator = makeSimulator(args);
            O.zeroTo(6, i => {
                simulator.nextBar();
                noOrders(simulator);
            });
            
            simulator.nextBar();
            hasOrders(simulator); // entry

            simulator.nextBar();
            noOrders(simulator);
            hasPosition(simulator);

            simulator.nextBar();
            hasOrders(simulator); // exit

            simulator.writeCurveFiles(curvesDirectory);
            var written = curvesDirectory.file(LIVE_SYSTEM.liveMarket(SYMBOL.name).fileName() + ".bin");
            AreEqual(9 * 3 * 8, written.size());
        }

        [Test]
        public void testSystemSystemCurveFileGeneration() {
            var args = new SystemArguments(SYMBOL, new Parameters {
                {"systemId", 320978},
                {"Risk", 10000},
                {"RunMode", (double) RunMode.LIVE}
            });
            MsivTable.MSIVS.insert(SYMBOL.name, args.siv());
            var simulator = makeSimulator(args);
            simulator.processBars(30);
            simulator.writeCurveFiles(curvesDirectory);
            var written = curvesDirectory.file(args.liveSystem().fileName(SYMBOL.name) + ".bin");
            AreEqual(30 * 3 * 8, written.size());
        }

        static SystemArguments arguments(IEnumerable<Symbol> symbols, LiveSystem liveSystem, RunMode mode, Type type) {
            O.each(symbols, symbol => MsivTable.MSIVS.insert(symbol.name, liveSystem.siv()));
            liveSystem.setQClassName(type.FullName);
            liveSystem.populateDetailsIfNeeded(false);
            return new SystemArguments(symbols, new Parameters{
                {"LeadBars", 5},
                {"systemId", liveSystem.id()},
                {"RunMode", (double) mode},
                {"lookback", 2}
            });
        }

        [Test]
        public void testOnCloseIntraDay() {
            var siv5Minute = new Siv("TestSystem1", "5minute", "1.0");
            var liveSystem = new LiveSystem(siv5Minute, FAST);
            var args = arguments(O.list(SYMBOL, SYMBOL2), liveSystem, RunMode.RIGHTEDGE, typeof(TestOnCloseSystem));
            var loader = new FakeBarLoader(SYMBOL, SYMBOL2);
            var simulator = loader.simulator(args, OrderTable.prefix);
            var system = simulator.theSymbolSystem<TestOnCloseSystem>(SYMBOL);
            var system2 = simulator.theSymbolSystem<TestOnCloseSystem>(SYMBOL2);

            MarketSessionTable.SESSION.update(SYMBOL.name, Session.DAY, "16:00:00", 360);
            MarketSessionTable.SESSION.update(SYMBOL2.name, Session.DAY, "15:50:00", 360);
            
            Action<int,int> bar = (close, minutesAfter3) => {
                var barTime = Dates.minutesAhead(minutesAfter3, Dates.date("2008/08/08 15:00:00"));
                loader.add(SYMBOL, close, close, close, close, barTime);
                loader.add(SYMBOL2, close, close, close, close, barTime);
                simulator.nextBar();
            };
            Action<bool, bool> closeTriggered = (expected1, expected2) => {
                AreEqual(expected1, system.onCloseTriggered);
                AreEqual(expected2, system2.onCloseTriggered);
                system.onCloseTriggered = false;
                system2.onCloseTriggered = false;
            };
            O.zeroTo(5, i => { 
                bar(i, 15 + i * 5);
                closeTriggered(false, false);
            });
            bar(6, 40); closeTriggered(false, false); // ? format but couldn't resist the parallellism. and terseness.
            bar(7, 45); closeTriggered(false, true);
            bar(8, 50); closeTriggered(false, false);
            bar(9, 55); closeTriggered(true, false);
            bar(10, 60); closeTriggered(false, false);
        }

        static Simulator makeSimulator(SystemArguments args) {
            return new Simulator(args, OrderTable.prefix);
        }

        public override void setUp() {
            base.setUp();
            curvesDirectory.destroyIfExists();
        }
        
        public override void tearDown() {
            base.tearDown();
            curvesDirectory.destroyIfExists();
        }

        [Test]
        public void testCanGoLiveWithoutCloseTime() {
            var liveSystem = LIVE_SYSTEM;
            liveSystem.setQClassName(typeof(ExampleSymbolSystem).FullName);
            liveSystem.populateDetailsIfNeeded(false);
            liveSystem.insertParameter("lookback", "2");
            liveSystem.insertParameter("LeadBars", "0");
            
            MarketSessionTable.SESSION.update(SYMBOL.name, Session.DAY, "NO_CLOSE", 0);
            ExchangeSessionTable.EXCHANGE_SESSION.update(new Market(SYMBOL.name).exchange(), Session.DAY, "NO_CLOSE", 0);
            var args = new SystemArguments(SYMBOL, new Parameters {
                {"systemId", liveSystem.id()},
                {"RunMode", (double) RunMode.LIVE}
            });
            var simulator = makeSimulator(args);
            simulator.processBars(300);
            while (O.hasContent(simulator.allPositions())) simulator.nextBar();
            simulator.goLive();
            var time = simulator.currentDate().AddDays(1);
            O.freezeNow(time);
            noPosition(simulator);
            var current = new Bar(120, 120, 120, 120, time);
            tick(simulator, current, SYMBOL);
            hasPosition(simulator);
            time = time.AddSeconds(1);
            current = current.update(new Tick(118, 1, time));
            tick(simulator, current, SYMBOL);
        }

        [Test]
        public void testIntraDayLiveBars() {
            var siv5Minute = new Siv("TestSystem1", "second", "1.0");
            var liveSystem = new LiveSystem(siv5Minute, FAST);
            liveSystem.insertParameter("LeadBars", "0");
            var args = arguments(O.list(SYMBOL, SYMBOL2), liveSystem, RunMode.LIVE, typeof(EmptySystem));
            var loader = new FakeBarLoader(SYMBOL, SYMBOL2);
            var simulator = loader.simulator(args, OrderTable.prefix);
            try {
                Action<int,int> bar = (close, minutesAfter9) => {
                    var barTime = Dates.minutesAhead(minutesAfter9, Dates.date("2008/09/08 09:00:00"));
                    loader.add(SYMBOL, close, close, close, close, barTime);
                    loader.add(SYMBOL2, close, close, close, close, barTime);
                    simulator.nextBar();
                };
                Action<Symbol, int, string> doTick = (symbol, price, time) => {
                    O.freezeNow("2008/09/08 " + time);
                    tick(simulator, new Bar(0, price, 0, price, O.now()), symbol);
                };
                bar(5, 0);
                O.freezeNow("2008/09/08 09:00:00");
                simulator.goLive();
                doTick(SYMBOL, 26, "09:00:00");
                doTick(SYMBOL2, 28, "09:00:00");
                doTick(SYMBOL, 25, "09:00:00");
                doTick(SYMBOL2, 29, "09:00:00");
                simulator.waitForBar();
                var system = simulator.theSymbolSystem<EmptySystem>(SYMBOL);
                var system2 = simulator.theSymbolSystem<EmptySystem>(SYMBOL2);
                AreEqual(new Bar(26, 26, 25, 25, date("2008/09/08 09:00:00")), system.bar);
                AreEqual(new Bar(28, 29, 28, 29, date("2008/09/08 09:00:00")), system2.bar);
                O.freezeNow("2008/09/08 09:00:01");
                doTick(SYMBOL, 42, "09:00:01");
                doTick(SYMBOL, 57, "09:00:01");
                doTick(SYMBOL, 15, "09:00:01");
                system.placeOrder(SYMBOL.buy("somethin", Order.market(), 1, FillOrKill.FILL_KILL));
                simulator.waitForBar(); // if it blows up in orderFilled here, it filled orders during live intraday bar (incorrect)
                IsTrue(O.isEmpty(system.allPositions())); 
                AreEqual(new Bar(42, 57, 15, 15, date("2008/09/08 09:00:01")), system.bar);
                AreEqual(new Bar(28, 29, 28, 29, date("2008/09/08 09:00:00")), system2.bar);
                AreEqual(3, system.bars.count());
                AreEqual(2, system2.bars.count());
            } finally {
                simulator.clearTimer();
            }
        }

        static void tick(Simulator simulator, Bar current, Symbol symbol) {
            symbol.publish(current);
            simulator.waitForTick();
        }

        [Test]
        public void testNDayBreak() {
            LogC.setOut("stop", @"U:\Knell\stop.log", true);
            O.freezeNow("2008/11/01"); // so that params don't change out from under us
            var args = new SystemArguments(SYMBOL, new Parameters {
                {"systemId", 39},
                {"RunMode", (double) RunMode.LIVE}
            });
            var simulator = makeSimulator(args);
            simulator.processBars(300);
            // using slippage for RE.TEST.TY.1C: 0.015625
            // AlmostEqual(6266670.11, simulator.pnl(), 0.01); // InitEquity 6000000, slippage = 0
            // full date range: AlmostEqual(5683201.36, simulator.pnl(), 0.01); // InitEquity 6000000, slippage = 0.015625
            AlmostEqual(293099.30, simulator.pnl(), 0.01);// 300 bars, InitEquity 6000000, slippage = 0.015625
        }

        [Test]
        public void testNBarFade() {
            O.freezeNow("2009/03/10");
            var symbol = new Symbol("RE.TEST.TY.1C", 1000);
            var args = new SystemArguments(symbol, new Parameters {
                {"systemId", 133486},
                {"RunMode", (double) RunMode.RIGHTEDGE},
                {"LeadBars", 50},
                {"ATRLen", 5},
                {"nDays", 6},
                {"nATRentry", 1.5},
                {"exitATRmultiple", 1},
                {"stopAfStep", 0.02},
                {"stopAfMax", 0.2},
                {"entryBarWindow", 2},
                {"closeBetter", 1},
                {"riskDollars", 100000000}
            });
            var loader = new SystemDbBarLoader(Interval.DAILY, O.list(symbol), date("2003/01/01"));
            var simulator = new Simulator(args, loader, OrderTable.prefix);
            simulator.processBars();
        }

        [Test]
        public void testOnClose() {
            var args = new SystemArguments(SYMBOL, new Parameters {
                {"systemId", 6172},
                {"LeadBars", 0},
                {"RunMode", (double) RunMode.RIGHTEDGE}
            });
            var simulator = makeSimulator(args);
            simulator.processBars(300);
            // simulator.processBars();
            // full date range: AlmostEqual(54890.63 - 164000, simulator.pnl(), 0.01);
             AlmostEqual(-8109.38, simulator.pnl(), 0.01);
        }

        static void hasPosition(Simulator simulator) {
            IsTrue(O.hasContent(simulator.allPositions()));
        }

        static void noPosition(Simulator simulator) {
            IsTrue(O.isEmpty(simulator.allPositions()));
        }

        static void noOrders(Simulator simulator) {
            IsTrue(O.isEmpty(simulator.allOrders()));
        }

        static void hasOrders(Simulator simulator) {
            IsTrue(O.hasContent(simulator.allOrders()));
        }
    }
}