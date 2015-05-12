package file;

import static util.Errors.*;
import static util.Index.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.util.*;

import util.*;
import au.com.bytecode.opencsv.*;


public class Csv {


    Map<String, Integer> header = emptyMap();
    List<String> headerColumns = empty();
	List<List<String>> records = empty();
    private final boolean useQuotesOnWrite;
    private final QFile inputFile;

    public Csv() { this(true); }
	public Csv(boolean useQuotesOnWrite) { this.useQuotesOnWrite = useQuotesOnWrite; inputFile = null; }
	public Csv(QFile file) { this(file, false); }
	public Csv(QFile file, boolean hasHeader) { 
	    this.inputFile = file;
        this.useQuotesOnWrite = false;
        CsvStreamer streamer = new CsvStreamer(file, hasHeader);
        try {
    	    if (hasHeader) 
    	        addHeader(streamer.header());
    	    load(streamer);
        } finally {
            streamer.close();
        }
	}
    
    public void addHeader(List<String> headerRecord) {
        for (Index<String> i : indexing(headerRecord)) { 
            String string = i.value.trim();
            header.put(string, i.num);
            headerColumns.add(string);
        }
    }
    
    public void addHeader(String ... headerRecord) { 
        addHeader(list(headerRecord));
    }
	
	public void add(List<String> record) {
		records.add(record);
	}
	
	public void add(String ... parts) {
	    add(list(parts));
	}

	public List<List<String>> records() {
		bombIf(records.isEmpty(), "records not loaded or added");
		return records;
	}
	
	private void load(CsvStreamer streamer) {
		bombUnless(records.isEmpty(), "cannot load " + inputFile.path() + " records already loaded!");
		while(true) {
            List<String> line = streamer.next();
            if(line == null) break;
            records.add(list(line));
        }
	}
	
	public static void main(String[] args) {
	    new Csv(new QFile("V:\\Futures\\CSI\\Core Energy Futures\\CL21480B.CSV"));
    }
	
	public void write(QFile file) {
		file.requireNotExists();
		Writer appender = file.appender();
		try {
			write(appender);
		} catch (RuntimeException e) {
			throw bomb("failed", e);
		} finally { 
			try { if (appender != null) appender.close(); } 
			catch (IOException e) { e.printStackTrace(); }
		}
	}

	private void write(Writer appender) {
		CSVWriter w = useQuotesOnWrite ? new CSVWriter(appender) : new CSVWriter(appender, ',', CSVWriter.NO_QUOTE_CHARACTER);
		if(!headerColumns.isEmpty())
		    w.writeNext(headerColumns.toArray(new String[0]));
		for (List<String> record : records) 
			w.writeNext(record.toArray(new String[0]));
	}
	
	public String asText() {
		StringWriter w = new StringWriter();
		write(w);
		return w.toString();
	}
	
    public int count() {
        return records.size();
    }
    
    public String value(String column, List<String> record) {
        Integer index = bombNull(header.get(column), "no index found for " + sQuote(column) + " in " + commaSep(header.keySet()));
        return bombNull(record.get(index), "no value in record for " + column);
    }

    public String value(String column, int record) {
        return value(column, record(record));
    }
    public List<String> record(int i) {
        return records.get(i);
    }
    public List<String> columns() {
        return headerColumns;
    }
    public void overwrite(QFile file) {
        file.deleteIfExists();
        write(file);
    }
    public void split(int maxPerFile, QDirectory dir, String prefix) {
        dir.createIfMissing();
        int i = 0;
        int fileNum = 1;
        Csv csv = emptyCopy();
        for (List<String> record : records) {
            csv.add(record);
            if(++i == maxPerFile) {
                i = 0;
                writeSplitCsv(dir, prefix, fileNum++, csv);
            }
        }
        if(i != 0) writeSplitCsv(dir, prefix, fileNum, csv);
    }
    
    private Csv emptyCopy() {
        Csv csv = new Csv(useQuotesOnWrite);
        csv.header.putAll(header);
        csv.headerColumns.addAll(headerColumns);
        return csv;
    }
    
    private void writeSplitCsv(QDirectory dir, String prefix, int fileNum, Csv csv) {
        QFile file = dir.file(prefix + "-" + fileNum + ".csv");
        csv.overwrite(file);
        info("writing " + csv.count() + " records into " + file.path());
        csv.clear();
    }
    
    private void clear() {
        records = empty();
    }
    
    public static List<String> header(QFile file) {
        String[] line;
        CSVReader r = null;
        try {
            r = new CSVReader(new StringReader(file.text()));
            line = r.readNext();
            while (true) {
                if (line == null) continue;
                if (line.length == 0) continue;
                if (line.length == 1 && isEmpty(line[0])) continue;
                List<String> result = empty();
                for (String string : line)
                    result.add(string.trim());
                return result;
            }
        } catch (IOException e) {
            throw bomb("failed to parse file text as csv", e);
        }finally {
            if (r != null) try {
                r.close();
            } catch (IOException e) {
                info("failed closing csv reader on " + file , e);
            }
        }
    }

	

}
