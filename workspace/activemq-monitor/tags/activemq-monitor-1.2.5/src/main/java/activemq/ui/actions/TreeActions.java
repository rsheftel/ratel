package activemq.ui.actions;

import java.awt.Dimension;
import java.util.HashSet;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
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
import activemq.ui.tree.DestinationTreeNode;

/**
 * Contain the actions that can be applied to a Tree.
 * 
 * The <i>tree</i> in this case is the left tree that lists the ActiveMQ topics.
 */
public class TreeActions {

    private static final class TopicFrameListener extends InternalFrameAdapter {
        private BrokerMonitor brokerMonitor;

        public TopicFrameListener(BrokerMonitor brokerMonitor) {
            this.brokerMonitor = brokerMonitor;
        }

        @Override
        public void internalFrameClosing(InternalFrameEvent e) {
            brokerMonitor.stopListeningTo(e.getInternalFrame().getTitle(), true);
        }
    }

    @Action
    public void monitorTopic() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
        List<TreePath> selectedTreeNodes = application.getSelectedTreeNodes();
        JDesktopPane desktop = application.getDesktop();

        HashSet<String> frameNames = getDesktopFrameNames(desktop);

        BrokerMonitor brokerMonitor = null;

        for (TreePath treePath : selectedTreeNodes) {
            DestinationTreeNode selectedTreeNode = (DestinationTreeNode) treePath.getLastPathComponent();
            // don't open the topic in multiple windows
            if (frameNames.contains(selectedTreeNode.getHierarchicalName())) {
                continue;
            }
            // TODO if this topic starts with 'ActiveMQ.Advisory' we need to create a different type of
            // table listener
            JInternalFrame jif = new JInternalFrame(selectedTreeNode.getHierarchicalName(), true, true, true,
                    true);
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

            jif.addInternalFrameListener(new TopicFrameListener(brokerMonitor));
            jif.setVisible(true);
        }
    }

    private HashSet<String> getDesktopFrameNames(JDesktopPane desktop) {
        JInternalFrame[] frames = desktop.getAllFrames();
        HashSet<String> names = new HashSet<String>();

        for (JInternalFrame frame : frames) {
            names.add(frame.getTitle());
        }

        return names;
    }

    @Action
    public void removeBroker() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();

        // Make a copy of this otherwise it will be modified under us
        List<TreePath> selectedTreeNodes = application.getSelectedTreeNodes();
        if (selectedTreeNodes.size() == 1) {
            TreePath treePath = selectedTreeNodes.get(0);
            BrokerTreeNode selectedTreeNode = (BrokerTreeNode) treePath.getLastPathComponent();

            int userChoice = JOptionPane.showConfirmDialog(application.getMainFrame(),
                    "Are you sure you want to remove broker '" + selectedTreeNode.getBrokerName() + "'",
                    "Confirm Remove Broker", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (userChoice == JOptionPane.YES_OPTION) {
                application.removeBroker(selectedTreeNode.getBrokerName());
                // remove from the tree itself
                application.getTreeModel().removeNodeFromParent(selectedTreeNode);
            }
        }
    }

    @Action
    public void removeTopic() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();

        // Make a copy of this otherwise it will be modified under us
        List<TreePath> selectedTreeNodes = application.getSelectedTreeNodes();
        if (selectedTreeNodes.size() > 1) {
            removeTopics(application, selectedTreeNodes);
        } else {
            removeTopic(application, selectedTreeNodes);
        }
    }

    private void removeTopic(ActiveMQMonitor application, List<TreePath> selectedTreeNodes) {
        TreePath treePath = selectedTreeNodes.get(0);
        DestinationTreeNode selectedTreeNode = (DestinationTreeNode) treePath.getLastPathComponent();

        int userChoice = JOptionPane.showConfirmDialog(application.getMainFrame(),
                "Are you sure you want to remove topic '" + selectedTreeNode.getHierarchicalName() + "'",
                "Confirm Remove Topic", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (userChoice == JOptionPane.YES_OPTION) {
            BrokerMonitor brokerMonitor = application.getBrokerMonitorFor(selectedTreeNode.getBrokerName());
            brokerMonitor.removeTopic(selectedTreeNode.getHierarchicalName());
            // remove from the tree itself
            application.getTreeModel().removeNodeFromParent(selectedTreeNode);
            application.getStatusBar().removeTopic();
        }

    }

    private void removeTopics(ActiveMQMonitor application, List<TreePath> selectedTreeNodes) {
        int numberOfTopics = selectedTreeNodes.size();

        int userChoice = JOptionPane.showConfirmDialog(application.getMainFrame(),
                "Are you sure you want to remove " + numberOfTopics + " topics?", "Confirm Remove Topics",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (userChoice == JOptionPane.YES_OPTION) {
            final ProgressMonitor progressMonitor = new ProgressMonitor(application.getMainFrame(),
                    "Deleting Topics", "", 0, selectedTreeNodes.size());

            TaskService defaultService = Application.getInstance().getContext().getTaskService();
            defaultService.execute(new RemoveTopicsTask(application, progressMonitor, selectedTreeNodes));
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
