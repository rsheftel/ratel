using System;
using System.Collections.Generic;
using ActiveMQExcel;
using NUnit.Framework;

namespace ActiveMQExcelTest {
    [TestFixture]
    public class AimAddinTest
    {
        [Test]
        public void TestAllSecurities() {
            var udf = new AimAddin();

            object updateFlag = null;
            var results = udf.AIM_AllSecurities("TEST", updateFlag);
            Assert.IsNotNull(results);
            Assert.IsTrue(typeof(object[,]) == results.GetType());
            var myObject = (object[,]) results;
            var rows = myObject.GetLength(0);
            var columns = myObject.GetLength(1);
            Assert.AreEqual(0, rows);
            Assert.AreEqual(2, columns);

            var positionCache = PositionCache.Instance;
            var cacheItem = new Dictionary<string, string>();
            cacheItem["ITEM1"] = "Item1";
            cacheItem["ITEM2"] = "Item2";

            positionCache.AddBatchCacheItem("TEST-KEY1", cacheItem);
            var lookupResult = positionCache.LookupBatchCacheValue("TEST-KEY1", "ITEM1");
            Assert.IsNotNull(lookupResult);

            results = udf.AIM_AllSecurities("TEST", updateFlag);
            Assert.IsNotNull(results);
            Assert.IsTrue(typeof(object[,]) == results.GetType());
            myObject = (object[,])results;
            rows = myObject.GetLength(0);
            columns = myObject.GetLength(1);
            Assert.AreEqual(1, rows);
            Assert.AreEqual(2, columns);
        }

        [Test]
        public void TestArraySort()
        {
            var myArray = new object[3, 2];
            myArray[2, 0] = "A";
            myArray[2, 1] = "a";

            myArray[1, 0] = "B";
            myArray[1, 1] = "b";

            myArray[0, 0] = "C";
            myArray[0, 1] = "c";


            Console.WriteLine(myArray);
            var sortedArray = AimAddin.Sort(myArray);
            Console.WriteLine(sortedArray);

        }
    }
}