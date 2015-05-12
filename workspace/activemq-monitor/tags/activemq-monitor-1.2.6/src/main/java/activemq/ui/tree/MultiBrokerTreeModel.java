package activemq.ui.tree;

import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.activemq.broker.jmx.DestinationViewMBean;

import util.IObserver;
import activemq.broker.BrokerConfiguration;

/**
 * Represent a tree that has multiple brokers.
 * 
 */
@SuppressWarnings("serial")
public class MultiBrokerTreeModel extends DefaultTreeModel {

    public MultiBrokerTreeModel(TreeNode root) {
        super(root);
        // TODO Auto-generated constructor stub
    }

    public MultiBrokerTreeModel() {
        super(new DefaultMutableTreeNode(), true);
    }

    public MultiBrokerTreeModel(List<BrokerConfiguration> brokers) {
        // create a dumby rootnode and then add the brokers as children
        super(new DefaultMutableTreeNode(), true);
        
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();

        for (BrokerConfiguration broker : brokers) {
            rootNode.add(new BrokerTreeNode(broker));
        }
    }

    public DestinationTreeNode addDestination(String brokerName, DestinationViewMBean newDestination) {
        BrokerTreeNode brokerNode = getBrokerNode(brokerName);
      
        DestinationTreeNode newNode = brokerNode.add(newDestination);
        
        reload(newNode.getParent());
        
        return newNode;
    }
   
    public void addDestinations(List<DestinationViewMBean> destList, IObserver<Integer> observer, BrokerConfiguration brokerConfig) {
        BrokerTreeNode brokerNode = getBrokerNode(brokerConfig.getServerName());
        
        if (brokerNode == null) {
            brokerNode = new BrokerTreeNode(brokerConfig);
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
            rootNode.add(brokerNode);
        }
        
        float count = 0;
        float max = destList.size();
        for (DestinationViewMBean destNode : destList) {
            brokerNode.add(destNode);
            if (observer != null) {
                count++;
                observer.onUpdate( (int)(count / max * 100f));
            }
        }
        reload();
    }

    /**
     * Find and return the broker node that has the matching server name.
     * 
     * @param brokerName
     * @return
     */
    @SuppressWarnings("unchecked")
    public BrokerTreeNode getBrokerNode(String brokerName) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
        Enumeration<BrokerTreeNode> brokers = rootNode.children();

        while (brokers.hasMoreElements()) {
            BrokerTreeNode child = brokers.nextElement();
            BrokerConfiguration broker = (BrokerConfiguration) child.getUserObject();
            if (brokerName.equals(broker.getServerName())) {
                return child;
            }
        }

        return null;
    }
   
    public BrokerTreeNode removeBrokerNode(String brokerName) {
        BrokerTreeNode brokerNode = getBrokerNode(brokerName);
        if (brokerNode != null) {
            removeNodeFromParent(brokerNode);
        }
        
        return brokerNode;
    }

    public void addBroker(BrokerTreeNode brokerNode) {
        BrokerConfiguration brokerConfig = (BrokerConfiguration)brokerNode.getUserObject();
        BrokerTreeNode oldBrokerNode = getBrokerNode(brokerConfig.getServerName());
        if (oldBrokerNode != null) {
            removeNodeFromParent(oldBrokerNode);
        } 
        
        // insert these in sorted order
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
        // find where to insert this node
        Enumeration<?> children = rootNode.children();
        int newChildIndex = 0;
        while (children.hasMoreElements()) {
            BrokerTreeNode btn = (BrokerTreeNode) children.nextElement();
            int comparedTo = btn.compareTo(brokerNode);
            if (comparedTo > 0) {
                break;
            }
            newChildIndex++;
        }
        
        rootNode.insert(brokerNode, newChildIndex);
        reload(brokerNode.getParent());
    }
}