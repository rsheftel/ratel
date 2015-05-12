package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.ProductCode;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.List;
import java.util.Arrays;

@Name("productCodeList")
public class ProductCodeList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(productCode.textCode) like concat(lower(#{productCodeList.productCode.textCode}),'%')",
			"lower(productCode.description) like concat(lower(#{productCodeList.productCode.description}),'%')",};

	private ProductCode productCode = new ProductCode();

	@Override
	public String getEjbql() {
		return "select productCode from ProductCode productCode";
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public ProductCode getProductCode() {
		return productCode;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}

}
