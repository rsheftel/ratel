package malbec.fer.mapping;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FuturesOutboundMapping")
class FuturesSymbolMap {

    @Id
    @Column(name = "Id")
    Long id;

    @Column(name = "PlatformId")
    String platform;
    
    @Column(name = "RicRoot")
    String ricRoot;
    
    @Column(name = "BloombergRoot")
    String bloombergRoot;

    @Column(name = "Multiplier")
    private BigDecimal priceMultiplier;

    
    public FuturesSymbolMap() {
        
    }
    
    public FuturesSymbolMap(String platform, String bloombergRoot, String ricRoot) {
        super();
        this.platform = platform.toUpperCase();
        this.ricRoot = ricRoot.toUpperCase();
        this.bloombergRoot = bloombergRoot.toUpperCase();
    }

    /**
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }

    public String getRicRoot() {
        return ricRoot;
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
        sb.append(", bloombergRoot=").append(bloombergRoot);
        sb.append(", ricRoot=").append(ricRoot);
        
        return sb.toString();
    }
    
    public double getPriceMultiplier() {
        return priceMultiplier.doubleValue();
    }
    
}
