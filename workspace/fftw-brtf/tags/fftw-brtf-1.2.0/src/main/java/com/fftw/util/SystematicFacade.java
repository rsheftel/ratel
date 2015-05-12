package com.fftw.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bloomberg.BloombergSecurity;

import com.fftw.bloomberg.types.BBProductCode;

public class SystematicFacade {

    static final Logger log = LoggerFactory.getLogger(SystematicFacade.class);

    public static long lookupContractSize(String bloombergSymbol, BBProductCode productCode) {
        String bloombergKey = bloombergSymbol + " " + productCode.getShortString();
        try {
            return (long) BloombergSecurity.security(bloombergKey).numeric("FUT_CONT_SIZE");
        } catch (Exception e) {
            log.error("Unable to lookup bloomberg security contract size: " + bloombergKey);
        }
        return 1;
    }

    public static String lookupExchangeTicker(String cusip) {
        try {
            StringBuilder queryStr = new StringBuilder(128);
            queryStr.append("/cusip/").append(cusip);
            
            return BloombergSecurity.security(queryStr.toString()).string("TICKER");
        } catch (Exception e) {
            log.error("Unable to lookup ticker for: " + cusip);
        }
        return "";
    }
    
}
