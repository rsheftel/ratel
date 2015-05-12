package db;

import java.sql.*;

public class ColumnAssignment<T> extends Cell<T> {

    private static final long serialVersionUID = 1L;
    private final Column<T> assignment;

	public ColumnAssignment(Column<T> assignee, Column<T> assignment) {
		super(assignee, null);
		this.assignment = assignment;
	}
	
	@Override public String asAssignmentString() {
		return column.name() + " = " + assignment.asSql();
	}

	@Override public int bindInto(int n, LoggingPreparedStatement st) throws SQLException {
		return n;
	}
}
