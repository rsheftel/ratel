using NUnit.Framework;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestRandomWalkKalman : SpudTestCase<double, double> {
        protected void addPoints() {
            addPoint(138.9);
            AreEqual(indicator[0],values[0]);
            addPoint(138.09);
            addPoint(138.02);
            addPoint(140.78);            
        }
        protected void addPointsLong() {
            addPoints();
            addPoint(136.29);            
            addPoint(136.62);           
            addPoint(135.94);
            addPoint(133.94);
            addPoint(134.45);
            addPoint(136.15);
            addPoint(136.23);
            addPoint(135.57);
        }
        
        [Test]
        public void testBase() {           
            indicator = new RandomWalkKalman(values,1,5,true);            
            addPoints();
            AreEqual(indicator[0],values[0]);
            addPoint(136.29);
            AlmostEqual(indicator,132.199920,1e-6);
            addPoint(136.62);
            AlmostEqual(indicator,131.610806,1e-6);
            addPoint(135.94);
            AlmostEqual(indicator,130.985779,1e-6);           
        }

        [Test]
        public void testHigherFrequency() {           
            indicator = new RandomWalkKalman(values,3,5,true);
            addPointsLong();      
            AreEqual(indicator[0],values[0]);
            addPoint(134.25);
            AlmostEqual(indicator,129.852016,1e-6);     
            addPoint(134.42);
            AlmostEqual(indicator,129.643252,1e-6);     
            addPoint(131.58);            
        }

        [Test]
        public void testHigherWindow() {           
            indicator = new RandomWalkKalman(values,4,6,true);
            addPointsLong();
            addPoint(134.25);  
            addPoint(134.42);            
            addPoint(131.58);            
            addPoint(131.45);
            addPoint(131.19);
            addPoint(131.81);
            addPoint(128.32);
            addPoint(127.53);
            addPoint(127.98);
            AlmostEqual(indicator,127.728186,1e-6);     
            addPoint(128.38);
            AlmostEqual(indicator,128.187677,1e-6);     
        }
        [Test]
        public void testLowWindow() {
            indicator = new RandomWalkKalman(values,4,3,true);
            addPoints();
            addPoint(136.29);            
            addPoint(136.62);           
            addPoint(135.94);
            addPoint(133.94);
            AreEqual(indicator[0],values[0]);
            addPoint(134.45);
            AlmostEqual(indicator,102.945855,1e-6); 
        }

        [Test]
        public void testBaseWithoutGoldenRatio() {           
            indicator = new RandomWalkKalman(values,1,4);            
            addPoints();            
            AlmostEqual(indicator,139.771165,1e-6);
            addPoint(136.29);
            AlmostEqual(indicator,137.596920,1e-6);
            addPoint(136.62);
            AlmostEqual(indicator,136.998462,1e-6);           
        }        
    }
}