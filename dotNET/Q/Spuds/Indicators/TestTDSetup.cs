using NUnit.Framework;
using Q.Trading;


namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestTDSetup : SpudTestCase<Bar, double> {
        [Test]
        public void testTDSetup() {
            indicator = new TDSetup(values);

            addPoint(new Bar(95,95,95,95));
            addPoint(new Bar(99,99,99,99));
            addPoint(new Bar(98,98,98,98));
            addPoint(new Bar(97,97,97,97));
            addPoint(new Bar(96,96,96,96));
            addPoint(new Bar(99.5,99.5,99.5,99.5));
            AreEqual(indicator[0], 0);
            addPoint(new Bar(97.5,97.5,97.5,97.5));
            AreEqual(indicator[0], -1);
            addPoint(new Bar(95,96.5,85,96.5));
            AreEqual(indicator[0], -2);
            addPoint(new Bar(94,97,94,95));
            AreEqual(indicator[0], -3);
            addPoint(new Bar(100,100,100,100));
            AreEqual(indicator[0], 1);
            addPoint(new Bar(102, 102, 102, 102));
            AreEqual(indicator[0], 2);
            addPoint(new Bar(102,102,102,102));
            AreEqual(indicator[0], 3);
            addPoint(new Bar(101,101,101,101));
            AreEqual(indicator[0], 4);
            addPoint(new Bar(101,101,101,101));
            AreEqual(indicator[0], 5);
            addPoint(new Bar(103, 103, 101.5, 102));
            AreEqual(indicator[0], 0);
        }
    }
}