using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Threading;
using Gui.Util;
using Q.Simulator;
using Q.Spuds.Core;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;
using ZedGraph;
using Bar=Q.Trading.Bar;

namespace Gui.Norad {
    public class BarSpudPane : QDateGraphPane {
        readonly BarSpud bars;
        readonly Collectible symbol;
        readonly BarSpudGraph parentTyped;
        readonly DispatcherObject dispatchTo;
        bool barsOnPlot;
        public readonly List<Spud<double>> spuds = new List<Spud<double>>();
        public readonly List<double> equity = new List<double>();
        public readonly List<string> names = new List<string>();

        public BarSpudPane(BarSpud bars, Collectible symbol, BarSpudGraph parent, DispatcherObject dispatchTo) : base(parent, dates(bars)) {
            this.bars = bars;
            this.symbol = symbol;
            parentTyped = parent;
            this.dispatchTo = dispatchTo;

            XAxis.Scale.Min = Math.Max(0, bars.count() - 100);
            XAxis.Scale.Max = bars.count() + 5;
            XAxis.Scale.IsVisible = false;
        }

        static Dictionary<DateTime, double> dates(Spud<Bar> bars) {
            var i = 1.0;
            var result = new Dictionary<DateTime, double>();
            Objects.each(bars.toArray(), bar => result[bar.time] = i++);
            return result;
        }


        public override void resetYAxis() {
            base.resetYAxis();
            var dataTable = parentTyped.dataTable();
            if (!barsOnPlot || dataTable == null) return;
            var maxTime = scaleMaxTime();
            // to be safe - don't want to miss last tick
            if(maxTime > dateParent.dateAt(dateParent.maxIndex() - 1)) maxTime = maxTime.AddDays(1);
            dataTable.filter<DateTime>("time", time => time.CompareTo(scaleMinTime()) >= 0 && time.CompareTo(maxTime) <= 0);
        }

        public void addBars() {
            var spl = new StockPointList();
            Objects.each(bars.toArray(), bar => spl.Add(stockPoint(bar)));

            var myCurve = AddJapaneseCandleStick(symbol.name, spl);
            myCurve.Stick.IsAutoSize = true;
            myCurve.Stick.Color = Color.Black;
            barsOnPlot = true;
            XAxis.Scale.IsVisible = true;
            var graphable = new BarSpudGraphable(bars);
            graphables.Add(graphable);

            var needsNewBar = false;
            bars.pushedDown += ()=> {
                                   needsNewBar = true;
                                   QControl.runOnGuiThread(dispatchTo, () =>
                                       parentTyped.dataTable().addAtStart(parentTyped.dataTable().NewRow())
                                       );
                               };
            bars.valueSet += bar => QControl.runOnGuiThread(dispatchTo, () => {
                                                                            if (needsNewBar) {
                                                                                spl.Add(stockPoint(bar));
                                                                                dateParent.setDate(bar.time, spl.Count);
                                                                            } else spl[spl.Count - 1] = stockPoint(bar);
                                                                            BarSpudGraph.addBarTo(Objects.first(parentTyped.dataTable().Rows), bar);
                                                                            needsNewBar = false;
                                                                            parentTyped.Invalidate();
                                                                        });
        }

        static StockPt stockPoint(Bar bar) {
            return new StockPt(new XDate(bar.time).XLDate, bar.high, bar.low, bar.open, bar.close, bar.volume);
        }

        public void add(PlotDefinition plot) {
            var spud = plot.spud;
            var count = spud.count();
            var xPoints = new double[count];
            var yPoints = new double[count];
            Objects.zeroTo(count, i => {
                                      if(i >= bars.count()) return; // spud has more data than bars
                                      xPoints[count - i - 1] = dateParent.index(bars[i].time);
                                      yPoints[count - i - 1] = spud[i];
                                  });
            var curve = AddCurve(plot.name, xPoints, yPoints, plot.color);
            curve.Symbol.IsVisible = false;
            curve.IsOverrideOrdinal = true;
            spuds.Add(spud);
            names.Add(plot.name);

            var needsNewPoint = false;
            spud.pushedDown += ()=> needsNewPoint = true;
            spud.valueSet += value => QControl.runOnGuiThread(dispatchTo, () => {
                                                                              var lastPoint = curve[curve.Points.Count-1];
                                                                              if (needsNewPoint) curve.AddPoint(lastPoint.X + 1, value);
                                                                              else lastPoint.Y = value;
                                                                              BarSpudGraph.addSpudTo(Objects.first(parentTyped.dataTable().Rows), plot.name, value);
                                                                              needsNewPoint = false;
                                                                              parentTyped.Invalidate();
                                                                          });
            graphables.Add(new DoubleSpudGraphable(plot.name, spud, bars));
        }

        public void add(List<Position> positions, List<Trade> trades, Simulator simulator) {
            Action<Trade, double> addTrade = (trade, x) => {
                                                 var y = trade.price;
                                                 var color = trade.direction.longShort(Color.Green, Color.Red);
                                                 var line = AddCurve("", new PointPairList {{x,y}}, color, SymbolType.Circle);
                                                 line.Symbol.Fill = new Fill(color);
                                                 line.Label.IsVisible = false;
                                                 line.Line.IsVisible = false;
                                                 line.IsOverrideOrdinal = true;
                                                 var arrow = new ArrowObj(x, trade.direction.longShort(0.93, 0.07), x, trade.direction.longShort(0.89, 0.11)) {
                                                     Location = {CoordinateFrame = CoordType.XScaleYChartFraction},
                                                     ZOrder = ZOrder.A_InFront
                                                 };
                                                 GraphObjList.Add(arrow);
                                                 var dottedLine = new LineObj(Color.LightGray, x, 0.11, x, 0.89) {
                                                     ZOrder = ZOrder.E_BehindCurves,
                                                     Line = {Style = DashStyle.Dot},
                                                     Location = {CoordinateFrame = CoordType.XScaleYChartFraction}
                                                 };
                                                 GraphObjList.Add(dottedLine);
                                                 var text = new TextObj(
                                                     trade.description + "\n" + trade.size + "@" + trade.price.ToString("n4"), 
                                                     x, trade.direction.longShort(0.935, 0.065), 
                                                     CoordType.XScaleYChartFraction, 
                                                     AlignH.Center, 
                                                     trade.direction.longShort(AlignV.Top, AlignV.Bottom)
                                                     ) {
                                                         FontSpec = {Size = 8, Border = {IsVisible = false}}
                                                     };
                                                 if (Objects.username().Equals("rsheftel")) {
                                                     text.FontSpec.FontColor = Color.Fuchsia;
                                                     text.FontSpec.Size = 11;
                                                 }
                                                 GraphObjList.Add(text);
                                             };
            Objects.each(trades, trade => addTrade(trade, dateParent.index(trade.time)));

            Action<Position> addPositionMaybe = position => {
                                                    if(!position.isClosed()) return;
                                                    var pnl = position.pnl(true, simulator.runInNativeCurrency());
                                                    var color = pnl > 0 ? Color.Green : (pnl < 0 ? Color.Red : Color.Black);
                                                    var line = AddCurve("", new PointPairList {{dateParent.index(position.entry().time), position.entry().price}, {dateParent.index(position.exitTrade().time), position.exitTrade().price}}, color);
                                                    line.Symbol.IsVisible = false;
                                                    line.Line.Style = DashStyle.Dot;
                                                    line.Line.Width = 2;
                                                    line.Label.IsVisible = false;
                                                    line.IsOverrideOrdinal = true;
                                                };
            Objects.each(positions, addPositionMaybe);
            
            simulator.addNewTradeListener((position, trade) => {
                                              if(!position.symbol.Equals(symbol)) return;
                                              addTrade(trade, bars.count());
                                              addPositionMaybe(position);
                                          });
        }

        public void moveBarsToBack() {
            CurveList.Move(0, 999);
            var points = new PointPairList();
            Objects.each(Objects.sort(dateParent.dates()), time => points.Add(new XDate(time).XLDate, 0.0));
            var line = new LineItem("", points, Color.Black, SymbolType.None) {
                Line = {IsVisible = false},
                Symbol = {IsVisible = false},
                Label = {IsVisible = false}
            };
            CurveList.Insert(0, line);
        }

        public void addEquity(Simulator simulator) {
            Bomb.when(Objects.hasContent(equity), () => "equity has content already: " + Objects.toShortString(equity));
            equity.AddRange(simulator.equity(symbol));
            var points = new PointPairList();
            Objects.eachIt(simulator.equity(symbol), (i, value) => points.Add(i + 1, value));
            var curve = AddCurve("equity", points, Color.Red);
            curve.Symbol.IsVisible = false;
            curve.IsOverrideOrdinal = true;
            var zero = new LineObj(Color.Gray, 1, 0, bars.count(), 0) {Line = {Style = DashStyle.Dash}, Location = {CoordinateFrame = CoordType.AxisXYScale}};
            GraphObjList.Add(zero);
            AxisChange();
            var graphable = new EquityGraphable("equity", simulator.equity(symbol), simulator.dates(symbol));
            graphables.Add(graphable);

            var needsNewPoint = false;
            bars.pushedDown += ()=> needsNewPoint = true;
            bars.valueSet += bar => { 
                                 var value = simulator.pnl(symbol);
                                 var todaysMarketPnl = Objects.sum(Objects.convert(simulator.positions(symbol), position => position.pnl(bars[1].close, bars[0].close)));
                                 value += todaysMarketPnl;
                                 graphable.add(bar.time, value);
                                 QControl.runOnGuiThread(dispatchTo, () => {
                                                                         var lastPoint = curve[curve.Points.Count-1];
                                                                         if (needsNewPoint) {
                                                                             curve.AddPoint(lastPoint.X + 1, value);
                                                                             equity.Add(value);
                                                                         } else {
                                                                             lastPoint.Y = value;
                                                                             equity[equity.Count - 1] = value;
                                                                         }
                                                                         BarSpudGraph.addEquityTo(Objects.first(parentTyped.dataTable().Rows), value);
                                                                         needsNewPoint = false;
                                                                         parentTyped.Invalidate();
                                                                     }); 
                             };
        }
    }

    public class EquityGraphable : Graphable {
        readonly string name;
        readonly List<double> equity;
        readonly Dictionary<DateTime, int> timeIndices;
        

        public EquityGraphable(string name, IEnumerable<double> equity, IEnumerable<DateTime> times) {
            this.name = name;
            this.equity = Objects.list(equity);
            timeIndices = new Dictionary<DateTime, int>();
            Objects.eachIt(times, (i, value) => timeIndices[value] = i);
        }

        public bool hasValue(DateTime time) {
            return timeIndices.ContainsKey(time);
        }

        public double minValue(DateTime time) {
            return value(time);
        }

        public double maxValue(DateTime time) {
            return value(time);
        }

        double value(DateTime time) {
            return equity[timeIndices[time]];
        }

        public string tooltip(DateTime time) {
            var tip = hasValue(time) ? "" + value(time) : "<no data>";
            return name + " = " + tip;
        }

        public void add(DateTime time, double value) {
            timeIndices[time] = equity.Count;
            equity.Add(value);
        }
    }

    public class DoubleSpudGraphable : Graphable {
        readonly string name;
        readonly Spud<double> doubles;
        Dictionary<DateTime, int> timeIndices;

        public DoubleSpudGraphable(string name, Spud<double> doubles, BarSpud bars) {
            this.name = name;
            this.doubles = doubles;
            timeIndices = bars.timeLookup();
            bars.valueSet += bar => timeIndices[bar.time] = 0;
            bars.pushedDown += () => timeIndices = bars.timeLookup();
        }

        public bool hasValue(DateTime time) {
            return timeIndices.ContainsKey(time) && doubles.count() > timeIndices[time];
        }

        public double minValue(DateTime time) {
            return value(time);
        }

        public double maxValue(DateTime time) {
            return value(time);
        }

        public string tooltip(DateTime time) {
            return name + " = " + Objects.prettyNumber(value(time));
        }

        double value(DateTime time) {
            return doubles[timeIndices[time]];
        }
    }

    public class BarSpudGraphable : Graphable {
        readonly BarSpud bars;
        Dictionary<DateTime, int> timeIndices;

        public BarSpudGraphable(BarSpud bars) {
            this.bars = bars;
            timeIndices = bars.timeLookup();
            bars.valueSet += bar => timeIndices[bar.time] = 0;
            bars.pushedDown += () => timeIndices = bars.timeLookup();
        }

        public bool hasValue(DateTime time) {
            return timeIndices.ContainsKey(time);
        }

        public double minValue(DateTime time) {
            return bars[timeIndices[time]].low;
        }

        public double maxValue(DateTime time) {
            return bars[timeIndices[time]].high;
        }

        public string tooltip(DateTime time) {
            var bar = bars[timeIndices[time]];
            return Objects.join("\n", Objects.list(
                "time = " + Objects.ymdHuman(bar.time),
                "open = " + bar.open.ToString("n7"),
                "high = " + bar.high.ToString("n7"),
                "low = " + bar.low.ToString("n7"),
                "close = " + bar.close.ToString("n7")
                ));
        }
    }
}