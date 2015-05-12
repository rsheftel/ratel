using System;
using System.Collections.Generic;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Util;

namespace Q.Systems.PairSystems {
    public class CommodityCarry : PairSystem {
        internal readonly SymbolSpud<Bar> leftPrior;
        internal readonly SymbolSpud<Bar> rightPrior;
        readonly Minus spread;
        readonly StdDeviationOfSample vol;
        readonly double cutoff;
        public readonly RootSpud<double> payoutRatio;

        public CommodityCarry(QREBridgeBase bridge, Pair pair) : base(bridge, pair) {
            leftPrior = prior(pair.left).bars(bars[pair.left]);
            rightPrior = prior(pair.right).bars(bars[pair.right]);
            spread = new Minus(bars[pair.left].close, bars[pair.right].close);
            vol = new StdDeviationOfSample(spread, parameter<int>("volWindow"));
            cutoff = parameter<double>("payoutRatioCutoff");
            payoutRatio = new RootSpud<double>(bridge.manager);
        }

        internal static Symbol prior(Symbol symbol) {
            var numString = penultimate(symbol.name.Split('.', 'C'));
            int number;
            try {
                number = int.Parse(numString);
            } catch(Exception e) {
                throw Bomb.toss("unable to parse " + numString + " as int", e);
            }
            Bomb.unless(number > 1, () => "no prior of contract <= 1.  contract number = " + number);
            return symbol.relatedSuffix((number-1) + "C");
        }

        public override DateTime onCloseTime() {
            return pair.closeTime();
        }

        protected override void onNewTick(Symbol symbol, Bar partialBar, Tick tick) {}
        protected override void onNewBar(Dictionary<Symbol, Bar> b) {}
        protected override void onFilled(Position position, Trade trade) {}
        protected override void onClose(Dictionary<Symbol, Bar> current) {
            var leftClose = current[pair.left].close;
            var leftCarry = (leftPrior[0].close - leftClose) / 3.0;
            var rightClose = current[pair.right].close;
            var rightCarry = (rightPrior[0].close - rightClose) / 3.0;
            payoutRatio.set((leftCarry - rightCarry) / (vol * Math.Sqrt(22)));

            var wantOut = Math.Abs(payoutRatio) <= cutoff;
            var wantLong = payoutRatio > 0;

            if (wantOut) {
                placeExits(leftClose, rightClose);
                return;
            }

            if(hasPosition() && wantLong == position(pair.left).direction().isLong()) return;
            
            placeExits(leftClose, rightClose);
            if (wantLong) 
                placeOrders(pair.buy("long payout achieved", limit(leftClose), limit(rightClose), 1000, oneBar()));
            else 
                placeOrders(pair.sell("short payout achieved", limit(leftClose), limit(rightClose), 1000, oneBar()));
        
        }

        void placeExits(double leftClose, double rightClose) {
            if (hasPosition()) 
                placeOrders(pair.exits(this, "exit " + position(pair.left).direction().longShort("long", "short") + " small payout ratio", limit(leftClose), limit(rightClose), oneBar()));
        }

        public void placeExits() {
            placeExits(bars[pair.left][0].close, bars[pair.right][0].close);
        }

        public override bool runOnClose() {
            return true;
        }

        public void enterTestMode() {
            leftPrior.enterTestMode();
            rightPrior.enterTestMode();
        }
    }
    
    public class CommodityCarryPairGenerator : PairGenerator {
        public CommodityCarryPairGenerator(SystemArguments arguments) : base(arguments) {}

        protected internal override IEnumerable<Pair> pairs() {
            var symbols = arguments.symbols;
            foreach(var over in seq(symbols.Count))
                foreach(var under in seq(symbols.Count - over - 1))
                    yield return new Pair(symbols[over], symbols[under + over + 1]);
        }
    }
}