using System;
using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    public class LiqInj : SymbolSystem {
        readonly EWAcf acf;
        public readonly SymbolSpud<double> zScore;
        public readonly SymbolSpud<double> residual;
        public readonly SymbolSpud<double> tc;
        public readonly SymbolSpud<double> rSquare;
        public readonly SymbolSpud<double> hedge;
        public readonly SymbolSpud<double> scale;
        public bool isLongSignal;
        public bool isShortSignal;
        readonly double zScoreMin;
        readonly double pScoreMin;
        public bool isTriggered;
        public bool isValidFit;
        readonly double rSquareMin;
        readonly int hedgeSwitch;
        readonly double hedgeMin;
        readonly double hedgeMax;
        public bool isValidAcf;
        readonly double acfTrigger;
        readonly double scaleMin;
        public bool isValidScale;
        readonly long startSize;
        readonly double nATR;
        readonly AverageTrueRange atr;
        public bool isConfirmed;
        public bool isAccepted;
        public string version;
        readonly double stopMultiple;
        internal double? liveTargetNetProfit;
        internal double? startOfDayTargetNetProfit;
        bool isInRealLiveClose;
        bool isStoppedIntraday;
        Position startOfDayPosition;
        Position livePosition;

        public LiqInj(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            version = symbol.name.Substring(3, 2);
            residual = symbol.relatedPrefix("PTV" + version).doubles(bars);
            tc = symbol.relatedPrefix("PTC" + version).doublesNoLive(bars);
            rSquare = symbol.relatedPrefix("PTR" + version).doubles(bars);
            hedge = symbol.relatedPrefix("PTH" + version).doubles(bars);
            scale = symbol.relatedPrefix("PTS" + version).doublesNoLive(bars, 0);
            var atrLength = parameter<int>("ATRLength");
            nATR = parameter<double>("nATR");
            if(nATR != 0 && atrLength != 0) atr = new AverageTrueRange(bars, atrLength);
            zScore = symbol.relatedPrefix("PTZ" + version).doubles(bars, 0);
            acf = new EWAcf(zScore, parameter<double>("acfHalfLife"), parameter<int>("acfLag"));

            zScoreMin = parameter<double>("zScoreMin");
            pScoreMin = parameter<double>("pScoreMin");
            rSquareMin = parameter<double>("rSquareMin");
            hedgeSwitch = parameter<int>("hedgeSwitch");
            hedgeMin = parameter<double>("hedgeMin");
            hedgeMax = parameter<double>("hedgeMax");
            acfTrigger = parameter<double>("acfTrigger");
            scaleMin = parameter<double>("scaleMin");
            startSize = parameter<long>("startSize");
            stopMultiple = parameter<double>("stopMultiple");
            bridge.manager.onLive += () => {
                if (startOfDayPosition != null) livePosition = startOfDayPosition;
                if (startOfDayTargetNetProfit.HasValue) liveTargetNetProfit = startOfDayTargetNetProfit;
            };
            addToPlot(zScore, "zScore", Color.Red, "zScore");
        }

        protected override void onFilled(Position position, Trade trade) {
            if (position.isEntry(trade)) {
                var target = tradeSize() * position.symbol.bigPointValue * Math.Abs(residual);
                if (bridge.inLiveMode()) {
                    liveTargetNetProfit = target;
                    livePosition = position;
                } else {
                    startOfDayTargetNetProfit = target;
                    startOfDayPosition = new Position(position);
                }
            } else if (position.isClosed()) {
                if (bridge.inLiveMode()) {
                    livePosition = null;
                    liveTargetNetProfit = null;
                } else {
                    startOfDayPosition = null;
                    startOfDayTargetNetProfit = null;
                }
            }
        }

        protected override void onNewBar() {
        }

        void doTrades(bool newIsInRealLiveClose) {
          
            isInRealLiveClose = newIsInRealLiveClose;
            if (isStoppedIntraday && !isInRealLiveClose) return;
            setFlags();
            if (hasPosition()) {
                checkStopLoss();
                checkInvalidFit();
                checkTargetReached();
                checkOppositeSignal();
                if (!bridge.inLiveMode()) checkBarsHeld(); // Historical Mode
            }
            placeEntries();
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            if (isStoppedIntraday) return;
            doTrades(false);
        }

        public override bool runOnNewTick() {
            return true;
        }

        protected override void onClose() {
            doTrades(bridge.inLiveMode());
            if(bridge.inLiveMode()) syncLiveToClose();
        }

        void checkStopLoss() {
            if (openPositionPnl() >= -stopMultiple * targetNetProfit()) return;
            exit("Stop Loss");
            if (bridge.inLiveMode() && !isInRealLiveClose) {
                isStoppedIntraday = true;
                throw new AbortBar();
            }
            deactivateAndStop(() => Math.Sign(zScore) != Math.Sign(zScore[1]) && Math.Abs(zScore - zScore[1]) > 1);
        }

        public override bool runOnClose() {
            return true;
        }

        void syncLiveToClose() {
            if(startOfDayPosition == null) return;
            var realPosition = livePosition ?? new Position(symbol, 0, Direction.LONG);

            var closePosition = new Position(symbol, startOfDayPosition.size, startOfDayPosition.direction());
            each(orders(symbol), order => order.direction.advance(closePosition, order.size));            
            if (realPosition.amount == closePosition.amount) return;
            if(!realPosition.isClosed() && closePosition.direction() == realPosition.direction()) {
                if(!realPosition.isClosed() && closePosition.size == 0) return;
                if (closePosition.size > realPosition.size)
                    placeOrder(realPosition.scaleUp("Sync Up To Close", market(), closePosition.size - realPosition.size, oneBar()));
                else
                    placeOrder(realPosition.scaleDown("Sync Down To Close", market(), realPosition.size - closePosition.size, oneBar()));
            } else {
                if(!realPosition.isClosed()) placeOrder(realPosition.exit("Sync Flatten Position", market(), oneBar()));
                if(closePosition.size == 0) return;
                if (closePosition.direction().isLong())
                    placeOrder(symbol.buy("Sync Long To Close", market(), closePosition.size, oneBar()));
                else
                    placeOrder(symbol.sell("Sync Short To Close", market(), closePosition.size, oneBar()));
            }
        }

        void checkBarsHeld() {
            if (position().barsHeld() >= parameter<int>("maxBarInTrade") && !isConfirmed)
                 exit("Bar Limit");
        }

        void checkOppositeSignal() {
            if(oppositeSignal())
                exit("Exit " + position().longShort("Long", "Short") + " On Signal");
        }

        void checkTargetReached() {
            if(openPositionPnl() > targetNetProfit() && (!isTriggered || !isValidAcf)) 
                exit("Exit " + position().longShort("Long", "Short") + " On Target");
        }

        double targetNetProfit() {
            if (isInRealLiveClose) return Bomb.ifNull(startOfDayTargetNetProfit, () => "no value for openTargetNetProfit!");
            return bridge.inLiveMode() 
                ? Bomb.ifNull(liveTargetNetProfit, () => "no value for liveTargetNetProfit!") 
                : Bomb.ifNull(startOfDayTargetNetProfit, () => "no value for openTargetNetProfit!");
        }

        void exit(string description) {
            placeOrder(position().exit(description, market(), oneBar()));
        }

        void checkInvalidFit() {
            if (!isValidFit) exit("Invalid Hedge");
        }

        protected override double openPositionPnl() {
            return isInRealLiveClose ? position().pnlNoSlippage(bars[1].close, arguments().runInNativeCurrency, bridge.fxRate(position().symbol)) : base.openPositionPnl();
        }

        public override Position position() {
            return isInRealLiveClose ? startOfDayPosition : base.position();
        }

        public override bool hasPosition() {
            var result = isInRealLiveClose ? startOfDayPosition != null : base.hasPosition();
            return result;
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
            isValidFit = rSquare > rSquareMin && isValidHedge(hedge, hedgeSwitch, hedgeMin, hedgeMax);
            isValidAcf = acf <= acfTrigger;
            isValidScale = scale <= 10 ? scale > scaleMin : scale < 21 - scaleMin;            
            isConfirmed = isValidFit && isTriggered && isValidAcf && isValidScale && tradeSize() > 0;
            isAccepted = isConfirmed && (noPosition() || oppositeSignal());
        }

        bool oppositeSignal() {
            return position().longShort(isShortSignal, isLongSignal);
        }

        internal static bool isValidHedge(double hedge, int hedgeSwitch, double hedgeMin, double hedgeMax) {
            if(hedgeSwitch == 0) return Math.Abs(hedge) >= hedgeMin && Math.Abs(hedge) <= hedgeMax;
            return hedgeSwitch * hedge >= hedgeMin && hedgeSwitch * hedge <= hedgeMax;
        }

        public long tradeSize() {
            return atr == null ? startSize : (long) Math.Round(startSize / (symbol.bigPointValue * nATR * atr));
        }
    }
}