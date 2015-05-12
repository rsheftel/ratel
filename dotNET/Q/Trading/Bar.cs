using System;
using System.Collections.Generic;
using java.lang;
using Q.Util;
using Double=System.Double;
using JBar = systemdb.data.Bar;
using JTick = systemdb.data.Tick;
using Math=System.Math;

namespace Q.Trading {
    public class Bar : Objects, IEquatable<Bar> {

        public readonly double open;
        public readonly double high;
        public readonly double low;
        public readonly double close;
        public readonly double volume;
        public readonly double openInterest;
        public readonly DateTime time;

        public Bar(JBar bar) : this(bar, true) {}

        public Bar(JBar bar, bool strict) : this(bar.open(), bar.high(), bar.low(), bar.close(), date(bar.date()), strict) {
            var jOpenInterest = bar.openInterest();
            openInterest = jOpenInterest == null ? Double.NaN : jOpenInterest.doubleValue();
            var jVolume = bar.volume();
            volume = jVolume == null ? Double.NaN : jVolume.doubleValue();
        }

        public Bar(double open, double high, double low, double close) : this(open, high, low, close, true) {}
        public Bar(double open, double high, double low, double close, bool strict) : this(open, high, low, close, default(DateTime), strict) {}

        public Bar(double open, double high, double low, double close, DateTime time) : this(open, high, low, close, time, true) {}
        public Bar(double open, double high, double low, double close, DateTime time, bool strict) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.time = time;
            if (strict) checkBar();
        }

        public Bar(systemdb.data.Tick tick) : this(tick.open, tick.high, tick.low, tick.last, date(tick.time)) {}

        Bar(double open, double high, double low, double close, DateTime time, double volume, double openInterest) 
            : this(open, high, low, close, time) {
            this.volume = volume;
            this.openInterest = openInterest;
        }

        public Bar(double testData) : this(testData, testData, testData, testData) {}
        public Bar(double testData, DateTime time) : this(testData, testData, testData, testData, time) {}

        public void checkBar() {
            Bomb.unless(high >= low, () => "high " + paren(high) + " must be above low " + paren(low) + "\nbar: " + this);
        }

        public double range() {
            return high - low;
        }

        public Bar update(Tick tick) {
            return new Bar(open, Math.Max(tick.price, high), Math.Min(tick.price, low), tick.price, tick.time, volume, openInterest);
        }

        public Bar update(DateTime newTime) {
            return new Bar(open, high, low, close, newTime, volume, openInterest);
        }

        public override string ToString() {
            return paren(commaSep(ymdHuman(time), open, high, low, close, volume, openInterest));
        }

        public bool Equals(Bar bar) {
            if (bar == null) return false;
            if (open != bar.open) return false;
            if (high != bar.high) return false;
            if (low != bar.low) return false;
            if (close != bar.close) return false;
            if (volume != bar.volume) return false;
            return openInterest == bar.openInterest && Equals(time, bar.time);
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as Bar);
        }

        public override int GetHashCode() {
            var result = open.GetHashCode();
            result = 29 * result + high.GetHashCode();
            result = 29 * result + low.GetHashCode();
            result = 29 * result + close.GetHashCode();
            result = 29 * result + volume.GetHashCode();
            result = 29 * result + openInterest.GetHashCode();
            result = 29 * result + time.GetHashCode();
            return result;
        }

        public List<double> orderedHighLow() {
//          This is a rounded version of: open - low <= high - open
            return 2 * open - low - high < 1e-10
                    ? list(low, high)
                    : list(high, low);
        }

        public JTick jTick() {
            return new JTick(close, (long) volume, open, high, low, jDate(time));
        }

        public Tick tick() {
            return new Tick(close, (ulong) volume, time);
        }

        public JBar java() {
            return new JBar(jDate(time), open, high, low, close, new Long((long) volume), new Long((long) openInterest));
        }
    }
}