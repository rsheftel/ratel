package activemq.ui;

import java.util.Enumeration;

import org.testng.annotations.Test;

import util.TestTopicViewMBean;
import util.TopicViewNode;
import util.TreeNodeTest;


/**
 * Test the creation of a UI Tree
 * 
 */
public class TopicTreeNodeTest extends TreeNodeTest {

    @Test(groups = { "unittest" }) 
    public void createTopicTree() {

        TopicTreeNode rootNode = new TopicTreeNode(new TopicViewNode(BROKER_NODE));

        assert 0 == rootNode.getDepth() : "Incorrect depth for root node";

        TopicViewNode leafNode1 = new TopicViewNode(new TestTopicViewMBean(DATA1_FULL_NODE));
        TopicViewNode leafNode2 = new TopicViewNode(new TestTopicViewMBean(DATA2_FULL_NODE));
        TopicViewNode leafNode3 = new TopicViewNode(new TestTopicViewMBean(DATA3_FULL_NODE));

        rootNode.add(leafNode1);
        assert 3 == rootNode.getDepth() : "Depth wrong after adding leaf";
        rootNode.add(leafNode2);
        assert 3 == rootNode.getDepth() : "Depth changed after adding sibling";
        rootNode.add(leafNode3);
        assert 3 == rootNode.getDepth() : "Depth changed after adding sibling";

        Enumeration<?> depthFirst = rootNode.depthFirstEnumeration();
        
        while (depthFirst.hasMoreElements()) {
            TopicTreeNode cn = (TopicTreeNode) depthFirst.nextElement();
            System.out.println(cn);
        }
        
    }

    /*
    @Test(groups = { "unittest" }) 
    public void createTopicTree_old() {
        TopicViewNode rootNode = new TopicViewNode(BROKER_NODE);
        TopicViewNode leafNode1 = new TopicViewNode(new TestTopicViewMBean(DATA1_FULL_NODE));
        TopicViewNode leafNode2 = new TopicViewNode(new TestTopicViewMBean(DATA2_FULL_NODE));

        ITopicView[] ancestors1 = leafNode1.createParentNodes();
        ITopicView[] ancestors2 = leafNode2.createParentNodes();

        // create the tree nodes for the 3 nodes
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootNode,true);

        DefaultMutableTreeNode currentNode = root;
        for (int i =0; i < ancestors1.length; i++) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(ancestors1[i]);
            currentNode.add(newNode);
            currentNode = newNode;
        }
        currentNode.add(new DefaultMutableTreeNode(leafNode1));

        assert 4 == root.getDepth() : "Wrong depth of tree"; 

        // Add the next leaf node
        // Find the matching parent
        boolean found = false;
        DefaultMutableTreeNode newNode = null;
        for (int i = ancestors2.length-1; i >=0 & !found ; i--) {
            Enumeration<?> searcher = root.postorderEnumeration();
            while (searcher.hasMoreElements() && !found) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) searcher.nextElement();
                if (node.getAllowsChildren() && node.getUserObject().equals(ancestors2[i])) {
                    // Found the highest level match.  Add any branches and then the leaf
                    found = true;
                    System.out.println("Match! " + node +" <-> " + ancestors2[i]);

                    for (int j = i +1; j < ancestors2.length; j++) {
                        newNode = new DefaultMutableTreeNode(ancestors2[j]);
                        node.add(newNode);
                        node = newNode;
                    }
                    // Add the leaf
                    node.add(new DefaultMutableTreeNode(leafNode2));
                }
            }
        }

        assert found : "Failed to add node";
        assert 3 == root.getDepth() : "Tree has wrong depth";
        assert 2 == newNode.getSiblingCount() : "Wrong number of siblings";
    }
     */    
}
