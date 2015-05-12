package r.generator;

import static r.generator.RFactory.*;

public class RObject extends RawR {

	public RObject(String name) {
		super(name);
	}

	public RCode var(String string) {
		return rawR(toR() + "$" + string);
	}
}
