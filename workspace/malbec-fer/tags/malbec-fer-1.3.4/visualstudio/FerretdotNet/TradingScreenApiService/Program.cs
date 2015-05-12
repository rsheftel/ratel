using System;
using System.ServiceProcess;

namespace TradingScreenApiService
{
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        static void Main() {

            TestLoadFuturesMapping();
            var servicesToRun = new ServiceBase[] 
                                          { 
                                              new TradingScreenApiService() 
                                          };
            ServiceBase.Run(servicesToRun);
        }


     public static void TestLoadFuturesMapping() {
         BADBEntities badbContext = new BADBEntities();

         var mapping = badbContext.FuturesSymbolMapping;

         Console.WriteLine(mapping);
        }

    }
    
}
