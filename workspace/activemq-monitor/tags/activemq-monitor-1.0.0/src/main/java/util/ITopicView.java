package util;

import org.apache.activemq.broker.jmx.TopicViewMBean;

public interface ITopicView extends TopicViewMBean {

    boolean isBranch();
    
    boolean isLeaf();
    
    String getName();
    
    String getBaseName();
    
    String getHierarchicalName();
    
    ITopicView getParent();
    
    /** 
     * If this is a leaf node crate the parents that would below to this node
     * 
     * @return
     */
    ITopicView[] createParentNodes();
    
    
}
