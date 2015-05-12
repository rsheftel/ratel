package db;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;
public class StringRow extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;

	public static class Pair {
		final String key;
		final String value;

		Pair(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}

	public static Pair c(String key, String value) {
		return new Pair(key, value);
	}
	
	public StringRow(Pair ... ps) {
		for (Pair pair : ps) 
			put(pair.key, pair.value);
	}
	
	public String java(String ... keys) {
		StringBuilder values = new StringBuilder();
		for(Iterator<String> i = list(keys).iterator(); i.hasNext(); ) {
			String key = i.next();
			values.append("        c(" + dQuote(key) + ", " + dQuote(get(key)) + ")");
			if (i.hasNext()) values.append(",\n");
		}
		return "new StringRow(\n" + values.toString() + "\n    )";
	}
}
