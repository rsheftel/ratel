package com.fftw.bloomberg.rtf;

import com.fftw.sbp.ProtocolSessionID;
import com.fftw.bloomberg.rtf.types.RtfMode;

/**
 *
 */
public class RealtimeSessionID implements ProtocolSessionID {

    private int pricingNumber;
    private int siteNumber;
    private RtfMode mode;

    public RealtimeSessionID() {

    }

    public RealtimeSessionID(int pricing, int site, RtfMode mode) {
        this.pricingNumber = pricing;
        this.siteNumber = site;
        this.mode = mode;
    }

    public String idAsString() {
        return pricingNumber + "-" + siteNumber + "-" + mode;
    }

    public void populateFromString(String strID) {

        String[] strParts = strID.split("\\-");

        pricingNumber = Integer.parseInt(strParts[0]);
        siteNumber = Integer.parseInt(strParts[1]);
        mode = RtfMode.valueOf(strParts[2]);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (!(obj instanceof RealtimeSessionID) || obj == null) {
            return false;
        }

        RealtimeSessionID other = (RealtimeSessionID) obj;

        return pricingNumber == other.pricingNumber && siteNumber == other.siteNumber && mode.equals(other.mode); 
    }

    public int hashCode() {
        int hashCode = (17 * pricingNumber) + (siteNumber * 19) + mode.ordinal();
        return hashCode;
    }
}
