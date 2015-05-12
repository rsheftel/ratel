using NUnit.Framework;
using TradingScreenApiService;
using TradingScreenApiServiceTests.Nms;

namespace TradingScreenApiServiceTests
{
    [TestFixture]
    public class TradingScreenPositionManagerTest : AbstractNmsBaseTest
    {

        [SetUp]
        public void Setup() {
            StartActiveMQBroker();
        }

        [TearDown]
        public void TearDown() {
            StopActiveMQBroker();
        }

        [Test]
        public void TestConnectToActiveMQ() {
            var positionManager = new TradingScreenPositionManager();
            positionManager.ConnectToBroker("failover:tcp://nyws802:60606?transport.requesttimeout=10000");
        }

    }
}
