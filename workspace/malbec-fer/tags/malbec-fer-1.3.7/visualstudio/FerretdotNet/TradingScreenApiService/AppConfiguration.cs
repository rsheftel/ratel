using System.Configuration;
using System.Data.EntityClient;
using System.Data.SqlClient;

namespace TradingScreenApiService {
    /// <summary>
    /// Description of Configuration.
    /// </summary>
    public class AppConfiguration {
        string _connectionString;
        #region Properties
        public string BrokerUrl { get; set; }

        public string ApiUserId { get; set; }

        public string ApiPassword { get; set; }

        public string ApiSite { get; set; }

        public string ApiPricingServer { get; set; }

        public string ConnectionString {
            get {
                if (string.IsNullOrEmpty(DatabaseUserId) && string.IsNullOrEmpty(DatabasePassword)) return _connectionString;

                // build the entity connection from the connection string
                // add the userid and password and return the connection string
                var ecsb = new EntityConnectionStringBuilder(_connectionString);
                var sqsb = new SqlConnectionStringBuilder(ecsb.ProviderConnectionString) {
                    UserID = DatabaseUserId,
                    Password = DatabasePassword,
                    PersistSecurityInfo = true,
                    IntegratedSecurity = false
                };

                ecsb.ProviderConnectionString = sqsb.ToString();

                return ecsb.ToString();
            }
            set { _connectionString = value; }
        }

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
                ConnectionString = ConfigurationManager.AppSettings["connectionString"],
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