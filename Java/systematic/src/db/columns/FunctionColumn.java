package db.columns;

import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import db.*;

public class FunctionColumn<T> extends SyntheticColumn<T> {
    private static final long serialVersionUID = 1L;

	private final String function;
	private final List<Column<T>> targets;

	public FunctionColumn(String function, Column<T>[] targets) {
		this(function, list(targets));
	}
	
	public FunctionColumn(String function, List<Column<T>> targets) {
        super(function + javaClassify(asSelect("", columns(targets))), first(targets).type(), Table.NOT_NULL, identity(function, targets));
        this.function = function;
        this.targets = targets;
	}   
    
    public static <T> FunctionColumn<T> coalesce(Column<T> ... columns) {
        return function("coalesce", columns);
    }
	
    private static <T> String identity(String function, List<Column<T>> targets) {
        String result = function + "_";
        for (Column<T> target : targets)
            result += target.identity() + "_";
        return result ;
    }

    @SuppressWarnings("unchecked") public FunctionColumn(String function, Column<T> target) {
	    this(function, array(target));
	}
	
	@Override public String asSql() {
	    List<String> sqls = empty();
	    for(Column<T> c : targets) sqls.add(c.asSql());
		return function + paren(commaSep(sqls));
	}

	@Override public String asSelect() {
		return asSql() + " as " + name();
	}

	@Override public void collectTables(Set<Table> tables) {
        for(Column<T> c : targets) c.collectTables(tables);
	}
	
	public static <T> FunctionColumn<T> function(String function, Column<T>[] targets) {
	    return new FunctionColumn<T>(function, targets);
	}
	 
	public static <T> FunctionColumn<T> function(String function, List<Column<T>> targets) {
	    return new FunctionColumn<T>(function, targets);
	}
	
	@Override public String string(T t) {
	    bombIf(isEmpty(targets), "no target to use for stringing " + t);
	    return first(targets).string(t);
	}
}
