package db;

public enum Comparison {
	EQ("="), NE("!="), LT("<"), LE("<="), GT(">"), GE(">="); 
	
	final String op;

	Comparison(String op) {
		this.op = op;
	}

	public String op() {
		return op;
	}
}