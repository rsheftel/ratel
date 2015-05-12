package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.TradeRecord;
import com.fftw.bfa.admin.seam.model.TradingStrategy;
import com.fftw.bfa.admin.seam.model.FuturesSymbolMapping;
import com.fftw.bfa.admin.seam.model.Platform;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("platformHome")
public class PlatformHome extends EntityHome<Platform> {

	public void setPlatformPlatformId(String id) {
		setId(id);
	}

	public String getPlatformPlatformId() {
		return (String) getId();
	}

	@Override
	protected Platform createInstance() {
		Platform platform = new Platform();
		return platform;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public Platform getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<TradeRecord> getTradeRecords() {
		return getInstance() == null ? null : new ArrayList<TradeRecord>(
				getInstance().getTradeRecords());
	}
	public List<FuturesSymbolMapping> getFuturesSymbolMappings() {
		return getInstance() == null
				? null
				: new ArrayList<FuturesSymbolMapping>(getInstance()
						.getFuturesSymbolMappings());
	}
	public List<TradingStrategy> getTradingStrategies() {
		return getInstance() == null ? null : new ArrayList<TradingStrategy>(
				getInstance().getTradingStrategies());
	}

}
