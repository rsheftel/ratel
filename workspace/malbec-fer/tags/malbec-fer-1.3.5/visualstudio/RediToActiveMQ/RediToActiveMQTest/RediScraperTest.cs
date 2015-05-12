using System;
using System.Collections.Generic;
using NUnit.Framework;
using RediToActiveMQ;

namespace RediToActiveMQTest
{
    [TestFixture]
    public class RediScraperTest : AbstractTest
    {
        AppConfiguration appConfig;

        [SetUp]
        public void LoadConfiguration() 
        {
            appConfig = AppConfiguration.Load();
        }

        [Test]
        public void TestPositionPublishable() {

            var rowValues = new Dictionary<string, string> {{"SHARESSOLD", "1"}, {"SHARESBOUGHT", "2"}};
            var rowValuesB = new Dictionary<string, string> { { "SHARESSOLD", "0" }, { "SHARESBOUGHT", "2" } };
            var rowValuesS= new Dictionary<string, string> { { "SHARESSOLD", "1" }, { "SHARESBOUGHT", "0" } };
            var rowValuesE = new Dictionary<string, string> ();

            Assert.IsTrue(RediScraper.PublishablePosition(rowValues));
            Assert.IsTrue(RediScraper.PublishablePosition(rowValuesB));
            Assert.IsTrue(RediScraper.PublishablePosition(rowValuesS));
            Assert.IsFalse(RediScraper.PublishablePosition(rowValuesE));
        }

        [Test]
        public void TestRediRunning() {
            var rs = new RediScraper("TestId", "TestPassword");
            Assert.IsFalse(rs.IsRediRunning());
        }

        [Test]
        public void TestStartAndConnectToRedi() {
            var rs = new RediScraper(appConfig.UserId, appConfig.Password);

            if (rs.IsRediRunning()) {
                Console.WriteLine("Had to kill Redi for tests");
                rs.KillRedi();
            }

            var rediStarted = rs.StartRedi();
            Assert.IsTrue(rediStarted, "Failed to start Redi");

            WaitFor(WaitingForCom, rs, 10 * 1000);
            Assert.IsFalse(rs.CanConnectViaCom(), "Redi started in 10 seconds, unlikely");

            WaitFor(WaitingForCom, rs, 120 * 1000);
            Assert.IsTrue(rs.CanConnectViaCom(), "Redi failed to start in 120 seconds");

            rs.ConnectMessageListener();
            Assert.IsTrue(rs.IsMessageListenerStarted, "Message listener failed to connect");

            rs.ConnectPositionListener();
            Assert.IsTrue(rs.IsPositionListenerStarted, "Position listener failed to connect");

            Assert.IsTrue(rs.IsConnected());

            Assert.IsTrue(rs.ExecutionCount > 0, "Failed to get executions from Message table");

            rs.DisconnectMessageListener();
            Assert.IsFalse(rs.IsConnected());

            rs.DisconnectPositionListener();
        }



        private static bool WaitingForCom(object rediScraper) {
            var rs = rediScraper as RediScraper;

            return (rs != null) ? rs.CanConnectViaCom() : false;
        }
    }
}
