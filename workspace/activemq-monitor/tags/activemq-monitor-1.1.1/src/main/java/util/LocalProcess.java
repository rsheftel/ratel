package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LocalProcess {

    private String[] command;

    public LocalProcess(String command) {
        this.command = new String[1];
        this.command[0] = command;
    }

    public LocalProcess(String[] cmd) {  
        command = new String[cmd.length];
        System.arraycopy(cmd, 0, command, 0, cmd.length);
    }

    public String execute() throws IOException {

        Process localProcess = Runtime.getRuntime().exec(command);

        BufferedReader clientReader = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
        StringBuilder sb = new StringBuilder(1024);

        String line;
        while ((line = clientReader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        clientReader.close();
        return sb.toString();
    }

}
