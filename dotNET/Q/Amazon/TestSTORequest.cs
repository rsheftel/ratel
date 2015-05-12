using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Amazon {
    [TestFixture]
    public class TestSTORequest : DbTestCase {
        [Test]
        public void testInOut() {
            var parameters = STOClient.STORunner.parameters(37859, 1);
            var request = new STORequest(O.list(new Symbol("RE.TEST.TY.1C", 1000)), new List<Portfolio>(), parameters);
            AreEqual(request, new STORequest(request.java()));
        }
    }
}