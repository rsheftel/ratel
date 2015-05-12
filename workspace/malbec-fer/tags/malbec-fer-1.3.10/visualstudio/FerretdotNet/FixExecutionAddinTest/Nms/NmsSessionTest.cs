using System;
using Apache.NMS;
using FixExecutionAddin.Nms;
using NUnit.Framework;

namespace FixExecutionAddinTest.Nms {
    [TestFixture]
    public class NmsSessionTest : AbstractNmsSetupTest
    {
        [Test]
        public void TestStartStop() {
            var nmsSession = new NmsSession(BrokerUrl, new TestApplication()) {ProducerQueue = FerCommandQueue};

            nmsSession.Start();
            
            WaitFor(Connected, nmsSession, 10000);
            Assert.IsTrue(nmsSession.Connected, "Connection failed to start");
                 
            nmsSession.Stop();
            
            WaitFor(Disconnected, nmsSession, 3000);
            Assert.IsFalse(nmsSession.Connected, "Did not disconnect");
        }

        //[Test]
        public void TestFailsafeUri() {
            var url = new Uri("failsafe:tcp://localhost:60606");
            Assert.IsNotNull(url.AbsoluteUri, "URL is null");
            Console.WriteLine(url.AbsoluteUri);
            Console.WriteLine(url.Port);
            Console.WriteLine(url);
            Assert.AreEqual("localhost", url.Host, "failed to determine hostname");
        }

        #region Broker Start/Stop
        [SetUp]
        public void Setup()
        {
            StartActiveMQBroker();
        }

        [TearDown]
        public void TearDown()
        {
            StopActiveMQBroker();
        }
        #endregion
    }

    internal class TestApplication : INmsApplication {
        public void InboundApp(IMessage message) {
            throw new NotImplementedException();
        }

        public void OutboundApp(IMessage message) {
            throw new NotImplementedException();
        }
    }
}