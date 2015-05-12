using System;
using System.Data.EntityClient;
using System.Data.SqlClient;
using NUnit.Framework;
using TradingScreenApiService;
using TradingScreenApiService.Database.Mappings;

namespace TradingScreenApiServiceTests.Database.Mappings {

    [TestFixture] 
    public class FuturesSymbolMappingTest {

        [Test] 
        public void TestLoadFuturesMapping() {
            var connectionString = BuildConnectionString();
            var fsm = new FuturesSymbolMapper();
            var recordsLoaded = fsm.Initialize(connectionString);
            Assert.IsTrue(recordsLoaded > 0);

            var badbContext = new BADBEntities(connectionString);
            var fsmC = new FuturesSymbolMapper();
            var recordsLoadedC = fsmC.Initialize(badbContext);

            Assert.IsTrue(recordsLoaded > 0);
            Assert.AreEqual(recordsLoaded, recordsLoadedC);

            var reloadCount = fsm.Reload(badbContext);
            Assert.AreEqual(reloadCount, recordsLoaded);
        }

        [Test] 
        public void TestMapping() {
            var fsm = new FuturesSymbolMapper();
            const string platform = "TEST";
            const string bloombergRoot = "SF";
            const string platformReceiveRoot = "6S";
            const string platformSendRoot = "SFSS";

            // Setup our test data
            fsm.AddBloombergMapping(platform, bloombergRoot, platformReceiveRoot, platformSendRoot, 10);

            var mappedBloomberg = fsm.ConvertPlatformReceiving(platform, platformReceiveRoot, false);
            Assert.AreEqual(mappedBloomberg, bloombergRoot);

            var ricRoot = fsm.LookupPlatformSendingRoot(platform, bloombergRoot, false);
            Assert.AreEqual(ricRoot, platformSendRoot);

            var mappedPlatform = fsm.LookupPlatformRoot(platform, bloombergRoot, false);
            Assert.AreEqual(mappedPlatform, platformReceiveRoot);

            var toBloomberg = fsm.LookupToBloombergPriceMultiplier(platform, bloombergRoot);
            Assert.AreEqual(toBloomberg, 10);

            var toPlatform = fsm.LookupToPlatformPriceMultiplier(platform, bloombergRoot);
            Assert.AreEqual(toPlatform, decimal.One / 10);

            // Test logic for converting exchange to bloomberg pricing and vice versa
            // multiply by 10
            var bbPrice = (decimal) 0.7268 * toBloomberg;
            Assert.AreEqual(bbPrice, 7.268);

            // multiply by 1/10
            var platformPrice = (decimal) 72.68 * toPlatform;
            Assert.AreEqual(platformPrice, 7.268);

            var bbSymbol = fsm.MapPlatformRootToBloombergSymbol(platform, platformReceiveRoot, "200906");
            Assert.AreEqual(bbSymbol, "SFM9");

            // Do the RIC to Bloomberg mapping
            // We are using TRADS for the platform to cheat, ensure we have it
            fsm.AddBloombergMapping("TRADS", bloombergRoot, platformReceiveRoot, platformSendRoot, 10);

            var sfRoot = fsm.ConvertRicRoot("SFSS");
            Assert.AreEqual("SF", sfRoot);

            var fredRoot = fsm.ConvertRicRoot("1FRED");
            Assert.IsNull(fredRoot);

        }

        internal static string BuildConnectionString() {
            // Specify the provider name, server and database.
            const string providerName = "System.Data.SqlClient";
            const string serverName = "NYSRV28,2433";
            const string databaseName = "BADB";

            // Initialize the connection string builder for the
            // underlying provider.
            var sqlBuilder = new SqlConnectionStringBuilder {
                DataSource = serverName,
                InitialCatalog = databaseName,
                IntegratedSecurity = true,
            };

            // Build the SqlConnection connection string.
            var providerString = sqlBuilder.ToString();

            // Initialize the EntityConnectionStringBuilder.
            var entityBuilder =
                new EntityConnectionStringBuilder {
                    Provider = providerName,
                    ProviderConnectionString = providerString,
                    Metadata =
                        @"res://*/FuturesMapping.csdl|res://*/FuturesMapping.ssdl|res://*/FuturesMapping.msl"
                };

            Console.WriteLine(entityBuilder.ToString());
            var config = new AppConfiguration {
                ConnectionString = entityBuilder.ToString(),
                DatabaseUserId = "sim",
                DatabasePassword = "Sim5878"
            };

            Console.WriteLine(config.ConnectionString);

            return entityBuilder.ToString();
        }
    }
}