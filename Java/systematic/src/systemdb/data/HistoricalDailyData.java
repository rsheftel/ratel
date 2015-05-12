package systemdb.data;

import java.util.*;

import util.*;

public interface HistoricalDailyData {
    List<Bar> bars(Range range);
    List<Bar> lastBars(int count);
    Double lastCloseBefore(Date date);
}