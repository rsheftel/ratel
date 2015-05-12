package com.fftw.bloomberg.cmfp;

/**
 * Identifier for a Bloomberg CMFP session.
 * 
 * 
 */
public class CmfSessionID
{
    private String pricingNumber;

    String specVersion;

    String connectionHost;

    String connectionPort;

    String sessionID;

    public CmfSessionID (String pricingNumber, String specVersion, String host, String port)
    {
        this.pricingNumber = pricingNumber;
        this.specVersion = specVersion;
        this.connectionHost = host;
        this.connectionPort = port;
        this.sessionID = createID();
        
        if (!validId())
        {
            throw new IllegalArgumentException("All values must be set '"+ sessionID+"'");
        }
    }

    private boolean validId ()
    {
        return isSet(pricingNumber) && isSet(specVersion) && isSet(connectionHost) && isSet(connectionPort); 
    }

    private boolean isSet(String str) {
        return (str != null && str.trim().length() > 0);
    }
    /**
     * Create an ID from the supplied values.
     * 
     * This value is used all over and it is better to create it once than to
     * re-create it many times.
     * 
     * @return
     */
    private String createID ()
    {
        StringBuffer sb = new StringBuffer(128);
        sb.append(pricingNumber).append("-").append(specVersion);
        sb.append("-").append(connectionHost).append("-");
        sb.append(connectionPort);

        return sb.toString();
    }

    @Override
    public boolean equals (Object obj)
    {
        if (obj instanceof CmfSessionID)
        {
            return sessionID.equals(((CmfSessionID)obj).getSessionID());
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode ()
    {
        return sessionID.hashCode();
    }

    @Override
    public String toString ()
    {
        StringBuffer sb = new StringBuffer(128);
        sb.append("pricingNumber=").append(pricingNumber).append(", specVersion=");
        sb.append(specVersion).append(", host=").append(connectionHost);
        sb.append(", port=").append(connectionPort);

        return sb.toString();
    }

    public String getSessionID ()
    {
        return sessionID;
    }

    public String getPricingNumber ()
    {
        return pricingNumber;
    }

    public String getSpecVersion ()
    {
        return specVersion;
    }

    public String getConnectionHost ()
    {
        return connectionHost;
    }

    public String getConnectionPort ()
    {
        return connectionPort;
    }

}
