package malbec.fer.mapping;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FuturesSymbolMapping")
class FuturesSymbol {

    @Id
    @Column(name = "ID")
    Long id;

    @Column(name = "PlatformId")
    String platform;
    
    @Column(name = "PlatformReceivingSymbolRoot")
    String platformReceivingRoot;
    
    @Column(name = "BloombergSymbolRoot")
    String bloombergRoot;

    @Column(name = "PlatformSendingSymbolRoot")
    String platformSendingRoot;

    @Column(name = "PriceMultiplier")
    private BigDecimal priceMultiplier;

    public FuturesSymbol() {
        // here for JPA
    }
    
    public FuturesSymbol(String platform, String platformReceivingRoot, String bloombergRoot, String platformSendingRoot, BigDecimal priceMultiplier) {
        this.platform = platform.toUpperCase();
        this.platformReceivingRoot = platformReceivingRoot.toUpperCase();
        this.platformSendingRoot = platformSendingRoot == null ? null : platformSendingRoot.toUpperCase();
        this.bloombergRoot = bloombergRoot.toUpperCase();
        this.priceMultiplier = priceMultiplier;
    }

    /**
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * @return the platformReceivingRoot
     */
    public String getPlatformReceivingRoot() {
        return platformReceivingRoot;
    }

    /**
     * @return the platformSendingRoot
     */
    public String getPlatformSendingRoot() {
        return platformSendingRoot;
    }

    /**
     * @return the bloombergRoot
     */
    public String getBloombergRoot() {
        return bloombergRoot;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("platform=").append(platform);
        sb.append(", platformReceivingRoot=").append(platformReceivingRoot);
        sb.append(", bloombergRoot=").append(bloombergRoot);
        sb.append(", platformSendingRoot=").append(platformSendingRoot);
        sb.append(", priceMultiplier=").append(priceMultiplier);
        
        return sb.toString();
    }
    
    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }
}
