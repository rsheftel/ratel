package db.columns;

import static util.Objects.*;
import db.*;
import db.clause.*;

public class StringColumn extends ConcreteColumn<String> {
    private static final long serialVersionUID = 1L;

	public StringColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}

	/** given a sql "LIKE" string, constructs a clause out of it. */
	public Clause like(String like) {
		return new LikeClause(this, like);
	}
	
	public Clause isWithoutCase(String s) {
		return new ComparisonClause<String>(this.upper(), s.toUpperCase());
	}

	public Clause hasContent() {
		return isNotNull().and(isNot(""));
	}
	
	public FunctionColumn<String> upper() {
		return new FunctionColumn<String>("upper", array(this));
	}
	
	public ConcatenationColumn plus(String constant) {
		return new ConcatenationColumn(this, constant);
	}
	
	public ConcatenationColumn plus(Column<String> col) {
		return new ConcatenationColumn(this, col);
	}
	
	@Override public String valueFromString(String s) {
		return s;
	}
}
