using System;
using System.Collections.Generic;
using Q.Util;
using ZedGraph;

namespace Gui.Norad {
    public class QDateGraphPane : QGraphPane {
        protected readonly List<Graphable> graphables = new List<Graphable>();
        protected readonly QDateGraphControl dateParent;

        protected QDateGraphPane(QGraphControl parent, Dictionary<DateTime, double> dateIndices) : base(parent) {
            dateParent = (QDateGraphControl) parent;
            dateParent.setDateIndices(dateIndices, this);
            XAxis.Type = AxisType.DateAsOrdinal;
            XAxis.Scale.Format = "yyyy/MM/dd";
        }

        public virtual void resetYAxis() {
            var range = new util.Range(Objects.jDate(scaleMinTime()), Objects.jDate(scaleMaxTime()));
            var dates = Objects.accept(dateParent.dates(), d => range.containsInclusive(Objects.jDate(d)));
            double[] high = {double.MinValue};
            double[] low = {double.MaxValue};
            Objects.each(dates, d => Objects.each(graphables, graphable => {
                                                                  if(!graphable.hasValue(d)) return;
                                                                  Objects.makeMax(ref high[0], graphable.maxValue(d));
                                                                  Objects.makeMin(ref low[0], graphable.minValue(d));
                                                              }));

            var extra = (high[0] - low[0]) * 0.18;
            if (extra == 0.0) return; 
            YAxis.Scale.Min = low[0] - extra;
            YAxis.Scale.Max = high[0] + extra;
            Y2Axis.Scale.Min = low[0] - extra;
            Y2Axis.Scale.Max = high[0] + extra;
            AxisChange();
        }

        protected DateTime scaleMaxTime() {
            var endIndex = Math.Min(maxIndex(), Math.Max(0, (int) XAxis.Scale.Max));
            return dateParent.dateAt(endIndex + 1);
        }

        protected DateTime scaleMinTime() {
            var startIndex = Math.Min(maxIndex(), Math.Max(0, (int) XAxis.Scale.Min - 1));
            return dateParent.dateAt(startIndex + 1);
        }

        public string tooltip(int index) {
            return Objects.join("\n", Objects.convert(graphables, graphable => graphable.tooltip(dateParent.dateAt(index))));
        }

        public int maxIndex() {
            return dateParent.maxIndex();
        }
    }

    public interface Graphable {
        bool hasValue(DateTime time);
        double minValue(DateTime time);
        double maxValue(DateTime time);
        string tooltip(DateTime time);
    }
}