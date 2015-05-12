using NUnit.Framework;
using systemdb.data;
using systemdb.metadata;
using RE = RightEdge.Common;
using Q.Util;
using O = Q.Util.Objects;

namespace Q.Plugins {
    [TestFixture] public class TestDataStore : DbTestCase {
        
        const string NAME = "TEST.TEST";
        readonly RE.SymbolFreq SYMBOL_FREQ = new RE.SymbolFreq(new RE.Symbol(NAME), 1440);

        [Test] public void testLoadBars() {
            RE.IBarDataStorage store = new DataStore();
            var bars = store.LoadBars(SYMBOL_FREQ, date("1987/06/01"), date("2008/04/18"));
            HasCount(5249, bars);
            DatesMatch(O.last(bars).PriceDateTime, "2008/04/18");
        }

        [Test] public void testLoadLastBars() {
            RE.IBarDataStorage store = new DataStore();
            var bars = store.LoadLastBars(SYMBOL_FREQ, 50);
            AreEqual(50, bars.Count);
            AreEqual(5249, store.GetBarCount(SYMBOL_FREQ));
            DatesMatch("1987/06/01", store.GetFirstBarDate(SYMBOL_FREQ));
            DatesMatch("2008/04/18", store.GetLastBarDate(SYMBOL_FREQ));
        }

        public override void setUp() {
            base.setUp();
            SystemTimeSeriesTable.SYSTEM_TS.insert(NAME, "ASCII", "ActiveMQ");
            AsciiTable.SYSTEM_ASCII.insert(NAME, @"..\..\..\..\Java\systematic\test\systemdb\DailyAsciiTest.csv", true);
        }
    }
}