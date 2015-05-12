using System;
using System.Collections.Generic;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Threading;
using Microsoft.Office.Interop.Excel;

namespace ActiveMQExcel {
    [ComVisible(true), ProgId("RTNow")] 
    public class NowRtdServer : IRtdServer {
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        readonly IDictionary<string, double> _nowCache = new Dictionary<string, double>();
        readonly IDictionary<string, IList<int>> _patternTotopic = new Dictionary<string, IList<int>>();
        readonly IDictionary<string, Timer> _timerCache = new Dictionary<string, Timer>();
        readonly IDictionary<int, string> _topicToPattern = new Dictionary<int, string>();

        readonly IList<string> _updatedPattern = new List<string>();

        IRTDUpdateEvent _updateEventHandler;

        public int TimerCount {
            get { return _timerCache.Count; }
        }
        #region IRtdServer Members
        public int ServerStart(IRTDUpdateEvent callbackObject) {
            _updateEventHandler = callbackObject;
            AppConfiguration.TryToLoadLog4Net();

            return 1;
        }

        public object ConnectData(int topicId, ref Array strings, ref bool getNewValues) {
            try {
                getNewValues = true; // always update
                var pattern = (string) strings.GetValue(0);

                var ourPattern = string.IsNullOrEmpty(pattern) ? "FRQ:5S" : pattern.ToUpperInvariant();

                if (!_timerCache.ContainsKey(ourPattern))
                    if (ourPattern.StartsWith("FRQ:")) {
                        var value = ourPattern.Substring(4, ourPattern.Length - 5);
                        var timeUnit = ourPattern.Substring(ourPattern.Length - 1, 1);
                        var period = TimeSpan.FromSeconds(5);

                        switch (timeUnit) {
                            case "S":
                                period = TimeSpan.FromSeconds(Convert.ToDouble(value));
                                break;
                            case "M":
                                period = TimeSpan.FromMinutes(Convert.ToDouble(value));
                                break;
                            case "H":
                                period = TimeSpan.FromHours(Convert.ToDouble(value));
                                break;
                        }

                        var timer = new Timer(TimerCallbackHandler, ourPattern, period, period);
                        _timerCache.Add(ourPattern, timer);
                        _nowCache[ourPattern] = DateTime.Now.ToOADate();
                    } else if (ourPattern == "TODAY") {
                        var tomorrow = DateTime.Now.Date.AddDays(1);
                        var timeToWait = tomorrow - DateTime.Now;

                        var timer = new Timer(TimerCallbackHandler, ourPattern, timeToWait, TimeSpan.FromHours(24));
                        _timerCache.Add(ourPattern, timer);

                        _nowCache[ourPattern] = DateTime.Now.Date.ToOADate();
                    } else return "#Invalid pattern: " + pattern;

                // Keep track so we can clean up timers and do proper updates
                if (!_topicToPattern.ContainsKey(topicId)) _topicToPattern.Add(topicId, ourPattern);
                if (_patternTotopic.ContainsKey(ourPattern)) {
                    var topicList = _patternTotopic[ourPattern];
                    topicList.Add(topicId);
                } else {
                    var topicList = new List<int> {topicId};
                    _patternTotopic.Add(ourPattern, topicList);
                }

                return _nowCache[ourPattern];
            } catch (Exception exception) {
                _log.Error(exception);
            }

            return "#Error";
        }

        public Array RefreshData(ref int topicCount) {
            try {
                IDictionary<int, double> refreshedData = new Dictionary<int, double>();

                foreach (var pattern in _updatedPattern) {
                    var topics = _patternTotopic[pattern];
                    var value = _nowCache[pattern];

                    foreach (var topic in topics) refreshedData.Add(topic, value);
                }

                _updatedPattern.Clear();

                var updatedTopics = new Object[2,refreshedData.Count];
                var i = 0;
                foreach (var nowEntry in refreshedData) {
                    updatedTopics[0, i] = nowEntry.Key;
                    updatedTopics[1, i] = nowEntry.Value;
                    i++;
                }

                topicCount = i;

                return updatedTopics;
            } catch (Exception exception) {
                _log.Error(exception);
            }

            return new Object[2,0];
        }

        public void DisconnectData(int topicId) {
            try {
                var pattern = _topicToPattern[topicId];
                var topicList = _patternTotopic[pattern];

                topicList.Remove(topicId);

                if (topicList.Count != 0) return;

                var timer = _timerCache[pattern];
                timer.Dispose();
                _timerCache.Remove(pattern);
            } catch (Exception exception) {
                _log.Error(exception);
            }
        }

        public int Heartbeat() {
            return 1;
        }

        public void ServerTerminate() {
            foreach (var timer in _timerCache) timer.Value.Dispose();
            _timerCache.Clear();
        }
        #endregion
        void TimerCallbackHandler(object state) {
            var pattern = state.ToString();

            _nowCache[pattern] = "TODAY" == pattern ? DateTime.Now.Date.ToOADate() : DateTime.Now.ToOADate();

            if (!_updatedPattern.Contains(pattern)) _updatedPattern.Add(pattern);
            _updateEventHandler.UpdateNotify();
        }
    }
}