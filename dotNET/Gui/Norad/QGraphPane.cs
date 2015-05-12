using System.Drawing;
using System.Drawing.Drawing2D;
using Q.Util;
using ZedGraph;

namespace Gui.Norad {
    public class QGraphPane : GraphPane {
        protected QGraphControl parent;
        LineObj xLine;
        LineObj yLine;

        protected QGraphPane(QGraphControl parent) {            
            this.parent = parent;
            Legend.IsVisible = true;
            Title.IsVisible = false;
            XAxis.Title.IsVisible = false;
            YAxis.Title.IsVisible = false;
            Y2Axis.Title.IsVisible = false;
            IsBoundedRanges = true;
            IsFontsScaled = false;
            
            XAxis.Scale.FontSpec.Size = 10;
            YAxis.Scale.FontSpec.Size = 10;
            Y2Axis.Scale.FontSpec.Size = 10;
            XAxis.Scale.FontSpec.IsBold = false;
            YAxis.Scale.FontSpec.IsBold = false;
            Y2Axis.Scale.FontSpec.IsBold = false;
            Y2Axis.IsVisible = true;
            XAxis.IsVisible = true;
            XAxis.Scale.IsVisible = true;
        }

        void addCrosshair(float x, float y) {
            Bomb.unlessNull(xLine, ()=> "addCrosshair called twice without removeCrossHair");
            Bomb.unlessNull(yLine, ()=> "addCrosshair called twice without removeCrossHair");
            var chartPoint = point(x, y);
            xLine = new LineObj(Color.Gray, chartPoint.X, 0, chartPoint.X, 1) {Location = {CoordinateFrame = CoordType.XScaleYChartFraction}};
            yLine = new LineObj(Color.Gray, 0, chartPoint.Y, 1, chartPoint.Y)  {Location = {CoordinateFrame = CoordType.XChartFractionYScale}};
            Objects.each(Objects.list(xLine, yLine), line => {
                                                         line.ZOrder = ZOrder.E_BehindCurves;
                                                         line.Line.Style = DashStyle.Dash;
                                                     });
            GraphObjList.Add(xLine);
            GraphObjList.Add(yLine);
        }

        public void updateCrosshair(float x, float y) {
            var updated = false;
            if(xLine != null) {
                removeCrosshair();
                updated = true;
            }
            if (parent.showCrosshair()) {
                addCrosshair(x, y);
                updated = true;
            }
            if(updated)
                parent.Invalidate();
        }

        public void removeCrosshair() {
            if (xLine == null) return;
            GraphObjList.Remove(xLine);
            GraphObjList.Remove(yLine);
            xLine = null;
            yLine = null;
        }

        PointF point(float x, float y) {
            double xZed, yZed;
            var apoint = new PointF(x, y);
            ReverseTransform(apoint, out xZed, out yZed);
            return new PointF((float) xZed, (float) yZed);
        }
    }
}