using Q.Simulator;

namespace Gui.Norad {
    public class EquityPlot : QDateGraphControl {
        public EquityPlot(Simulator simulator) : base(parent => new EquityPane(parent, simulator)) {
            var equityPane = ((EquityPane) mainPane());
            equityPane.addPlots(simulator);
            ScrollMinX = -5;
            ScrollMaxX = simulator.allCollector().dates().Count + 5;
            RestoreScale(mainPane());
        }
    }
}