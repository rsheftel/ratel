using System;
using System.Collections;
using System.Configuration;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Xml;
using Microsoft.Win32;
using Excel = Microsoft.Office.Interop.Excel;


// look for a file named NmsRtdClient.exe.config or NmsRtdClient.dll.config
//[assembly : log4net.Config.XmlConfigurator(Watch = true)]

/// <summary>
/// A NMS client that listens to Bloomberg realtime online position updates
/// </summary>
namespace NmsRtdClient
{
    [ComVisible(true), ProgId("AIM.ActiveMQ")]
    public class NmsRtdClient : Excel.IRtdServer
    {
        //private static readonly log4net.ILog log = log4net.LogManager.GetLogger("RtdClientApp.Logging");

        //private readonly IDictionary<int,string> topicMap = new Dictionary<int,string>();
        private NmsClient client;
        private Excel.IRTDUpdateEvent m_xlRTDUpdate;

        private String brokerUrl;

        #region IRtdServer Members

        /// <summary>
        /// Called when a new instance of the RTD function is created.
        /// </summary>
        /// <param name="TopicID"></param>
        /// <param name="Strings"></param>
        /// <param name="GetNewValues"></param>
        /// <returns></returns>-
        public object ConnectData(int TopicID, ref Array Strings, ref bool GetNewValues)
        {
            TopicDetails details = new TopicDetails();
            details.key = client.BuildPositionLookupKey((string)Strings.GetValue(0), (string)Strings.GetValue(1));
            details.valueField = (string) Strings.GetValue(1);
            details.topicID = TopicID;

            string jmsTopic = client.extractJmsTopic((string) Strings.GetValue(0));
            client.StartListeningTo(jmsTopic, details.key, details.valueField, details.topicID);

            // ensure we replace the value in the spreadsheet
            GetNewValues = true; 
            
            return client.lookupValue(details.key, details.valueField);
        }

        public void DisconnectData(int topicID)
        {
            // remove the topic from the list
            client.RemoveTopic(topicID);
        }

        public int Heartbeat()
        {
            if (client.isConnected())
            {
                return 1;
            } 
            else
            {
                return 0;
            }
            
        }

        /// <summary>
        /// Build the array of topics that have been updated.
        /// </summary>
        /// <remarks>
        /// To save time when transmitting and not to overwhelming Excel we can batch the cell
        /// updates here.  Each cell has a unique TopicID
        /// </remarks>
        /// <param name="TopicCount"></param>
        /// <returns></returns>
        public Array RefreshData(ref int topicCount)
        {
            IDictionary changedTopics = client.ConsumeChangedTopics();

            Object[,] updatedTopics = new Object[2, changedTopics.Count];

            int i = 0;

            foreach (int topicID in changedTopics.Keys)
            {
                updatedTopics[0, i] = topicID;
                updatedTopics[1, i] = changedTopics[topicID];
                i++;
            }
            topicCount = i;

            return updatedTopics;
        }

        /// <summary>
        /// Called when loaded by Excel.  Connect to the NMS Broker. 
        /// </summary>
        /// <param name="CallbackObject"></param>
        /// <returns></returns>
        public int ServerStart(Excel.IRTDUpdateEvent CallbackObject)
        {
            m_xlRTDUpdate = CallbackObject; 

            loadConfiguration();

            if (brokerUrl == null)
            {
                //brokerUrl = "tcp://localhost:61616";
                brokerUrl = "tcp://nysrv61:61616";
            }

            try
            {
                client = new NmsClient(CallbackObject,
                                       new String[]
                                           {
                                               "account", "securityId", "level1TagName"
                                           }, brokerUrl);
                // The level2-4 tags do not add anything to uniqueness
                // , "level2TagName", "level3TagName", "level4TagName"
                Thread m_WorkerThread = new Thread(client.NMSReaderLoop);
                m_WorkerThread.Name = "NMSListener";
                m_WorkerThread.Start();

                return 1;
            }
            catch (Exception e)
            {
                //log.Error("Caught exception during serverstart", e);
            }
            return -1;
        }

        public void ServerTerminate()
        {
            // Nothing to do here
            client.shutdown();
            client = null;
        }

        #endregion


        private void loadConfiguration()
        {
            String attributeName = "brokerUrl";

            Object l = Assembly.GetCallingAssembly().Location;
            XmlDocument xmlDoc = new XmlDocument();
            String configurationFileName = Assembly.GetExecutingAssembly().Location + ".config";
            xmlDoc.Load(configurationFileName);
            String queryStr = "configuration/appSettings";
            XmlElement AppSettingsNode = (XmlElement) xmlDoc.SelectSingleNode(queryStr);

            if (AppSettingsNode == null)
                return;

            queryStr += "/add[@key='"+attributeName +"']";
            XmlNodeList XmlNodeList = xmlDoc.SelectNodes(queryStr);
            XmlElement Node;

            if (XmlNodeList.Count > 0)
            {
                Node = (XmlElement)XmlNodeList[0];
                XmlAttributeCollection xmlAttributes = Node.Attributes;
                XmlNode value = xmlAttributes.GetNamedItem("value");
                brokerUrl = value.InnerText;
            }
        }
    }

    internal class TopicDetails
    {
        public string key;
        public string valueField;
        public int topicID;
    }
}