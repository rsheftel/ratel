using System.ComponentModel;
using System.Configuration.Install;

namespace TradingScreenApiService
{
    [RunInstaller(true)]
    public partial class ProjectInstaller : Installer
    {
        public ProjectInstaller()
        {
            InitializeComponent();
        }
    }
}
