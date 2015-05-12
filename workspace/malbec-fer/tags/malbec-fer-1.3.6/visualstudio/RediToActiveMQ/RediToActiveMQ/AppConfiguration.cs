using System;
using System.Collections.Specialized;
using System.Configuration;

namespace RediToActiveMQ {
    /// <summary>
    /// Description of Configuration.
    /// </summary>
    public class AppConfiguration {
        #region Properties
        public Uri BrokerUrl { get; set; }

        public string UserId { get; set; }

        public string Password { get; set; }

        public string RediPath { get; set; }

        public string RediArgs { get; set; }

        public int RediStartupDelay { get; set; }

        public string ShutdownTime { get; set; }
        #endregion

        AppConfiguration() {}

        public static AppConfiguration Load() {

            var appConfig = new AppConfiguration {
                BrokerUrl = new Uri(ConfigurationManager.AppSettings["brokerUrl"]),
                UserId = ConfigurationManager.AppSettings["rediUserId"],
                Password = ConfigurationManager.AppSettings["rediUserPassword"],
                // TODO handle this being missing
                RediPath = ConfigurationManager.AppSettings["rediPath"],
                RediArgs = ConfigurationManager.AppSettings["rediArgs"],
                RediStartupDelay = Convert.ToInt32(GetStringSetting(ConfigurationManager.AppSettings,"rediStartupDelay", "60")),
                ShutdownTime = ConfigurationManager.AppSettings["shutdownTime"]
            };

            if (string.IsNullOrEmpty(appConfig.ShutdownTime)) {
                appConfig.ShutdownTime = "22:00:00";
            }
            return appConfig;
        }

        static string GetStringSetting(NameValueCollection settings, string propertyName, string defaultValue) {
            var tmp = settings.Get(propertyName);

            return string.IsNullOrEmpty(tmp) ? defaultValue : tmp;
        }
    }
}