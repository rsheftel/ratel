using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestFXCommodityCloseReversed : AbstractFXCommodityCloseTest {
          
        protected override Symbol initializeSymbol() {
            return new Symbol("USDCHFTRI", 10000);
        }

         [Test]
        public void testEnterLongExit() {
            AreEqual(symbolSystem.bars[0].close, 6);
            close(5, 7);
            close(5, 7);
            close(5, 7);
            close(5, 7.5);
            noOrders();
            
            close(4, 11.0);
            requireReversedShortEntry(4, 62);
            fill(0, 4.0);
            close(4, 11.0);
            requireShortStop(5.61290322580645);
            close(4, 10);
            requireShortStop(5.61290322580645);
            close(4, 10);
            requireShortStop(5.61290322580645);
            close(4, 9);
            requireShortStop(5.61290322580645);
            close(4, 8);
            requireShortExit(4,5.61290322580645,62);
            fill(1,4.0);
            noOrders();
        }
        
        [Test]
        public void testEnterShortExit() {
            AreEqual(symbolSystem.bars[0].close, 6);
            close(5, 8.5);
            close(7, 8.5);
            close(7, 8.5);
            close(7, 8);
            noOrders();
            close(7, 7);
            requireReversedLongEntry(7, 41);
            fill(0, 7);

            close(7, 7);
            requireLongStop(4.5609756097561);
            close(7, 7);
            requireLongStop(4.5609756097561);
            close(7, 7.5);
            requireLongStop(4.5609756097561);
            close(7, 7.5);
            requireLongStop(4.5609756097561);
            close(7, 7.5);
            requireLongStop(4.5609756097561);
            close(7, 8);
            requireLongExit(7.0, 4.5609756097561, 125);
            fill(1,7.0);
            noOrders();
        } 
        
        [Test]
        public void testEnterLongStopOutActivate() {
            close(5, 7);
            close(5, 7);
            close(5, 7);
            close(6, 7.5);
            close(4, 11);
            requireReversedShortEntry(4, 31);
            fill(0, 4);

            close(4, 12);
            requireShortStop(7.2258064516129);
            close(8, 7.5);
            requireShortStop(7.2258064516129);
            fill(0, 8.0);

            close(8, 7.75);
            close(8, 7);
            close(8, 6);
            requireReversedLongEntry(8, 31);
            fill(0, 8);

            close(5, 6);
            requireLongStop(4.7741935483871);

        }    
        
        [Test]
        public void testEnterShortStopOutActivate() {
            close(7.5, 8.5);
            close(7.5, 8.5);
            close(7.5, 8.5);
            close(7, 8.25);
            noOrders();
            close(8, 6.5);
            requireReversedLongEntry(8, 41);
            fill(0, 8);

            close(8, 5);
            requireLongStop(5.5609756097561);
            close(8, 5);
            requireLongStop(5.5609756097561);
            close(5, 6.75);
            requireLongStop(5.5609756097561);
            fill(0, 5);
            noOrders();
            close(5, 9);
            close(5, 10);
            requireReversedShortEntry(5, 41);
            fill(0, 5.0);
            close(5, 10);
            requireShortStop(7.4390243902439);
        }
        [Test]
        public void testDenyLongEntryOnNegativeMA() {
            close(6, 7);
            close(7, 7);
            close(7, 7);
            close(8, 7.5);
            close(8, 11);
            noOrders();
        }

        [Test]
        public void testDenyShortEntryOnPositiveMA() {
            close(7, 8.5);
            close(6, 8.5);
            close(5, 8.5);
            close(4, 8);
            close(4, 7);
            noOrders();
        }



        protected void requireReversedShortEntry(double shortEntry, long size) {
            hasOrders(
                symbol().sell("Upper Range Break", protectiveStop(shortEntry), size, oneBar())
                );
        }

        protected void requireReversedLongEntry(double longEntry, long size) {
            hasOrders(
                symbol().buy("Lower Range Break", protectiveStop(longEntry), size, oneBar())
                );
        }
    }
}