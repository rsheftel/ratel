using System.Collections.Generic;

namespace Q.Trading {
    public interface SystemKey {
        bool coveredBy(Dictionary<Symbol, Bar> bars);
        List<Symbol> symbols();
    }
}