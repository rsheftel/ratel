using System.Collections.Generic;
using Q.Trading;

namespace Gui.Norad {
    public interface FilteredPositionsPlot {
        void updatePlot(IEnumerable<Position> positions);
    }
}