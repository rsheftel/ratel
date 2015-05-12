using System;
using Microsoft.Office.Interop.Excel;
using NUnit.Framework;

namespace ActiveMQExcelTest
{
    [TestFixture]
    public class MockRangeTest
    {
        [Test]
        public void TestTwoDimensionRangeCreate() {
            var lengths = new[] {1, 2};
            var lowerBounds = new[] {1, 1};

            // create the array in Excel format and convert to what we need
            var myArray = Array.CreateInstance(typeof(object), lengths, lowerBounds);
            var value2 = (object[,])myArray;

            value2[1, 1] = "one";
            value2[1, 2] = "two";

            var range = new MockRange(value2, 1, 1);

            var value = range.Value2;
            Assert.IsNotNull(value);
            Assert.IsTrue(typeof(object[,]) == value.GetType());
            Assert.AreEqual(2, range.Count);
            Assert.AreEqual(1, range.Row);

            Assert.AreEqual(1, range.Column);

            Assert.AreEqual(1, range.Rows.Count);
            Assert.IsTrue(typeof(object[,]) == range.Rows.Value2.GetType());
            Assert.AreEqual(2, range.Columns.Count);
            Assert.IsTrue(typeof(object[,]) == range.Columns.Value2.GetType());
            Assert.AreEqual(2, range.Cells.Count);

            var getValue = range.get_Value(XlRangeValueDataType.xlRangeValueDefault);
            Assert.IsNotNull(getValue);
            Assert.IsTrue(typeof(object[,]) == getValue.GetType());
            var asObjectArray = (object[,]) getValue;
            Assert.AreEqual(1, asObjectArray.GetLength(0));
            Assert.AreEqual(2, asObjectArray.GetLength(1));
            Assert.AreEqual(1, asObjectArray.GetLowerBound(0));
            Assert.AreEqual(1, asObjectArray.GetLowerBound(1));
        }

        [Test]
        public void TestTwoDimensionRangeFromArray()
        {
            var range = new MockRange(new[] { "One", "two"}, 1, 1, true);

            var value = range.Value2;
            Assert.IsNotNull(value);
            Assert.IsTrue(typeof(object[,]) == value.GetType());
            Assert.AreEqual(2, range.Count);
            Assert.AreEqual(1, range.Row);

            Assert.AreEqual(1, range.Column);

            Assert.AreEqual(1, range.Rows.Count);
            Assert.IsTrue(typeof(object[,]) == range.Rows.Value2.GetType());
            Assert.AreEqual(2, range.Columns.Count);
            Assert.IsTrue(typeof(object[,]) == range.Columns.Value2.GetType());
            Assert.AreEqual(2, range.Cells.Count);

            var getValue = range.get_Value(XlRangeValueDataType.xlRangeValueDefault);
            Assert.IsNotNull(getValue);
            Assert.IsTrue(typeof(object[,]) == getValue.GetType());
            var asObjectArray = (object[,])getValue;
            Assert.AreEqual(1, asObjectArray.GetLength(0));
            Assert.AreEqual(2, asObjectArray.GetLength(1));
            Assert.AreEqual(1, asObjectArray.GetLowerBound(0));
            Assert.AreEqual(1, asObjectArray.GetLowerBound(1));
        }

        [Test]
        public void TestCellRange()
        {
            var range = new MockRange("simple_string", 1, 1);

            var value = range.Value2;
            Assert.IsNotNull(value);
            Assert.IsTrue(typeof(string) == value.GetType());
            Assert.AreEqual(1, range.Count);

            Assert.AreEqual(1, range.Row);
            Assert.AreEqual(1, range.Column);
            
            Assert.AreEqual(1, range.Rows.Count);
            Assert.IsTrue(typeof(string) == range.Rows.Value2.GetType());
            Assert.AreEqual(1, range.Columns.Count);
            Assert.IsTrue(typeof(string) == range.Columns.Value2.GetType());
            Assert.AreEqual(1, range.Cells.Count);

            var getValue = range.get_Value(XlRangeValueDataType.xlRangeValueDefault);
            Assert.IsNotNull(getValue);
            Assert.IsTrue(typeof(string) == getValue.GetType());
        }
    }
}
