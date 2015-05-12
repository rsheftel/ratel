package com.fftw.bloomberg.rtf.messages;

import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import org.joda.time.LocalDate;

/**
 *
 */
public interface RtfMessageTradeBody extends RtfMessageBody {


    BBSecurityIDFlag getSecurityIdFlag();

    String getSecurityId();

    String getAccount();

    BBProductCode getProductCode();

    LocalDate getTradeDate();

    int getLevel1TagId();

    String getLevel1TagName();

    int getLevel2TagId();

    String getLevel2TagName();

    int getLevel3TagId();

    String getLevel3TagName();

    int getLevel4TagId();

    String getLevel4TagName();

    int getLevel5TagId();

    String getLevel5TagName();

    int getLevel6TagId();

    String getLevel6TagName();

    String getCfd();

}
