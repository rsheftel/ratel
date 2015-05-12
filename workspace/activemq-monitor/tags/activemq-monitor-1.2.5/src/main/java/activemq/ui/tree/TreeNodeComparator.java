package activemq.ui.tree;

import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;


public class TreeNodeComparator implements Comparator<DefaultMutableTreeNode> {

    @Override
    public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
        // compare Destination and Hierarchical nodes
        // hierarchical are always before destination
        if (o1 instanceof HierarchicalTreeNode && o2 instanceof DestinationTreeNode) {
            return -1;
        } else if (o1 instanceof DestinationTreeNode && o2 instanceof HierarchicalTreeNode) {
            return 1;
        } else if (o1 instanceof DestinationTreeNode && o2 instanceof DestinationTreeNode) {
            return ((DestinationTreeNode)o1).compareTo((DestinationTreeNode)o2); 
        } else {
            String str1 = (String)o1.getUserObject();
            String str2 = (String)o2.getUserObject();
            
            return str1.compareTo(str2); 
        }
    }

}
