package activemq.ui.tree;

import java.util.Collections;

import javax.swing.tree.MutableTreeNode;

import org.apache.activemq.broker.jmx.DestinationViewMBean;

import activemq.broker.BrokerConfiguration;

/**
 * Represent a broker node.
 * 
 * There are only two possible children types, HierarchicalTreeNode and DestinationTreeNode.
 * 
 */
@SuppressWarnings("serial")
public class BrokerTreeNode extends DefaultTreeNode implements Comparable<BrokerTreeNode> {

    private String brokerName;

    public BrokerTreeNode(BrokerConfiguration brokerConfig) {
        super(brokerConfig, true);
        this.brokerName = brokerConfig.getServerName();
    }

    public String getBrokerName() {
        return brokerName;
    }

    /**
     * Add the destination to this broker node.
     * 
     * @param node
     */
    public DestinationTreeNode add(DestinationViewMBean nodeBean) {
        // create the actual leaf node for this bean
        DestinationTreeNode nodeToAdd = new DestinationTreeNode(brokerName, nodeBean);

        // create all of the connectors between the broker and the leaf
        HierarchicalTreeNode[] ancestors = nodeToAdd.createParentNodes();

        DefaultTreeNode currentNode = this;
        // starting with the first ancestor, find the existing child that matches
        TreeNodeComparator myComparator = new TreeNodeComparator();
        int i = 0;
        while (currentNode.hasChildren() && i < ancestors.length) {
            HierarchicalTreeNode childToFind = ancestors[i];
            int bsResult = currentNode.findChild(childToFind, myComparator);
            if (bsResult >= 0) {
                // we have a match, move to the next level
                currentNode = (DefaultTreeNode) currentNode.getChildAt(bsResult);
                i++;
            } else {
                // no match, add the ancestors and the new node
                DefaultTreeNode newNode = null;
                for (int j = i; j < ancestors.length; j++) {
                    newNode = ancestors[j];
                    currentNode.add(newNode);
                    currentNode = newNode;
                }
                // Add the leaf
                currentNode.add(nodeToAdd);
                return nodeToAdd;
            }

        }
        // We ran out of children (or didn't have any) add the ancestors and the new node
        DefaultTreeNode newNode = null;
        for (int j = i; j < ancestors.length; j++) {
            newNode = ancestors[j];
            currentNode.add(newNode);
            currentNode = newNode;
        }
        // Add the leaf
        currentNode.add(nodeToAdd);
        return nodeToAdd;
    }

    @Override
    public void add(MutableTreeNode newChild) {
        if (newChild instanceof DefaultTreeNode) {
            add((DefaultTreeNode) newChild);
        } else {
            super.add(newChild);
        }
    }

    @SuppressWarnings("unchecked")
    public void add(DefaultTreeNode newChild) {
        if (children == null) {
            insert(newChild, 0);
            return;
        }
        int bsResult = Collections.binarySearch(children, newChild, new TreeNodeComparator());
        int tmpInsert = (-bsResult) - 1;
        insert(newChild, tmpInsert);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BrokerTreeNode)) {
            return false;
        }

        BrokerTreeNode other = (BrokerTreeNode) obj;
        return getUserObject().equals(other.getUserObject());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 17 * getUserObject().hashCode();

    }

    @Override
    public int compareTo(BrokerTreeNode o) {
        BrokerTreeNode btn = (BrokerTreeNode) o;

        BrokerConfiguration obc = (BrokerConfiguration) btn.getUserObject();
        BrokerConfiguration bc = (BrokerConfiguration) getUserObject();

        return bc.getServerName().compareTo(obc.getServerName());
    }

}
