package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.Platform;
import com.fftw.bfa.admin.seam.model.TradeRecord;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("tradeRecordHome")
public class TradeRecordHome extends EntityHome<TradeRecord> {

	@In(create = true)
	PlatformHome platformHome;

	public void setTradeRecordTradeRecordId(Long id) {
		setId(id);
	}

	public Long getTradeRecordTradeRecordId() {
		return (Long) getId();
	}

	@Override
	protected TradeRecord createInstance() {
		TradeRecord tradeRecord = new TradeRecord();
		return tradeRecord;
	}

	public void wire() {
		Platform platform = platformHome.getDefinedInstance();
		if (platform != null) {
			getInstance().setPlatform(platform);
		}
	}

	public boolean isWired() {
		if (getInstance().getPlatform() == null)
			return false;
		return true;
	}

	public TradeRecord getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
