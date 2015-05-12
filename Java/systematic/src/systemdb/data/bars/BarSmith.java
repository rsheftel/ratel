package systemdb.data.bars;

import java.util.*;

import systemdb.data.*;

public interface BarSmith {
    List<Bar> convert(HistoricalDailyData data, List<ProtoBar> observations);
    String name();
}