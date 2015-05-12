package systemdb.metadata;

import db.*;
import db.clause.*;
import db.columns.*;

public class Pv implements Comparable<Pv> {

    private final String name;

    public Pv(String name) {
        this.name = name;
    }

    @Override public int compareTo(Pv o) {
        return name.compareTo(o.name);
    }

    public String name() {
        return name;
    }
    
    @Override public String toString() {
        return name();
    }

    public Clause matches(StringColumn col) {
        return col.is(name());
    }

    public Cell<?> cell(StringColumn col) {
        return col.with(name());
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Pv other = (Pv) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

}
