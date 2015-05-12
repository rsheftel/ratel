using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using ActiveMQClient;
using TradingScreenApiService.Database.Mappings;
using TradingScreenApiService.Position;
using TradingScreenApiService.TradingScreen;
using TradingScreenApiService.Util;


namespace TradingScreenApiService
{
    /// <summary>
    /// Glues everything together.
    /// 
    /// </summary>
    internal class TradingScreenPositionManager 
    {
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        string _brokerUrl;

        readonly ICollection<int> _orderEventIds = new List<int>();

        #region Components
        BrokerFactory _broker;
        readonly PositionCache _positionCache = new PositionCache();
        readonly FuturesSymbolMapper _fsm;
        TradingScreenApi _api;
        #endregion

        Timer _mapperReloadTimer;
        Timer _positionPurgeTimer;

        public TradingScreenPositionManager(FuturesSymbolMapper futuresSymbolMapper) {
            _fsm = futuresSymbolMapper;
        }

        public TradingScreenPositionManager(): this (new FuturesSymbolMapper()){
            
        }

        private void InitializeApi() {
            if (_api == null || _api.ConnectionDropped) {
                _api = new TradingScreenApi {
                    UserId = ApiUserId,
                    Password = ApiPassword,
                    Site = ApiSite,
                    PricingServer = ApiPricingServer
                };
            }
        }

        public string BrokerUrl {
            get {
                return _brokerUrl;
            }
            set {
                if (!value.Contains("failover:")) {
                    if (value.StartsWith("activemq:")) {
                        var sb = new StringBuilder(value);
                        sb.Replace("activemq:", "activemq:failover:");
                        _brokerUrl = sb.ToString();
                    } else {
                        _brokerUrl = "failover:" + value;
                    }
                } else {
                    _brokerUrl = value;
                }
            }
        }
        public string ApiUserId { get; set; }
        public string ApiPassword { get; set; }
        public bool IsPublisherStarted {
            get { return _broker.IsStarted; }
            
        }
        public ICollection<int> OrderEventIds {
            get { return _orderEventIds; }
        }
        public ICollection<IDictionary<string, string>> Positions
        {
            get { return _positionCache.Positions; }
        }

        public bool IsMapperReloadScheduled {
            get { return _mapperReloadTimer != null; }
        }
        public string DatabaseConnectionString {
            get; set;
        }
        public bool IsPositionPurgeScheduled {
            get { return _positionPurgeTimer != null; }
        }
        public string ApiSite { get; set; }
        public string ApiPricingServer { get; set; }

        public bool LoggedIn {
            get {
                InitializeApi();
                return _api.LoggedIn;
            }
        }
        public bool LoginStarted { get { return _api.LoginStarted; } }
        public bool ConnectionDropped
        {
            get {
                return _api != null && _api.ConnectionDropped;
            }
        }

        public void StartPublisher() {
            _log.Info("Using BrokerURL: " + BrokerUrl);
            _broker = BrokerFactory.Broker(BrokerUrl);
        }

        /// <summary>
        /// Event handler for order creation and/or update.                                                                                                                                                                                                                                                                                               
        /// </summary>
        /// <param name="orderId">the order id or multiple if more than one order is received.</param>
        /// <param name="xmlOrders"></param>
        /// <param name="uniqueId"></param>
        /// <param name="confirmed"></param>
        public void ReceiveOrders(string orderId, string xmlOrders, int uniqueId, ref bool confirmed) {
            // extract the orders, translate symbols, and update the cache
            var positionsToPublish = new List<IDictionary<string, string>>();

            lock (_orderEventIds) {
                try {
                    if (_orderEventIds.Contains(uniqueId)) return;
                    var orders = MessageUtil.ParseXmlToDictionary(xmlOrders, uniqueId);
                    foreach (var order in orders) {
                        // convert order into a trade record
                        if (MessageUtil.IsEquityOrder(order)) {
                            var ticker = EquityTickerUtil.ConvertRicToTicker(order["RIC"]);
                            order["BID"] = ticker;
                    
                        } else if (MessageUtil.IsFuturesOrder(order)) {
                            var ric = order["RIC"];
                            var ricRoot = FuturesSymbolUtil.ExtractSymbolRoot(ric);
                            var bbRoot = _fsm.ConvertRicRoot(ricRoot);
                            var monthYear = FuturesSymbolUtil.ExtractMaturityMonthFromSymbol(order["RIC"]);
                            var bbSymbol = FuturesSymbolUtil.CombineRootMaturityMonthYear(bbRoot, monthYear);
                            order["BID"] = bbSymbol;
                        } else {
                            continue;
                        }

                        var tradeRecord = MessageUtil.CreateTradeRecord(order);
                        var updatedPosition = _positionCache.Update(tradeRecord);
                        positionsToPublish.Add(updatedPosition);
                    }
                    _orderEventIds.Add(uniqueId);
                    confirmed = true;
                } catch (Exception e) {
                    _log.Error("Processing order events from TradingScreen", e);
                }
            }
            try {
                foreach (var position in positionsToPublish) {
                    position.Add("Source", "TradingScreen");
                    var strategy = position["Strategy"];
                    if (string.IsNullOrEmpty(strategy)) {
                        strategy = "MISSING";
                    }
                    var sb = new StringBuilder(512);
                    sb.Append("TradingScreen.Positions.").Append(strategy).Append(".").Append(position["BID"]);

                    _broker.Publish(sb.ToString(), position);
                }
            } catch (Exception e) {
                _log.Error("Publishing positions from TradingScreen", e);
            }

        }

        public int ScheduleMapperReload() {

            if (_mapperReloadTimer != null) {
                _mapperReloadTimer.Dispose();
                _mapperReloadTimer = null;
            }
            var minutesToWait = 15 - DateTime.Now.Minute % 15;

            if (minutesToWait == 0) {
                minutesToWait = 15;
            }

            _mapperReloadTimer = new Timer(ReloadMapper, null, minutesToWait * 1000 * 60, (int)TimeSpan.FromMinutes(15).TotalMilliseconds);

            return minutesToWait;
        }

        void ReloadMapper(object state) {
            _log.Info("Mapper load started");
            _fsm.Reload(DatabaseConnectionString);
            _log.Info("Mapper load completed");
        }

        public int LoadMapper() {
            _log.Info("Connecting to database using: " + DatabaseConnectionString);
            return _fsm.Initialize(DatabaseConnectionString);
        }

        public int SchedulePositionPurge() {
            if (_positionPurgeTimer != null) {
                _positionPurgeTimer.Dispose();
                _positionPurgeTimer = null;
            }

            var hoursToWait = 24 - DateTime.Now.Hour % 24;

            if (hoursToWait == 0) {
                hoursToWait = 24;
            }
            _positionPurgeTimer = new Timer(PurgePositions, null, hoursToWait * 1000 * 60 * 60, (int)TimeSpan.FromHours(24).TotalMilliseconds);

            return hoursToWait;
        }

        void PurgePositions(object state) {
            _log.Info("Position purge started");
            _positionCache.Clear();
            _log.Info("Position purge completed");
        }

        public bool LoginToTradingScreen() {
            InitializeApi();

            _log.Info("Clearing position cache");
            _positionCache.Clear();
            _log.Info("Clearing order event cache");
            _orderEventIds.Clear();

            if (!_api.Login()) return false;

            _log.Info(_api.LastLoginMessage);

            return !_api.LoginFailed && _api.LoggedIn;
        }

        public bool StartProcessingOrders() {
            return "OK" == _api.StartListeningForOrders(ReceiveOrders);
        }

        public bool LogoffTradingScreen() {
            return _api.Logout();
        }

    }
}
