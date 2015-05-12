using System.Collections.Generic;
using Q.Util;
using systemdb.metadata;

namespace Q.Trading {
    public class LiveTradeMonitor : TradeMonitor {
        readonly PositionMonitor positionMonitor;
        readonly OrderSubmitter orderSubmitter;
        readonly Dictionary<Symbol, SystemHeartbeat> systemHeartbeat;

        public LiveTradeMonitor(LiveSystem liveSystem, IEnumerable<Symbol> symbols, string topicPrefix) {
            positionMonitor = new PositionMonitor(liveSystem, symbols, topicPrefix);
            orderSubmitter = new OrderSubmitter(liveSystem, topicPrefix);
            systemHeartbeat = dictionary(symbols, symbol => new SystemHeartbeat(liveSystem, symbol, topicPrefix));
        }

        public override void tickProcessed(Symbol symbol, Tick tick) {
            positionMonitor.publishAll();
            systemHeartbeat[symbol].tickProcessed(tick);
        }

        public override void goLive(System system) {
            positionMonitor.goLive(system);
            orderSubmitter.goLive(system);
            eachValue(systemHeartbeat, heartbeat => heartbeat.goLive());
        }

        public override void orderFilled(Position position, Trade trade, PositionMonitor.TradeEmailFunc tradeEmail) {
            positionMonitor.positionUpdate(position, trade, tradeEmail);
            positionMonitor.publishAll();
        }

        public override void orderPlaced(Order order) {
            orderSubmitter.orderPlaced(order);
        }

        public override void orderForecast(System s, Order o) {
            positionMonitor.forecastCloseOrder(s, o);
        }

        public override void resetForecasts(System s) {
            positionMonitor.resetForecasts(s);
        }

        public override void shutdown() {
            eachValue(systemHeartbeat, heartbeat => heartbeat.stop());
        }

        public static bool inNoPublishMode() {
            return isEnvSet("RE_TEST_MODE", false);
        }
    }

    public class TradeMonitor : Objects {
        public virtual void orderPlaced(Order order) {}
        public virtual void tickProcessed(Symbol symbol, Tick tick) {}
        public virtual void goLive(System system) { Bomb.toss("cannot go live when not in LIVE mode"); }
        public virtual void orderFilled(Position position, Trade trade, PositionMonitor.TradeEmailFunc tradeEmail) {}
        public virtual void orderForecast(System s, Order o) {}
        public virtual void resetForecasts(System system) {}
        public virtual void shutdown() {}
    }
}