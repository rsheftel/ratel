package systemdb.data.bars;

import static util.Objects.*;

import java.util.*;

import systemdb.data.*;

public class OpenIsPriorClose implements BarSmith {

    @Override public List<Bar> convert(HistoricalDailyData data, List<ProtoBar> protos) {
        List<Bar> result = empty();
        if (protos.isEmpty()) return result;
        Double priorClose = data.lastCloseBefore(first(protos).date);
        for(ProtoBar bar : protos) {
            if (priorClose != null) result.add(bar.fromClose(priorClose));
            priorClose = bar.close;
        }
        return result;
    }

    @Override public String name() {
        return "OpenIsPriorClose";
    }

}
