using NUnit.Framework;
using Q.Trading;
using Q.Util;
using O = Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestSystemWithUnplacedObjects: QAsserts {
        [Test]
        public void testOrderFinalizer() {
            Bombs(
                delegate {
                    using (new Order(null, null,  Order.limit(5),null , 1, OnClose.ON_CLOSE)) {}
                },
                "unplaced order");
        }
    }
}