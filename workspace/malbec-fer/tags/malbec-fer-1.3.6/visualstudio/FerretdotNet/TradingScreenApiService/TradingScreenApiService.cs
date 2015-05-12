using System;
using System.ServiceProcess;
using System.Threading;
using TradingScreenApiService.Email;

namespace TradingScreenApiService {
    public partial class TradingScreenApiService : ServiceBase {
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        readonly TradingScreenPositionManager _app;
        Timer _loginTimer;
        readonly object _lockObject = new object();

        public TradingScreenApiService() {
            InitializeComponent();
            _app = CreateApp();
        }

        protected override void OnStart(string[] args) {
            _log.Info("Starting service");
            _log.Info("Starting timer");
            _loginTimer = new Timer(CheckLoginState, args, 1000, 10000);
        }

        void CheckLoginState(object state) {
            lock (_lockObject) {
                try {
                    if (_app.LoggedIn || _app.LoginStarted) return;

                    if (_app.ConnectionDropped) {
                        Emailer.Send("TradingScreen Connection Dropped", "API service is disconnected, trying to connect");
                    }
                    _log.Info("Starting login process");

                    var args = state as string[];

                    if (!OurStart(args)) _log.Error("Unable to start process");
                } catch (Exception e) {
                    _log.Error("Failed to start", e);
                }
            }
        }

        internal bool OurStart(string[] args) {
            _log.Info("Starting Publisher");
            _app.StartPublisher();
            _log.Info("Loading Mapping data");
            _app.LoadMapper();
            _log.Info("Schdeduling Reload");
            _app.ScheduleMapperReload();
            _log.Info("Scheduling Position Purge");
            _app.SchedulePositionPurge();
            _log.Info("Logging into TradingScreen");

            if (!_app.LoginToTradingScreen()) {
                return false;
            }
            _log.Info("Processing Orders");
            _app.StartProcessingOrders();

            return true;
        }

        static TradingScreenPositionManager CreateApp() {
            var config = AppConfiguration.Load();

            Emailer.FromAddress = config.MailFromAddress;
            Emailer.ToAddress = config.MailToAddress;

            return new TradingScreenPositionManager {
                ApiUserId = config.ApiUserId,
                ApiPassword = config.ApiPassword,
                ApiSite = config.ApiSite,
                ApiPricingServer = config.ApiPricingServer,
                BrokerUrl = config.BrokerUrl,
                DatabaseConnectionString = config.ConnectionString
            };
        }

        protected override void OnStop() {
            _log.Info("Stopping service");
            _loginTimer.Dispose();
            _loginTimer = null;

            if (_app.LoginStarted) {
                WaitForLoginToStop();
            }

            OurStop();
        }

        void WaitForLoginToStop() {
            while (_app.LoginStarted) {
                Thread.Sleep(1000);
            }
        }

        internal void OurStop() {
            _app.LogoffTradingScreen();
        }
    }
}