using System;
using System.Collections.Generic;
using System.Linq;
using Q.Trading;


namespace Q.Systems.MultiSymbolSystems {
    public class DTDPortfolioMatchedPairs : DTDPortfolioMultiSymbolBase {
        readonly double maxPairs;
        readonly double slippageMultiplier;

        public DTDPortfolioMatchedPairs(QREBridgeBase bridge) : base(bridge) {
            maxPairs = parameter<double>("maxPairs");
            slippageMultiplier = parameter<double>("requiredSlippageMultiplier");
        }

        protected override void managerOnClose(Dictionary<Symbol, Bar> current) {
            var currentOrders = allOrders();
            var currentPositions = list(allPositions());

            var exitOrders = accept(currentOrders, order => order.hasPosition());
            var entryOrders = copy(currentOrders);

            each(exitOrders, order => entryOrders.Remove(order));
            each(exitOrders, order => currentPositions.Remove(order.position()));

            var newLongs = accept(entryOrders, order => order.direction.isLong());
            var newShorts = accept(entryOrders, order => order.direction.isShort());
            var longPositions = accept(currentPositions, p => p.direction().isLong());
            var shortPositions = accept(currentPositions, p => p.direction().isShort());

            var totalLongCount = longPositions.Count + newLongs.Count;
            var totalShortCount = shortPositions.Count + newShorts.Count;

            if(totalLongCount == totalShortCount && totalLongCount <= maxPairs) return;

            var positionsWithoutData = reject(currentPositions, p => current.ContainsKey(p.symbol));
            var symbolsWithoutData = convert(positionsWithoutData, p => p.symbol);

            handlePositions(newLongs, longPositions, newShorts, shortPositions, symbolsWithoutData);

            each(newLongs, o => systems_[o.symbol].cancelOrder(o));
            each(newShorts, o => systems_[o.symbol].cancelOrder(o));
        }

        void handlePositions(List<Order> newLongs, ICollection<Position> longPositions, List<Order> newShorts,
                             ICollection<Position> shortPositions, IEnumerable<Symbol> symbolsWithoutData) {

            var newLongExpectedProfits = expectedProfitsOrders(newLongs, false);
            var oldLongExpectedProfits = expectedProfitsPositions(longPositions, symbolsWithoutData);
            var newShortExpectedProfits = expectedProfitsOrders(newShorts, false);
            var oldShortExpectedProfits = expectedProfitsPositions(shortPositions, symbolsWithoutData);

            var longCount = newLongExpectedProfits.Count + oldLongExpectedProfits.Count;
            var shortCount = newShortExpectedProfits.Count + oldShortExpectedProfits.Count;

            double positionCount = Math.Min(longCount, shortCount);
            positionCount = Math.Min(positionCount, maxPairs);
            
            executeGoodOrders(positionCount - longPositions.Count, newLongs, newLongExpectedProfits);
            executeGoodOrders(positionCount - shortPositions.Count, newShorts, newShortExpectedProfits);
            
            newLongExpectedProfits = expectedProfitsOrders(newLongs, true);
            newShortExpectedProfits = expectedProfitsOrders(newShorts, true);

            swapTrades(newLongs, newLongExpectedProfits, oldLongExpectedProfits);
            swapTrades(newShorts, newShortExpectedProfits, oldShortExpectedProfits);
        }

        void swapTrades(List<Order> newOrders,  
                        IList<KeyValuePair<Symbol, double>> orderProfits,
                        IList<KeyValuePair<Symbol, double>> positionProfits) {
            
            if(doneSwapping(positionProfits, newOrders)) return;
            
            while(orderProfits[0].Value > positionProfits[0].Value) {
                
                var pos = position(positionProfits[0].Key);
                var orderDescription = pos.lOrS() + "X Max Pairs";
                    
                systems_[pos.symbol].placeOrder(pos.exit(orderDescription, market(), oneBar()));

                executeGoodOrders(1, newOrders, orderProfits);
                positionProfits.RemoveAt(0);
                if(doneSwapping(positionProfits, newOrders)) break;
            }            
        }

        static bool doneSwapping(IList<KeyValuePair<Symbol, double>> positionProfits, List<Order> newOrders) {
            return isEmpty(positionProfits) || isEmpty(newOrders);
        }

        static void executeGoodOrders(double count, List<Order> orders, IList<KeyValuePair<Symbol, double>> profits) {
            while(count > 0) {
                orders.RemoveAll(o => o.symbol.Equals(profits[0].Key));
                profits.RemoveAt(0);
                count--;
            }
        }

        List<KeyValuePair<Symbol, double>> expectedProfitsPositions(IEnumerable<Position> positions, IEnumerable<Symbol> symbolsWithoutData) {
            var exitSymbols = convert(positions, p => p.symbol);
            var adjustedProfits = convert(positions, p => singlePositionProfit(p, symbolsWithoutData));
            var exitHurdles = pairsSortedByValue(dictionary(exitSymbols, adjustedProfits));
            exitHurdles.Reverse();
            return exitHurdles;
        }

        double singlePositionProfit(Position p, IEnumerable<Symbol> symbolsWithoutData) {
            if(symbolsWithoutData.Contains(p.symbol)) return 100000000; //if there is no data, must stay in position
            
            var profit = systems_[p.symbol].expectedProfit(p.direction()) * p.size * p.symbol.bigPointValue;
            var slippageHurdle = p.size * slippage(p.symbol) * slippageMultiplier;
            
            return profit + slippageHurdle;
        }

        List<KeyValuePair<Symbol, double>> expectedProfitsOrders(IEnumerable<Order> orders, bool useSlippageMultiplier) {
            var entrySymbols = convert(orders, o => o.symbol);
            var adjustedProfits = convert(orders, o => singleOrderProfit(o, useSlippageMultiplier));
            return pairsSortedByValue(dictionary(entrySymbols, adjustedProfits));
        }

        double singleOrderProfit(Order o, bool useSlippageMultiplier) {
            var multiplier = useSlippageMultiplier ? slippageMultiplier : 1;
        
            var profit = systems_[o.symbol].expectedProfit(o.direction) * o.size * o.symbol.bigPointValue;
            var expectedSlippage = o.size * slippage(o.symbol) * multiplier;
 
            return (profit - expectedSlippage);
        }
    }
}