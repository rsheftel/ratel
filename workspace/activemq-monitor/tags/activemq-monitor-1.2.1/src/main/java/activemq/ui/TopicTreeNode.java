package activemq.ui;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import util.ITopicView;
import util.TopicViewNode;

@SuppressWarnings("serial")
public class TopicTreeNode extends DefaultMutableTreeNode {

    public TopicTreeNode(ITopicView node) {
        super(node, node.isBranch());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(MutableTreeNode newChild) {
        TopicTreeNode newTopicChild = (TopicTreeNode) newChild;

        // We want to add these in sorted order
        ITopicView newChildUserObject = newTopicChild.getUserObject();
        Enumeration<TopicTreeNode> children = children();

        int insertAtIndex = 0;
        int currentIndex = 0;
        while (children.hasMoreElements()) {
            currentIndex++;
            TopicTreeNode sibling = (TopicTreeNode) children.nextElement();
            ITopicView userObject = sibling.getUserObject();

            if (userObject.getName().compareTo(newChildUserObject.getName()) < 0) {
                insertAtIndex = currentIndex;
            } else {
                continue;
            }
        }

        // We know where to insert, now do it
        insert(newChild, insertAtIndex);
    }

    @Override
    public boolean isLeaf() {
        return getUserObject().isLeaf();
    }

    @SuppressWarnings("unchecked")
    public TopicTreeNode add(TopicViewNode nodeToAdd) {
        TopicTreeNode root = (TopicTreeNode) getRoot();

        TopicTreeNode addedNode = null;
        
        ITopicView[] ancestors = nodeToAdd.createParentNodes();
        // Find the matching parent
        boolean found = false;
        TopicTreeNode newNode = null;
        for (int i = ancestors.length - 1; i >= 0 & !found; i--) {
            Enumeration<?> searcher = root.postorderEnumeration();
            while (searcher.hasMoreElements() && !found) {
                TopicTreeNode node = (TopicTreeNode) searcher.nextElement();
                if (node.getAllowsChildren() && node.getUserObject().equals(ancestors[i])) {
                    // Found the highest level match. Add any branches and then the leaf
                    found = true;

                    for (int j = i + 1; j < ancestors.length; j++) {
                        newNode = new TopicTreeNode(ancestors[j]);
                        node.add(newNode);
                        node = newNode;
                    }
                    // Add the leaf
                    addedNode = new TopicTreeNode(nodeToAdd);
                    node.add(addedNode);
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
            addedNode = new TopicTreeNode(nodeToAdd);
            node.add(addedNode);
        }
        
        return addedNode;
    }

    public String getHierarchicalName() {
        return getUserObject().getHierarchicalName();
    }

    @SuppressWarnings("unchecked")
    public ITopicView getUserObject() {
        return (ITopicView) super.getUserObject();
    }

}