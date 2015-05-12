using System.Collections.Generic;
using ActiveMQExcel;
using NUnit.Framework;

namespace ActiveMQExcelTest {
    [TestFixture]
    public class PositionCacheTest
    {

        [Test]
        public void TestLookupBatchCacheItem() {
            var cache = PositionCache.Instance;
            var cacheItem = new Dictionary<string, string>();
            cacheItem["ITEM1"] = "Item1";
            cacheItem["ITEM2"] = "Item2";

            cache.AddBatchCacheItem("TEST-KEY1", cacheItem);

            var lookedUpItem = cache.LookupBatchCacheValue("TEST-KEY1", "ITEM1");
            Assert.AreEqual("Item1", lookedUpItem);

            var lookedUpItem2 = cache.LookupBatchCacheValue("TEST-KEY1", "ITEM2");
            Assert.AreEqual("Item2", lookedUpItem2);
        }

        [Test]
        public void TestLookupOnlineCacheItem()
        {
            var cache = PositionCache.Instance;
            var cacheItem = new Dictionary<string, string>();
            cacheItem["ITEM1"] = "Item1";
            cacheItem["ITEM2"] = "Item2";

            cache.AddOnlineCacheItem("TEST-KEY1", cacheItem);

            var lookedUpItem = cache.LookupOnlineCacheValue("TEST-KEY1", "ITEM1");
            Assert.AreEqual("Item1", lookedUpItem);

            var lookedUpItem2 = cache.LookupOnlineCacheValue("TEST-KEY1", "ITEM2");
            Assert.AreEqual("Item2", lookedUpItem2);
        }

        [Test]
        public void TestFindAllBatchValues() {
            var cache = PositionCache.Instance;
            var cacheItem = new Dictionary<string, string>();
            cacheItem["ITEM1"] = "Item1";
            cacheItem["ITEM2"] = "Item2";
            
            cache.AddBatchCacheItem("TEST-KEY1", cacheItem);
            
            var allItems = new Dictionary<string, string[]>();
            var fields = new string[2];
            fields[0] = "ITEM1";
            fields[1] = "ITEM2";

            var count = cache.FindAllBatchFieldValues("TEST", fields, allItems);

            Assert.AreEqual(1, count);
            Assert.AreEqual(1, allItems.Count);

            Assert.IsTrue(allItems.ContainsKey("Item1|Item2"));
            var value = allItems["Item1|Item2"];
            Assert.IsNotNull(value);
            Assert.AreEqual(2, value.Length);
        }

        [Test]
        public void TestFindAllOnlineValues()
        {
            var cache = PositionCache.Instance;
            var cacheItem = new Dictionary<string, string>();
            cacheItem["ITEM1"] = "Item1";
            cacheItem["ITEM2"] = "Item2";

            cache.AddOnlineCacheItem("TEST-KEY1", cacheItem);

            var allItems = new Dictionary<string, string[]>();
            var fields = new string[2];
            fields[0] = "ITEM1";
            fields[1] = "ITEM2";

            var count = cache.FindAllOnlineFieldValues("TEST", fields, allItems);

            Assert.AreEqual(1, count);
            Assert.AreEqual(1, allItems.Count);

            Assert.IsTrue(allItems.ContainsKey("Item1|Item2"));
            var value = allItems["Item1|Item2"];
            Assert.IsNotNull(value);
            Assert.AreEqual(2, value.Length);
        }

    }
}