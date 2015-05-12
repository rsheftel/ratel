package r.generator;

public class Assignment implements RCode {

	private RCode lhs;
	private RCode rhs;

	public Assignment(RCode lhs, RCode rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override public String toR() {
		return lhs.toR() + " <- " + rhs.toR();
	}

}
