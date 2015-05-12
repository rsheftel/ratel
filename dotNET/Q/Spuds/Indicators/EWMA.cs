using System;
using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class EWMA : Spud<double> {
        readonly Spud<double> values;
        readonly double lambda;

        public EWMA(Spud<double> values, double halfLife) : base(values.manager) {
            this.values = dependsOn(values);
            lambda = Math.Pow(0.5, (1.0 / halfLife));
        }
        
        protected override double calculate() {                   
            return (values.count() > 1 && !Equals(this[1], double.NaN)) ? (1.0 - lambda) * values + lambda * this[1] : values;
        }
    }
    
    public class EWSD : Spud<double> {                          
        readonly Spud<double> deviationSpud;
        readonly Spud<double> res;

        public EWSD(Spud<double> values, double halfLife) : base(values.manager) {            
            deviationSpud = dependsOn(new Minus(values,new EWMA(values,halfLife)));
            res = dependsOn(new EWMA(new Times (deviationSpud,deviationSpud),halfLife));
        }

        protected override double calculate() {
            return Math.Sqrt(res);
        }
    }

    public class EWZScore : Spud<double> {                          
        readonly Spud<double> deviationSpud;
        readonly Spud<double> sdSpud;

        public EWZScore(Spud<double> values, double halfLife) : base(values.manager) {            
            deviationSpud = dependsOn(new Minus(values,new EWMA(values,halfLife)));
            sdSpud = dependsOn(new EWSD(values,halfLife));
        }

        protected override double calculate() {
            return safeDivide(0,deviationSpud,sdSpud);            
        }
    }
}