package r.generator;

import static util.Strings.*;

public class Not implements RCode {

	private RCode code;
	
	public Not(RCode code) {
		this.code = code;
	}

	@Override public String toR() {
		return "!" + paren(code.toR());
	}
}
