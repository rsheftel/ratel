using System;
using System.Diagnostics;
using System.Text;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using NUnit.Framework;

namespace FixExecutionAddin.Nms
{
    public class AbstractNmsSetupTest : AbstractNmsTest
    {
        IConnection connection;
        ISession session;

        readonly Process broker = new Process();


        [SetUp]
        public void StartActiveMQBroker()
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


            //ActiveMQ Message Broker
        
            ReadProcesStdOut(broker);
      
            //Thread.Sleep(9000);
        }

        [TearDown]
        public void StopActiveMQBroker()
        {
            broker.Kill();
            var stdout = broker.StandardOutput.ReadToEnd();
            Console.WriteLine("stdout length: " + stdout.Length);
        }

        static void ReadProcesStdOut(Process process) 
        {
            string line;
            var sb = new StringBuilder();
            var lineCount = 0;
            while ((line = process.StandardOutput.ReadLine()) != null && !line.Contains("ActiveMQ JMS Message Broker")) {
                sb.Append(line).Append("\n");
                lineCount++;
            }
            if (line != null) sb.Append(line).Append("\n");
            sb.Append("Lines read: ").Append(lineCount);
            Console.WriteLine(sb.ToString());
        }

        #region simulated server
        protected bool Connect()
        {
            var factory = new ConnectionFactory(new Uri(BrokerUrl));
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

        protected  IMessageProducer CreateQueueProducer(IDestination destination)
        {
            return session.CreateProducer(destination);
        }

        protected ITextMessage CreateTextMessage(string messageBody)
        {
            return session.CreateTextMessage(messageBody);
        }
    }
}
