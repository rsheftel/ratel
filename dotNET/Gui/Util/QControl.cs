using System;
using System.Data;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Threading;
using AvalonDock;
using Microsoft.Windows.Controls;
using Q.Recon;
using Q.Util;

namespace Gui.Util {
    public class QControl : UserControl, QGUI {
        public static readonly SolidColorBrush WINDOWS_GRAY = new SolidColorBrush(Color.FromRgb(212, 208, 200));

        public void runOnGuiThread(Action action) {
            runOnGuiThread(this, action);
        }

        public void logAndAlert(string s, Exception e) {
            var message = LogC.errMessage(s, e);
            LogC.err(message);
            alertUser(s + ": examine " + LogC.errFile() + "\n" + message);
        }

        public static void runOnGuiThread(DispatcherObject dispatcher, Action action) {
            dispatcher.Dispatcher.BeginInvoke(DispatcherPriority.Background, action);
        }

        public static ResizingPanel downPanel() {
            return new ResizingPanel {Orientation = Orientation.Vertical};
        }

        public static ResizingPanel rightPanel() {
            return new ResizingPanel {Orientation = Orientation.Horizontal};
        }

        public DependencyObject parent() {
            return LogicalTreeHelper.GetParent(this);
        }

        public static DockablePane dockableChild(Panel outer) {
            var pane = new DockablePane();
            outer.Children.Add(pane);
            return pane;
        }

        public static void makeColor(DispatcherObject dispatcher, Control row, Brush color) {
            runOnGuiThread(dispatcher, () => {
                if(row.Background != color) row.Background = color;
            });
        }
        
        public void makeColor(Control row, Brush color) {
            makeColor(this, row, color);
        }

        public void makeWhite(Control row) {
            makeColor(row, Brushes.White);
        }
        
        public static DataRow dataRow(DataGridRow row) {
            var view = (DataRowView) row.Item;
            return view.Row;
        }
        
        public void showMessageBox(string message) {
            MessageBox.Show(message, "Notification", MessageBoxButton.OK, MessageBoxImage.Information);
        }

        public static void forceLoad() {}

        protected static DockPanel withLabel(string labelText, UIElement needsLabel) {
            var systemBoxPanel = new DockPanel();
            DockPanel.SetDock(needsLabel, Dock.Right);
            systemBoxPanel.Children.Add(label(labelText, needsLabel));
            systemBoxPanel.Children.Add(needsLabel);
            return systemBoxPanel;
        }

        public static Label label(string label, UIElement needsLabel) {
            var result = QControl.label(label);
            result.Target = needsLabel;
            return result;
        }

        public static Label label(string label) {
            return new Label {
                VerticalAlignment = VerticalAlignment.Top, 
                Content = label 
            };
        }

        protected static DatePicker datePicker() {
            var picker = new DatePicker { SelectedDateFormat = DatePickerFormat.Short };
            return picker;
        }

        public void alertUser(string message) { 
            MessageBox.Show(message, "Notification", MessageBoxButton.OK, MessageBoxImage.Information);
        }

        public YesNoCancel askUser(string message) {
            var result = MessageBox.Show(message, "Yes/No/Cancel?", MessageBoxButton.YesNoCancel, MessageBoxImage.Information, MessageBoxResult.Cancel);
            switch (result) {
                case MessageBoxResult.Yes: return YesNoCancel.YES;
                case MessageBoxResult.No: return YesNoCancel.NO;
                case MessageBoxResult.Cancel: return YesNoCancel.CANCEL;
            }
            throw Bomb.toss("impossible.");
        }

    }
}