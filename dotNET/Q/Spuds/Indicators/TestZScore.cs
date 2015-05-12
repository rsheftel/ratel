using System;
using file;
using java.util;
using NUnit.Framework;
using O=Q.Util.Objects;
namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestZScore : SpudTestCase<double, double> {

        [Test]
        public void testZScoreWithDefaultConstructor() {
            indicator = new ZScoreSpud(values);
            testUnbiased();
        }

        [Test]
        public void testZScoreBiased() {
            indicator = new ZScoreSpud(values, true);
            addPoint(3, 0.0);
            addPoint(7, 1.0);
            addPoint(7);
            AlmostEqual((4.0 / 3.0) / Math.Sqrt(32.0/9.0), indicator[0], 1e-6);
            addPoint(19, 10.0 / 6.0);
        }

        [Test]
        public void testZScoreUnBiased() {
            indicator = new ZScoreSpud(values,false);
            testUnbiased();
        }
               
        [Test]
        public void testZScoreUnBiasedForWindow() {
            indicator = new ZScoreSpud(values,3);
            testUnbiasedThreeWindow();
        }

        [Test]
        public void testZScoreUnBiasedForWindowAlternative() {
            indicator = new ZScoreSpud(values,3,false);
            testUnbiasedThreeWindow();
        }

        [Test]
        public void testDavesUseCase() {
            indicator = new ZScoreSpud(values, 250, true);
            addPoints(@"..\..\testdata\Spuds\testSeries1.csv");
            AlmostEqual(0.470930066149, indicator[0], 1e-6);
            addPoint(36.539344090000, 0.377952911928, 1e-6);
            addPoint(39.180824180000, 0.615454770040, 1e-6);
        }

        [Test]
        public void testStandardDeviationUnbiased() {
            var zScoreSpud = new ZScoreSpud(values, 250, false);
            indicator = zScoreSpud;
            addPoint(3);
            addPoint(7);
            AlmostEqual(2.828427, zScoreSpud.standardDeviation(), 1e-6);
            addPoint(7);
            AlmostEqual(2.309401, zScoreSpud.standardDeviation(), 1e-6);
        }

        [Test]
        public void testStandardDeviationBiased() {
            var zScoreSpud = new ZScoreSpud(values, 250, true);
            indicator = zScoreSpud;
            addPoint(3);
            addPoint(7);
            AlmostEqual(2, zScoreSpud.standardDeviation(), 1e-6);
            addPoint(7);
            AlmostEqual(1.885618, zScoreSpud.standardDeviation(), 1e-6);
        }
        void addPoint(double newValue, double newTarget, double tolerance) {
            addPoint(newValue);
            AlmostEqual(newTarget, indicator[0], tolerance);
        }

        void testUnbiased() {
            addPoint(3, 0.0);
            addPoint(7,0.707107,1e-6);            
            addPoint(6,0.320256,1e-6);
            addPoint(8,0.92582, 1e-6); 
        }

        void testUnbiasedThreeWindow() {
            addPoint(3, 0.0);
            addPoint(7,0.707107,1e-6);            
            addPoint(6,0.320256,1e-6);
            addPoint(8,1); 
            addPoint(8.5,0.755929, 1e-6); 
        }

        void addPoints(string fileName) {
            var csv = new QFile(fileName).csv();
            Collection c = csv.records();
            foreach(var record in O.list<List>(c)) {
                addPoint(Double.Parse(O.the(O.list<string>(record))));
            }
        }
    }
}