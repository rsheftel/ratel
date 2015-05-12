using System;
using System.Configuration;
using System.Reflection;

namespace FixExecutionAddin {
    /// <summary>
    /// Description of Configuration.
    /// </summary>
    public class AppConfiguration
    {
        readonly static log4net.ILog _log = log4net.LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        #region Properties
        public string BrokerUrl { get; set; }

        public static string AssemblyPath
        {
            get
            {
                var codebase = Assembly.GetExecutingAssembly().CodeBase;
                return codebase.Substring(8) + ".config";
            }
        }
        #endregion
        public static AppConfiguration Load()
        {
            try {
                // When running as a DLL from Excel the default ConfigurationManager does not find the
                // correct DLL, it looks for EXCEL.exe.config
                var configFileMap = new ExeConfigurationFileMap { ExeConfigFilename = AssemblyPath };
                var ourConfig = ConfigurationManager.OpenMappedExeConfiguration(configFileMap, ConfigurationUserLevel.None);

                var appConfig = new AppConfiguration
                                {
                                    BrokerUrl = ourConfig.AppSettings.Settings["brokerUrl"].Value,
                                    //BrokerUrl = ConfigurationManager.AppSettings["brokerUrl"],
                                };

                if (appConfig.BrokerUrl == null) {
                    appConfig.BrokerUrl = "failover:tcp://nyws802:60606";
                    _log.Warn("Failed to load configuration file, using default broker URL: " + appConfig.BrokerUrl);
                }
                return appConfig;
            } catch (Exception e) {
                _log.Error("Unable to load configuation file: " + AssemblyPath, e);
            }
            return null;
        }


    }
}