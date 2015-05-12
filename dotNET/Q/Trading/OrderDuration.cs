using System;
using Q.Util;
using systemdb.data;

namespace Q.Trading {
    public abstract class OrderDuration : Objects {
        public abstract bool shouldCancelOnTick(); // can this be renamed to "cancelOnTick" and "cancelOnBar"? 
        public abstract bool shouldCancelOnNewBar(DateTime currentBarTime);
        public abstract void addToFerret(Fields fields);

        public virtual bool canFill(bool isClose) {
            return true;
        }

        public virtual void requireSupported(OrderDetails details) {}

        public virtual bool isOnClose() {
            return false;
        }

        public virtual void placedAt(DateTime time) {}
    }

    public class FillOrKill : OrderDuration, IEquatable<FillOrKill> {
        public static readonly OrderDuration FILL_KILL = new FillOrKill();
        private FillOrKill() {}
        public override bool shouldCancelOnTick() { return true; }
        public override bool shouldCancelOnNewBar(DateTime currentBarTime) { return true; }

        public bool Equals(FillOrKill fillOrKill) {
            return fillOrKill != null;
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as FillOrKill);
        }

        public override int GetHashCode() {
            return 0;
        }

        public override void addToFerret(Fields fields) {
            fields.put("TIMEINFORCE", "FOK");
        }
    }

    public class OneDay : OrderDuration, IEquatable<OneDay> {
        DateTime placedAtTime;

        public override bool shouldCancelOnTick() {
            return false;
        }

        public bool Equals(OneDay obj) {
            return !ReferenceEquals(null, obj);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (OneDay) && Equals((OneDay) obj);
        }

        public override int GetHashCode() {
            return 0;
        }

        public static bool operator ==(OneDay left, OneDay right) {
            return Equals(left, right);
        }

        public static bool operator !=(OneDay left, OneDay right) {
            return !Equals(left, right);
        }

        public override bool shouldCancelOnNewBar(DateTime currentBarTime) {
            Bomb.when(placedAtTime.Equals(default(DateTime)), () => "OneDay order cannot be cancelled before it is placed");
            return currentBarTime.Date > placedAtTime.Date;
        }

        public override void addToFerret(Fields fields) {
            fields.put("TIMEINFORCE", "DAY");
        }

        public override void placedAt(DateTime time) {
            placedAtTime = time;
        }
    }

    public class OneBar : OrderDuration, IEquatable<OneBar> {
        public static readonly OrderDuration ONE = new OneBar();
        private OneBar() {}
        public override bool shouldCancelOnTick() { return false; }
        public override bool shouldCancelOnNewBar(DateTime currentBarTime) { return true; }

        public bool Equals(OneBar other) {
            return other != null;
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as OneBar);
        }

        public override int GetHashCode() {
            return 1;
        }

        public override string ToString() {
            return "onebar";
        }

        public override void addToFerret(Fields fields) {
            fields.put("TIMEINFORCE", "DAY");
        }
    }

    public class OnClose : OrderDuration, IEquatable<OnClose> {
        public static readonly OrderDuration ON_CLOSE = new OnClose();
        private OnClose() {}
        public override bool shouldCancelOnTick() { return false; }
        public override bool shouldCancelOnNewBar(DateTime currentBarTime) { return true; }

        public bool Equals(OnClose other) {
            return other != null;
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as OnClose);
        }

        public override int GetHashCode() {
            return 1;
        }

        public override string ToString() {
            return "on_close";
        }

        public override void addToFerret(Fields fields) {
            fields.put("TIMEINFORCE", "CLOSE");
        }

        public override bool canFill(bool isClose) {
            return isClose;
        }

        public override void requireSupported(OrderDetails details) {
            base.requireSupported(details);
            Bomb.unless(details is Market || details is Limit, () => "can't place " + details + " using MOC/LOC logic");
        }

        public override bool isOnClose() {
            return true;
        }
    }
}