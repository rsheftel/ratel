package com.malbec.tsdb.ivydb.definitions;

import com.malbec.tsdb.ivydb.*;
import com.malbec.tsdb.ivydb.StdOptionPriceTable.*;

public class StdImpliedVolSeriesDefinition extends StdOptionSeriesDefinition {
	
	public StdImpliedVolSeriesDefinition() {
		super("vol_ln");
	}

	@Override protected Double seriesValue(StdOptionPriceRow row)  {
		return row.impliedVol();
	}
}
