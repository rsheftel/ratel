using System;
using System.Collections.Generic;
using db;
using java.util;
using Q.Recon;
using Q.Util;
using systemdb.data;
using systemdb.live;
using systemdb.metadata;
using util;

namespace Q.Trading {
    public class Order : Util.Objects, IEquatable<Order>, IDisposable {
        const long MAX_TRADE_SIZE = 1000000000000;
        public static readonly Order ANY = new Order("==ANY==", Symbol.NULL, market(), Direction.LONG, 1,  OneBar.ONE);
        public readonly string description;
        public readonly Symbol symbol;
        public readonly OrderDetails details;
        public readonly long size;
        public readonly OrderDuration duration;
        public readonly Direction direction;
        Position position_;
        string stack;
        bool isPlacedOnClose;
        bool cancelled;
        internal OrderSubmission ferretSubmission;
        System placingSystem;
        static int nextIdentifier;
        public readonly int id;
        string enterExit_ = "Enter";
        static readonly LazyDictionary<string, ExecutionConfigurationTable.Configuration> configs = 
            new LazyDictionary<string, ExecutionConfigurationTable.Configuration>(ExecutionConfigurationTable.currentConfiguration);

        internal class OrderSubmission {
            internal readonly string id;
            public int liveOrderId;
            readonly Date submittedAt;

            public OrderSubmission() : this("T" + Strings.leftZeroPad(OrderCounter.COUNTER.nextId(), 5), Dates.now()) {}

            public OrderSubmission(string id, Date submittedAt) {
                this.id = id;
                this.submittedAt = submittedAt;
            }

            public void addToFerretOrderSubmit(Fields fields) {
                fields.put("USERORDERID", id);
                fields.put("ORDERDATE", FerretControl.ferretDate(submittedAt));
                addClientInfo(fields);
            }

            public void sendCancelToFerret() {
                var fields = new Fields();
                fields.put("ORIGINALUSERORDERID", id);
                fields.put("USERORDERID", cancelId());
                fields.put("MESSAGETYPE", "CancelOrder");
                fields.put("ORDERDATE", FerretControl.ferretDate(submittedAt));
                addClientInfo(fields);
                FerretControl.outgoing().send(fields);
            }

            static void addClientInfo(Map fields) {
                fields.put("CLIENTHOSTNAME", hostname());
                fields.put("CLIENTUSERID", "BLACKBOX");
                fields.put("CLIENTAPPNAME", "TOMAHAWK");
            }

            string cancelId() {
                return id.Replace('T', 'C');
            }

            public void setLiveOrderId(int newLiveOrderId) {
                liveOrderId = newLiveOrderId;
            }
        }
        
        void cancelFerretOrder() {
            ferretSubmission.sendCancelToFerret();
        }

        public void addToFerret(Fields fields, OrderSubmitter submitter) {
            fields.put("SYMBOL", symbol.name);
            details.addToFerret(fields);
            duration.addToFerret(fields);
            ferretSubmission = new OrderSubmission();
            ferretSubmission.addToFerretOrderSubmit(fields);
            fields.put("SIDE", direction.longShort("BUY", "SELL"));
            fields.put("QUANTITY", size);
            var config = configs.get(symbol.type());
            fields.put("PLATFORM", config.platform());
            fields.put("ROUTE", config.route());
            fields.put("SECURITYTYPE", symbol.type().Equals("Future") ? "FUTURES" : "EQUITY");
            fields.put("STATUS", "NEW");
            fields.put("MESSAGETYPE", "NewOrder");
        }

        public Order(String description, Symbol symbol, OrderDetails details, Direction direction, long size, OrderDuration duration) {
            this.description = description;
            this.symbol = symbol;
            this.details = details;
            this.size = size;
            this.duration = duration;
            this.direction = direction;
            stack = reDebug() ? Environment.StackTrace : "locked";
            Bomb.when(size <= 0, () => "size must be > 0: " + this);
            Bomb.when(size > MAX_TRADE_SIZE, () => "size must be < 1 trillion (for now - expand if this is not an error): " + this);
            duration.requireSupported(details);
            id = nextIdentifier++;
        }

        ~Order() {
            Dispose();
        }

        public void Dispose() {
            GC.SuppressFinalize(this);
            if (this == ANY) return;
            Bomb.unlessNull(stack, () => "unplaced order " + this + " created at:\n" + stack);
            stack = null;
        }

        public override string ToString() {
            return description + ":" + details + " " + direction + " " + symbol + " " + size + " " + duration;
        }

        public bool Equals(Order obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return 
                Equals(obj.direction, direction) && 
                Equals(obj.duration, duration) && 
                obj.size == size && 
                Equals(obj.details, details) && 
                Equals(obj.symbol, symbol) && 
                Equals(obj.description, description);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (Order) && Equals((Order) obj);
        }

        public override int GetHashCode() {
            unchecked {
                var result = (direction != null ? direction.GetHashCode() : 0);
                result = (result * 397)^(duration != null ? duration.GetHashCode() : 0);
                result = (result * 397)^size.GetHashCode();
                result = (result * 397)^(details != null ? details.GetHashCode() : 0);
                result = (result * 397)^(symbol != null ? symbol.GetHashCode() : 0);
                result = (result * 397)^(description != null ? description.GetHashCode() : 0);
                return result;
            }
        }

        public void refersTo(Position position) {
            position_ = position;
            if(position.direction().Equals(direction)) enterExit_ = "Scale up";
            else enterExit_ = position.size.Equals(size) ? "Exit" : "Scale down";
        }

        public bool hasPosition() {
            return position_ != null;
        }

        public Position fill(Trade trade) {
            if (! hasPosition()) position_ = new Position(symbol, Position.nextId());
            position_.addTrade(trade);
            return direction.advance(position_, size);
        }

        public static Market market() { return new Market(); }
        public static Stop stop(double stopLevel) { return new Stop(stopLevel); }
        public static Limit limit(double limitLevel) { return new Limit(limitLevel); }
        public static StopLimit stopLimit(double level) { return stopLimit(level, level); }
        public static StopLimit stopLimit(double stopLevel, double limitLevel) { return new StopLimit(stopLevel, limitLevel); }
        public static ProtectiveStop protectiveStop(double level) { return protectiveStop(level, level); }
        public static ProtectiveStop protectiveStop(double stopLevel, double limitLevel) { return new ProtectiveStop(stopLevel, limitLevel); }

        public void cancel() {
            LogC.info("cancel " + this);
            cancelled = true;
            if (ferretSubmission != null) cancelFerretOrder();
        }

        public bool descriptionAndSymbolMatch(Order o) {
            return description.Equals(o.description) && symbol.Equals(o.symbol);
        }

        public Order placed() {
            stack = null;
            return this;
        }

        public Position position() {
            return position_;
        }

        public bool matches(Symbol other) {
            return symbol.Equals(other);
        }

        public bool canFill(double tick, bool isClose) {
            if(cancelled) return false;
            if(hasPosition() && position().isClosed()) return false;
            return details.canFill(direction, tick) && duration.canFill(isClose);
        }

        public double fillPrice(double tick, bool isOpen) {
            return details.fillPrice(tick, isOpen || isPlacedOnClose || duration.isOnClose());
        }

        public string shortString(double fillPrice) {
            return size + " " + symbol + " @ " + fillPrice + " " + details;
        }

        public void placedOnClose() {
            isPlacedOnClose = true;
        }

        public Direction positionDirection() {
            if(position().size == 0) return -direction;
            return position().direction();
        }

        public void placedBy(System system, DateTime time) {
            placed();
            duration.placedAt(time);
            Bomb.unlessNull(placingSystem, () => "trying to place by " + system + " is nonsensical, it was placed by " + placingSystem);
            placingSystem = system;
        }

        public System system() {
            return Bomb.ifNull(placingSystem, () => "this order was not placed by a system");
        }

        public int filledUpdateDb(double fillPrice, LiveSystem liveSystem, string topicPrefix) {
            int liveOrderId;
            if (ferretSubmission == null) {
                liveOrderId = LiveOrders.ORDERS.insert(
                    liveSystem.id(), symbol.name, jDate(now()), null, enterExit(), 
                    positionDirection().ToString(), size, new java.lang.Double(fillPrice),
                    details.ToString(), description, hostname(), topicPrefix, null
                );
            } else {
                liveOrderId = ferretSubmission.liveOrderId;
                var submitted = LiveOrders.ORDERS.order(liveOrderId);
                submitted.updateFill(fillPrice, jDate(now()));
            }
            Db.commit();
            OrderTable.topic().send(new Dictionary<string, object> {
                { "liveOrderId", liveOrderId },
                { "timestamp", ymdHuman(now()) }
            });
            return liveOrderId;
        }

        
        public int submittedInsertDb(LiveSystem liveSystem, string topicPrefix) {
            var positionDir = hasPosition() ? positionDirection() : direction;
            var liveOrderId = LiveOrders.ORDERS.insert(
                liveSystem.id(), symbol.name, null, jDate(now()), enterExit(), 
                positionDir.ToString(), size, null,
                details.ToString(), description, hostname(), topicPrefix, ferretSubmission.id
            );
            Db.commit();
            OrderTable.topic().send(new Dictionary<string, object> {
                { "liveOrderId", liveOrderId },
                { "timestamp", now() }
            });
            return liveOrderId;
        }

        string enterExit() {
            return enterExit_;
        }

        public string tradeString(double price) {
            return enterExit() + " " + positionDirection() + " " + shortString(price);
        }

        public bool canSubmitToFerret() {
            return details.canSubmitToFerret(duration);
        }

        public static void clearCache() {
            configs.clear();
        }
    }
}