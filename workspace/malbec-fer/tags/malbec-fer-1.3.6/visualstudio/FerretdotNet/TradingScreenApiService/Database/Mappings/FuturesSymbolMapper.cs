using System;
using System.Collections.Generic;
using TradingScreenApiService.Util;

namespace TradingScreenApiService.Database.Mappings
{
    /// <summary>
    /// Mapping interface for FuturesSymbols.
    /// 
    /// Maps the Bloomberg symbol root to the platform's sending and receiving symbol roots.
    /// </summary>
    public class FuturesSymbolMapper
    {
        readonly object _lockObject = new object();
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        readonly IDictionary<FuturesSymbolKey, FuturesSymbolMapping> _receivingSymbol = new Dictionary<FuturesSymbolKey, FuturesSymbolMapping>();
        readonly IDictionary<FuturesSymbolKey, FuturesSymbolMapping> _sendingSymbol = new Dictionary<FuturesSymbolKey, FuturesSymbolMapping>();

        readonly IDictionary<FuturesSymbolKey, FuturesSymbolMapping> _bloombergSymbol = new Dictionary<FuturesSymbolKey, FuturesSymbolMapping>();

        #region Populate From Database
        public int Initialize(string connectionString)
        {
            _log.Debug(connectionString);
            var badbContext = new BADBEntities(connectionString);
            return Initialize(badbContext);
        }

        public int Initialize(BADBEntities context)
        {
            return Reload(context);
        }

        /// <summary>
        /// Reload the mapping data.
        /// 
        /// Ensure that we do not end up with bad data if the database is broken.
        /// 
        /// </summary>
        /// <returns></returns>
        public int Reload(BADBEntities context)
        {
            var receivingSymbol = new Dictionary<FuturesSymbolKey, FuturesSymbolMapping>();
            var sendingSymbol = new Dictionary<FuturesSymbolKey, FuturesSymbolMapping>();
            var bloombergSymbol = new Dictionary<FuturesSymbolKey, FuturesSymbolMapping>();

            var mapping = context.FuturesSymbolMapping;

            var recordCount = 0;
            foreach (var symbolMapping in mapping) {
                AddMapping(symbolMapping, bloombergSymbol, sendingSymbol, receivingSymbol);
                recordCount++;
            }

            lock (_lockObject) {
                ClearAndAddAll(_bloombergSymbol, bloombergSymbol);
                ClearAndAddAll(_sendingSymbol, sendingSymbol);
                ClearAndAddAll(_receivingSymbol, receivingSymbol);
            }
            return recordCount;
        }

        public int Reload(string connectionString)
        {
            var badbContext = new BADBEntities(connectionString);
            return Reload(badbContext);
        }

        #endregion

        public void AddBloombergMapping(string platform, string bloombergRoot, string platformReceiveRoot, string platformSendRoot, decimal priceMultiplier)
        {

            var mapping = new FuturesSymbolMapping
            {
                PlatformId = platform.ToUpperInvariant(),
                BloombergSymbolRoot = bloombergRoot.ToUpperInvariant(),
                PlatformReceivingSymbolRoot = platformReceiveRoot.ToUpperInvariant(),
                PlatformSendingSymbolRoot = platformSendRoot.ToUpperInvariant(),
                PriceMultiplier = priceMultiplier
            };

            AddMapping(mapping);
        }

        internal void AddMapping(FuturesSymbolMapping mapping)
        {
            AddMapping(mapping, _bloombergSymbol, _sendingSymbol, _receivingSymbol);
        }

        internal static void AddMapping(FuturesSymbolMapping mapping, IDictionary<FuturesSymbolKey, FuturesSymbolMapping> bloombergSymbol, IDictionary<FuturesSymbolKey, FuturesSymbolMapping> sendingSymbol, IDictionary<FuturesSymbolKey, FuturesSymbolMapping>  receivingSymbol) 
        {
            // only the bloomberg symbol is required
            if (!string.IsNullOrEmpty(mapping.PlatformReceivingSymbolRoot)) {
                var receivingKey = new FuturesSymbolKey(mapping.PlatformId, mapping.PlatformReceivingSymbolRoot);
                receivingSymbol.Add(receivingKey, mapping);
            }

            // only the bloomberg symbol is required
            if (!string.IsNullOrEmpty(mapping.PlatformSendingSymbolRoot))
            {
                var sendingKey = new FuturesSymbolKey(mapping.PlatformId, mapping.PlatformSendingSymbolRoot);
                sendingSymbol.Add(sendingKey, mapping);
            }

            var bloombergKey = new FuturesSymbolKey(mapping.PlatformId, mapping.BloombergSymbolRoot);
            if (!bloombergSymbol.ContainsKey(bloombergKey)) {
                bloombergSymbol.Add(bloombergKey, mapping);
            } else {
                bloombergSymbol[bloombergKey] = mapping;
            }
        }

        internal static void ClearAndAddAll(IDictionary<FuturesSymbolKey, FuturesSymbolMapping> dst, IDictionary<FuturesSymbolKey, FuturesSymbolMapping> src) {
            lock (dst) {
                dst.Clear();
                foreach (var entry in src) {
                    dst.Add(entry.Key, entry.Value);
                }
            }
        }

        internal class FuturesSymbolKey : IEquatable<FuturesSymbolKey> {
            readonly string _platformId;
            readonly string _symbolRoot;

            internal FuturesSymbolKey(string platform, string symbolRoot)
            {
                _platformId = platform.ToUpperInvariant();
                _symbolRoot = symbolRoot.ToUpperInvariant();
            }

            public bool Equals(FuturesSymbolKey other) {
                if (ReferenceEquals(null, other)) return false;
                if (ReferenceEquals(this, other)) return true;
                return Equals(other._platformId, _platformId) && Equals(other._symbolRoot, _symbolRoot);
            }

            public override bool Equals(object obj) {
                if (ReferenceEquals(null, obj)) return false;
                if (ReferenceEquals(this, obj)) return true;
                return obj.GetType() == typeof (FuturesSymbolKey) && Equals((FuturesSymbolKey) obj);
            }

            public override int GetHashCode() {
                unchecked {
                    return (_platformId.GetHashCode() * 397)^_symbolRoot.GetHashCode();
                }
            }

            public static bool operator ==(FuturesSymbolKey left, FuturesSymbolKey right) {
                return Equals(left, right);
            }

            public static bool operator !=(FuturesSymbolKey left, FuturesSymbolKey right) {
                return !Equals(left, right);
            }
        }

        public string ConvertPlatformReceiving(string platform, string receivingRoot, bool returnRoot)
        {
            var receivingKey = new FuturesSymbolKey(platform, receivingRoot);

            lock (_receivingSymbol) {
                if (_receivingSymbol.ContainsKey(receivingKey)) {
                    return _receivingSymbol[receivingKey].BloombergSymbolRoot;
                }
            }

            return returnRoot ? receivingRoot : null;
        }

        public string LookupPlatformSendingRoot(string platform, string bloombergRoot, bool returnRoot) {
            var bloombergKey = new FuturesSymbolKey(platform, bloombergRoot);

            lock (_bloombergSymbol) {
                if (_bloombergSymbol.ContainsKey(bloombergKey)) {
                    return _bloombergSymbol[bloombergKey].PlatformSendingSymbolRoot;
                }
            }

            return returnRoot ? bloombergRoot : null;
        }

        public string LookupPlatformRoot(string platform, string bloombergRoot, bool returnRoot)
        {
            var bloombergKey = new FuturesSymbolKey(platform, bloombergRoot);

            lock (_bloombergSymbol) {
                if (_bloombergSymbol.ContainsKey(bloombergKey)) {
                    return _bloombergSymbol[bloombergKey].PlatformReceivingSymbolRoot;
                }
            }

            return returnRoot ? bloombergRoot : null;
        }

        public decimal LookupToBloombergPriceMultiplier(string platform, string bloombergRoot) {
            var bloombergKey = new FuturesSymbolKey(platform, bloombergRoot);
            lock (_bloombergSymbol) {
                return _bloombergSymbol.ContainsKey(bloombergKey)
                    ? _bloombergSymbol[bloombergKey].PriceMultiplier
                    : decimal.One;
            }
        }

        public decimal LookupToPlatformPriceMultiplier(string platform, string bloombergRoot)
        {
            return decimal.One / LookupToBloombergPriceMultiplier(platform, bloombergRoot);
        }

        public string MapPlatformRootToBloombergSymbol(string platform, string platformRoot, string monthYear) {
            var bloombergRoot = ConvertPlatformReceiving(platform, platformRoot, true);

            return FuturesSymbolUtil.CombineRootMaturityMonthYear(bloombergRoot, monthYear);
        }

        /// <summary>
        /// return the RIC root to a Bloomberg root
        /// </summary>
        /// <param name="ricRoot"></param>
        /// <returns></returns>
        public string ConvertRicRoot(string ricRoot)
        {
            // we are going to cheat and use the TradingScreen platform mappings
            var sendingKey = new FuturesSymbolKey("TRADS", ricRoot);

            lock (_sendingSymbol) {
                return _sendingSymbol.ContainsKey(sendingKey) ? _sendingSymbol[sendingKey].BloombergSymbolRoot : null;
            }
        }
    }
}
