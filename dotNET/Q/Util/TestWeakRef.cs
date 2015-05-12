using System;
using System.Threading;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Util {
    [TestFixture]
    public class TestWeakRef : DbTestCase {
        [Test]
        public void testWeakRefWorks() {
            var reference = new WeakRef<Bar>(new Bar(1,1,1,1));
            GC.Collect();
            IsFalse(reference.isAlive());
            var b = new Bar(1, 1, 1, 1);
            var barRef = new WeakRef<Bar>(b);
            Bar b2 = barRef;
            AreEqual(b, b2);
        }

        [Test]
        public void testWeakRefList() {
            var b1 = new Bar(1, 1, 1, 1);
            var b2 = new Bar(2, 2, 2, 2);
            var refList = new WeakRefList<Bar> {
                new Bar(0, 0, 0, 0),
                b1,
                b2,
                new Bar(3, 3, 3, 3)
            };
            GC.Collect();
            HasCount(2, O.list<Bar>(refList));
        }

        public void testRaceCondition() {
            O.zeroTo(10000000, i => new WeakRef<Bar>(new Bar(1,1,1,1)).safeValue());
        }
        
        public void testMultiThreadDictionaryLookup() {
            var refDict = new WeakRefDictionary<Bar, double>();
            
            O.zeroTo(10000000, i => {
                refDict[new Bar(1, 1, 1, 1)] = 0.0;
                refDict[new Bar(2, 2, 2, 2)] = 1.0;
                new Thread(() => O.toShortString(refDict.Keys)).Start();
                O.toShortString(refDict.Keys);
            });
        }

    }
}
