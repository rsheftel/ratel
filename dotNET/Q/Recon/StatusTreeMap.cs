using System;
using System.Collections.Generic;
using System.Threading;
using Q.Messaging;
using Q.Simulator;
using Q.Trading;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using Symbol=Q.Trading.Symbol;

namespace Q.Recon {
    public class StatusTreeMap : Objects, TreeMapModel {
        readonly StatusMapGUI gui;
        readonly LazyDictionary<MsivPv, TickTracker> ticks = new LazyDictionary<MsivPv, TickTracker>(s => new TickTracker());
        readonly LazyDictionary<MsivPv, Averager> ticksReceived = new LazyDictionary<MsivPv, Averager>(s => new Averager(60));
        bool isEqualSizes_ = true;
        Action updateModelNodes = ()=>{};
        Timer timer;
        readonly List<LiveSystem> systems;

        public StatusTreeMap(StatusMapGUI gui) : this(gui,  StatusTracker.allLiveSystems()) {}

        public StatusTreeMap(StatusMapGUI gui, List<LiveSystem> liveSystems) {
            this.gui = gui;
            systems = liveSystems;
        }

        public List<QNode> nodes() {
            var bySystem = new LazyDictionary<string, List<LiveSystem>> (system => new List<LiveSystem>());
            each(systems, liveSystem => bySystem.get(liveSystem.siv().system()).Add(liveSystem));
            var result = list(convert(bySystem.keys(), system => {
                var liveSystems = bySystem.get(system);
                var systemNode = new QNode(system, liveSystems.Count, 0);
                each(liveSystems, liveSystem => {
                    var liveSystemNode = systemNode.add(new LiveSystemNode(liveSystem, 1, 0));
                    var liveMarkets = list<MsivPv>(liveSystem.liveMarkets());
                    each(liveMarkets, liveMarket => {
                        try {
                            var symbol = new Symbol(liveMarket.market());
                            var liveMarketNode = new LiveMarketNode(symbol, 1, 0);
                            liveSystemNode.add(liveMarketNode);
                            var topic = new Topic(liveSystem.topicName(OrderTable.prefix, symbol.name + "." + SystemHeartbeat.SUFFIX));
                            topic.subscribeIfNeeded();
                            updateModelNodes += () => updateNode(topic, liveMarket, liveMarketNode);
                            symbol.subscribe(bar => recordMarketDataTickReceived(liveMarket, bar.time));
                            topic.subscribe(fields => {
                                var tickTime = fields.time("lastTickProcessed");
                                ticks.get(liveMarket).systemProcessed(date(tickTime));
                            });
                        } catch(Exception ex) {
                            LogC.err("exception caught subscribing to tick data for " + liveMarket + ", " + system, ex);
                            gui.alertUser("exception caught susbcribing to data for " + liveMarket + ", " + system + ".\nSkipping... see log for details.");
                        }
                    });
                    updateModelNodes += () => updateNode(liveSystem, liveSystemNode);
                });
                updateModelNodes += () => updateNode(systemNode);
                return systemNode;
            }));
            timerManager().everyMillis(1000, updateModelNodes, out timer);
            LiveLauncher.subscribeHeartbeat(gui.launcherAvailable);
            LogC.ignore(timer);
            return result;
        }



        public void setIsEqualSizes(bool newSetting) {
            isEqualSizes_ = newSetting;
            updateModelNodes();
        }

        void updateNode(Topic topic, MsivPv liveMarket, LiveMarketNode node) {
            var averager = ticksReceived.get(liveMarket);
            if(!topic.has("ticksReceived")) return;
            node.ticksReceived = topic.get<int>("ticksReceived");
            node.runningOn = topic.get<string>("hostname");
            averager.add(node.ticksReceived);
            node.tickRate = (float) averager.movingAverage();
            node.size = isEqualSizes_ ? 1 : Math.Max(1F, node.tickRate);
            node.tickLag = ticks.get(liveMarket).tickLag;
            node.color = node.tickLag - 5000;
            node.isDown = now().Subtract(date(topic.get<string>("timestamp"))).TotalSeconds < 10;
            var downText = node.isDown ? "" : "DOWN ";
            node.lastTickProcessed = ticks.get(liveMarket).lastTickProcessed();
            node.text = 
                downText + liveMarket.market() + "\n" + 
                node.tickRate.ToString("n0") + "/min, " + node.tickLag + "ms\n" + 
                node.ticksReceived.ToString("n0") + "\n" + 
                node.lastTickProcessed.ToString("HH:mm:ss") + "\n" +
                node.runningOn  ;             
        }

        void updateNode(LiveSystem liveSystem, QNode node) {
            var childNodes = list<LiveMarketNode>(node.children());
            var totalTicks = sum(convert(childNodes, child => child.ticksReceived));
            if(totalTicks == 0) return;
            var tickRate = sum(convert(childNodes, child => child.tickRate));
            var tickLag = max(convert(childNodes, child => child.tickLag));
            node.color = tickLag - 5000;
            node.size = isEqualSizes_ ? 1 : Math.Max(1F, tickRate);
            var isDown = exists(childNodes, child => child.isDown);
            var downText = isDown ? "" : "DOWN ";
            var lastTickProcessed = max(convert(childNodes, child => child.lastTickProcessed));
            node.text = 
                downText +
                liveSystem.pv().name() + " - " + liveSystem.id() + "\n" + 
                tickRate.ToString("n0") + "/min, " + tickLag + "ms\n" + 
                totalTicks.ToString("n0") + "\n" + 
                lastTickProcessed.ToString("HH:mm:ss") + "\n"; 
        }

        static void updateNode(QNode node) {
            node.size = sum(convert(node.children(), child => child.size));
            node.color = max(convert(node.children(), child => child.color));
        }
        
        void recordMarketDataTickReceived(MsivPv liveMarket, DateTime time) {
            ticks.get(liveMarket).marketDataTicked(time);
        }

        public static void kill(QNode selected) {
            if(selected is LiveMarketNode || selected is LiveSystemNode) {
                kill(selected.parent());
                return;
            }
            kill(selected.text);
        }

        public void killAndRestart(QNode selected, string runOn) {
            if(selected is LiveMarketNode || selected is LiveSystemNode) {
                killAndRestart(selected.parent(), runOn);
                return;
            }
            restart(runOn, selected.text);
        }
    

        void restart(string hostname, string system) {
            if (BloombergTagsTable.TAGS.anyAutoExecute(system) && !FerretControl.status().Equals("Stage")) {
                var answer = gui.askUser("This will put Ferret into Stage mode.  Are you sure you want to do this?");
                if (answer != YesNoCancel.YES) return;
                FerretControl.requestFerretChange("Stage");
                wait(()=> FerretControl.status().Equals("Stage"));
            }
            var fields = new Fields();
            fields.put("System", system);
            fields.put("Hostname", hostname);
            fields.put("Timestamp", ymdHuman(now()));
            LiveLauncher.restartTopic().send(fields);
        }

        static void kill(string system) {
            LiveLauncher.killTopic().send("System", system + "");
        }
    }

    public interface StatusMapGUI : QGUI {
        void launcherAvailable(string host, DateTime time);
    }

    public class QTypedNode<T> : QNode {
        readonly T content_;
        public QTypedNode(T t, String name, double size, double color) : base(name, size, color) {
            content_ = t;
        }
        public T content() { return content_; }
    }

    public class LiveMarketNode : QTypedNode<Symbol> {
        public readonly string id;
        public string runningOn;
        public int ticksReceived;
        public double tickRate;
        public double tickLag;
        public bool isDown;
        public DateTime lastTickProcessed;

        public LiveMarketNode(Symbol symbol, double size, double color) : base(symbol, symbol.name, size, color) {
            id = symbol.name;
        }
    }

    public class LiveSystemNode : QTypedNode<LiveSystem> {
        public LiveSystemNode(LiveSystem liveSystem, double size, double color) : base(liveSystem,liveSystem.pv().name(), size, color) {}
    }

    class Averager : Objects {
        readonly int length;
        readonly List<double> doubles = new List<double>();
        public Averager(int length) {
            this.length = length;
        }

        public void add(double d) {
            if(doubles.Count == length) doubles.RemoveAt(0);
            if(hasContent(doubles) && d < doubles[0]) doubles.Clear();
            doubles.Add(d);
        }

        public double movingAverage() {
            if(isEmpty(doubles)) return double.NaN;
            return (last(doubles) - first(doubles)) * length / doubles.Count;
        }
    }
}