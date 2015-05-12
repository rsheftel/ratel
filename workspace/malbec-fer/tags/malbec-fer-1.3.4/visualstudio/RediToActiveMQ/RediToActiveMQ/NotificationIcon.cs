using System;
using System.Drawing;
using System.Security.Principal;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using System.ComponentModel;
using Timer=System.Threading.Timer;

[assembly : log4net.Config.XmlConfigurator(Watch = true)]

namespace RediToActiveMQ {
    public sealed class NotificationIcon : IDisposable {
        static readonly log4net.ILog log =
            log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        readonly NotifyIcon notifyIcon;
        readonly ContextMenu notificationMenu;
        readonly AppConfiguration appConfig;

        /// <summary>
        /// This is the only menu that we enable/disable
        /// </summary>
        MenuItem launchRedi;
        MenuItem connectToRedi;
        MenuItem exit;

        RediScraper rediScraper;
        ActiveMQPublisher amqPublisher;
        Timer pingTimer;
        Timer shutdownTimer;

        readonly string userId = "Uknown";
        #region Properties
        bool SendingToActiveMQ { get; set; }
        bool ShuttingDown { get; set; }

        internal bool IsRediRunning {
            get { return rediScraper.IsRediRunning(); }
        }

        internal bool IsMessageListenerConnected {
            get { return rediScraper.IsMessageListenerStarted && rediScraper.PingMessageTable(); }
        }

        internal bool IsPositionListenerConnected {
            get { return rediScraper.IsPositionListenerStarted && rediScraper.PingPositionTable(); }
        }

        internal bool IsActiveMQConnected {
            get { return amqPublisher.IsConnected; }
        }

        internal bool CanConnect {
            get { return rediScraper.CanConnectViaCom(); }
        }
        #endregion
        #region Initialize icon and menu
        public NotificationIcon() {
            notifyIcon = new NotifyIcon();
            notificationMenu = new ContextMenu(InitializeMenu());

            notifyIcon.DoubleClick += IconDoubleClick;

            var resources = new ComponentResourceManager(typeof (NotificationIcon));
            notifyIcon.Icon = (Icon) resources.GetObject("$this.Icon");
            notifyIcon.ContextMenu = notificationMenu;

            // load our configuration information
            appConfig = AppConfiguration.Load();

            userId = GetCurrentUserId(userId);
        }

        public static string GetCurrentUserId(string defaultId) {
            var currentUser = WindowsIdentity.GetCurrent();
            if (currentUser == null) return defaultId;

            var name = currentUser.Name;
            var slashPos = name.IndexOf(@"\");
            return slashPos > -1 ? name.Substring(slashPos + 1) : name;
        }

        MenuItem[] InitializeMenu() {
            launchRedi = new MenuItem("Launch Redi", MenuLaunchRediClick);
            connectToRedi = new MenuItem("Connect To Redi", MenuConnectToRediClick);
            exit = new MenuItem("Exit", MenuExitClick);

            var menu = new[] {
                new MenuItem("RediToActiveMQ", MenuDefaultClick),
                new MenuItem("About", MenuAboutClick),
                launchRedi,
                connectToRedi,
                exit
            };
            return menu;
        }
        #endregion
        #region Main - Program entry point
        /// <summary>Program entry point.</summary>
        /// <param name="args">Command Line Arguments</param>
        [STAThread] public static void Main(string[] args) {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            bool isFirstInstance;
            // Please use a unique name for the mutex to prevent conflicts with other programs
            using (var mtx = new Mutex(true, "RediToActiveMQ", out isFirstInstance))
                if (isFirstInstance) {
                    Thread.CurrentThread.Name = "Main";
                    // we do not want to be running between 10:00 PM and 3:00 AM.  If we are running, shutdown at 
                    // at 10:00 AM
                    log.Info("Starting first instance");

                    var notificationIcon = new NotificationIcon();

                    log.Info("Checking runtime limits");
                    if (!notificationIcon.WithinRuntime(DateTime.Now)) log.Info("Not starting, not within runtime window");

                    notificationIcon.notifyIcon.Visible = true;
                    log.Info("ActiveMQ Broker: " + notificationIcon.appConfig.BrokerUrl);
                    log.Info("Redi UserId: " + notificationIcon.appConfig.UserId);
                    // start a thread to do the work, and then let the UI have this thread
                    var t = new Thread(notificationIcon.ApplicationWork) {Name = "Scraper", IsBackground = true};
                    t.Start();

                    Application.Run();
                    notificationIcon.notifyIcon.Dispose();
                    log.Info("Stopping instance");
                } else // The application is already running
                    // TODO: Display message box or change focus to existing application instance
                    log.Info("Non-first instance started.  Shutting down");
        }

        public bool WithinRuntime(DateTime timeToCompare) {
            var now = DateTime.Now;
            var runtimeWindowStart = new DateTime(now.Year, now.Month, now.Day, 3, 0, 0); // 3:00 AM
            var runtimeWindowStop = CreateDate(now, appConfig.ShutdownTime); // 10 PM

            return (timeToCompare <= runtimeWindowStop && timeToCompare >= runtimeWindowStart);
        }

        public Timer CreateShutdownTime(DateTime timeToCheck, TimerCallback callback, out long millisToWait) {
            var now = DateTime.Now;
            var runtimeWindowStop = CreateDate(now, appConfig.ShutdownTime); // 10 PM

            var delay = runtimeWindowStop - timeToCheck;
            millisToWait = (long) delay.TotalMilliseconds;
            var tmpTimer = new Timer(callback, this, millisToWait, 0);

            return tmpTimer;
        }

        public static DateTime CreateDate(DateTime date, string timeStr) {
            var shutdownTime = Convert.ToDateTime(timeStr);

            return new DateTime(
                date.Year, date.Month, date.Day, shutdownTime.Hour, shutdownTime.Minute, shutdownTime.Second);
        }
        #endregion
        #region WorkerThread
        void ApplicationWork() {
            log.Info("Worker thread started");
            rediScraper = new RediScraper(appConfig.UserId, appConfig.Password);
            rediScraper.StatusChanged += RediStatusChanged;
            rediScraper.ShouldProcessEvents = true;

            amqPublisher = new ActiveMQPublisher(appConfig.BrokerUrl);
            amqPublisher.Connect();

            pingTimer = new Timer(RediPingHandler, rediScraper, 500, 10 * 1000);
            long millisToWait;
            shutdownTimer = CreateShutdownTime(DateTime.Now, ShutdownTimerHandler, out millisToWait);

            SendingToActiveMQ = true;

            while (SendingToActiveMQ) {
                while (rediScraper.PositionCount > 0 && amqPublisher.IsConnected && SendingToActiveMQ) {
                    var positionMessage = rediScraper.GetNextPosition();
                    if (positionMessage == null) continue;

                    // remove the yellow key/product code
                    var bid = positionMessage["BID"].Split(' ');
                    positionMessage["BID"] = bid[0];

                    if (bid.Length > 1) positionMessage.Add("YELLOWKEY", bid[1]);

                    var topicName = positionMessage["ACCOUNT"] + "." + positionMessage["BID"];
                    positionMessage.Add("SOURCE", "REDI");

                    amqPublisher.Publish("Redi.Positions." + topicName, positionMessage);
                }

                while (rediScraper.ExecutionCount > 0 && amqPublisher.IsConnected && SendingToActiveMQ) {
                    var executionMessage = rediScraper.GetNextExecution();
                    if (executionMessage == null) continue;

                    var topicName = executionMessage["ACCOUNT"] + "." + executionMessage["BID"];
                    amqPublisher.Publish("Redi.Executions." + topicName, executionMessage);
                }

                Thread.Sleep(10);
            }
            log.Info("Worker thread stopped");
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="state">Expecting a RediScraper</param>
        void RediPingHandler(object state) {
            try {
                // We cannot have muliple threads in here. 
                lock (pingTimer) {
                    //log.Debug("Ping Timer Called");
                    var rs = state as RediScraper;

                    if (rs == null) {
                        log.Error("Received wrong object in PingHandler. " + state.GetType());
                        return;
                    }

                    var positionListenerConnected = IsPositionListenerConnected;
                    var messageListenerConnected = IsMessageListenerConnected;

                    if (rediScraper.CanConnectViaCom() && !rediScraper.IsConnected() && !positionListenerConnected &&
                        !messageListenerConnected) {
                        log.Error("We should be connected, but not.");
                        DisconnectFromRedi();
                    }
                    UpdateToolTip(positionListenerConnected, messageListenerConnected);
                    // Stop connecting if we are requested to shutdown
                    if (ShuttingDown) return;
                    ConnectoToMessageListener(rs, CanConnect);
                    if (ShuttingDown) return;
                    ConnectToPositionListener(rs, CanConnect);
                    if (ShuttingDown) return;
                    if (!amqPublisher.IsConnected) amqPublisher.Connect();
                    else
                        amqPublisher.SendHeartbeat(
                            userId,
                            Application.ProductVersion,
                            IsRediRunning,
                            positionListenerConnected,
                            rediScraper.LastRediPositionEvent);
                }
            } catch (Exception e) {
                log.Error("Ping failed.", e);
            }
        }

        void DisconnectFromRedi() {
            rediScraper.DisconnectPositionListener();
            rediScraper.DisconnectMessageListener();
        }

        void ShutdownTimerHandler(object state) {
            lock (shutdownTimer) {
                log.Info("Shutdown Timer Fired.  Application will exit.");
                MenuExitClick(state, EventArgs.Empty);
            }
        }

        void ConnectToPositionListener(RediScraper rs, bool canConnect) {
            if (!rs.IsPositionListenerStarted && IsRediRunning && canConnect) rs.ConnectPositionListener();
        }

        void ConnectoToMessageListener(RediScraper rs, bool canConnect) {
            if (!rs.IsMessageListenerStarted && IsRediRunning && canConnect) rs.ConnectMessageListener();
        }

        void UpdateToolTip() {
            UpdateToolTip(IsPositionListenerConnected, IsMessageListenerConnected);
        }

        void UpdateToolTip(bool positionListenerConnected, bool messageListenerConnected) {
            // Determine the status of everything

            if (ShuttingDown) {
                notifyIcon.Text = "Shutting down - please wait...";
                connectToRedi.Enabled = false;
            } else {
                var toolTip = new StringBuilder(128);
                toolTip.Append("Redi Running: ").Append(IsRediRunning).Append("\n");
                toolTip.Append("Connected: ").Append(messageListenerConnected).Append("/");
                toolTip.Append(positionListenerConnected).Append("\n");
                toolTip.Append("AMQ: ").Append(IsActiveMQConnected);

                notifyIcon.Text = toolTip.ToString();
                connectToRedi.Enabled = IsRediRunning && !messageListenerConnected && !positionListenerConnected;
            }

            notifyIcon.Visible = true;

            launchRedi.Enabled = !IsRediRunning && !string.IsNullOrEmpty(appConfig.UserId) &&
                !string.IsNullOrEmpty(appConfig.Password);
        }
        #endregion
        #region Event Handlers
        static void MenuAboutClick(object sender, EventArgs e) {
            MessageBox.Show(
                Application.CompanyName + "\n" + Application.ProductName + "\n" + Application.ProductVersion);
        }

        void MenuExitClick(object sender, EventArgs ea) {
            try {
                // Stop the timer, stop event processing, disconnect, shutdown
                ShuttingDown = true;
                rediScraper.ShouldProcessEvents = false;

                pingTimer.Dispose(); // stop firing events

                exit.Enabled = false;

                notifyIcon.Text = "Shutting down - please wait...";

                DisconnectFromRedi();
                SendingToActiveMQ = false;

                amqPublisher.SendHeartbeat(
                    userId,
                    Application.ProductVersion,
                    IsRediRunning,
                    IsPositionListenerConnected,
                    rediScraper.LastRediPositionEvent);

                amqPublisher.Disconnect();

                Application.Exit();
            } catch (Exception e) {
                log.Error(e);
            }
        }

        static void MenuDefaultClick(object sender, EventArgs e) {
            IconDoubleClick(sender, e);
        }

        static void IconDoubleClick(object sender, EventArgs e) {
            MessageBox.Show("RediToActiveMQ - Configuration and Status Dialog");
        }

        void MenuLaunchRediClick(object sender, EventArgs e) {
            rediScraper.StartRedi();
            launchRedi.Enabled = false;
        }

        /// <summary>
        /// We are trusting the user here to know that they can connect
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="ea"></param>
        void MenuConnectToRediClick(object sender, EventArgs ea) {
            connectToRedi.Enabled = false;
            ConnectoToMessageListener(rediScraper, true);
            ConnectToPositionListener(rediScraper, true);
        }

        void RediStatusChanged(object sender, EventArgs e) {
            UpdateToolTip();
        }
        #endregion
        #region IDisposable
        public void Dispose() {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        /// <summary>
        /// We are either called from Dispose or Finalize
        /// </summary>
        /// <param name="calledFromDispose"></param>
        void Dispose(bool calledFromDispose) {
            if (!calledFromDispose) return;

            notifyIcon.Dispose();
            notificationMenu.Dispose();
        }
        #endregion
    }
}