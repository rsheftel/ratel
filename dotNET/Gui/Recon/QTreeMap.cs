using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using System.Windows.Forms.Integration;
using Microsoft.Research.CommunityTechnologies.Treemap;
using Q.Recon;
using Q.Util;

namespace Gui.Recon {
    public class QTreeMap : WindowsFormsHost {
        readonly TreemapControl control;
        System.Threading.Timer timer;
        readonly List<QNode> modelNodes;
        List<Node> guiNodes;
        readonly Dictionary<Node, QNode> nodeMap = new Dictionary<Node, QNode>();
        QNode zoomedNode;

        public QTreeMap(TreeMapModel model) {
            control = new TreemapControl { IsZoomable =  true};
            Child = control;

            control.NodeDoubleClick += (s, e) => {
                var qNode = nodeMap[e.Node];
                if(!qNode.hasChildren()) return;
                control.Clear();
                zoomedNode = qNode;
                addToControl(control.Nodes, visibleNodes(), 2);
                guiNodes = Objects.list<Node>(control.Nodes);

            };

            control.MinColorMetric = -5000F;
            control.MaxColorMetric = 5000F;
            control.MinColor = Color.SpringGreen;
            control.MaxColor = Color.Red;
            control.PaddingPx = 1;
            refreshContextMenus();

            control.BeginUpdate();
            modelNodes = model.nodes();
            addToControl(control.Nodes, modelNodes);
            control.EndUpdate();
            guiNodes = Objects.list<Node>(control.Nodes);
            setRefreshRate(5000);
        }

        public void setRefreshRate(int millis) {
            if(timer != null) timer.Dispose();
            Objects.timerManager().everyMillis(millis, updateGuiNodes, out timer);
        }

        public void refreshContextMenus() {
            control.ContextMenu = new ContextMenu(new [] { 
                new MenuItem("Zoom out", (s, unused) => {
                    if(zoomedNode == null) return;
                    zoomedNode = zoomedNode.parent();
                    control.Clear();
                    addToControl(control.Nodes, visibleNodes(), zoomedNode == null ? 1 : 2);
                    guiNodes = Objects.list<Node>(control.Nodes);
                }),
            });
        }

        IEnumerable<QNode> visibleNodes() {
            return zoomedNode == null ? modelNodes : Objects.list(zoomedNode);
        }

        public void updateGuiNodes() {
            control.BeginUpdate();
            update(guiNodes, visibleNodes());
            control.EndUpdate();
        }

        static void update(IEnumerable<Node> nodes, IEnumerable<QNode> modelNodes) {
            Objects.each(nodes, modelNodes, (guiNode, modelNode) => {
                guiNode.Text = modelNode.text;
                guiNode.ToolTip = modelNode.text;
                guiNode.SizeMetric = (float) modelNode.size;
                guiNode.ColorMetric = (float) modelNode.color;
                if(guiNode.Nodes.Count > 0)
                    update(Objects.enumerable<Node>(guiNode.Nodes), modelNode.children());
            });
        }

        void addToControl(Nodes nodes, IEnumerable<QNode> toAdd, int toLevel) {
            Objects.each(toAdd, nodeToAdd => {
                var newNode = nodes.Add(nodeToAdd.text, (float) nodeToAdd.size, (float) nodeToAdd.color);
                nodeMap[newNode] = nodeToAdd;
                if(toLevel > 0 && nodeToAdd.hasChildren()) addToControl(newNode.Nodes, nodeToAdd.children(), toLevel - 1);
            });
        }

        void addToControl(Nodes nodes, IEnumerable<QNode> toAdd) {
            addToControl(nodes, toAdd, 1);
        }

        public void addContextMenuItems(params MenuItem[] items) {
            Objects.each(items, item => control.ContextMenu.MenuItems.Add(item));
        }

        public QNode selected() {
            return nodeMap[control.SelectedNode];
        }
    }

}