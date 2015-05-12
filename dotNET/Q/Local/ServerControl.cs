using util;

namespace Q.Local {
    public class ServerControl : Util.Objects {
        public static void Main(string[] args) {
            var arguments = Arguments.arguments(args, jStrings("id", "command"));
            var id = arguments.@string("id");
            var command = arguments.@string("command");
            var queueName = "LocalSTO.Requests." + id;
            var theCommand = typeof(STOServer).FullName + " -systemId " + first(id.Split('-')) + " -queue " + queueName;
            var mainDir = STOClient.DLL_CACHE.directory(new[] {id}).path();
            if(command.Equals("kill") || command.Equals("restart"))
                Bootstrap.sendStopCommand(mainDir, theCommand);
            if(command.Equals("restart"))
                sleep(2000);
            if(command.Equals("start") || command.Equals("restart"))
                Bootstrap.sendStartCommand(3, mainDir, theCommand);
        }
    }
}
