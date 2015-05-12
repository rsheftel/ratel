package malbec.fer.mapping;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import malbec.bloomberg.types.BBYellowKey;

@Entity
@Table(name = "MarketTickers")
class MarketTicker {

    @Id
    @Column(name = "Market")
    String market;
    
    @Column(name = "Bloomberg")
    String bloomberg;

    @Column(name = "YellowKey")
    BBYellowKey yellowKey;
    
    @Column(name = "TSDB")
    String tsdb;
    
    @Column(name = "Timestamp")
    Date updatedAt;
    
    public MarketTicker() {
        
    }

    public MarketTicker(String market, String bloomberg, String yellowKey, String tsdb) {
        this.market = market;
        this.bloomberg = bloomberg;
        this.yellowKey = BBYellowKey.valueFor(yellowKey);
        this.tsdb = tsdb;
    }

    /**
     * @return the market
     */
    public String getMarket() {
        return market;
    }

    /**
     * @param market the market to set
     */
    public void setMarket(String market) {
        this.market = market;
    }

    /**
     * @return the bloomberg
     */
    public String getBloomberg() {
        return bloomberg;
    }

    /**
     * @param bloomberg the bloomberg to set
     */
    public void setBloomberg(String bloomberg) {
        this.bloomberg = bloomberg;
    }

    public void setYellowKey(String yellowKey) {
        this.yellowKey = BBYellowKey.valueFor(yellowKey);
    }

    public void setYellowKey(BBYellowKey yellowKey) {
        this.yellowKey = yellowKey;
    }
    
    public BBYellowKey getYellowKey() {
        return yellowKey;
    }
    
    /**
     * @return the tsdb
     */
    public String getTsdb() {
        return tsdb;
    }

    /**
     * @param tsdb the tsdb to set
     */
    public void setTsdb(String tsdb) {
        this.tsdb = tsdb;
    }

    /**
     * @return the updatedAt
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

}
