package com.fftw.positions;

import com.fftw.bloomberg.types.BBFuturesCategory;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.BBSecurityType;

public class IndexFutures extends DefaultFuturesSecurity {

    public IndexFutures(String name, BBProductCode productCode, String securityId,
        BBSecurityIDFlag securityIdFlag, BBSecurityType securityType2, String ticker,
        BBFuturesCategory futuresCategory) {

        super(name, productCode, securityId, securityIdFlag, securityType2, ticker, futuresCategory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fftw.positions.DefaultSecurity#copy()
     */
    @Override
    public ISecurity copy() {
        return new IndexFutures(getName(), getProductCode(), getSecurityId(), getSecurityIdFlag(),
            getSecurityType2(), getTicker(), getFuturesCategory());
    }
}
