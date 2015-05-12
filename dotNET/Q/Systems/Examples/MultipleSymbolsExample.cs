using System;
using System.Collections.Generic;
using Q.Spuds.Core;
using Q.Trading;

namespace Q.Systems.Examples {
    public class MultipleSymbolsExample : Trading.System {
        public readonly Dictionary<Symbol, SymbolSpud<double>> richCheap = new Dictionary<Symbol, SymbolSpud<double>>();
        readonly int numPairs;

        public MultipleSymbolsExample(QREBridgeBase bridge) : this(bridge, bridge.arguments().symbols) {}
        public MultipleSymbolsExample(QREBridgeBase bridge, IEnumerable<Symbol> symbols) : base(bridge) {
            numPairs = parameter<int>("NumPairs");
            each(symbols, symbol => richCheap[symbol] = new Symbol("RC." + symbol.name).doubles(bars[symbol]));
        }

        protected override void onNewTick(Symbol symbol, Bar partialBar, Tick tick) {
            throw new NotImplementedException();
        }

        protected override void onNewBar(Dictionary<Symbol, Bar> b) {
            var cheapest = keysSortedByValue(richCheap, (left, right) => left[0].CompareTo(right[0]));
            var richest = copy(cheapest);
            richest.Reverse();

            reposition(cheapest.GetRange(0, numPairs), Direction.LONG);
            reposition(richest.GetRange(0, numPairs), Direction.SHORT);
        }

        protected override void onFilled(Position position, Trade trade) {}
        protected override void onClose(Dictionary<Symbol, Bar> current) {}

        public override DateTime onCloseTime() {
            throw new NotImplementedException();
        }

        void reposition(IList<Symbol> desired, Direction direction) {
            var current = accept(allPositions(), p => p.direction().Equals(direction));
            for (var i = current.Count; i < numPairs; i++) enter(direction, desired);
            each(copy(current), position => {
                                    if (!desired.Contains(position.symbol)) return;
                                    desired.Remove(position.symbol);
                                    current.Remove(position);
                                });
            // at this point, anything left in current is not in desired
            each(copy(current), position => {
                                    if(position.barsHeld() < parameter<int>("MinimumHoldingPeriod")) return;
                                    placeOrder(position.exit("exit " + direction, market(), oneBar()));
                                    enter(direction, desired);
                                });
        }

        void enter(Direction direction, IList<Symbol> desired) {
            placeOrder(new Order("enter " + direction, first(desired), market(), direction, 1, oneBar()));
            desired.RemoveAt(0);
        }
    }
}