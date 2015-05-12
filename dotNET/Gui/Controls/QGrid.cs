using System.Windows;
using System.Windows.Controls;
using O=Q.Util.Objects;

namespace Gui.Controls {
    public class QGrid : Grid {
        
        public void addColumn(int width, GridUnitType type) {
            addColumn(new ColumnDefinition {Width = new GridLength(width, type)});
        }

        void addColumn(ColumnDefinition definition) {
            ColumnDefinitions.Add(definition);
        }

        public void addColumn(int width) {
            addColumn(width, GridUnitType.Pixel);
        }

        public void addColumn() {
            addColumn(new ColumnDefinition());
        }

        public void add(UIElement child) {
            Children.Add(child);
        }

        public void add(UIElement child, int colNum) {
            SetColumn(child, colNum);
            add(child);
        }

        public void addColumns(int n) {
            O.zeroTo(n, i => addColumn());
        }
    }
}
