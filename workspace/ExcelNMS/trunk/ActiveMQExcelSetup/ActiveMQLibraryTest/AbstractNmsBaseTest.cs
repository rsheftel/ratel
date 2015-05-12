using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;
using ActiveMQLibrary;
using Apache.NMS;
using NUnit.Framework;

namespace ActiveMQLibraryTest {
    public abstract class AbstractNmsBaseTest : AbstractTest
    {
        private readonly Process _broker = new Process();
        protected string _receivedMessage;

        [SetUp]
        public void StartActiveMQBroker()
        {
            var javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            var activeMQHome = Environment.GetEnvironmentVariable("ACTIVEMQ_HOME");
            if (string.IsNullOrEmpty(activeMQHome)) {
                activeMQHome = @"C:\Developer\apache-activemq-5.2.0";
            }

            _broker.StartInfo.Arguments = "-Dcom.sun.management.jmxremote -Xmx1536M " +
                @"-Dorg.apache.activemq.UseDedicatedTaskRunner=false -jar " +
                    activeMQHome + @"\bin\run.jar start ";
            // broker:(tcp://localhost:61616)?persistent=false ";

            _broker.StartInfo.FileName = @javaHome + @"\bin\java";

            _broker.StartInfo.UseShellExecute = false;
            _broker.StartInfo.RedirectStandardInput = true;
            _broker.StartInfo.RedirectStandardOutput = true;
            _broker.StartInfo.RedirectStandardError = true;
            _broker.Start();
            Console.WriteLine("started " + _broker.StartInfo.FileName + _broker.StartInfo.Arguments);

            string line;
            var sb = new StringBuilder();
            var lineCount = 0;
            while ((line = _broker.StandardOutput.ReadLine()) != null && lineCount < 10) {
                sb.Append(line).Append("\n");
                lineCount++;
            }
            Console.WriteLine(sb.ToString());
        }

        [TearDown]
        public void StopActiveMQBroker()
        {
            if (!_broker.HasExited)
                _broker.Kill();
        }

        protected static string PublishTestMessage(Broker broker, string testTopic, string fieldName, string fieldValue) {
            var message = new Dictionary<string, string> {{fieldName, fieldValue}};

            return PublishTestMessage(broker, testTopic, message);
        }


        protected static string PublishTestMessage(Broker broker, string testTopic, IDictionary<string, string> message)
        {
            var session = broker.Connection.CreateSession(AcknowledgementMode.AutoAcknowledge);
            var producer = session.CreateProducer(session.GetTopic(testTopic));
            var messageBody = TextMessageUtil.CreateMessage(testTopic, message);
            var textMessage = producer.CreateTextMessage(messageBody);

            producer.Send(textMessage);

            return textMessage.NMSMessageId;
        }

        protected IMessageConsumer StartTestListener(Broker broker, string topicName) {
            var session = broker.Connection.CreateSession(AcknowledgementMode.AutoAcknowledge);

            IDestination topic = session.GetTopic(topicName);
            var consumer = session.CreateConsumer(topic);
            consumer.Listener += OnMessageTestHandler;

            Console.WriteLine("Started Test Listener");

            return consumer;
        }

        protected static void StopTestListener(IMessageConsumer messageConsumer) {
            messageConsumer.Close();
            messageConsumer.Dispose();
        }

        protected void OnMessageTestHandler(IMessage message) {
            Console.WriteLine("Received message");
            var textMessage = message as ITextMessage;
            if (textMessage != null) _receivedMessage = textMessage.Text;
        }

        protected static bool HasMessageArrived(object source) {
            var us = source as BrokerTest;

            if (us == null) return false;
            return us._receivedMessage != null;
        }
    }
}