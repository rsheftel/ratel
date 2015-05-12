using System.Collections.Generic;
using System.Drawing;
using Q.Research;
using Q.Trading;
using Q.Util;
using ZedGraph;

namespace Gui.Norad {
    public class TradePathHistogramControl : QGraphControl, FilteredPositionsPlot {
        public TradePathHistogramControl(Researcher researcher, bool isAverage, bool toEnd) : base(parent => new TradePathHistogramPane(researcher, parent, isAverage, toEnd)) {
            updatePlot(researcher.positions);
        }

        public void updatePlot(IEnumerable<Position> positions) {
            ((TradePathHistogramPane) mainPane()).doPlot(positions);
            RestoreScale(GraphPane);
        }
    }

    public class HistogramPane : QGraphPane {
        protected HistogramPane(QGraphControl parent) : base(parent) {
            Legend.IsVisible = false;            
        }

        protected void addBars(string name, Color[] colors, PointPairList pointsWithZColor) {
            var bars = AddBar(name, pointsWithZColor, Color.Empty);
            bars.Bar.Fill = new Fill( colors ) {
                Type = FillType.GradientByColorValue,
                SecondaryValueGradientColor = Color.Empty,
            };
            return;
        }
    }

    public class TradePathHistogramPane : HistogramPane {
        readonly Researcher researcher;
        readonly bool isAverage;
        readonly bool toEnd;

        public TradePathHistogramPane(Researcher researcher, QGraphControl parent, bool isAverage, bool toEnd) : base(parent) {
            this.researcher = researcher;
            this.isAverage = isAverage;
            this.toEnd = toEnd;
        }

        public void doPlot(IEnumerable<Position> positions) {
            CurveList.Clear();
            var infos = Objects.list(Objects.convert(positions, position => researcher.positionInfo(position)));
            var totalBars = (int) Objects.max(Objects.convert(infos, pnls => (double) pnls.count()));
            debug("total bars: " + totalBars);
            var unconditional = new List<double>(Objects.nCopies(totalBars, 0.0));
            var conditional = new List<double>(Objects.nCopies(totalBars, 0.0));
            var count = new List<int>(Objects.nCopies(totalBars, 0));
            Objects.each(infos, pnls => Objects.zeroTo(totalBars, barNum => {
                                                          unconditional[barNum] += pnls.pnl(barNum);
                                                          conditional[barNum] += toEnd ? pnls.pnlFrom(barNum) : pnls.pnlTo(barNum);
                                                          if(barNum < pnls.count())
                                                              count[barNum]++;
                                                          debug("barNum = " + barNum + ", pnls.count() - 1 = " + (pnls.count() - 1) + ", count[barNum] = " + count[barNum]);
                                                      }));
            debug("unconditional: " + Objects.toShortString(unconditional));
            debug("conditional: " + Objects.toShortString(conditional));
            debug("count: " + Objects.toShortString(count));
            if(isAverage) {
                Objects.eachIt(count, (i, c) => unconditional[i] /= c);
                Objects.eachIt(count, (i, c) => conditional[i] /= toEnd ? c : infos.Count);
                debug("aa unconditional: " + Objects.toShortString(unconditional));
                debug("aa conditional: " + Objects.toShortString(conditional));
            }

            addBars("conditional", new[] {Color.Red, Color.Lime}, points(conditional));
            addBars("unconditional", new[] {Color.DarkRed, Color.DarkGreen}, points(unconditional));
        }

        void debug(string message) {
            if(!toEnd && isAverage && toEnd)
                LogC.debug(message);
        }

        static PointPairList points(IEnumerable<double> values) {
            var result = new PointPairList();
            Objects.eachIt(values, (i, pnl) => result.Add(i + 1, pnl, pnl < 0 ? 0 : 1));
            return result;
        }
    }
}