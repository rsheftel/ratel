package com.malbec.tsdb.ivydb.definitions;

import com.malbec.tsdb.ivydb.*;
import com.malbec.tsdb.ivydb.StdOptionPriceTable.*;

public class StdDeltaSeriesDefinition extends StdOptionSeriesDefinition {
	
	public StdDeltaSeriesDefinition() {
		super("delta");
	}

	@Override protected Double seriesValue(StdOptionPriceRow row)  {
		return row.delta();
	}
}
