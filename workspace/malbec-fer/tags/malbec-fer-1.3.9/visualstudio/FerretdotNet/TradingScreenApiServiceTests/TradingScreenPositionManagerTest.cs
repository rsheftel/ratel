using System.Collections;
using System.Collections.Generic;
using System.Threading;
using ActiveMQClientTest;
using NUnit.Framework;
using TradingScreenApiService;
using TradingScreenApiService.Database.Mappings;
using TradingScreenApiServiceTests.Util;

namespace TradingScreenApiServiceTests
{
    [TestFixture]
    [Category("integration")]
    public class TradingScreenPositionManagerTest : AbstractNmsBaseTest 
    {

        [Test]
        public void TestConnectToActiveMQ() {
            var positionManager = new TradingScreenPositionManager {
                BrokerUrl = "failover:tcp://nyws802:60606",
            };

            Assert.IsNotNull(positionManager.BrokerUrl);

            positionManager.StartPublisher();
            Assert.IsTrue(positionManager.IsPublisherStarted);
        }

        [Test]
        public void TestMapperReloadScheduled() {
            var positionManager = new TradingScreenPositionManager();

            var loadCount = positionManager.LoadMapper();
            Assert.IsTrue(loadCount > 0);
            var firstDelay = positionManager.ScheduleMapperReload();
            Assert.AreNotSame(0, firstDelay);

            var isSheduled = positionManager.IsMapperReloadScheduled;
            Assert.IsTrue(isSheduled);
        }

        [Test]
        public void TestProcessOrderEvent() {
            var fsm = new FuturesSymbolMapper();

            // Setup our test data
            fsm.AddBloombergMapping("TRADS", "TY", "TY", "ZN", 1);

            var positionManager = new TradingScreenPositionManager(fsm);

            var fileOrders = MessageUtilTest.LoadTestOrders();
            var confirmed = false;
            positionManager.ReceiveOrders("Multiple", fileOrders, 1, ref confirmed);
            var orderEventIds = positionManager.OrderEventIds;
            Assert.IsNotNull(orderEventIds);
            Assert.Contains(1, (ICollection) orderEventIds);
            var positions = positionManager.Positions;
            Assert.IsNotNull(positions);
            Assert.AreEqual(5, positions.Count);
            // ensure the RIC are converted correctly
            var tym0Position = FindPosition("TYM0", positions);
            Assert.IsNotNull(tym0Position);
        }

        [Test]
        public void TestPositionCachePurge() {
            var positionManger = new TradingScreenPositionManager();

            var firstDelay = positionManger.SchedulePositionPurge();
            Assert.IsTrue(firstDelay > 0);

            Assert.IsTrue(positionManger.IsPositionPurgeScheduled);
        }

        [Test] 
        public void TestTradingScreen() {

            var positionManager = new TradingScreenPositionManager {
                ApiUserId = "malbec2_uat",
                ApiPassword =  "trader888",
                ApiSite = "UAT Prod B",
                ApiPricingServer = "tcp://uatprod.trandingscreen.net:9901",
                BrokerUrl = "tcp://nyws802:60606",
            };

            // Fire up the infrastructure
            positionManager.LoadMapper();
            positionManager.StartPublisher();

            // Do our tests
            Assert.IsFalse(positionManager.LoggedIn);
            Assert.IsTrue(positionManager.LoginToTradingScreen());
            Assert.IsTrue(positionManager.LoggedIn);
            Assert.IsTrue(positionManager.StartProcessingOrders());
            Thread.Sleep(10000);
            
            Assert.IsTrue(positionManager.LogoffTradingScreen());
            Assert.IsFalse(positionManager.LoggedIn);

            // This may fail as we may not have positions.  Should this assume we don't?
            Assert.IsTrue(positionManager.Positions.Count > 0);
        }

        static IDictionary<string, string> FindPosition(string bid, IEnumerable<IDictionary<string, string>> positions) {
            foreach (var position in positions) {
                if (position["BID"] == bid) {
                    return position;
                }
            }

            return null;
        }

        /*
        [SetUp]
        public void Setup() {
            StartActiveMQBroker();
        }

        [TearDown]
        public void TearDown() {
            StopActiveMQBroker();
        }
        */
    }
}
