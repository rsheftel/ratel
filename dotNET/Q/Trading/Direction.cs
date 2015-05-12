using System;

namespace Q.Trading {
    public class Direction : IEquatable<Direction> {
        readonly int value;
        public static readonly Direction LONG = new Direction(1);
        public static readonly Direction SHORT = new Direction(-1);

        private Direction(int value) {
            this.value = value;
        }

        public static Direction operator - (Direction d) {
            return d.longShort(SHORT, LONG);
        }

        public static long operator * (long t, Direction d) {
            return t * d.value;
        }

        public static long operator *(Direction d, long t) {
            return t * d;
        }

        public static double operator *(double t, Direction d) {
            return t * d.value;
        }

        public static double operator *(Direction d, double t) {
            return t * d;
        }
        public bool Equals(Direction direction) {
            if (direction == null) return false;
            return value == direction.value;
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as Direction);
        }

        public override int GetHashCode() {
            return value;
        }

        public Position advance(Position position, long amount) {
            position.adjust(value * amount);
            return position;
        }

        public static Direction from(long amount) {
            return amount > 0 ? LONG : SHORT;
        }

        public override string ToString() {
            return longShort("long", "short");
        }

        public bool isLong() {
            return this == LONG;
        }

        public T longShort<T>(T ifLong, T ifShort) {
            return isLong() ? ifLong : ifShort;
        }

        public Order order(String description, Symbol symbol, OrderDetails details, long size, OrderDuration duration) {
            return new Order(description, symbol, details, this, size, duration);
        }

        public bool isShort() {
            return !isLong();
        }
    }
}