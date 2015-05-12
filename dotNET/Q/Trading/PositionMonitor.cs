using System;
using System.Collections.Generic;
using mail;
using Q.Messaging;
using Q.Util;
using systemdb.metadata;

namespace Q.Trading {
    public class PositionMonitor : Objects {
        readonly LiveSystem liveSystem;
        readonly IEnumerable<Symbol> symbols;
        readonly string topicPrefix;
        readonly Dictionary<Symbol, long> livePositions = new Dictionary<Symbol, long>();
        readonly Dictionary<Symbol, long> beginPositions = new Dictionary<Symbol, long>();
        readonly Dictionary<Symbol, long> closePositions = new Dictionary<Symbol, long>();
        readonly Dictionary<Symbol, bool> dirty = new Dictionary<Symbol, bool>();
        readonly Dictionary<System, List<Order>> closeForecastOrders = new Dictionary<System, List<Order>>();
        readonly DateTime start = now();
        protected bool isLive;

        public PositionMonitor(LiveSystem liveSystem, IEnumerable<Symbol> symbols, string topicPrefix) {
            this.liveSystem = liveSystem;
            this.symbols = symbols;
            this.topicPrefix = topicPrefix;
        }

        void publishInitialPositions(IEnumerable<Position> positionsForSymbol, Symbol symbol) {
            Bomb.ifNull(liveSystem, () => "ticks received in non-live mode");
            long total = 0;
            each(positionsForSymbol, position => { total += position.amount; });
            livePositions[symbol] = total;
            beginPositions[symbol] = total;
            closePositions[symbol] = total;
            publishPosition(symbol);
        }

        void publishPosition(Symbol symbol) {
            var message = new Dictionary<string, object> {
                {"beginValue", beginPositions[symbol]},
                {"liveValue", livePositions[symbol]},
                {"forecastCloseValue", closePositions[symbol]},
                {"beginTimestamp", start},
                {"liveTimestamp", now()},
                {"hostname", hostname()}
            };
            var topicName = liveSystem.topicName(topicPrefix, symbol.name + ".optimalPosition");
            if (LiveTradeMonitor.inNoPublishMode()) 
                LogC.info("not published to: " + topicName + ":\n" + toShortString(message));
            else 
                new Topic(topicName).send(message);
            dirty.Clear();
        }

        public void publishAll() {
            eachCopiedKey(dirty, publishPosition);
        }

        public delegate Email TradeEmailFunc(LiveSystem system, Trade trade, int liveOrderId);

        public void positionUpdate(Position position, Trade trade, TradeEmailFunc tradeEmail) {
            if(!isLive) return;
            // just in case we are trading this for the first time in live mode (typically just a test situation)
            var symbol = position.symbol;
            initialize(symbol);
            livePositions[symbol] += trade.size * trade.direction;
            closePositions[symbol] += trade.size * trade.direction;
            dirty[symbol] = true;

            if(LiveTradeMonitor.inNoPublishMode()) return;
            var id = trade.order().filledUpdateDb(trade.price, liveSystem, topicPrefix);

            var email = tradeEmail(liveSystem, trade, id);
            var liveOrder = LiveOrders.ORDERS.order(id);
            liveOrder.emailInterestedParties(email);
        }

        void initialize(Symbol symbol) {
            if (beginPositions.ContainsKey(symbol)) return;
            beginPositions[symbol] = 0;
            livePositions[symbol] = 0;
            closePositions[symbol] = 0;
        }

        public void forecastCloseOrder(System system, Order order) {
            if(!isLive) return;
            // just in case we are trading this for the first time in live mode (typically just a test situation)
            var symbol = order.symbol;
            initialize(symbol);
            closePositions[symbol] += order.size * order.direction;
            dirty[symbol] = true;
            if (!closeForecastOrders.ContainsKey(system)) closeForecastOrders.Add(system, new List<Order>());
            closeForecastOrders[system].Add(order);
        }

        public void resetForecasts(System system) {
            if (!closeForecastOrders.ContainsKey(system)) return;
            each(closeForecastOrders[system], unwindForecast);
            closeForecastOrders.Remove(system);
        }

        void unwindForecast(Order order) {
            var symbol = order.symbol;
            Bomb.missing(closePositions, symbol);
            dirty[symbol] = true;
            closePositions[symbol] -= order.size * order.direction;
        }

        public void goLive(System system) {
            each(symbols, symbol => goLive(system.positions(symbol), symbol));
        }

        internal void goLive(List<Position> positions, Symbol symbol) {
            isLive = true;
            publishInitialPositions(positions, symbol);
        }

        public static Email basicTradeEmail(LiveSystem system, Trade trade, int liveOrderId) {
            return Email.trade(
                system.siv().system() + paren(system.pv().name()) + " Filled Order for " + trade.order().symbol.name + " - " + hostname(), 
                    "Order (" + liveOrderId + "): " + trade.shortString() + 
                        "\nTimestamp: " + ymdHuman(now()) + 
                        "\nDescription: " + trade.description
            );        
        }
    }
}