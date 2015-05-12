using System.Collections.Generic;
using NUnit.Framework;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestSymbolSystemSpudDates : DbTestCase {
        static readonly Symbol A = new Symbol("A");
        static readonly Symbol B = new Symbol("B");
        QREBridge<IndependentSymbolSystems<TestSystemWithEWMA>> combined;
        QREBridge<IndependentSymbolSystems<TestSystemWithEWMA>> a;
        QREBridge<IndependentSymbolSystems<TestSystemWithEWMA>> b;

        [Test]
        public void testDifferentDatesBehaviorIsConsistent() {
            insertSymbol(A.name);
            insertSymbol(B.name);
            var systemId = OneSystemTest<TestSystemWithEWMA>.fakeLiveSystem(new Parameters {{"LeadBars", 2.0}}, false).id();
            var parameters = new Parameters {{"systemId", (double) systemId}, {"RunMode", (double) RunMode.LIVE}};
            combined = new SystemArguments(O.list(A, B), parameters).bridge<IndependentSymbolSystems<TestSystemWithEWMA>>();
            a = new SystemArguments(O.list(A), parameters).bridge<IndependentSymbolSystems<TestSystemWithEWMA>>();
            b = new SystemArguments(O.list(B), parameters).bridge<IndependentSymbolSystems<TestSystemWithEWMA>>();
            bar(1.0, null);
            bar(2.0, 1.0);
            bar(null, 2.0);
            bar(3.0, 3.0);
            AreEqual(ewma(a, A)[0], ewma(b, B)[0]);

            
        }

        void bar(double? aValue, double? bValue) {
            var bars = new Dictionary<Symbol, Bar>();
            if(aValue.HasValue) {
                bars.Add(A, new Bar(aValue.Value));
                a.processBar(O.dictionaryOne(A, new Bar(aValue.Value)));
            }
            if(bValue.HasValue) {
                bars.Add(B, new Bar(bValue.Value));
                b.processBar(O.dictionaryOne(B, new Bar(bValue.Value)));
            }
            combined.processBar(bars);

            var aCount = ewma(a, A).count();
            var bCount = ewma(b, B).count();
            AreEqual(ewma(combined, A).count(), aCount);
            AreEqual(ewma(combined, B).count(), bCount);
            if(aCount > 0)
                AreEqual(ewma(combined, A)[0], ewma(a, A)[0]);
            if(bCount > 0)
                AreEqual(ewma(combined, B)[0], ewma(b, B)[0]);
        }

        static EWMA ewma(QREBridge<IndependentSymbolSystems<TestSystemWithEWMA>> bridge, Symbol symbol) {
            return bridge.system.systems_[symbol].ewma;
        }
    }

    class TestSystemWithEWMA : SymbolSystem {
        internal readonly EWMA ewma;
        public TestSystemWithEWMA(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            ewma = new EWMA(bars.close, 5);
        }

        protected override void onFilled(Position position, Trade trade) {}
        protected override void onNewBar() {}
        protected override void onNewTick(Bar partialBar, Tick tick) {}
        protected override void onClose() {}
    }
}
