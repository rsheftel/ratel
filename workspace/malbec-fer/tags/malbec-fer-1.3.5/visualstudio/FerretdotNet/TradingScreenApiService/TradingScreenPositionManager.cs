
// Configure log4net using the .config file
using System;
using Apache.NMS;
using Apache.NMS.ActiveMQ;

[assembly: log4net.Config.XmlConfigurator(Watch = true)]

namespace TradingScreenApiService
{
    /// <summary>
    /// Glues everything together.
    /// 
    /// </summary>
    internal class TradingScreenPositionManager
    {
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        IConnection _brokerConnection;

        /// <summary>
        /// Connect to the specified broker URL.
        /// 
        /// This assumes that it is ActiveMQ and that we are using failover.  All other
        /// brokers and URLs may fail.
        /// </summary>
        /// <param name="brokerUrl"></param>
        public void ConnectToBroker(string brokerUrl) {
            var factory = new ConnectionFactory(brokerUrl);
            _brokerConnection = factory.CreateConnection();
        }
    }
}
