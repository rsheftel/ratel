using System;
using System.Diagnostics;
using System.Text;

namespace ActiveMQClientTest {
    public abstract class AbstractNmsBaseTest
    {
        private readonly Process _broker = new Process();

        //[SetUp]
        protected void StartActiveMQBroker()
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

        //[TearDown]
        protected void StopActiveMQBroker()
        {
            if (!_broker.HasExited)
                _broker.Kill();
        }
    }
}