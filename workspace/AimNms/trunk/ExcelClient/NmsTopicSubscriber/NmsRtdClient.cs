using System;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Xml;
using Excel = Microsoft.Office.Interop.Excel;


// look for a file named NmsRtdClient.exe.config or NmsRtdClient.dll.config
[assembly : log4net.Config.XmlConfigurator(Watch = true)]

/// <summary>
/// A NMS client that listens to Bloomberg realtime online position updates
/// </summary>
namespace NmsRtdClient
{
    [ComVisible(true), ProgId("AIM.ActiveMQ")]
    public class NmsRtdClient : Excel.IRtdServer
    {
        private readonly log4net.ILog _log = log4net.LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        //private readonly IDictionary<int,string> topicMap = new Dictionary<int,string>();
        private NmsClient _client;
        private Excel.IRTDUpdateEvent _mXlRtdUpdate;

        private String _brokerUrl;

        public NmsRtdClient()
        {
            var configurationFileName = Assembly.GetExecutingAssembly().Location + ".config";
            log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(configurationFileName));
//            _log = log4net.LogManager.GetLogger("SubscriberApp.Logging");
        }

        #region IRtdServer Members

        /// <summary>
        /// Called when a new instance of the RTD function is created.
        /// </summary>
        /// <param name="topicId"></param>
        /// <param name="strings"></param>
        /// <param name="getNewValues"></param>
        /// <returns></returns>-
        public object ConnectData(int topicId, ref Array strings, ref bool getNewValues)
        {
            try {
                var rtdString = (string)strings.GetValue(0);
                if (rtdString == "" || rtdString.Trim().Length == 0) {
                    return "#Topic error - " + rtdString;
                }

                var details = new TopicDetails {
                    key = _client.BuildPositionLookupKey(rtdString, (string) strings.GetValue(1)),
                    valueField = (string) strings.GetValue(1),
                    topicID = topicId
                };

                var jmsTopic = _client.ExtractJmsTopic((string)strings.GetValue(0));
                _client.StartListeningTo(jmsTopic, details.key, details.valueField, details.topicID);

                // ensure we replace the value in the spreadsheet
                getNewValues = true;

                return _client.LookupValue(details.key, details.valueField);
            } catch (Exception e) {
                _log.Error("Unable to ConnectData", e);
                return "#ConnectData";
            }
        }

        public void DisconnectData(int topicId)
        {
            try {
                // remove the topic from the list
                _client.RemoveTopic(topicId);
            } catch (Exception e) {
                _log.Error("DisconnectData:", e);
            }
        }

        public int Heartbeat()
        {
            try {
                return _client.IsConnected() ? 1 : 0;
            } catch (Exception e) {
                _log.Error("Heartbeat:", e);
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
        /// <param name="topicCount"></param>
        /// <returns></returns>
        public Array RefreshData(ref int topicCount)
        {
            try {
                var changedTopics = _client.ConsumeChangedTopics();

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
                _log.Error("RefreshData:", e);
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
                _mXlRtdUpdate = callbackObject;

                LoadConfiguration();
                _log.Info("Starting AIM RTD server");
                if (_brokerUrl == null) {
                    //_brokerUrl = "tcp://localhost:61616";
                    _brokerUrl = "tcp://amqpositions:61616";
                }

                _client = new NmsClient(callbackObject,
                                       new[]
                                           {
                                               "account", "securityId", "level1TagName"
                                           }, _brokerUrl);
                // The level2-4 tags do not add anything to uniqueness
                // , "level2TagName", "level3TagName", "level4TagName"
                //Thread m_WorkerThread = new Thread(_client.NMSReaderLoop);
                //m_WorkerThread.Name = "NMSListener";
                //m_WorkerThread.Start();

                return 1;
            }
            catch (Exception e)
            {
                _log.Error("Caught exception during server start", e);
            }
            return -1;
        }

        public void ServerTerminate()
        {
            try {
                // Nothing to do here
                _client.Shutdown();
                _client = null;
                _log.Info("Stopping AIM RTD server");
            } catch (Exception e) {
                _log.Error("ServerTerminate:", e);
            }
        }

        #endregion


        private void LoadConfiguration()
        {
            const string attributeName = "brokerUrl";

            Object l = Assembly.GetCallingAssembly().Location;
            var xmlDoc = new XmlDocument();
            var configurationFileName = Assembly.GetExecutingAssembly().Location + ".config";
            xmlDoc.Load(configurationFileName);
            var queryStr = "configuration/appSettings";
            var appSettingsNode = (XmlElement) xmlDoc.SelectSingleNode(queryStr);

            if (appSettingsNode == null)
                return;

            queryStr += "/add[@key='"+attributeName +"']";
            var xmlNodeList = xmlDoc.SelectNodes(queryStr);
            XmlElement node;

            if (xmlNodeList != null)
                if (xmlNodeList.Count > 0)
                {
                    node = (XmlElement)xmlNodeList[0];
                    var xmlAttributes = node.Attributes;
                    var value = xmlAttributes.GetNamedItem("value");
                    _brokerUrl = value.InnerText;
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