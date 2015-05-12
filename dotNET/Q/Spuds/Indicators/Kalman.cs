using System;
using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class RandomWalkKalman : Spud<double> {
        readonly Spud<double> values;
        readonly LogSpud logValues;            
        readonly double g = (1 + Math.Sqrt(5))/2;
        readonly int freq;
        readonly int window;    
        readonly bool useGoldenRatio;
        readonly double[] sequence;

        public RandomWalkKalman(Spud<double> values,int freq) : this(values,freq,10,false) {}

        public RandomWalkKalman(Spud<double> values,int freq, int window) : this(values,freq,window,false) {}

        public RandomWalkKalman(Spud<double> values,int freq, int window, bool useGoldenRatio) : base(values.manager) {
            this.values = dependsOn(values);
            this.window = window;
            this.freq = freq;
            this.useGoldenRatio = useGoldenRatio;
            sequence = Fibonacci.Sequence(2 * window);
            logValues = dependsOn(new LogSpud (values));        
        }
    
        protected override double calculate() {
            if(values.count() < (freq * window) + (freq > 1 ?  - (freq - 1) : 0)) return values;
            double res = 0;
            if(useGoldenRatio) {
                zeroTo(window, i => res = res + logValues[i * freq] * Math.Pow(1 / g, i * 2 + 1));
            }else {            
                zeroTo(window, i => res = res + logValues[i * freq] * sequence[2 * window - (i * 2 + 1)] / sequence[2 * window]);
            }
            return Math.Exp(res);
        }
    }
}