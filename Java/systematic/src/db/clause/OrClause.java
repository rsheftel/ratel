package db.clause;


public class OrClause extends ConjunctionClause {
    private static final long serialVersionUID = 1L;

	public OrClause(Clause left, Clause right) {
		super(left, right, "or");
	}


}
