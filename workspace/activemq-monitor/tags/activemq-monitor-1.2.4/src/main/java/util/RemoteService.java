package util;

import java.io.IOException;

public class RemoteService {

    private static final String WINDOWS_OK = "WIN32_EXIT_CODE    : 0\t(0x0)";
    private static final String SERVICE_OK = "SERVICE_EXIT_CODE  : 0\t(0x0)";

    private String remoteHost;
    private String serviceName;

    private String lastCommandOuput;

    public RemoteService(String server, String serviceName) {
        this.remoteHost = server;
        this.serviceName = serviceName;
    }

    public String getLastCommandOuput() {
        return lastCommandOuput;
    }

    public boolean stop() throws IOException {

        String[] command = new String[4];
        command[0] = "C:\\WINDOWS\\system32\\sc.exe";
        command[1] = "\\\\" + remoteHost;
        command[2] = "stop";
        command[3] = "\"" + serviceName + "\"";

        LocalProcess localProcess = new LocalProcess(command);
        String commandOutput = localProcess.execute();

        boolean success = commandOutput.contains("The service has not been started")
                || (commandOutput.contains(WINDOWS_OK) && commandOutput.contains(SERVICE_OK));

        lastCommandOuput = commandOutput;
        return success;
    }

    public boolean start() throws IOException {
        String[] command = new String[4];
        command[0] = "C:\\WINDOWS\\system32\\sc.exe";
        command[1] = "\\\\" + remoteHost;
        command[2] = "start";
        command[3] = "\"" + serviceName + "\"";

        LocalProcess localProcess = new LocalProcess(command);
        String commandOutput = localProcess.execute();

        boolean success = commandOutput.contains("service is already running")
                || (commandOutput.contains(WINDOWS_OK) && commandOutput.contains(SERVICE_OK));

        lastCommandOuput = commandOutput;

        return success;
    }

    public boolean restart() throws IOException {

        if (stop()) {
            // Give the box some time to recover
            Monitoring.sleep(10000);
            return start();
        } else {
            return false;
        }
    }

}
