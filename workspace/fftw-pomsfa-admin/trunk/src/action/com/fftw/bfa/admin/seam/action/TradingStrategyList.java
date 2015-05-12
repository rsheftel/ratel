package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.TradingStrategy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.List;
import java.util.Arrays;

@Name("tradingStrategyList")
public class TradingStrategyList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(tradingStrategy.tagValue) like concat(lower(#{tradingStrategyList.tradingStrategy.tagValue}),'%')",
			"lower(tradingStrategy.bloombergAccount) like concat(lower(#{tradingStrategyList.tradingStrategy.bloombergAccount}),'%')",
			"lower(tradingStrategy.bloombergStrategy) like concat(lower(#{tradingStrategyList.tradingStrategy.bloombergStrategy}),'%')",};

	private TradingStrategy tradingStrategy = new TradingStrategy();

	@Override
	public String getEjbql() {
		return "select tradingStrategy from TradingStrategy tradingStrategy";
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public TradingStrategy getTradingStrategy() {
		return tradingStrategy;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}

}
