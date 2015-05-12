using System;
using System.Collections.Generic;
using Q.Util;

namespace Q.Trading {
    public class Pair : Objects, SystemKey {
        public readonly Symbol left;
        public readonly Symbol right;

        public Pair(Symbol left, Symbol right) {
            this.left = left;
            this.right = right;
        }

        // Pair currently is written with the convention that to be long the pair is to be long the left and short the right
        // Also equal sized
        public List<Order> buy(string description, OrderDetails detailsLeft, OrderDetails detailsRight, long size, OrderDuration duration) {
            return list(
                new Order(description, left, detailsLeft, Direction.LONG, size, duration),
                new Order(description, right, detailsRight, Direction.SHORT, size, duration)
                );
        }

        public List<Order> sell(string description, OrderDetails detailsLeft, OrderDetails detailsRight, long size, OrderDuration duration) {
            return list(
                new Order(description, left, detailsLeft, Direction.SHORT, size, duration),
                new Order(description, right, detailsRight, Direction.LONG, size, duration)
                );
        }

        public bool coveredBy(Dictionary<Symbol, Bar> bars) {
            return bars.ContainsKey(left) && bars.ContainsKey(right);
        }

        public List<Symbol> symbols() {
            return list(left, right);
        }

        public void addTo(LazyDictionary<Symbol, List<Pair>> dictionary) {
            dictionary.get(left).Add(this);
            dictionary.get(right).Add(this);
        }

        public string name() {
            return left.name + "-" + right.name;
        }

        public override string ToString() {
            return name();
        }

        bool Equals(Pair other) {
            if (ReferenceEquals(null, other)) return false;
            if (ReferenceEquals(this, other)) return true;
            return Equals(other.left, left) && Equals(other.right, right);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (Pair) && Equals((Pair) obj);
        }

        public override int GetHashCode() {
            unchecked {
                return (left.GetHashCode() * 397)^right.GetHashCode();
            }
        }

        public Dictionary<Symbol, Bar> closeBars(double leftClose, double rightClose, DateTime time) {
            return dictionary(symbols(), list(new Bar(leftClose, time), new Bar(rightClose, time)));
        }

        public DateTime closeTime() {
            Bomb.unless(left.closeAt().Equals(right.closeAt()), () => 
                "both symbols in a pair need to have the same close time for the pair to have a close time.  " + ymdHuman(left.closeAt()) + " != " + ymdHuman(right.closeAt()));
            return left.closeAt();
        }

        public IEnumerable<Order> exits(PairSystem system, string description, OrderDetails leftDetails, OrderDetails rightDetails, OrderDuration duration) {
            return list(system.position(left).exit(description, leftDetails, duration), system.position(right).exit(description, rightDetails, duration));
        }
    }
}