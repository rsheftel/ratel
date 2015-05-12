package activemq.ui.tasks;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.jdesktop.application.Task;

import activemq.ActiveMQMonitor;
import activemq.broker.BrokerMonitor;
import activemq.ui.tree.DefaultTreeNode;
import activemq.ui.tree.DestinationTreeNode;
import activemq.ui.tree.HierarchicalTreeNode;

public class RemoveTopicsTask extends Task<Boolean, Void> {
    private final ProgressMonitor progressMonitor;
    private final ActiveMQMonitor application;
    private final List<TreePath> selectedTreeNodes;
    
    private int topicCount;
    private int count;

    public RemoveTopicsTask(ActiveMQMonitor application, ProgressMonitor pm, List<TreePath> selectedTreeNodes, int topicCount) {
        super(application);
        this.progressMonitor = pm;
        this.application = application;
        this.selectedTreeNodes = selectedTreeNodes;
        this.topicCount = topicCount;
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

    protected Boolean doInBackground() throws Exception {
        
        progressMonitor.setMaximum(topicCount);
        for (TreePath treePath : selectedTreeNodes) {
            DefaultTreeNode dtn = (DefaultTreeNode) treePath.getLastPathComponent();
            recursiveRemoveTopic(dtn);
        }
        return Boolean.TRUE;
    }
    
    private void recursiveRemoveTopic(DefaultTreeNode node) {
        BrokerMonitor brokerMonitor = null;
        
        if (node instanceof DestinationTreeNode) {
            final DestinationTreeNode dtn = (DestinationTreeNode) node;
            brokerMonitor = application.getBrokerMonitorFor(dtn.getBrokerName());
            brokerMonitor.removeTopic(dtn.getHierarchicalName());
            progressMonitor.setProgress(++count);
            
            // remove from the tree itself
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    application.getTreeModel().removeNodeFromParent(dtn);
                    application.getStatusBar().removeTopic();
                }
                
            });
            
        } else if (node instanceof HierarchicalTreeNode) {
            final HierarchicalTreeNode htn = (HierarchicalTreeNode) node;
            // Make a copy of the child list, otherwise we miss ever other one
            List<DefaultTreeNode> myChildren = copyChildren(htn.children());

            //while (children.hasMoreElements()) {
            for (DefaultTreeNode child : myChildren) {
                if (child instanceof DestinationTreeNode) {
                    recursiveRemoveTopic((DefaultTreeNode)child);
                } else if (child instanceof HierarchicalTreeNode) {
                    recursiveRemoveTopic((DefaultTreeNode)child);
                }
            }
            // Remove the current hierarchical node
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    application.getTreeModel().removeNodeFromParent(htn);
                }
                
            });

        } 
    }

    @SuppressWarnings("unchecked")
    private List<DefaultTreeNode> copyChildren(Enumeration children) {
        List<DefaultTreeNode> copy = new ArrayList<DefaultTreeNode>();
         
        while (children.hasMoreElements()) {
            copy.add((DefaultTreeNode)children.nextElement());
        }
        
        return copy;
    }

}
