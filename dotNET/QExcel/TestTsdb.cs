using NUnit.Framework;
using Q.Util;
using systemdb.metadata;
using O=Q.Util.Objects;

namespace QExcel {
    [TestFixture]
    public class TestTsdb : DbTestCase {
        [Test]
        public void testOneValue() {
            var tsdb = new Tsdb();
            AreEqual(26.5, tsdb.retrieveOneValueByTimeSeries("aapl close", "yahoo", "1984/09/07"));
        }

        [Test]
        public void testBusinessDaysAgo() {
            var tsdb = new Tsdb();
            AreEqual(date("1984/09/06"), tsdb.businessDaysAgo(1, "1984/09/07", "nyb"));
            AreEqual(date("2007/12/20"), tsdb.businessDaysAgo(7, "2008/01/01", "nyb"));
        }

        [Test]
        public void testParameterValue() {
            ParameterValuesTable.VALUES.insert("FaderClose", "EquityVolatility2816", "RiskDollars", "-10");
            var tsdb = new Tsdb();

            var parameters = (object[,]) tsdb.liveParameters("FaderClose", "EquityVolatility2816");
            for (var i = 0; i < parameters.Length / 2; i++) 
                if (parameters[i, 0].Equals("RiskDollars")) 
                    AreEqual("-10", parameters[i, 1]);
        }
    }
}
