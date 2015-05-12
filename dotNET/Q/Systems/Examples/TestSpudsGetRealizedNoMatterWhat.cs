using NUnit.Framework;
using Q.Util;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestSpudsGetRealizedNoMatterWhat : OneSymbolSystemTest<EmptySystem> {
        public override void setUp() {
            base.setUp();
            Objects.zeroTo(arguments().leadBars, i => {
                processBar(1, 3, 1, 2);
                noOrders(); 
            });
        }

        [Test]
        public void testStuff() {
            processBar(1,3,1,2);
            var latest = double.NaN;
            symbolSystem.bars.close.valueChanged += value => latest = value;
            processTick(3.5);
            AreEqual(3.5, latest);
            processTick(4.0);
            AreEqual(4.0, latest);
        }

        protected override int leadBars() {
            return 5;
        }

        
    }
}