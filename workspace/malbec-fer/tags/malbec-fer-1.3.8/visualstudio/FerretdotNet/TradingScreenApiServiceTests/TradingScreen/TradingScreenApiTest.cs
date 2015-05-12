using System;
using System.Threading;
using NUnit.Framework;
using TradingScreenApiService.TradingScreen;

namespace TradingScreenApiServiceTests.TradingScreen
{
    [TestFixture]
    public class TradingScreenApiTest
    {
        [Test]
        public void TestLogonThreaded() {

            var api = new TradingScreenApi {UserId = "malbec2_uat", Password = "trader888", Site = "UAT Prod B", PricingServer = "tcp://uatprod.trandingscreen.net:9901"};

            Assert.IsFalse(api.LoggedIn);
            Assert.IsFalse(api.LoginStarted);
            Assert.IsTrue(api.StartLogonProcess());
            Assert.IsTrue(api.LoginStarted);
            WaitFor(LoggedIn, api, 60000);
            Assert.IsTrue(api.LoggedIn);
            Assert.IsNotNull(api.LastLoginMessage);

            // The API will state we are logged in before we receive the LoginOK status message.
            WaitFor(LoginFinished, api, 30000);
            Assert.IsFalse(api.LoginStarted);

            Assert.AreEqual("OK", api.StartListeningForOrders(TestOrderEventHandler));
            // wait for a second in case we have orders to receive 
            Thread.Sleep(1000);

            Assert.IsTrue(api.Logout());
        }

        [Test]
        public void TestLogon()
        {
            var api = new TradingScreenApi { UserId = "malbec2_uat", Password = "trader888", Site = "UAT Prod B", PricingServer = "tcp://uatprod.trandingscreen.net:9901" };

            Assert.IsFalse(api.LoggedIn);
            Assert.IsFalse(api.LoginStarted);
            Assert.IsTrue(api.Login());
            Assert.IsFalse(api.LoginStarted);
            Assert.IsTrue(api.LoggedIn);
            Assert.IsNotNull(api.LastLoginMessage);


            Assert.AreEqual("OK", api.StartListeningForOrders(TestOrderEventHandler));
            // wait for a second in case we have orders to receive 
            Thread.Sleep(1000);

            Assert.IsTrue(api.Logout());
        }


        static void TestOrderEventHandler(string orderid, string xml, int uniqueid, ref bool confirmed) {
            Console.WriteLine("Received Orders: " + xml);
        }

        static bool LoggedIn(object source) {
            var api = (TradingScreenApi)source;

            return api.LoggedIn;
        }

        static bool LoginFinished(object source)
        {
            var api = (TradingScreenApi)source;

            return !api.LoginStarted && api.LoggedIn;
        }

        #region Test Wait Logic
        delegate bool WaitForHandler(object source);

        static void WaitFor(WaitForHandler d, Object source, long waitPeriod)
        {
            //Console.WriteLine("Starting to wait:" + DateTime.Now);
            var timedOut = DateTime.Now.AddMilliseconds(waitPeriod);
            while (DateTimeOffset.Now < timedOut && !d(source)) {
                //Console.WriteLine("looped");
            }
            //Console.WriteLine("Finished waiting:" + DateTime.Now);
        }
        #endregion
    }
}
