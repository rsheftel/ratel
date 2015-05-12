package activemq.ui;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DataStructure;
import org.apache.activemq.command.DestinationInfo;

import activemq.IJmsObserver;

import util.TopicViewNode;

@SuppressWarnings("serial")
public class TopicTreeModel extends DefaultTreeModel implements IJmsObserver<ActiveMQMessage> {

    public TopicTreeModel(TreeNode root) {
        super(root);
        // TODO Auto-generated constructor stub
    }

    public TopicTreeModel() {
        this(new TopicTreeNode(new TopicViewNode("Broker")));
        // TreeTopicNode rootNode = (TreeTopicNode)getRoot();
        // TopicViewNode leafNode1 = new TopicViewNode(new
        // TestTopicViewMBean(DATA1_FULL_NODE));
        // TopicViewNode leafNode2 = new TopicViewNode(new
        // TestTopicViewMBean(DATA2_FULL_NODE));
        // TopicViewNode leafNode3 = new TopicViewNode(new
        // TestTopicViewMBean(DATA3_FULL_NODE));
        //
        // rootNode.add(leafNode1);
        // rootNode.add(leafNode2);
        // rootNode.add(leafNode3);
    }

    public TopicTreeModel(List<TopicViewMBean> topicList) {
        super(new TopicTreeNode(new TopicViewNode("Broker")));

        TopicTreeNode rootNode = (TopicTreeNode) getRoot();

        for (TopicViewMBean topicNode : topicList) {
            rootNode.add(new TopicViewNode(topicNode));
        }
    }

    @Override
    public void onUpdate(ActiveMQMessage message) {
        // We are only interested in new destinations at this time.
        DataStructure dataStruct = message.getDataStructure();
        if (dataStruct instanceof DestinationInfo) {
            // TODO add the new topic to the tree
//            String messageTopicName = message.getDestination().getPhysicalName();
            DestinationInfo destInfo = (DestinationInfo)dataStruct;
            String newTopicName = destInfo.getDestination().getPhysicalName();
             
            synchronized (this) {
                JOptionPane.showMessageDialog(null,
                        "New Topic '"+ newTopicName+"'",
                        "New Topic",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            addTopic(null);
        }
        
    }
    
    private void addTopic(TopicViewMBean newTopicBean) {
        System.out.println("Adding topic: " + newTopicBean);
    }
}