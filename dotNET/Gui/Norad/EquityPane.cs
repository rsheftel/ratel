using System;
using System.Collections.Generic;
using System.Drawing;
using Q.Simulator;
using Q.Trading.Results;
using Q.Util;
using ZedGraph;

namespace Gui.Norad {
    public class EquityPane : QDateGraphPane {
        public EquityPane(QGraphControl parent, Simulator simulator) :base(parent, dates(simulator.allCollector())) {
            XAxis.MajorGrid.IsVisible = true;
            YAxis.MajorGrid.IsVisible = true;
            XAxis.MajorGrid.Color = Color.Gray;
            YAxis.MajorGrid.Color = Color.Gray;
        }

        static Dictionary<DateTime, double> dates(StatisticsCollector all) {
            var c  = 1.0;
            return Objects.dictionary(all.dates(), d => c++);
        }

        void addCurve(StatisticsCollector collector, int divisor, string name, Color color, bool useDates) {
            var equity = Objects.array(Objects.convert(Objects.cumulativeSum(collector.pnl()), p => p / divisor));
            var dates = Objects.array(Objects.convert(collector.dates(), d => useDates ? new XDate(d).XLDate : dateParent.index(d)));
            var line = AddCurve(name, dates, equity, color, SymbolType.None);
            graphables.Add(new EquityGraphable(name, equity, collector.dates()));
            if(!useDates)
                line.IsOverrideOrdinal = true;
        }

        public void addPlots(Simulator simulator) {            
            var all = simulator.allCollector();
            var numMarkets = simulator.symbols.Count;
            addCurve(all, numMarkets, "ALL", Color.Black, true);

            var divisor = Math.Max(7, numMarkets + 2 + ((numMarkets + 1) % 2));
            const int maxColor = 16777216;
            var spread =maxColor / divisor;
            var colors = new uint[numMarkets];
            const uint alpha = 4278190080u;
            Objects.zeroTo(numMarkets, i => colors[i] = alpha + (uint) ((i + 1) * spread));
            Objects.eachIt(simulator.symbols, (i, symbol) => addCurve(simulator.collector(symbol), 1, symbol.name, Color.FromArgb(((int) (colors[i] + int.MinValue))), false));
        }
    }
}