using System;
using System.Reflection;
using System.Runtime.InteropServices;
using Microsoft.Office.Interop.Excel;

namespace ActiveMQExcel {
    [ComVisible(true), ProgId("AIM.ActiveMQ")]
    public class NmsRtdClient : IRtdServer
    {
        private readonly log4net.ILog _log = log4net.LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        private NmsClient _client;
        private IRTDUpdateEvent _mXlRtdUpdate;
        readonly AppConfiguration _config;

        public NmsRtdClient(AppConfiguration config) {
            _config = config;
        }

        public NmsRtdClient()
        {
            _config = AppConfiguration.Load();
        }

        public NmsClient Client {
            get { return _client; }
            private set { _client = value;}
        }
        public int TopicCount {
            get { return Client.TopicCount; }
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

                var details = new TopicMapDetails {
                    Key = _client.BuildPositionLookupKey(rtdString, (string) strings.GetValue(1)),
                    ValueField = (string) strings.GetValue(1),
                    TopicId = topicId
                };

                var jmsTopic = _client.ExtractJmsTopic(rtdString);
                _client.StartListeningTo(jmsTopic, details.Key, details.ValueField, details.TopicId);

                // ensure we replace the value in the spreadsheet
                getNewValues = true;

                return _client.LookupValue(details.Key, details.ValueField);
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

                var updatedTopics = new object[2, changedTopics.Count];

                var i = 0;

                foreach (var topicId in changedTopics.Keys) {
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
        public int ServerStart(IRTDUpdateEvent callbackObject)
        {
            try
            {
                _mXlRtdUpdate = callbackObject;

                _log.Info("Starting AIM RTD server");
                Client = new NmsClient(callbackObject,
                    new[]
                    {
                        "account", "securityId", "level1TagName"
                    }, _config.PositionsBrokerUrl);
                // The level2-4 tags do not add anything to uniqueness
                // , "level2TagName", "level3TagName", "level4TagName"

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
    }

    internal class TopicMapDetails
    {
        public string Key;
        public string ValueField;
        public int TopicId;
    }
}