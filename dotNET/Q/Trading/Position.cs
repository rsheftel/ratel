using System;
using System.Collections.Generic;
using Q.Util;

namespace Q.Trading {
    public class Position : Objects {
        long amountDONOTREFERENCE_;
        public readonly Symbol symbol;
        readonly List<Trade> trades_ = new List<Trade>();
        int barsHeld_;
        static int nextIdentifier;
        readonly int id;
        public event Action onPositionClosed;

        public Position(Symbol symbol) {
            amount = 0;
            this.symbol = symbol;
            onPositionClosed += doNothing;
        }

        public Position(Symbol symbol, int id) : this(symbol) {
            this.id = id;
        }

        public Position(Symbol symbol, long size, Direction d) : this(symbol){
            adjust(size * d);
        }

        public Position(Position position) : this(position.symbol, position.size, position.direction()) {
            each(position.trades(), trade => addTrade(new Trade(trade)));
        }

        public bool Equals(Position other) {
            if (ReferenceEquals(null, other)) return false;
            if (ReferenceEquals(this, other)) return true;
            return other.id == id;
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (Position) && Equals((Position) obj);
        }

        public override int GetHashCode() {
            return id;
        }

        public override string ToString() {
            return paren("POS-" + id  + "C#" + identity() + ":" + direction() + " " + symbol + " " + amount);
        }

        public long amount {
            get { return amountDONOTREFERENCE_; }
            set { amountDONOTREFERENCE_ = value; }
        }

        public long size {
            get { return Math.Abs(amount); }
        }

        public Direction direction() {
            return Direction.from(amount);
        }

        public void adjust(long adjustment) {
            amount += adjustment;
            if(amount == 0) onPositionClosed();
        }

        public bool isClosed() {
            return amount == 0;
        }

        public T longShort<T>(T ifLong, T ifShort) {
            return direction().longShort(ifLong, ifShort);
        }

        public List<Trade> trades() {
            return trades_;
        }

        public Trade lastTrade() {
            return last(trades());
        }

        public Trade addTrade(Trade trade) {
            trades_.Add(trade);
            return trade;
        }

        public Order exit(string description, OrderDetails details, OrderDuration duration) { 
            var order = new Order(description, symbol, details, -direction(), size, duration);
            order.refersTo(this);
            return order;
        }
        public Order exitLong(string description, OrderDetails details, OrderDuration duration) {
            Bomb.unless(direction().isLong(), () => "short position cannot be exited long!");
            return exit(description, details, duration);
        }
        public Order exitShort(string description, OrderDetails details, OrderDuration duration) {
            Bomb.when(direction().isLong(), () => "long position cannot be exited short!");
            return exit(description, details, duration);
        }

        public Order scaleDown(string description, OrderDetails details, long tradeSize, OrderDuration duration) {
            Bomb.unless(
                tradeSize < size, 
                () => "trade size must be less than total size in partial exit.  trade size: " + tradeSize + ". total size: " + size
                );
            var order = new Order(description, symbol, details, -direction(), tradeSize, duration); 
            order.refersTo(this);
            return order;
        }
        public Order scaleDownLong(string description, OrderDetails details, long tradeSize, OrderDuration duration) {
            Bomb.unless(direction().isLong(), () => "short position cannot be scaled down long!");
            return scaleDown(description, details, tradeSize, duration);
        }
        public Order scaleDownShort(string description, OrderDetails details, long tradeSize, OrderDuration duration) {
            Bomb.when(direction().isLong(), () => "long position cannot be scaled down short!");
            return scaleDown(description, details, tradeSize, duration);
        }

        public Order scaleUp(string description, OrderDetails details, long tradeSize, OrderDuration duration) {
            var order = new Order(description, symbol, details, direction(), tradeSize, duration);
            order.refersTo(this);
            return order;
        }
        public Order scaleUpLong(string description, OrderDetails details, long tradeSize, OrderDuration duration) {
            Bomb.unless(direction().isLong(), () => "short position cannot be scaled up long!");
            return scaleUp(description, details, tradeSize, duration);
        }
        public Order scaleUpShort(string description, OrderDetails details, long tradeSize, OrderDuration duration) {
            Bomb.when(direction().isLong(), () => "long position cannot be scaled up short!");
            return scaleUp(description, details, tradeSize, duration);
        }

        public string lOrS() {
            return longShort("L", "S");
        }

        public double pnl(double earlierPrice, double laterPrice) {
            return symbol.pnl(amount, earlierPrice, laterPrice);
        }

        public double pnl(bool applySlippage, bool runInNativeCurrency) {
            Bomb.unless(isClosed(), () => "can't calculate pnl without price unless position is closed.");
            Bomb.when(isEmpty(trades()), () => "can't calculate closed position pnl without trades!");
            return pnl(Double.NaN, applySlippage, Double.NaN, runInNativeCurrency, Double.NaN);
        }

        public double pnlNoSlippage(double closePrice, bool runInNativeCurrency, double currentFxRate) {
            return pnl(closePrice, false, double.NaN, runInNativeCurrency, currentFxRate);
        }

        public double pnlWithSlippage(double closePrice, double currentSlippage, bool runInNativeCurrency, double currentFxRate) {
            return pnl(closePrice, true, currentSlippage, runInNativeCurrency, currentFxRate);
        }

        private double pnl(double closePrice, bool applySlippage, double currentSlippage, bool runInNativeCurrency, double currentFxRate) {
            var direction = first(trades()).direction;
            var entrySum = 0.0;
            var exitSum = 0.0;
            var sharesTraded = 0.0;
            double slippage, fxRate;
            foreach (var t in trades()) {
                slippage = applySlippage ? direction * t.slippage : 0;
                fxRate = runInNativeCurrency ? 1.0 : t.fxRate;
                if (t.direction.Equals(direction)) {
                    entrySum += t.size * (t.price + slippage) * fxRate;
                    sharesTraded += t.size;
                } else {
                    exitSum += t.size * (t.price - slippage) * fxRate;
                }
            }
            slippage = applySlippage ? direction * currentSlippage : 0;
            fxRate = runInNativeCurrency ? 1.0 : currentFxRate;
            if(size > 0)
                exitSum += size * (closePrice - slippage) * fxRate;
            var averageEntryPrice = entrySum / sharesTraded;
            var averageExitPrice = exitSum / sharesTraded;
            return symbol.pnl(sharesTraded * direction, averageEntryPrice, averageExitPrice);
        }

        public Trade entry() {
            return first(trades());
        }

        public bool isEntry(Trade trade) {
            return trade.Equals(entry());
        }

        public int barsHeld() {
            return barsHeld_;
        }

        public void newBar() {
            barsHeld_++;
        }

        public static int nextId() {
            return nextIdentifier++;
        }

        public void requireMatches(Position other) {
            Bomb.unless(symbol.Equals(other.symbol) && amountDONOTREFERENCE_.Equals(other.amountDONOTREFERENCE_),
                () => "positions do not match: " + this + ", " + other);
        }

        public double priceAtPnlNoSlippage(double targetPnl, bool runInNativeCurrency, double fxRate) {
            var pnlFromZero = targetPnl - pnlNoSlippage(0, runInNativeCurrency, fxRate);
            var dollarPrice =  pnlFromZero / (symbol.bigPointValue * amount);
            return runInNativeCurrency ? dollarPrice : dollarPrice / fxRate;
        }

        public Trade exitTrade() {
            Bomb.unless(isClosed(), () => "no exit trade if position is closed\n" + this);
            return last(trades());
        }

        public double slippage() {
            return sum(convert(trades(), t => symbol.bigPointValue * t.size * t.slippage));
        }
    }
}
