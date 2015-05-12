package systemdb.portfolio;

import java.util.*;

import systemdb.metadata.*;
import util.*;

public class GroupLeaf extends Group {

    private final MsivPv msivpv;
    public GroupLeaf(MsivPv msivpv, double weight) {
        super(weight);
        this.msivpv = msivpv;
    }

    @Override public void addWeights(Map<MsivPv, Double> weights, double scale) {
        double existing = 0.0;
        if(weights.containsKey(msivpv)) existing  = weights.get(msivpv);
        weights.put(msivpv, existing + scale * weight);
    }

    @Override public String name() {
        return msivpv.name();
    }

    @Override public void addTo(Tag parent) {
        Tag node = parent.add("leaf");
        node.add("market", msivpv.market());
        node.add("siv", msivpv.siv().sivName("_"));
        node.add("pv", msivpv.pv().name());
        node.add("weight", String.valueOf(weight()));
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((msivpv == null) ? 0 : msivpv.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final GroupLeaf other = (GroupLeaf) obj;
        if (msivpv == null) {
            if (other.msivpv != null) return false;
        } else if (!msivpv.equals(other.msivpv)) return false;
        return true;
    }
    
    

}
