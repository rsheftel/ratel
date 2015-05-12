using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public abstract class OneSymbolSystemTestReverseDTD<S> : OneSymbolSystemTest<S> where S : SymbolSystem {
        protected override Symbol initializeSymbol()
        {
            return new Symbol("TESTDTDSTOCK.SPY.TRI", 10000);
        }
    }
}