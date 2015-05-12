package db.clause;


public class AndClause extends ConjunctionClause {
    private static final long serialVersionUID = 1L;

	public AndClause(Clause left, Clause right) {
		super(left, right, "and");
	}
}
