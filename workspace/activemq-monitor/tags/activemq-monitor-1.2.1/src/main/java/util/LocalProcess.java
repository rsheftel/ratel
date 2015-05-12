package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LocalProcess {

    private String[] command;
    
    private Process subprocess;

    public LocalProcess(String command) {
        this.command = new String[1];
        this.command[0] = command;
    }

    public LocalProcess(String[] cmd) {  
        command = new String[cmd.length];
        System.arraycopy(cmd, 0, command, 0, cmd.length);
    }

    public String execute() throws IOException {
        return execute(-1);
    }
    
    public String execute(int linesToRead) throws IOException {
        subprocess = Runtime.getRuntime().exec(command);

        BufferedReader clientReader = new BufferedReader(new InputStreamReader(subprocess.getInputStream()));
        StringBuilder sb = new StringBuilder(1024);

        String line;
        int lines = 0;
        while ((line = clientReader.readLine()) != null && (lines < linesToRead || linesToRead == -1)) {
            sb.append(line).append("\n");
            lines++;
        }

        clientReader.close();
        return sb.toString();
        
    }
    
    public void kill() {
        if (subprocess != null) {
            subprocess.destroy();
        }
    }

    
    
}
