using NUnit.Framework;
using Q.Util;

namespace Q.Spuds.Core {

    [TestFixture]
    public class TestSpudObserver : QAsserts {
        int count;
        double value;

        [Test]
        public void testObserver() {
            var manager = new SpudManager();
            var observed = new RootSpud<double>(manager);
            observed.valueChanged += onChanged;
            observed.set(1.0);
            AreEqual(1, getAndReset());
            manager.newBar();
            AreEqual(0, getAndReset());
            observed.set(1.0);
            AreEqual(0, getAndReset());
            AreEqual(1.0, value);
        }

        public void onChanged(double newValue) {
            count++;
            value = newValue;
        }

        public int getAndReset() {
            var temp = count;
            count = 0;
            return temp;
        }
    }
}
