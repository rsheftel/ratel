package activemq.ui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import activemq.ui.tree.DefaultTreeNode;
import activemq.ui.tree.DestinationTreeNode;


/**
 * Custom renderer for TreeNodes.
 * 
 * <b>Do NOT forget, we are a <code>JLabel</code></b>
 */
@SuppressWarnings("serial")
public class TopicNodeRenderer extends DefaultTreeCellRenderer {

    public TopicNodeRenderer() {
        super();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultTreeNode treenode = (DefaultTreeNode)value;
        DestinationTreeNode viewNode = (DestinationTreeNode)treenode.getUserObject();
        
        setText(viewNode.getName());
        
        return this;
    }

}
