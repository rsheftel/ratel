using NHibernate;
using NHibernate.Cfg;
using TradingScreenApiService.Database.Mappings;

namespace TradingScreenApiService.Database
{
    public class NHibernateHelper
    {
        private static ISessionFactory _sessionFactory;

        private static ISessionFactory SessionFactory
        {

            get
            {
                if (_sessionFactory == null) {
                    var configuration = new Configuration();
                    configuration.Configure();
                    configuration.AddAssembly(typeof(FuturesSymbolMapping).Assembly);
                    _sessionFactory = configuration.BuildSessionFactory();
                }

                return _sessionFactory;
            }
        }


        public static ISession OpenSession()
        {
            return SessionFactory.OpenSession();
        }
    }
}
