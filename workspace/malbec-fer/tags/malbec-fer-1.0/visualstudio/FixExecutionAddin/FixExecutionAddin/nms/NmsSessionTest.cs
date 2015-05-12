using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using Apache.NMS;
using NUnit.Framework;

namespace FixExecutionAddin.nms
{
    [TestFixture]
    public class NmsSessionTest : AbstractNmsSetupTest
    {
        [Test]
        public void testStartStop() {
            var nmsSession = new NmsSession(BROKER_URL, new TestApplication());
            // TODO We may have to remove the '\' in the username
            Console.WriteLine(nmsSession.ReplyTo);
            Assert.IsNotNull(nmsSession.ReplyTo, "ReplyTo not set");

            nmsSession.ConsumerQueue = FER_RESPONSE;
            nmsSession.ProducerQueue = FER_COMMAND;
            nmsSession.Start();
            
            WaitFor(Connected, nmsSession, 3000);
            Assert.IsTrue(nmsSession.Connected, "Connection failed to start");
                 
            nmsSession.Stop();
            
            WaitFor(Disconnected, nmsSession, 3000);
            Assert.IsFalse(nmsSession.Connected, "Did not disconnect");
        }

        //[Test]
        public void testFailsafeUri() {
            var url = new Uri("failsafe:tcp://localhost:60606");
            Assert.IsNotNull(url.AbsoluteUri, "URL is null");
            Console.WriteLine(url.AbsoluteUri);
            Console.WriteLine(url.Port);
            Console.WriteLine(url);
            Assert.AreEqual("localhost", url.Host, "failed to determine hostname");
        }
    }

    internal class TestApplication : INmsApplication {
        public void InboundApp(IMessage message) {
            throw new System.NotImplementedException();
        }

        public void OutboundApp(IMessage message) {
            throw new System.NotImplementedException();
        }
    }
}
