using NUnit.Framework;
using Q.Trading;

namespace Q.Util {
    [TestFixture]
    public class TestLazy : DbTestCase {
        [Test]
        public void testLazyInstantiation() {
            int[] x = {7};
            var num = new Lazy<int>(() => x[0]);
            x[0] = 9;
            AreEqual(9, num);
            x[0] = 10;
            AreEqual(9, num);
            num = new Lazy<int>(() => x[0]);
            AreEqual(10, num);
        }

        [Test]
        public void testLazyInstantiationWhenInitReturnsNull() {
            var y = 0;
            var bar = new Lazy<Bar>(() => y++ == 0 ? null : new Bar((systemdb.data.Bar) null));
            AreEqual(0, y);
            IsNull( (Bar) bar);
            AreEqual(1, y);
            IsNull((Bar) bar);
            IsNull((Bar) bar); // stays null, initializes once
        }
    }
}
