package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.Platform;
import com.fftw.bfa.admin.seam.model.TradingStrategy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("tradingStrategyHome")
public class TradingStrategyHome extends EntityHome<TradingStrategy> {

	@In(create = true)
	PlatformHome platformHome;

	public void setTradingStrategyId(Long id) {
		setId(id);
	}

	public Long getTradingStrategyId() {
		return (Long) getId();
	}

	@Override
	protected TradingStrategy createInstance() {
		TradingStrategy tradingStrategy = new TradingStrategy();
		return tradingStrategy;
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

	public TradingStrategy getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
