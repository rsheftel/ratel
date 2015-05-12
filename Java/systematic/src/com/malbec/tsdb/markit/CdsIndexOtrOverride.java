package com.malbec.tsdb.markit;

import static db.columns.DatetimeColumn.*;
import static db.temptables.TSDB.MaxSeriesVersionBase.*;
import static util.Dates.*;
import static util.Objects.*;
import static util.Range.*;
import static util.Sequence.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.tables.TSDB.*;

public class CdsIndexOtrOverride extends CdsIndexTickerOtrOverrideBase {
    private static final long serialVersionUID = 1L;
    public static CdsIndexOtrOverride OVERRIDE = new CdsIndexOtrOverride();
    
    public CdsIndexOtrOverride() {
        super("override");
    }

    public void insert(String markitName, Date start, Date end, int series, int version) {
        insert(
            C_MARKIT_NAME.with(markitName), 
            C_START_DATE.with(start), 
            C_END_DATE.withMaybe(end), 
            C_OTR_SERIES.with(series), 
            C_OTR_VERSION.with(version)
        );
    }

    public void updateMaxSeriesVersion(Date asOf) {
        Clause isAlive = dateInRange(asOf, C_START_DATE, C_END_DATE);
        Clause isMarkit = T_MAXSERIESVERSION.C_NAME.is(C_MARKIT_NAME);
        T_MAXSERIESVERSION.updateAll(new Row(
            T_MAXSERIESVERSION.C_SERIES.withColumn(C_OTR_SERIES), 
            T_MAXSERIESVERSION.C_VERSION.withColumn(C_OTR_VERSION)
        ), isMarkit.and(isAlive));
    }

    public class OverrideRow extends Row {
        private static final long serialVersionUID = 1L;
        public OverrideRow(Row r) {super(r);}
        public String shortString() { 
            return value(C_MARKIT_NAME) + " is using" +
            		" series: " + value(C_OTR_SERIES) + " version: " + value(C_OTR_VERSION) + 
            		" from " + range(value(C_START_DATE), value(C_END_DATE));
        }
    }
    
    public List<OverrideRow> overriddenNear(Date asOf) {
        List<OverrideRow> result = empty();
        for(int i : zeroTo(7))
            for(Row r : rows(dateInRange(daysAhead(i, asOf), C_START_DATE, C_END_DATE))) {
                OverrideRow override = new OverrideRow(r);
                if (result.contains(override)) continue;
                result.add(override);
            }
        return result;
    }

}
