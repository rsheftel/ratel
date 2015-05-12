package activemq.ui.tree;

import java.util.Collections;

import javax.swing.tree.MutableTreeNode;


/**
 * Represent a path between a broker node and a destination node.
 * 
 */
@SuppressWarnings("serial")
public class HierarchicalTreeNode extends DefaultTreeNode {

    private String pathNode;
    private int pathLevel;
    

    public HierarchicalTreeNode(String path) {
        super(path, true);
        String[] parts = path.split("\\.");
        
        pathNode = parts[parts.length-1];
        
        pathLevel = parts.length;
    }

    /**
     * @return the pathNode
     */
    public String getPathNode() {
        return pathNode;
    }

    /**
     * @param pathNode the pathNode to set
     */
    public void setPathNode(String pathNode) {
        this.pathNode = pathNode;
    }

    @Override
    public void add(MutableTreeNode newChild) {
        if (newChild instanceof DefaultTreeNode) {
            add((DefaultTreeNode) newChild);
        } else {
            super.add(newChild);
        }
    }

    @SuppressWarnings("unchecked")
    public void add(DefaultTreeNode newChild) {
        if (children == null) {
            insert(newChild, 0);
            return;
        }
        
        int bsResult = Collections.binarySearch(children, newChild, new TreeNodeComparator());
        int tmpInsert = (-bsResult)-1;
        insert(newChild, tmpInsert);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HierarchicalTreeNode)) {
            return false;
        }

        // cast to native object is now safe
        HierarchicalTreeNode other = (HierarchicalTreeNode) obj;

        // The UserObject is a string
        return getUserObject().equals(other.getUserObject());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 17 * getUserObject().hashCode();
    }

    public int getPathLevel() {
        return pathLevel;
    }

    
    public HierarchicalTreeNode add(HierarchicalTreeNode[] parents) {
        return add(parents, 0);
    }
    
    public HierarchicalTreeNode add(HierarchicalTreeNode[] parents, int first) {
        // let the called deal with the null array
        return add(parents, first, parents == null ? 0 : parents.length);
    }
    
    public HierarchicalTreeNode add(HierarchicalTreeNode[] parents, int first, int last) {
        if (parents == null) {
            return this;
        }
        
        HierarchicalTreeNode currentNode = this;
        
        for (int i= first; i < last; i++) {
            HierarchicalTreeNode newNode = parents[i];
            currentNode.add(newNode);
            currentNode = newNode;
        }
        return currentNode;
    }

}
