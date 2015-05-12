using System;
using System.Reflection;
using System.ServiceProcess;
using System.Text;
using Q.Util;

namespace QRun {
    class Program : Objects {
        public static int Main(string[] args) {
            var q = qAssembly();
            var className = args[0];
            var type = q.GetType(className, true, false);
            if(type.IsSubclassOf(typeof(ServiceBase))) {
                try {
                    var variables = Environment.GetEnvironmentVariables();
                    var buf = new StringBuilder();
                    foreach (string name in variables.Keys) 
                        buf.AppendLine("ENV: " + name + "=" + variables[name]);
                    LogC.eventInfo(buf.ToString(), "QRun");
                    ServiceBase.Run((ServiceBase) type.GetConstructor(Type.EmptyTypes).Invoke(new object[0]));
                } catch (Exception e) {
                    LogC.eventError("exception thrown from QRun Service: " + className + "\n", e, "QRun");
                    return -1;
                }
            } else {
                try {
                    var main = type.GetMethod("Main", new[] {typeof (string[])});
                    var newArgs = new string[args.Length - 1];
                    zeroTo(args.Length - 1, i => newArgs[i] = args[i + 1]);
                    Bomb.ifNull(main, () => "Could not find Main method in " + className);
                    main.Invoke(null, new[] {newArgs});
                } catch (Exception e) {
                    LogC.eventError("exception invoking " + className + ".Main\n", e, "QRun");
                    LogC.err("exception invoking " + className + ".Main\n", e);
                    return -1;
                }
            }
            return 0;
        }


        internal static Assembly qAssembly() {
            Assembly q = null; 
            foreach(var assembly in AppDomain.CurrentDomain.GetAssemblies()) {
                if(assembly.GetName().Name.Equals("Q")) q = assembly;
            }
            if(q == null)
                throw Bomb.toss(
                    "Cannot find Q.dll in assemblies:\n" + toShortString(convert(AppDomain.CurrentDomain.GetAssemblies(),a => a.FullName))
                    );
            return q;
        }
    }
}
