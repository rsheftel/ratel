package com.fftw.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bloomberg.BloombergSecurity;

import com.fftw.bloomberg.types.BBProductCode;

public class SystematicFacade {

    static final Logger log = LoggerFactory.getLogger(SystematicFacade.class);
    
    private static Map<String, Long> contractSizeCache = new HashMap<String, Long>();
    private static Map<String, String> equityTicker = new HashMap<String, String>();
    

    public static long lookupContractSize(String bloombergSymbol, BBProductCode productCode) {
        // Equities all have a 1 for the contract size
        if (productCode == BBProductCode.Equity) {
            return 1;
        }
        
        String bloombergKey = bloombergSymbol + " " + productCode.getShortString();
        
        Long contractSize = contractSizeCache.get(bloombergKey);
        if (contractSize != null) {
            return contractSize;
        }
        
        try {
            long cs = (long) BloombergSecurity.security(bloombergKey).numeric("FUT_CONT_SIZE");
            contractSizeCache.put(bloombergKey, cs);
            return cs;
        } catch (Exception e) {
            log.warn("Unable to lookup bloomberg security contract size: " + bloombergKey);
        }
        return 1;
    }

    public static String lookupExchangeTicker(String cusip) {
        String ticker = equityTicker.get(cusip);
        
        if (ticker != null) {
            return ticker;
        }
        try {
            StringBuilder queryStr = new StringBuilder(128);
            queryStr.append("/cusip/").append(cusip);
            
            ticker = BloombergSecurity.security(queryStr.toString()).string("TICKER");
            equityTicker.put(cusip, ticker);
            
            return ticker;
        } catch (Exception e) {
            log.error("Unable to lookup ticker for: " + cusip);
        }
        return "";
    }
    
}
