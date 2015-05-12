package db.columns;

import java.util.*;
import static db.Table.*;

import util.*;
import db.*;

public class ConstantColumn<T> extends SyntheticColumn<T> {
    private static final long serialVersionUID = 1L;

	private final T value;

	public ConstantColumn(T value) {
		super("c_" + value, null, NOT_NULL, "c_" + value);
		this.value = value;
	}
	
	@Override public String asSql() {
		return Sql.quote(String.valueOf(value));
	}
	
	@Override public void collectTables(Set<Table> tables) {}

	@Override public String asSelect() {
		return asSql() + " as " + name();
	}
	
	public static <T> Column<T> constant(T value) { return new ConstantColumn<T>(value); }

    @Override public String string(T t) {
        return String.valueOf(t);
    }
}
