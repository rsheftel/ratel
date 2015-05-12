using System;
using System.Collections.Generic;
using java.util;
using Q.Trading;
using Q.Trading.Results;
using tsdb;
using util;
using Object=java.lang.Object;

namespace Q.Systems.SystemSystems {
    public class SectorRotationShort : Trading.System {
        readonly Dictionary<Symbol, double> performance = new Dictionary<Symbol, double>();        
        readonly Dictionary<Symbol, double> weights = new Dictionary<Symbol, double>();                     
        internal Date barDate;
        internal Date evaluationDate;
        internal Date refDate;
        internal double basketSize;
        readonly double nBest;
        readonly int daysBuffer;
        readonly int daysInTrade;        
        readonly string financialCenter;
        readonly int risk;
        internal Date rebalancingDate;
        internal Date tradeExitDate;
        readonly int leadBars;
        readonly int cumulative;
        readonly int minBasketSize;
        readonly double maxNBestBasketSizeRatio;

        public SectorRotationShort(QREBridgeBase bridge) : this(bridge, bridge.arguments().symbols) {}
        public SectorRotationShort(QREBridgeBase bridge, IEnumerable<Symbol> symbols) : base(bridge) {                        
            risk = parameter<int>("Risk");
            nBest = parameter<int>("NBest");            
            daysBuffer = parameter<int>("DaysBuffer");
            minBasketSize = parameter<int>("MinBasketSize");
            maxNBestBasketSizeRatio = parameter<double>("MaxNBestBasketSizeRatio");
            cumulative = parameter<int>("Cumulative");
            leadBars = parameter<int>("LeadBars");
            daysInTrade = parameter<int>("DaysInTrade");
            financialCenter = FinancialCenterTable.CENTER.name(39);
            eachValue(bars, barSpud => barSpud.times.prepare());
        }

        public override DateTime onCloseTime() {
            return requireAllMatchFirst(bridge.arguments().symbols, sym => sym.closeAt());
        }

        protected override void onNewTick(Symbol symbol, Bar partialBar, Tick tick) {}

        public override bool runOnClose() { return true; }

        protected override void onNewBar(Dictionary<Symbol, Bar> b) {}

        protected override void onFilled(Position position, Trade trade) {}

        protected override void onClose(Dictionary<Symbol, Bar> current) {

           var valid = new Dictionary<Symbol, Bar>();            
            eachKey(current, symbol => {
                if (bars[symbol].count() >= leadBars) valid[symbol] = current[symbol];
            });
            if(valid.Count < basketSize) return;
            basketSize = valid.Count;           
            if(basketSize * maxNBestBasketSizeRatio < nBest || basketSize < minBasketSize) return;                                               
            barDate = Dates.midnight(jDate(current[first(current.Keys)].time));                        
            if (evaluationDate == null || barDate.after(tradeExitDate)) recacheSystemDates();                        
            if (barDate.Equals(tradeExitDate)) exitTrades();               
            if (barDate.Equals(evaluationDate)) calcPerformanceMetrics(valid, refDate, evaluationDate);                
            if (!barDate.Equals(rebalancingDate)) return;
            exitTrades();
            enterTrades(valid);
        }

        void enterTrades(IDictionary<Symbol, Bar> current) {
            weights.Clear();
            var performanceOrderedSymbols = keysSortedByValue(performance, (left, right) => left.CompareTo(right));
            var rangeStart = cumulative == 1 ? 0 : (nBest-1);
            var rangeEnd = cumulative == 1 ? nBest : 1;
            var shorts = performanceOrderedSymbols.GetRange((int) rangeStart, (int) rangeEnd);                      
            // set all to long weights
            eachKey(current, symbol => {                                                                          
                weights[symbol] = risk/basketSize/bars[symbol][0].close;                
            });
            var nBestAdj = cumulative == 1 ? nBest : 1;
            // overwrite shorts with short weights
            each(shorts, symbol => {                
                weights[symbol] = weights[symbol] - risk/nBestAdj/bars[symbol][0].close;                
            });
            eachKey(current, symbol => {                                                                          
                var size = (long) Math.Round(weights[symbol],0);
                if(weights[symbol]>0) placeOrder(symbol.buy("enter long",market(),size,oneBar()));
                if(weights[symbol]<0) placeOrder(symbol.sell("enter short",market(),-size,oneBar()));                
            });
        }

        void exitTrades() {           
            each(arguments().symbols, symbol => 
                each(positions(symbol), p => 
                    placeOrder(p.exit("exit on days in trade", market(), oneBar()))
            ));            
        }
        void calcPerformanceMetrics(IDictionary<Symbol, Bar> current, Date refD, Date evaluationD) {                        
            performance.Clear();            
            eachKey(current, symbol => {                                
                var refPrice = priceForDate(symbol, refD);
                var lastPrice = priceForDate(symbol, evaluationD);               
                performance[symbol] = (lastPrice-refPrice)/refPrice;                
            });     
        }

        double priceForDate(Symbol symbol, Date d) {
            return bars[symbol][date(d)].close;
        }

        protected void recacheSystemDates() {   
            var firstOfCurrentMonth = Dates.date(Dates.year(barDate), Dates.monthNumber(barDate), 1);
            var firstOfNextMonth = Dates.monthsAhead(1, firstOfCurrentMonth);    
            
            evaluationDate = businessDaysAdj(1,firstOfNextMonth,true);                        
            refDate = businessDaysAdj(1, firstOfCurrentMonth,true);
            rebalancingDate = daysBuffer == 0 ? evaluationDate : businessDaysAdj(daysBuffer,evaluationDate,false);                                                    
            tradeExitDate = businessDaysAdj(daysInTrade,rebalancingDate,false);
        }

        Date businessDaysAdj(int i, Date d, bool ago) {
            var resultDate = ago
                ? Dates.businessDaysAgo(i, d, financialCenter)
                : Dates.businessDaysAhead(i, d, financialCenter);
            if (isBadDate(resultDate))
                resultDate = ago
                    ? Dates.businessDaysAgo(1, resultDate, financialCenter)
                    : Dates.businessDaysAhead(1, resultDate, financialCenter);
            return resultDate;
        }

        static bool isBadDate(Object d) {
            return d.Equals(Dates.date("2005/12/30")); // Missing data point                
        }

        public override void addCollectorsTo(SystemArguments arguments, Dictionary<Collectible, StatisticsCollector> collectors) {
            each(arguments.symbols, symbol => collectors[symbol] = new StatisticsCollector(arguments) );
        }
    }
}