package activemq.ui.tree;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

import org.testng.annotations.Test;

import util.TreeNodeTest;
import activemq.broker.BrokerConfiguration;

public class BrokerTreeTest extends TreeNodeTest {
    
    @Test(groups = ("unittest"))
    public void testEmptyrStringTopics() {
        MultiBrokerTreeModel mbtm = createTestBrokerConfiguration();

        Object root = mbtm.getRoot();
        assertNotNull(root, "Failed to create tree model with root");
        assertTrue(mbtm.getChildCount(root) == 2, "Wrong number of children");

        DefaultMutableTreeNode brokerNode = mbtm.getBrokerNode("TestBroker1");
        assertNotNull(brokerNode, "Failed to find test broker 1");

        DestinationTreeNode dtn = mbtm.addDestination("TestBroker1", new TestDestinationMBean(""));
        assertEquals(dtn.getBaseName(), "");

        
        HierarchicalTreeNode[] parents = dtn.createParentNodes();
        assertEquals(parents.length, 1);
    }
    
    
    @Test(groups = ("unittest"))
    public void testTreeTrailingDotTopics() {
        MultiBrokerTreeModel mbtm = createTestBrokerConfiguration();

        Object root = mbtm.getRoot();
        assertNotNull(root, "Failed to create tree model with root");
        assertTrue(mbtm.getChildCount(root) == 2, "Wrong number of children");

        DefaultMutableTreeNode brokerNode = mbtm.getBrokerNode("TestBroker1");
        assertNotNull(brokerNode, "Failed to find test broker 1");

        DestinationTreeNode dtn = mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2a.Level3a.Topic-2."));
        assertEquals(dtn.getBaseName(), "Level1.Level2a.Level3a");
        assertEquals(dtn.getName(), "Topic-2.");
        
        HierarchicalTreeNode[] parents = dtn.createParentNodes();
        assertEquals(parents.length, 3);
    }

    @Test(groups = ("unittest"))
    public void testTreeLeadingDotTopics() {
        MultiBrokerTreeModel mbtm = createTestBrokerConfiguration();

        Object root = mbtm.getRoot();
        assertNotNull(root, "Failed to create tree model with root");
        assertTrue(mbtm.getChildCount(root) == 2, "Wrong number of children");

        DefaultMutableTreeNode brokerNode = mbtm.getBrokerNode("TestBroker1");
        assertNotNull(brokerNode, "Failed to find test broker 1");

        DestinationTreeNode dtn = mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2a.Level3a..Topic-2"));
        assertEquals(dtn.getBaseName(), "Level1.Level2a.Level3a");
        assertEquals(dtn.getName(), ".Topic-2");
        
        HierarchicalTreeNode[] parents = dtn.createParentNodes();
        assertEquals(parents.length, 3);
    }

    @Test(groups = ("unittest"))
    public void testHierarchicalNode() {
        HierarchicalTreeNode root = new HierarchicalTreeNode(LEVEL1_NODE);
        assertTrue(root.getPathLevel() == 1, "Failed to parse correct level");
        assertTrue(root.getDepth() == 0, "Incorrect depth");

        HierarchicalTreeNode htn1 = new HierarchicalTreeNode(DATA1_FULL_NODE);
        assertTrue(htn1.getPathLevel() == 3, "Failed to parse correct level");
        HierarchicalTreeNode htn2 = new HierarchicalTreeNode(DATA2_FULL_NODE);
        assertTrue(htn2.getPathLevel() == 3, "Failed to parse correct level");
        HierarchicalTreeNode htn3 = new HierarchicalTreeNode(DATA3_FULL_NODE);
        assertTrue(htn3.getPathLevel() == 3, "Failed to parse correct level");

        root.add(htn1);
        assertEquals(root.getDepth(), 1, "Incorrect depth after adding first child");

        root.add(htn2);
        assertEquals(root.getDepth(), 1, "Incorrect depth after adding second child");

        root.add(htn3);
        assertEquals(root.getDepth(), 1, "Incorrect depth after adding third child");

        Enumeration<?> depthFirst = root.depthFirstEnumeration();

        while (depthFirst.hasMoreElements()) {
            HierarchicalTreeNode cn = (HierarchicalTreeNode) depthFirst.nextElement();
            System.out.println(cn);
        }
    }
    
    @Test(groups = ("unittest"))
    public void testDestinationNode() {
        
        DestinationTreeNode dtn1 = new DestinationTreeNode("TestBroker", new TestDestinationMBean(DATA1_FULL_NODE));
        HierarchicalTreeNode[] parents1 = dtn1.createParentNodes();
        assertEquals(parents1.length, 2, "Wrong number of parents");
        
        DestinationTreeNode dtn2 = new DestinationTreeNode("TestBroker", new TestDestinationMBean(DATA2_FULL_NODE));
        HierarchicalTreeNode[] parents2 = dtn2.createParentNodes();
        assertEquals(parents2.length, 2, "Wrong number of parents");

        DestinationTreeNode dtn3 = new DestinationTreeNode("TestBroker", new TestDestinationMBean(DATA3_FULL_NODE));
        HierarchicalTreeNode[] parents3 = dtn3.createParentNodes();
        assertEquals(parents3.length, 2, "Wrong number of parents");

        // The add with array does not validate, it blinding adds, we must specify which 
        // nodes in the array to add
        HierarchicalTreeNode root = new HierarchicalTreeNode(LEVEL1_NODE);
        HierarchicalTreeNode lastNodeAdded = root.add(parents1, 1);
        lastNodeAdded.add(dtn1);
        assertEquals(root.getDepth(), 2, "Incorrect depth after adding first child");

        lastNodeAdded = root.add(parents2, 2);
        lastNodeAdded.add(dtn2);
        assertEquals(root.getDepth(), 2, "Incorrect depth after adding second child");

        lastNodeAdded = root.add(parents3, 1);
        lastNodeAdded.add(dtn3);
        printNodes(root);

        assertEquals(root.getDepth(), 2, "Incorrect depth after adding third child");
    }

    private void printNodes(HierarchicalTreeNode root) {
        Enumeration<?> depthFirst = root.depthFirstEnumeration();

        while (depthFirst.hasMoreElements()) {
            DefaultTreeNode cn = (DefaultTreeNode) depthFirst.nextElement();
            System.out.println(cn);
        }
    }

    @Test(groups = ("unittest"))
    public void testCreateTreeModel() {
        // create a tree passing in the broker configuration and list of DestinationViewMBean
        // A broker tree know how to create a view from the model

        MultiBrokerTreeModel mbtm = createTestBrokerConfiguration();

        Object root = mbtm.getRoot();
        assertNotNull(root, "Failed to create tree model with root");
        assertTrue(mbtm.getChildCount(root) == 2, "Wrong number of children");

        DefaultMutableTreeNode brokerNode = mbtm.getBrokerNode("TestBroker1");
        assertNotNull(brokerNode, "Failed to find test broker 1");

        mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2a.Level3a.Topic-2"));
        mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2a.Level3a.Topic-1"));
        mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2b.Topic-3"));
        mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2b.Level3b.Topic-4"));

    }

    private static MultiBrokerTreeModel createTestBrokerConfiguration() {
        BrokerConfiguration broker1 = new BrokerConfiguration("TestBroker1", 61616, 1099);
        BrokerConfiguration broker2 = new BrokerConfiguration("TestBroker2", 61600, 11099);

        List<BrokerConfiguration> brokers = new ArrayList<BrokerConfiguration>();
        brokers.add(broker1);
        brokers.add(broker2);

        //        List<DestinationViewMBean> destBeans = createTestDestinationBeans();
        //        BrokerTreeNode bt = new BrokerTreeNode(brokerConfig, destBeans);

        MultiBrokerTreeModel mbtm = new MultiBrokerTreeModel(brokers);
        return mbtm;
    }

    private static boolean useSystemLookAndFeel = true;

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        //Create and set up the window.
        JFrame frame = new JFrame("TreeDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        MultiBrokerTreeModel mbtm = createTestBrokerConfiguration();
        mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2a.Level3a.Topic-2"));
        mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2a.Level3a.Topic-1"));
        mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2b.Topic-3"));
        mbtm.addDestination("TestBroker1", new TestDestinationMBean("Level1.Level2b.Level3b.Topic-4"));

        JTree tree = new JTree(mbtm);


        //tree.setRootVisible(false);
        frame.add(tree);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}
