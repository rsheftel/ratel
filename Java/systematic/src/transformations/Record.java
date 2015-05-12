package transformations;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

public class Record {

	private String name;
	Map<String, String> fields = emptyMap();
	
	public Record(String name, String key, String value) {
	    this.name = name;
        fields.put(key, value);
	}
	
    public String name() {
	    return name;
	}
	
    public Set<String> keys() {
        return fields.keySet();
    }
	
    public String string(String key) {
        return bombNull(fields.get(key), name() + " does not contain a field named " + key + "\ncontents\n" + fields);
    }

    
    public static <T> Record record(String name, T value) {
        return record(name, "LastPrice", value);
    }
    
    public static <T> Record record(String name, String key, T value) {
        return new Record(name, key, String.valueOf(value));
    }
}