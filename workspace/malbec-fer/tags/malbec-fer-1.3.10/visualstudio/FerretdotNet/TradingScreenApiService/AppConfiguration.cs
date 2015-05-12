using System.Configuration;

namespace TradingScreenApiService {
    /// <summary>
    /// Description of Configuration.
    /// </summary>
    public class AppConfiguration {

        #region Properties
        public string BrokerUrl { get; set; }

        public string ApiUserId { get; set; }

        public string ApiPassword { get; set; }

        public string ApiSite { get; set; }

        public string ApiPricingServer { get; set; }

        public string ConnectionString_ { get; set; }

        public string DatabasePassword { get; set; }

        public string DatabaseUserId { get; set; }
        #endregion
        internal AppConfiguration() {}

        public string MailToAddress { get; set; }

        public string MailFromAddress { get; set; }

        public static AppConfiguration Load() {
            var appConfig = new AppConfiguration {
                BrokerUrl = ConfigurationManager.AppSettings["brokerUrl"],
                ApiUserId = ConfigurationManager.AppSettings["apiUserId"],
                ApiPassword = ConfigurationManager.AppSettings["apiPassword"],
                ApiSite = ConfigurationManager.AppSettings["apiSite"],
                ApiPricingServer = ConfigurationManager.AppSettings["apiPricingServer"],
                // options values
                DatabaseUserId = ConfigurationManager.AppSettings["dbUserId"],
                DatabasePassword = ConfigurationManager.AppSettings["dbPassword"],
                MailFromAddress = ConfigurationManager.AppSettings["mailFromAddress"],
                MailToAddress = ConfigurationManager.AppSettings["mailToAddress"]
            };

            return appConfig;
        }
    }
}