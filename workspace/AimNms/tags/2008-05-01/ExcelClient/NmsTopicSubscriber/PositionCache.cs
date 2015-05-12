using System;
using System.Text;
//using System.Text.RegularExpressions;
using System.Collections;

namespace NmsRtdClient
{
    public class PositionCache
    {
        public static readonly IDictionary batchCache = new Hashtable();
        public static readonly IDictionary onlineCache = new Hashtable();


        public static String lookupBatchCacheValue(String key, String fieldName)
        {
            return lookupValueFromCache(batchCache, key, fieldName);
        }


        public static String lookupOnlineCacheValue(String key, String fieldName)
        {
            return lookupValueFromCache(onlineCache, key, fieldName);
        }

        /// <summary>
        /// Lookup up an item from the specified cache using the specified key.  If the item
        /// is found return the specified field value.
        /// </summary>
        /// <param name="cache"></param>
        /// <param name="key"></param>
        /// <param name="fieldName"></param>
        /// <returns>the value or null</returns>
        private static String lookupValueFromCache(IDictionary cache, String key, String fieldName)
        {
            IDictionary positionRecord = (IDictionary)cache[key];
            if (positionRecord != null)
            {
                String value = (String)positionRecord[fieldName];
                if (value != null)
                {
                    return value;
                }
            }

            return null;
        }

        public static int countBatchCacheItems(String startsWith)
        {
            return countCacheItems(startsWith, batchCache);
        }

        public static int countOnlineCacheItems(String startsWith)
        {
            return countCacheItems(startsWith, onlineCache);
        }

        /// <summary>
        /// Count the items within the specified cache using the startsWith string to do a
        /// partial compare on the key.
        /// </summary>
        /// <param name="startsWith"></param>
        /// <param name="cache"></param>
        /// <returns>number of items that have keys starting with startWith</returns>
        private static int countCacheItems(String startsWith, IDictionary cache)
        {
            int count = 0;

            foreach (DictionaryEntry entry in cache)
            {
                if (entry.Key.ToString().StartsWith(startsWith))
                {
                    count++;
                }
            }
            return count;
        }

        public static int findAllBatchFieldValues(String startsWith, String[] fieldNames, IDictionary book)
        {
            return findAllFieldValues(startsWith, fieldNames, batchCache, book);
        }

        public static int findAllOnlineFieldValues(String startsWith, String[] fieldNames, IDictionary book)
        {
            return findAllFieldValues(startsWith, fieldNames, onlineCache, book);
        }

        /// <summary>
        /// Search the specified map for items using the startsWith string, when found, add the time to the 'book'
        /// as the key, using the 'foundValue' as the value.
        /// </summary>
        /// <param name="startsWith"></param>
        /// <param name="fieldName"></param>
        /// <param name="cache"></param>
        /// <param name="book"></param>
        /// <param name="foundValue"></param>
        /// <returns>Number of items found/added to the book</returns>
        private static int findAllFieldValues(String startsWith, String[] fieldNames, IDictionary cache, IDictionary book)
        {
            int count = 0;
            foreach (DictionaryEntry entry in cache)
            {
                if (entry.Key.ToString().StartsWith(startsWith))
                {
                    IDictionary record = (IDictionary)entry.Value;
                    StringBuilder sb = new StringBuilder(100);

                    String[] fieldValues = new String[fieldNames.Length];
                    int index = 0;
                    Boolean first = true;
                    foreach (String fieldName in fieldNames) 
                    {
                        if (!first)
                        {
                            sb.Append("|");
                        }
                        sb.Append(record[fieldName].ToString());
                        fieldValues[index] = record[fieldName].ToString();
                        index++;
                        first = false;
                    }

                    String key = sb.ToString();

                    if (!book.Contains(key))
                    {
                        book.Add(key, fieldValues);
                        count++;
                    }
                }
            }
            return count;
        }

        public static void addBatchCacheItem(String key, IDictionary record)
        {
            addCacheItem(key, record, batchCache);
        }

        public static void addOnlineCacheItem(String key, IDictionary record)
        {
            addCacheItem(key, record, onlineCache);
        }

        private static void addCacheItem(String key, IDictionary record, IDictionary cache)
        {
            if (cache.Contains(key))
            {
                cache.Remove(key);
            }
            cache.Add(key, record);
        }

        public static void clearCaches()
        {
            batchCache.Clear();
            onlineCache.Clear();
        }


    }
}
