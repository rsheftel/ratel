package r.generator;
import static util.Strings.*;
public class RString implements RCode {

	private String s;

	public RString(String s) {
		this.s = s;
	}

	@Override public String toR() {
		return dQuote(s);
	}
	
	public String string() {
		return s;
	}

}
