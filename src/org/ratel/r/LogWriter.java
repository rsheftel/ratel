package org.ratel.r;

import java.io.*;

import static org.ratel.r.Util.*;

public class LogWriter {

    private final Writer writer;
    
    public LogWriter(Writer writer) {
        this.writer = writer;
    }

    public void message(String message) {
        try {
            writer.append(message);
            writer.flush();
        } catch (IOException e) {
            throw bomb("error writing to log", e);
        }
    }

    public void info(String text) {
        message(text);
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw bomb("could not close file!", e);
        }
    }

}
