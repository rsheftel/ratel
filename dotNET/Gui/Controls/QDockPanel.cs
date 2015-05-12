using System.Windows;
using System.Windows.Controls;
using Gui.Util;

namespace Gui.Controls {
    public class QDockPanel : DockPanel {
        public QDockPanel() {
            LastChildFill = true;
            Background = QControl.WINDOWS_GRAY;
        }

        public void add(UIElement element, Dock position) {
            SetDock(element, position);
            add(element);
        }

        public void add(UIElement element) {
            Children.Add(element);
        }
    }
}