using System;
using System.Reflection;
using System.Windows;
using Gui.Util;
using Q.Util;
using O=Q.Util.Objects;

namespace QRunGui {
    public class Program : Application {
        [STAThread]
        public static int Main(string[] args) {
            QControl.forceLoad();
            Assembly q = null; 
            foreach(var assembly in AppDomain.CurrentDomain.GetAssemblies()) {
                if(assembly.GetName().Name.Equals("Gui")) q = assembly;
            }
            if(q == null)
                throw Bomb.toss(
                    "Cannot find Gui.dll in assemblies:\n" + O.toShortString(O.convert(AppDomain.CurrentDomain.GetAssemblies(),a => a.FullName))
                );

            var appName = args[0];
            var className = "Gui." + appName + ".MainWindow";
            try {
                var type = q.GetType(className, true, false);
                var newArgs = new string[args.Length - 1];
                O.zeroTo(args.Length - 1, i => newArgs[i] = args[i + 1]);

                var application = new Application();
                System.Windows.Forms.Application.ThreadException += (s, e) => { DockingWindow.handleUncaughtException(e.Exception, appName); throw e.Exception;};
                var mainWindow = (Window) type.GetConstructor(new[] {typeof(string[])}).Invoke(new[] {newArgs});
                mainWindow.Closed += (s, e) => Environment.Exit(0);
                application.Run(mainWindow);
            } catch (Exception e) {
                DockingWindow.handleUncaughtException(e, appName);
                return -1;
            }
            return 0;
        }
    }
}