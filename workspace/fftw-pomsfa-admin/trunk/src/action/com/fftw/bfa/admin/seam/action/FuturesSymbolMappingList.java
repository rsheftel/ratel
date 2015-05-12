package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.FuturesSymbolMapping;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.List;
import java.util.Arrays;

@Name("futuresSymbolMappingList")
public class FuturesSymbolMappingList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(futuresSymbolMapping.platformSymbol) like concat(lower(#{futuresSymbolMappingList.futuresSymbolMapping.platformSymbol}),'%')",
			"lower(futuresSymbolMapping.bloombergSymbol) like concat(lower(#{futuresSymbolMappingList.futuresSymbolMapping.bloombergSymbol}),'%')",
			"lower(futuresSymbolMapping.description) like concat(lower(#{futuresSymbolMappingList.futuresSymbolMapping.description}),'%')",};

	private FuturesSymbolMapping futuresSymbolMapping = new FuturesSymbolMapping();

	@Override
	public String getEjbql() {
		return "select futuresSymbolMapping from FuturesSymbolMapping futuresSymbolMapping";
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public FuturesSymbolMapping getFuturesSymbolMapping() {
		return futuresSymbolMapping;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}

}
