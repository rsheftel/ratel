using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public abstract class OneSymbolSystemTestCDS<S> : OneSymbolSystemTest<S> where S : SymbolSystem {
        protected override Symbol initializeSymbol()
        {
            return new Symbol("CDS.TEST.CAH.5Y.ACB20", 10000);
        }
    }
}