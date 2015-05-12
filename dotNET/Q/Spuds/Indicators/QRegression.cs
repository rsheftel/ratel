using org.apache.commons.math.stat.descriptive.moment;
using Q.Spuds.Core;
using System;
using Q.Util;

namespace Q.Spuds.Indicators {
    
    public class QRegression : Spud<RegressionBar> {       
        protected readonly Spud<double> y;
        protected readonly Spud<double> x;                
        protected readonly int window;
        protected readonly bool noIntercept;
        protected double[] weights;
        protected readonly double weightSum;
        
        public QRegression(Spud<double> y, Spud<double> x, int window, bool noIntercept, double[] weights) : base(y.manager) {
            if(Equals(weights[0], double.NaN)) {
                var w = new double[window];
                zeroTo(window, i => w[i] = 1);
                this.weights = w;
            }else {
                Bomb.when(weights.Length != window, () => "The number of weights is not equal to window size");    
                this.weights = weights;
            }
            this.y = dependsOn(y);
            this.x = dependsOn(x);
            
            this.window = window;
            this.noIntercept = noIntercept;
            weightSum = weightsSum();          
        }

        public QRegression(Spud<double> y, Spud<double> x, int window, bool noIntercept) : this(y,x,window,noIntercept,new[] {double.NaN}) {}

        public bool hasInvalidNumbers(Spud<double>s) {
            return s.hasInfinity(window)||s.hasNaN(window);
        }

        protected bool isMissingData(Spud<double> values) {
            return values.count() < window || hasInvalidNumbers(values);
        }

        protected bool isMissingData() {
            return isMissingData(y) || isMissingData(x);
        }
      
        protected double weightedSumBivariate(Spud<double> s1,Spud<double> s2) {
            var sum = 0.0;
            zeroTo(window, i => sum = sum + weights[window - i - 1] * (s1[i]) * (s2[i]));
            return sum;
        }

        protected double weightedSum(Spud<double> s) {
            var sum = 0.0;
            zeroTo(window, i => sum = sum + weights[window - i - 1] * s[i]);
            return sum;
        }

        protected double weightsSum() {
            var sum = 0.0;
            zeroTo(window, i => sum = sum + weights[window - i - 1]);
            return sum;
        }

        protected override RegressionBar calculate() {  
            if(isMissingData())return RegressionBar.NAN;
            double slope;
            double intercept;
            double numFactors;                  

            var wxy = weightedSumBivariate(x,y);
            var wxx = weightedSumBivariate(x,x);
            var wx = weightedSum(x);
            var wy = weightedSum(y);

            var den = weightSum * wxx - wx*wx;            

            if(noIntercept) {                                
                slope = wxx == 0 ? 0 : wxy/wxx;
                intercept = 0;
                numFactors = 1;
            }else{            
                var num = (weightSum * wxy - wx * wy);
                slope = num == 0 ? 0 : num / den;                
                intercept = num == 0 ? wy / weightSum : (wxx * wy - wx * wxy) / den;

                numFactors = 2;
            }            
            return new RegressionBar(intercept,slope,y.last(window),x.last(window),numFactors,weights);
        }
    }
    
    public class QRegressionForcedSlope : QRegression {
        readonly Spud<double> slope;
        public QRegressionForcedSlope(Spud<double> y, Spud<double> x, int window, Spud<double> slope, double[] weights) : base(y,x,window,false, weights) {
            this.slope = dependsOn(slope);
        }
        public QRegressionForcedSlope(Spud<double> y, Spud<double> x, int window, Spud<double> slope) : base(y,x,window,false) {
            this.slope = dependsOn(slope);
        }     
        protected override RegressionBar calculate() {
            if(isMissingData() || slope.hasNaN(1) || slope.hasInfinity(1) || slope.count() < 1) return(RegressionBar.NAN);                                 
            var intercept = (weightedSum(y) - slope[0] * weightedSum(x)) / weightSum;            
            return new RegressionBar(intercept,slope[0],y.last(window),x.last(window),2,weights);
        }
    }

    public class RegressionBar : Objects {
        readonly double intercept;
        readonly double slope;
        readonly double[] y;
        readonly double[] x;
        readonly int n;
        readonly Mean meanStat = new Mean();

        public static readonly RegressionBar NAN = new RegressionBar(double.NaN, double.NaN,new double[0],new double[0],double.NaN,new double[0]);
        readonly double numFactors;
        readonly double[] weights;
        
        public RegressionBar(double intercept, double slope, double[] y, double[] x, double numFactors,double[] weights) {
            Bomb.when(x.Length != y.Length, () => "Regression underlying series should have the same length");
            this.intercept = intercept;
            this.slope = slope;
            this.y = y;
            this.x = x;
            this.numFactors = numFactors;
            this.weights = weights;            
            n = x.Length;
        }

        public double getNumFactors() {            
            return numFactors;
        }

        public double getSlope() {
            return slope;
        }
        public double getIntercept() {                        
            return intercept;
        }
        public double predict(double newX) {
            return intercept + slope * newX;
        }
       
        public double getLastPrediction() {
            return predict(x[n-1]);
        }
        
        public double getResidual(bool useWeights, int pos) {
            if(isEmpty()) return double.NaN;            
            var unWeightedPrediction = y[pos] - predict(x[pos]);                            
            if(useWeights) return Math.Sqrt(weights[pos]) * unWeightedPrediction;
            return unWeightedPrediction;
        }

        public double[] getResiduals(bool useWeights) {            
            var residuals = new double[n];
            zeroTo(n, i => residuals[i] = getResidual(useWeights,i));            
            return residuals;
        }

        public double[] getResiduals() {
            return getResiduals(false);
        }  

        public double getLastResidual(bool useWeights) {
            return getResidual(useWeights,n-1);
        }
        
        public double getLastResidual() {
            return getLastResidual(false);
        }

        public double getSigma(bool useWeights) {            
            return Math.Sqrt(getRSS(false,getResiduals(useWeights)) / (n - getNumFactors()));
        }

        public double getSigma() {
           return getSigma(false);
        }
              
        public double getLastZScore(bool useWeights) {            
            return (getLastResidual(useWeights) - meanStat.evaluate(getResiduals(useWeights))) / getSigma(useWeights);
        }

        public double getLastZScore() {
            return getLastZScore(false);
        }

        public double rSquare(bool useWeights) {                         
            return getMSS(useWeights) / (getRSS(useWeights,getResiduals(false)) + getMSS(useWeights));
        }

        public double rSquare() {
            return rSquare(false);
        }

        double getRSS(bool useWeights, double[] residuals) {            
            var rss = 0.0;
            if (useWeights)  zeroTo(n, i => rss = rss + weights[i] * residuals[i] * residuals[i]);
            else zeroTo(n, i => rss = rss + residuals[i] * residuals[i]);         
            return rss;
        }
              
        public double rSquareAdj() {        
            return rSquareAdj(false);
        }

        public double rSquareAdj(bool useWeights) {        
            return 1 - ((1 - rSquare(useWeights)) * (n - getNumFactors() + 1) / (n - getNumFactors()));
        }

        bool isEmpty() {
            return isEmpty(y) || isEmpty(x);
        }
   
        public double getMSS() {
            return getMSS(false);
        }

        double getMSS(bool useWeights) {
            var fitted = getFitted();
            var fittedMean = getFittedMean(fitted);           
            var mss = 0.0;           
            if (useWeights) zeroTo(n, i => mss = mss + weights[i] * (fitted[i] - fittedMean) * (fitted[i] - fittedMean));  
            else zeroTo(n, i => mss = mss + (fitted[i] - fittedMean) * (fitted[i] - fittedMean));  
            return mss;
        }

        double getFittedMean(double[] fitted) {
            if(getNumFactors() != 2) return (0);
            double fittedMean = 0;
            double weightSum = 0;
            zeroTo(n, i => fittedMean = fittedMean + weights[i] * fitted[i]);
            zeroTo(n, i => weightSum = weightSum + weights[i]);                        
            return fittedMean / weightSum;
        }

        double[] getFitted() {            
            var fitted = new double[n];
            zeroTo(n, i => fitted[i] = predict(x[i]));            
            return fitted;
        }
    }
}