using NUnit.Framework;
using Q.Spuds.Core;

namespace Q.Spuds.Indicators {

    [TestFixture]
    public class QRegressionBaseTest : SpudTestCase<double, RegressionBar> {
        protected RootSpud<double> x;
        protected void checkOutputs(double intercept, double slope) {
            AlmostEqual(indicator[0].getSlope(),slope, 1e-5);
            AlmostEqual(indicator[0].getIntercept(),intercept, 1e-5);  
        }

        protected void hasNaN() {
            AreEqual(indicator[0],RegressionBar.NAN);
        }

        protected void addPoint(double newY, double newX) {
            addPoint(newY); x.set(newX); 
        }
    }

    [TestFixture]
    public class QRegressionTest : QRegressionBaseTest {
        [Test]
        public void testBasicUseCase() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,3,false);  
            addPoint(7, 9);
            hasNaN();
            addPoint(6, 8);
            hasNaN();
            addPoint(5, 0);
            checkOutputs(4.952055, 0.18493);                  
            addPoint(6, 8);
            checkOutputs(5, 0.125);
            addPoint(9,7);
            checkOutputs(5.21929, 0.28947);     
        }    

        [Test]
        public void testXIsZero() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,3,false);  
            addPoint(5, 0);
            hasNaN();
            addPoint(6, 0);
            hasNaN();
            addPoint(9, 0);         
            checkOutputs(6.66666, 0);            
        }
        [Test]
        public void testYIsZero() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,3,false);  
            addPoint(0,5);
            hasNaN();
            addPoint(0,6);
            hasNaN();
            addPoint(0,9);           
            checkOutputs(0, 0);
        }
        [Test]
        public void testYAndYAreZero() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,3,false);  
            addPoint(0,0);
            hasNaN();
            addPoint(0,0);
            hasNaN();
            addPoint(0,0);
            checkOutputs(0, 0);   
        }

        [Test]
        public void testYIsConstant() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,3,false);  
            addPoint(8,5);
            hasNaN();
            addPoint(8,6);
            hasNaN();
            addPoint(8,9);     
            checkOutputs(8, 0);            
        }
        [Test]
        public void testXIsConstant() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,3,false);  
            addPoint(5, 2);
            hasNaN();
            addPoint(6, 2);
            hasNaN();
            addPoint(9, 2);    
            checkOutputs(6.66666, 0);            
        }
        [Test]
        public void testXHasNaN() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,3,false);  
            addPoint(5, 2);
            hasNaN();
            addPoint(double.NaN, 2);
            hasNaN();
            addPoint(9, 2);             
            hasNaN();                        
        }
        [Test]
        public void testXHasInfinity() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,3,false);  
            addPoint(5, 2);
            hasNaN();
            addPoint(double.NegativeInfinity, 2);
            hasNaN();
            addPoint(9, 2);             
            hasNaN();                        
        }
        [Test]
        public void testLongSeries() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,10,false);  
            addPoint(-5,-1);
            hasNaN();
            addPoint(5,3);           
            addPoint(1,0.5);             
            addPoint(3,1);             
            addPoint(2,1.1);             
            addPoint(8,1.3);             
            addPoint(-2,-1);             
            addPoint(-8,-5);             
            addPoint(1,0.5);             
            addPoint(0,0);    
            checkOutputs(0.42234, 1.94136);            
        }
        [Test]
        public void testWeightedRegression() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,10,false,new [] {0.5,0.5358867,0.5743492,0.6155722,0.6597540,0.7071068,0.7578583,0.8122524,0.8705506,0.9330330});  
            
            addPoint(-5,-1);
            hasNaN();
            addPoint(5,3);           
            addPoint(1,0.5);             
            addPoint(3,1);             
            addPoint(2,1.1);             
            addPoint(8,1.3);             
            addPoint(-2,-1);             
            addPoint(-8,-5);             
            addPoint(1,0.5);             
            addPoint(0,0);    
            checkOutputs(0.5499542078002,1.91841053644205);            
            AlmostEqual(indicator[0].getLastResidual(),-0.54995,1e-5);            
            AlmostEqual(indicator[0].getLastResidual(true),-0.53122,1e-5);
            AlmostEqual(indicator[0].getSigma(),2.30431, 1e-5); 
            AlmostEqual(indicator[0].getSigma(true),1.84317, 1e-5); 
            AlmostEqual(indicator[0].getLastZScore(),-0.18368, 1e-5); 
            AlmostEqual(indicator[0].getLastZScore(true),-0.26229, 1e-5);             
            AlmostEqual(indicator[0].rSquare(),0.77860, 1e-5); 
            AlmostEqual(indicator[0].rSquare(true),0.79859, 1e-5); 
            AlmostEqual(indicator[0].rSquareAdj(true),0.77341, 1e-5); 
        }  

        [Test]
        public void testOutputsWithIntercept() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,10,false);  
            addPoint(-5,-1);
            hasNaN();
            addPoint(5,3);           
            addPoint(1,0.5);             
            addPoint(3,1);             
            addPoint(2,1.1);             
            addPoint(8,1.3);             
            addPoint(-2,-1);             
            addPoint(-8,-5);             
            addPoint(1,0.5);             
            addPoint(0,0);

            var residuals = indicator[0].getResiduals();
            AlmostEqual(residuals[0],-3.48098, 1e-5);            
            AlmostEqual(residuals[1],-1.24643, 1e-5);            
            AlmostEqual(residuals[7],1.28446, 1e-5); 
            AlmostEqual(residuals[8],-0.39302, 1e-5);                                   
            AlmostEqual(residuals[9],-0.42234, 1e-5);            
            AlmostEqual(indicator[0].getLastResidual(),-0.42234, 1e-5);                    
            AlmostEqual(indicator[0].getSigma(),2.29937, 1e-5);            
            AlmostEqual(indicator[0].getLastZScore(),-0.18367, 1e-5);            
            AlmostEqual(indicator[0].getNumFactors(),2, 1e-5);
            AlmostEqual(indicator[0].predict(5),10.12916, 1e-5);
            AlmostEqual(indicator[0].rSquare(),0.78253, 1e-5);            
            AlmostEqual(indicator[0].rSquareAdj(),0.75535, 1e-5);
        }
        [Test]
        public void testOutputsNoIntercept() {            
            x = new RootSpud<double>(values.manager);           
            indicator = new QRegression(values,x,10,true);  
            addPoint(-5,-1);
            hasNaN();
            addPoint(5,3);           
            addPoint(1,0.5);             
            addPoint(3,1);             
            addPoint(2,1.1);             
            addPoint(8,1.3);             
            addPoint(-2,-1);             
            addPoint(-8,-5);             
            addPoint(1,0.5);             
            addPoint(0,1);

            checkOutputs(0, 1.89855);
            var residuals = indicator[0].getResiduals();
            AlmostEqual(residuals[0],-3.10144, 1e-5);            
            AlmostEqual(residuals[1],-0.69565, 1e-5);            
            AlmostEqual(residuals[7],1.49275, 1e-5); 
            AlmostEqual(residuals[8],0.05072, 1e-5);                                   
            AlmostEqual(residuals[9],-1.89855, 1e-5);            
            AlmostEqual(indicator[0].getLastResidual(),-1.89855, 1e-5);                          
            AlmostEqual(indicator[0].getSigma(),2.30395, 1e-5);            
            AlmostEqual(indicator[0].getLastZScore(),-0.92569, 1e-5);            
            AlmostEqual(indicator[0].getNumFactors(),1, 1e-5);
            AlmostEqual(indicator[0].predict(5),9.49275, 1e-5);
            AlmostEqual(indicator[0].rSquare(),0.75749, 1e-5);             
            AlmostEqual(indicator[0].rSquareAdj(),0.73054, 1e-5);
        }
    }
    
    [TestFixture]
    public class QRegressionForcedSlopeTest : QRegressionBaseTest {
        RootSpud<double> slope;
        [Test]
        public void testLongSeries() {            
            x = new RootSpud<double>(values.manager);           
            slope = new RootSpud<double>(values.manager);           
            indicator = new QRegressionForcedSlope(values,x,10,slope);  
            addPoint(-5,-1,0.5);
            hasNaN();
            addPoint(5,3,0.5);           
            addPoint(1,0.5,2);             
            addPoint(3,1,0.2);             
            addPoint(2,1.1,0.6);             
            addPoint(8,1.3,0.4);             
            addPoint(-2,-1,0.6);             
            addPoint(-8,-5,1.9);             
            addPoint(1,0.5,2);             
            addPoint(0,0,1.678);
            checkOutputs(0.43288,1.678);     
            addPoint(5,1,1.5);
            checkOutputs(1.14,1.5);     
            addPoint(4,1,0);
            checkOutputs(1.4,0);     
        }   
        [Test]
        public void testSlopeHasInfinity() {            
            x = new RootSpud<double>(values.manager);           
            slope = new RootSpud<double>(values.manager);           
            indicator = new QRegressionForcedSlope(values,x,3,slope);  
            addPoint(-5,-1,0.5);
            hasNaN();
            addPoint(1,-2,0);
            hasNaN();
            addPoint(-3,0,0.5);
            addPoint(-5,-1,double.NegativeInfinity);  
            hasNaN();
            addPoint(8,3,0.7);
            checkOutputs(-0.46666,0.7);   
            addPoint(5,1,0);
            checkOutputs(2.66666,0);   
            addPoint(4,0,-2);
            checkOutputs(8.33333,-2);   
        }
        [Test]
        public void testYIsConstant() {            
            x = new RootSpud<double>(values.manager);           
            slope = new RootSpud<double>(values.manager);           
            indicator = new QRegressionForcedSlope(values,x,3,slope);  
            addPoint(2,0,0.5);
            hasNaN();
            addPoint(2,-1,0.5);
            hasNaN();
            addPoint(2,3,0.7);
            checkOutputs(1.53333,0.7);
        }
        [Test]
        public void testXIsConstant() {            
            x = new RootSpud<double>(values.manager);           
            slope = new RootSpud<double>(values.manager);           
            indicator = new QRegressionForcedSlope(values,x,3,slope);  
            addPoint(2,-1,0.5);
            hasNaN();
            addPoint(3,-1,0.5);
            hasNaN();
            addPoint(1,-1,0.7);
            checkOutputs(2.7,0.7);
        }
        protected void addPoint(double newY, double newX, double newSlope) {
            addPoint(newY,newX); slope.set(newSlope); 
        }
    }
}