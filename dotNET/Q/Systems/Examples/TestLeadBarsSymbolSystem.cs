using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestLeadBarsSymbolSystem : OneSystemTest<IndependentSymbolSystems<EmptySystem>> {
        static readonly List<Symbol> symbols = O.list(new Symbol("RE.TEST.TY.1C"), new Symbol("RE.TEST.TU.1C"));

        [Test]
        public void testLeadBars() {
            var a = symbols[0];
            var b = symbols[1];
            var bar = new Bar(0, 0, 0, 0);
            IsTrue(system().systems_[a].inLeadBars());
            IsTrue(system().systems_[b].inLeadBars());
            processBar(O.dictionaryOne(a, bar));
            IsFalse(system().systems_[a].inLeadBars());
            IsTrue(system().systems_[b].inLeadBars());
            processBar(O.dictionaryOne(b, bar));
            IsFalse(system().systems_[a].inLeadBars());
            IsFalse(system().systems_[b].inLeadBars());

        }

        protected override SystemArguments arguments() {
            return new SystemArguments(symbols, parameters());
        }

        protected override int leadBars() {
            return 0;
        }
    }
}
