package org.ratel.tsdb.loader;

import org.ratel.tsdb.*;

public abstract class TimeSeriesDefinition<ROW> {
    
    protected abstract String name(AttributeValues values);
    public abstract TimeSeriesDataPoint dataPoint(ROW row, TimeSeriesLookup lookup);

    protected int id(AttributeValues values, TimeSeriesLookup lookup) {
        Integer id = lookup.id(values);
        if(id != null)
            return id;
        return lookup.create(name(values), values);
    }
    
}