using NUnit.Framework;
using Q.Spuds.Indicators;
using Q.Trading;
using systemdb.data;
using Bar=Q.Trading.Bar;

namespace Q.Spuds.Core {
    [TestFixture]
    public class TestIntervalSpud : SpudTestCase<Bar,Bar> {
        [Test]
        public void testAggregation() {
            values = new BarSpud(manager);
            indicator = new IntervalSpud(values, Interval.DAILY);

            addPoint(new Bar(1, date("2009/01/01 09:00:00")), new Bar(1, date("2009/01/01 09:00:00")));
            addPoint(new Bar(2, date("2009/01/01 09:05:00")), new Bar(1, 2, 1, 2, date("2009/01/01 09:05:00")));
            addPoint(new Bar(1.5, date("2009/01/01 23:59:59")), new Bar(1, 2, 1, 1.5, date("2009/01/01 23:59:59")));
            addPoint(new Bar(3, date("2009/01/02 00:00:00")), new Bar(3, 3, 3, 3, date("2009/01/02 00:00:00")));
            addPoint(new Bar(2, date("2009/01/02 00:00:01")), new Bar(3, 3, 2, 2, date("2009/01/02 00:00:01")));
            AreEqual(2, indicator.count());


        }
    }
}
