package util;

import static util.Errors.*;
import static util.Objects.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.commons.codec.binary.*;

import db.columns.*;
public class Strings {

    private static final String SPACES = "                                                        ";

    public static String leftSpacePad(int desired, String string) {
        if (string.length() > desired) return string;
        return SPACES.substring(0, desired - string.length()) + string;
    }
    
	public static String dQuote(String s) {
		return wrap(s, "\"");
	}
	
	private static String wrap(String s, String wrapping) {
		return wrap(wrapping, s, wrapping);
	}

    public static boolean toBoolean(String human, boolean defalt) {
        return isEmpty(human) ? defalt : BitColumn.toBoolean(human);
    }

    private static String wrap(String left, String s, String right) {
		bombNull(left, "no left!");
		bombNull(left, "no right!");
		return left + bombNull(s, "no s!") + right;
	}

	public static String sQuote(String s) {
		return wrap(s, "'");
	}
	
	public static String paren(String s) {
		return wrap("(", s, ")");
	}
	
	public static String brace(String s) {
		return wrap("{", s, "}");
	}
	
	public static String bracket(String s) {
		return wrap("[", s, "]");
	}
	
	public static List<String> split(String delim, String s) {
		bombNull(s, "can't split null value!");
		if (!s.contains(delim)) return list(s);
		int i = 0;
		List<String> result = empty();
		while (i < s.length()) {
			int newI = s.indexOf(delim, i);
			if (newI == -1) newI = s.length();
			result.add(s.substring(i, newI));
			i = newI + delim.length();
		}
		if (s.endsWith(delim)) result.add("");
		return result;
	}

	public static String chomp(String maybeLineTerminated) { 
	    return maybeLineTerminated.replaceFirst("[\n\r]*$", "");
	}
	
	public static String join(String delim, String ... s) {
		return join(delim, list(s));
	}
	
	public static <T> List<String> strings(T ... ts) {
		return strings(list(ts));
	}

	public static <T> List<String> strings(Collection<T> ts) {
		List<String> result = empty();
		for (T t : ts) 
			result.add(t == null ? "null" : t.toString());
		return result;
	}
	
	public static String join(String delim, Collection<String> strings) {
		StringBuilder b = new StringBuilder();
		for(Iterator<String> i = strings.iterator(); i.hasNext(); ) {
			b.append(i.next());
			if (i.hasNext()) b.append(delim);
		}
		return b.toString();
	}
    
    public static String javaClassify(String name) {
        if (isEmpty(name)) return "";
        String alpha = name.replaceAll("[^A-Za-z0-9]+", "|");
        alpha = alpha.replaceAll("([0-9])([a-z])", "$1|$2");
        String[] words = alpha.split("\\|");
        for(int i = 0; i < words.length; i++) {
            if (isEmpty(words[i])) continue;
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }
        return join("", words);
    }
    
    public static String javaIdentifier(String name) {
        if (isEmpty(name)) return "";
        String result = javaClassify(name);
        return result.substring(0, 1).toLowerCase() + result.substring(1);
    }

	public static String javaConstify(String name) {
		String alpha = name.replaceAll("[^A-Za-z0-9_]+", "");
		return alpha.toUpperCase();
	}
    
    public static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }
    
    public static boolean isEmpty(Collection<?> foos) {
        return bombNull(foos, "null foos!").isEmpty();
    }
    public static boolean hasContent(Collection<?> foos) {
        return !isEmpty(foos);
    }
	public static boolean hasContent(String s) {
		return !isEmpty(s);
	}

	public static String commaSep(Collection<String> s) {
		return join(", ", s);
	}
	
	public static String commaSep(String ... s) {
		return commaSep(list(s));
	}
	
	public static String commaSep(Object ... s) {
	    return commaSep(strings(s));
	}
	
	public static String leftZeroPad(int num, int length) { 
		NumberFormat f = NumberFormat.getIntegerInstance();
		f.setMinimumIntegerDigits(length);
		f.setGroupingUsed(false);
		return f.format(num);
	}
	public static String human(double d) { 
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumFractionDigits(12);
		f.setGroupingUsed(false);
		return f.format(d);
	}
    
    public static <K extends Comparable<K>,V> String toSortedHumanString(Map<K, V> data) {
        List<K> keys = list(data.keySet());
        Collections.sort(keys);
        return toHumanString(data, keys);
    }

    private static <K, V> String toHumanString(Map<K, V> data, Iterable<K> keys) {
        if (data.isEmpty()) return "{}";
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        for(K k : keys)
            buf.append("\t").append(k).append("==").append(data.get(k)).append("\n");
        buf.append("}");
        return buf.toString();
    }
    
    public static <K ,V> String toHumanString(Map<K, V> data) {
        return toHumanString(data, data.keySet());
    }


	public static String nDecimals(int n, double value) {
	    if(Double.isInfinite(value)) return value > 0 ? "Inf" : "-Inf";
	    if(Double.isNaN(value)) return "NaN";
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(n);
		nf.setMaximumFractionDigits(n);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		nf.setGroupingUsed(false);
		return nf.format(value);
	}

    public static String sprintf(String format, Object ... params) {
        ByteArrayOutputStream w = null;
        PrintStream out = null;
        try {
            w = new ByteArrayOutputStream();
            out = new PrintStream(w);
            out.printf(format, params);
            return w.toString();
        } finally {
            if (out != null) out.close();
        }
    }
    
	public static String toBase64(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}

	public static byte[] fromBase64(String base64) {
		return Base64.decodeBase64(base64.getBytes());
	}
}
