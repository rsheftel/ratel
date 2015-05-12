using System.IO;
using System.Windows.Media.Imaging;
using Gui.Util;
using Q.Util;
using util;
using o=Q.Util.Objects;

namespace Gui.Norad {
    public class MainWindow : DockingWindow {
        public MainWindow(string[] unused) : base("NORAD") {
            var arguments = Arguments.arguments(unused, o.jStrings("live", "settings"));
            System.Environment.SetEnvironmentVariable("RE_TEST_MODE", "TRUE");
            LogC.useJavaLog = true;
            LogC.info("starting MainWindow.");
            var workbench = new WorkbenchPanel();
            dockManager.Content = workbench;
            var path = Systematic.mainDir().file("dotNET/Gui/Resources/target.ico").path();
            Icon = BitmapFrame.Create(new FileStream(path, FileMode.Open, FileAccess.Read));
            if (arguments.containsKey("settings")) 
                if (arguments.get("live", false)) workbench.liveButton.doClick();
                else workbench.runButton.doClick();
        }
    }
}