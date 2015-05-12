using System.Collections.Generic;
using Q.Systems.MultiPairSystems;
using Q.Trading;

namespace Q.Systems.MultiSymbolSystems {
    public class DTDPortfolioPositionLimits : DTDPortfolioMultiSymbolBase {
        readonly MaxTradesManager<DTDRichCheapV2, Symbol> manager;
        readonly double slippageMultiplier;

        public DTDPortfolioPositionLimits(QREBridgeBase bridge) : base(bridge) {
            manager = new MaxTradesManager<DTDRichCheapV2, Symbol>(systems_.Values, parameter<int>("maxPositions"), compareSystems, placeExits);
            slippageMultiplier = parameter<double>("requiredSlippageMultiplier");
        }

        int compareSystems(DTDRichCheapV2 a, DTDRichCheapV2 b) {
            return a.expectedProfitWithSlippage(slippageMultiplier).CompareTo(b.expectedProfitWithSlippage(slippageMultiplier));
        }

        static void placeExits(DTDRichCheapV2 system) {
            var pos = system.position();
            system.placeOrder(pos.exit(pos.lOrS() + "X Max Positions", market(), oneBar()));
        }

        protected override void managerOnClose(Dictionary<Symbol, Bar> current) {
            manager.onClose(current);
        }
    }

    public class DTDPortfolioPositionLimitsOld : DTDPortfolioMultiSymbolBase {
        readonly double maxPositions;
        readonly double slippageMultiplier;

        public DTDPortfolioPositionLimitsOld(QREBridgeBase bridge) : base(bridge) {
            maxPositions = parameter<double>("maxPositions");
            slippageMultiplier = parameter<double>("requiredSlippageMultiplier");
        }

        protected override void managerOnClose(Dictionary<Symbol, Bar> current) {
            var currentOrders = allOrders();
            var currentPositions = list(allPositions());

            var exitOrders = accept(currentOrders, order => order.hasPosition());
            var entryOrders = reject(currentOrders, order => order.hasPosition());

            each(exitOrders, order => currentPositions.Remove(order.position()));

            var positionCount = (entryOrders.Count + currentPositions.Count);
            if (positionCount <= maxPositions) return;
            if(currentPositions.Count < maxPositions && entryOrders.Count > 0) 
                fillInWithNewPositions(entryOrders, (int) (maxPositions - currentPositions.Count));

            var positionsWithLiveBars = accept(currentPositions, p => current.ContainsKey(p.symbol));

            replacePositions(entryOrders, positionsWithLiveBars, positionCount);

            each(entryOrders, o => systems_[o.symbol].cancelOrder(o));
        }

        void fillInWithNewPositions(List<Order> entryOrders, int remainingPositionSlots) {
            var entryProfits = entryOrderAdjustedProfits(entryOrders);

            while(remainingPositionSlots > 0) {
                allowBestEntryOrder(entryOrders, entryProfits);
                remainingPositionSlots--;
            }
        }

        void replacePositions(List<Order> entryOrders, IEnumerable<Position> currentPositions, int positionCount) {
            var entryProfits = entryOrderAdjustedProfits(entryOrders);
            var exitHurdles = exitHurdleProfits(currentPositions);

            while(positionCount > maxPositions) {
                if(isEmpty(currentPositions) || isEmpty(exitHurdles) || isEmpty(entryProfits)) break;
                if (entryProfits[0].Value <= exitHurdles[0].Value) break;
                var pos = position(exitHurdles[0].Key);
                var orderDescription = pos.lOrS() + "X Max Positions";
                    
                systems_[pos.symbol].placeOrder(pos.exit(orderDescription, market(), oneBar()));

                allowBestEntryOrder(entryOrders, entryProfits);
                exitHurdles.RemoveAt(0);
                positionCount--;
            }
            return;
        }

        static void allowBestEntryOrder(List<Order> entryOrders, IList<KeyValuePair<Symbol, double>> entryProfits) {
            entryOrders.RemoveAll(order => order.symbol.Equals(entryProfits[0].Key));
            entryProfits.RemoveAt(0);
        }

        List<KeyValuePair<Symbol, double>> exitHurdleProfits(IEnumerable<Position> positions) {
            var positionSymbols = convert(positions, p => p.symbol);
            var exitHurdles = convert(positions, p => singleHurdle(p.symbol, p.direction(), p.size, false));
            var tempExitHurdles = pairsSortedByValue(dictionary(positionSymbols, exitHurdles));
            tempExitHurdles.Reverse();
            return tempExitHurdles;
        }

        List<KeyValuePair<Symbol, double>> entryOrderAdjustedProfits(IEnumerable<Order> entryOrders) {
            var entrySymbols = convert(entryOrders, o => o.symbol);
            var adjustedProfits = convert(entryOrders, o => singleHurdle(o.symbol, o.direction, o.size, true));
            return pairsSortedByValue(dictionary(entrySymbols, adjustedProfits));
        }

        double singleHurdle(Symbol symbol, Direction direction, long size, bool isEntry) {
            var entry = isEntry ? 1 : -1;
            var expectedProfit = systems_[symbol].expectedProfit(direction) * size * symbol.bigPointValue;
            var expectedSlippages = size * slippage(symbol) * slippageMultiplier;
            return expectedProfit - (entry * expectedSlippages);
        }
    }
}