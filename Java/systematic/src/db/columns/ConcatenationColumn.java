package db.columns;

import static db.columns.ConstantColumn.*;
import static util.Strings.*;

import java.util.*;

import db.*;

public class ConcatenationColumn extends SyntheticColumn<String> {
    private static final long serialVersionUID = 1L;

	private final Column<String> lhs;
	private final Column<String> rhs;

	public ConcatenationColumn(Column<String> lhs, String constant) {
		this(lhs, constant(constant));
	}

	public ConcatenationColumn(Column<String> lhs, Column<String> rhs) {
		super(javaClassify(lhs.toString()) + "_concat", lhs.type(), Table.NOT_NULL, lhs.identity() + "_concat_" + rhs.identity());
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public ConcatenationColumn(String string, Column<String> rhs) {
	    this(constant(string), rhs);
	}

    @Override public String asSql() {
		return lhs.asSql() + " + " + rhs.asSql();
	}

	@Override public String asSelect() {
		return asSql() + " as " + name();
	}

	@Override public void collectTables(Set<Table> tables) {
		lhs.collectTables(tables);
		rhs.collectTables(tables);
	}
	
	public ConcatenationColumn plus(String constant) {
		return new ConcatenationColumn(this, constant);
	}
	
	public ConcatenationColumn plus(Column<String> col) {
		return new ConcatenationColumn(this, col);
	}
	
	@Override public String string(String t) {
	    return t;
	}
}
