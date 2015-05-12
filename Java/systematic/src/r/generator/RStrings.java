package r.generator;

import static util.Objects.*;

import java.util.*;

import util.*;

public class RStrings {

	public static String commaSep(RCode ... args) {
		return commaSep(list(args));
	}

	public static String commaSep(List<RCode> args) {
		return Strings.commaSep(rStrings(args));
	}
	
	public static List<String> rStrings(RCode... args) {
		return rStrings(list(args));
	}

	public static List<String> rStrings(List<RCode> args) {
		List<String> s = empty();
		for (RCode arg : args)
			s.add(arg.toR());
		return s;
	}
	
}
