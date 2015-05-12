using System;
using System.Windows.Controls;

namespace Gui.Controls {
    public class QContextMenu : ContextMenu {
        public QContextMenu() {}
        public QContextMenu(string name, Action doSomething) {
            add(name, doSomething);
        }

        public void add(string name, Action doSomething) {
            var item = new MenuItem {Header = name};
            item.Click += (sender, e) => doSomething();
            Items.Add(item);
        }
    }
}