using NUnit.Framework;
using systemdb.metadata;
using util;
using DbTestCase=Q.Util.DbTestCase;
using O=Q.Util.Objects;

namespace Q.Trading {
    [TestFixture]
    public class TestSymbol : DbTestCase {

        public override void setUp() {
            base.setUp();
            TestMarket.insertTestData();
        }

        [Test]
        public void testProcessCloseOrdersTime() {
            AreEqual(O.date(Dates.todayAt("12:30:00")), new Symbol("MKT2").processCloseOrdersTime());
        }

        [Test]
        public void testMarketSessions() {
            MarketSessionTable.SESSION.insert("MKT1", Session.DAY, "12:00:00", "13:00:00", 10);
            var session = new Symbol("MKT1").session(Session.DAY);
            AreEqual(Dates.todayAt("12:59:50"), session.processCloseAt());
        }

        [Test]
        public void testRelatedSymbol() {
            var startSymbol = new Symbol("TEST.SYMBOL");
            AreEqual("SWITCH.SYMBOL", startSymbol.relatedPrefix("SWITCH").name);
            AreEqual("TEST.SWITCH", startSymbol.relatedSuffix("SWITCH").name);
            var startSymbol2 = new Symbol("TEST.SPY.SYMBOL");
            AreEqual("TEST.SWITCH", startSymbol2.relatedSuffixFromFirstDot(".SWITCH").name);
        }

        [Test]
        public void testCurrency() {
            AreEqual(new Currency("USD"), new Symbol("RE.TEST.TY.1C").currency());
        }

        [Test]
        public void testMarketPeriods() {
            var testSymbol = new Symbol("CDS.TEST.CAH.5Y.ACB20");
            IsTrue(testSymbol.isPeriodStart(O.date("2003-06-25")));
            IsTrue(testSymbol.isPeriodStart(O.date("2008-06-01")));
            IsTrue(testSymbol.isPeriodEnd(O.date("2007-10-10")));

            IsFalse(testSymbol.isPeriodStart(O.date("2003-06-26")));
            IsFalse(testSymbol.isPeriodEnd(O.date("2009-01-01")));

            IsTrue(testSymbol.isPeriodInactive(O.date("2007-10-11")));
            IsFalse(testSymbol.isPeriodInactive(O.date("2007-10-10")));

            MarketHistoryTable.MARKET_HISTORY.insert(
                new systemdb.metadata.Market(testSymbol.name), null, O.jDate("1980-06-30"));
            Symbol.clearPeriodsCache();
            IsFalse(testSymbol.isPeriodInactive(O.date("1980-06-29")));
            IsFalse(testSymbol.isPeriodInactive(O.date("2008-06-30")));
            
        }
    }
}
