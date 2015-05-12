using NUnit.Framework;
using Q.Spuds.Core;
using Q.Trading;
using O = Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestExampleSymbolSystem : OneSymbolSystemTest<ExampleSymbolSystem> {


        protected override Parameters parameters() {
            return new Parameters{
                {"systemId", "" + systemId}, 
                {"RunMode", (int) RunMode.LIVE}, 
                {"lookback", "2"},
                {"stringParam", "foobar!"}
            };
        }

        public override void setUp() {
            base.setUp();
            symbolSystem.doRunOnTick = true;
            O.zeroTo(arguments().leadBars, i => {
                                            processBar(1, 3, 1, 2);
                                            noOrders();                           
                                        });
            emailer.allowMessages();
        }

        [Test]
        public void testSystem() {
            processBar(1, 3.5, 0, 2);
            hasOrders(buy("enter long", stop(3.5), 100, fillOrKill()));
            fill(0, 3.0);
            hasPosition(100);
            processBar(1, 2, 0, 2);
            hasOrders(
                sell("exit long", market(), 100, fillOrKill()),
                sell("exit long 2", market(), 100, fillOrKill())
            );
            fill(0, 2.0);
            noPositions();
            processBar(1, 2, 0, 2);
            processBar(1, 2, 0, 2);
            noOrders();
        }

        [Test]
        public void testPositionCantExitLongWhenShortAndViceVersa() {
            processBar(1, 3.5, 0, 2);
            hasOrders(buy("enter long", stop(3.5), 100, fillOrKill()));
            fill(0, 3.0);
            Bombs(() => position().exitShort("ima rdr", stop(3.0), fillOrKill()), "long position cannot be exited short!");
            var expected = sell("ima rdr", stop(3.0), 100, fillOrKill());
            var actual = position().exitLong("ima rdr", stop(3.0), fillOrKill());
            AreEqual(expected, actual);
        }

        [Test]
        public void testPositionCanPartialExitLong() {
            processBar(1, 3.5, 0, 2);
            hasOrders(buy("enter long", stop(3.5), 100, fillOrKill()));
            fill(0, 3.0);
            Bombs(() => position().scaleDownShort("ima rdr", stop(3.0), 45, fillOrKill()), "long position cannot be scaled down short!");
            Bombs(() => position().scaleDownLong("ima rdr", stop(3.0), 100, fillOrKill()), "trade size must be less than total size");
            var expected = sell("ima rdr", stop(3.0), 45, fillOrKill());
            var actual = position().scaleDownLong("ima rdr", stop(3.0), 45, fillOrKill());
            AreEqual(expected, actual);
        }

        [Test]
        public void testTick() {
            processTick(5);
            highestIs(5);
            highestIs(3, 1);
            processTick(6);
            highestIs(6);
            highestIs(3, 1);
            processTick(7);
            highestIs(7);
            highestIs(3, 1);
        }

        [Test]
        public void testTickSkippingWhenNotInRunOnTickMode() {
            symbolSystem.doRunOnTick = false;
            processTick(5, date("1980/01/01 10:00:00"));
            highestIs(5);
            highestIs(3, 1);
            processTick(6, date("1980/01/01 10:00:01"));
            highestIs(5);
            processTick(6, date("1980/01/01 10:01:00"));
            highestIs(6);
            highestIs(3, 1);
            processTick(7, date("1980/01/01 10:02:00"));
            highestIs(7);
            highestIs(3, 1);
        }

        class LiveSpud : Spud<bool> {
            bool state;

            public LiveSpud(Spud<double> spud) : base(spud.manager) {
                dependsOn(spud);
                manager.onLive += delegate { state = true; thyselfBeDirty();};
                
            }

            protected override bool calculate() {
                return state;
            }
        }

        [Test]
        public void testGoLive() {
            var baseSpud = new RootSpud<double>(bridge().manager);
            var spud = new LiveSpud(baseSpud);
            IsFalse(spud);
            processTick(1.5);
            IsTrue(spud);
        }

        [Test]
        public void testPositionPublish() {
            var counter = positionPublishCounter();
            // get a position in "simulation"
            processBar(1, 3.5, 0, 2);
            hasOrders(buy("enter long", stop(3.5), 100, fillOrKill()));
            fill(0, 3.0);
            // now we are live
            processTick(3.5);
            AreEqual(100, counter.getOne<int>("beginValue"));
            AreEqual(100, counter.getOne<int>("liveValue"));
            AreEqual(O.hostname(), counter.getOne<string>("hostname"));
            var beginTime = counter.getOne<string>("beginTimestamp");
            var liveTime = counter.getOne<string>("liveTimestamp");
            counter.clear();
            // this way we should get a new timestamp
            util.Dates.freezeNow(util.Dates.secondsAhead(1, util.Dates.now()));
            processTick(3.5);
            counter.requireNoMessages();
            hasOrders(
                sell("exit long", market(), 100, fillOrKill()),
                sell("exit long 2", market(), 100, fillOrKill())
            );
            fill(0, 2.0);
            O.sleep(100);
            AreNotEqual(liveTime, counter.getOne<string>("liveTimestamp"));
            AreEqual(beginTime, counter.getOne<string>("beginTimestamp"));
            AreEqual(100, counter.getOne<int>("beginValue"));
            AreEqual(0, counter.getOne<int>("liveValue"));
        }

        [Test]
        public void testTickOrders() {
            processTick(5);
            hasOrders(buy("enter long", stop(5), 100, fillOrKill()));
            processTick(5);
            hasOrders(buy("enter long", stop(5), 100, fillOrKill()));
        }

        void highestIs(int i) { highestIs(i, 0);}

        void highestIs(int i, int lookback) {
            AreEqual(i, symbolSystem.highest[lookback]);
        }

        protected override int leadBars() {
            return 5;
        }
    }
}