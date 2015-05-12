using System;
using System.Windows;
using AvalonDock;
using Q.Util;
using util;

namespace Gui.Util {
    public class DockingWindow : Window {
        readonly string appName;
        protected readonly DockingManager dockManager = new DockingManager();
        readonly string safeAppName;

        protected DockingWindow(string appName) {
            this.appName = appName;
            safeAppName = Strings.javaClassify(appName);
            AppDomain.CurrentDomain.UnhandledException += exceptionHandler;
            LogC.setOut(appName, @"C:\logs\" + safeAppName + ".log", true);
            LogC.useJavaLog = true;
            setWindowSettings();
        }

        void setWindowSettings() {
            Title = appName;
            Height = 800;
            Width = 1280;
            Content = dockManager;
        }

        void exceptionHandler(object sender, UnhandledExceptionEventArgs e) {
            var ex = (Exception) e.ExceptionObject;
            handleUncaughtException(ex, safeAppName);
        }

        public static void handleUncaughtException(Exception ex, string appName) {
            LogC.useJavaLog = false;
            LogC.setErr(@"C:\logs\" + appName + "Crash.log");
            LogC.err("uncaught exception: ", ex);
            MessageBox.Show(
                LogC.errMessage("exception written to C:\\logs\\" + appName + "Crash.log: ", ex), 
                "Exception - Crashing", MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }
}