package util;

import static util.Errors.*;
import static util.Log.*;
import static util.Strings.*;

import java.util.*;

import tsdb.*;


public class Arguments extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;
    private final List<String> allowed;

	public Arguments(List<String> allowed) {
        this.allowed = allowed;
    }

    public String get(String name, String defalt) {
		return containsKey(name) ? get(name) : logUsing(name, defalt, true);
	}
	
	public Date get(String name, Date defalt) {
		return containsKey(name) ? date(name) : logUsing(name, defalt, true);
	}
	
	public int get(String name, int defalt) {
	    return containsKey(name) ? integer(name) : logUsing(name, defalt, true);
	}

    public boolean get(String key, boolean default_) {
        return containsKey(key) 
            ? toBoolean(get(key), default_) 
            : logUsing(key, default_, true);
    }
	
	private <T> T logUsing(String name, T value, boolean isDefault) {
	    info("using -" + name + " " + value + (isDefault ? " [default]" : ""));
	    return value;
	}
	
	@Override public String get(Object key) {
		String result = super.get(key);
        return bombEmpty(logUsing(key + "", result, false), "required argument " + key + " is missing. allowed: " + allowed);
	}
	
	public String string(String name) {
		return get(name);
	}
	
	public Date date(Object key) {
	    return Dates.date(get(key));
	}
	
	public int integer(String key) {
	    return Integer.parseInt(get(key));
	}
	
	public double numeric(String key) {
		return Double.parseDouble(get(key));
	}
	
	public static AttributeValues values(Arguments arguments) {
        AttributeValues values = new AttributeValues();
    	for (String attr : arguments.keySet())
    		values.add(Attribute.attribute(attr).value(arguments.get(attr)));
        return values;
    }

    public static Arguments arguments(String[] args, List<String> allowed ) {
		bombUnless(args.length % 2 == 0, "args must be alternating key/value pairs: \n" + join("\n:", args));
		Arguments result = new Arguments(allowed);
		for(int i = 0; i < args.length; i++) {
		    bombUnless(args[i].charAt(0) == '-', 
		        "argument names must start with a - character (did you forget an argument name?): " + args[i]);
			String name = args[i].substring(1);
			if (!allowed.isEmpty())
				bombUnless(allowed.contains(name), name + " is not in allowed arguments: " + allowed);
			result.put(name, args[++i]);
		}
		return result;
	}

    public double get(String key, double default_) {
        return containsKey(key) ? Double.parseDouble(get(key)) : logUsing(key, default_, true);
    }

}