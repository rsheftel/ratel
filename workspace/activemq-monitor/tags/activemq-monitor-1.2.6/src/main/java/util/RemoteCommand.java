package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.net.bsd.RExecClient;

public class RemoteCommand {

    private String remoteHost;
    private String remoteHostUserID;
    private String userPassword;
    private String command;

    public RemoteCommand(String server, String userID, String password, String command) {
        remoteHost = server;
        remoteHostUserID = userID;
        userPassword = password;
        this.command = command;
    }

    public RemoteCommand(String server, String userID, String password) {
        remoteHost = server;
        remoteHostUserID = userID;
        userPassword = password;
    }
    
    
    public String execute() throws IOException {
        return execute(command);
    }
    
    public String execute(String currentCommand) throws IOException {
        RExecClient client = new RExecClient();

        client.connect(remoteHost);
        client.rexec(remoteHostUserID, userPassword, currentCommand);

        BufferedReader clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        StringBuilder sb = new StringBuilder(1024);

        String line;
        while ((line = clientReader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        client.disconnect();
        clientReader.close();

        return sb.toString();
    }
    

}
