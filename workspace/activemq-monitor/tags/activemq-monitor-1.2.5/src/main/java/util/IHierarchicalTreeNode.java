package util;


public interface IHierarchicalTreeNode {

    boolean isBranch();
    
    boolean isLeaf();
    
    String getName();
    
    String getBaseName();
    
    String getHierarchicalName();
    
    /** 
     * If this is a leaf node create the parents that would below to this node
     * 
     * @return
     */
    IHierarchicalTreeNode[] createParentNodes();
    
    
}
