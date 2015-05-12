package malbec.fer.mapping;

import malbec.bloomberg.types.BBYellowKey;

/**
 * Represent all the indicative data we have about a futures security.
 * 
 */
public class FuturesSecurity {

    private String bloombergRoot;
    private BBYellowKey yellowKey;

    public FuturesSecurity(String bloombergRoot,BBYellowKey yellowKey) {
        super();
        this.bloombergRoot = bloombergRoot;
        this.yellowKey = yellowKey;
    }

    /**
     * @return the bloombergRoot
     */
    public String getBloombergRoot() {
        return bloombergRoot;
    }

    /**
     * @param bloombergRoot
     *            the bloombergRoot to set
     */
    public void setBloombergRoot(String bloombergRoot) {
        this.bloombergRoot = bloombergRoot;
    }

    /**
     * @return the yellowKey
     */
    public BBYellowKey getYellowKey() {
        return yellowKey;
    }

    /**
     * @param yellowKey
     *            the yellowKey to set
     */
    public void setYellowKey(BBYellowKey yellowKey) {
        this.yellowKey = yellowKey;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        
        sb.append("bloombergRoot=").append(bloombergRoot);
        sb.append(", yellowKey=").append(yellowKey);
        
        return sb.toString();
    }
}
