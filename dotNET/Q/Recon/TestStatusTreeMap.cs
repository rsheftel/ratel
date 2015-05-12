using System;
using jms;
using NUnit.Framework;
using Q.Systems;
using Q.Systems.Examples;
using Q.Trading;
using Q.Util;
using systemdb.data;
using O=Q.Util.Objects;

namespace Q.Recon {
    class FakeStatusMapGUI : FakeGUI, StatusMapGUI {
        public void launcherAvailable(string host, DateTime time) {
            throw new NotImplementedException();
        }
    }

    [TestFixture] public class TestStatusTreeMap : DbTestCase {


        [Test] public void testOneSubscribe() {
            OrderTable.prefix = "PREFIX";
            O.freezeNow("2009/07/10 13:00:00");
            var liveSystem = OneSystemTest<EmptySystem>.fakeLiveSystem(new Parameters(), false);
            liveSystem.removeAllLiveMarkets();
            insertMarket("FOO", 0);
            liveSystem.addLiveMarket("FOO", "2009/07/07", null);
            insertMarket("BAR", 0);
            liveSystem.addLiveMarket("BAR", "2009/07/07", null);
            var fooTopic = new QTopic("PREFIX.TestSystem1.1.0.daily.Slow.FOO.heartbeat");
            O.timerManager().isInterceptingTimersForTest = true;

            O.timerManager().intercept("2009/07/10 13:00:00", "gui update");
            var map = new StatusTreeMap(new FakeStatusMapGUI(), O.list(liveSystem));
            map.setIsEqualSizes(false);
            var systemNode = O.the(map.nodes());
            AreEqual("TestSystem1", systemNode.text);
            var pvNode = O.the(systemNode.children());
            AreEqual("Slow", pvNode.text);
            var symbolNodes = O.dictionaryFromValues(O.convert(pvNode.children(), child => child as LiveMarketNode), child => child.id);
            AreEqual(1d, symbolNodes["FOO"].size);
            AreEqual(0, symbolNodes["FOO"].ticksReceived);
            AreEqual(1d, symbolNodes["BAR"].size);
            var message = new Fields();
            message.put("timestamp", "2009/07/10 12:59:59");
            message.put("lastTickProcessed", "2009/07/10 12:59:58");
            message.put("ticksReceived", "200");
            message.put("hostname", "hullabaloo");
            fooTopic.send(message); 
            Objects.timerManager().intercept("2009/07/10 13:00:01", "second gui update");
            O.timerManager().runTimers("2009/07/10 13:00:00");
            waitMatches(200d, () => symbolNodes["FOO"].ticksReceived); // you just
        }
    }
}