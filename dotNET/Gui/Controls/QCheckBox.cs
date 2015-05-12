using System;
using System.Windows.Controls;

namespace Gui.Controls {
    public class QCheckBox : CheckBox {
        public QCheckBox(string title, bool @default, Action<bool> onNewSelected) {
            Content = title;
            IsChecked = @default;
            Checked += (unused, e) => onNewSelected(true);
            Unchecked += (unused, e) => onNewSelected(false);
        }
    }
}