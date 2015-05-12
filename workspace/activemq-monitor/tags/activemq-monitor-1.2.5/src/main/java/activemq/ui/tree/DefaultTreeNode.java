package activemq.ui.tree;

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class DefaultTreeNode extends DefaultMutableTreeNode {

    public DefaultTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    public boolean hasChildren() {
        return getChildCount() > 0;
    }
    
    @SuppressWarnings("unchecked")
    public int findChild(DefaultTreeNode childToFind, Comparator<? super DefaultTreeNode> c) {
        return Collections.binarySearch(children, childToFind, c);
    }
}
    