using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using Q.Util;
using RE = RightEdge.Common;
using JTick = systemdb.data.Tick;
using O = Q.Util.Objects;

namespace Q.Plugins {
    [TestFixture]
    public class TestLiveData : DbTestCase {


        [Test]
        public void testLiveData() {
            const string name = "testone";
            systemdb.metadata.SystemTimeSeriesTable.SYSTEM_TS.insert(name, "ASCII", "ActiveMQ", "atopic");
            var service = new LiveData();
            service.Connect(new RE.ServiceConnectOptions());
            var testSymbol = new Symbol(new RE.Symbol(name));
            var symbols = O.list(testSymbol.re());
            var ticks = new List<RE.TickData>();
            RE.GotTickData reTickListener = delegate(RE.Symbol s, RE.TickData t) {
                Bomb.unless(s.Equals(testSymbol.re()), () => "unexpected symbol: " + s.Name);
                ticks.Add(t);
            };
            service.TickListener = reTickListener;
            service.SetWatchedSymbols(symbols);
            var jSymbol = testSymbol.javaSymbol();
            var tick = new JTick(3.0, 20, 2.0, 4.0, 1.0, O.jDate(O.now()));
            jSymbol.jmsLive().publish(tick);
            O.sleep(100);
            var reTicks = Tick.reTicks(tick);
            logTick(reTicks[0]);
            logTick(ticks[12]);
            AreEqual(reTicks[0].time, ticks.GetRange(12, 4)[0].time);
            AreEqual(reTicks, ticks.GetRange(12, 4));
            HasCount(16, ticks);
        }

        static void logTick(RE.TickData tick) {
            LogC.consoleOut("" + tick.price + " " + tick.size + " " + tick.tickType + " " + tick.time);
        }
    }
}
