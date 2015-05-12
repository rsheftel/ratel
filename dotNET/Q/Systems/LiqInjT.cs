using System;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;

namespace Q.Systems {
    public class LiqInjT : SymbolSystem {
        readonly EWAcf acf;
        public readonly SymbolSpud<double> zScore;
        public readonly SymbolSpud<double> residual;
        public readonly SymbolSpud<double> tc;        
        public readonly SymbolSpud<double> beta;
        public readonly SymbolSpud<double> betaShort;
        public bool isLongSignal;
        public bool isShortSignal;
        readonly double zScoreMin;
        readonly double pScoreMin;
        public bool isTriggered;
        public bool isValidFit;        
        public bool isValidAcf;
        readonly double acfTrigger;     
        readonly long risk;
        readonly double betaMin;
        readonly double betaMax;
        
        public bool isConfirmed;
        public bool isAccepted;        
        readonly double stopMultiple;
        readonly double targetNetProfit;

        public LiqInjT(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            beta = symbol.relatedPrefix("LIB" + "10").doubles(bars);
            betaShort = symbol.relatedPrefix("LIO" + "10").doubles(bars);
            residual = symbol.relatedPrefix("LIV" + "10").doubles(bars);            
            zScore = symbol.relatedPrefix("LIZ" + "10").doubles(bars, 0);
            tc = symbol.relatedPrefix("LIC" + "10").doublesNoLive(bars);
            acf = new EWAcf(zScore, parameter<double>("acfHalfLife"), parameter<int>("acfLag"));

            zScoreMin = parameter<double>("zScoreMin");
            pScoreMin = parameter<double>("pScoreMin");
            betaMin = parameter<double>("betaMin");
            betaMax = parameter<double>("betaMax");
            acfTrigger = parameter<double>("acfTrigger");
            risk = parameter<long>("risk");
            stopMultiple = parameter<double>("stopMultiple");
            targetNetProfit = stopMultiple * risk;
        }

        protected override void onFilled(Position position, Trade trade) {}

        protected override void onNewBar() {}

        void doTrades() {                        
            setFlags();
            if (hasPosition()) {                
                checkStopLoss();
                checkInvalidFit();
                checkTargetReached();
                checkOppositeSignal();
                checkBarsHeld();
            }
            placeEntries();
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            return;            
        }

        protected override void onClose() {
            doTrades();            
        }

        void checkStopLoss() {
            if (openPositionPnl() >= -risk) return;
            exit("Stop Loss");            
            deactivateAndStop(() => Math.Sign(zScore) != Math.Sign(zScore[1]) && Math.Abs(zScore - zScore[1]) > 1);
        }

        public override bool runOnClose() {
            return true;
        }
       
        void checkBarsHeld() {
            if (position().barsHeld() < parameter<int>("maxBarInTrade")) return;
            exit("Bar Limit");
        }

        void checkOppositeSignal() {
            if(oppositeSignal())
                exit("Exit " + position().longShort("Long", "Short") + " On Signal");
        }

        void checkTargetReached() {            
            if(openPositionPnl() > targetNetProfit && (!isTriggered || !isValidAcf)) 
                exit("Exit " + position().longShort("Long", "Short") + " On Target");
        }

        void exit(string description) {
            placeOrder(position().exit(description, market(), oneBar()));
        }

        void checkInvalidFit() {
            if (!isValidFit) exit("Invalid Hedge");
        }
           
        void placeEntries() {
            if(!isAccepted) return;
            if(isLongSignal && !isLong()) placeOrder(symbol.buy("Entry Long", market(), tradeSize(), oneBar()));
            if(isShortSignal && !isShort()) placeOrder(symbol.sell("Entry Short", market(), tradeSize(), oneBar()));
        }

        void setFlags() {
            isLongSignal = zScore < -zScoreMin;
            isShortSignal = zScore > zScoreMin;
            var pScore = Math.Abs(residual) / tc;            
            isTriggered = pScore > pScoreMin && (isLongSignal || isShortSignal);
            isValidFit = isValidBeta(beta,betaShort,betaMin, betaMax);
            isValidAcf = acf <= acfTrigger;            
            isConfirmed = isValidFit && isTriggered && isValidAcf && tradeSize() > 0;
            isAccepted = isConfirmed && (noPosition() || oppositeSignal());
        }

        bool oppositeSignal() {
            return position().longShort(isShortSignal, isLongSignal);
        }

        internal static bool isValidBeta(double betaActual,double betaShort, double betaMin, double betaMax) {
            return !(Math.Min(betaActual,betaShort) < betaMin || Math.Max(betaActual,betaShort) > betaMax);
        }

        public long tradeSize() {
            return (long) Math.Round(targetNetProfit / (bigPointValue() * Math.Abs(residual)));
        }
    }
}