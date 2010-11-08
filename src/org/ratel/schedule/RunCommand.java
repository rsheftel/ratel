package org.ratel.schedule;

import static org.ratel.schedule.JobStatus.*;
import static org.ratel.transformations.Constants.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Strings.*;

import java.io.*;
import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.util.*;

public class RunCommand implements Schedulable {

    private StreamGobbler output;
    private StreamGobbler error;
    private String jobName;

    @Override public JobStatus run(Date asOf, Job item) {
        String command = item.parameterExpanded("command", asOf);
        jobName = item.name();
        int exit = runCommand(command);
        if(exit != 0) {
            String errorString = "command " + paren(command) + " failed with non-zero exit code: " + exit;
            errorString = errorString + ifAvailable(error, "ERR") + ifAvailable(output, "OUT"); 
            Log.err(errorString);
            throw bomb(errorString);
        }
        return SUCCESS;
    }

    // Command line runner adapted from:
    // http://www.javalobby.org/java/forums/t53333.html

    private String ifAvailable(StreamGobbler gobbler, String type) {
        if (gobbler == null) return "\nNO STD" + type + "\n"; 
        return "\nSTD" + type + ":\n\n" + gobbler.text() + 
            "\n----------------------------------------------------------\n";
    }
    
    public String out() {
        return ifAvailable(output, "OUT");
    }
    
    public String err() {
        return ifAvailable(error, "ERR");
    }

    public int runCommand(String commandLine) {
        Log.info("running command: " + commandLine);
        Process process = process(commandLine);
        try {
            return process.waitFor();
        } catch (Throwable ex) {
            throw bomb("command " + commandLine + " failed", ex);
        } finally {
            stopReaders();
        }
    }
    
    private Process process(String command) {
        try {
            String[] args = isWindows() 
                ? array("cmd", "/c", command)
                : array("sh", "-c", command);
            Process process = Runtime.getRuntime().exec(args);
            output = new StreamGobbler(process.getInputStream(), false, logStream(), jobName);
            output.start();
            error = new StreamGobbler(process.getErrorStream(), true, errStream(), jobName);
            error.start();
            return process;
        } catch (IOException e) {
            throw bomb("command " + command + " failed", e);
        }
    }
    
    private void stopReaders() {
        output.stopReading();
        error.stopReading();
    }

    static class StreamGobbler extends Thread {
        private StringBuffer buffer = new StringBuffer();
        private InputStream input = null;
        private boolean stop = false;
        private final boolean isErr;
        private final PrintStream logStream;
        private final String jobName;
        
        public StreamGobbler(InputStream input, boolean isErr, PrintStream logStream, String jobName) {
            this.input = input;
            this.isErr = isErr;
            this.logStream = logStream;
            this.jobName = jobName;
        }    
        
        public String text() {        
            return buffer.toString();
        }
        
        @Override public void run() {
            Log.setBothStreams(logStream);
            Log.setContext(jobName);
            try {
                readCommandOutput();
            } catch (Exception ex) {
                bomb("caught exception", ex);
            }
        }
        
        private void readCommandOutput() throws IOException {        
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));        
            String line = null;
            while ( (stop == false) && ((line = reader.readLine()) != null) ) {
                buffer.append(line + "\n");
                write(line);
            }        
            reader.close();
        }
        
        public void stopReading() {
            stop = true;
        }
        
        private void write(String line) {
            if(isErr) Log.err(line);
            else info(line);
        }

    }


    
}
