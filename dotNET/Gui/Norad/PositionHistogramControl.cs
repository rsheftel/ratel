using System;
using System.Collections.Generic;
using System.Drawing;
using Q.Research;
using Q.Trading;
using ZedGraph;
using O=Q.Util.Objects;
using System.Drawing.Drawing2D;

namespace Gui.Norad {
    public class PositionHistogramControl : QGraphControl, FilteredPositionsPlot {
        public PositionHistogramControl(Researcher researcher, Converter<Position, double> toValue) : base(parent => new PositionHistogramPane(parent, toValue)) {
            updatePlot(researcher.positions);
        }

        public void updatePlot(IEnumerable<Position> positions) {
            ((PositionHistogramPane) mainPane()).doPlot(positions);
            RestoreScale(GraphPane);
        }
    }

    public class PositionHistogramPane : HistogramPane {
        readonly Converter<Position, double> toValue;

        public PositionHistogramPane(QGraphControl parent, Converter<Position, double> toValue) : base(parent) {
            this.toValue = toValue;
        }

        public void doPlot(IEnumerable<Position> positions) {
            clear();
            var closed = O.accept(positions, position => position.isClosed());
            var pnls = O.sort(O.convert(closed, toValue));
            var min = O.first(pnls);
            var max = O.last(pnls);
            var range = max - min;
            var bucketCount = Math.Max(Math.Min(150, pnls.Count / 5), 1);
            var blockSize = Math.Max(1, range / bucketCount);
            if(blockSize == 1) bucketCount = (int) range + 1;
            var buckets = O.array(O.nCopies(bucketCount, 0.0));
            O.each(pnls, pnl => {
                             var bucket = (int) ((pnl - min) / blockSize);
                             if (bucket == bucketCount) bucket--;
                             buckets[bucket]++;
                         });
            var bucketStarts = O.array(O.convert(buckets, (i, unused) => i * blockSize + min));
            var pnlCount = O.count(pnls);
            var lowerQuartile = pnls[pnlCount / 4];
            var upperQuartile = pnls[3 * pnlCount / 4];
            var median = pnls[pnlCount / 2];
            var mean = O.average(pnls);
            var sd = O.populationStandardDeviation(pnls);
            var colors = O.array(O.convert(bucketStarts, (i, bucket) => {
                                                             var isLow = bucket < median;
                                                             var isBright = bucket <= lowerQuartile || bucket >= upperQuartile;
                                                             if (isLow) return isBright ? (2.0/3.0) : 0.0;
                                                             return isBright ? 1.0 : (1.0/3.0);
                                                         }));
            var points = new PointPairList(bucketStarts, buckets, colors);
            addBars("pnls", O.array(Color.DarkRed, Color.FromArgb(0, 18, 110), Color.Red, Color.FromArgb(0, 60, 255)), points);
            
            GraphObjList.Add(new LineObj(Color.Lime, 0, 0, 0, 1) {
                Location = {CoordinateFrame = CoordType.XScaleYChartFraction},
                Line = {Style = DashStyle.Dot, Width = 2}
            });
            GraphObjList.Add(new TextObj(O.join("\n", O.list(
                "min = " + O.prettyNumber(min),
                "25% = " + O.prettyNumber(lowerQuartile),
                "50% = " + O.prettyNumber(median),
                "75% = " + O.prettyNumber(upperQuartile),
                "max = " + O.prettyNumber(max), "",
                "mean = " + O.prettyNumber(mean),
                "sd(pop) = " + O.prettyNumber(sd)
                )), 0.9, 0.12, CoordType.ChartFraction) {FontSpec = {StringAlignment = StringAlignment.Near}});
            BarSettings.MinClusterGap = 0;
            XAxis.Scale.MinGrace = 0;
            XAxis.Scale.MaxGrace = 0;
        }

        void clear() {
            CurveList.Clear();
            GraphObjList.Clear();
        }

    }
}