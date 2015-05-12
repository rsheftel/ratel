using System;
using Q.Util;
using systemdb.data;

namespace Q.Trading {
    public abstract class OrderDetails : Objects {

        public virtual bool canSubmitToFerret(OrderDuration duration) {
            return true;
        }

        public abstract bool canFill(Direction direction, double tick);
        public abstract double fillPrice(double tick, bool isOpen);
        public abstract void addToFerret(Fields message);
        protected static bool almostEquals(double left, double right) {
            return Math.Abs(left - right) < 1e-12;
        }
        public abstract string ToLongString();
    }

    public class Limit : OrderDetails, IEquatable<Limit> {
        readonly double limitLevel;
        public Limit(double limitLevel) {
            this.limitLevel = limitLevel;
        }

        public override bool canFill(Direction direction, double tick) {
            return direction * tick <= direction * limitLevel;
        }

        public override double fillPrice(double tick, bool isOpen) {
            return isOpen ? tick : limitLevel;
        }

        public override void addToFerret(Fields message) {
            message.put("ORDERTYPE", "LIMIT");
            message.put("LIMITPRICE", limitLevel);
        }

        public override string ToString() { return "LIMIT" + paren(limitLevel.ToString("n7")); }
        public override string ToLongString() { return "LIMIT" + paren(limitLevel.ToString("n14")); }

        public bool Equals(Limit limit) {
            return limit != null && almostEquals (limitLevel,limit.limitLevel);
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as Limit);
        }

        public override int GetHashCode() {
            return limitLevel.GetHashCode();
        }
    }

    public class Stop : OrderDetails, IEquatable<Stop> {
        readonly double stopLevel;
        public Stop(double stopLevel) {
            this.stopLevel = stopLevel;
        }

        public override string ToString() { return "STOP" + paren(stopLevel.ToString("n7")); }
        public override string ToLongString() { return "STOP" + paren(stopLevel.ToString("n14")); }

        public override bool canFill(Direction direction, double tick) {
            return direction * tick >= direction * stopLevel;
        }

        public override double fillPrice(double tick, bool isOpen) {
            return isOpen ? tick : stopLevel;
        }

        public override void addToFerret(Fields message) {
            message.put("ORDERTYPE", "STOP");
            message.put("STOPPRICE", "" + stopLevel);
        }

        public bool Equals(Stop stop) {
            return stop != null && almostEquals(stopLevel,stop.stopLevel);
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as Stop);
        }

        public override int GetHashCode() {
            return stopLevel.GetHashCode();
        }
    }

    public class ProtectiveStop : StopLimit {
        public ProtectiveStop(double stopLevel, double limitLevel) : base(stopLevel, limitLevel) {}
        public override bool canFill(Direction direction, double tick) {
            return stop.canFill(direction, tick);
        }

        public override double fillPrice(double tick, bool isOpen) {
            return stop.fillPrice(tick, isOpen);
        }

        public override string ToString() {
            return "PROTECTIVE_STOP" + levelString("n7");
        }

        public override string ToLongString() {
            return "PROTECTIVE_STOP" + levelString("n14");
        }

        public bool Equals(ProtectiveStop obj) {
            return base.Equals(obj);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            return ReferenceEquals(this, obj) || Equals(obj as ProtectiveStop);
        }

        public override int GetHashCode() {
            return base.GetHashCode();
        }
    }

    public class StopLimit : OrderDetails, IEquatable<StopLimit> {
        protected readonly double stopLevel;
        protected readonly double limitLevel;
        protected readonly Stop stop;
        readonly Limit limit;
        bool stopTripped;

        public StopLimit(double stopLevel, double limitLevel) {
            this.stopLevel = stopLevel;
            this.limitLevel = limitLevel;
            stop = new Stop(stopLevel);
            limit = new Limit(limitLevel);
        }

        public override string ToString() { return "STOP_LIMIT" + levelString("n7"); }
        public override string ToLongString() { return "STOP_LIMIT" + levelString("n14"); }
        protected string levelString(string format) { return paren(stopLevel == limitLevel ? "" + stopLevel.ToString(format) : commaSep(stopLevel.ToString(format), limitLevel.ToString(format))); }

        public override bool canFill(Direction direction, double tick) {
            stopTripped |= stop.canFill(direction, tick);
            return stopTripped && limit.canFill(direction, tick);
        }

        public override double fillPrice(double tick, bool isOpen) {
            return limit.fillPrice(tick, isOpen);
        }

        public override void addToFerret(Fields message) {
            message.put("ORDERTYPE", "STOPLIMIT");
            message.put("LIMITPRICE", limitLevel);
            message.put("STOPPRICE", stopLevel);
        }

        public bool Equals(StopLimit stopLimit) {
            if (stopLimit == null) return false;
            return almostEquals(limitLevel, stopLimit.limitLevel) && almostEquals(stopLevel,stopLimit.stopLevel);
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as StopLimit);
        }

        public override int GetHashCode() {
            return limitLevel.GetHashCode() + 29 * stopLevel.GetHashCode();
        }
    }

    public class Market : OrderDetails {
        public override string ToString() { return "MARKET"; }
        public override string ToLongString() { return "MARKET"; }

        public bool Equals(Market market) {
            return market != null;
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as Market);
        }

        public override int GetHashCode() {
            return 0;
        }

        public override bool canSubmitToFerret(OrderDuration duration) {
            return duration.isOnClose();
        }

        public override bool canFill(Direction direction, double tick) {
            return true;
        }

        public override double fillPrice(double tick, bool isOpen) {
            return tick;
        }

        public override void addToFerret(Fields message) {
            message.put("ORDERTYPE", "MARKET");
        }
    }
}