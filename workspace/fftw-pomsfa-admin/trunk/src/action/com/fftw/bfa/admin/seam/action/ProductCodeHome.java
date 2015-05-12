package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.FuturesSymbolMapping;
import com.fftw.bfa.admin.seam.model.ProductCode;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("productCodeHome")
public class ProductCodeHome extends EntityHome<ProductCode> {

	public void setProductCodeTextCode(String id) {
		setId(id);
	}

	public String getProductCodeTextCode() {
		return (String) getId();
	}

	@Override
	protected ProductCode createInstance() {
		ProductCode productCode = new ProductCode();
		return productCode;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public ProductCode getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<FuturesSymbolMapping> getFuturesSymbolMappings() {
		return getInstance() == null
				? null
				: new ArrayList<FuturesSymbolMapping>(getInstance()
						.getFuturesSymbolMappings());
	}

}
