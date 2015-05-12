package db.columns;
import static db.Table.*;
import static util.Strings.*;

import java.util.*;

import db.*;

public class BinaryOperatorColumn<T> extends SyntheticColumn<T> {
    private static final long serialVersionUID = 1L;

	private final Column<T> lhs;
	private final Column<T> rhs;
	private final Operator operator;

	enum Operator {
		PLUS("+", "plus"), MINUS("-", "minus"), TIMES("*", "times"), DIVIDE("/", "dividedBy"), MOD("%", "mod");
		private final String op;
		private final String name;

		Operator(String op, String name) {
			this.op = op;
			this.name = name;
		}
	}
	
	public BinaryOperatorColumn(Operator operator, Column<T> lhs, Column<T> rhs) {
		super(lhs.name() + "_" + operator.name.toUpperCase() + "_" + rhs.name(), lhs.type(), NULL, lhs.identity() + "_" + rhs.identity());
		this.operator = operator;
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override public void collectTables(Set<Table> tables) {
		lhs.collectTables(tables);
		rhs.collectTables(tables);
	}
	
	@Override public String asSql() {
		return paren(paren(lhs.asSql()) + " " + operator.op + " " + paren(rhs.asSql()));
	}

	@Override public String asSelect() {
		return asSql() + " as " + name();
	}
	
	public static <T> BinaryOperatorColumn<T> plus(Column<T> augend, Column<T> addend) {
		return new BinaryOperatorColumn<T>(Operator.PLUS, augend, addend);
	}
	
	public static <T> BinaryOperatorColumn<T> minus(Column<T> minuend, Column<T> subtrahend) {
		return new BinaryOperatorColumn<T>(Operator.MINUS, minuend, subtrahend);
	}
	
	public static <T> BinaryOperatorColumn<T> times(Column<T> multiplicand, Column<T> multiplier) {
		return new BinaryOperatorColumn<T>(Operator.TIMES, multiplicand, multiplier);
	}
	
	public static <T> BinaryOperatorColumn<T> divide(Column<T> dividend, Column<T> divisor) {
		return new BinaryOperatorColumn<T>(Operator.DIVIDE, dividend, divisor);
	}
	
	public static <T> BinaryOperatorColumn<T> mod(Column<T> dividend, Column<T> divisor) {
		return new BinaryOperatorColumn<T>(Operator.MOD, dividend, divisor);
	}

    @Override public String string(T t) {
        return String.valueOf(t);
    }

}
