package r.generator;

import static r.generator.RStrings.*;

import java.util.*;
import static util.Objects.*;
import util.*;
public class Anonymous implements RCode {

	private List<RCode> args;
	private Block body;

	public Anonymous(List<RCode> args, Block body) {
		this.args = args;
		this.body = body;
	}
	public Anonymous(RCode[] args, Block body) {
		this(list(args), body);
	}

	@Override public String toR() {
		return "function" + Strings.paren(commaSep(args)) + body.toR();
	}
	
}
