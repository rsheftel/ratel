using System;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Xml;
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
        //private static readonly log4net.ILog log = log4net.LogManager.GetLogger("SubscriberApp.Logging");
        private readonly log4net.ILog log;

        //private readonly IDictionary<int,string> topicMap = new Dictionary<int,string>();
        private NmsClient client;
        private Excel.IRTDUpdateEvent m_xlRTDUpdate;

        private String brokerUrl;

        public NmsRtdClient()
        {
            var configurationFileName = Assembly.GetExecutingAssembly().Location + ".config";
            log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(configurationFileName));
            log = log4net.LogManager.GetLogger("SubscriberApp.Logging");
        }

        #region IRtdServer Members

        /// <summary>
        /// Called when a new instance of the RTD function is created.
        /// </summary>
        /// <param name="topicId"></param>
        /// <param name="Strings"></param>
        /// <param name="GetNewValues"></param>
        /// <returns></returns>-
        public object ConnectData(int topicId, ref Array Strings, ref bool GetNewValues)
        {
            try {
                var rtdString = (string)Strings.GetValue(0);
                if (rtdString == "" || rtdString.Trim().Length == 0) {
                    return "#Topic error - " + rtdString;
                }

                var details = new TopicDetails {
                    key = client.BuildPositionLookupKey(rtdString, (string) Strings.GetValue(1)),
                    valueField = ((string) Strings.GetValue(1)),
                    topicID = topicId
                };

                var jmsTopic = NmsClient.ExtractJmsTopic((string)Strings.GetValue(0));
                client.StartListeningTo(jmsTopic, details.key, details.valueField, details.topicID);

                // ensure we replace the value in the spreadsheet
                GetNewValues = true;

                return client.LookupValue(details.key, details.valueField);
            } catch (Exception e) {
                log.Error("Unable to ConnectData", e);
                return "#ConnectData";
            }
        }

        public void DisconnectData(int topicId)
        {
            try {
                // remove the topic from the list
                client.RemoveTopic(topicId);
            } catch (Exception e) {
                log.Error("DisconnectData:", e);
            }
        }

        public int Heartbeat()
        {
            try {
                return client.IsConnected() ? 1 : 0;
            } catch (Exception e) {
                log.Error("Heartbeat:", e);
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
        /// <returns></returns>
        public Array RefreshData(ref int topicCount)
        {
            try {
                var changedTopics = client.ConsumeChangedTopics();
                var updatedTopics = new Object[2, changedTopics.Count];
                var i = 0;

                foreach (int topicId in changedTopics.Keys) {
                    updatedTopics[0, i] = topicId;
                    updatedTopics[1, i] = changedTopics[topicId];
                    i++;
                }
                topicCount = i;

                return updatedTopics;
            } catch (Exception e) {
                log.Error("RefreshData:", e);
                topicCount = 0;
                return new Object[2, 0];
            }
        }

        /// <summary>
        /// Called when loaded by Excel.  Connect to the NMS Broker. 
        /// </summary>
        /// <param name="callbackObject"></param>
        /// <returns></returns>
        public int ServerStart(Excel.IRTDUpdateEvent callbackObject)
        {

            try
            {
                m_xlRTDUpdate = callbackObject;

                LoadConfiguration();
                log.Info("Starting AIM RTD server");
                if (brokerUrl == null) {
                    //brokerUrl = "tcp://localhost:61616";
                    brokerUrl = "tcp://amqpositions:61616";
                }

                client = new NmsClient(callbackObject,
                                       new[]
                                           {
                                               "account", "securityId", "level1TagName", "normOpenPosition"
                                           }, brokerUrl);
                // The level2-4 tags do not add anything to uniqueness
                // , "level2TagName", "level3TagName", "level4TagName"
                //Thread m_WorkerThread = new Thread(client.NMSReaderLoop);
                //m_WorkerThread.Name = "NMSListener";
                //m_WorkerThread.Start();

                return 1;
            }
            catch (Exception e)
            {
                log.Error("Caught exception during server start", e);
            }
            return -1;
        }

        public void ServerTerminate()
        {
            try {
                // Nothing to do here
                client.Shutdown();
                client = null;
                log.Info("Stopping AIM RTD server");
            } catch (Exception e) {
                log.Error("ServerTerminate:", e);
            }
        }

        #endregion


        private void LoadConfiguration()
        {
            var attributeName = "brokerUrl";

            Object l = Assembly.GetCallingAssembly().Location;
            var xmlDoc = new XmlDocument();
            var configurationFileName = Assembly.GetExecutingAssembly().Location + ".config";
            xmlDoc.Load(configurationFileName);
            var queryStr = "configuration/appSettings";
            var AppSettingsNode = (XmlElement) xmlDoc.SelectSingleNode(queryStr);

            if (AppSettingsNode == null)
                return;

            queryStr += "/add[@key='"+attributeName +"']";
            var XmlNodeList = xmlDoc.SelectNodes(queryStr);

            if (XmlNodeList== null || XmlNodeList.Count <= 0) return;

            var Node = (XmlElement)XmlNodeList[0];
            var xmlAttributes = Node.Attributes;
            var value = xmlAttributes.GetNamedItem("value");
            brokerUrl = value.InnerText;
        }
    }

    internal class TopicDetails
    {
        public string key;
        public string valueField;
        public int topicID;
    }
}