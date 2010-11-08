package org.ratel.tsdb;

import java.util.*;

import org.ratel.db.*;

public interface TimeSeriesGroupDefinition {

    SelectOne<Integer> seriesLookup(int id, Date asOf);

    void delete(int id);

}