using System;
using System.Data;
using System.Windows;
using System.Windows.Media;
using AvalonDock;
using Gui.Controls;
using Gui.Util;
using Q.Recon;
using Q.Simulator;
using Q.Util;
using O=Q.Util.Objects;

namespace Gui.Recon {
    public class StatusPanel : QControl, StatusTrackerGUI {
        readonly StatusTracker statusTracker;
        bool initialized;
        readonly QDataTableGrid statusGrid;
        readonly Timers<string> launchers = new Timers<string>();
        readonly Timers<int> restarts = new Timers<int>();

        public StatusPanel() {
            var panel = downPanel();
            Content = panel;
            Loaded += initialize;
            statusGrid = new QDataTableGrid(loadStatusRow, unloadStatusRow);
            var systemStatus = new DockableContent {
                Name = "Status", Title = "System Status", Content = statusGrid
            };
            panel.Children.Add(systemStatus);
            LogC.info("starting status tracker");
            statusTracker = new StatusTracker(this, StatusTracker.allLiveSystems());
        }

        private void initialize(object sender, RoutedEventArgs e) {
            if(initialized) return;
            statusTracker.initialize();
            initialized = true;
        }

        public void setHeartbeatStatus(DataRow row, SystemStatus status) {
            setStatus(row, status, "lastBeat");
        }

        public void setTickStatus(DataRow row, SystemStatus status) {
            setStatus(row, status, "lastTick");
        }

        void setStatus(DataRow row, SystemStatus status, string columnName) {
            statusGrid.ifVisible(row, () => {
                if(status != SystemStatus.UNKNOWN)
                    statusGrid.makeWhite(row);
                Action<Brush> setColor = color => makeColor(row, columnName, color);
                switch (status) {
                    case SystemStatus.GREEN: setColor(Brushes.SpringGreen); break;
                    case SystemStatus.YELLOW: setColor(Brushes.Yellow); break;
                    case SystemStatus.RED: setColor(Brushes.Red); break;
                    case SystemStatus.UNKNOWN: statusGrid.makeColor(row, Brushes.Silver); break;
                    default: throw Bomb.toss("unknown status " + status);
                }
            });
        }

        void makeColor(DataRow row, string column, Brush color) {
            statusGrid.makeColor(row, statusTracker.index(column), color);
        }

        public void setStatusTable(DataTable table) {
            statusGrid.populateFromDataTable(table);
        }

        void loadStatusRow(DataRow row) {
            setHeartbeatStatus(row, statusTracker.status(row));
            statusGrid.resetContextMenu(row, restartMenu(row));
        }

        QContextMenu restartMenu(DataRow row) {
            var menu = new QContextMenu();
            menu.add("Kill", kill);
            menu.add("Kill & Restart", () => {
                if (statusGrid.selectedCount() == 1) restart((string) row["hostname"]);
                else alertUser("Cannot restart multiple systems on their host system - use \"Kill & Restart On\" functionality.");
            });
            launchers.each(host => menu.add("Kill everywhere and Restart on " + host, () => restart(host)));
            return menu;
        }

        void kill() {
            clearRestarts();
            O.each(statusGrid.selectedRows(), row =>StatusTracker.kill((int) row["id"]));
        }

        void restart(string host) {
            if (host.Equals("unknown")) { alertUser("Cannot restart system that has not been started"); return; }
            clearRestarts();
            O.each(statusGrid.selectedRows(), (i, last, row)  => {
                var id = (int) row["id"];
                var restartTime = O.now().AddSeconds(10 * i);
                restarts.replace( id, restartTime, () => statusTracker.restart(host, id));
            });
        }

        void clearRestarts() {
            if (restarts.clear()) alertUser("new work item cancelled pending restarts.");
        }

        public void launcherAvailable(string host, DateTime time) {
            launchers.replace(host, time.AddMilliseconds(LiveLauncher.BEAT_WAIT_MILLIS * 3), () => { 
                launchers.remove(host);
                refreshContextMenus(); 
            });
            refreshContextMenus();
        }

        void refreshContextMenus() {
            runOnGuiThread(() => statusGrid.eachRow(row => statusGrid.resetContextMenu(row, restartMenu(row))));
        }

        void unloadStatusRow(DataRow row) {
            statusGrid.makeWhite(row);
        }
    }
}