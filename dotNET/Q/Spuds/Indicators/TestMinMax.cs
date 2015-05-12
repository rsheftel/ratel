using System;
using NUnit.Framework;
using Q.Spuds.Core;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestComparableSpud : SpudTestCase<double, double> {
        [Test]
        public void testHighest() {
            indicator = new ComparableSpudWrapper<double>(values).highest(3);
            addPoint(10, 10);
            addPoint(11, 11);
            addPoint(10, 11);
            addPoint(9, 11);
            addPoint(10, 10);

            addPoint(0, 10);
            addPoint(0, 10);
            addPoint(0, 0);
            addPoint(2, 2);
            addPoint(1, 2);
            addPoint(0, 2);
            addPoint(0, 1);
            addPoint(0, 0);
        }
        [Test]
        public void testLowest() {
            indicator = new ComparableSpudWrapper<double>(values).lowest(3);
            addPoint(10, 10);
            addPoint(11, 10);
            addPoint(10, 10);
            addPoint(9, 9);
            addPoint(10, 9);

            addPoint(0, 0);
            addPoint(0, 0);
            addPoint(0, 0);
            addPoint(2, 0);
            addPoint(1, 0);
            addPoint(0, 0);
            addPoint(0, 0);
            addPoint(0, 0);
        }

        [Test]
        public void benchmarkCSMin() {
            indicator = new MinMax<double>(values, 20, MinMax<double>.MIN);
            var start = DateTime.Now;
            var rand = new Random();
            O.zeroTo(100000, i => addPoint(rand.Next()));
            LogC.info("total time: " + DateTime.Now.Subtract(start).TotalMilliseconds + " millis");
        }

        [Test]
        public void benchmarkMin() {
            indicator = new Min(values, 20);
            var start = DateTime.Now;
            var rand = new Random();
            O.zeroTo(100000, i => addPoint(rand.Next()));
            LogC.info("total time: " + DateTime.Now.Subtract(start).TotalMilliseconds + " millis");
        }
    }
}