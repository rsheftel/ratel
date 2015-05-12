package activemq.ui.tasks;

import java.util.List;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.jdesktop.application.Task;

import activemq.ActiveMQMonitor;
import activemq.broker.BrokerMonitor;
import activemq.ui.tree.DestinationTreeNode;

public class RemoveTopicsTask extends Task<Boolean, Void> {
    private final ProgressMonitor progressMonitor;
    private final ActiveMQMonitor application;
    private final List<TreePath> selectedTreeNodes;

    public RemoveTopicsTask(ActiveMQMonitor application, ProgressMonitor pm, List<TreePath> selectedTreeNodes) {
        super(application);
        this.progressMonitor = pm;
        this.application = application;
        this.selectedTreeNodes = selectedTreeNodes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jdesktop.application.Task#finished()
     */
    @Override
    protected void finished() {
        progressMonitor.close();
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        int topicCount = selectedTreeNodes.size();
        progressMonitor.setMaximum(topicCount);

        BrokerMonitor brokerMonitor = null;

        int count = 0;
        for (TreePath treePath : selectedTreeNodes) {
            final DestinationTreeNode selectedTreeNode = (DestinationTreeNode) treePath.getLastPathComponent();
            if (brokerMonitor == null
                    || !brokerMonitor.getServerName().equals(selectedTreeNode.getBrokerName())) {
                brokerMonitor = application.getBrokerMonitorFor(selectedTreeNode.getBrokerName());
            }

            brokerMonitor.removeTopic(selectedTreeNode.getHierarchicalName());

            // remove from the tree itself
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    application.getTreeModel().removeNodeFromParent(selectedTreeNode);
                    application.getStatusBar().removeTopic();
                }
                
            });
            progressMonitor.setProgress(++count);
        }

        return Boolean.TRUE;
    }

}
