using System;
using System.Text.RegularExpressions;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Util;
using systemdb.data;
using tsdb;
using util;
using Bar=Q.Trading.Bar;
using O=Q.Util.Objects;
using Symbol=Q.Trading.Symbol;
using Tick=Q.Trading.Tick;

namespace Q.Systems {
    public class ITrend : SymbolSystem {
        
        internal readonly AverageTrueRangeEW atr;
        readonly double nATRStop;
        readonly double nATRTrigger;        
        readonly int risk;
        readonly int atrLength;
        static int sizeScaleSwitch;
        readonly double timeStampClose;
        readonly int dailyATRSwitch;
        protected double[] scaleWeights;
        public double[] scaleWins;
        public double lastTradePNL;
        double refClose;        
        double stopPriceDistance;
        bool hasPositionLastBar;
        bool hasAlreadyTraded;
        DateTime lastDateInTrade;
        DateTime refDate;
        readonly string financialCenter;      
        bool isSetupLong;
        bool isSetupShort;
        double setupCloseLevel;
        readonly double timeStampMark;
        public bool hasBeenMarked;
        public bool hasBeenClosed;

        public ITrend(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {          
            atrLength = parameter<int>("atrLength");
            sizeScaleSwitch = parameter<int>("sizeScaleSwitch");            
            nATRStop = parameter<double>("nATRStop");            
            nATRTrigger = parameter<double>("nATRTrigger");            
            dailyATRSwitch = parameter<int>("useDailyATR");            
            risk = parameter<int>("risk");
            timeStampClose = parameter<double>("timeStampClose");
            timeStampMark = parameter<double>("timeStampMark");
            
            atr = new AverageTrueRangeEW(
                dailyATRSwitch == 1 ? (Spud<Bar>) new IntervalSpud(bars, Interval.DAILY) : bars,
                atrLength);              

            scaleWeights = new double[10];
            zeroTo(scaleWeights.Length, i => scaleWeights[i] = 1.5-(double)i/9);
            scaleWins = new double[10];
            initializeWinScale();
            financialCenter = FinancialCenterTable.CENTER.name(39);                       
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {}

        protected override void onClose() {}

        protected override void onFilled(Position position, Trade trade) {
            if (!Regex.IsMatch(trade.description, "Entry.*")) return;
            stopPriceDistance = nATRStop*atr;
            hasAlreadyTraded = true;
            isSetupLong = false;
            isSetupShort = false;            
            placeStops();
        }

        public override bool runOnClose() {
            return false;
        }

        void placeStops() {        
            var entryPrice = position().entry().price;
            placeOrder(position().exit("Stop " + position().lOrS(), protectiveStop(roundPrice(entryPrice + position().longShort(-1,1) * stopPriceDistance)),oneDay()));           
        }
        
        protected override void onNewBar() {

            // don't use stale trade data
            if(hasAlreadyTraded & bar.time.Subtract(lastDateInTrade).TotalDays > 30)
                initializeWinScale();

            if(!hasPosition() & hasPositionLastBar) updateScaleWins();
            hasPositionLastBar = hasPosition(); 

            if (isTimeStamp(timeStampClose)) {
                refClose = bar.close;
                refDate = bar.time;
                isSetupLong = false;
                isSetupShort = false;
                hasBeenMarked = false;
            }

            if(hasPosition()) {
                lastDateInTrade = bar.time;
                if (runEndOfDayLogic()) {
                    exit();
                }
                lastTradePNL = position().lastTrade().pnlNoSlippage(bar.close);
            } else {
                if(runMarkTimeLogic()) {
                    setup();
                    placeEntries();
                    hasBeenClosed = false;
                }                
            }
        }

        void initializeWinScale() {
            zeroTo(scaleWins.Length, i => scaleWins[i] = 0.5); // Initilialization        
        }

        bool isTimeStamp(double timeStampDouble) {
            var timeStampMinutes = timeStampDouble * 60;
            var baseDate = bar.time.Date;
            var minTolerance = 0.25;
            var barDate = bar.time;
            var dateInf = baseDate.AddMinutes(timeStampMinutes - minTolerance);
            var dateSup = baseDate.AddMinutes(timeStampMinutes + minTolerance);            
            return dateInf.CompareTo(barDate) < 0 & dateSup.CompareTo(barDate) >0;          
        }

        public bool runMarkTimeLogic() {
            var isTime = isTimeStamp(timeStampMark) & !hasBeenMarked;
            if(isTime) hasBeenMarked = true;
            return isTime;  
        }

        public bool runEndOfDayLogic() {            
            var isTime = isTimeStamp(timeStampClose) & !hasBeenClosed;
            if(isTime) hasBeenClosed = true;
            return isTime;
        }

        void updateScaleWins() {
            var scaleWinsCopy = new double[10];
            zeroTo(scaleWins.Length, i => scaleWinsCopy[i] = scaleWins[i]);                        
            zeroTo(scaleWins.Length-1, i => scaleWins[i+1] = scaleWinsCopy[i]);                        
            scaleWins[0] = lastTradePNL > 0? 1 : 0;         
        }

        void exit() {
            placeOrder(position().exit("Exit EOD " + position().lOrS(), limit(roundPrice(bar.close + position().longShort(-1,1) * 10 * slippage())), oneDay()));
        }
        
        void setup() {            
            Bomb.when(hasPosition(),() => "There should be no position carried overnight");
            if(!isOneBusinessDayReturn()) return;
            if(refClose==0) return;            
            var atrReturn = (bar.close-refClose)/atr;
            isSetupLong = atrReturn > nATRTrigger;
            isSetupShort = atrReturn < -nATRTrigger;
            setupCloseLevel = bar.close;
        }

        void placeEntries() {            
            if(isSetupLong) placeOrder(symbol.buy("Entry L",limit(roundPrice(setupCloseLevel + slippage())), tradeSize(), oneDay()));
            if(isSetupShort) placeOrder(symbol.sell("Entry S",limit(roundPrice(setupCloseLevel - slippage())), tradeSize(), oneDay()));
        }

        bool isOneBusinessDayReturn() {
            return jDate(refDate.ToShortDateString()).Equals(jDate(date(Dates.businessDaysAgo(1,jDate(bar.time), financialCenter))));
        }

        public long tradeSize() {
            var theoTradeSize = (long) Math.Round(scaleMap(scaleWins,sizeScaleSwitch) * risk / (bigPointValue() * nATRStop * Math.Abs(atr)));
            return Math.Max(theoTradeSize,1); // The lowest size possible is one contract (~ shadow trading)
        }

        public double roundPrice(double price) {
            return Math.Round(price,2);
        }

        public double scaleMap(double[] a,int switchInt) {
            double successCount = 0;
            zeroTo(a.Length, i => successCount = successCount + a[i] * scaleWeights[i]);
            var successCountRound = Math.Round(successCount, 0);            
            if(successCountRound==10) return 1;         
            switch(switchInt) {
                case 0:
                    return 1;                    
                case 1:
                    if(successCountRound<=3) return 0;
                    if(successCountRound>=8) return 1;
                    return(1-0.2*(8-successCountRound));                    
                case 2:                    
                    return(1-0.1*(10-successCountRound));                    
                default:
                    Bomb.toss("Not valid sizeScaleSwitch");
                    break;
            }
            return successCountRound;
        }       
    }
}