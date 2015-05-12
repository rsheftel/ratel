namespace Q.Util {
    public class Env {
        public static string env(string name) {
            return System.Environment.GetEnvironmentVariable(name);
        }

        public static string svn(string partial) {
            return env("MAIN") + "\\" + partial;
        }

        public static string javaHome(string partial) {
            return env("JAVA_HOME") + "\\" + partial;
        }
    }
}