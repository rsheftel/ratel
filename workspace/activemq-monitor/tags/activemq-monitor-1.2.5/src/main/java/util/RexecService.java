package util;

import java.io.IOException;

/**
 * Builds on the <code>RemoteClient</code> to start/stop/restart remote services.
 * 
 * <b>The current <tt>rexecd</tt> that we have installed crashes and is thus useless.</b>
 */
public class RexecService {

    private static final String NET_STOP = "net stop";
    private static final String NET_START = "net start";

    private String serviceName;

    private RemoteCommand remoteCommand;

    public RexecService(String server, String userid, String password, String serviceName) {
        this.serviceName = serviceName;
        remoteCommand = new RemoteCommand(server, userid, password);
    }

    public boolean stop() throws IOException {
        StringBuilder stopCommand = new StringBuilder(50);
        stopCommand.append(NET_STOP).append(" \"").append(serviceName).append("\"");

        String commandOutput = remoteCommand.execute(stopCommand.toString());

        // It was either stopped or not running, either way, it was a success
        boolean success = commandOutput.contains("service was stopped successfully")
                || commandOutput.contains("service is not started");
        
        return success;
    }
    
    public boolean start() throws IOException {
        StringBuilder stopCommand = new StringBuilder(50);
        stopCommand.append(NET_START).append(" \"").append(serviceName).append("\"");

        String commandOutput = remoteCommand.execute(stopCommand.toString());

        // It was either stopped or not running, either way, it was a success
        boolean success = commandOutput.contains("service was started successfully")
                || commandOutput.contains("service has already been started");
        
        return success;
    }

}
