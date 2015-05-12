using System.Collections.Generic;
using NUnit.Framework;
using Q.Recon;
using Q.Trading;
using Q.Util;
using systemdb.data;
using systemdb.live;
using systemdb.metadata;
using Bar=Q.Trading.Bar;
using O=Q.Util.Objects;
using Symbol=Q.Trading.Symbol;
using Tick=Q.Trading.Tick;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestOrderSubmitter : OneSymbolSystemTest<GenericTestSystem> {
        FakeFerret ferret;

        public override void setUp() {
            base.setUp();
            ferret = new FakeFerret();
            FerretControl.onOutgoing(fields => ferret.onMessage(fields));
            liveSystem.setAutoExecuteTrades(true);
            Order.clearCache();
        }

        class FakeFerret {
            readonly List<Fields> received = new List<Fields>();
            public void onMessage(Fields message) {
                lock(received) {
                    received.Add(message);
                }
            }
            
            public List<Fields> get() {
                return received;
            }

            public void requireNoMessage() {
                Bomb.unless(O.isEmpty(received), ()=> "unexpected messages " + O.toShortString(received));
            }

            public bool hasMessages(int length) {
                return received.Count >= length;
            }
        }

        [Test]
        public void testOrderSubmission() {
            O.freezeNow("2008/08/08 08:08:08");
            var order1 = buy("order 1", stop(99.50), 100, oneBar());
            symbolSystem.order(order1);
            processBar(1,3,1,2);
            hasOrders(order1);
            requireFerretHasOrders();
            processTick(2.5);
            requireFerretHasOrders(order1);
            emailer.allowMessages();
            var submitted = O.the<LiveOrders.LiveOrder>(LiveOrders.ORDERS.ordersSubmitted(systemId, symbol().name));
            AreEqual("Enter", submitted.entryExit());
            fill(order1, 123);
            submitted = O.the<LiveOrders.LiveOrder>(LiveOrders.ORDERS.ordersSubmitted(systemId, symbol().name));
            var filled = O.the(O.list<LiveOrders.LiveOrder>(LiveOrders.ORDERS.ordersFilled(systemId, symbol().name)));
            AreEqual(123.0, filled.price().doubleValue());
            AreEqual(submitted, filled);
            var order2 = sell("order 2", stop(97.50), 10, oneBar());
            symbolSystem.order(order2);
            processTick(3.0);
            requireFerretHasOrders(order1, order2);
            var order3 = sell("market", market(), 50, oneBar());
            symbolSystem.order(order3);
            processTick(3.0);
            requireFerretHasOrders(order1, order2);
            symbolSystem.order(symbolSystem.position().exit("exit 1", market(), onTheClose()));
            processTick(4.0);
            submitted = O.first(O.list<LiveOrders.LiveOrder>(LiveOrders.ORDERS.ordersSubmitted(systemId, symbol().name)));
            AreEqual("Exit", submitted.entryExit());
        }

        [Test]
        public void testFerretMessages() {
            O.freezeNow("2008/08/08 08:08:08");
            ExecutionConfigurationTable.CONFIG.insert("Future", "CurrentPlatform", "CurrentRoute");
            var order1 = buy("order 1", stop(99.50), 100, oneBar());
            symbolSystem.order(order1);
            processTick(2.5);
            requireFerretHasOrders(order1);
            order1.cancel();
            O.wait(() => ferret.hasMessages(2));
            var orderMessage = O.first(ferret.get());
            var cancelMessage = O.last(ferret.get());
            var orderId = order1.ferretSubmission.id;
            AreEqual(orderId, orderMessage.text("USERORDERID"));
            AreEqual("BLACKBOX", orderMessage.text("CLIENTUSERID"));
            AreEqual("TOMAHAWK", orderMessage.text("CLIENTAPPNAME"));
            AreEqual("2008-08-08", orderMessage.text("ORDERDATE"));
            AreEqual("CurrentRoute", orderMessage.text("ROUTE"));
            AreEqual("CurrentPlatform", orderMessage.text("PLATFORM"));

            AreEqual(orderId, cancelMessage.text("ORIGINALUSERORDERID"));
            AreEqual(orderId.Replace('T', 'C'), cancelMessage.text("USERORDERID"));
            AreEqual("CancelOrder", cancelMessage.text("MESSAGETYPE"));
            AreEqual("2008-08-08", cancelMessage.text("ORDERDATE"));
            AreEqual(O.hostname(), cancelMessage.text("CLIENTHOSTNAME"));
            AreEqual("BLACKBOX", cancelMessage.text("CLIENTUSERID"));
            AreEqual("TOMAHAWK", cancelMessage.text("CLIENTAPPNAME"));
        }


        void requireFerretHasOrders(params Order[] orders) {
            if(O.isEmpty(orders)) { ferret.requireNoMessage(); return; }
            O.wait(() => ferret.hasMessages(orders.Length));
            O.each(orders, ferret.get(), requireOrderMatches);
            AreEqual(orders.Length, LiveOrders.ORDERS.ordersSubmitted(systemId, symbol().name).size());
        }

        static void requireOrderMatches(Order order, Fields message) {
            AreEqual(order.symbol.name, message.text("SYMBOL"));

        }

        protected override int leadBars() {
            return 0;
        }

        protected override Parameters parameters() {
            return new Parameters{
                { "systemId", systemId },
                { "RunMode", (double) RunMode.LIVE }
            };
        }
    }

    public class GenericTestSystem : SymbolSystem {
        readonly List<Order> ordersToPlace = new List<Order>();

        public GenericTestSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {}
        protected override void onNewBar() {
            placeOrders();
        }

        void placeOrders() {
            each(ordersToPlace, placeOrder);
            ordersToPlace.Clear();
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            placeOrders();
        }
        public override bool runOnNewTick() {
            return true;
        }
        
        public void order(Order o) {
            ordersToPlace.Add(o);
        }

        protected override void onClose() {}
        protected override void onFilled(Position position, Trade trade) {}
    }
}