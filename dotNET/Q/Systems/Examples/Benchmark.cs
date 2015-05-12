using System;
using System.Drawing;
using Q.Spuds.Indicators;
using Q.Trading;

namespace Q.Systems.Examples {
    public class Benchmark : SymbolSystem {
        readonly BollingerBand bbLower;
        readonly BollingerBand bbUpper;


        public Benchmark(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            bbLower = new BollingerBand(bars.close, parameter<int>("LengthDn"), -parameter<int>("NumDevsDn"));
            bbUpper = new BollingerBand(bars.close, parameter<int>("LengthUp"), parameter<int>("NumDevsUp"));
            addToPlot(bbLower, "bbLower", Color.Blue);
            addToPlot(bbUpper, "bbUpper", Color.Blue);
        }

        protected override void onNewBar() {
            var beShort = bar.high < bbUpper[0] && bars[1].high > bbUpper[1];
            var beLong = bar.low > bbLower[0] && bars[1].low < bbLower[1];
            if (!beShort && !beLong) return;

            var desired = beShort ? Direction.SHORT : Direction.LONG;
            var myPositions = positions();
            if(myPositions.Count != 0 && desired.Equals(the(myPositions).direction())) return;
            var price = beShort ? bbUpper[0] : bbLower[0];
            each(myPositions, p => placeOrder(p.exit("Exit", market(), oneBar())));
            placeOrder(desired.order("Entry", symbol, stop(price), 100, oneBar()));
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            throw new NotImplementedException();
        }

        protected override void onClose() {}
        protected override void onFilled(Position position, Trade trade) {}
    }
}