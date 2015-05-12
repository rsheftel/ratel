using System.Collections.Generic;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;

namespace Q.Systems.PairSystems {
    public abstract class OnePairSystemTest<T, PG> : OneSystemTest<IndependentPairSystems<T, PG>> 
        where T : PairSystem where PG : PairGenerator 
    {        
        private Pair pair_;
        protected T pairSystem;

        public override void setUp() {
            base.setUp();
            pairSystem = pairSystemFromBridge();
        }

        protected override void initializeSymbols() {
            Symbol.clearCache();
            pair_ = initializePair();
        }

        T pairSystemFromBridge() {
            return Bomb.missing(system().systems_, pair_);
        }

        protected virtual Pair initializePair() {
            insertMarket("RE.TEST.TY.3C", 0);
            insertMarket("RE.TEST.TY.5C", 0);
            return new Pair(new Symbol("RE.TEST.TY.3C", 1000), new Symbol("RE.TEST.TY.5C", 1000));
        }

        protected Pair pair() {
            return pair_;
        }

        protected void processClose(double left, double right) {
            processClose(lastBar => pair().closeBars(left, right, nextTime()));
        }

        protected void fill(int index, double fillPriceLeft, double fillPriceRight) {
            fill(pair().left, index, fillPriceLeft);
            fill(pair().right, index, fillPriceRight);
        }

        protected List<Order> orders() {
            return Objects.collect(pair().symbols(), s => orders(s));
        }
        
        protected void hasOrders(List<Order> expecteds) {
            hasOrders(orders(), expecteds.ToArray());
        }

        protected override SystemArguments arguments() {
            var testParams = parameters();
            return arguments(testParams);
        }

        protected SystemArguments arguments(Parameters testParams) {
            return new SystemArguments(pair().symbols(), new List<Portfolio>(), testParams, leadBars());
        }

        protected List<Order> buy(string description, OrderDetails detailsLeft, OrderDetails detailsRight, long size, OrderDuration duration) {
            var result = pair().buy(description, detailsLeft, detailsRight, size, duration);
            Objects.each(result, order => order.placed());
            return result;
        }

        protected List<Order> sell(string description, OrderDetails detailsLeft, OrderDetails detailsRight, long size, OrderDuration duration) {
            var result = pair().sell(description, detailsLeft, detailsRight, size, duration);
            Objects.each(result, order => order.placed());
            return result;
        }

        protected List<Position> positions() {
            return Objects.collect(pair().symbols(), s => system().positions(s));
        }        
        
        protected void hasPosition(double amountLeft, double amountRight) {
            AreEqual(amountLeft, position(pair().left).amount);
            AreEqual(amountRight, position(pair().right).amount);
        }

        protected List<Position> position() {
            return Objects.list(system().position(pair().left), system().position(pair().right));
        }
    }
}