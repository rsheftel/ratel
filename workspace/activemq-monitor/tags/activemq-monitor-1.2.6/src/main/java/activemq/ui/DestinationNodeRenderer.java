package activemq.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import activemq.broker.BrokerConfiguration;
import activemq.ui.tree.BrokerTreeNode;
import activemq.ui.tree.DestinationTreeNode;
import activemq.ui.tree.HierarchicalTreeNode;

/**
 * Custom renderer for TreeNodes.
 * 
 * <b>Do NOT forget, we are a <code>JLabel</code></b>
 */
@SuppressWarnings("serial")
public class DestinationNodeRenderer extends DefaultTreeCellRenderer {

    private Font normalFont;
    public DestinationNodeRenderer() {
        super();
        normalFont = getFont();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        // Initialize ourselves this == parentRenderer
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        if (value instanceof BrokerTreeNode) {
            BrokerTreeNode broker = (BrokerTreeNode) value;
            BrokerConfiguration brokerConfig = (BrokerConfiguration)broker.getUserObject(); 
            setText(brokerConfig.getServerName());
            setFont(normalFont);
        } else if (value instanceof HierarchicalTreeNode) {
            HierarchicalTreeNode node = (HierarchicalTreeNode) value;
            setText(node.getPathNode());
            setFont(normalFont);
        } else if (value instanceof DestinationTreeNode) {
            DestinationTreeNode destNode = (DestinationTreeNode)value;
            setText(destNode.getName());
            setFont(normalFont);
        } else if (value instanceof DefaultMutableTreeNode) {
            setIcon(null);
            setText("Brokers");
            Font textFont = getFont();
            setFont(new Font(textFont.getName(), textFont.getStyle(), 13));
        }
        return this;
    }

}
