using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using System;

namespace Q.Systems {
    public abstract class TrendingMABase : SymbolSystem {
        internal Spud<double> ma;
        internal BollingerBand upperBand;
        internal BollingerBand lowerBand;
        protected Sum shortSum;
        protected int maDays;
        protected double riskDollars;
        protected AverageTrueRange atr;
        protected internal Spud<double> signal;

        protected TrendingMABase(QREBridgeBase bridge, Symbol symbol, Converter<BarSpud, Spud<double>> signalSeries) : base(bridge, symbol) {
            maDays = parameter<int>("MADays");
            riskDollars = parameter<double>("RiskDollars");
            atr = new AverageTrueRange(bars, parameter<int>("ATRLen"));
            signal = signalSeries(bars);
            ma = new Average(signal, maDays);
            shortSum = new Sum(signal, maDays-1);
            var numDeviations = parameter<double>("BollingerBandDeviations");
            var barsBack = parameter<int>("BollingerBandBarsBack");
            upperBand = new BollingerBand(ma, barsBack, numDeviations);
            lowerBand = new BollingerBand(ma, barsBack, -numDeviations);
            
        }
        
        double stopPoints() {
            return parameter<int>("nATR") * atr;
        }

        protected long tradeSize() {
            var tradeSize = (long) (riskDollars / (stopPoints() * bigPointValue()));
            return tradeSize < 1 ? 1 : tradeSize;
        }
        
        protected string lOrS() {
            return position().lOrS();
        }

        protected abstract void placeEntries();

        protected abstract void placeExits();

    }
}