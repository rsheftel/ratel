using System;
using System.Collections.Generic;
using System.Data;
using jms;
using NUnit.Framework;
using Q.Messaging;
using Q.Trading;
using Q.Util;
using systemdb.live;
using Channel=Q.Messaging.Channel;
using O = Q.Util.Objects;

namespace Q.Recon {
    [TestFixture]
    public class TestLiveWatcher : DbTestCase {
        public override void setUp() {
            base.setUp();
            LiveWatcher.setPositionsBrokerForTest(JMSTestCase.TEST_BROKER2);
        }

        [Test]
        public void testIsolatedTomahawkMessages() {
            O.freezeNow("07/01/2009");
            MarketTickersTable.TICKERS.C_BLOOMBERG.updateOne(MarketTickersTable.TICKERS.C_MARKET.@is("FV.1C"), "anything");
            var nday20 = new Topic("TOMAHAWK.NDayBreak.1.0.daily.BFBD20.FV.1C.optimalPosition");
            var nday30 = new Topic("TOMAHAWK.NDayBreak.1.0.daily.BFBD30.FV.1C.optimalPosition");
            var fakeGui = new FakeWatcherGui();
            var watcher = new LiveWatcher(fakeGui);
            watcher.initialize();
            watcher.eachRow(row => AreEqual(row["tomahawk"], 0));
            watcher.requireContains("QF.NDayBreak", "anything Comdty");
            var fv1C = new Symbol("FV.1C");
            Action<double> waitForCount = expected => fakeGui.waitMatches(expected, () => watcher.row("QF.NDayBreak", fv1C)["tomahawk"]);
            publishTomahawk(nday20, 3, "2009/07/01 14:00:00");
            waitForCount(3);
            publishTomahawk(nday20, 2, "2009/07/01 14:00:01");
            waitForCount(2);
            publishTomahawk(nday30, 3, "2009/07/01 14:00:03");
            waitForCount(5);
        }

        [Test]
        public void testIsolatedAimMessages() {
            O.freezeNow("07/01/2009");
            MarketTickersTable.TICKERS.C_BLOOMBERG.updateOne(MarketTickersTable.TICKERS.C_MARKET.@is("FV.1C"), "anything");
            MarketTickersTable.TICKERS.C_BLOOMBERG.updateOne(MarketTickersTable.TICKERS.C_MARKET.@is("TU.1C"), "TOE");
            var fvTopic = LiveWatcher.tickerTopic("QF.NDayBreak", "anything", "Comdty");
            var tuTopic = LiveWatcher.tickerTopic("QF.NDayBreak", "TOE", "Comdty");
            var brokenTopic = LiveWatcher.tickerTopic("QF.NDayBreak", "BROKE", "Comdty");
            var fakeGui = new FakeWatcherGui();
            var watcher = new LiveWatcher(fakeGui);
            watcher.initialize();
            watcher.eachRow(row => AreEqual(row["aim"], 0));
            watcher.requireContains("QF.NDayBreak", "anything Comdty");
            var fv1C = new Symbol("FV.1C");
            var tu1C = new Symbol("TU.1C");
            Action<Symbol, double> waitForCount = (symbol, expected) => fakeGui.waitMatches(expected, () => watcher.row("QF.NDayBreak", symbol)["aim"]);
            publishAim(fvTopic, "QF.NDayBreak", "anything", "COMDTY", 3, "2009/07/01 14:00:00");
            waitForCount(fv1C, 3);
            publishAim(fvTopic, "QF.NDayBreak", "anything", "COMDTY",  2, "2009/07/01 14:00:01");
            waitForCount(fv1C, 2);
            publishAim(tuTopic, "QF.NDayBreak", "TOE", "COMDTY",  5, "2009/07/01 14:00:03");
            waitForCount(tu1C, 5);
            publishAim(brokenTopic, "QF.NDayBreak", "BROKE", "COMDTY",  7, "2009/07/01 14:00:03");
            fakeGui.waitMatches(7.0, () => watcher.firstRow()["aim"]);
            AreEqual(watcher.firstRow()["symbol"], "BROKE Comdty");
        }

        [Test]
        public void testIntegratedMessages() {
            O.freezeNow("07/01/2009");
            MarketTickersTable.TICKERS.C_BLOOMBERG.updateOne(MarketTickersTable.TICKERS.C_MARKET.@is("FV.1C"), "anything");
            MarketTickersTable.TICKERS.C_BLOOMBERG.updateOne(MarketTickersTable.TICKERS.C_MARKET.@is("TU.1C"), "TOE");
            var nday20 = new Topic("TOMAHAWK.NDayBreak.1.0.daily.BFBD20.FV.1C.optimalPosition");
            var nday30 = new Topic("TOMAHAWK.NDayBreak.1.0.daily.BFBD30.FV.1C.optimalPosition");
            var fvTopic = LiveWatcher.tickerTopic("QF.NDayBreak", "anything", "Comdty");
            var tuTopic = LiveWatcher.tickerTopic("QF.NDayBreak", "TOE", "Comdty");
            var brokenTopic = LiveWatcher.tickerTopic("QF.NDayBreak", "BROKE", "Comdty");
            var fakeGui = new FakeWatcherGui();
            var watcher = new LiveWatcher(fakeGui);
            watcher.initialize();
            var fv1C = new Symbol("FV.1C");
            var tu1C = new Symbol("TU.1C");
            tu1C.setTypeForTest("Equity");
            Action<Symbol, double, double, double> waitForCount = (symbol, aim, tomahawk, diff) => {
                var last = new [] {0.0, 0, 0};
                try {                 
                    fakeGui.wait(() => {
                        var row = watcher.row("QF.NDayBreak", symbol);
                        last[0] = (double) row["aim"];
                        last[1] = (double) row["tomahawk"];
                        last[2] = (double) row["diff"];
                        return last[0] == aim && last[1] == tomahawk && last[2] == diff;
                    });
                } catch (Exception e) {
                    throw Bomb.toss("count never matched - last: " + O.toShortString(last), e);
                }
            };
            publishAim(fvTopic, "QF.NDayBreak", "anything", "COMDTY", 3, "2009/07/01 14:00:00");
            waitForCount(fv1C, 3, 0, -3);
            AreEqual(SystemStatus.RED, fakeGui.status(watcher.row("QF.NDayBreak", fv1C)));
            publishAim(fvTopic, "QF.NDayBreak", "anything", "COMDTY",  2, "2009/07/01 14:00:01");
            waitForCount(fv1C, 2, 0, -2);
            AreEqual(SystemStatus.RED, fakeGui.status(watcher.row("QF.NDayBreak", fv1C)));
            publishAim(tuTopic, "QF.NDayBreak", "TOE", "COMDTY",  105, "2009/07/01 14:00:03");
            waitForCount(tu1C, 105, 0, -105);
            AreEqual(SystemStatus.RED, fakeGui.status(watcher.row("QF.NDayBreak", tu1C)));
            publishAim(tuTopic, "QF.NDayBreak", "TOE", "COMDTY",  5, "2009/07/01 14:00:03");
            waitForCount(tu1C, 5, 0, -5);
            AreEqual(SystemStatus.GREEN, fakeGui.status(watcher.row("QF.NDayBreak", tu1C)));
            publishAim(brokenTopic, "QF.NDayBreak", "BROKE", "COMDTY",  7, "2009/07/01 14:00:03");
            fakeGui.waitMatches(7.0, () => watcher.firstRow()["aim"]);
            AreEqual(SystemStatus.RED, fakeGui.status(watcher.firstRow()));
            AreEqual(watcher.firstRow()["symbol"], "BROKE Comdty");
            publishTomahawk(nday20, 11, "2009/07/01 14:00:04");
            waitForCount(fv1C, 2, 11, 9);
            AreEqual(SystemStatus.RED, fakeGui.status(watcher.row("QF.NDayBreak", fv1C)));
            publishTomahawk(nday20, 2, "2009/07/01 14:00:04");
            waitForCount(fv1C, 2, 2, 0);
            AreEqual(SystemStatus.GREEN, fakeGui.status(watcher.row("QF.NDayBreak", fv1C)));

        }

        [Test]
        public void testHideShowZeros() {
            O.freezeNow("07/01/2009");
            MarketTickersTable.TICKERS.C_BLOOMBERG.updateOne(MarketTickersTable.TICKERS.C_MARKET.@is("FV.1C"), "anything");
            MarketTickersTable.TICKERS.C_BLOOMBERG.updateOne(MarketTickersTable.TICKERS.C_MARKET.@is("TU.1C"), "TOE");
            var nday20 = new Topic("TOMAHAWK.NDayBreak.1.0.daily.BFBD20.FV.1C.optimalPosition");
            var tuTopic = LiveWatcher.tickerTopic("QF.NDayBreak", "TOE", "Comdty");
            var fakeGui = new FakeWatcherGui();
            var watcher = new LiveWatcher(fakeGui);
            watcher.initialize();
            watcher.requireVisible(0);
            publishAim(tuTopic, "QF.NDayBreak", "TOE", "COMDTY",  5, "2009/07/01 14:00:03");
            fakeGui.waitMatches(1, watcher.visible);
            publishTomahawk(nday20, 11, "2009/07/01 14:00:04");
            fakeGui.waitMatches(2, watcher.visible);
            watcher.setFilterZeroes(false);
            fakeGui.wait(() => watcher.visible() > 10);

        }

        [Test]
        public void testTagFilter() {
            O.freezeNow("07/01/2009");
            var fakeGui = new FakeWatcherGui();
            var watcher = new LiveWatcher(fakeGui);
            watcher.initialize();
            watcher.setFilterZeroes(false);
            fakeGui.wait(() => watcher.visible() > 10);
            var count = watcher.visible();
            watcher.setTagFilter("QF.NDayBreak");
            fakeGui.wait(() => watcher.visible() < count);
            watcher.eachVisible(row => AreEqual("QF.NDayBreak", row["tag"]));
        }

        

        [Test]
        public void testShowHidden() {
            O.freezeNow("07/01/2009");
            WatcherExclusionsTable.EXCLUSIONS.insert("QF.NDayBreak");
            MarketTickersTable.TICKERS.C_BLOOMBERG.updateOne(MarketTickersTable.TICKERS.C_MARKET.@is("TU.1C"), "TOE");
            var tuTopic = LiveWatcher.tickerTopic("QF.NDayBreak", "TOE", "Comdty");
            var fakeGui = new FakeWatcherGui();
            var watcher = new LiveWatcher(fakeGui);
            watcher.initialize();
            watcher.requireVisible(0);
            publishAim(tuTopic, "QF.NDayBreak", "TOE", "COMDTY",  5, "2009/07/01 14:00:03");
            fakeGui.waitMatches(0, watcher.visible);
            watcher.setShowHidden(true);
            fakeGui.waitMatches(1, watcher.visible);
            var visible = O.first(watcher.visibleRows());

            watcher.setShowHidden(false);
            fakeGui.waitMatches(0, watcher.visible);

            watcher.removeExclusion(visible);
            fakeGui.waitMatches(1, watcher.visible);
            
            watcher.addExclusion(visible);
            fakeGui.waitMatches(0, watcher.visible);
            
        }

        static void publishAim(Channel topic, string tag, string ticker, string yellowKey, int position, string timestamp) {
            topic.send(new Dictionary<string, object> {
                {"TIMESTAMP", timestamp},
                {"level1TagName", tag},
                {"currentPosition", position + ""},
                {"ticker", ticker},
                {"yellowKey", yellowKey},
            });
        }

        static void publishTomahawk(Channel topic, int position, string timestamp) {
            topic.send(new Dictionary<string, object> {
                {"liveValue", position},
                {"liveTimestamp", timestamp}
            });
        }
    }

    public class FakeWatcherGui : FakeGUI, WatcherGui {
        readonly Dictionary<DataRow, SystemStatus> statuses = new Dictionary<DataRow, SystemStatus>();

        public SystemStatus status(DataRow row) {
            return statuses[row];
        }

        public void setStatus(DataRow row, SystemStatus status) {
            statuses[row] = status;
        }
    }
}