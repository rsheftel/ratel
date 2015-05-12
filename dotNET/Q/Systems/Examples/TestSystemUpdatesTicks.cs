using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;


namespace Q.Systems.Examples {
    [TestFixture]
    public class TestSystemUpdatesTicks : OneSystemTest<IndependentSymbolSystems<EmptySystem>> {
        static readonly Symbol FOO = new Symbol("RE.TEST.TY.1C");
        static readonly Symbol BAR = new Symbol("RE.TEST.TU.1C");

        protected override int leadBars() { return 0; }
        protected override SystemArguments arguments() {
            return new SystemArguments(O.list(FOO, BAR), parameters());
        }

        [Test] public void testUpdatesTicks() {
            var bars = new Dictionary<Symbol, Bar> {
                { FOO, new Bar(5,5,5,5) },
                { BAR, new Bar(6,6,6,6) }
            };
            processBar(bars);
            var time = date("2008/12/07 16:00:01");
            var time2 = date("2008/12/07 16:00:02");
            processTick(FOO, 17, time);
            processTick(BAR, 17, time2);
            AreEqual(time, bridge().bars()[FOO].lastTickedAt());
            AreEqual(time2, bridge().bars()[BAR].lastTickedAt());

        }
    }
}