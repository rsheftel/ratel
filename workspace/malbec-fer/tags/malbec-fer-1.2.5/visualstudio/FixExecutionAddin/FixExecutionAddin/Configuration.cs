using System.Xml;
using System.Reflection;

namespace FixExecutionAddin
{
    static class Configuration
    {
        internal static bool loaded;
        private static string brokerUrl = "tcp://localhost:60606";  // needs to match the local ActiveMQ configuration

        public static string BrokerUrl
        {
            get
            {
                
                loadConfiguration();
                return brokerUrl;
            }
        }


        /// <summary>
        /// Load the configuration properties.
        /// 
        /// Currently we are only looking for brokerUrl.
        /// </summary>
        private static void loadConfiguration()
        {
            if (loaded) {
                return;
            }

            var attributeName = "brokerUrl";

            //Object l = Assembly.GetCallingAssembly().Location;
            var xmlDoc = new XmlDocument();
            var configurationFileName = Assembly.GetExecutingAssembly().Location + ".config";
            xmlDoc.Load(configurationFileName);
            var queryStr = "configuration/appSettings";
            var AppSettingsNode = (XmlElement)xmlDoc.SelectSingleNode(queryStr);

            if (AppSettingsNode == null) {
                return;
            }

            queryStr += "/add[@key='" + attributeName + "']";
            var XmlNodeList = xmlDoc.SelectNodes(queryStr);
            XmlElement Node;

            if (XmlNodeList != null)
                if (XmlNodeList.Count > 0) {
                    Node = (XmlElement)XmlNodeList[0];
                    var xmlAttributes = Node.Attributes;
                    var value = xmlAttributes.GetNamedItem("value");
                    brokerUrl = value.InnerText;
                }

            loaded = true;
        }
    }
}
