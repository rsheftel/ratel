package activemq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.swing.ActionMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DataStructure;
import org.apache.activemq.command.DestinationInfo;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskService;

import util.Monitoring;
import activemq.broker.BrokerConfiguration;
import activemq.broker.BrokerMonitor;
import activemq.ui.DestinationNodeRenderer;
import activemq.ui.IConfigurable;
import activemq.ui.MdiDesktopPane;
import activemq.ui.StatusBar;
import activemq.ui.WindowMenu;
import activemq.ui.actions.ConfigurationActions;
import activemq.ui.actions.ServerActions;
import activemq.ui.actions.TreeActions;
import activemq.ui.actions.WindowActions;
import activemq.ui.tasks.LoadTopicsTask;
import activemq.ui.tree.BrokerTreeNode;
import activemq.ui.tree.DestinationTreeNode;
import activemq.ui.tree.MultiBrokerTreeModel;

public class ActiveMQMonitor extends Application implements IJmsObserver<ActiveMQMessage>, IConfigurable {

    private static final String CONFIG_FILE = "ActiveMQ.config.xml";
    // private JmxMonitor jmxMonitor;
    // private TopicMonitor topicMonitor;
    // private AdvisoryMessageMonitor advisoryMonitor;

    private List<BrokerMonitor> monitors = new ArrayList<BrokerMonitor>();
    private ConnectionMonitorTask connectionMonitor;

    private List<TreePath> selectedTreeNodes = new ArrayList<TreePath>();

    // initialize this in startup to get the Windows look and feel
    private MdiDesktopPane desktop;

    private StatusBar statusBar;
    private MultiBrokerTreeModel treeModel = new MultiBrokerTreeModel();

    final JFrame appFrame = new JFrame("ActiveMQ Monitor");

    private JTree topicTree;
    // private Outline topicTree;

    private int topicCount;

    private BrokerConfiguration config;
    // used to track the old configuration during a config update
    private BrokerConfiguration oldConfig;
    private List<BrokerConfiguration> brokerConfig = new ArrayList<BrokerConfiguration>();

    public ActiveMQMonitor() {
        // use the default configuration
        // config = new Configuration();
    }

    public JFrame getMainFrame() {
        return appFrame;
    }

    public List<BrokerConfiguration> getBrokerConfiguration() {
        return brokerConfig;
    }

    public BrokerConfiguration getConfiguration() {
        if (config == null) {
            return null;
        } else {
            this.oldConfig = this.config;
            return new BrokerConfiguration(config);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jdesktop.application.Application#initialize(java.lang.String[])
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void initialize(String[] args) {
        try {
            Object storedConfig = getContext().getLocalStorage().load(CONFIG_FILE);
            if (storedConfig instanceof BrokerConfiguration) {
                this.config = (BrokerConfiguration) storedConfig;
            } else if (storedConfig instanceof List) {
                brokerConfig = (List<BrokerConfiguration>) storedConfig;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startup() {
        // Outline topicTree = new Outline(DefaultOutlineModel.createOutlineModel(treeModel, new
        // BrokerRowModel()));

        topicTree = new JTree(treeModel);
        // display this until we can switch to the Outline component
        // topicTree.setRootVisible(false);

        topicTree.setCellRenderer(new DestinationNodeRenderer());

        // The selection event seems to be called first, handle it, then the
        // double-click
        ActionMap actionMap = getContext().getActionMap(new TreeActions());
        JPopupMenu leafMenu = new JPopupMenu();
        leafMenu.add(actionMap.get("monitorTopic"));
        leafMenu.addSeparator();
        leafMenu.add(actionMap.get("removeTopic"));

        JPopupMenu brokerMenu = new JPopupMenu();
        brokerMenu.add(actionMap.get("refreshTopics"));

        //ActionMap configActionMap = getContext().getActionMap(new ConfigurationActions());
        brokerMenu.add(actionMap.get("configureBroker")); // only for broker nodes
        brokerMenu.addSeparator();
        brokerMenu.add(actionMap.get("removeBroker"));

        topicTree.addMouseListener(new NodeMouseListener(leafMenu, brokerMenu));
        topicTree.addTreeSelectionListener(new NodeSelectionListener());

        JScrollPane treeScrollPane = new JScrollPane(topicTree);
        // creating this here gives us the platform look and feel
        desktop = new MdiDesktopPane();
        JScrollPane desktopScrollPane = new JScrollPane(desktop);
        // scrollPane.setBorder(border);

        JPanel panel = new JPanel();

        statusBar = new StatusBar(Application.getInstance().getContext().getTaskMonitor());
        // put the two panels into a split pane
        // desktop = new JDesktopPane();
        // desktop.setLayout(new FlowLayout());
        desktop.setMinimumSize(new Dimension(200, 200));

        // The split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, treeScrollPane,
                desktopScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(10);
        topicTree.setMinimumSize(new Dimension(100, 300));

        appFrame.add(splitPane, BorderLayout.CENTER);
        appFrame.add(panel, BorderLayout.NORTH);
        appFrame.add(statusBar, BorderLayout.SOUTH);

        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.pack();
        appFrame.setSize(new Dimension(900, 800));
        appFrame.setLocationRelativeTo(null);
        appFrame.setJMenuBar(createMenuBar());
        appFrame.setVisible(true);

        TaskService taskService = new TaskService("Timer", Executors.newScheduledThreadPool(1));
        Application.getInstance().getContext().addTaskService(taskService);

        taskService = new TaskService("Loader", Executors.newScheduledThreadPool(1));
        Application.getInstance().getContext().addTaskService(taskService);

    }

    protected void ready() {
        if (brokerConfig.size() == 0) {
            // force the user to enter the configuration information
            ConfigurationActions ca = new ConfigurationActions();
            ca.editConfiguration();
        } else {
            // Create all of the monitors and connect them later
            connectAndLoad();
        }
    }

    private synchronized void connectAndLoad(BrokerConfiguration bc) {
        BrokerMonitor bm = new BrokerMonitor(bc);
        monitors.add(bm);

        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();

        final ProgressMonitor progressMonitor = new ProgressMonitor(application.getMainFrame(),
                "Loading topics for " + bc.getServerName(), "", 0, 100);

        TaskService loaderService = Application.getInstance().getContext().getTaskService("Loader");
        loaderService.execute(new LoadTopicsTask(this, bm, progressMonitor));


        TaskService taskService = Application.getInstance().getContext().getTaskService("Timer");
        connectionMonitor = new ConnectionMonitorTask(Application.getInstance());
        taskService.execute(connectionMonitor);
    }

    private void connectAndLoad() {
        for (BrokerConfiguration bc : brokerConfig) {
            connectAndLoad(bc);
        }
    }

    /**
     * Create the application menus
     * 
     * @return
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        // Put in the default File->Exit
        ActionMap defaultActionMap = getContext().getActionMap();
        JMenu fileMenu = new JMenu("File");

        ActionMap configActionMap = getContext().getActionMap(new ConfigurationActions());

        JMenuItem configItem = new JMenuItem(configActionMap.get("editConfiguration"));
        // JMenuItem configItem = new JMenuItem("Configuration");
        // configItem.setEnabled(false);
        fileMenu.add(configItem);

        fileMenu.add(new JSeparator());

        JMenuItem exitItem = new JMenuItem(defaultActionMap.get("quit"));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        ActionMap serverActionMap = getContext().getActionMap(new ServerActions());
        JMenu serverMenu = new JMenu("Servers");
        JMenuItem restartItem = new JMenuItem();
        restartItem.setAction(serverActionMap.get("restartActiveMQServer"));
        restartItem.setIcon(null);
        serverMenu.add(restartItem);

        menuBar.add(serverMenu);

        ActionMap windowActionMap = getContext().getActionMap(new WindowActions());
        WindowMenu windowMenu = new WindowMenu(getDesktop(), windowActionMap);
        menuBar.add(windowMenu);

        return menuBar;
    }

    public static void main(String[] args) {
        Application.launch(ActiveMQMonitor.class, args);
    }

    public MdiDesktopPane getDesktop() {
        return desktop;
    }

    public MultiBrokerTreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * @return
     */
    public String getServerName() {
        return config.getServerName();
    }

    /**
     * TODO This should be from the configuration
     * 
     * @return
     */
    public String getServiceName() {
        return "ActiveMQ 5.1.0";
    }

    @Override
    public void onUpdate(Object source, ActiveMQMessage message) {
        // We are only interested in new destinations at this time.
        DataStructure dataStruct = message.getDataStructure();


        if (dataStruct instanceof DestinationInfo) {
            // TODO add the new topic to the tree
            DestinationInfo destInfo = (DestinationInfo) dataStruct;
            String newTopicName = destInfo.getDestination().getPhysicalName();

            if (newTopicName != null) {
                System.out.println("Adding new topic: "+ newTopicName);
                BrokerMonitor bm = getBrokerMonitorFor(source.toString());

                if (bm != null) {
                    List<TopicViewMBean> newTopics = bm.filterTopics(newTopicName);
                    if (newTopics.size() > 0) {
                        final String serverName = bm.getServerName();
                        final TopicViewMBean newNode = newTopics.get(0);
                        
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                getTreeModel().addDestination(serverName, newNode);
                                setTopicCount(getTopicCount() + 1);
                            }
                        });
                    }
                }
            }
        }
    }

    private final class ConnectionMonitorTask extends Task<Void, Boolean> {
        private ConnectionMonitorTask(Application application) {
            super(application);
        }

        @Override
        protected Void doInBackground() throws Exception {
            // Sleep for 10 seconds before we start working
            Monitoring.sleep(10000);
            while (!isCancelled()) {
                Thread.sleep(500);
                // TODO Add this back!!
                // publish(connectionMonitor.isConnected());
            }

            return (Void) null;
        }

        @Override
        protected void process(List<Boolean> values) {
            super.process(values);
            boolean connected = values.get(0);
            if (connected) {
                statusBar.setMessageForeground(Color.GREEN.darker());
                statusBar.setMessage("Connected to " + config.getJmsUrl());
            } else {
                statusBar.setMessageForeground(Color.RED);
                statusBar.setMessage("Disconnected!  Restart GUI! - " + config.getJmsUrl());
                cancel(true);
                JOptionPane.showMessageDialog(appFrame, "Disconnected from broker.", "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * TODO This needs to be smart about the nodes and communicate with the possible popup menus
     */
    private final class NodeSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                TreePath[] paths = e.getPaths();
                for (int i = 0; i < paths.length; i++) {
                    TreePath currentPath = paths[i];
                    if (e.isAddedPath(i)) {
                        selectedTreeNodes.add(currentPath);
                    } else {
                        selectedTreeNodes.remove(currentPath);
                    }
                }

                int selectedItemCount = selectedTreeNodes.size();
                statusBar.setSelectedCount(selectedItemCount);
            } else {
                selectedTreeNodes.clear();
                statusBar.setSelectedCount(-1);
            }
        }
    }

    private final class NodeMouseListener extends MouseAdapter {
        private final JPopupMenu leafMenu;
        private final JPopupMenu brokerMenu;

        public NodeMouseListener(JPopupMenu leafMenu, JPopupMenu brokerMenu) {
            this.leafMenu = leafMenu;
            this.brokerMenu = brokerMenu;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && onlyLeafNodes()) {
                ActionMap actionMap = getContext().getActionMap(new TreeActions());
                actionMap.get("monitorTopic").actionPerformed(null);
            }
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger() && onlyLeafNodes()) {
                leafMenu.show(e.getComponent(), e.getX(), e.getY());
            } else if (e.isPopupTrigger() && isOnlyBrokerNode()) {
                brokerMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        private boolean isOnlyBrokerNode() {
            if (selectedTreeNodes.size() == 1) {
                TreePath treePath = selectedTreeNodes.get(0);
                Object selectedTreeNode = treePath.getLastPathComponent();

                return (selectedTreeNode instanceof BrokerTreeNode);
            }
            return false;
        }

        private boolean onlyLeafNodes() {
            if (selectedTreeNodes.size() > 0) {
                boolean allLeafNodes = true;

                for (TreePath treePath : selectedTreeNodes) {
                    Object selectedTreeNode = treePath.getLastPathComponent();
                    allLeafNodes = allLeafNodes && (selectedTreeNode instanceof DestinationTreeNode);
                }
                return allLeafNodes;
            } else {
                return false;
            }
        }
    }

    public void setTopicCount(int size) {
        topicCount = size;
        statusBar.setTopicCount(topicCount);
    }

    public int getTopicCount() {
        return topicCount;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void setTopicTreeModel(MultiBrokerTreeModel treeModel) {
        this.topicTree.setModel(treeModel);
        this.treeModel = treeModel;
        this.topicTree.invalidate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jdesktop.application.Application#quit(java.awt.event.ActionEvent)
     */
    @Override
    public void quit(ActionEvent event) {
        closeConnections();
        super.quit(event);
    }

    private synchronized void closeConnections() {
        try {
            if (connectionMonitor != null) {
                connectionMonitor.cancel(true);
            }
            for (BrokerMonitor bm : monitors) {
                bm.shutdownAll();
            }
            monitors.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setConfiguration(BrokerConfiguration config) {
        // if we have configuration and a new configuration
        if (this.config != null && config != null && !this.config.equals(config)) {
            this.config = config;
        } else if (config != null && this.config == null) {
            // if we have new configuration and nothing to check
            this.config = config;
        } else if (config == null) {
            // remove the configuration
            this.config = null;
        }
    }

    @Override
    public void configurationUpdated() {
        // We have an updated configuration, replace/add to brokers
        if (oldConfig != null) {
            treeModel.removeBrokerNode(oldConfig.getServerName());
            brokerConfig.remove(oldConfig);
            BrokerMonitor bm = getBrokerMonitorFor(oldConfig.getServerName());
            if (bm != null) {
                monitors.remove(bm);
                bm.shutdownAll();
            }
            oldConfig = null;
        }

        BrokerConfiguration bc = findBrokerConfiguration(config);
        if (bc == null) {
            brokerConfig.add(config);
            bc = config;
        } else {
            // we have an existing broker, shutdown and remove
            BrokerMonitor bm = getBrokerMonitorFor(config.getServerName());
            if (bm != null) {
                monitors.remove(bm);
                bm.shutdownAll();
            }
        }

        // save the configuration
        saveConfiguration();
        config = null;
        // We have a new broker or changed the config of an existing, load it up
        connectAndLoad(bc);
    }

    private void saveConfiguration() {
        try {
            getContext().getLocalStorage().save(brokerConfig, CONFIG_FILE);
            System.out.println(getContext().getLocalStorage().getDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private BrokerConfiguration findBrokerConfiguration(BrokerConfiguration newConfig) {
        for (BrokerConfiguration bc : brokerConfig) {
            if (bc.getID() == newConfig.getID() || bc.getServerName().equals(newConfig.getServerName())) {
                return bc;
            }
        }
        return null;
    }

    public List<TreePath> getSelectedTreeNodes() {
        return new ArrayList<TreePath>(selectedTreeNodes);
    }

    public BrokerMonitor getBrokerMonitorFor(String brokerName) {
        for (BrokerMonitor bm : monitors) {
            if (bm.getServerName().equals(brokerName)) {
                return bm;
            }
        }

        return null;
    }

    /**
     * Remove the broker from the list if brokers being monitored.
     * 
     * @param brokerName
     */
    public void removeBroker(String brokerName) {
        BrokerMonitor bm = getBrokerMonitorFor(brokerName);
        BrokerConfiguration bc = findBrokerConfiguration(bm.getConfig());

        bm.shutdownAll();
        monitors.remove(bm);
        brokerConfig.remove(bc);
        setTopicCount(getTopicCount() - bm.getTopicCount());
        saveConfiguration();
    }

}