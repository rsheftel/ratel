using System;
using System.Windows;
using System.Windows.Forms;
using AvalonDock;
using Gui.Controls;
using Gui.Util;
using Q.Recon;
using Q.Simulator;
using Q.Util;
using O=Q.Util.Objects;

namespace Gui.Recon {
    public class StatusMapPanel : QControl, StatusMapGUI {
        readonly QTreeMap treeMap;
        readonly Timers<string> launchers = new Timers<string>();
        readonly StatusTreeMap model;

        public StatusMapPanel() {
            var panel = downPanel();
            model = new StatusTreeMap(this);
            treeMap = new QTreeMap(model);
            Content = panel;
            var controlGrid = new QGrid();
            ResizingPanel.SetResizeHeight(controlGrid, 23);
            controlGrid.addColumn(1, GridUnitType.Star);
            controlGrid.addColumn(1, GridUnitType.Star);
            controlGrid.addColumn(7, GridUnitType.Star);
            controlGrid.add(new QCheckBox("Equal Size Boxes", true, selected => { model.setIsEqualSizes(selected); treeMap.updateGuiNodes();}) {VerticalAlignment = VerticalAlignment.Center}, 0);
            var refershBox = new QComboBox("5", refreshRate => treeMap.setRefreshRate(int.Parse(refreshRate) * 1000)) {Height = 20};
            controlGrid.add(withLabel("Refresh Rate (sec): ", refershBox), 1);
            refershBox.populateWithFirst("1", O.list("3", "5", "10", "30", "60"), false);
            panel.Children.Add(controlGrid);
            panel.Children.Add(treeMap);
            refreshContextMenus();

        }

        public void launcherAvailable(string host, DateTime time) {
            launchers.replace(host, time.AddMilliseconds(LiveLauncher.BEAT_WAIT_MILLIS * 3), () => { 
                launchers.remove(host);
                refreshContextMenus(); 
            });
            refreshContextMenus();
        }

        void refreshContextMenus() {
            treeMap.refreshContextMenus();
            treeMap.addContextMenuItems(new MenuItem("Kill", (e, unused) => StatusTreeMap.kill(treeMap.selected())));
            launchers.each(launcher => treeMap.addContextMenuItems(new MenuItem("Kill everywhere and Restart on " + launcher, (e, unused) => model.killAndRestart(treeMap.selected(), launcher))));
        }
    }
}