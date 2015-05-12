package file;

import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.util.*;

import util.*;

import au.com.bytecode.opencsv.*;

public class CsvStreamer {

    private CSVReader reader;
    private final List<String> header;
    private final QFile file;
    
    public CsvStreamer(QFile file) { this(file, false); }
    public CsvStreamer(QFile file, boolean hasHeader) {
        this.file = file;
        Log.info("reading csv " + file.name() + " " + file.path());
        Reader innerReader = null;
        try {
			innerReader = file.reader();
			reader = new CSVReader(innerReader);
	        header = hasHeader ? next() : null;
        } catch(RuntimeException e) {
        	if(innerReader != null) QFile.maybeClose(innerReader);
        	throw e;
        }
    }
    
    public List<String> next() {
        try {
            while(true) {
                String[] line = reader.readNext();
                if(line == null) return null;
                if (line.length == 0) continue;
                if (line.length == 1 && isEmpty(line[0])) continue;
                return list(line);
            }
        } catch (IOException e) {
            throw bomb(file.fileError("Error parsing CSV"));
        }
    }
    
    public List<String> header() {
        return header;
    }
    
    public void close() {
        try {
            reader.close();
        } catch (IOException uncatchable) {
            uncatchable.printStackTrace();
        }
        if (Log.verbose()) info("closed csv " + file.name() + " " + file.path());
    }

}
