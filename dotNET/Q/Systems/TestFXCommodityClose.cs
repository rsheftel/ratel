using NUnit.Framework;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    [TestFixture]
    public class TestFXCommodityClose : AbstractFXCommodityCloseTest {
        [Test]
        public void testList() {

            // enter long and exit
            // enter short and exit
            // enter long and stop and reactivate
            // enter short/stop/reactivate
            // try and enter long but can't due to MA of tri.
            // try and enter short but can't due to MA of tri.
        }    

        [Test]
        public void testEnterLongExit() {
            AreEqual(symbolSystem.bars[0].close, 6);
            close(7, 7);
            close(7, 7);
            close(7, 7);
            close(7, 7.5);
            noOrders();
            
            close(8, 11.0);
            requireLongEntry(8, 62);
            fill(0, 8.0);
            close(8, 11.0);
            requireLongStop(6.38709677419355);
            close(8, 10);
            requireLongStop(6.38709677419355);
            close(8, 10);
            requireLongStop(6.38709677419355);
            close(8, 9);
            requireLongStop(6.38709677419355);
            close(8, 8);
            requireLongExit(8,6.38709677419355,62);
            fill(1,8.0);
            noOrders();
        }
    
        [Test]    
        public void testEnterShortExit() {
            AreEqual(symbolSystem.bars[0].close, 6);
            close(5, 8.5);
            close(6, 8.5);
            close(6, 8.5);
            close(6, 8);
            noOrders();
            close(6, 7);
            requireShortEntry(6, 62);
            fill(0, 6);

            close(5, 7);
            requireShortStop(6.61290322580645);
            close(5, 7);
            requireShortStop(6.61290322580645);
            close(5, 7.5);
            requireShortStop(6.61290322580645);
            close(5, 7.5);
            requireShortStop(6.61290322580645);
            close(5, 7.5);
            requireShortStop(6.61290322580645);
            close(5, 8);
            requireShortExit(5.0, 6.61290322580645, 125);
            fill(1,5.0);
            noOrders();
        } 
        
    [Test]
        public void testEnterLongStopOutActivate() {
            close(5, 7);
            close(5, 7);
            close(5, 7);
            close(6, 7.5);
            close(8, 11);
            requireLongEntry(8.0, 31);
            fill(0, 8.0);

            close(9, 12);
            requireLongStop(5.7741935483871);
            close(5, 7.5);
            requireLongStop(5.7741935483871);
            fill(0, 5.0);

            close(5, 7.75);
            close(5, 7);
            close(5, 6);
            requireShortEntry(5, 25);
            fill(0, 5);

            close(5, 6);
            requireShortStop(9.0);

        }    
        
        [Test]
        public void testEnterShortStopOutActivate() {
            close(7.5, 8.5);
            close(7.5, 8.5);
            close(7.5, 8.5);
            close(7, 8.25);
            noOrders();
            close(6, 6.5);
            requireShortEntry(6, 41);
            fill(0, 6.0);

            close(4, 5);
            requireShortStop(6.4390243902439);
            close(4, 5);
            requireShortStop(6.4390243902439);
            close(6.5, 6.75);
            requireShortStop(6.4390243902439);
            fill(0, 6.5);
            noOrders();
            close(8, 9);
            close(9, 10);
            requireLongEntry(9, 17);
            fill(0, 9.0);
            close(9, 10);
            requireLongStop(3.1176470588235);
        }
    
        [Test]
        public void testDenyLongEntryOnNegativeMA() {
            close(5, 7);
            close(5, 7);
            close(4, 7);
            close(4, 7.5);
            close(3, 11);
            noOrders();
        }

        [Test]
        public void testDenyShortEntryOnPositiveMA() {
            close(7, 8.5);
            close(7, 8.5);
            close(7, 8.5);
            close(8, 8);
            close(9, 7);
            noOrders();
        }

        [Test]
        public void testStopOutOnMaxBarsHeld() {

            close(7, 7);
            close(7, 7);
            close(7, 7);
            close(7, 7.5);
            noOrders();

            close(8, 11.0);
            requireLongEntry(8, 62);
            fill(0, 8.0);
            Objects.zeroTo(
                9,
                i => close(8, 11));
            close(8, 11);
            close(8, 11);
            hasOrders(
                position().exit("Exit L StopOut", stop(6.38709677419355), oneBar()),
                position().exit("Exit L MaxBars", protectiveStop(8), oneBar()));

        }



// This has the correct currency precedence

        protected override Symbol initializeSymbol() {
            return new Symbol("EURUSDTRI", 10000);
        }
    }
}
