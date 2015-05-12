using System.Collections.Generic;
using Q.Util;

namespace Q.Recon {
    public class QNode : Objects {
        public string text;
        public double size;
        public double color;
        readonly List<QNode> children_ = new List<QNode>();
        QNode parent_;

        public QNode(string text, double size, double color) {
            this.text = text;
            this.size = size;
            this.color = color;
        }

        public T add<T>(T child) where T : QNode {
            children_.Add(child);
            child.setParent(this);
            return child;
        }

        void setParent(QNode newParent) {
            Bomb.unlessNull(parent_, () => "parent is already set to " + parent_.text);
            parent_ = newParent;
        }

        public IEnumerable<QNode> children() { return children_; }

        public bool hasChildren() {
            return hasContent(children_);
        }

        public QNode parent() {
            return parent_;
        }

        public override string ToString() {
            return string.Format("text: {0}, size: {1}, color: {2}", text, size, color);
        }
    }
}