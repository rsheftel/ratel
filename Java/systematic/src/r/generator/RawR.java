package r.generator;

public class RawR implements RCode {

	private String code;
	
	public RawR(String code) {
		this.code = code;
	}

	@Override public String toR() {
		return code;
	}
}
