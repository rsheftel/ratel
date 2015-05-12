package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.MarketCalendar;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.List;
import java.util.Arrays;

@Name("marketCalendarList")
public class MarketCalendarList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(marketCalendar.marketId) like concat(lower(#{marketCalendarList.marketCalendar.marketId}),'%')",
			"lower(marketCalendar.description) like concat(lower(#{marketCalendarList.marketCalendar.description}),'%')",};

	private MarketCalendar marketCalendar = new MarketCalendar();

	@Override
	public String getEjbql() {
		return "select marketCalendar from MarketCalendar marketCalendar";
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public MarketCalendar getMarketCalendar() {
		return marketCalendar;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}

}
