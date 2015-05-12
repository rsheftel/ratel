namespace Q.Trading {
    public abstract class MultiSymbolSystem<T> : MultiSystem<Symbol, T> where T : SymbolSystem {
        protected MultiSymbolSystem(QREBridgeBase bridge) : base(bridge) {
            var args = arguments();
            args.forSymbols(symbol => addSystem(symbol, SymbolSystem.create<T>(typeof (T).FullName, bridge, symbol)));
        }


        public override BarSpud barSpud(Symbol s) {
            // currently, MultiPairSystem doesn't have a good answer for this question.  Of course, netiher does System.  This implementation sticks with MultiSymbolSystem.
            return systems_[s].bars;
        }
    }
}