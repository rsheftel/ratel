using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;
using RediLib;

namespace RediToActiveMQ {
    /// <summary>
    /// Description of RediScraper.
    /// </summary>
    public class RediScraper : IDisposable {
        static readonly log4net.ILog log =
            log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        readonly string userId;
        readonly string password;
        // default values
        readonly string rediPath = @"C:\Program Files\GS\REDIPlus\Logon\REDIStart.exe";
        readonly string rediArgs = "/app:rediplus";
        readonly int rediStartupDelay = 64;

        readonly object lockObject = new object();
        readonly object eventLock = new object();

        readonly Queue<IDictionary<string, string>> executionQueue = new Queue<IDictionary<string, string>>();
        readonly Queue<IDictionary<string, string>> positionQueue = new Queue<IDictionary<string, string>>();
        readonly IList<string> watchedAccounts = new List<string>();

        Process rediProcess_;

        DateTime? lastRediPositionEvent;
        DateTime? lastRediMessageEvent;

        CacheControlClass messageCacheControl;
        CacheControlClass positionCacheControl;

        

        const string REDI_PROCESS_NAME = "Redi";

        #region Constructors
        public RediScraper(string userId, string password) {
            this.userId = userId;
            this.password = password;
        }

        public RediScraper(string userId, string password, AppConfiguration config) {
            this.userId = userId;
            this.password = password;

            if (!string.IsNullOrEmpty(config.RediPath)) rediPath = config.RediPath;

            if (!string.IsNullOrEmpty(config.RediPath)) rediArgs = config.RediArgs;

            rediStartupDelay = config.RediStartupDelay;
        }
        #endregion

        #region Properties
        public bool Connecting
        {
            get; set;
        }

        public bool Connected
        {
            get; set;
        }

        public bool Disconnecting
        {
            get; set;
        }

        internal DateTime? LastRediPositionEvent {
            get { 
                lock (eventLock) {
                    return lastRediPositionEvent;
                } 
            }
            set {
                lock (eventLock) {
                    lastRediPositionEvent = value;
                }
            }
        }

        DateTime? LastRediMessageEvent
        {
            get
            {
                lock (eventLock) {
                    return lastRediMessageEvent;
                }
            }
            set
            {
                lock (eventLock) {
                    lastRediMessageEvent = value;
                }
            }
        }
        public bool IsMessageListenerStarted { get; set; }
        public bool IsPositionListenerStarted { get; set; }
        public bool ShouldProcessEvents { get; set; }
        public int ExecutionCount {
            get { return executionQueue.Count; }
        }
        public int PositionCount {
            get { return positionQueue.Count; }
        }
        internal ICacheControl MessageCacheControl {
            get { return messageCacheControl; }
        }

        Process RediProcess {
            get {
                lock (lockObject) {
                    return rediProcess_;
                }
            }

            set {
                lock (lockObject) {
                    rediProcess_ = value;
                    if (rediProcess_ == null) return;
                    rediProcess_.EnableRaisingEvents = true;
                    rediProcess_.Exited += ProcessExitEventHandler;
                }
            }
        }
        #endregion

        #region Redi Process Handling
        public void ConnectMessageListener() 
        {
            if (IsMessageListenerStarted || Disconnecting) return;

            try {
                lock (lockObject) {
                    messageCacheControl = new CacheControlClass {UserID = userId, Password = password};
                    messageCacheControl.CacheEvent += MessageCacheEventHandler;
                    object errorCode = null;

                    var submittedAt = DateTime.Now;
                    // assume everything will be fine - the submit does not return until we have
                    // processed all the events, so it is easier to assume success
                    IsMessageListenerStarted = true;
                    var submitResult = messageCacheControl.Submit("Message", "true", ref errorCode);

                    OnStatusChanged(EventArgs.Empty);
                    log.Info(
                        "Submitted Message Listener at " + submittedAt + ", submitResult=" + submitResult +
                            ", errorCode=" + errorCode);
                }
            } catch (Exception e) {
                IsMessageListenerStarted = false;
                log.Error("Failed to connect message listener to Redi", e);
            }
        }

        /// <summary>
        /// Connect to the postion table.
        /// 
        /// Check at logical step to see if we have been requested to shutdown.
        /// </summary>
        public void ConnectPositionListener()
        {
            if (IsPositionListenerStarted || Disconnecting) return;

            try {
                lock (lockObject) {
                    Connecting = true;
                    positionCacheControl = new CacheControlClass {UserID = userId, Password = password};
                    positionCacheControl.CacheEvent += PositionCacheEventHandler;
                    object submitErrorCode = null;

                    var accountList = GetRediAccounts();
                    var submittedAt = DateTime.Now;
                    var submitResult = positionCacheControl.Submit("Position", "true", ref submitErrorCode);
                    foreach (var account in accountList) {
                        object watchErrorCode = null;
                        var watchResult = positionCacheControl.AddWatch(2, "", account, ref watchErrorCode);
                        watchedAccounts.Add(account);
                        log.Info("Watching account: " + account + ", result=" + watchResult+", errorCode=" + watchErrorCode);
                        if (Disconnecting) break;
                    }
                    IsPositionListenerStarted = true;
                    Connecting = false;
                    Connected = true;

                    OnStatusChanged(EventArgs.Empty);
                    log.Info(
                        "Submitted Position Listener at " + submittedAt + ", submitResult=" +
                            submitResult + ", errorCode=" + submitErrorCode);
                }
            } catch (Exception e) {
                IsPositionListenerStarted = false;
                log.Error("Failed to connect position listener to Redi", e);
            }
        }

        static string[] GetRediAccounts() {
            /*var order = new ORDERClass {UserID = userId, Password = password};

            object numberOfAccountsVariant = null;
            order.GetAccountCount(ref numberOfAccountsVariant);

            var numberOfAccounts = Convert.ToInt32(numberOfAccountsVariant);
            var accountList = new string[numberOfAccounts];

            for (var i =0; i < numberOfAccounts; i++) {
                object account = null;
                order.GetAccountX(i, ref account);
                accountList[i] = Convert.ToString(account);
            }
            return accountList;
             */

            return new[] {"00182087-T"};
        }

        public void DisconnectMessageListener()
        {
            lock (lockObject) {
                if (!IsMessageListenerStarted) {
                    return;
                }
            }
            try {
                object errorCode = null;
                var revokedResult = messageCacheControl.Revoke(ref errorCode);
                IsMessageListenerStarted = false;
                log.Info("Disconnect Message result: " + revokedResult + ", ErrorCode: " + errorCode);
            } catch (Exception e) {
                // it might not make sense to set this to true
                IsMessageListenerStarted = true;
                log.Error("Failed to disconnect message listener to Redi", e);
            }
        }

        public void DisconnectPositionListener()
        {
            try {
                lock (lockObject) {
                    if (!IsPositionListenerStarted) {
                        return;
                    }

                    foreach (var account in watchedAccounts) {
                        object watchErrorCode = null;
                        var watchResult = positionCacheControl.DeleteWatch(2, "", account, ref watchErrorCode);
                        log.Info("Removed watch on account: " + account + ", result=" + watchResult);
                    }

                    watchedAccounts.Clear();

                    object errorCode = null;
                    var revokedResult = positionCacheControl.Revoke(ref errorCode);
                    IsPositionListenerStarted = false;
                    log.Info("Disconnect Position result: " + revokedResult + ", ErrorCode: " + errorCode);
                }
            } catch (Exception e) {
                // it might not make sense to set this to true
                IsPositionListenerStarted = true;
                log.Error("Failed to disconnect position listener to Redi", e);
            }
        }

        public bool IsRediRunning() {
            lock (lockObject) {
                if (RediProcess == null) {
                    var processes = Process.GetProcessesByName(REDI_PROCESS_NAME);
                    if (processes.Length > 0) {
                        RediProcess = processes[0];
                    }
                }

                return (RediProcess != null && !RediProcess.HasExited);
            }
        }

        public void KillRedi() {
            lock (lockObject) {
                if (RediProcess == null) {
                    var processes = Process.GetProcessesByName(REDI_PROCESS_NAME);

                    foreach (var process in processes) {
                        log.Info("Killing process " + process.ProcessName + "(" + process.Id + ")");
                        process.Kill();
                    }
                } else {
                    RediProcess.Kill();
                    RediProcess = null;
                }
            }
        }

        /// <summary>
        /// This uses the REDIStart application, not Redi.
        /// </summary>
        /// <returns></returns>
        public bool StartRedi() {
            lock (lockObject) {
                if (IsRediRunning()) return true;

                log.Info("Starting Redi");
                var rediProcess = new Process {StartInfo = {UseShellExecute = false}};

                rediProcess.StartInfo.FileName = rediPath;

                var args = new StringBuilder(128);
                args.Append(rediArgs).Append(" ");
                args.Append(" /user:").Append(userId);
                args.Append(" /pwd:").Append(password);
                args.Append(" /autostart");

                rediProcess.StartInfo.Arguments = args.ToString();

                return rediProcess.Start();
            }
        }

        /// <summary>
        /// Check that Redi is in a state that will accept our COM connections.
        /// 
        /// This estimates based on the CPU time and/or the actual amount of time the 
        /// Redi process has been running.
        /// </summary>
        public bool CanConnectViaCom() {
            var processes = Process.GetProcessesByName(REDI_PROCESS_NAME);

            // Assumes we have 0 or 1 items
            foreach (var process in processes) {
                var tpt = process.TotalProcessorTime;
                var startTime = process.StartTime;
                var timeDiff = DateTime.Now - startTime;

                return (tpt.TotalSeconds >= rediStartupDelay || timeDiff.TotalMinutes >= 5);
            }

            return false;
        }

        public bool IsConnected()
        {
            // We are connected when we have a RediProcess, a Message Listener, a Position Listener and we can
            // get data from Redi via COM
            return  RediProcess != null && IsMessageListenerStarted && IsPositionListenerStarted;
        }

        public bool isPingable() 
        {
            return PingMessageTable() && PingPositionTable();
        }

        internal bool PingMessageTable()
        {
            log.Info("Pinging Redi MessageTable, last update at " + LastRediMessageEvent);
            RediError pingError; // = RediError.None;

            GetTableRowColumn(messageCacheControl, 0, RediTableConstants.MESSAGE_FILTER_COLUMN, out pingError);
            

            return pingError == RediError.None;
        }

        internal bool PingPositionTable()
        {
            log.Info("Pinging Redi PositionTable, last update at " + LastRediPositionEvent);
            RediError pingError; // = RediError.None;

            GetTableRowColumn(positionCacheControl, 0, RediTableConstants.POSITION_FILTER_COLUMN, out pingError);

            return pingError == RediError.None;
        }

        #endregion

        #region Queue Logic
        public IDictionary<string, string> GetNextPosition()
        {
            return positionQueue.Count > 0 ? positionQueue.Dequeue() : null;
        }

        public IDictionary<string, string> GetNextExecution()
        {
            return executionQueue.Count > 0 ? executionQueue.Dequeue() : null;
        }
        #endregion

        #region Callbacks
        void MessageCacheEventHandler(int action, int row) 
        {
            LastRediMessageEvent = DateTime.Now;
            var ca = CacheActionFor(action);

            if (!ShouldProcessEvents) {
                return;
            }

            log.Info("Action=" + action + " '" + ca + "', row=" + row);

            switch (ca) {
                case CacheAction.Submit:
                    // Initial callback, get all the rows on the table
                    // row equals the count not the actual row
                    for (var i = 0; i < row; ++i) {
                        var submitRowValues = GetTableRow(messageCacheControl, i, RediTableConstants.MESSAGE_COLUMN_NAMES, RediTableConstants.MESSAGE_FILTER_COLUMN, RediTableConstants.EXECUTION_TYPE);

                        if (submitRowValues.Count <= 0) continue;
                        executionQueue.Enqueue(submitRowValues);
                        //log.Info("Row(" + i + "): " + DictionaryToString(submitRowValues));
                    }
                    break;
                case CacheAction.Insert:
                case CacheAction.Update:
                    // the row is the row index to get NOT the count
                    var insertRowValues = GetTableRow(messageCacheControl, row, RediTableConstants.MESSAGE_COLUMN_NAMES, RediTableConstants.MESSAGE_FILTER_COLUMN, RediTableConstants.EXECUTION_TYPE);
                    if (insertRowValues.Count > 0) {
                        executionQueue.Enqueue(insertRowValues);
                        //log.Info("Row(" + row + "): " + DictionaryToString(insertRowValues));
                    }
                    break;
                default:
                    log.Info("Ignoring event: " + ca);
                    break;
            }
        }

        void PositionCacheEventHandler(int action, int row)
        {
            LastRediPositionEvent = DateTime.Now;
            var ca = CacheActionFor(action);

            if (!ShouldProcessEvents) {
                return;
            }

            log.Debug("Action=" + action + " '" + ca + "', row=" + row);

            switch (ca) {
                case CacheAction.Submit:
                    // Initial callback, get all the rows on the table
                    // row equals the count not the actual row
                    for (var i = 0; i < row; ++i) {
                        var submitRowValues = GetTableRow(
                            positionCacheControl, i, RediTableConstants.POSITION_COLUMN_NAMES);

                        if (!PublishablePosition(submitRowValues)) continue;
                        positionQueue.Enqueue(submitRowValues);
                        //log.Info("Row(" + i + "): " + DictionaryToString(submitRowValues));
                    }
                    break;
                case CacheAction.Insert:
                case CacheAction.Update:
                    // the row is the row index to get NOT the count
                    var insertRowValues = GetTableRow(
                        positionCacheControl, row, RediTableConstants.POSITION_COLUMN_NAMES);
                    if (PublishablePosition(insertRowValues)) {
                        positionQueue.Enqueue(insertRowValues);
                        //log.Info("Row(" + row + "): " + DictionaryToString(insertRowValues));
                    }
                    break;
                default:
                    log.Info("Ignoring event: " + ca);
                    break;
            }
        }

        public static bool PublishablePosition(IDictionary<string, string> positionRow) {
            return positionRow.Count > 0 && (positionRow["SHARESSOLD"] != "0" || positionRow["SHARESBOUGHT"] != "0") ;
        }

        void ProcessExitEventHandler(object sender, EventArgs e)
        {
            lock (lockObject) {
                log.Info("Redi has exited");
                RediProcess = null;
                IsMessageListenerStarted = false;
                OnStatusChanged(e);
            }
        }

        #endregion

        #region Events
        public event EventHandler<EventArgs> StatusChanged;

        void OnStatusChanged(EventArgs ea) {
            if (StatusChanged != null) {
                StatusChanged(this, ea);
            }
        }
        #endregion

        #region IDisposable
        public void Dispose() {
            // clean up the Redi integration
        }
        #endregion

        #region Redi Helpers

        internal static IDictionary<string, string> GetTableRow(ICacheControl icc, int row, string[] columnNames) {
            return GetTableRow(icc, row, columnNames, null, null);
        }

        /// <summary>
        /// Get the row if it is an execution.
        /// </summary>
        /// <param name="icc"></param>
        /// <param name="row"></param>
        /// <param name="filterColumnName"></param>
        /// <param name="columnNames"></param>
        /// <param name="filterValue"></param>
        /// <returns></returns>
        internal static IDictionary<string, string> GetTableRow(ICacheControl icc, int row, string[] columnNames, string filterColumnName, string filterValue)
        {
            log.Debug("Retrieving row:" + row);

            var rowValues = new Dictionary<string, string>();
            RediError error; // = RediError.None;

            if (filterColumnName != null) {
                var rowType = GetTableRowColumn(icc, row, filterColumnName, out error);
                if (filterValue.ToUpperInvariant().Equals(rowType.ToUpperInvariant())) {
                    rowValues[filterColumnName] = rowType;
                } else {
                    return rowValues;
                }
            }
            foreach (var columnName in columnNames) {
                    rowValues[columnName.ToUpperInvariant()] = GetTableRowColumn(icc, row, columnName, out error);
            }

            return rowValues;
        }

        static string GetTableRowColumn(ICacheControl icc, int row, string columnName, out RediError errorCode) {
            try {
                object myValue = null;
                object myError = null;

                icc.GetCell(row, columnName, ref myValue, ref myError);
                errorCode = RediErrorFor(Convert.ToInt32(myError));

                if (RediError.None != errorCode)
                    log.Error(
                        "Received error: " + errorCode + ", when retrieving row (" + row + "), column '" + columnName +
                            "'");

                return myValue != null ? myValue.ToString() : "";
            } catch (Exception e) {
                errorCode = RediError.Exception;
                log.Error(e);
            }
            return "";
        }
        #endregion

        #region Static Helpers
        static CacheAction CacheActionFor(int actionValue) {
            if (Enum.IsDefined(typeof (CacheAction), actionValue)) return (CacheAction) Enum.ToObject(typeof (CacheAction), actionValue);
            return CacheAction.Unknown;
        }

        static RediError RediErrorFor(int errorValue) {
            if (Enum.IsDefined(typeof (RediError), errorValue)) return (RediError) Enum.ToObject(typeof (RediError), errorValue);
            return RediError.Unknown;
        }

        internal static string DictionaryToString(IEnumerable<KeyValuePair<string, string>> dictionary) {
            var sb = new StringBuilder(256);
            sb.Append("[");
            var processedFirstEntry = false;
            foreach (var keyValuePair in dictionary) {
                if (processedFirstEntry) sb.Append(", ");
                sb.Append(keyValuePair.Key).Append("=").Append(keyValuePair.Value);
                processedFirstEntry = true;
            }
            sb.Append("]");

            return sb.ToString();
        }
        #endregion

    }

    #region Enumerations
    public enum CacheAction {
        Submit = 1,
        Insert = 4,
        Update = 5,
        Removing = 7,
        Removed = 8,
        // incase we get something we don't know
        Unknown = 100
    }

    public enum RediError {
        None = 0, // This is defined as Unknown in the docs ...
        UserId = 1,
        Password = 2,
        Connect = 3,
        Cache = 4,
        Query = 5,
        Column = 6,
        // Our values
        Exception = 99,
        Unknown = 100
    }

    public enum PositionWatchType {
        PriceQuote = 0,
        MarketMaker = 1,
        Account = 2,
        Firm = 3
    }
    #endregion

}