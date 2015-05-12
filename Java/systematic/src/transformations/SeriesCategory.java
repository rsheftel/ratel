package transformations;

import static util.Objects.*;

import java.util.*;

public class SeriesCategory {
    
    private final String name;
    private final List<Record> records;

    public static SeriesCategory category(String name, Record ... records) {
        return new SeriesCategory(name, list(records));
    }

    public SeriesCategory(String name, List<Record> records) {
        this.name = name;
        this.records = records;
    }

	public String name() { return name; }
	public List<Record> records() { return copy(records); }

}