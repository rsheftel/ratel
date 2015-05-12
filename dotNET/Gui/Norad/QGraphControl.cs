using System;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using Q.Util;
using ZedGraph;
using O=Q.Util.Objects;

namespace Gui.Norad {
    public class QGraphControl : ZedGraphControl {
        protected bool hasValidated;
        bool showLegend = true;
        bool showCrosshair_;
        protected readonly Dictionary<string, QGraphPane> panes = new Dictionary<string, QGraphPane>();
        readonly PaneGenerator makePane;

        protected QGraphControl(PaneGenerator makePane) {
            IsShowHScrollBar = false;
            IsEnableHPan = false;
            IsEnableVPan = false;

            IsEnableHZoom = false;
            IsEnableVZoom = false;
            IsZoomOnMouseCenter = false;

            IsSynchronizeXAxes = false;
            MouseLeave += endTrackingMouse;
            MouseMove += updateMouse;

            this.makePane = makePane;
            ContextMenuBuilder += addContextMenuItems;
            initVirtualCallInConstructor();

            MasterPane.PaneList.Clear();
            panes[MAIN] = makePane(this);
            MasterPane.PaneList.Add(mainPane());
        }

        protected const string MAIN = "**MAIN**";

        protected delegate QGraphPane PaneGenerator(QGraphControl parent);

        protected void initVirtualCallInConstructor() {
            DoubleBuffered = true;
        }

        protected void endTrackingMouse(object chart, EventArgs e) {
            Objects.eachValue(panes, pane => pane.removeCrosshair());
        }

        protected void updateMouse(object chart, EventArgs e) {
            var me = (MouseEventArgs) e;
            Objects.eachValue(panes, pane => pane.updateCrosshair(me.X, me.Y));
        }

        protected void addContextMenuItems( ZedGraphControl control, ContextMenuStrip menuStrip, Point mousePt, ContextMenuObjectState objState ) {   
            var legend = new ToolStripMenuItem {Name = "ShowLegend", Tag = "ShowLegend", Text = "Show Legend", Checked = showLegend};
            legend.Click += (s, e) => { showLegend = !showLegend; Objects.eachValue(panes, pane => {pane.Legend.IsVisible = showLegend;}); Invalidate();};
            menuStrip.Items.Add( legend );
            var crosshair = new ToolStripMenuItem {Name = "ShowCrosshair", Tag = "ShowCrosshair", Text = "Show Crosshair", Checked = showCrosshair_};
            crosshair.Click += (s, e) => { showCrosshair_ = !showCrosshair_; Invalidate();};
            menuStrip.Items.Add( crosshair );
        }

        public bool showCrosshair() {
            return showCrosshair_;
        }

        protected void makeSubPane(string paneName) {
            var pane = makePane(this);
            panes[paneName] = pane;
            MasterPane.Add(pane);
            var paneCount = MasterPane.PaneList.Count;
            using (var g = CreateGraphics()) MasterPane.SetLayout(g, true, ones(paneCount), proportions(paneCount));
            Resize += (s, e) => {
                          RectangleF main;
                          using (var g = CreateGraphics()) main = panes[MAIN].CalcChartRect(g);
                          pane.Chart.Rect = new RectangleF(main.X, pane.Rect.Y + 1, main.Width, pane.Rect.Height - 2); 
                      };
        }

        static float[] proportions(int count) {
            var result = new float[count];
            Objects.zeroTo(count, i => result[i] = 1);
            result[0] = 3;
            return result;
        }

        static int[] ones(int count) {
            var result = new int[count];
            Objects.zeroTo(count, i => result[i] = 1);
            return result;
        }

        protected QGraphPane pane(string paneName) {
            if (!panes.ContainsKey(paneName)) makeSubPane(paneName);
            return panes[paneName];
        }

        protected QGraphPane mainPane() {
            return pane(MAIN);
        }

    }
}