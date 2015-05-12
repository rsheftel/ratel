package db;

import java.util.*;

import amazon.*;

import db.clause.*;

public class ConcreteColumn<T> extends Column<T> {
    private static final long serialVersionUID = 1L;

	private Table owner;
	Boolean exists;

	public ConcreteColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, nullable, owner.aliased() + ":" + name);
		this.owner = owner;
		owner.addColumn(this);
	}
	
	@Override public void collectTables(Set<Table> tables) {
		if (owner != null) owner.collectTables(tables);
	}

	@Override public String asSql() {
		return (owner == null ? "" : owner.aliased() + ".") + name();
	}

	public Table owner() {
		return owner;
	}

	/** create a join clause from this column to the <b>same named</b> column 
	 * on another table. */
	public Clause joinOn(Table table) {
		return this.is(table.column(this));
	}

	public void updateOne(Clause matches, T replace) {
		update(matches, replace, true);
	}
	
	private void update(Clause matches, T replace, boolean expectOne) {
		owner.update(with(replace), matches, expectOne);
	}
	
	public void updateAll(Clause matches, T replace) {
	    owner.update(with(replace), matches, false);
	}
	
    @Override public boolean isOwnedBy(Table table) {
        return owner() == table;
    }
	
    
    public boolean exists(T value) {
        return owner().rowExists(this.is(value));
    }

    public boolean exists() {
        if (S3Cache.sqsDbMode()) return true; // figure it out on the other side. 
        if (exists == null) exists = Schema.hasColumn(owner(), this);
        return exists;
    }

    @Override public boolean isConcrete() {
        return true;
    }

}
