package malbec.fix.message;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.joda.time.LocalDate;

/**
 * Represent a FIX Execution report of ExecType in 1,2 and F.
 * 
 * Handle both FIX 4.2 and 4.4 execution reports.
 * 
 * This is different from the ExecutionReport in that it uses the same conversion 
 * logic as the Feed Aggregator.  The fill is enriched with Bloomberg data. 
 * 
 */
@Entity
@Table(name = "FixFill")
public class FixFill {

    // JPA
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id")
    private Long id;

    @Column(name = "BeginString")
    private String beginString;

    @Column(name = "SenderCompId")
    private String senderCompId;

    @Column(name = "SenderSubId")
    private String senderSubId;

    @Column(name = "TargetCompId")
    private String targetCompId;
    
    @Column(name = "PossibleDuplicate")
    private char possibleDuplicate = 'N';

    @Column(name = "PossibleResend")
    private char possibleResend = 'N';

    @Column(name = "SendingTime")
    private Date sendingTime;
    
    // order
    @Column(name = "Account")
    private String account;
    
    @Column(name = "Strategy")
    private String strategy;

    @Column(name = "ClientOrderId")
    private String clientOrderId;

    @Column(name = "OrderId")
    private String orderId;

    @Column(name = "OrderQuantity")
    private BigDecimal orderQuantity;

    @Column(name = "OrderStatus")
    private char orderStatus;

    @Column(name = "OrderType")
    private char orderType;

    @Column(name = "Side")
    private char side;
    
    @Column(name = "PositionEffect")
    private Character positionEffect; // allow nulls in the DB

    @Column(name = "Symbol")
    private String symbol;

    @Column(name = "SecurityIdSource")
    private String securityIdSource;

    @Column(name = "SecurityId")
    private String securityId;
    
    @Column(name = "OriginalSecurityIdSource")
    private String originalSecurityIdSource;

    @Column(name = "OriginalSecurityId")
    private String originalSecurityId;
    
    @Column(name = "MaturityMonth")
    private String maturityMonth;

    @Column(name = "TimeInForce")
    private char timeInForce;

    @Column(name = "SecurityType")
    private String securityType = "EQUITY"; // not an official FIX value

    @Column(name = "BloombergProductCode")
    private String bloombergProductCode;
    
    @Column(name = "TradeDate")
    private Date tradeDate = new LocalDate().toDateMidnight().toDate();
    
    @Column(name = "StopPrice")
    private BigDecimal stopPrice;

    @Column(name = "ListId")
    private String listId;

    // fill
    @Column(name = "AveragePrice")
    private BigDecimal averagePrice;

    @Column(name = "PriceMultiplier")
    private BigDecimal priceMultiplier = BigDecimal.ONE;

    @Column(name = "CumulatedQuantity")
    private BigDecimal cumulatedQuantity;

    @Column(name = "ExecutionId")
    private String executionId;

    /**
     * Execution Bust has this field set
     */
    @Column(name = "ExecutionReferenceId")
    private String executionReferenceId;
    
    @Column(name = "ExecutionTransactionType")
    private char executionTransactionType;

    @Column(name = "LastPrice")
    private BigDecimal lastPrice;

    @Column(name = "LastShares")
    private BigDecimal lastShares;

    @Column(name = "Price")
    private BigDecimal price;

    @Column(name = "TransactionTime")
    private Date transactionTime;

    @Column(name = "ExecutingBroker")
    private String executingBroker;

    @Column(name = "ExecutionType")
    private char executionType;

    @Column(name = "LeavesQuantity")
    private BigDecimal leavesQuantity;

    @Column(name = "SecurityExchange")
    private String securityExchange;
    
    @Column(name = "LastMarket")
    private String lastMarket;
    
    @Column(name = "LastCapacity")
    private String lastCapacity;

    // tracking
    @Column(updatable = false, insertable = false, name = "CreatedAt")
    @Generated(GenerationTime.INSERT)
    private Date createdAt;

    @Column(updatable = false, insertable = false, name = "UpdatedAt")
    @Generated(GenerationTime.ALWAYS)
    private Date updatedAt;

    FixFill() {
    // For JPA and factory
    }

    public String getBeginString() {
        return beginString;
    }

    public String getSenderCompId() {
        return senderCompId;
    }

    public String getTargetCompId() {
        return targetCompId;
    }

    public String getAccount() {
        return account;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    /**
     * This is defined as a double in QFJ, FIX defines as quantity, used to be integer.
     * 
     * @return
     */
    public int getOrderQuantity() {
        return orderQuantity.intValue();
    }

    public char getOrderStatus() {
        return orderStatus;
    }

    public char getOrderType() {
        return orderType;
    }

    public char getSide() {
        return side;
    }

    public String getSymbol() {
        return symbol;
    }

    public char getTimeInForce() {
        return timeInForce;
    }

    public double getAveragePrice() {
        return averagePrice.doubleValue();
    }

    public int getCumulatedQuantity() {
        return cumulatedQuantity.intValue();
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getExecutionReferenceId() {
        return executionReferenceId;
    }
    
    public char getExecutionTransactionType() {
        return executionTransactionType;
    }

    public double getLastPrice() {
        return lastPrice.doubleValue();
    }

    public int getLastShares() {
        return lastShares.intValue();
    }

    public double getPrice() {
        return price.doubleValue();
    }

    public Date getTransactionTime() {
        return transactionTime == null ? null : new Date(transactionTime.getTime());
    }

    public String getExecutingBroker() {
        return executingBroker;
    }

    public char getExecutionType() {
        return executionType;
    }

    public int getLeavesQuantity() {
        return leavesQuantity.intValue();
    }

    public String getSecurityExchange() {
        return securityExchange;
    }

    public char getPossibleDuplicate() {
        return possibleDuplicate;
    }

    public char getPossibleResend() {
        return possibleResend;
    }

    public String getSecurityType() {
        return securityType;
    }

    public Date getTradeDate() {
        return tradeDate == null ? null : new Date(tradeDate.getTime());
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setExecutionReferenceId(String executionReferenceId) {
        this.executionReferenceId = executionReferenceId;
    }
    
    void setBeginString(String beginString) {
        this.beginString = beginString;
    }

    void setSenderCompId(String senderCompId) {
        this.senderCompId = senderCompId;
    }

    void setTargetCompId(String targetCompId) {
        this.targetCompId = targetCompId;
    }

    void setPossibleDuplicate(char possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    void setPossibleResend(char possibleResend) {
        this.possibleResend = possibleResend;
    }

    void setOrderQuantity(BigDecimal orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    void setOrderStatus(char orderStatus) {
        this.orderStatus = orderStatus;
    }

    void setSide(char side) {
        this.side = side;
    }

    void setSymbol(String symbol) {
        if (symbol != null) {
            this.symbol = symbol.toUpperCase();
        } else {
            this.symbol = null;
        }
    }

    void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    void setSecurityIdSource(String securityIdSource) {
        this.securityIdSource = securityIdSource;
    }

    void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    void setTradeDate(Date tradeDate) {
        this.tradeDate = tradeDate;
    }

    void setAccount(String account) {
        this.account = account;
    }

    void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    void setOrderType(char orderType) {
        this.orderType = orderType;
    }

    void setTimeInForce(char tif) {
        this.timeInForce = tif;
    }

    void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    void setCumulatedQuantity(BigDecimal cumulatedQuantity) {
        this.cumulatedQuantity = cumulatedQuantity;
    }

    void setExecutionTransactionType(char executionTransactionType) {
        this.executionTransactionType = executionTransactionType;
    }

    void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    void setLastShares(BigDecimal lastShares) {
        this.lastShares = lastShares;
    }

    void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    void setExecutionType(char executionType) {
        this.executionType = executionType;
    }

    void setLeavesQuantity(BigDecimal leavesQuantity) {
        this.leavesQuantity = leavesQuantity;
    }

    void setPrice(BigDecimal price) {
        this.price = price;
    }

    void setSecurityExchange(String securityExchange) {
        this.securityExchange = securityExchange;
    }

    void setExecutingBroker(String executingBroker) {
        this.executingBroker = executingBroker;
    }

    BigDecimal getLastPriceAsBigDecimal() {
        return lastPrice;
    }

    void setMaturityMonth(String maturityMonth) {
        this.maturityMonth = maturityMonth;
    }

    public String getSecurityId() {
        return securityId;
    }

    public String getListId() {
        return listId;
    }

    public void setPositionEffect(char positionEffect) {
        this.positionEffect = positionEffect;
    }

    public String getMaturityMonth() {
        return maturityMonth;
    }

    public void setBloombergProductCode(String bloombergProductCode) {
        this.bloombergProductCode = bloombergProductCode;
    }

    public String getSecurityIdSource() {
        return securityIdSource;
    }

    public boolean isPossibleDuplicate() {
        return possibleDuplicate == 'Y' || possibleResend == 'Y';
    }

    public boolean isOrderAck() {
        return orderStatus == '0' && executionTransactionType == '0' && executionType == '0'; 
    }
    /**
     * @return the positionEffect
     */
    public Character getPositionEffect() {
        return positionEffect;
    }

    /**
     * @return the bloombergProductCode
     */
    public String getBloombergProductCode() {
        return bloombergProductCode;
    }

    public long getId() {
        return id;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public void setSenderSubId(String senderSubId) {
        this.senderSubId = senderSubId;
    }

    public void setSendingTime(Date sendingTime) {
        this.sendingTime = sendingTime == null ? null : new Date(sendingTime.getTime());
    }

    public void setLastMarket(String lastMarket) {
        this.lastMarket = lastMarket;
    }

    public void setLastCapacity(String lastCapacity) {
        this.lastCapacity = lastCapacity;
    }

    /**
     * @return the senderSubId
     */
    public String getSenderSubId() {
        return senderSubId;
    }

    /**
     * @return the sendingTime
     */
    public Date getSendingTime() {
        return sendingTime == null ? null : new Date(sendingTime.getTime());
    }

    /**
     * @return the stopPrice
     */
    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    /**
     * @return the lastMarket
     */
    public String getLastMarket() {
        return lastMarket;
    }

    /**
     * @return the lastCapacity
     */
    public String getLastCapacity() {
        return lastCapacity;
    }

    public boolean isBust() {
        return executionReferenceId != null;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public void setPriceMultiplier(BigDecimal priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setOriginalSecurityId(String securityId) {
        originalSecurityId = securityId;
    }

    public void setOriginalSecurityIdSource(String securityIdSource) {
        originalSecurityIdSource = securityIdSource;
    }
    
    public String getOriginalSecurityId() {
        return originalSecurityId;
    }

    public String getOriginalSecurityIdSource() {
        return originalSecurityIdSource;
    }

    /**
     * @return the createdAt
     */
    public Date getCreatedAt() {
        if (createdAt != null) {
            return new Date(createdAt.getTime());
        }
        return null;
    }

    /**
     * @return the updatedAt
     */
    public Date getUpdatedAt() {
        if (updatedAt != null) {
            return new Date(updatedAt.getTime());
        }
        return null;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
    
    public String getStrategy() {
        return strategy;
    }
    
    public String toString() {
        // TODO implement this fully
        StringBuilder sb = new StringBuilder();
        
        sb.append("executionId=").append(executionId);
        sb.append("targetCompId=").append(targetCompId);
        sb.append("senderCompId=").append(senderCompId);
        sb.append("senderSubId=").append(senderSubId);
        sb.append("symbol=").append(symbol);
        
        return sb.toString();
    }
    /**
     * Based on the security type determine if the <code>FixFill</code> represents a equity
     * security.
     * 
     * @return
     */
    public boolean isEquity() {
        return ("EQUITY".equalsIgnoreCase(getSecurityType()) || "CS".equalsIgnoreCase(getSecurityType())); 
    }
}
