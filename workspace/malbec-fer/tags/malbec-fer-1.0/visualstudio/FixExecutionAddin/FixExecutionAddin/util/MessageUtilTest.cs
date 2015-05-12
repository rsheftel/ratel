using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using NUnit.Framework;

namespace FixExecutionAddin.util
{
    public class MessageUtilTest
    {
        [TestFixture]
        public class TestTextMessageUtil
        {
            const string MESSAGE_STRING = "fieldNameFour=fieldValueFour|fieldNameOne=fieldValueOne|fieldNameThree=fieldValueThree|fieldNameTwo=fieldValueTwo";

            [Test]
            public void testMessageCreate()
            {
                IDictionary<string, string> testMessage = new SortedDictionary<string, string> {
                    {"fieldNameOne", "fieldValueOne"},
                    {"fieldNameTwo", "fieldValueTwo"},
                    {"fieldNameThree", "fieldValueThree"},
                    {"fieldNameFour", "fieldValueFour"}
                };

                var messageString = MessageUtil.CreateMessage(testMessage);
                Assert.AreEqual(MESSAGE_STRING, messageString.Substring(0, 113));
                Assert.AreEqual(true, messageString.Contains("MSTimestamp"));
            }

            [Test]
            public void testMessageCreateSimple()
            {
                var messageString = MessageUtil.CreateMessage("TestTopic", "fieldNameOne", "fieldValueOne");
                Assert.AreEqual(true, messageString.Contains("MSTimestamp"));
                Assert.AreEqual(true, messageString.Contains("MSTopicName"));
                Assert.AreEqual(true, messageString.Contains("TestTopic"));
                Assert.AreEqual(true, messageString.Contains("fieldNameOne"));
                Assert.AreEqual(true, messageString.Contains("fieldValueOne"));
            }

            [Test]
            public void testMessageExtract()
            {
                var testMessage = MessageUtil.ExtractRecord(MESSAGE_STRING);

                Assert.AreEqual(4, testMessage.Count);
                Assert.AreEqual(true, testMessage.Keys.Contains("fieldNameOne"));
                Assert.AreEqual(true, testMessage.Keys.Contains("fieldNameTwo"));
                Assert.AreEqual(true, testMessage.Keys.Contains("fieldNameThree"));
                Assert.AreEqual(true, testMessage.Keys.Contains("fieldNameFour"));
                Assert.AreEqual(false, testMessage.Keys.Contains("MSTimestamp"));
                Assert.AreEqual(false, testMessage.Keys.Contains("MSTopicName"));

                Assert.AreEqual(true, testMessage.Values.Contains("fieldValueOne"));
                Assert.AreEqual(true, testMessage.Values.Contains("fieldValueTwo"));
                Assert.AreEqual(true, testMessage.Values.Contains("fieldValueThree"));
                Assert.AreEqual(true, testMessage.Values.Contains("fieldValueFour"));
            }
        }

    }
}
