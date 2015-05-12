using System.Collections.Generic;

namespace FixExecutionAddin.Nms
{
    /// <summary>
    /// Keep track of NmsClients based on the brokerUrl.  
    /// </summary>
    public class NmsClientFactory
    {
        static readonly IDictionary<string, NmsClientApp> nmsClients = new Dictionary<string, NmsClientApp>();

        static readonly NmsClientFactory instance = new NmsClientFactory();

        private NmsClientFactory() {}

        public static NmsClientFactory Instance {
            get {
                return instance;
            }
        }

        public static NmsClientApp GetClientFor(string brokerUrl) {
            lock (instance) {
                if (nmsClients.ContainsKey(brokerUrl)) {
                    var client = nmsClients[brokerUrl];
                    // ensure that we are started
                    client.Start();
                    return client;
                }
                var newClient = new NmsClientApp(brokerUrl);
                // The queues should not change...
                newClient.Configure(brokerUrl, "FER.Command");
                nmsClients[brokerUrl] = newClient;
                
                newClient.Start();
                
                return newClient;
            }
        }
    }
}
