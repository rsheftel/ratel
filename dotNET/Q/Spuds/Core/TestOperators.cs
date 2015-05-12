using O=Q.Util.Objects;
using NUnit.Framework;
using Q.Util;

namespace Q.Spuds.Core {
    [TestFixture]
    public class TestOperators : QAsserts {
        public SpudManager manager = new SpudManager();      

        [Test]
        public void testPlus() {            
            var a = new RootSpud<double>(manager);
            var b = new RootSpud<double>(manager);
            var c = new RootSpud<double>(manager);
            var sum = new Plus(a, b);
            var sum2 = new Plus(sum, c);
            a.set(5);
            Bombs(() => O.info("" + sum[0]), "uninitialized");
            b.set(3);
            AreEqual(8.0, sum[0]);
            a.set(8);
            AreEqual(11.0, sum[0]);
            b.set(9);
            AreEqual(17.0, sum[0]);

            manager.newBar();
            AreEqual(17.0, sum[0]);
            Bombs(() => O.info("" + sum2[0]), "uninitialized");

            c.set(3.0);
            AreEqual(20.0, sum2[0]);
            a.set(1.0);
            b.set(2.0);
            AreEqual(6.0, sum2[0]);
            AreEqual(3.0, sum[0]);

            a.set(4.0);
            b.set(5.0);
            c.set(6.0);
            AreEqual(15.0, sum2[0]);
            AreEqual(9.0, sum[0]);
        }

        [Test]
        public void testMinus() {            
            var a = new RootSpud<double>(manager);
            var b = new RootSpud<double>(manager);
            var sub = new Minus(a, b);
            a.set(5);
            Bombs(() => O.info("" + sub[0]), "uninitialized");
            b.set(3);
            AreEqual(2, sub[0]);
            a.set(8);
            AreEqual(5, sub[0]);
            b.set(9);
            AreEqual(-1, sub[0]);
        }
        [Test]
        public void testTimes() {            
            var a = new RootSpud<double>(manager);
            var b = new RootSpud<double>(manager);
            var sub = new Times(a, b);
            a.set(5);
            Bombs(() => O.info("" + sub[0]), "uninitialized");
            b.set(3);
            AreEqual(15, sub[0]);
            a.set(8);
            AreEqual(24, sub[0]);
            b.set(-9);
            AreEqual(-72, sub[0]);
        }

        [Test]
        public void testDivide() {            
            var a = new RootSpud<double>(manager);
            var b = new RootSpud<double>(manager);
            var sub = new Divide(a, b);
            a.set(6);
            Bombs(() => O.info("" + sub[0]), "uninitialized");
            b.set(3);
            AreEqual(2, sub[0]);
            a.set(5);
            AlmostEqual(1.666667, sub[0],1e-6);
            b.set(0);
            AreEqual(0, sub[0]);
        }       
    }
}