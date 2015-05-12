package bloomberg;

import static db.tables.BloombergFeedDB.BloombergDataBase.*;
import static tsdb.DataSource.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Strings.*;

import java.util.*;

import tsdb.*;

public class BloombergCheck {

    static Date d = yesterday();
    
    @SuppressWarnings("deprecation") public static void main(String[] args) {
        debugSql(false);
        List<String> items = T_BLOOMBERGDATA.C_NAMETIMESERIES.values();
        for (String item : items) {
            SeriesSource ss = new SeriesSource(item);
            SeriesSource newSs = ss.series().with(BLOOMBERG_TEST);
            boolean oldExists = ss.hasObservationToday(d);
            boolean newExists = newSs.hasObservationToday(d);
            if (oldExists ^ newExists ) {
                debug("old ss " + ss +  " " + paren(""+ oldExists) + " does not match " + newSs + paren(""+ newExists));
                continue;
            }
            if (!oldExists) continue;
            try {
                compareValues(ss, newSs);
            } catch (RuntimeException e) {
                debug(e.getMessage() + "\n\t" + e.getCause().getMessage());
            }
        }
    }

    private static void compareValues(SeriesSource ss, SeriesSource newSs) {
        double oldValue = ss.observationValue(d);
        double newValue = newSs.observationValue(d);
        if (oldValue != newValue)
            info("old value for ss " + ss + " " + paren(""+ oldValue) + " does not match " + paren("" + newValue));
    }

}
