using System;
using System.Collections.Generic;
using System.Text;

namespace ActiveMQExcel {
    public class PositionCache
    {
        private readonly log4net.ILog _log;

        readonly IDictionary<string, IDictionary<string, string>> _batchCache = new Dictionary<string, IDictionary<string, string>>();
        readonly IDictionary<string, IDictionary<string, string>> _onlineCache = new Dictionary<string, IDictionary<string, string>>();

        private readonly object _lockObject = new object();

        private static readonly PositionCache _instance = new PositionCache();


        private PositionCache () {
            var configurationFileName = System.Reflection.Assembly.GetExecutingAssembly().Location + ".config";
            log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(configurationFileName));
            _log = log4net.LogManager.GetLogger("Cache.Logging");
        }

        public static PositionCache Instance {
            get {
                return _instance;
            }
        }

        public string LookupBatchCacheValue(string key, string fieldName)
        {
            return LookupValueFromCache(_batchCache, key, fieldName);
        }


        public string LookupOnlineCacheValue(string key, string fieldName)
        {
            return LookupValueFromCache(_onlineCache, key, fieldName);
        }

        /// <summary>
        /// Lookup up an item from the specified cache using the specified key.  If the item
        /// is found return the specified field value.
        /// </summary>
        /// <param name="cache"></param>
        /// <param name="key"></param>
        /// <param name="fieldName"></param>
        /// <returns>the value or null</returns>
        private string LookupValueFromCache(IDictionary<string, IDictionary<string ,string>> cache, string key, string fieldName)
        {
            lock (_lockObject) {
                if (cache.ContainsKey(key)) {
                    var positionRecord = cache[key];
                    if (positionRecord != null) {
                        if (positionRecord.ContainsKey(fieldName)) {
                            var value = positionRecord[fieldName];
                            if (value != null) {
                                return value;
                            }
                        }
                    }
                }
            }

            return null;
        }

        public int CountBatchCacheItems(string startsWith)
        {
            return CountCacheItems(startsWith, _batchCache);
        }

        public int CountOnlineCacheItems(string startsWith)
        {
            return CountCacheItems(startsWith, _onlineCache);
        }

        /// <summary>
        /// Count the items within the specified cache using the startsWith string to do a
        /// partial compare on the key.
        /// </summary>
        /// <param name="startsWith"></param>
        /// <param name="cache"></param>
        /// <returns>number of items that have keys starting with startWith</returns>
        private int CountCacheItems(string startsWith, IEnumerable<KeyValuePair<string, IDictionary<string, string>>> cache)
        {
            var count = 0;

            lock (_lockObject) {
                foreach (var entry in cache) {
                    if (entry.Key.StartsWith(startsWith)) {
                        count++;
                    }
                }
            }
            return count;
        }

        public int FindAllBatchFieldValues(string startsWith, string[] fieldNames, IDictionary<string, string[]> book)
        {
            return FindAllFieldValues(startsWith, fieldNames, _batchCache, book);
        }

        public int FindAllOnlineFieldValues(string startsWith, string[] fieldNames, IDictionary<string, string[]> book)
        {
            return FindAllFieldValues(startsWith, fieldNames, _onlineCache, book);
        }

        /// <summary>
        /// Search the specified map for items using the startsWith string, when found, add the time to the 'book'
        /// as the key, using the 'foundValue' as the value.
        /// </summary>
        /// <param name="startsWith"></param>
        /// <param name="fieldNames"></param>
        /// <param name="cache"></param>
        /// <param name="book"></param>
        /// <returns>Number of items found/added to the book</returns>
        private int FindAllFieldValues(string startsWith, ICollection<string> fieldNames, IEnumerable<KeyValuePair<string, IDictionary<string, string>>> cache, IDictionary<string, string[]> book)
        {
            var count = 0;
            lock (_lockObject) {
                foreach (var entry in cache) {
                    if (!entry.Key.StartsWith(startsWith)) continue;

                    var record = entry.Value;
                    var sb = new StringBuilder(100);

                    var fieldValues = new String[fieldNames.Count];
                    var index = 0;
                    var first = true;
                    foreach (var fieldName in fieldNames) {
                        if (!first) {
                            sb.Append("|");
                        }
                        if (!record.ContainsKey(fieldName)) continue;
                        var fieldValue = record[fieldName];
                        if (fieldValue == null) continue;
                        sb.Append(fieldValue);
                        fieldValues[index] = fieldValue;
                        index++;
                        first = false;
                    }

                    var key = sb.ToString();

                    if (book.ContainsKey(key)) continue;

                    book.Add(key, fieldValues);
                    count++;
                }
            }
            return count;
        }

        public void AddBatchCacheItem(string key, IDictionary<string, string> record)
        {
            AddCacheItem(key, record, _batchCache);
        }

        public void AddOnlineCacheItem(string key, IDictionary<string, string> record)
        {
            AddCacheItem(key, record, _onlineCache);
        }

        private void AddCacheItem(string key, IDictionary<string, string> record, IDictionary<string, IDictionary<string, string>> cache)
        {
            lock (_lockObject) {
                if (cache.ContainsKey(key)) {
                    cache.Remove(key);
                }
                cache.Add(key, record);
            }
        }

        public void ClearCaches()
        {
            lock (_lockObject) {
                _batchCache.Clear();
                _onlineCache.Clear();
                //_log.Info("Cleared caches, dumping online cache");
                foreach (var key in _onlineCache.Keys) {
                    _log.Info(key);
                }
                //_log.Info("Cleared caches, dumping batch cache");
                foreach (var key in _batchCache.Keys) {
                    _log.Info(key);
                }
                //_log.Info("Dumped caches");
            }
        }
    }
}