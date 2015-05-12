using System.Data;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using db.clause;
using Gui.Controls;
using Gui.Util;
using Q.Recon;
using systemdb.metadata;
using O=Q.Util.Objects;

namespace Gui.Recon {
    public class WatcherPanel : QControl, WatcherGui {
        readonly QDataTableGrid grid;
        readonly LiveWatcher watcher;
        bool initialized;

        public WatcherPanel() {           
            watcher = new LiveWatcher(this);
            var panel = new QDockPanel();
            grid = new QDataTableGrid(loadRow, row => {});
            Content = panel;
            panel.add(filterPanel(), Dock.Top);
            panel.add(grid);
            Loaded += (s, e) => initialize();
        }

        UIElement filterPanel() {
            var result = new QGrid {Height = 25 };
            result.addColumn(200);
            result.addColumn(100);
            result.addColumn(100);
            result.add(new QComboBox("ALL", selected => watcher.setTagFilter(selected), O.list<string>(BloombergTagsTable.TAGS.C_TAG.distinct(Clause.TRUE))));
            result.add(new QCheckBox("Show Zeros", false, setting => watcher.setFilterZeroes(!setting)), 1);
            result.add(new QCheckBox("Show Hidden", false, setting => watcher.setShowHidden(setting)), 2);
            return result;
        }

        void initialize() {
            if(initialized) return;
            watcher.initialize();
            grid.populateFromDataTable(watcher.dataTable());
            initialized = true;
        }

        void loadRow(DataRow row) {
            grid.resetContextMenu(row, restartMenu(row));
            setStatus(row, LiveWatcher.status(row));
        }

        QContextMenu restartMenu(DataRow row) {
            var menu = new QContextMenu();
            menu.add("Hide this Tag (sticky)", () => watcher.addExclusion(row));
            menu.add("Unhide this Tag (sticky)",  () => watcher.removeExclusion(row));
            return menu;
        }

        public void setStatus(DataRow row, SystemStatus status) {
            grid.makeColor(row, status == SystemStatus.GREEN ? Brushes.SpringGreen : Brushes.Red);
        }
    }
}