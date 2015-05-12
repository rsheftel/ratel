package activemq.ui;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import util.ITopicView;
import util.TopicViewNode;

@SuppressWarnings("serial")
public class TopicTreeNode extends DefaultMutableTreeNode {

    public TopicTreeNode(ITopicView node) {
        super(node, node.isBranch());
    }

    public void add(TopicViewNode nodeToAdd) {
        TopicTreeNode root = (TopicTreeNode)getRoot();

        ITopicView[] ancestors = nodeToAdd.createParentNodes();
        // Find the matching parent
        boolean found = false;
        TopicTreeNode newNode = null;
        for (int i = ancestors.length-1; i >=0 & !found ; i--) {
            Enumeration<?> searcher = root.postorderEnumeration();
            while (searcher.hasMoreElements() && !found) {
                TopicTreeNode node = (TopicTreeNode) searcher.nextElement();
                if (node.getAllowsChildren() && node.getUserObject().equals(ancestors[i])) {
                    // Found the highest level match.  Add any branches and then the leaf
                    found = true;

                    for (int j = i +1; j < ancestors.length; j++) {
                        newNode = new TopicTreeNode(ancestors[j]);
                        node.add(newNode);
                        node = newNode;
                    }
                    // Add the leaf
                    node.add(new TopicTreeNode(nodeToAdd));
                }
            }
        }
        if (!found) {
            // add all the children to the root node.
            TopicTreeNode node = root;
            for (int i = 0; i < ancestors.length; i++) {
                newNode = new TopicTreeNode(ancestors[i]);
                node.add(newNode);
                node = newNode;
            }
            // Add the leaf
            node.add(new TopicTreeNode(nodeToAdd));
        }
    }
}