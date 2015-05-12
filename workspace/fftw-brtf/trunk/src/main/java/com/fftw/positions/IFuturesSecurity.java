package com.fftw.positions;

import com.fftw.bloomberg.types.BBFuturesCategory;

public interface IFuturesSecurity extends ISecurity {

    BBFuturesCategory getFuturesCategory();

    String getSecurityRoot();
    
    void setUseSecurityRoot(boolean useSecurityRoot);

}