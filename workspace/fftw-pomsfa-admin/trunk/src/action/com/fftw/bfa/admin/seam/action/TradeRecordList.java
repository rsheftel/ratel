package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.TradeRecord;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.List;
import java.util.Arrays;

@Name("tradeRecordList")
public class TradeRecordList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(tradeRecord.execId) like concat(lower(#{tradeRecordList.tradeRecord.execId}),'%')",
			"lower(tradeRecord.securityId) like concat(lower(#{tradeRecordList.tradeRecord.securityId}),'%')",
			"lower(tradeRecord.traderName) like concat(lower(#{tradeRecordList.tradeRecord.traderName}),'%')",
			"lower(tradeRecord.account) like concat(lower(#{tradeRecordList.tradeRecord.account}),'%')",
			"lower(tradeRecord.broker) like concat(lower(#{tradeRecordList.tradeRecord.broker}),'%')",
			"lower(tradeRecord.primeBroker) like concat(lower(#{tradeRecordList.tradeRecord.primeBroker}),'%')",
			"lower(tradeRecord.errorMsg) like concat(lower(#{tradeRecordList.tradeRecord.errorMsg}),'%')",
			"lower(tradeRecord.processingStatus) like concat(lower(#{tradeRecordList.tradeRecord.processingStatus}),'%')",
			"lower(tradeRecord.tradingStrategy) like concat(lower(#{tradeRecordList.tradeRecord.tradingStrategy}),'%')",
			"lower(tradeRecord.transCode) like concat(lower(#{tradeRecordList.tradeRecord.transCode}),'%')",};

	private TradeRecord tradeRecord = new TradeRecord();

	@Override
	public String getEjbql() {
		return "select tradeRecord from TradeRecord tradeRecord";
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public TradeRecord getTradeRecord() {
		return tradeRecord;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}

}
