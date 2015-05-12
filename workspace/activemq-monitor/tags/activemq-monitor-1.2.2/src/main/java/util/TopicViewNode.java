package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jms.InvalidSelectorException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

import org.apache.activemq.broker.jmx.TopicViewMBean;

public class TopicViewNode implements ITopicView {

    private TopicViewMBean delegate;

    private String name;

    private String baseName = "";

    public TopicViewNode(String name) {
        setNames(name);
    }

    public TopicViewNode(TopicViewMBean leaf) {
        delegate = leaf;
        setNames(leaf.getName());
    }

    private void setNames(String qualifiedName) {
        String[] names = qualifiedName.split("\\.");
        this.name = names[names.length - 1];

        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot != -1) {
            this.baseName = qualifiedName.substring(0, lastDot);
        } else {
            baseName = "";
        }
    }

    @Override
    public ITopicView[] createParentNodes() {
        if (isLeaf()) {
            String baseName = getHierarchicalName();

            int dotPos = baseName.indexOf('.');
            List<ITopicView> parentList = new ArrayList<ITopicView>();

            while (dotPos != -1) {
                parentList.add(new TopicViewNode(baseName.substring(0, dotPos)));
                dotPos = baseName.indexOf('.', dotPos + 1);
            }
            return parentList.toArray(new ITopicView[parentList.size()]);
        } else {
            return new ITopicView[0];
        }

    }

    @Override
    public boolean isBranch() {
        return delegate == null;
    }

    @Override
    public boolean isLeaf() {
        return !isBranch();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public String getHierarchicalName() {
        if (baseName != null && baseName.length() > 0) {
            return baseName + "." + getName();
        } else {
            return getName();
        }
    }

    @Override
    public String toString() {
        return "isLeaf=" + isLeaf() + ", baseName=" + getBaseName() + ", name=" + getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TopicViewNode)) {
            return false;
        }

        TopicViewNode other = (TopicViewNode) obj;

        if (isLeaf() && other.isLeaf()) {
            return delegate.equals(other.delegate);
        } else if (isBranch() && other.isBranch()) {
            return getHierarchicalName().equals(other.getHierarchicalName());
        }

        return false;
    }

    
    @Override
    public int hashCode() {
        return getBaseName().hashCode() + (17 * getName().hashCode());
    }

    public CompositeData[] browse() throws OpenDataException {
        return delegate.browse();
    }

    public CompositeData[] browse(String selector) throws OpenDataException, InvalidSelectorException {
        return delegate.browse(selector);
    }

    public TabularData browseAsTable() throws OpenDataException {
        return delegate.browseAsTable();
    }

    public TabularData browseAsTable(String selector) throws OpenDataException, InvalidSelectorException {
        return delegate.browseAsTable(selector);
    }

    public List<?> browseMessages() throws InvalidSelectorException {
        return delegate.browseMessages();
    }

    public List<?> browseMessages(String selector) throws InvalidSelectorException {
        return delegate.browseMessages(selector);
    }

    public double getAverageEnqueueTime() {
        return delegate.getAverageEnqueueTime();
    }

    public long getConsumerCount() {
        return delegate.getConsumerCount();
    }

    public long getDequeueCount() {
        return delegate.getDequeueCount();
    }

    public long getDispatchCount() {
        return delegate.getDispatchCount();
    }

    public long getEnqueueCount() {
        return delegate.getEnqueueCount();
    }

    public long getInFlightCount() {
        return delegate.getInFlightCount();
    }

    public int getMaxAuditDepth() {
        return delegate.getMaxAuditDepth();
    }

    public long getMaxEnqueueTime() {
        return delegate.getMaxEnqueueTime();
    }

    public int getMaxProducersToAudit() {
        return delegate.getMaxProducersToAudit();
    }

    public long getMemoryLimit() {
        return delegate.getMemoryLimit();
    }

    public int getMemoryPercentUsage() {
        return delegate.getMemoryPercentUsage();
    }

    public float getMemoryUsagePortion() {
        return delegate.getMemoryUsagePortion();
    }

    public long getMinEnqueueTime() {
        return delegate.getMinEnqueueTime();
    }

    public long getProducerCount() {
        return delegate.getProducerCount();
    }

    public long getQueueSize() {
        return delegate.getQueueSize();
    }

    public boolean isProducerFlowControl() {
        return delegate.isProducerFlowControl();
    }

    public void resetStatistics() {
        delegate.resetStatistics();
    }

    @SuppressWarnings("unchecked")
    public String sendTextMessage(Map headers, String body) throws Exception {
        return delegate.sendTextMessage(headers, body);
    }

    public String sendTextMessage(String body) throws Exception {
        return delegate.sendTextMessage(body);
    }

    public void setMaxAuditDepth(int maxAuditDepth) {
        delegate.setMaxAuditDepth(maxAuditDepth);
    }

    public void setMaxProducersToAudit(int maxProducersToAudit) {
        delegate.setMaxProducersToAudit(maxProducersToAudit);
    }

    public void setMemoryLimit(long limit) {
        delegate.setMemoryLimit(limit);
    }

    public void setMemoryUsagePortion(float value) {
        delegate.setMemoryUsagePortion(value);
    }

    public void setProducerFlowControl(boolean producerFlowControl) {
        delegate.setProducerFlowControl(producerFlowControl);
    }

}
