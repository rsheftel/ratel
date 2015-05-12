using System;
using Q.Spuds.Core;
using Q.Trading;

namespace Q.Spuds.Indicators {
    public class Fibonacci : Spud<BreakPoints> {
        readonly BarSpud values;
        readonly Spud<double> highestHigh;
        readonly Spud<double> lowestLow;
        readonly int nDays;


        public Fibonacci(BarSpud values, int nDays) : base(values.manager) {
            this.values = dependsOn(values);
            this.nDays = nDays;
            highestHigh = values.high.highest(this.nDays);
            lowestLow = values.low.lowest(this.nDays);
        }

        public static double Number(int num) {
            var fiboSeries = Sequence(num);            
            return fiboSeries[num];
        }

        public static double[] Sequence(int length) {
            var fiboSeries = new double[Math.Max(length+1,2)];
            fiboSeries[0] = 0;
            fiboSeries[1] = 1;
            for(var j = 2; j <= length; j++)fiboSeries[j] = fiboSeries[j - 1] + fiboSeries[j - 2];       
            return fiboSeries;
        }

        protected override BreakPoints calculate() {
            return new BreakPoints(values[0], highestHigh[0], lowestLow[0]);
        }
    }

        public class BreakPoints {
        readonly Bar bar;
        readonly double highestHigh;
        readonly double lowestLow;

        public BreakPoints(Bar bar, double highestHigh, double lowestLow) {
            this.bar = bar;
            this.highestHigh = highestHigh;
            this.lowestLow = lowestLow;
        }

        public bool Equals(BreakPoints obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return Equals(obj.bar, bar) && obj.highestHigh == highestHigh && obj.lowestLow == lowestLow;
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (BreakPoints) && Equals((BreakPoints) obj);
        }

        public override int GetHashCode() {
            unchecked {
                var result = (bar != null ? bar.GetHashCode() : 0);
                result = (result * 397)^highestHigh.GetHashCode();
                result = (result * 397)^lowestLow.GetHashCode();
                return result;
            }
        }
        
        double fiboNum(double ratio) {
            return (highestHigh - lowestLow) * ratio + lowestLow;
        }

        public double fibo0()  { return lowestLow;}
        public double fibo38() { return fiboNum(0.382); }
        public double fibo50() { return fiboNum(0.5); }
        public double fibo62() { return fiboNum(0.618); }
        public double fibo100() {
            return highestHigh;
        }        
    }

}