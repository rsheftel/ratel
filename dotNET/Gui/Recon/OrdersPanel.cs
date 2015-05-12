using System;
using System.Collections.Generic;
using System.Data;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using Gui.Controls;
using Gui.Util;
using Q.Recon;
using Q.Util;
using O=Q.Util.Objects;


namespace Gui.Recon {
    public class OrdersPanel : QControl, OrderTrackerGUI {
        readonly OrderTracker orderTracker;
        readonly Timers<DataRow> orderTimers = new Timers<DataRow>();
        bool initialized;
        readonly QComboBox systemBox;
        readonly QComboBox pvBox;
        readonly QComboBox marketBox;
        readonly QComboBox filterBox;
        readonly QDataTableGrid orderGrid;

        public OrdersPanel() {
            var panel = new QDockPanel();
            Content = panel;
            Loaded += initialize;

            systemBox = new QComboBox(onSystemSelected);
            pvBox = new QComboBox(onPvSelected);
            marketBox = new QComboBox(onMarketSelected);
            filterBox = new QComboBox("ALL", onFilterSelected, O.list("Not Ferret", "Ferret"));
            panel.add(comboBoxPanel(), Dock.Top);
            panel.add(new FerretPanel(), Dock.Bottom);
            orderGrid = new QDataTableGrid(loadOrderRow, unloadOrderRow);
            panel.add(orderGrid);
            
            LogC.info("starting order tracker");
            orderTracker = new OrderTracker(this);
        }

        UIElement comboBoxPanel() {
            var comboBoxes = new QGrid {Height = 25 };
            comboBoxes.addColumns(6);
            comboBoxes.add(withLabel("_System:", systemBox), 0);
            comboBoxes.add(withLabel("_Pv:", pvBox), 1);
            comboBoxes.add(withLabel("_Market:", marketBox), 2);
            comboBoxes.add(new QButton("Subscribe", subscribeButtonClicked) {HorizontalAlignment = HorizontalAlignment.Left }, 3);
            comboBoxes.add(withLabel("_Filter:", filterBox), 4);
            return comboBoxes;
        }

        private void initialize(object sender, RoutedEventArgs e) {
            if(initialized) return;
            orderTracker.initialize();
            initialized = true;
        }

        bool comboBoxesPopulated() {
            return O.all(O.list(systemBox, marketBox, pvBox), box => box.isSelected());
        }

        public void setMarketChoices(List<string> markets) {
            marketBox.populateWithFirst("ALL", markets);
        }

        public void setPvChoices(List<string> pvs) {
            pvBox.populateWithFirst("ALL", pvs);
        }

        public string pv() {
            return pvBox.selected("ALL");
        }

        public bool pvSelected() {
            return !pv().Equals("ALL");
        }

        void onSystemSelected(object sender, SelectionChangedEventArgs e) {
            if(comboBoxesPopulated())
                orderTracker.systemUpdated();
        }

        public void setSystemChoices(IEnumerable<string> sivs) {
            systemBox.populateWithFirst("ALL", sivs);
        }

        public string market() {
            return marketBox.selected("ALL");
        }

        public string filter() {
            return filterBox.selected("ALL");
        }

        public void setStatus(DataRow row, OrderStatus status) {
            Action<Brush> makeColor = newColor => orderGrid.ifVisible(row, () => orderGrid.makeColor(row, newColor));
            switch (status) {
                case OrderStatus.NO_ACTION_REQUIRED: makeColor(Brushes.SpringGreen); break;
                case OrderStatus.ACTION_REQUIRED: makeColor(Brushes.Yellow); break;
                case OrderStatus.STAGE: makeColor(Brushes.Yellow); break;
                case OrderStatus.FAILED: makeColor(Brushes.Red); break;
                case OrderStatus.SIM_MISMATCH: makeColor(Brushes.DarkOrange); break;
                case OrderStatus.NOT_FERRET: break;
                default: Bomb.toss("don't know how to color " + status); break;
            }
        }

        void updateContextMenu(DataRow row) {
            removeContextMenu(row);
            addContextMenu(row);
        }

        public void addContextMenu(DataRow row) {
            orderGrid.ifVisible(row, () => {
                var menu = new QContextMenu();
                if(FerretControl.canRelease()) 
                    menu.add("Release", () => orderTracker.releaseMaybe(orderGrid.selectedRows()));
                menu.add("Cancel", () => orderTracker.cancelMaybe(orderGrid.selectedRows()));
                orderGrid.resetContextMenu(row, menu);
            });
        }

        public void removeContextMenu(DataRow row) {
            orderGrid.ifVisible(row, () =>orderGrid.clearContextMenu(row));
        }

        public bool sivSelected() {
            return !siv().Equals("ALL");
        }

        public string siv() {
            return systemBox.selected("ALL");
        }

        public void setOrderTable(DataTable table) {
            orderGrid.populateFromDataTable(table);
        }

        private void onPvSelected(object sender, SelectionChangedEventArgs e) {
            if(comboBoxesPopulated())
                orderTracker.pvUpdated();
        }

        private void onMarketSelected(object sender, SelectionChangedEventArgs e) {
            if(comboBoxesPopulated())
                orderTracker.marketsUpdated();
        }

        private void onFilterSelected(string selected) {
            if(comboBoxesPopulated())
                orderTracker.filterUpdated();
        }

        void subscribeButtonClicked() {
            orderTracker.subscribe();
        }

        void unloadOrderRow(DataRow row) {
            orderGrid.makeWhite(row);
            orderTimers.remove(row);
        }

        void loadOrderRow(DataRow row) {            
            updateContextMenu(row);
            if(O.hasContent(row, "status")) {
                setStatus(row, orderTracker.status(row));
                return;
            }
            var time = Objects.date((string) row["simFillTime"]);
            var whiteTime = time.AddMinutes(30);
            if (O.isBeforeNow(whiteTime)) return;
            orderGrid.makeColor(row, Brushes.PaleGoldenrod);
            orderTimers.add(row, whiteTime, () => orderGrid.makeWhite(row));     
        }
    }
}