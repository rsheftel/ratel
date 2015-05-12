using NUnit.Framework;
using Q.Util;

namespace Q.Spuds.Core {
    [TestFixture]
    public class TestCrossOver : DbTestCase {
        [Test]
        public void testCrossOver() {
            var manager = new SpudManager();
            var spud1 = new RootSpud<double>(manager);
            var spud2 = new RootSpud<double>(manager);
            var crossOver = new CrossOverSpud<double>(spud1, spud2);
            spud1.set(1);
            spud2.set(1);
            IsFalse(crossOver.crossedAbove());
            IsFalse(crossOver.crossedBelow());
            IsFalse(crossOver.crossed());
            manager.newBar();
            spud1.set(1);
            spud2.set(2);
            IsFalse(crossOver.crossedAbove());
            IsFalse(crossOver.crossedBelow());
            IsFalse(crossOver.crossed());
            manager.newBar();
            spud1.set(2);
            spud2.set(1);
            IsTrue(crossOver.crossedAbove());
            IsFalse(crossOver.crossedBelow());
            IsTrue(crossOver.crossed());
            manager.newBar();
            spud1.set(1);
            spud2.set(2);
            IsFalse(crossOver.crossedAbove());
            IsTrue(crossOver.crossedBelow());
            IsTrue(crossOver.crossed());
            manager.newBar();
            spud1.set(2);
            IsFalse(crossOver.crossedAbove());
            IsFalse(crossOver.crossedBelow());
            IsFalse(crossOver.crossed());
            manager.newBar();
            spud1.set(3);
            IsTrue(crossOver.crossedAbove());
            IsFalse(crossOver.crossedBelow());
            IsTrue(crossOver.crossed());
        }
    }
}
