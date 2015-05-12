using O=Q.Util.Objects;
using System;
using NUnit.Framework;
using Q.Util;

namespace Q.Spuds.Core {
    [TestFixture]
    public class TestSpud : QAsserts {
        [Test]
        public void testSpud() {
            var manager = new SpudManager();
            var v = new RootSpud<double>(manager);
            v.set(7);
            Double foo = v; 
            AreEqual(7.0, foo);
            v.set(6);
            AreEqual(6.0, (double) v);
            manager.newBar();
            AreEqual(6.0, v[0]);
            AreEqual(6.0, v[1]);
            v.set(3);
            AreEqual(3.0, v[0]);
            AreEqual(6.0, v[1]);
            manager.newBar();
            v.set(8);
            v.set(9);
            AreEqual(9.0, (double) v);
            AreEqual(9.0, v[0]);
            AreEqual(3.0, v[1]);
            AreEqual(6, v[2]);
        }

        [Test]
        public void testLast10() {
            var manager = new SpudManager();
            var v = new RootSpud<double>(manager);
            AreEqual("EMPTYSPUD", v.last10());
            Last10Matches(manager, "1", v, 1);
            Last10Matches(manager, "2, 1", v, 2);
            Last10Matches(manager, "3, 2, 1", v, 3);
            Last10Matches(manager, "4, 3, 2, 1", v, 4);
            Last10Matches(manager, "5, 4, 3, 2, 1", v, 5);
            Last10Matches(manager, "6, 5, 4, 3, 2, 1", v, 6);
            Last10Matches(manager, "7, 6, 5, 4, 3, 2, 1", v, 7);
            Last10Matches(manager, "8, 7, 6, 5, 4, 3, 2, 1", v, 8);
            Last10Matches(manager, "9, 8, 7, 6, 5, 4, 3, 2, 1", v, 9);
            Last10Matches(manager, "10, 9, 8, 7, 6, 5, 4, 3, 2, 1", v, 10);
            Last10Matches(manager, "11, 10, 9, 8, 7, 6, 5, 4, 3, 2, ...", v, 11);
        }

        static void Last10Matches(SpudManager manager, string s, RootSpud<double> v, int i) {
            v.set(i);
            AreEqual(s, v.last10());
            manager.newBar();
        }

        [Test]
        public void testChanged() {
            var manager = new SpudManager();
            var v = new RootSpud<double>(manager);
            Bombs(() => v.changed(), "uninitialized");
            v.set(4.0);
            IsTrue(v.changed());
            manager.newBar();
            IsFalse(v.changed());
            v.set(5.0);
            IsTrue(v.changed());
            manager.newBar();
            v.set(5.0);
            IsFalse(v.changed());
        }

        [Test]
        public void testHasNaNs() {
            var manager = new SpudManager();
            var d = new RootSpud<double>(manager);
            IsFalse(d.hasNaN(10));
            manager.newBar();
            d.set(double.NaN);
            IsTrue(d.hasNaN(1));
            manager.newBar();
            d.set(0);
            IsFalse(d.hasNaN(1));
            IsTrue(d.hasNaN(2));
            IsTrue(d.hasNaN(10));
        }

        [Test]
        public void testSynchronicity() {
            var manager = new SpudManager();
            var d = new RootSpud<double>(manager);
            Bombs(() => O.info("" + d[0]), "uninitialized");
            d.set(7.5);
            var i = new RootSpud<int>(manager);
            i.set(4);
            var b = new RootSpud<bool>(manager);
            b.set(true);
            manager.newBar();
            d.set(6.5);
            i.set(3);
            b.set(false);
            manager.newBar();
            i.set(2);
            AreEqual(8.5, d[0] + i[0]);
            AreEqual(6.5, d[0]);
            AreEqual(6.5, d[1]);
            AreEqual(7.5, d[2]);
            AreEqual(2, i[0]);
            AreEqual(3, i[1]);
            AreEqual(4, i[2]);
            AreEqual(false, b[0]);
            AreEqual(false, b[1]);
            AreEqual(true, b[2]);
        }

        [Test]
        public void testTransform() {
            var manager = new SpudManager();
            var d = new RootSpud<double>(manager);
            var i = d.transform(x => (int) Math.Round(x));
            d.set(7.5);
            AreEqual(8, i[0]);
            d.set(6.5);
            AreEqual(6, i[0]);
            manager.newBar();
            AreEqual(6, i[0]);
            d.set(5.4);
            AreEqual(5, i[0]);
            AreEqual(6, i[1]);
        }

        [Test]
        public void testDiffSpudLag0() {
            var manager = new SpudManager();
            var r = new RootSpud<double>(manager);     
            Bombs(() => new DiffSpud(r,0), "lag should be >= 1");                     
        }

        [Test]
        public void testDiffSpudLag1() {
            var manager = new SpudManager();
            var r = new RootSpud<double>(manager);
            var d = new DiffSpud(r,1);
            r.set(7.5);
            AreEqual(double.NaN, d[0]);
            manager.newBar();
            r.set(6.5);            
            AreEqual(-1, d[0]);
            manager.newBar();
            r.set(10);            
            AreEqual(3.5, d[0]);            
        }

        [Test]
        public void testDiffSpudLag3() {
            var manager = new SpudManager();
            var r = new RootSpud<double>(manager);
            var d = new DiffSpud(r,3);
            r.set(7.5);
            AreEqual(double.NaN, d[0]);
            manager.newBar();
            r.set(6.5);            
            AreEqual(double.NaN, d[0]);
            manager.newBar();
            r.set(10);            
            AreEqual(double.NaN, d[0]);            
            manager.newBar();
            r.set(10);            
            AreEqual(2.5, d[0]);                        
            manager.newBar();
            r.set(0);            
            AreEqual(-6.5, d[0]);                        
            AreEqual(2.5, d[1]);                                    
        }

        [Test]
        public void testLogSpud() {
            var manager = new SpudManager();
            var r = new RootSpud<double>(manager);
            var d = new LogSpud(r);
            r.set(-5);
            AreEqual(double.NaN, d[0]);
            r.set(1);
            AreEqual(0, d[0]);
            manager.newBar();
            r.set(6.5);            
            AlmostEqual(1.871802, d[0],1e-6);
            manager.newBar();
            r.set(10);            
            AlmostEqual(2.302585, d[0],1e-6);     
            AlmostEqual(1.871802, d[1],1e-6);
            manager.newBar();
            r.set(0);            
            AreEqual(double.NaN, d[0]);                        
            manager.newBar();
            r.set(-1);            
            AreEqual(double.NaN, d[0]);                                  
            r.set(3);            
            AlmostEqual(1.098612, d[0],1e-6);                      
            AreEqual(double.NaN, d[1]);                                  
        }
    }
}