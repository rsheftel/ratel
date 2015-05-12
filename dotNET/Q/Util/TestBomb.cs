using NUnit.Framework;
using O = Q.Util.Objects;

namespace Q.Util {
    [TestFixture] public class TestBomb : DbTestCase {

        [Test] public void testBombMissing() {
            var d = Objects.dictionaryOne(5, "hello");
            AreEqual("hello", Bomb.missing(d, 5));
            Bombs(() => Bomb.missing(d, 4), "no value for 4 in ");
        }

    }
}