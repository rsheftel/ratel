package activemq.ui;

import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.activemq.broker.jmx.TopicViewMBean;

import util.TopicViewNode;

@SuppressWarnings("serial")
public class TopicTreeModel extends DefaultTreeModel {

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

    public void addTopic(TopicViewMBean newTopicBean) {
        TopicTreeNode rootNode = (TopicTreeNode) getRoot();
        /*TopicTreeNode addedNode=*/ rootNode.add(new TopicViewNode(newTopicBean));
        
        //fireTreeNodesInserted(this, addedNode.getPath(), null, null);
        reload();
    }
}