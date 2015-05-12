using System;
using System.Collections.Generic;
using System.Data;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using amazon;
using Gui.Controls;
using Gui.Util;
using Q.Amazon;
using Q.Recon;
using Q.Util;
using systemdb.metadata;
using util;
using O=Q.Util.Objects;

namespace Gui.Recon {
    public class CloudSTOPanel : QControl, CloudSTOTrackerGUI {
        readonly QDataTableGrid instanceGrid;
        readonly CloudSTOTracker tracker;
        readonly QComboBox systemIdBox;
        int currentSystemId_ = 80337;
        readonly Label redGreenLabel;
        readonly Label instancesLabel;
        readonly Label summaryLabel;
        readonly Label completionTimeLabel;

        public CloudSTOPanel() {
            var panel = new QDockPanel();
            Content = panel;
            Loaded += initialize;
            instanceGrid = new QDataTableGrid(loadInstanceRow, unloadInstanceRow);
            var controls = new QDockPanel();
            systemIdBox = new QComboBox("-1", resetInstanceId, true) {
                MinWidth = 80, IsTextSearchEnabled = true, IsSynchronizedWithCurrentItem = false
            };
            controls.add(systemIdBox, Dock.Left);
            var grid = new QGrid();
            grid.addColumns(4);
            instancesLabel = label("");
            grid.add(instancesLabel, 0);
            redGreenLabel = label("");
            grid.add(redGreenLabel, 1);
            summaryLabel = label("");
            grid.add(summaryLabel, 2);
            completionTimeLabel = label("");
            grid.add(completionTimeLabel, 3);
            controls.add(grid, Dock.Left);
            controls.add(new QButton("Refresh System Ids", refreshSystemIds), Dock.Left);
            panel.add(controls, Dock.Top);
            panel.add(instanceGrid, Dock.Top);
            LogC.info("starting cloud STO tracker");
            tracker = new CloudSTOTracker(this);
        }


        static IEnumerable<string> allIds() {
            var jids = SystemDetailsTable.allAvailableStoIds();
            var ids = O.list<java.lang.Integer>(jids);
            return O.convert(ids, i => "" + i);
        }

        void resetInstanceId(string newId) {
            try {
                currentSystemId_ = int.Parse(newId);
            } catch {
                alertUser("systemId " + newId + " does not parse as an int.");
                return; // don't alert tracker
            }
            systemIdUpdated();
        }

        void unloadInstanceRow(DataRow row) {
            instanceGrid.makeWhite(row);
        }

        void loadInstanceRow(DataRow row) {
            setStatus(row, tracker.status(row));
            var menu = new QContextMenu("Kill Instance", () => {
                var instanceId = (string) row["instanceId"];
                new Instance(instanceId).shutdown();
                tracker.kill(instanceId);
            });
            instanceGrid.resetContextMenu(row, menu);
        }

        private void initialize(object sender, RoutedEventArgs e) {
            refreshSystemIds();
        }

        void refreshSystemIds() {
            systemIdBox.populate(allIds());
            tracker.clear();
        }

        void systemIdUpdated() {
            if(systemId() != -1)
                tracker.systemIdUpdated();
        }

        public void setInstanceTable(DataTable table) {
            instanceGrid.populateFromDataTable(table);
        }

        public int systemId() {
            return currentSystemId_;
        }

        public void setStatus(DataRow row, SystemStatus status) {
            instanceGrid.ifVisible(row, () => { 
                if(status.Equals(SystemStatus.UNKNOWN)) return;
                if(status.Equals(SystemStatus.GREEN)) instanceGrid.makeColor(row, Brushes.SpringGreen);
                else if(status.Equals(SystemStatus.YELLOW)) instanceGrid.makeColor(row, Brushes.Yellow);
                else instanceGrid.makeColor(row, Brushes.Red);
            });
        }

        public void setTotals(int red, int green) {
            redGreenLabel.Content = "Total Red: " + red + " Total Green: " + green;
        }

        public void setInstanceCount(int numInstances) {
            instancesLabel.Content = "Instances: " + numInstances;
        }

        public void setSummary(long complete, long total, double rpm, DateTime completionTime) {
            summaryLabel.Content = "Completed: " + complete + "/" + total + " (" + Strings.nDecimals(2, rpm) + " per min)";
            var dateString = completionTime.CompareTo(O.now().AddDays(1)) > 0 ? O.ymdHuman(completionTime) : Dates.hhMmSs(O.jDate(completionTime));
            completionTimeLabel.Content = completionTime == O.SQL_MIN_DATE 
                ? "" 
                : "Est. Completion Time: " + dateString;
        }
    }
}