using System.Collections.Generic;
using System.Windows;
using System.Windows.Controls;
using AvalonDock;
using bloomberg;
using db;
using Gui.Controls;
using Gui.Util;
using mail;
using Q.Recon;
using Q.Util;
using systemdb.live;
using util;
using HorizontalAlignment=System.Windows.HorizontalAlignment;
using MessageBox=System.Windows.MessageBox;
using O=Q.Util.Objects;
using TextBox=System.Windows.Controls.TextBox;

namespace Gui.Recon {
    public class MainWindow : DockingWindow {
        static readonly List<string> DEVELOPERS = O.list("jbay", "mfranz");
        readonly DockablePane dockPane = new DockablePane();

        public MainWindow(string[] args) : base("Recon") {
            LogC.info("started MainWindow.");
            LogC.useJavaLog = true;
            var arguments = Arguments.arguments(args, O.jStrings("prefix", "pane"));
            OrderTable.prefix = arguments.get("prefix", "TOMAHAWK");
            var pane = arguments.get("pane", "all");
            dockManager.Content = dockPane;
            addPanels(dockPane, pane);
            Title = "Recon " + OrderTable.prefix;
            if(DEVELOPERS.Contains(O.username())) return;
            while(!UsersTable.USERS.isLoggedIn(O.username())) {
                var result = MessageBox.Show(
                    "You are not logged in to Bloomberg, or do not have permission to view data from the Server API.  Press OK to try again, Cancel to close Recon.",
                    "Please Log In To Bloomberg",
                    MessageBoxButton.OKCancel,
                    MessageBoxImage.Error,
                    MessageBoxResult.OK
                );
                if(result.Equals(MessageBoxResult.Cancel)) O.systemExit(0);

            }
        }

        public class ReconPanel : QControl {
            public ReconPanel() {
                Producer<string> buttonText = ()=> "set LOG__VERBOSE " + (Log.verbose() ? "off" : "on");
                QButton[] verboseButton = {null};
                verboseButton[0] = new QButton(buttonText(), () => {
                    bool newVerbose;
                    LogC.flipVerbose(out newVerbose);
                    runOnGuiThread(()=> verboseButton[0].setText(buttonText()));
                }) {HorizontalAlignment = HorizontalAlignment.Left, VerticalAlignment =  VerticalAlignment.Top, Width=250};
                var dockPanel = new QDockPanel {LastChildFill = false};
                Content = dockPanel;
                dockPanel.add(verboseButton[0], Dock.Top);

                var executionConfigurationGrid = new QGrid {Height = 25 };
                dockPanel.add(executionConfigurationGrid, Dock.Top);
                O.zeroTo(4, i => executionConfigurationGrid.addColumn(200));
                var platformBox = new TextBox();
                var routeBox = new TextBox();
                var typeBox = new QComboBox("Future", selected => {
                    var config = ExecutionConfigurationTable.currentConfiguration(selected);
                    platformBox.Text = config.platform();
                    routeBox.Text = config.route();
                }, O.list("Equity"));
                executionConfigurationGrid.add(typeBox, 0);
                executionConfigurationGrid.add(withLabel("Platform", platformBox), 1);
                executionConfigurationGrid.add(withLabel("Route", routeBox), 2);
                executionConfigurationGrid.add(new QButton("Set Current Execution Configuration", () => {
                    var type = typeBox.selected("NA");
                    var platform = platformBox.Text;
                    var route = routeBox.Text;
                    ExecutionConfigurationTable.CONFIG.insert(type, platform, route);
                    Db.commit();
                    Email.notification("Execution Configuration Changed for " + type + ": " + platform + ", "  + route, "").sendTo("team");
                    alertUser("Configuration changed for " + type + " - don't forget to restart systems to pick up the change.");
                }) {Width = 200}, 3);
            }
        }

        static void addPanels(ItemsControl pane, string userPane) {
            addContentPanel("Orders", userPane, pane, () => new OrdersPanel());
            if (userPane.Equals("OldSystemStatus"))  addContentPanel("OldSystemStatus", userPane, pane, () => new StatusPanel());
            addContentPanel("SystemStatus", userPane, pane, () => new StatusMapPanel());
            addContentPanel("STO", userPane, pane, () => new CloudSTOPanel());
            addContentPanel("Positions", userPane, pane, () => new PositionsPanel());
            addContentPanel("Watcher", userPane, pane, () => new WatcherPanel());
            addContentPanel("Recon", userPane, pane, () => new ReconPanel());
        }

        static void addContentPanel(string title, string userPane, ItemsControl pane, Producer<QControl> makePanel) {
            if (!usePane(userPane, title)) return;
            LogC.info("creating control " + title);
            var content = makePanel();
            LogC.info("control created. " + title);
            pane.Items.Add(new DockableContent {
                Name = title, Title = title, Content = content
            });
        }

        static bool usePane(string userPane, string potential) {
            var result = "all".Equals(userPane) || userPane.Equals(potential);
            LogC.info("usePane " + potential + " " + result);
            return result;
        }
    }


}