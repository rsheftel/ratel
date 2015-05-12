package util;

import java.io.IOException;

import org.testng.annotations.Test;

public class RemoteCommandTest {

    private static final String LOGON_FAILURE = "Logon failure";
    private static final String SERVER = "nyws802";
    private static final String USERID = "mfranz";
    private static final String PASSWORD = "sun678Darwin";
    private static final String COMMAND = "dir";

    private static final String NET_STOP = "net stop";
    private static final String NET_START = "net start";

    private static final String SERVER_INVALID = "invalid_server_name_9876";
    private static final String USERID_INVALID = "invalid_user_5432";

    @Test(groups = { "unittest-broken", "remote-broken" })
    public void testRexec() throws IOException {

        RemoteCommand command = new RemoteCommand(SERVER, USERID, PASSWORD, COMMAND);
        String commandOutput = command.execute();

        assert commandOutput.contains("<DIR>") : "Failed to execute command";

        try {
            RemoteCommand failedCommand = new RemoteCommand(SERVER_INVALID, USERID, PASSWORD, COMMAND);
            String failedCommandOutput = failedCommand.execute();
            System.out.println(failedCommandOutput);

            assert false : "Command should have failed - invalid server";
        } catch (IOException e) {
            assert e.getMessage().contains(SERVER_INVALID);
        }

        try {
            RemoteCommand failedCommand = new RemoteCommand(SERVER, USERID_INVALID, PASSWORD, COMMAND);
            String failedCommandOutput = failedCommand.execute();
            System.out.println(failedCommandOutput);

            assert false : "Command should have failed - invalid user";
        } catch (IOException e) {
            assert e.getMessage().contains(LOGON_FAILURE);
        }

        try {
            RemoteCommand failedCommand = new RemoteCommand(SERVER, USERID, "FREDDY", COMMAND);
            String failedCommandOutput = failedCommand.execute();
            System.out.println(failedCommandOutput);

            assert false : "Command should have failed - invalid password";
        } catch (IOException e) {
            assert e.getMessage().contains(LOGON_FAILURE);
        }

        try {
            RemoteCommand failedCommand = new RemoteCommand(SERVER, USERID_INVALID, PASSWORD, COMMAND);
            String failedCommandOutput = failedCommand.execute();
            System.out.println(failedCommandOutput);

            assert false : "Command should have failed - invalid password";
        } catch (IOException e) {
            assert e.getMessage().contains(LOGON_FAILURE);
        }
        // TODO when the exception is 'Connection refused' the service is not running
    }

    @Test(groups = { "remote-broken" })
    public void testRestartServiceCommand() throws IOException {
        RemoteCommand stopCommand = new RemoteCommand(SERVER, USERID, PASSWORD, NET_STOP
                + " \"ActiveMQ 5.1.0\"");
        String stopCommandOutput = stopCommand.execute();

        assert stopCommandOutput.contains("service was stopped successfully") : "Failed to stop service";

        // stopping an already stopped service -- 'service is not started'
        RemoteCommand stopCommandAgain = new RemoteCommand(SERVER, USERID, PASSWORD, NET_STOP
                + " \"ActiveMQ 5.1.0\"");
        String stopAgainCommandOutput = stopCommandAgain.execute();

        assert stopAgainCommandOutput.contains("service is not started") : "Failed to stop a stopped service";

        RemoteCommand startCommand = new RemoteCommand(SERVER, USERID, PASSWORD, NET_START
                + " \"ActiveMQ 5.1.0\"");
        String startCommandOutput = startCommand.execute();

        assert startCommandOutput.contains("service was started successfully") : "Failed to start service";

        RemoteCommand startAgainCommand = new RemoteCommand(SERVER, USERID, PASSWORD, NET_START
                + " \"ActiveMQ 5.1.0\"");
        String startAgainCommandOutput = startAgainCommand.execute();

        assert startAgainCommandOutput.contains("service has already been started") : "Failed to start service a running service";
    }

}
