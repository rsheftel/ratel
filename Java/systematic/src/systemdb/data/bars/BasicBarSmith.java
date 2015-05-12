package systemdb.data.bars;

import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import systemdb.data.*;
import tsdb.*;
import util.*;

public class BasicBarSmith implements BarSmith {

    @Override public List<Bar> convert(HistoricalDailyData data, List<ProtoBar> observations) {
        List<Bar> result = empty();
        for(ProtoBar bar : observations)
            result.add(bar.asBar());
        return result;
    }

    @Override public String name() {
        return "";
    }
    
    public static <T> List<ProtoBar> protoBars(ObservationsMap<T> group, T open, T high, T low, T close, T volume, T openInterest) {
        List<ProtoBar> protoBars = empty();
        Set<Date> dateSet = emptySet();
        for(T ss : group)
	        dateSet.addAll(group.get(ss).times());
        List<Date> uniqueDates = list(dateSet);
        Collections.sort(uniqueDates);
        for(Date day : uniqueDates) {
            ProtoBar bar = new ProtoBar();
            // we move the timestamp to midnight, since the data is timestamped as of the close, and we are doing "daily" bars
            bar.date = midnight(day);
            bar.open = valueMaybe(group, day, open); 
            bar.high = valueMaybe(group, day, high); 
            bar.low = valueMaybe(group, day, low); 
            bar.close = valueMaybe(group, day, close); 
            bar.volume = valueMaybeLong(group, day, volume); 
            bar.openInterest = valueMaybeLong(group, day, openInterest); 
            protoBars.add(bar);
        }
        return protoBars;
	}

    private static <T> Long valueMaybeLong(ObservationsMap<T> group, Date day, T series) {
        if (series == null) return null; 
        return group.longValueMaybe(series, day);
    }

    private static <T> Double valueMaybe(ObservationsMap<T> group, Date day, T series) {
        if (series == null) return null; 
        return group.valueMaybe(series, day);
    }
    
    public static BarSmith calculator(String calculateName) {
        if (Strings.isEmpty(calculateName)) return new BasicBarSmith();
        if (calculateName.equals("OpenIsPriorClose")) return new OpenIsPriorClose();
        if (calculateName.equals("OmitIncomplete")) return new OmitIncomplete();
        throw bomb("calculateName of " + calculateName + " is unknown.");
    }
}
