using NUnit.Framework;
using Q.Trading;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestFibonacci : SpudTestCase<Bar, BreakPoints> {
        [Test]
        public void testFibonacci() {
            values = new BarSpud(values.manager);
            indicator = new Fibonacci((BarSpud) values, 20);

            var testBar1 = bar(1,1,1,1);
            var testBar2 = bar(1, 2, 0, 1);
            addPoint(testBar1, new BreakPoints(testBar1,1,1));
            AreEqual(indicator[0].fibo0(), 1);
            AreEqual(indicator[0].fibo38(), 1);
            AreEqual(indicator[0].fibo50(), 1);
            AreEqual(indicator[0].fibo62(), 1);
            AreEqual(indicator[0].fibo100(), 1);
            addPoint(testBar2, new BreakPoints(testBar2, 2, 0));
            AreEqual(indicator[0].fibo0(), 0);
            AreEqual(indicator[0].fibo38(), 0.764);
            AreEqual(indicator[0].fibo50(), 1);
            AreEqual(indicator[0].fibo62(), 1.236);
            AreEqual(indicator[0].fibo100(), 2);
        }

        static Bar bar(double open, double high, double low, double close) {
            return new Bar(open, high, low, close);
        }

        [Test]
        public void testFiboNumber() {
            AreEqual(Fibonacci.Number(0), 0); 
            AreEqual(Fibonacci.Number(1), 1); 
            AreEqual(Fibonacci.Number(2), 1); 
            AreEqual(Fibonacci.Number(3), 2); 
            AreEqual(Fibonacci.Number(4), 3); 
            AreEqual(Fibonacci.Number(5), 5); 
            AreEqual(Fibonacci.Number(8), 21); 
        }
        [Test]
        public void testFiboSequence() {
            var seq = Fibonacci.Sequence(8);
            AreEqual(seq[0], 0); 
            AreEqual(seq[1], 1); 
            AreEqual(seq[2], 1); 
            AreEqual(seq[3], 2); 
            AreEqual(seq[4], 3); 
            AreEqual(seq[5], 5); 
            AreEqual(seq[8], 21); 
            AreEqual(seq.Length,9); 
        }
    }
}