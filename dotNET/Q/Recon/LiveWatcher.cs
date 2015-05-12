using System;
using System.Collections.Generic;
using System.Data;
using db;
using NUnit.Framework;
using Q.Messaging;
using Q.Research;
using Q.Trading;
using Q.Util;
using systemdb.live;
using systemdb.metadata;
using util;
using Market=systemdb.metadata.Market;
using Objects=Q.Util.Objects;

namespace Q.Recon {
    public class LiveWatcher : Objects {
        readonly WatcherGui gui;
        readonly LazyDictionary<string, LazyDictionary<Symbol, Dictionary<LiveSystem, double>>> tags = 
            new LazyDictionary<string, LazyDictionary<Symbol, Dictionary<LiveSystem, double>>> (
                tag => new LazyDictionary<Symbol, Dictionary<LiveSystem, double>>(
                    symbol => new Dictionary<LiveSystem, double>()
        ));
        readonly LazyDictionary<string, Dictionary<MsivPv, DateTime>> lastUpdateTime = new LazyDictionary<string, Dictionary<MsivPv, DateTime>>(tag => new Dictionary<MsivPv, DateTime>());
        readonly Dictionary<string, DataRow> rowsByTagTicker = new Dictionary<string, DataRow>();
        readonly QDataTable table;
        readonly LazyDictionary<string, bool> isExcludedByTag = 
            new LazyDictionary<string, bool>(tag => WatcherExclusionsTable.EXCLUSIONS.isExcluded(tag));

        static string positionsBroker_ = DEFAULT_POSITIONS_BROKER;
        bool filterZeroes = true;
        bool showHidden_;
        string tagFilter = "ALL";
        internal const string DEFAULT_POSITIONS_BROKER = PositionTracker.DEFAULT_RECONCILIATION_BROKER;
        const string TICKER_TOPIC_BASE = "Positions.QMF.";

        internal static Topic tickerTopic(string tag, string ticker, string yellowKey) {
            return new Topic(TICKER_TOPIC_BASE + tag + "." + ticker + "_" + yellowKey.ToUpper(), positionsBroker_);
        }

        internal static void setPositionsBrokerForTest(string testBroker) {
            positionsBroker_ = testBroker;
        }

        public LiveWatcher(WatcherGui gui) {
            this.gui = gui;
            table = new QDataTable {
                {"tag", typeof (string)},
                {"symbol", typeof (string)},
                {"tomahawk", typeof (double)},
                {"aim", typeof (double)},
                {"diff", typeof (double)},
                {"symbolHIDDEN", typeof(Symbol)}
            };
            table.filter(row => {
                var visible = true;
                if (filterZeroes) visible = (double) row["aim"] != 0 || (double) row["tomahawk"] != 0;
                if (!showHidden_) visible &= !isExcludedByTag.get((string) row["tag"]);
                if (!tagFilter.Equals("ALL")) {
                    visible &= row["tag"].Equals(tagFilter);
                }
                return visible;
            });
        }

        public void initialize() {
            each(StatusTracker.allLiveSystems(), liveSystem => {
                var tag = liveSystem.bloombergTag();
                var tagTotals = tags.get(tag);
                each(list<MsivPv>(liveSystem.liveMarkets()), liveMarket => {
                    var symbol = new Symbol(new Market(liveMarket.market()));
                    if(!tagTotals.has(symbol)) {
                        var myRow = addRow(tag, nameString(symbol), rowKey(tag, symbol));
                        myRow["symbolHIDDEN"] = symbol;
                    }
                    var symbolTotals = tagTotals.get(symbol);
                    symbolTotals[liveSystem] = 0;

                    new Topic(liveSystem.topicName("TOMAHAWK", symbol.name + ".optimalPosition")).subscribe(fields => {
                        lock(symbolTotals) {
                            symbolTotals[liveSystem] = fields.numeric("liveValue");
                            lastUpdateTime.get(tag)[liveMarket] = date(fields.time("liveTimestamp"));
                            var total = sum(symbolTotals.Values);
                            gui.runOnGuiThread(() => {
                                var myRow = row(tag, symbol);
                                myRow["tomahawk"] = total; 
                                update(myRow); 
                            });
                        }
                    });
                });
            });
            new Topic("Positions.QMF.>", positionsBroker_).subscribe(fields => {
                var tag = fields.text("level1TagName");
                var ticker = fields.text("ticker");
                if(isEmpty(ticker)) ticker = fields.text("securityId");
                var yellowKey = Strings.javaClassify(fields.text("yellowKey").ToLower());
                gui.runOnGuiThread(() => {
                    var myRow = row(tag, ticker, yellowKey);
                    myRow["aim"] = fields.numeric("currentPosition");
                    update(myRow);
                });
            });
        }

        void update(DataRow dataRow) {
            var diff = (double) dataRow["tomahawk"] - (double) dataRow["aim"];
            dataRow["diff"] = diff;
            table.updateVisible(dataRow);
            gui.setStatus(dataRow, status(dataRow));
        }

        public static SystemStatus status(DataRow dataRow) {
            if(!dataRow.IsNull("symbolHIDDEN") && ((Symbol) dataRow["symbolHIDDEN"]).type().Equals("Equity"))
                return Math.Abs((double) dataRow["diff"]) < 100 ? SystemStatus.GREEN : SystemStatus.RED;
            return (double) dataRow["diff"] == 0 ? SystemStatus.GREEN : SystemStatus.RED;
        }

        DataRow addRow(string tag, string fullTicker, string rowKey) {
            var row = table.NewRow();
            row["tag"] = tag;
            row["symbol"] = fullTicker;
            row["tomahawk"] = 0;
            row["aim"] = 0;
            row["diff"] = 0;
            table.addAtStart(row);
            rowsByTagTicker[rowKey] = row;
            return row;
        }

        static string rowKey(string tag, Symbol symbol) {
            return tag + "_" + nameString(symbol);
        }

        static string rowKey(string tag, string ticker, string yellowKey) {
            return tag + "_" + nameString(ticker, yellowKey);
        }

        static string nameString(Symbol symbol) {
            return symbol.hasBloombergTicker() ? symbol.bloombergTicker() : "*" + symbol.name;
        }

        static string nameString(string bloombergTicker, string yellowKey) {
            return bloombergTicker + " " + yellowKey;
        }

        public void eachRow(Action<DataRow> onRow) {
            each(table.Rows, onRow);
        }

        public void requireContains(string tag, string ticker) {
            Assert.IsTrue(exists(table.Rows, row => row["tag"].Equals(tag) && row["symbol"].Equals(ticker)));
        }

        public DataRow row(string tag, Symbol symbol) {
            return rowsByTagTicker[rowKey(tag, symbol)];
        }

        DataRow row(string tag, string ticker, string yellowKey) {
            var k = rowKey(tag, ticker, yellowKey);
            if (!rowsByTagTicker.ContainsKey(k)) addRow(tag, nameString(ticker, yellowKey), k);
            return rowsByTagTicker[k];
        }

        public DataTable dataTable() {
            return table;
        }

        public DataRow firstRow() {
            return dataTable().Rows[0];
        }

        public void requireVisible(int expected) {
            Assert.AreEqual(expected, visible());
        }

        public int visible() {
            return table.visibleRowCount();
        }

        public void setFilterZeroes(bool doFilter) {
            filterZeroes = doFilter;
            table.updateVisible();
        }

        public void setShowHidden(bool setting) {
            showHidden_ = setting;
            table.updateVisible();
        }

        public void setTagFilter(string tag) {
            tagFilter = tag;
            table.updateVisible();
        }

        public void removeExclusion(DataRow selected) {
            var tag = (string) selected["tag"];
            WatcherExclusionsTable.EXCLUSIONS.remove(tag);
            Db.commit();
            isExcludedByTag.overwrite(tag, false);
            table.updateVisible();
        }

        public void addExclusion(DataRow selected) {
            var tag = (string) selected["tag"];
            WatcherExclusionsTable.EXCLUSIONS.insert(tag);
            Db.commit();
            isExcludedByTag.overwrite(tag, true);
            table.updateVisible();
        }

        public List<DataRow> visibleRows() {
            return table.visibleRows();
        }

        public void eachVisible(Action<DataRow> onRow) {
            table.eachVisibleRow(onRow);
        }
    }
}