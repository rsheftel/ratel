using System;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;
using Position=Q.Trading.Position;
using Symbol=Q.Trading.Symbol;
using Trade=Q.Trading.Trade;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestOnClose : OneSymbolSystemTest<TestOnCloseSystem>{


        [Test]
        public void testOnClose() {
            var bar = new Bar(1, 1, 1, 1);
            processClose(bar);
            IsFalse(symbolSystem.onCloseTriggered);
            processBar(bar);
            bar = new Bar(2, 2, 2, 2);
            processClose(bar);
            IsTrue(symbolSystem.onCloseTriggered);
            processBar(bar);
            bar = new Bar(3, 3, 3, 3);
            processClose(bar);
            processBar(bar);
        }

        [Test]
        public void testOnCloseLive() {
            O.freezeNow("2009/01/01 09:00:00");
            var bar = new Bar(1, 1, 1, 1);
            processBar(bar);
            var closeTime = date("2009/01/01 12:34:56");
            symbolSystem.processCloseAt = closeTime;
            O.timerManager().isInterceptingTimersForTest = true;
            O.timerManager().intercept("2009/01/01 12:34:56", "subsystem close");
            O.timerManager().intercept("2009/01/11 09:00:00", "multisystem close");
            O.timerManager().intercept("2009/01/01 09:00:00", "system heartbeat");
            processTick(3, closeTime);
            processTick(2, closeTime);
            IsFalse(symbolSystem.onCloseTriggered);
            O.timerManager().runTimers(closeTime);
            IsTrue(symbolSystem.onCloseTriggered);
            AreEqual(2.0, symbolSystem.lastCloseProcessed);
        }

        [Test]
        public void testOnCloseCalledInForecastMode() {
            var date = O.date("2009/09/09 11:22:33");
            var bar = new Bar(1, 1, 1, 1, date);
            processBar(bar);
            var counter = positionPublishCounter();
            processTick(2, date);
            counter.requireCount(2);
            AreEqual(0, counter.get<int>(1, "beginValue"));
            AreEqual(0, counter.get<int>(1, "liveValue"));
            AreEqual(100, counter.get<int>(1, "forecastCloseValue"));
            date = date.AddSeconds(59);
            symbolSystem.tickProcessed = false;
            processTick(2, date);
            IsFalse(symbolSystem.tickProcessed);
            counter.requireCount(2);
            date = date.AddSeconds(1);
            processTick(2, date);
            IsTrue(symbolSystem.tickProcessed);
            counter.requireCount(3);
            AreEqual(100, counter.get<int>(2, "forecastCloseValue"));
        }

        [Test]
        public void testOnCloseLogicIsCompletelySkippedWhenTurnedOffBySystem() {
            symbolSystem.useOnCloseLogic = false;
            var bar = new Bar(1, 1, 1, 1);
            processBar(bar);
            processTick(2);
            O.sleep(100);
            IsFalse(symbolSystem.onCloseTriggered);
            O.sleep(1100);
            IsFalse(symbolSystem.onCloseTriggered);
        }

        protected override int leadBars() {
            return 1;
        }
    }

    public class TestOnCloseSystem : SymbolSystem {
        public bool onCloseTriggered;
        public bool useOnCloseLogic = true;
        internal DateTime processCloseAt;
        public bool tickProcessed;
        internal double lastCloseProcessed;

        public TestOnCloseSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            bars.close.prepare();
        }

        protected override void onNewBar() {
            Assert.AreEqual(bars[0], bar);
            Assert.AreEqual(bars.count(), bars.close[0]);
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {}

        protected override void onClose() {
            if (!forecastMode) {
                onCloseTriggered = true;
                lastCloseProcessed = bar.close;
                Assert.AreEqual(bars.count(), bars.close[0]);
            }
            tickProcessed = true;
            Assert.AreEqual(bars[0], bar);
            placeOrder(symbol.buy("buy me", market(), 100, oneBar()));
        }

        protected override void onFilled(Position position, Trade trade) {}

        public override DateTime onCloseTime() {
            return processCloseAt;
        }

        public override bool runOnClose() {
            return useOnCloseLogic;
        }
    }
}