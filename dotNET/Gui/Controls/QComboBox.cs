using System;
using System.Collections.Generic;
using System.Windows.Controls;
using O=Q.Util.Objects;

namespace Gui.Controls {
    public class QComboBox : ComboBox {
        public QComboBox(SelectionChangedEventHandler selectionChanged) : this(selectionChanged, false) {}

        public QComboBox(SelectionChangedEventHandler selectionChanged, bool isEditable) {
            IsEditable = isEditable;
            SelectedIndex = 0;
            SelectionChanged += selectionChanged;
        }

        public QComboBox(string defalt, Action<string> onNewSelection) : this(defalt, onNewSelection, false) {}

        public QComboBox(string defalt, Action<string> onNewSelection, bool isEditable) :
            this((box, args) => runOnSelection((QComboBox) box, onNewSelection, defalt), isEditable) {
        }

        public QComboBox(string defalt, Action<string> selected, IEnumerable<string> rest) :this(defalt, selected, false) {
            populateWithFirst(defalt, rest);
        }

        static void runOnSelection(QComboBox box, Action<string> onNewSelection, string defalt) {
            onNewSelection(box.selected(defalt));
        }

        public bool isSelected() {
            return SelectedIndex != -1;
        }

        public void populateWithAll(IEnumerable<string> rest) {
            populateWithFirst("ALL", rest, true);
        }

        public void populateWithAll(IEnumerable<string> rest, bool sortEntries) {
            populateWithFirst("ALL", rest, sortEntries);
        }

        public void populateWithFirst(string first, IEnumerable<string> rest) {
            populateWithFirst(first, rest, true);
        }

        public void populateWithFirst(string first, IEnumerable<string> rest, bool sortEntries) {
            clear();
            Items.Add(new ComboBoxItem {Content = first});
            SelectedIndex = 0;
            populateNoClear(rest, sortEntries);
            
        }

        public void populate(IEnumerable<string> rest) {
            clear();
            populateNoClear(rest, true);
        }

        void populateNoClear(IEnumerable<string> rest, bool sortEntries) {
            if (O.isEmpty(rest)) return;
            var items = sortEntries ? O.sort(rest) : rest;
            O.each(items, item => Items.Add(new ComboBoxItem {Content = item}));
        }

        void clear() {
            Items.Clear();
            SelectedIndex = -1;
        }

        public string selected(string defalt) {
            return isSelected() ? ((ComboBoxItem) SelectedItem).Content.ToString() : defalt;
        }
    }
}