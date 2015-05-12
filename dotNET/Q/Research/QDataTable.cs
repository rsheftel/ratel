using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using Q.Util;

namespace Q.Research {
    public class QDataTable : DataTable, IEnumerable {
        Predicate<DataRow> isVisible_ = row => true;
        const string VISIBLE = "rowVisibleHIDDEN";
        public event Action<List<DataRow>> filterChanged;

        public QDataTable(params string[] columns) {
            Objects.each(columns, column => Columns.Add(column, typeof (string)));
            addTypedColumn(VISIBLE, typeof (bool));
            DefaultView.RowFilter = VISIBLE;
            filterChanged += a => { };  // doNothing
        }

        public void addTypedColumn(string name, Type type) {
            Columns.Add(name, type);
        }

        public void filter<T>(string column, Predicate<T> isVisible) {
            filter(row => row.IsNull(column) || isVisible( (T) row[column]));
        }

        public void filter( Predicate<DataRow> isVisible) {
            isVisible_ = isVisible;
            updateVisible();
            filterChanged(visibleRows());
        }

        public List<DataRow> visibleRows() {
            return Objects.accept(Rows, row => (bool) row[VISIBLE]);
        }

        public void filter(string column, string selected) {
            filter<string>(column, value => selected.Equals("ALL") || value.Equals(selected));
        }

        public void add(DataRow row) {
            updateVisible(row);
            Rows.Add(row);
        }

        public void addAtStart(DataRow row) {
            updateVisible(row);
            Rows.InsertAt(row, 0);
        }

        public void updateVisible(DataRow row) {
            row[VISIBLE] = isVisible_(row);
        }

        public DataRow firstRow(string column, object o) {
            foreach(DataRow row in Rows) if(row[column].Equals(o)) return row;
            throw Bomb.toss("row not found where column " + column + " matches " + o);
        }

        public void Add(string name, Type type) {
            addTypedColumn(name, type);
        }

        public IEnumerator GetEnumerator() {
            return Columns.GetEnumerator();
        }

        public IEnumerable<DataColumn> columns() {
            foreach(DataColumn c in Columns)
                if (!c.ColumnName.Equals(VISIBLE)) 
                    yield return c;
        }

        public int visibleRowCount() {
            return visibleRows().Count;
        }

        public void updateVisible() {
            Objects.each(Rows, row => row[VISIBLE] = isVisible_(row));
        }

        public void eachVisibleRow(Action<DataRow> action) {
            Objects.each(visibleRows(), action);
        }
    }
}