package com.fftw.positions;

import java.util.Map;

import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.BBSecurityType;

public interface ISecurity {

    BBSecurityIDFlag getSecurityIdFlag();

    String getSecurityId();

    String getTicker();

    BBProductCode getProductCode();

    String getName();

    BBSecurityType getSecurityType2();

    ISecurity copy();

    ISecurity copy(Map<String, Object> newValues);

    ISecurity combineWith(ISecurity other);

    void setName(String name);

    String toTextMessage(String delimiter);

}
