using Q.Util;

namespace Q.Trading {
    public class Currency : Objects {
        readonly string name;

        public Currency(string name) {
            this.name = Bomb.ifNull(name, () => "can't make currency sans name.");
        }

        bool Equals(Currency other) {
            if (ReferenceEquals(null, other)) return false;
            return ReferenceEquals(this, other) || Equals(other.name, name);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (Currency) && Equals((Currency) obj);
        }

        public override int GetHashCode() {
            return (name != null ? name.GetHashCode() : 0);
        }

        public Symbol fxRateSymbol() {
            Bomb.when(isUSD(), () => "can't convert USD to USD!");
            return new Symbol(name + "USD");
        }

        public bool isUSD() {
            return name.Equals("USD");
        }

    }
}