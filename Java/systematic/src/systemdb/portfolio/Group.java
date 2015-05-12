package systemdb.portfolio;

import static systemdb.portfolio.GroupLeafs.*;
import static systemdb.portfolio.Groups.*;
import static util.Errors.*;
import static util.Strings.*;

import java.util.*;

import systemdb.metadata.*;
import util.*;
import db.*;

public abstract class Group {

    protected final double weight;
    
    public Group(double weight) {
        this.weight = weight;
    }
    public static Group group(Row row) {
        if (GROUPS.owns(row)) return GROUPS.group(row);
        else if (LEAFS.owns(row)) return LEAFS.group(row);
        else throw bomb("eek");
    }

    
    public abstract void addWeights(Map<MsivPv, Double> weights, double scale);
    public abstract String name();

    public Map<MsivPv, Double> weighting() {
        return GROUPS.weighting(this);
    }
    
    public double weight() {
        return weight;
    }
    public List<Group> members() {
        return GROUPS.members(name());
    }
    
    @Override public String toString() {
        return paren(commaSep(name(), "" + weight));
    }
    public abstract void addTo(Tag parent);

    public static Tag asXml(Group group) {
        Tag result = Tag.tag("root");
        group.addTo(result);
        return result;
    }
    
}
