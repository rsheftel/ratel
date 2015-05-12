using System;
using Q.Util;

namespace Q.Trading {
    public class Trade : Objects {
        private Order order_;
        public readonly double price;
        public readonly double slippage;
        public readonly double fxRate;
        public readonly long size;
        public readonly Direction direction;
        public readonly string description;
        public readonly DateTime time;

        public Trade(Trade other) : this( null, other.price, other.size, other.direction, other.description, other.slippage, other.fxRate) {}


        public Trade(Order order, double price, long size, double slippage, double fxRate) : this(order, price, size, order.direction, order.description, slippage, fxRate) {}

        public Trade(Order order, double price, long size, double slippage, DateTime time, double fxRate) : this(order, price, size, slippage, fxRate) {
            this.time = time;
        }

        Trade(Order order_, double price, long size, Direction direction, string description, double slippage, double fxRate) {
            this.order_ = order_;
            this.price = price;
            this.size = size;
            this.direction = direction;
            this.description = description;
            this.slippage = slippage;
            this.fxRate = fxRate;
        }

        public override string ToString() {
            return description + ": " + direction + " " + size + "@" + price + ": (" + order_ + ")";
        }

        public double amount() {
            return size * direction;
        }
        
        public double pnl(double toPrice, bool runInNativeCurrency) {
            return order_.symbol.pnl(amount(), (price + direction * slippage) * (runInNativeCurrency ? 1.0 : fxRate), toPrice);
        }

        public double pnl(double toPrice) {          
            return pnl(toPrice, false);
        }

        public double pnlNoSlippage(double toPrice) {          
            return order_.symbol.pnl(amount(), price, toPrice);
        }

        public string shortString() {
            return order_.tradeString(price);
        }


        public void updateOrder(Order replacement) {
            order_ = replacement;
        }

        public Order order() {
            return order_;
        }

        public bool isLong() {
            return direction.isLong();
        }

        public bool isShort() {
            return direction.isShort();
        }

        public bool Equals(Trade obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return Equals(obj.order_, order_) && obj.price == price && obj.size == size && Equals(obj.direction, direction) && Equals(obj.description, description) && obj.slippage == slippage;
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (Trade) && Equals((Trade) obj);
        }

        public override int GetHashCode() {
            unchecked {
                var result = (order_ != null ? order_.GetHashCode() : 0);
                result = (result * 397)^price.GetHashCode();
                result = (result * 397)^size.GetHashCode();
                result = (result * 397)^(direction != null ? direction.GetHashCode() : 0);
                result = (result * 397)^(description != null ? description.GetHashCode() : 0);
                result = (result * 397)^slippage.GetHashCode();
                return result;
            }
        }

    }
}