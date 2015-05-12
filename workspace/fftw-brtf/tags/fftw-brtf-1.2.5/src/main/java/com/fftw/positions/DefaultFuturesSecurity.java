package com.fftw.positions;

import malbec.util.FuturesSymbolUtil;

import com.fftw.bloomberg.types.BBFuturesCategory;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.BBSecurityType;

public class DefaultFuturesSecurity extends DefaultSecurity implements IFuturesSecurity {

    private BBFuturesCategory futuresCategory;

    private boolean useSecurityRoot;

    private String securityIdRoot;

    public DefaultFuturesSecurity(String name, BBProductCode productCode, String securityId,
        BBSecurityIDFlag securityIdFlag, BBSecurityType securityType2, String ticker,
        BBFuturesCategory futuresCategory) {
        super(name, productCode, securityId, securityIdFlag, securityType2, ticker);

        this.futuresCategory = futuresCategory;
    }

    public BBFuturesCategory getFuturesCategory() {
        return futuresCategory;
    }

    public String getSecurityRoot() {
        if (securityIdRoot == null) {
            securityIdRoot = FuturesSymbolUtil.extractSymbolRoot(super.getSecurityId());
        }

        return securityIdRoot;
    }

    @Override
    public void setUseSecurityRoot(boolean useSecurityRoot) {
        this.useSecurityRoot = useSecurityRoot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fftw.positions.DefaultSecurity#getSecurityId()
     */
    @Override
    public String getSecurityId() {
        if (useSecurityRoot) {
            return getSecurityRoot();
        }
        return super.getSecurityId();
    }

    public String getTicker() {
        if (useSecurityRoot) {
            return null;
        }
        
        return super.getTicker();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fftw.positions.DefaultSecurity#copy()
     */
    @Override
    public ISecurity copy() {
        DefaultFuturesSecurity copy = new DefaultFuturesSecurity(getName(), getProductCode(),
            getSecurityId(), getSecurityIdFlag(), getSecurityType2(), getTicker(), futuresCategory);

        copy.useSecurityRoot = useSecurityRoot;

        return copy;
    }

}
