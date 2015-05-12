package db;

import db.clause.*;
import db.columns.*;

public abstract class Named {

	private final String name;

	public Named(String name) {
		this.name = name;
	}
	public int id() {
		return idColumn().value(nameColumn().is(name));
	}
	
	protected abstract StringColumn nameColumn();
	protected abstract Column<Integer> idColumn();

	public Clause joinIdToName(IntColumn other) {
		return idColumn().is(other).and(nameColumn().is(name));
	}
	
	public String name() { 
		return name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Named other = (Named) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	public boolean exists() {
		try {
			idColumn().value(nameColumn().is(name));
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}
	
	
}
