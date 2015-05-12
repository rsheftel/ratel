using System;
using System.Collections.Generic;
using System.Text;
using System.Collections;

namespace NmsRtdClient
{
    public class PositionCache
    {
        private readonly log4net.ILog log;

        private readonly IDictionary batchCache = new Hashtable();
        readonly IDictionary onlineCache = new Hashtable();

        private readonly object lockObject = new object();

        private static readonly PositionCache instance = new PositionCache();


        private PositionCache () {
            var configurationFileName = System.Reflection.Assembly.GetExecutingAssembly().Location + ".config";
            log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(configurationFileName));
            log = log4net.LogManager.GetLogger("Cache.Logging");
        }

        #region Properties
        public static PositionCache Instance {
            get
            {
                return instance;
            }
        }
        #endregion

        public string LookupBatchCacheValue(string key, string fieldName)
        {
            return LookupValueFromCache(batchCache, key, fieldName);
        }


        public string LookupOnlineCacheValue(string key, string fieldName)
        {
            return LookupValueFromCache(onlineCache, key, fieldName);
        }

        /// <summary>
        /// Lookup up an item from the specified cache using the specified key.  If the item
        /// is found return the specified field value.
        /// </summary>
        /// <param name="cache"></param>
        /// <param name="key"></param>
        /// <param name="fieldName"></param>
        /// <returns>the value or null</returns>
        private string LookupValueFromCache(IDictionary cache, string key, string fieldName)
        {
            lock (lockObject) {
                var positionRecord = (IDictionary)cache[key];
                if (positionRecord != null) {
                    var value = (String)positionRecord[fieldName];
                    if (value != null) {
                        return value;
                    }
                }
            }

            return null;
        }

        public int CountBatchCacheItems(string startsWith)
        {
            return CountCacheItems(startsWith, batchCache);
        }

        public int CountOnlineCacheItems(string startsWith)
        {
            return CountCacheItems(startsWith, onlineCache);
        }

        /// <summary>
        /// Count the items within the specified cache using the startsWith string to do a
        /// partial compare on the key.
        /// </summary>
        /// <param name="startsWith"></param>
        /// <param name="cache"></param>
        /// <returns>number of items that have keys starting with startWith</returns>
        private int CountCacheItems(string startsWith, IDictionary cache)
        {
            var count = 0;

            lock (lockObject) {
                foreach (DictionaryEntry entry in cache) {
                    if (entry.Key.ToString().StartsWith(startsWith)) {
                        count++;
                    }
                }
            }
            return count;
        }

        public int FindAllBatchFieldValues(string startsWith, string[] fieldNames, IDictionary book)
        {
            return FindAllFieldValues(startsWith, fieldNames, batchCache, book);
        }

        public int FindAllOnlineFieldValues(string startsWith, string[] fieldNames, IDictionary book)
        {
            return FindAllFieldValues(startsWith, fieldNames, onlineCache, book);
        }

        /// <summary>
        /// Search the specified map for items using the startsWith string, when found, add the item to the 'book'
        /// as the key, using the 'foundValue' as the value.
        /// </summary>
        /// <param name="startsWith"></param>
        /// <param name="fieldNames"></param>
        /// <param name="cache"></param>
        /// <param name="book"></param>
        /// <returns>Number of items found/added to the book</returns>
        private int FindAllFieldValues(string startsWith, ICollection<string> fieldNames, IDictionary cache, IDictionary book)
        {
             var count = 0;
             lock (lockObject) {
                foreach (DictionaryEntry entry in cache) {
                    if (!entry.Key.ToString().StartsWith(startsWith)) continue;

                    var record = (IDictionary)entry.Value;
                    var sb = new StringBuilder(100);

                    var fieldValues = new String[fieldNames.Count];
                    var index = 0;
                    var first = true;
                    foreach (var fieldName in fieldNames) {
                        if (!first) {
                            sb.Append("|");
                        }
                        var value = record[fieldName];
                        if (value != null) {
                            sb.Append(value.ToString());
                            fieldValues[index] = value.ToString();
                        } else {
                            log.Info("Null value for field: " + fieldName);
                        }
                        index++;
                        first = false;
                    }

                    var key = sb.ToString();

                    if (book.Contains(key)) continue;

                    book.Add(key, fieldValues);
                    count++;
                }
            }
            return count;
        }

        public void AddBatchCacheItem(string key, IDictionary record)
        {
            AddCacheItem(key, record, batchCache);
        }

        public void AddOnlineCacheItem(string key, IDictionary record)
        {
            AddCacheItem(key, record, onlineCache);
        }

        private void AddCacheItem(string key, IDictionary record, IDictionary cache)
        {
            lock (lockObject) {
                if (cache.Contains(key)) {
                    cache.Remove(key);
                }
                cache.Add(key, record);
            }
        }

        public void ClearCaches()
        {
            lock (lockObject) {
                batchCache.Clear();
                onlineCache.Clear();
                //log.Info("Cleared caches, dumping online cache");
                foreach (string key in onlineCache.Keys) {
                    log.Info(key);
                }
                //log.Info("Cleared caches, dumping batch cache");
                foreach (string key in batchCache.Keys) {
                    log.Info(key);
                }
                //log.Info("Dumped caches");
            }
        }
    }
}
