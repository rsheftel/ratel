using System;
using Q.Util;
using JTick=systemdb.data.Tick;

namespace Q.Trading {
    public class Tick : Objects {
        public readonly double price;
        readonly ulong volume;
        public readonly DateTime time;

        public Tick(double price, ulong volume, DateTime time) {
            this.price = price;
            this.volume = volume;
            this.time = time;
        }
        public Tick(JTick tick) : this(tick.last, (ulong) tick.volume, date(tick.time)) {}

        public override string ToString() {
            return "" + volume + "@" + price + " " + time;
        }

        public bool Equals(Tick obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.price == price && obj.volume == volume && obj.time.Equals(time);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (Tick) && Equals((Tick) obj);
        }

        public override int GetHashCode() {
            unchecked {
                var result = price.GetHashCode();
                result = (result * 397)^volume.GetHashCode();
                result = (result * 397)^time.GetHashCode();
                return result;
            }
        }
    }
}