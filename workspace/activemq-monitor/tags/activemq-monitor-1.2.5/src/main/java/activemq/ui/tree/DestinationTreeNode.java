package activemq.ui.tree;

import java.util.ArrayList;
import java.util.List;

import org.apache.activemq.broker.jmx.DestinationViewMBean;


/**
 * Represent a JMS destination.
 * 
 */
@SuppressWarnings("serial")
public class DestinationTreeNode extends DefaultTreeNode implements Comparable<DestinationTreeNode> {

    private String brokerName;
    
    private String name;

    private String baseName = "";

    public DestinationTreeNode(String brokerName, DestinationViewMBean leaf) {
        super(leaf, false);
        this.brokerName = brokerName;
        setNames(leaf.getName());
        setAllowsChildren(false);
    }

    /**
     * parse the name to determine the path and name.
     * 
     * @param qualifiedName
     */
    private void setNames(String qualifiedName) {
        String[] names = qualifiedName.split("\\.");
        this.name = names[names.length - 1];

        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot != -1) {
            this.baseName = qualifiedName.substring(0, lastDot);
        } else {
            baseName = "";
        }
    }

    /**
     * Create all of the parents for this node.
     */
    public HierarchicalTreeNode[] createParentNodes() {
        String baseName = getHierarchicalName();

        int dotPos = baseName.indexOf('.');
        List<HierarchicalTreeNode> parentList = new ArrayList<HierarchicalTreeNode>();

        while (dotPos != -1) {
            parentList.add(new HierarchicalTreeNode(baseName.substring(0, dotPos)));
            dotPos = baseName.indexOf('.', dotPos + 1);
        }
        return parentList.toArray(new HierarchicalTreeNode[parentList.size()]);
    }

    public String getName() {
        return name;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getHierarchicalName() {
        if (baseName != null && baseName.length() > 0) {
            return baseName + "." + getName();
        } else {
            return getName();
        }
    }

    @Override
    public String toString() {
        return "baseName=" + getBaseName() + ", name=" + getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DestinationTreeNode)) {
            return false;
        }

        DestinationTreeNode other = (DestinationTreeNode) obj;
        return getUserObject().equals(other.getUserObject());
    }

    
    @Override
    public int hashCode() {
        return getBaseName().hashCode() + (17 * getName().hashCode());
    }

    @Override
    public int compareTo(DestinationTreeNode other) {
        if (other == this) {
            return 0;
        }
        
        // We are only interested in the hierarchical names for comparison
        return getHierarchicalName().compareTo(other.getHierarchicalName());
    }

    public String getBrokerName() {
        return brokerName;
    }
}

