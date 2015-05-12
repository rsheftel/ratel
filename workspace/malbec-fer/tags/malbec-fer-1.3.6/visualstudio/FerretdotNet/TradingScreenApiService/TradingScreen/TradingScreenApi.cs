using System;
using System.Collections.Generic;
using System.Threading;
using TSCom;

namespace TradingScreenApiService.TradingScreen {
    public class TradingScreenApi {
        readonly object _lockObject = new object();
        readonly log4net.ILog _log =
            log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        readonly ManualResetEvent _loginSemaphore = new ManualResetEvent(false);
        readonly ManualResetEvent _logoutSemaphore = new ManualResetEvent(false);
        readonly IList<_IServerEvents_Order2EventHandler> _orderHandlers = new List<_IServerEvents_Order2EventHandler>();
        readonly IServer _server;
        readonly ApplicationClass _tsApp;

       
        public TradingScreenApi() {
            _tsApp = new ApplicationClass();
            _server = _tsApp.Server;

            ((_IServerEvents_Event) _server).LoginProgress += LoginProgress;
            _log.Info(_tsApp.Version);
            _log.Info(_tsApp.APIVersion);
            _log.Info(_tsApp.JvmVersion);
        }

        public bool LoggedOut { get; private set; }

        public bool LoginFailed { get; private set; }

        public bool LoginStarted { get; private set; }

        public string Password { get; set; }

        public string UserId { get; set; }
        public string Site { get; set; }
        public string PricingServer { get; set; }

        public bool LoggedIn {
            get {
                lock (_lockObject) {
                    return _server.CheckLogin();
                    //return _tsApp.IsLoggedIn;
                }
            }
        }
        public string LastLoginMessage { get; private set; }
        bool ListeningForOrders { get; set; }

        void LoginProgress(string message, int uniqueId) {
            LastLoginMessage = message;
            _log.Info(message);
            Console.WriteLine(message);

            if (message.StartsWith("LoginFail") && "LoginFail: ForcedLogoff" != message) {
                LoginFailed = true;
                LoginStarted = false;
            }

            switch (message) {
                case "LoginOK":
                    LoginStarted = false;
                    _loginSemaphore.Set();
                    break;
                case "Logoff OK":
                    LoggedOut = true;
                    _logoutSemaphore.Set();
                    break;
                case "ConnectionDropped":
                    LoggedOut = true;
                    LoginStarted = false;
                    ConnectionDropped = true;
                    break;
            }
        }

        public bool ConnectionDropped { get; private set; }

        internal bool StartLogonProcess() {
            LoginFailed = false;
            LoginStarted = true;
            Console.WriteLine(LastLoginMessage);
            return _tsApp.Login(UserId, Password, Site, PricingServer);
        }

        public bool Login() {
            lock (_lockObject) {
                if (StartLogonProcess()) _loginSemaphore.WaitOne(TimeSpan.FromMinutes(10), true);
            }
            return LoggedIn;
        }

        public bool Logout() {
            lock (_lockObject) {
                if (ListeningForOrders) {
                    foreach (var handler in _orderHandlers) ((_IServerEvents_Event) _server).Order2 -= handler;
                    ListeningForOrders = false;
                    _orderHandlers.Clear();
                }

                LoginStarted = false;

                if (_tsApp.Logoff()) _logoutSemaphore.WaitOne(TimeSpan.FromMinutes(1));
            }
            return !LoggedIn;
        }

        public string StartListeningForOrders(_IServerEvents_Order2EventHandler handler) {
            if (!ListeningForOrders) {
                if (!_orderHandlers.Contains(handler)) {
                    ((_IServerEvents_Event) _server).Order2 += handler;
                    _orderHandlers.Add(handler);
                }
                ListeningForOrders = true;
                return _server.RestoreOrders();
            }

            return "AlreadyListening";
        }
    }
}