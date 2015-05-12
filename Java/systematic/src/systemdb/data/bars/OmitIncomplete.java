package systemdb.data.bars;

import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import systemdb.data.*;

public class OmitIncomplete implements BarSmith {

    @Override public List<Bar> convert(HistoricalDailyData data, List<ProtoBar> observations) {
        List<Bar> result = empty();
        for(ProtoBar bar : observations)
            try { result.add(bar.asBar()); }
            catch (Exception e) { 
                info("dropping incomplete bar " + bar); 
            }
        return result;
    }

    @Override public String name() {
        return "OmitIncomplete";
    }

}
