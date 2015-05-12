using NHibernate;
using NHibernate.Cfg;
using NUnit.Framework;
using TradingScreenApiService.Database.Mappings;

namespace TradingScreenApiServiceTests.Database.Mappings {

    [TestFixture] 
    public class FuturesSymbolMappingTest {

        private ISessionFactory _sessionFactory;
        private Configuration _configuration;

        [Test] 
        public void TestLoadFuturesMapping() {
            var sessionFactory = BuildSessionFactory();
            var fsm = new FuturesSymbolMapper();
            var recordsLoaded = fsm.Initialize(sessionFactory.OpenSession());
            Assert.IsTrue(recordsLoaded > 0);
          
            var fsmC = new FuturesSymbolMapper();
            var recordsLoadedC = fsmC.Initialize(sessionFactory.OpenSession());

            Assert.IsTrue(recordsLoaded > 0);
            Assert.AreEqual(recordsLoaded, recordsLoadedC);

            var reloadCount = fsm.Reload(sessionFactory.OpenSession());
            Assert.AreEqual(reloadCount, recordsLoaded);
        }

        static ISessionFactory BuildSessionFactory() {
            var config = new Configuration();
            config.SetProperty("connection.provider", "NHibernate.Connection.DriverConnectionProvider");
            config.SetProperty("dialect", "NHibernate.Dialect.MsSql2005Dialect");
            config.SetProperty("connection.driver_class", "NHibernate.Driver.SqlClientDriver");
            config.SetProperty(
                "connection.connection_string", "Server=NYSRV28,2433;Initial Catalog=BADB;User Id=sim;Password=Sim5878");
            config.SetProperty("proxyfactory.factory_class", "NHibernate.ByteCode.LinFu.ProxyFactoryFactory, NHibernate.ByteCode.LinFu");
            config.AddAssembly(typeof (FuturesSymbolMapping).Assembly);

            //            config.Configure(); // load from configuration file

            return config.BuildSessionFactory();
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
    }
}