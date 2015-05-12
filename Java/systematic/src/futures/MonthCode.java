package futures;

import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import util.*;

public enum MonthCode {

	F("January"), 
	G("February"), 
	H("March"), 
	J("April"), 
	K("May"), 
	M("June"), 
	N("July"), 
	Q("August"), 
	U("September"), 
	V("October"), 
	X("November"), 
	Z("December");
	
	private static final Map<Integer, MonthCode> codes = emptyMap();
	static { 
		for(MonthCode code : values()) codes.put(code.ordinal(), code);
	}
	private final String name;

	MonthCode(String name) {
		this.name = name;
	}
	
	@Override public String toString() {
		return super.toString().toLowerCase();
	}

	public static MonthCode quarter(Date asOf) { // march june sep dec
		return quarter(month(asOf));
	}

	private static MonthCode quarter(MonthCode month) {
		int index = month.ordinal();
		return codes.get(index / 3 * 3 + 2);
	}

	public static MonthCode month(Date asOf) {
		return codes.get(calendar(asOf).get(Calendar.MONTH));
	}

	public static MonthCode fromChar(char charAt) {
		return valueOf("" + Character.toUpperCase(charAt));
	}
	
	public static MonthCode fromChar(String s) {
	    bombUnless(s.length() == 1, "multi-character strings are not allowed in fromChar: " + s);
	    return fromChar(s.charAt(0));
	}

	public String numberString() {
        return "" + number();
    }

    public int number() {
        return ordinal() + 1;
    }

	public static MonthCode fromNumber(String twoDigit) {
		return fromNumber(Integer.parseInt(twoDigit));
	}
	
	public static MonthCode fromNumber(int number) {
	    return bombNull(codes.get(number - 1), "no month code for # " + number);
	}

	public String letter() {
		return toString();
	}
	
	public String longName() {
		return name;
	}

	public MonthCode toQuarter() {
		return quarter(this);
	}

	public static boolean isQuarter(YearMonth ym) {
		return ym.month() % 3 == 0;
	}

    public YearMonth yearMonth(Date d) {
        return new YearMonth(year(d), number());
    }
	
}
