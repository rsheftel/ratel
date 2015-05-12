package malbec.fer.mapping;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.LocalDate;

@Entity
@Table(name = "ShortSellList")
class ShortSellItem {

    @Id
    @Column(name = "Id")
    Long id;

    @Column(name = "Source")
    String source;
    
    @Column(name = "Symbol")
    String symbol;
    
    @Column(name = "Cusip")
    String cusip;

    @Column(name = "Description")
    private String description;
    
    @Column(name = "Quantity")
    private BigDecimal quantity;

    @Column(name = "Date")
    Date shareDate;
    
    public ShortSellItem() {
        
    }
    
    public ShortSellItem(String primeBroker, String ticker, BigDecimal quantity) {
        this.source = primeBroker;
        this.symbol = ticker;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("source=").append(source);
        sb.append(", symbol=").append(symbol);
        sb.append(", cusip=").append(cusip);
        sb.append(", description=").append(description);
        sb.append(", quantity=").append(quantity);
        sb.append(", shareDate=").append(new LocalDate(shareDate));
        
        return sb.toString();
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @return the cusip
     */
    public String getCusip() {
        return cusip;
    }

    /**
     * @param cusip the cusip to set
     */
    public void setCusip(String cusip) {
        this.cusip = cusip;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the date
     */
    public Date getShareDate() {
        return shareDate;
    }

    /**
     * @param date the date to set
     */
    public void setShareDate(Date date) {
        this.shareDate = date;
    }
}

