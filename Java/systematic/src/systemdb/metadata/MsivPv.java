package systemdb.metadata;

import static systemdb.metadata.MsivTable.*;
import static util.Objects.*;

import java.util.*;

import systemdb.metadata.MsivTable.*;
import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.SystemDB.*;

public class MsivPv implements Comparable<MsivPv> {

	private final MsivRow msiv;
	private final Pv pv;

	public MsivPv(MsivRow msiv, Pv pv) {
		this.msiv = msiv;
		this.pv = pv;
	}
	
	public MsivPv(String msiv, String pv) {
	    this(MSIVS.forName(msiv), new Pv(pv));
	}
	
	@Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((msiv == null) ? 0 : msiv.hashCode());
        result = prime * result + ((pv == null) ? 0 : pv.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final MsivPv other = (MsivPv) obj;
        if (msiv == null) {
            if (other.msiv != null) return false;
        } else if (!msiv.equals(other.msiv)) return false;
        if (pv == null) {
            if (other.pv != null) return false;
        } else if (!pv.equals(other.pv)) return false;
        return true;
    }

    @Override public String toString() {
		return "(" + msiv + ", " + pv + ")";
	}

	@Override public int compareTo(MsivPv o) {
		int msivs = this.msiv.compareTo(o.msiv);
		return msivs == 0 ? this.pv.compareTo(o.pv) : msivs;
	}

	public String fileName() {
	    return liveSystem().fileName(msiv.marketName());
	}

    public String market() {
        return msiv.marketName();
    }
    
    public static Clause matches(Collection<MsivPv> msivpvs, MSIVParameterValuesBase params) {
        List<String> msivpvStrings = empty();
        for (MsivPv msivpv : msivpvs)
            msivpvStrings.add(msivpv.msiv.name() + "::" + msivpv.pv.name());
        return new ConcatenationColumn(params.C_MSIV_NAME, "::").plus(params.C_PV_NAME).in(msivpvStrings);
    }
    
    public Clause matches(Column<String> msivColumn, Column<String> pvColumn) {
        return msivColumn.is(msiv.name()).and(pvColumn.is(pv.name()));
    }
    
    public String name() {
        return msivName() + ":" + pv;
    }

    public Pv pv() {
        return pv;
    }
    
    public LiveSystem liveSystem() {
        return new LiveSystem(siv(), pv());
    }

    public String msivName() {
        return msiv.name();
    }

    public Cell<?> msivCell(StringColumn col) {
        return msiv.cell(col);
    }
    
    public Cell<?> pvCell(StringColumn col) {
        return pv.cell(col);
    }

    public Siv siv() {
        return msiv.siv();
    }
    
    public static List<String> names(List<MsivPv> msivpvs) {
        List<String> result = empty();
        for (MsivPv msivPv : msivpvs)
            result.add(msivPv.name());
        return result;
    }

}
