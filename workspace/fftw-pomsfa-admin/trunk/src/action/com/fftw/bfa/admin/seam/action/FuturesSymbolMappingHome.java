package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.FuturesSymbolMapping;
import com.fftw.bfa.admin.seam.model.Platform;
import com.fftw.bfa.admin.seam.model.ProductCode;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("futuresSymbolMappingHome")
public class FuturesSymbolMappingHome extends EntityHome<FuturesSymbolMapping> {

	@In(create = true)
	ProductCodeHome productCodeHome;
	@In(create = true)
	PlatformHome platformHome;

	public void setFuturesSymbolMappingId(Long id) {
		setId(id);
	}

	public Long getFuturesSymbolMappingId() {
		return (Long) getId();
	}

	@Override
	protected FuturesSymbolMapping createInstance() {
		FuturesSymbolMapping futuresSymbolMapping = new FuturesSymbolMapping();
		return futuresSymbolMapping;
	}

	public void wire() {
		ProductCode productCode = productCodeHome.getDefinedInstance();
		if (productCode != null) {
			getInstance().setProductCode(productCode);
		}
		Platform platform = platformHome.getDefinedInstance();
		if (platform != null) {
			getInstance().setPlatform(platform);
		}
	}

	public boolean isWired() {
		if (getInstance().getProductCode() == null)
			return false;
		if (getInstance().getPlatform() == null)
			return false;
		return true;
	}

	public FuturesSymbolMapping getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
