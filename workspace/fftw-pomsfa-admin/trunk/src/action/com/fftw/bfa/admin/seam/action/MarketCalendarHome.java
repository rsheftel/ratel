package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.MarketCalendar;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("marketCalendarHome")
public class MarketCalendarHome extends EntityHome<MarketCalendar> {

	public void setMarketCalendarId(Long id) {
		setId(id);
	}

	public Long getMarketCalendarId() {
		return (Long) getId();
	}

	@Override
	protected MarketCalendar createInstance() {
		MarketCalendar marketCalendar = new MarketCalendar();
		return marketCalendar;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public MarketCalendar getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
