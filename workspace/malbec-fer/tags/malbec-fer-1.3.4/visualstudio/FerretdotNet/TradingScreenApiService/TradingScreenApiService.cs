using System.ServiceProcess;

namespace TradingScreenApiService
{
    public partial class TradingScreenApiService : ServiceBase
    {
        public TradingScreenApiService()
        {
            InitializeComponent();
        }

        protected override void OnStart(string[] args)
        {
        }

        protected override void OnStop()
        {
        }
    }
}
