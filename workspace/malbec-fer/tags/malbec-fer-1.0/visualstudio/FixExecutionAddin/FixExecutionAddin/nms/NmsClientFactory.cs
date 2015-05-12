using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FixExecutionAddin.nms
{
    /// <summary>
    /// Keep track of NmsClients based on the brokerUrl.  
    /// </summary>
    public class NmsClientFactory
    {
        static readonly IDictionary<string, NmsClientApp> nmsClients = new Dictionary<string, NmsClientApp>();

        static readonly NmsClientFactory instance = new NmsClientFactory();


        public static NmsClientFactory Instance {
            get {
                return instance;
            }
        }

        public NmsClientApp getClientFor(string brokerUrl) {
            lock (instance) {
                if (nmsClients.ContainsKey(brokerUrl)) {
                    return nmsClients[brokerUrl];
                }
                var newClient = new NmsClientApp(brokerUrl);
                // The queues should not change...
                newClient.Configure(brokerUrl, "FER.Response", "FER.Command");
                nmsClients[brokerUrl] = newClient;
                
                newClient.Start();
                
                return newClient;
            }
        }
    }
}
