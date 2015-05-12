using System;
using System.Collections.Generic;
using Q.Spuds.Core;
using Q.Util;

namespace Q.Trading {
    public class BarSpud : RootSpud<Bar> {
        public static readonly Converter<Bar, double> HIGH = b => b.high;
        public static readonly Converter<Bar, double> LOW = b => b.low;
        public static readonly Converter<Bar, double> OPEN = b => b.open;
        public static readonly Converter<Bar, double> CLOSE = b => b.close;
        public static readonly Converter<Bar, double> VOLUME = b => b.volume;
        public static readonly Converter<Bar, DateTime> TIME = b => b.time;
        readonly Lazy<ComparableSpud<double>> open_;
        readonly Lazy<ComparableSpud<double>> high_;
        readonly Lazy<ComparableSpud<double>> low_;
        readonly Lazy<ComparableSpud<double>> close_;
        readonly Lazy<ComparableSpud<double>> volume_;
        readonly Lazy<ComparableSpud<DateTime>> times_;
        DateTime lastTickedAt_;
        public event Action<DateTime> ticked;

        public BarSpud(SpudManager manager) : base(manager) {
            open_ = new Lazy<ComparableSpud<double>>(()=> comparableTransform(OPEN));
            high_ = new Lazy<ComparableSpud<double>>(()=> comparableTransform(HIGH));
            low_ = new Lazy<ComparableSpud<double>>(()=> comparableTransform(LOW));
            close_ = new Lazy<ComparableSpud<double>>(()=> comparableTransform(CLOSE));
            volume_ = new Lazy<ComparableSpud<double>>(()=> comparableTransform(VOLUME));
            times_ = new Lazy<ComparableSpud<DateTime>>(()=> comparableTransform(TIME));
            ticked += doNothing;
        }        

        ComparableSpud<T> comparableTransform<T>(Converter<Bar, T> converter) where T : IComparable<T> {
            Bomb.when(isInitialized() && hasContent(), () => "Can't initialize bar sub-Spud after bars has started pushing down.  Try calling prepare() in your system constructor.");
            return comparable(transform(converter));
        }

        public ComparableSpud<double> open { get { return open_; } }
        public ComparableSpud<double> high { get { return high_; } }
        public ComparableSpud<double> low { get { return low_; } }
        public ComparableSpud<double> close { get { return close_; }
        }
        public ComparableSpud<double> volume { get { return volume_; } }
        public Spud<DateTime> times { get { return times_; } }

        public Bar this[DateTime date] {
            get {
                return this[indexOf(times, t1 => t1.Equals(date))];
            }
        }

        public DateTime lastTickedAt() {
            Bomb.unless(hasTicked(), () => "bar spud has not been notified of any ticks");
            return lastTickedAt_;
        }

        public bool hasTicked() {
            return lastTickedAt_ != default(DateTime);
        }

        public void lastTickedAt(DateTime time) {
            lastTickedAt_ = time;
            ticked(time);
        }

        public void requireCount(int i) {
            Bomb.unless(count() == i, () => "expected count " + i + ".  was " + count() + ": " + last10());
        }

        public Dictionary<DateTime, int> timeLookup() {
            var i = 0;
            var result = new Dictionary<DateTime, int>();
            each(this, bar => result[bar.time] = i++);
            return result;
        }
    }
}