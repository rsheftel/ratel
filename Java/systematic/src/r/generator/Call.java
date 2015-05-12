package r.generator;

import static r.generator.RStrings.*;
import static util.Strings.*;

import java.util.*;

public class Call implements RCode {

	private final String name;
	private final List<RCode> parameters;

	public Call(String name, List<RCode> parameters) {
		this.name = name;
		this.parameters = parameters;
	}

	@Override public String toR() {
		return name + paren(commaSep(parameters));
	}
	

}
