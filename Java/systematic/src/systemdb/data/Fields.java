package systemdb.data;

import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.Serializable;
import java.util.*;

import util.*;

public class Fields extends HashMap<String, String> implements Serializable {
	private static final long serialVersionUID = 1L;

    public Fields copy() {
        Fields fields = new Fields();
        fields.putAll(this);
        return fields;
    }

    public double numeric(String key) {
		return Double.parseDouble(getNotNull(key));
	}

	public Date time(String key) {
		return date(getNotNull(key));
	}

    public int integer(String key) {
        return Integer.parseInt(getNotNull(key));
    }

    public long longg(String key) {
        return Long.parseLong(getNotNull(key));
    }
    
    public byte[] bytes(String key) {
    	return getNotNull(key).getBytes();
    }

	private String getNotNull(String key) {
		return bombNull(get(key), "no value for " + key);
	}
	
	public String text(String key) { 
	    return getNotNull(key);
	}

    public String messageText() {
        List<String> fields = empty();
        for (String key : keySet()) 
            fields.add(key + "=" + get(key));
        return join("|", fields);
    }

    public void put(String key, Date value) {
        put(key, ymdHuman(value));
    }

    public void put(String key, double value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }

    public long longMaybe(String key) {
        try {
            return longg(key);
        } catch(Exception e) {
            return 0;
        }
    }
    
    public boolean hasContent(String key) { 
        return !Strings.isEmpty(key);
    }
    
    public boolean isEmpty(String key) { 
        return !hasContent(key);
    }
    
    public boolean hasValue(String key, String value) {
        return hasContent(key) ? text(key).equals(value) : false;
    }

    public boolean isLong(String key) {
        try {
            longg(key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

	public static Fields parse(String text) {
		String[] fields = text.split("\\|");
		Fields result = new Fields();
		for(String s : fields) {
			String [] parts = s.split("=", 2);
			result.put(parts[0], parts.length == 1 ? "" : parts[1]);
		}
		return result;
	}
	
}