package activemq.ui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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


    @Test(groups = { "unittest" }) 
    public void createTopicTreeSorted() {
        TopicTreeNode rootNode = new TopicTreeNode(new TopicViewNode("A"));

        assert 0 == rootNode.getDepth() : "Incorrect depth for root node";

        TopicViewNode leafNode1 = new TopicViewNode(new TestTopicViewMBean("A.A"));
        TopicViewNode leafNode2 = new TopicViewNode(new TestTopicViewMBean("A.Z"));
        TopicViewNode leafNode3 = new TopicViewNode(new TestTopicViewMBean("A.B"));

        rootNode.add(leafNode1);
        rootNode.add(leafNode2);
        rootNode.add(leafNode3);
        
        assert 1 == rootNode.getDepth() : "Wrong depth";
        
        List<?> children = enumerationToList(rootNode.children());
        
        TopicTreeNode zero = (TopicTreeNode)children.get(0);
        TopicTreeNode one = (TopicTreeNode)children.get(1);
        TopicTreeNode two = (TopicTreeNode)children.get(2);
        
        assert "A".equals(zero.getUserObject().getName()) : "First item incorrect";
        assert "B".equals(one.getUserObject().getName()) : "Second item incorrect";
        assert "Z".equals(two.getUserObject().getName()) : "Third item incorrect";
    }
    
    
    private List<?> enumerationToList(Enumeration<?> items) {
        
        List<Object> list = new ArrayList<Object>();
        
        while (items.hasMoreElements()) {
            list.add(items.nextElement());
        }
        return list;
    }
}
