using System.Collections.Generic;
using Q.Spuds.Core;
using Q.Trading;

namespace Q.Systems.Examples {
    public class ExampleSystem : Trading.System {
        readonly Dictionary<Symbol, Spud<double>> highest = new Dictionary<Symbol, Spud<double>>();
        readonly Symbol mySymbol;

        public ExampleSystem(QREBridgeBase bridge) : base(bridge) {
            mySymbol = the(arguments().symbols);
            highest[mySymbol] = bars[mySymbol].high.highest(2);
        }

        protected override void onNewBar(Dictionary<Symbol, Bar> b) {
            info("in onNewBar");
            if (highest[mySymbol].changed())
                info("new highest " + highest[mySymbol][0]);
        }

        protected override void onFilled(Position position, Trade trade) {}
        protected override void onClose(Dictionary<Symbol, Bar> current) {}
        public override string name {
            get { return mySymbol.name; }
        }

        protected override void onNewTick(Symbol symbol, Bar partialBar, Tick tick) {}
        public override System.DateTime onCloseTime() {
            return mySymbol.processCloseOrdersTime();
        }
    }
}