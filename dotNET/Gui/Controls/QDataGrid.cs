using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Globalization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Media;
using Gui.Util;
using Microsoft.Windows.Controls;
using Q.Util;
using Action=System.Action;
using O=Q.Util.Objects;

namespace Gui.Controls {
    public class QDataTableGrid : QDataGrid<DataRow, DataRowView> {
        public QDataTableGrid(Action<DataRow> onLoadRow, Action<DataRow> onUnloadRow) : base(onLoadRow, onUnloadRow, view => view.Row) {}
        public QDataTableGrid() : base(view => view.Row) {}
    }

    public class QDataGrid<ROW, ROWVIEW> : DataGrid {
        readonly Converter<ROWVIEW, ROW> viewToRow;
        readonly Dictionary<ROW, DataGridRow> rows = new Dictionary<ROW, DataGridRow>();
        event Action<ROW> onLoadRow;
        event Action<ROW> onUnloadRow;
        static readonly IValueConverter ourConverter = new OurValueConverter();

        public QDataGrid(Action<ROW> onLoadRow, Action<ROW> onUnloadRow, Converter<ROWVIEW, ROW> viewToRow) : this(viewToRow) {
            this.viewToRow = viewToRow;
            this.onLoadRow += onLoadRow;
            this.onUnloadRow += onUnloadRow;
        }

        static void skipHidden(object sender, DataGridAutoGeneratingColumnEventArgs e) {
            if(((string) e.Column.Header).EndsWith("HIDDEN")) e.Cancel = true;
            var textColumn = e.Column as DataGridTextColumn;
            if(textColumn == null) return;
            ((Binding) textColumn.Binding).Converter = ourConverter;
        }

        public QDataGrid(Converter<ROWVIEW, ROW> viewToRow) {
            this.viewToRow = viewToRow;
            CanUserReorderColumns = true;
            FrozenColumnCount = 1;
            IsTabStop = false;
            HorizontalGridLinesBrush = Brushes.LightGray;
            VerticalGridLinesBrush = Brushes.LightGray;
            EnableColumnVirtualization = true;
            EnableRowVirtualization = true;
            IsReadOnly = true;
            AreRowDetailsFrozen = false;
            SelectionUnit = DataGridSelectionUnit.FullRow;
            MinColumnWidth = 15;
            ColumnWidth = DataGridLength.Auto;
            // this breaks virutalization
            // ScrollViewer.SetCanContentScroll(this, false);

            LoadingRow += loadRow;
            UnloadingRow += unloadRow;

            onLoadRow += row => { };
            onUnloadRow += row => { };
            
            AutoGeneratingColumn += skipHidden;
        }

        void unloadRow(object sender, DataGridRowEventArgs e) {
            var row = viewToRow((ROWVIEW) e.Row.Item);
            onUnloadRow(row);
            rows.Remove(row);
        }

        void loadRow(object sender, DataGridRowEventArgs e) {
            var row = viewToRow((ROWVIEW) e.Row.Item);
            rows[row] = e.Row;
            onLoadRow(row);
        }

        public void populateFromDataTable(DataTable table) {
            populate(table.DefaultView);
        }

        public void populate(IEnumerable table) {
            ItemsSource = table;
        }

        public void makeWhite(ROW row) {
            makeColor(row, Brushes.White);
        }        
        
        public void ifVisible(ROW row, Action action) {
            withGuiRow(row, guiRow => action());
        }

        void withGuiRow(ROW row, Action<DataGridRow> onGuiRow) {
            if (!rows.ContainsKey(row)) { LogC.verbose(() => "discarding action on original thread for non-showing " + row); return; }
            QControl.runOnGuiThread(this, () => {
                DataGridRow guiRow;
                rows.TryGetValue(row, out guiRow);
                if (guiRow != null) onGuiRow(guiRow); 
                else LogC.verbose(() => "discarding action on gui thread for non showing" + row); 
            });
        }

        public void makeColor(ROW row, int columnNum, Brush color) {
            withGuiRow(row, guiRow => {
                var cellBlock = (TextBlock) Columns[columnNum].GetCellContent(guiRow);
                if (cellBlock == null) return;
                var cell = (DataGridCell) LogicalTreeHelper.GetParent(cellBlock);
                QControl.makeColor(this, cell, color);
            });
        }

        public void makeColor(ROW row, Brush color) {
            withGuiRow(row, guiRow => QControl.makeColor(this, guiRow, color));
        }

        public void resetContextMenu(ROW row, ContextMenu menu) {
            withGuiRow(row, guiRow => guiRow.ContextMenu = menu);
        }

        public void clearContextMenu(ROW row) {
            withGuiRow(row, guiRow => guiRow.ContextMenu = null);
        }

        public bool hasContextMenu(ROW row) {
            return rows[row].ContextMenu != null;
        }

        public void eachRow(Action<ROW> onRow) {
            O.eachKey(rows, onRow);
        }

        public IEnumerable<ROW> selectedRows() {
            var selectedEntries = O.accept(rows, (model, ui) => ui.IsSelected);
            return O.convert(selectedEntries, e => e.Key);
        }

        public int selectedCount() {
            return O.list(selectedRows()).Count;
        }

    }

    internal class OurValueConverter : IValueConverter {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture) {
            Bomb.unless(targetType.Equals(typeof(string)), () => "only know how to convert to string, not " + targetType.FullName);
            if(value is double) return Objects.prettyNumber((double) value);
            if(value is int) return Objects.prettyNumber((int) value);
            if(value is long) return Objects.prettyNumber((long) value);
            if(value is DateTime) return Objects.ymdHuman((DateTime) value);
            return value.ToString();
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) {
            throw new NotImplementedException();
        }
    }
}