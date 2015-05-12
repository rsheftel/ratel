using System;
using System.Collections.Generic;
using System.Data;
using System.Windows.Threading;
using Q.Research;
using Q.Simulator;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;
using Bar=Q.Trading.Bar;

namespace Gui.Norad {
    public class BarSpudGraph : QDateGraphControl  {
        readonly BarSpud barSpud;
        QDataTable dataTable_;
        const string EQUITY = "EQUITY";

        public BarSpudGraph(BarSpud bars, Collectible collectible, DispatcherObject dispatchTo) : base(parent => new BarSpudPane(bars, collectible, (BarSpudGraph) parent, dispatchTo)) {
            barSpud = bars;
            MasterPane.InnerPaneGap = 0;
            MasterPane.Margin.All = 0;
            addBars();
        }

        private void addBars() {
            spudPane(MAIN).addBars();
            ScrollMinX = -5;
            ScrollMaxX = barSpud.count() + 6;
            resetYAxis();
            Invalidate();
        }

        public void add(PlotDefinition plot) {
            plot.requireManagerMatches(barSpud);
            LogC.info("adding plot " + plot);
            pane(plot).add(plot);
            resetYAxis();
            Invalidate();
        }

        BarSpudPane pane(PlotDefinition plot) {
            var paneName = plot.pane ?? MAIN;
            return spudPane(paneName);
        }

        public void add(List<Position> positions, List<Trade> trades, Simulator simulator) {
            spudPane(MAIN).add(positions, trades, simulator);
            Invalidate();
        }

        BarSpudPane spudPane(string paneName) {
            return (BarSpudPane) pane(paneName);
        }

        public void moveBarsToBack() {
            eachPane(pane => pane.moveBarsToBack());
        }

        void eachPane(Action<BarSpudPane> onPane) {
            Objects.eachValue(panes, pane => onPane((BarSpudPane) pane));
        }

        public void addEquity(Simulator simulator) {
            makeSubPane(EQUITY);
            spudPane(EQUITY).addEquity(simulator);
        }

        public QDataTable makeDataTable() {
            var columns = Objects.list("open", "high", "low", "close");
            eachPane(pane => columns.AddRange(pane.names));
            columns.Add("equity");
            Bomb.unlessNull(dataTable_, () => "lazy early? wierd");
            dataTable_ = new QDataTable {
                {"time", typeof (DateTime)}
            };
            Objects.each(columns, c => dataTable_.Add(c, typeof (double)));
            Objects.eachIt(barSpud, addRow);
            return dataTable_;
        }

        void addRow(int barIndex, Bar bar) {
            var row = dataTable_.NewRow();
            addBarTo(row, bar);
            eachPane(pane => Objects.each(pane.spuds, pane.names, (spud, name) => {
                                                                if(spud.count() > barIndex) 
                                                                    addSpudTo(row, name, spud[barIndex]);
                                                            }));
            var equity = spudPane(EQUITY).equity;
            addEquityTo(row, equity[equity.Count - barIndex - 1]);
            dataTable_.add(row);
        }

        public static void addSpudTo(DataRow row, string name, double value) {
            row[name] = value;
        }

        public static void addEquityTo(DataRow row, double equity) {
            row["equity"] = equity;
        }

        public static void addBarTo(DataRow row, Bar bar) {
            row["time"] = bar.time;
            row["open"] = bar.open;
            row["high"] = bar.high;
            row["low"] = bar.low;
            row["close"] = bar.close;
        }

        public QDataTable dataTable() {
            return dataTable_;
        }
    }
}