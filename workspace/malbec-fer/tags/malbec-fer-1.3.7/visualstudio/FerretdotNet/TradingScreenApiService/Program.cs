using System;
using System.ServiceProcess;
using System.Threading;

// Configure log4net using the .config file
[assembly: log4net.Config.XmlConfigurator(Watch = true)]

namespace TradingScreenApiService
{
    static class Program
    {
        static readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main(string[] args) {
            if (args != null && args.Length > 0) {
                _log.Info("Running as Console Application");
                if (Thread.CurrentThread.Name == null) {
                    Thread.CurrentThread.Name = "Main";
                }
                var service = new TradingScreenApiService();
                service.OurStart(Environment.GetCommandLineArgs());
                
                Console.WriteLine("Press any key to exit...");
                Console.ReadKey();
                _log.Info("Disconnecting from TradingScreen");
                service.OurStop();
            } else {

                var servicesToRun = new ServiceBase[] {
                    new TradingScreenApiService()
                };
                ServiceBase.Run(servicesToRun);
            }
        }
    }
    
}
