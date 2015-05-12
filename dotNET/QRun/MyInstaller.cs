using System;
using System.ComponentModel;
using System.Configuration.Install;
using System.ServiceProcess;
using Microsoft.Win32;
using Q.Util;
using O=Q.Util.Objects;

namespace QRun {
    [RunInstaller(true)]
    public class MyInstaller : Installer {
        public MyInstaller() {
            Installers.Add(new ServiceProcessInstaller {Account = ServiceAccount.LocalSystem});
            var q = Program.qAssembly();
            O.each(q.GetTypes(), type => {
                if(!type.IsSubclassOf(typeof(ServiceBase))) return;
                var service = (ServiceBase) type.GetConstructor(Type.EmptyTypes).Invoke(new object[0]);
                Installers.Add(new MyServiceInstaller(type) {
                    StartType = ServiceStartMode.Disabled,
                    ServiceName = service.ServiceName
                });
            });
        }
    }

    internal class MyServiceInstaller : ServiceInstaller {
        readonly Type type;

        public MyServiceInstaller(Type type) {
            this.type = type;
        }

        public override void Install(System.Collections.IDictionary stateSaver) {
            base.Install(stateSaver);
            var keyName = @"HKEY_LOCAL_MACHINE\System\CurrentControlSet\Services\" + ServiceName;
            var imagePath = Registry.GetValue(keyName, "ImagePath", null);
            Bomb.ifNull(imagePath, () => "no ImagePath value in " + keyName);
            Registry.SetValue(keyName, "ImagePath", imagePath + " " + type.FullName);
        }
    }
}
