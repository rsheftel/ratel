package systemdb.portfolio;

import static systemdb.portfolio.Groups.*;

import java.util.*;

import systemdb.metadata.*;
import util.*;

public class GroupNode extends Group {

    private final String group;
    public GroupNode(String group, double weight) {
        super(weight);
        this.group = group;
        
    }

    @Override public void addWeights(Map<MsivPv, Double> weights, double scale) {
        List<Group> members = GROUPS.members(group);
        for (Group member : members)
            member.addWeights(weights, scale * weight);
    }

    @Override public String name() {
        return group;
    }

    @Override public void addTo(Tag parent) {
        Tag node = parent.add("group");
        node.add("name", group);
        node.add("weight", String.valueOf(weight()));
        List<Group> members = GROUPS.members(group);
        for (Group member : members)
            member.addTo(node);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final GroupNode other = (GroupNode) obj;
        if (group == null) {
            if (other.group != null) return false;
        } else if (!group.equals(other.group)) return false;
        return true;
    }

    
    

}
