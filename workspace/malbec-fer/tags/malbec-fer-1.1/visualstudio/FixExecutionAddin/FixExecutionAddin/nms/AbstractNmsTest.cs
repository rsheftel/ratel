using System;
using System.Collections.Generic;

namespace FixExecutionAddin.Nms
{
    public abstract class AbstractNmsTest
    {
        
        protected const string BrokerUrl = "tcp://localhost:60606";
        protected const string FerCommandQueue = "FER.Command";

        protected delegate bool WaitForHandler(Object source);
        protected delegate bool WaitForValueHandler(Object source, Object value);

        protected static Order CreateLimitOrder()
        {
            var order = new Order(OrderTest.CreateLimitOrderDictionary());

            return order;
        }

        protected static bool Response(Object source)
        {
            var nmsClient = source as NmsClientApp;
            return nmsClient != null ? nmsClient.ReceivedMessageCount > 0 : false;
        }

        protected static void PrintLine(string message, IEnumerable<KeyValuePair<string, string>> record)
        {
            if (!string.IsNullOrEmpty(message)) {
                Console.Write(message);
            }
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

        protected static void PrintLine(IEnumerable<KeyValuePair<string, string>> record)
        {
            PrintLine(null, record);
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
            return nmsSession != null ? nmsSession.Connected() : false;
        }

        protected static bool Disconnected(Object source) {
            var nmsSession = source as IConnectable;
            return nmsSession != null ? !nmsSession.Connected() : false;
        }
        #endregion

    }
}
