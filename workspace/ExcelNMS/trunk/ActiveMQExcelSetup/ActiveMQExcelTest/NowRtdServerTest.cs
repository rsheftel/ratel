using System;
using System.Threading;
using ActiveMQExcel;
using Microsoft.Office.Interop.Excel;
using NUnit.Framework;

namespace ActiveMQExcelTest {
    [TestFixture] 
    public class NowRtdServerTest {
        [Test] 
        public void TestServerFrequency() {
            var server = new NowRtdServer();
            IRTDUpdateEvent updateEventHandler = new TestEventHandler();
            var startResult = server.ServerStart(updateEventHandler);
            Assert.AreEqual(1, startResult);
            Assert.AreEqual(1, server.Heartbeat());
            Assert.AreEqual(0, server.TimerCount);

            var newValues = false;
            Array parameters = new[] {"FRQ:1S"};

            var connectResult = server.ConnectData(1, ref parameters, ref newValues);
            Assert.IsNotNull(connectResult);
            Assert.AreEqual(1, server.TimerCount);

            var topicCount = 0;
            var topicData = server.RefreshData(ref topicCount);
            Assert.AreEqual(0, topicCount);
            Assert.IsNotNull(topicData);
            Assert.AreEqual(1, topicData.GetUpperBound(0));
            Assert.AreEqual(-1, topicData.GetUpperBound(1));
            Assert.AreEqual(2, topicData.Rank);
            // We are testing the 1 second timer, wait that long
            Thread.Sleep(1 * 1500);

            topicData = server.RefreshData(ref topicCount);
            Assert.AreEqual(1, topicCount);
            Assert.IsNotNull(topicData);
            Assert.AreEqual(1, topicData.GetUpperBound(0));
            Assert.AreEqual(0, topicData.GetUpperBound(1));
            Assert.AreEqual(2, topicData.Rank);
            Assert.AreEqual(topicData.GetValue(0, 0), 1);
            Assert.IsNotNull(topicData.GetValue(1, 0));

            // Add another topic with the same pattern
            Assert.IsNotNull(server.ConnectData(2, ref parameters, ref newValues));
            Assert.AreEqual(1, server.TimerCount);

            // Add another topic with a different pattern
            Array parameters2 = new[] {"FRQ:1M"};
            Assert.IsNotNull(server.ConnectData(3, ref parameters2, ref newValues));
            Assert.AreEqual(2, server.TimerCount);

            // Remove single topic pattern
            server.DisconnectData(3);
            Assert.AreEqual(1, server.TimerCount);

            // Remove duplicate topic pattern
            server.DisconnectData(1);
            Assert.AreEqual(1, server.TimerCount);
            // Remove last topic
            server.DisconnectData(2);
            Assert.AreEqual(0, server.TimerCount);

            server.ServerTerminate();
        }

        [Test] 
        public void TestServerToday() {
            var server = new NowRtdServer();
            IRTDUpdateEvent updateEventHandler = new TestEventHandler();
            var startResult = server.ServerStart(updateEventHandler);
            Assert.AreEqual(1, startResult);
            Assert.AreEqual(1, server.Heartbeat());

            var newValues = false;
            Array parameters = new[] {"Today"};

            var connectResult = server.ConnectData(1, ref parameters, ref newValues);
            Assert.IsNotNull(connectResult);
            Assert.AreEqual(DateTime.Now.Date.ToOADate(), connectResult);
            Console.WriteLine(connectResult);

            var topicCount = 0;
            var topicData = server.RefreshData(ref topicCount);
            Assert.AreEqual(0, topicCount);
            Assert.IsNotNull(topicData);
            Assert.AreEqual(1, topicData.GetUpperBound(0));
            Assert.AreEqual(-1, topicData.GetUpperBound(1));
            Assert.AreEqual(2, topicData.Rank);

            server.DisconnectData(1);
            server.ServerTerminate();
        }
    }
}