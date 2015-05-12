using System.Collections.Generic;
using NUnit.Framework;
using Q.Util;
using JTick=systemdb.data.Tick;
using O=Q.Util.Objects;
using RE=Q.Trading.Symbol;

namespace Q.Trading {
    [TestFixture]
    public class TestSymbolTick : DbTestCase {
        [Test]
        public void testTicks() {
            var actual = new List<Tick>();
            var listener = new Symbol.JTickListener(actual.Add, true);
            var d = O.now();
            var firstTick = new JTick(3.0, 100, 2.0, 4.0, 1.5, O.jDate(d));
            listener.onTick(firstTick);
            var opensLow = new List<Tick> {
                new Tick(2.0, 0, d),
                new Tick(1.5, 0, d),
                new Tick(4.0, 0, d),
                new Tick(3.0, 100, d)
            };
            AreEqual(opensLow, actual);
            actual.Clear();
            var nextTick = new JTick(2.5, 100, 2.0, 4.0, 1.5, O.jDate(d));
            listener.onTick(nextTick);
            AreEqual(new Tick(2.5, 100, d), O.the(actual));
            actual.Clear();
            listener = new Symbol.JTickListener(actual.Add, true); // reset
            firstTick = new JTick(1.5, 100, 2.0, 2.5, 1.0, O.jDate(d));
            listener.onTick(firstTick);
            var opensHigh = new List<Tick> {
                new Tick(2.0, 0, d),
                new Tick(2.5, 0, d),
                new Tick(1.0, 0, d),
                new Tick(1.5, 100, d)
            };
            AreEqual(opensHigh, actual);
        }

        [Test]
        public void testOldTicksGetSkipped() {
            var actual = new List<Tick>();
            var listener = new Symbol.JTickListener(actual.Add, true);
            O.freezeNow("2008/09/29 09:18:00");
            var oldTick = new JTick(3.0, 100, 2.0, 4.0, 1.5, util.Dates.date("2008/09/27 09:17:59"));
            listener.onTick(oldTick);
            IsEmpty(actual);
            var newTick = new JTick(3.0, 100, 2.0, 4.0, 1.5, util.Dates.date("2008/09/27 09:18:00"));
            listener.onTick(newTick);
            HasCount(4, actual);
            
        }
    }
}