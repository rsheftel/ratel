using System;
using System.Diagnostics;
using System.Text;
using Apache.NMS;
using Apache.NMS.ActiveMQ;

namespace FixExecutionAddinTest.Nms {
    public class AbstractNmsSetupTest : AbstractNmsTest
    {
        IConnection _connection;
        ISession _session;

        readonly Process _broker = new Process();

        protected const string Nyws802 = "failover:tcp://nyws802:60606";

        protected void StartActiveMQBroker()
        {
            var javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            var activeMQHome = Environment.GetEnvironmentVariable("ACTIVEMQ_HOME");

            if (String.IsNullOrEmpty(activeMQHome)) {
                activeMQHome = @"C:\Developer\apache-activemq-5.2.0";
            }

            _broker.StartInfo.Arguments = "-Dcom.sun.management.jmxremote -Xmx128M " +
                @"-Dorg.apache.activemq.UseDedicatedTaskRunner=false -jar " +
                    activeMQHome + @"\bin\run.jar start";

            _broker.StartInfo.FileName = @javaHome + @"\bin\java";

            _broker.StartInfo.UseShellExecute = false;
            _broker.StartInfo.RedirectStandardInput = true;
            _broker.StartInfo.RedirectStandardOutput = true;
            _broker.StartInfo.RedirectStandardError = true;
            _broker.Start();
            Console.WriteLine("started " + _broker.StartInfo.FileName + _broker.StartInfo.Arguments);


            //ActiveMQ Message Broker
            ReadProcesStdOut(_broker);
      
            //Thread.Sleep(9000);
        }

        protected void StopActiveMQBroker()
        {
            _broker.Kill();
            var stdout = _broker.StandardOutput.ReadToEnd();
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
            _connection = factory.CreateConnection();
            _session = _connection.CreateSession(AcknowledgementMode.AutoAcknowledge);
            _connection.Start();
            return true;
        }

        protected bool Disconnect()
        {
            _session.Close();
            _connection.Close();
            return true;
        }
        #endregion

        protected IMessageConsumer CreateQueueConsumer(string destination)
        {
            return _session.CreateConsumer(_session.GetQueue(destination));
        }

        protected IMessageProducer CreateQueueProducer(string destination)
        {
            return _session.CreateProducer(_session.GetQueue(destination));
        }

        protected  IMessageProducer CreateQueueProducer(IDestination destination)
        {
            return _session.CreateProducer(destination);
        }

        protected IMessageProducer CreateTopicProducer(string topic)
        {
            var destination = _session.GetTopic(topic);
            var producer = _session.CreateProducer(destination);
            return producer;
        }

        protected ITextMessage CreateTextMessage(string messageBody)
        {
            return _session.CreateTextMessage(messageBody);
        }
    }
}