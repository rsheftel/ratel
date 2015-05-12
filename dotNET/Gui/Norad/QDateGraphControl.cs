using System;
using System.Collections.Generic;
using System.Windows.Forms;
using Q.Util;
using ZedGraph;

namespace Gui.Norad {
    public class QDateGraphControl : QGraphControl  {      
        Dictionary<DateTime, double> dateIndices;
        Dictionary<double, DateTime> indexDates;

        protected QDateGraphControl(PaneGenerator makePane) : base(makePane) {
            IsShowHScrollBar = true;
            IsEnableHPan = true;

            IsEnableHZoom = true;
            IsZoomOnMouseCenter = true;
            ZoomButtons = MouseButtons.Left;
            ZoomModifierKeys = Keys.Control;
            PanButtons = MouseButtons.Left;
            PanModifierKeys = Keys.None;

            IsSynchronizeXAxes = true;
            ScrollProgressEvent += scrollProgress;
            ZoomEvent += zoomed;
            PointValueEvent += tooltip;

            Resize += (s, e) => {if(hasValidated) return; hasValidated = true; resetYAxis(); Invalidate(); };
        }

        string tooltip(ZedGraphControl sender, GraphPane gp, CurveItem curve, int iPt) {
            var index = curve is JapaneseCandleStickItem ? iPt : (int) curve.Points[iPt].X - 1;
            if (index < 0 || index > mainPaneSpecific().maxIndex()) return "";
            var result = mainPaneSpecific().tooltip(index + 1);
            eachPane((name, pane) => { if (!name.Equals(MAIN)) result += "\n" + pane.tooltip(index + 1); });
            return result.Trim();
        }

        void eachPane(Action<string, QDateGraphPane> onPane) {
            Objects.each(panes, (name, pane) => onPane(name, (QDateGraphPane) pane));
        }

        QDateGraphPane mainPaneSpecific() {
            return (QDateGraphPane) panes[MAIN];
        }

        void zoomed(ZedGraphControl sender, ZoomState oldState, ZoomState newState) {
            resetYAxis();
        }

        public void resetYAxis() {
            eachPane((name, pane) => pane.resetYAxis());
            AxisChange();
            Refresh();
        }

        void scrollProgress(ZedGraphControl master, ScrollBar scrollBar, ZoomState oldState, ZoomState newState) {
            resetYAxis();
        }

        public void setDateIndices(Dictionary<DateTime, double> newDateIndices, QDateGraphPane pane) {
            if(dateIndices != null) return;  // only set once - can't bomb, unfortunately
            dateIndices = newDateIndices;
            indexDates = Objects.invert(dateIndices);
        }

        public IEnumerable<DateTime> dates() {
            return dateIndices.Keys;
        }

        public DateTime dateAt(int i) {
            return Bomb.missing(indexDates, i);
        }

        public int maxIndex() {
            return indexDates.Count - 1;
        }

        public void setDate(DateTime time, int index) {
            dateIndices[time] = index;
            indexDates[index] = time;
        }

        public double index(DateTime time) {
            return Bomb.missing(dateIndices, time);
        }
    }
}