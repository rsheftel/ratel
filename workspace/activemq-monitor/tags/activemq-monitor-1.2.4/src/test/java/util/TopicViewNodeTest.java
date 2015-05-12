package util;

import org.testng.annotations.Test;


public class TopicViewNodeTest extends TreeNodeTest {

    private static final String DATA1_NODE = "Data1";


    @Test(groups = { "unittest" }) 
    public void createTopicViewNode() {
        TopicViewNode branchNode = new TopicViewNode(BROKER_NODE);

        assert BROKER_NODE.equals(branchNode.getName()) : "Failed to create root node";
        assert branchNode.isBranch() : "Should be branch node";

        try {
            branchNode.getHierarchicalName();
            assert true : "Failed ";
        } catch (NullPointerException e) {
            // expected
        }

        TopicViewNode leafNode = new TopicViewNode(new TestTopicViewMBean(DATA1_FULL_NODE));

        assert DATA1_NODE.equals(leafNode.getName()): "Failed to parse name";
        assert DATA1_FULL_NODE.equals(leafNode.getHierarchicalName()) : "Failed to get hierarchical name";
        assert DATA1_BASE_NAME.equals(leafNode.getBaseName()) : "Failed to get base name";
        assert leafNode.isLeaf() : "Should be leaf node";

        ITopicView[] ancestors = leafNode.createParentNodes();

        assert ancestors.length == 2 : "Incorrect ancestor count";
        assert LEVEL1_NODE.equals(ancestors[0].getName()) : "First node is not Level1";
    }

    @Test(groups = { "unittest" }) 
    public void determineHierarchy() {
        TopicViewNode leafNode1 = new TopicViewNode(new TestTopicViewMBean(DATA1_FULL_NODE));
        TopicViewNode leafNode2 = new TopicViewNode(new TestTopicViewMBean(DATA2_FULL_NODE));

        ITopicView[] ancestors1 = leafNode1.createParentNodes();
        ITopicView[] ancestors2 = leafNode2.createParentNodes();

        assert ancestors1[0].equals(ancestors2[0]) : "Nodes have different ancestors level 0";
        assert ancestors1[1].equals(ancestors2[1]) : "Nodes have different ancestors level 1";
        //assert ancestors1[2].equals(ancestors2[2]) : "Nodes have different ancestors level 2";

        TopicViewNode leafNode3 = new TopicViewNode(new TestTopicViewMBean(DATA3_FULL_NODE));
        ITopicView[] ancestors3 = leafNode3.createParentNodes();

        assert ancestors1[0].equals(ancestors3[0]) : "Nodes have different ancestors level 0";
        assert !ancestors1[1].equals(ancestors3[1]) : "Nodes have have ancestors level 1";
        //assert !ancestors1[2].equals(ancestors3[2]) : "Nodes have same ancestors level 2";
    }
    
    @Test(groups = { "unittest" }) 
    public void createAncestors() {
        TopicViewNode leafNode1 = new TopicViewNode(new TestTopicViewMBean(DATA1_FULL_NODE));

        ITopicView[] ancestors1 = leafNode1.createParentNodes();

        assert DATA1_BASE_NAME.equals(ancestors1[1].getHierarchicalName()) : "Parent and child have wrong base names";
    }
}

