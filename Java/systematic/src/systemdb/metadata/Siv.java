package systemdb.metadata;

import static systemdb.metadata.MsivLiveHistory.*;
import static systemdb.metadata.ParameterValuesTable.*;
import static systemdb.metadata.SystemTable.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;
import static systemdb.metadata.MsivTable.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.columns.*;

public class Siv {

    private final String system;
    private final String interval;
    private final String version;

    public Siv(String system, String interval, String version) {
        this.system = system;
        this.interval = interval;
        this.version = version;
    }
    
    @Override public String toString() {
        return sivName(":");
    }
    
    public String name() {
        return sivName("-");
    }

    public Clause matches(StringColumn systemCol, StringColumn intervalCol, StringColumn versionCol) {
        return systemCol.is(system).and(intervalCol.is(interval).and(versionCol.is(version)));
    }

    private List<Pv> livePvs() {
        return LIVE.pvs(this);
    }
    
    public List<LiveSystem> liveSystems() {
        List<LiveSystem> result = empty();
        List<Pv> livePvs = livePvs();
        for (Pv pv : livePvs)
            result.add(new LiveSystem(this, pv));
        return result ;
    }

    public Cell<?> systemCell(StringColumn column) {
        return column.with(system);
    }
    
    public Cell<?> intervalCell(StringColumn column) {
        return column.with(interval);
    }
    
    public Cell<?> versionCell(StringColumn column) {
        return column.with(version);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((interval == null) ? 0 : interval.hashCode());
        result = prime * result + ((system == null) ? 0 : system.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Siv other = (Siv) obj;
        if (interval == null) {
            if (other.interval != null) return false;
        } else if (!interval.equals(other.interval)) return false;
        if (system == null) {
            if (other.system != null) return false;
        } else if (!system.equals(other.system)) return false;
        if (version == null) {
            if (other.version != null) return false;
        } else if (!version.equals(other.version)) return false;
        return true;
    }

    public String qClass() {
        return SYSTEM.qClass(system);
    }

    public Map<String, String> params(Pv pv) {
        return VALUES.params(system, pv);
    }

    public String system() {
        return system;
    }
    
    public String interval() { 
        return interval;
    }

    public String version() { 
        return version;
    }
    
    public String topicName() {
        return sviName(".");
    }

    public String sviName(String separator) {
        return join(separator, system, version, interval);
    }
    
    public String svimName(String separator, String m, String extra) {
    	return sviName(separator) + separator + m + extra;
    }
    
    public static Siv fromSivName(String name, String separator) {
        List<String> parts = split(separator, name);
        bombUnless(parts.size() == 3, "could not parse siv from " + name + " sep: " + separator);
        return new Siv(first(parts), second(parts), third(parts));
    }

    public String sivName(String separator) {
        return join(separator, system, interval, version);
    }

    public MsivTable.MsivRow with(String market) {
        return MSIVS.msiv(market, this);
    }
    
    public boolean hasMarket(String name) {
        return MSIVS.exists(this, name);
    }

    public List<Cell<String>> cells(NvarcharColumn systemCol, NvarcharColumn intervalCol, NvarcharColumn versionCol) {
        List<Cell<String>> result = empty();
        result.add(systemCol.with(system));
        result.add(intervalCol.with(interval));
        result.add(versionCol.with(version));
        return result;
    }
}
