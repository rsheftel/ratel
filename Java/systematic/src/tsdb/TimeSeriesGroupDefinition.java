package tsdb;

import java.util.*;

import db.*;

public interface TimeSeriesGroupDefinition {

	SelectOne<Integer> seriesLookup(int id, Date asOf);

    void delete(int id);

}