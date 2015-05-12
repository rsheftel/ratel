package malbec.fer.mapping;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FUTURES_SYMBOL_MAPPING")
class FuturesSymbol {

    @Id
    @Column(name = "ID")
    Long id;

    @Column(name = "PLATFORM_ID")
    String platform;
    
    @Column(name = "PLATFORM_SYMBOL")
    String platformSymbol;
    
    @Column(name = "BLOOMBERG_SYMBOL")
    String bloombergSymbol;

    @Column(name = "MULTIPLIER")
    private BigDecimal priceMultiplier;
    
    @Column(name = "BLOOMBERG_SECTOR")
    private String bloombergProductCode;
    
    public FuturesSymbol() {
        
    }
    
    public FuturesSymbol(String platform, String platformSymbol, String bloombergSymbol) {
        super();
        this.platform = platform.toUpperCase();
        this.platformSymbol = platformSymbol.toUpperCase();
        this.bloombergSymbol = bloombergSymbol.toUpperCase();
    }

    /**
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * @return the platformSymbol
     */
    public String getPlatformSymbol() {
        return platformSymbol;
    }

    /**
     * @return the bloombergSymbol
     */
    public String getBloombergSymbol() {
        return bloombergSymbol;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("platform=").append(platform);
        sb.append(", platformSymbol=").append(platformSymbol);
        sb.append(", bloombergSymbol=").append(bloombergSymbol);
        
        return sb.toString();
    }
    
    public double getPriceMultiplier() {
        return priceMultiplier.doubleValue();
    }
    
    public String getBloombergProductCode() {
        return bloombergProductCode;
    }
}
