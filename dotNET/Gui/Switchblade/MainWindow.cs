using Gui.Util;

namespace Gui.Switchblade {
    public class MainWindow : DockingWindow {
        public MainWindow(string[] unused) : base("SwitchBlade") {
            dockManager.Content = new MainPanel();
            //var path = Systematic.mainDir().file("dotNET/Gui/Resources/target.ico").path_();
            //Icon = BitmapFrame.Create(new FileStream(path, FileMode.Open, FileAccess.Read));
        }
    }
}
