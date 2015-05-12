using System;
using System.Configuration;
using System.Reflection;

namespace ActiveMQExcel {
    /// <summary>
    /// Description of Configuration.
    /// </summary>
    public class AppConfiguration
    {
        readonly static log4net.ILog _log = log4net.LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        #region Properties
        public string MarketDataBrokerUrl { get; set; }
        public string PositionsBrokerUrl { get; set; }

        public static string AssemblyPath
        {
            get {
//                var codebase = Assembly.GetExecutingAssembly().CodeBase;
//                return codebase.Substring(8) +".config";

                var location = Assembly.GetCallingAssembly().Location;
                return location + ".config";
            }
        }
        #endregion
        public static AppConfiguration Load()
        {
            TryToLoadLog4Net();
            try {
                // When running as a DLL from Excel the default ConfigurationManager does not find the
                // correct DLL, it looks for EXCEL.exe.config
                var configFileMap = new ExeConfigurationFileMap { ExeConfigFilename = AssemblyPath };
                var ourConfig = ConfigurationManager.OpenMappedExeConfiguration(configFileMap, ConfigurationUserLevel.None);

                var appConfig = new AppConfiguration {
                    MarketDataBrokerUrl = ourConfig.AppSettings.Settings["marketDataBrokerUrl"].Value,
                    PositionsBrokerUrl = ourConfig.AppSettings.Settings["positionsBrokerUrl"].Value,
                    //BrokerUrl = ConfigurationManager.AppSettings["brokerUrl"],
                };

                if (appConfig.MarketDataBrokerUrl == null) {
                    appConfig.MarketDataBrokerUrl = "failover:tcp://nyws802:60606";
                    _log.Warn("Failed to load configuration file, using default market data broker URL: " + appConfig.MarketDataBrokerUrl);
                }
                if (appConfig.PositionsBrokerUrl == null) {
                    appConfig.PositionsBrokerUrl = "failover:tcp://nyws802:60606";
                    _log.Warn("Failed to load configuration file, using default positions broker URL: " + appConfig.PositionsBrokerUrl);
                }

                return appConfig;
            } catch (Exception e) {
                _log.Error("Unable to load configuation file: " + AssemblyPath, e);
            }
            return null;
        }

        public static void TryToLoadLog4Net() {
            try {
                log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(AssemblyPath));
            } catch (Exception exception) {
                _log.Error("Unable to load our configuration from: " + AssemblyPath, exception);
            }
        }
    }
}