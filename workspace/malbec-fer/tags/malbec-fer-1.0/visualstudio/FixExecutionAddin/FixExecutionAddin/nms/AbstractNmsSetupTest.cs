using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using NUnit.Framework;

namespace FixExecutionAddin.nms
{
    public class AbstractNmsSetupTest : AbstractNmsTest
    {
        IConnection connection;
        ISession session;

        readonly Process broker = new Process();


        [SetUp]
        public void startActiveMQBroker()
        {

            var javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            var activeMQHome = Environment.GetEnvironmentVariable("ACTIVEMQ_HOME");

            if (String.IsNullOrEmpty(activeMQHome)) {
                activeMQHome = @"C:\Developer\apache-activemq-5.1.0";
            }

            broker.StartInfo.Arguments = "-Dcom.sun.management.jmxremote -Xmx128M " +
                @"-Dorg.apache.activemq.UseDedicatedTaskRunner=false -jar " +
                    activeMQHome + @"\bin\run.jar start";

            broker.StartInfo.FileName = @javaHome + @"\bin\java";

            broker.StartInfo.UseShellExecute = false;
            broker.StartInfo.RedirectStandardInput = true;
            broker.StartInfo.RedirectStandardOutput = true;
            broker.StartInfo.RedirectStandardError = true;
            broker.Start();
            Console.WriteLine("started " + broker.StartInfo.FileName + broker.StartInfo.Arguments);

            var line = "";
            var sb = new StringBuilder();
            var lineCount = 0;
            while ((line = broker.StandardOutput.ReadLine()) != null && lineCount < 10) {
                sb.Append(line).Append("\n");
                lineCount++;
            }
            Console.WriteLine(sb.ToString());
            //Thread.Sleep(9000);
        }

        [TearDown]
        public void stopActiveMQBroker()
        {
            broker.Kill();
        }

        #region simulated server
        protected bool Connect()
        {
            var factory = new ConnectionFactory(new Uri(BROKER_URL));
            connection = factory.CreateConnection();
            session = connection.CreateSession(AcknowledgementMode.AutoAcknowledge);
            connection.Start();
            return true;
        }

        protected bool Disconnect()
        {
            session.Close();
            connection.Close();
            return true;
        }
        #endregion

        protected IMessageConsumer CreateQueueConsumer(string destination)
        {
            return session.CreateConsumer(session.GetQueue(destination));
        }

        protected IMessageProducer CreateQueueProducer(string destination)
        {
            return session.CreateProducer(session.GetQueue(destination));
        }

        protected ITextMessage CreateTextMessage(string messageBody)
        {
            return session.CreateTextMessage(messageBody);
        }

    }
}
