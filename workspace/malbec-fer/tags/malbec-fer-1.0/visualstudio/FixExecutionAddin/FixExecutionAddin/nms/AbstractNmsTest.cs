using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using NUnit.Framework;

namespace FixExecutionAddin.nms
{
    public class AbstractNmsTest
    {
        
        protected const string BROKER_URL = "tcp://localhost:60606";
        protected const string FER_RESPONSE = "FER.Response";
        protected const string FER_COMMAND = "FER.Command";

        protected delegate bool WaitForHandler(Object source);
        protected delegate bool WaitForValueHandler(Object source, Object value);

        protected static Order CreateLimitOrder()
        {
            var order = new Order {
                orderType = "LIMIT",
                symbol = "ZZVTZ",
                side = "SELL",
                limitPrice = 16.90m,
                securityType = "EQUITY",
                quantity = 25m,
                ClientOrderID = ("CS-" + DateTime.Now.Millisecond),
                platform = "TESTSERVER"
            };

            return order;
        }

        protected static bool Response(Object source)
        {
            var nmsClient = source as NmsClientApp;
            return nmsClient.ReceivedMessageCount > 0;
        }

        protected static void PrintLine(IEnumerable<KeyValuePair<string, string>> record)
        {
            var first = true;
            foreach (var pair in record) {
                if (!first) {
                    Console.Write(", ");
                }
                Console.Write(pair);
                first = false;
            }
            Console.WriteLine();
        }

        #region waiting logic
        protected static void WaitFor(WaitForHandler d, Object source, long waitPeriod) {
            //Console.WriteLine("Starting to wait:" + DateTime.Now);
            var timedOut = DateTime.Now.AddMilliseconds(waitPeriod);
            while (DateTimeOffset.Now < timedOut && !d(source)) {
                //Console.WriteLine("looped");
            }
            //Console.WriteLine("Finished waiting:" + DateTime.Now);
        }

        protected static bool Connected(Object source) {
            var nmsSession = source as IConnectable;
            return nmsSession.Connected();
        }

        
        protected static bool Disconnected(Object source) {
            var nmsSession = source as IConnectable;
            return !nmsSession.Connected();
        }
        #endregion

    }
}
