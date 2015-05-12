using db.clause;
using mail;
using NUnit.Framework;
using Q.Messaging;
using Q.Systems;
using Q.Systems.Examples;
using Q.Util;
using systemdb.metadata;
using O=Q.Util.Objects;

namespace Q.Trading {
    public class CustomTradeEmailer : EmptySystem {
        public CustomTradeEmailer(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            
        }

        public override Email tradeEmail(LiveSystem liveSystem, Trade trade, int liveOrderId) {
            LogC.info("calcing trade email");
            return Email.trade("he hopes@" + trade.price, "jerome can use this functionality.");       
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            placeOrder(symbol.buy("foo", market(), 1, oneBar()));
        }
    }
    [TestFixture]
    public class TestSystemCustomTradeEmailExample : OneSymbolSystemTest<CustomTradeEmailer> {
        protected override int leadBars() { return 0; }
        [Test]
        public void testEmail() {
            emailer.allowMessages();
            processBar(1, 1, 1, 1);
            processTick(7);
            hasOrders(symbol().buy("foo", market(), 1, oneBar()));
            fill(0, 7);
            emailer.sent().hasContent("jerome can use this functionality.");
            emailer.sent().hasSubject("he hopes@7");
        }
    }

    [TestFixture]
    public class TestPositionMonitor : DbTestCase {
        
        [Test]
        public void testEmails() {
            var abc = new Symbol("RE.TEST.TY.1C");
            var slippage = MarketTable.MARKET.fixedSlippage(abc.name);
            var monitor = new PositionMonitor(LIVE_SYSTEM, O.list(abc, new Symbol("DEF")), "A_TOPIC");
            var counter = new PublishCounter("OrderTracker.orderAdded");
            var order = abc.buy("Go LONG", new Stop(50), 10, FillOrKill.FILL_KILL).placed();
            var trade = new Trade(order, 52, 10, slippage, 1);
            var position = order.fill(trade);
            monitor.positionUpdate(position, trade, PositionMonitor.basicTradeEmail);
            monitor.goLive(O.list(position), abc);
            O.freezeNow("2008/09/22 14:47:04");
            LiveOrderEmailsTable.ORDER_EMAILS.insert("S-I-V", "PV", "ALL", "team");
            emailer.allowMessages();
            monitor.positionUpdate(position, trade, PositionMonitor.basicTradeEmail);
            var message = emailer.sent();
            message.hasSubject(@"S\(PV\) Filled Order for RE.TEST.TY.1C - " + O.hostname());
            message.hasContent(@"Order.*: Enter long 10 RE.TEST.TY.1C @ 52 STOP\(50.0000000\)");
            message.hasContent("Timestamp: 2008/09/22 14:47:04");
            message.hasContent("Description: Go LONG");
            emailer.clear();

            var liveOrders = O.list<LiveOrders.LiveOrder>(LiveOrders.ORDERS.ordersFilled(LIVE_SYSTEM.id(), abc.name));
            HasCount(1, liveOrders);
            var liveOrder = O.the(liveOrders);
            AreEqual(10, liveOrder.size());
            AreEqual("Enter", liveOrder.entryExit());
            AreEqual("long", liveOrder.positionDirection());
            AreEqual(liveOrder.id(), counter.getOneAndClear<int>("liveOrderId"));

            order = position.scaleUpLong("gimme more", new StopLimit(50, 60), 25, FillOrKill.FILL_KILL).placed();
            trade = new Trade(order, 55, 25, slippage, 1);
            order.fill(trade);
            monitor.positionUpdate(position, trade, PositionMonitor.basicTradeEmail);
            emailer.sent().hasContent(@"Order.*: Scale up long 25 RE.TEST.TY.1C @ 55 STOP_LIMIT\(50.0000000, 60.0000000\)");
            emailer.clear();
            liveOrders = O.list<LiveOrders.LiveOrder>(LiveOrders.ORDERS.ordersFilled(LIVE_SYSTEM.id(), abc.name));
            HasCount(2, liveOrders);
            liveOrder = O.first(liveOrders);
            AreEqual(25, liveOrder.size());
            AreEqual("Scale up", liveOrder.entryExit());
            AreEqual("long", liveOrder.positionDirection());

            order = position.scaleDownLong("gimme more", new Limit(55), 20, FillOrKill.FILL_KILL).placed();
            trade = new Trade(order, 55, 20, slippage, 1);
            order.fill(trade);
            monitor.positionUpdate(position, trade, PositionMonitor.basicTradeEmail);
            emailer.sent().hasContent(@"Order.*: Scale down long 20 RE.TEST.TY.1C @ 55 LIMIT\(55.0000000\)");
            emailer.clear();

            order = position.exitLong("Get out", new Market(), OneBar.ONE).placed();
            trade = new Trade(order, 40, 15, slippage, 1);
            order.fill(trade);
            monitor.positionUpdate(position, trade, PositionMonitor.basicTradeEmail);
            emailer.sent().hasContent(@"Order.*: Exit long 15 RE.TEST.TY.1C @ 40 MARKET");

            emailer.disallowMessages(); 
            LiveOrderEmailsTable.ORDER_EMAILS.deleteAll(Clause.TRUE);
            monitor.positionUpdate(position, trade, PositionMonitor.basicTradeEmail);

        }
    }
}
