using System;
using System.Windows;
using System.Windows.Controls;

namespace Gui.Controls {
    public class QButton : Button {
        readonly Action onClick;

        public QButton(string name, Action onClick) {
            this.onClick = onClick;
            Click += (unused, args) => doClick();
            Content = name;
            Width = 80;
            HorizontalAlignment = HorizontalAlignment.Right;
        }

        public void setText(string newText) {
            Content = newText;
        }

        public void doClick() {
            onClick();
        }
    }
}