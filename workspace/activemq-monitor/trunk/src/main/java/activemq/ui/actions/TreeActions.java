package activemq.ui.actions;

import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.TreePath;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.TaskService;

import activemq.ActiveMQMonitor;
import activemq.broker.BrokerMonitor;
import activemq.ui.table.JmsMessageTableModel;
import activemq.ui.table.TableUtils;
import activemq.ui.tasks.LoadTopicsTask;
import activemq.ui.tasks.RemoveTopicsTask;
import activemq.ui.tree.BrokerTreeNode;
import activemq.ui.tree.DefaultTreeNode;
import activemq.ui.tree.DestinationTreeNode;
import activemq.ui.tree.HierarchicalTreeNode;

/**
 * Contain the actions that can be applied to a Tree.
 * 
 * The <i>tree</i> in this case is the left tree that lists the ActiveMQ topics.
 */
public class TreeActions {

    private static final class TopicFrameListener extends InternalFrameAdapter {
        private BrokerMonitor brokerMonitor;
        private String topic;

        public TopicFrameListener(BrokerMonitor brokerMonitor, String topicToMonitor) {
            this.brokerMonitor = brokerMonitor;
            topic = topicToMonitor;
        }

        @Override
        public void internalFrameClosing(InternalFrameEvent e) {
            brokerMonitor.stopListeningTo(topic, true);
        }
    }

    @Action
    public void monitorTopic() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
        List<TreePath> selectedTreeNodes = application.getSelectedTreeNodes();
        JDesktopPane desktop = application.getDesktop();

        //HashSet<String> frameNames = getDesktopFrameNames(desktop);

        BrokerMonitor brokerMonitor = null;

        for (TreePath treePath : selectedTreeNodes) {
            DestinationTreeNode selectedTreeNode = (DestinationTreeNode) treePath.getLastPathComponent();
            // don't open the topic in multiple windows
            StringBuilder windowName = new StringBuilder(128);
            windowName.append(selectedTreeNode.getBrokerName()).append(" - ");
            windowName.append(selectedTreeNode.getHierarchicalName());

            JInternalFrame existingFrame = findFrame(desktop, windowName.toString());

            if (existingFrame != null) {
                if (!existingFrame.isSelected()) {
                    try {
                        existingFrame.setSelected(true);
                    } catch (PropertyVetoException e) {
                        // We don't care that we are currently selected
                    }
                }
                continue;
            }
            // TODO if this topic starts with 'ActiveMQ.Advisory' we need to create a different type of
            // table listener
            JInternalFrame jif = new JInternalFrame(windowName.toString(), true, true, true, true);
            jif.setPreferredSize(new Dimension(430, 180));
            JmsMessageTableModel newTable = new JmsMessageTableModel(selectedTreeNode.getHierarchicalName());

            if (brokerMonitor == null
                || !brokerMonitor.getServerName().equals(selectedTreeNode.getBrokerName())) {
                brokerMonitor = application.getBrokerMonitorFor(selectedTreeNode.getBrokerName());
            }

            brokerMonitor.addTopicObserver(newTable);
            brokerMonitor.listenTo(selectedTreeNode.getHierarchicalName(), true);

            JTable recordTable = new JTable(newTable);
            // JTable recordTable = new PreferredSizeTable(newTable);
            recordTable.setDefaultRenderer(Object.class,
                new JmsMessageTableModel.ChangeRowTableCellRenderer());
            recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            TableUtils.setColumnPreferredWidths(recordTable, new int[] { 0, 1, 2 },
                new int[] { 70, 170, 180 });

            JScrollPane scrollPane = new JScrollPane(recordTable);
            jif.getContentPane().add(scrollPane);
            jif.pack();
            application.getDesktop().add(jif);

            jif.addInternalFrameListener(new TopicFrameListener(brokerMonitor, selectedTreeNode.getHierarchicalName()));
            jif.setVisible(true);
        }
    }

    private JInternalFrame findFrame(JDesktopPane desktop, String frameName) {
        JInternalFrame[] frames = desktop.getAllFrames();

        for (JInternalFrame frame : frames) {
            if (frameName.equals(frame.getTitle())) {
                return frame;
            }
        }

        return null;
    }

    @Action
    public void removeBroker() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();

        // Make a copy of this otherwise it will be modified under us
        List<TreePath> selectedTreeNodes = application.getSelectedTreeNodes();
        if (selectedTreeNodes.size() == 1) {
            TreePath treePath = selectedTreeNodes.get(0);
            final BrokerTreeNode selectedTreeNode = (BrokerTreeNode) treePath.getLastPathComponent();

            int userChoice = JOptionPane.showConfirmDialog(application.getMainFrame(),
                "Are you sure you want to remove broker '" + selectedTreeNode.getBrokerName() + "'",
                "Confirm Remove Broker", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (userChoice == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        application.removeBroker(selectedTreeNode.getBrokerName());
                        // remove from the tree itself
                        application.getTreeModel().removeNodeFromParent(selectedTreeNode);
                    }
                });
            }
        }
    }

    @Action
    public void removeTopic() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();

        // Make a copy of this otherwise it will be modified under us
        List<TreePath> selectedTreeNodes = application.getSelectedTreeNodes();
        removeTopics(application, selectedTreeNodes);
    }

    @SuppressWarnings("unchecked")
    private int[] countNodes(DefaultTreeNode node, int nodeCount, int childCount) {
        if (node instanceof DestinationTreeNode) {
            return new int[] { nodeCount, childCount + 1 };
        } else if (node instanceof HierarchicalTreeNode) {
            HierarchicalTreeNode htn = (HierarchicalTreeNode) node;
            Enumeration children = htn.children();

            int[] counts = new int[] { nodeCount + 1, childCount };

            while (children.hasMoreElements()) {
                Object child = children.nextElement();
                if (child instanceof DestinationTreeNode) {
                    counts[1] = counts[1] + 1;
                } else if (child instanceof HierarchicalTreeNode) {
                    counts = countNodes((DefaultTreeNode) child, counts[0], counts[1]);
                }
            }

            return counts;
        } else {
            return new int[] { nodeCount, childCount };
        }
    }

    private void removeTopics(ActiveMQMonitor application, List<TreePath> selectedTreeNodes) {
        int[] runningCount = new int[] { 0, 0 };
        for (TreePath treePath : selectedTreeNodes) {
            DefaultTreeNode node = (DefaultTreeNode) treePath.getLastPathComponent();
            if (!selectedTreeNodes.contains(treePath.getParentPath())) {
                int[] nodeCounts = countNodes(node, 0, 0);
                runningCount[0] = runningCount[0] + nodeCounts[0];
                runningCount[1] = runningCount[1] + nodeCounts[1];
            }
        }

        int numberOfTopics = runningCount[1];
        int numberOfNodes = runningCount[0];

        StringBuilder sb = new StringBuilder(128);
        sb.append("Are you sure you want to remove ").append(numberOfTopics).append(" topics");
        if (numberOfNodes > 0) {
            sb.append(" and ").append(numberOfNodes).append(" nodes");
        }

        sb.append("?");

        int userChoice = JOptionPane.showConfirmDialog(application.getMainFrame(), sb.toString(),
            "Confirm Remove Topics", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (userChoice == JOptionPane.YES_OPTION) {
            final ProgressMonitor progressMonitor = new ProgressMonitor(application.getMainFrame(),
                "Deleting Topics", "", 0, selectedTreeNodes.size());

            TaskService defaultService = Application.getInstance().getContext().getTaskService();
            defaultService.execute(new RemoveTopicsTask(application, progressMonitor, selectedTreeNodes,
                numberOfTopics));
        }
    }

    @Action
    public void refreshTopics() {
        // TODO Update this to work with the new multiple broker tree
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
        List<TreePath> selectedTreeNodes = application.getSelectedTreeNodes();

        if (selectedTreeNodes.size() == 1) {
            TreePath treePath = selectedTreeNodes.get(0);
            BrokerTreeNode selectedTreeNode = (BrokerTreeNode) treePath.getLastPathComponent();
            final ProgressMonitor progressMonitor = new ProgressMonitor(application.getMainFrame(),
                "Refreshing Broker " + selectedTreeNode.getBrokerName(), "", 0, 100);

            BrokerMonitor bm = application.getBrokerMonitorFor(selectedTreeNode.getBrokerName());

            application.setConfiguration(bm.getConfig());

            TaskService loaderService = Application.getInstance().getContext().getTaskService("Loader");
            loaderService.execute(new LoadTopicsTask(application, bm, progressMonitor));
        }
    }

    @Action
    public void configureBroker() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
        List<TreePath> selectedTreeNodes = application.getSelectedTreeNodes();

        if (selectedTreeNodes.size() == 1) {
            TreePath treePath = selectedTreeNodes.get(0);
            BrokerTreeNode selectedTreeNode = (BrokerTreeNode) treePath.getLastPathComponent();
            BrokerMonitor bm = application.getBrokerMonitorFor(selectedTreeNode.getBrokerName());

            application.setConfiguration(bm.getConfig());
            ConfigurationActions ca = new ConfigurationActions();
            ca.editConfiguration();
        }
    }
}
