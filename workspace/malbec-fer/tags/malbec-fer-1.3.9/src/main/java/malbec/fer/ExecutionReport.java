package malbec.fer;

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

/**
 * Feret execution reports.
 * 
 */
@Entity
@Table(name = "ExecutionReport")
public class ExecutionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "SenderCompId")
    private String senderCompId;
    
    @Column(name = "ExecutionId")
    private String executionId;
    
    /**
     * Execution Bust has this field set
     */
    @Column(name = "ExecutionReferenceId")
    private String executionReferenceId;
    
    @Column(name = "OrderId")
    private String orderId;
    
    @Column(name = "ClientOrderId")
    private String clientOrderId;
    
    
    @Column(name = "SenderSubId")
    private String senderSubId;
    
    @Column(name = "SendingTime")
    private Date sendingTime; // 20081107-18:51:26
    
    @Column(name = "ExecutionType")
    private String executionType;
    
    @Column(name = "OrderStatus")
    private String orderStatus;
    
    @Column(name = "Account")
    private String account;
    
    @Column(name = "Symbol")
    private String symbol;
    
    @Column(name = "Side")
    private String side;
    
    @Column(name = "OrderQuantity")
    private BigDecimal orderQuantity;
    
    @Column(name = "OrderType")
    private String orderType;
    
    @Column(name = "Price")
    private BigDecimal price;
    
    @Column(name = "StopPrice")
    private BigDecimal stopPrice;
    
    @Column(name = "Currency")
    private String currency;
    
    @Column(name = "TimeInForce")
    private String timeInForce;
    
    @Column(name = "LastQuantity")
    private BigDecimal lastQuantity;
    
    @Column(name = "LastPrice")
    private BigDecimal lastPrice;
    
    @Column(name = "LastMarket")
    private String lastMarket;
    
    @Column(name = "LastCapacity")
    private String lastCapacity;
    
    @Column(name = "LeavesQuantity")
    private BigDecimal leavesQuantity;
    
    @Column(name = "CumulatedQuantity")
    private BigDecimal cumulatedQuantity;
    
    @Column(name = "AveragePrice")
    private BigDecimal averagePrice;
    
    @Column(name = "TradeDate")
    private Date tradeDate;
    
    @Column(name = "TransactionTime")
    private Date transactionTime;
    
    @Column(name = "ReportToExchange")
    private String reportToExchange;
    
    @Column(name = "Product")
    private String product;

    @Column(name = "ListId")
    private String listId;

    // Our stuff
    @Column(updatable = false, insertable = false, name = "CreatedAt")
    @Generated(GenerationTime.INSERT)
    private Date createdAt;
    
    @Column(updatable = false, insertable = false, name = "UpdatedAt")
    @Generated(GenerationTime.ALWAYS)
    private Date updatedAt;
    
    public ExecutionReport() {
        
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the senderCompId
     */
    public String getSenderCompId() {
        return senderCompId;
    }

    /**
     * @param senderCompId the senderCompId to set
     */
    public void setSenderCompId(String senderCompId) {
        this.senderCompId = senderCompId;
    }
    
    /**
     * @return the executionId
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * @param executionId the executionId to set
     */
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    /**
     * @return the executionReferenceId
     */
    public String getExecutionReferenceId() {
        return executionReferenceId;
    }

    /**
     * @param executionId the executionReferenceId to set
     */
    public void setExecutionReferenceId(String executionReferenceId) {
        this.executionReferenceId = executionReferenceId;
    }

    /**
     * @return the orderId
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * @param orderId the orderId to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the clientOrderId
     */
    public String getClientOrderId() {
        return clientOrderId;
    }

    /**
     * @param clientOrderId the clientOrderId to set
     */
    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    /**
     * @return the senderSubId
     */
    public String getSenderSubId() {
        return senderSubId;
    }

    /**
     * @param senderSubId the senderSubId to set
     */
    public void setSenderSubId(String senderSubId) {
        this.senderSubId = senderSubId;
    }

    /**
     * @return the sendingTime
     */
    public Date getSendingTime() {
        return sendingTime == null ? null : new Date(sendingTime.getTime());
    }

    /**
     * @param sendingTime the sendingTime to set
     */
    public void setSendingTime(Date sendingTime) {
        this.sendingTime = sendingTime == null ? null : new Date(sendingTime.getTime());
    }

    /**
     * @return the executionType
     */
    public String getExecutionType() {
        return executionType;
    }

    /**
     * @param executionType the executionType to set
     */
    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    /**
     * @return the orderStatus
     */
    public String getOrderStatus() {
        return orderStatus;
    }

    /**
     * @param orderStatus the orderStatus to set
     */
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     * @return the account
     */
    public String getAccount() {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(String account) {
        this.account = account;
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
     * @return the side
     */
    public String getSide() {
        return side;
    }

    /**
     * @param side the side to set
     */
    public void setSide(String side) {
        this.side = side;
    }

    /**
     * @return the orderQuantity
     */
    public BigDecimal getOrderQuantity() {
        return orderQuantity;
    }

    /**
     * @param orderQuantity the orderQuantity to set
     */
    public void setOrderQuantity(BigDecimal orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    /**
     * @return the orderType
     */
    public String getOrderType() {
        return orderType;
    }

    /**
     * @param orderType the orderType to set
     */
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    /**
     * @return the price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * @return the stopPrice
     */
    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    /**
     * @param stopPrice the stopPrice to set
     */
    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    /**
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return the timeInForce
     */
    public String getTimeInForce() {
        return timeInForce;
    }

    /**
     * @param timeInForce the timeInForce to set
     */
    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    /**
     * @return the lastQuantity
     */
    public BigDecimal getLastQuantity() {
        return lastQuantity;
    }

    /**
     * @param lastQuantity the lastQuantity to set
     */
    public void setLastQuantity(BigDecimal lastQuantity) {
        this.lastQuantity = lastQuantity;
    }

    /**
     * @return the lastPrice
     */
    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    /**
     * @param lastPrice the lastPrice to set
     */
    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    /**
     * @return the lastMarket
     */
    public String getLastMarket() {
        return lastMarket;
    }

    /**
     * @param lastMarket the lastMarket to set
     */
    public void setLastMarket(String lastMarket) {
        this.lastMarket = lastMarket;
    }

    /**
     * @return the lastCapacity
     */
    public String getLastCapacity() {
        return lastCapacity;
    }

    /**
     * @param lastCapacity the lastCapacity to set
     */
    public void setLastCapacity(String lastCapacity) {
        this.lastCapacity = lastCapacity;
    }

    /**
     * @return the leavesQuantity
     */
    public BigDecimal getLeavesQuantity() {
        return leavesQuantity;
    }

    /**
     * @param leavesQuantity the leavesQuantity to set
     */
    public void setLeavesQuantity(BigDecimal leavesQuantity) {
        this.leavesQuantity = leavesQuantity;
    }

    /**
     * @return the cumulatedQuantity
     */
    public BigDecimal getCumulatedQuantity() {
        return cumulatedQuantity;
    }

    /**
     * @param cumulatedQuantity the cumulatedQuantity to set
     */
    public void setCumulatedQuantity(BigDecimal cumulatedQuantity) {
        this.cumulatedQuantity = cumulatedQuantity;
    }

    /**
     * @return the averagePrice
     */
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    /**
     * @param averagePrice the averagePrice to set
     */
    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    /**
     * @return the tradeDate
     */
    public Date getTradeDate() {
        return tradeDate == null ? null : new Date(tradeDate.getTime());
    }

    /**
     * @param tradeDate the tradeDate to set
     */
    public void setTradeDate(Date tradeDate) {
        this.tradeDate = tradeDate == null ? null : new Date(tradeDate.getTime());
    }

    /**
     * @return the transactionTime
     */
    public Date getTransactionTime() {
        return transactionTime == null ? null : new Date(transactionTime.getTime());
    }

    /**
     * @param transactionTime the transactionTime to set
     */
    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime == null ? null : new Date(transactionTime.getTime());
    }

    /**
     * @return the reportToExchange
     */
    public String getReportToExchange() {
        return reportToExchange;
    }

    /**
     * @param reportToExchange the reportToExchange to set
     */
    public void setReportToExchange(String reportToExchange) {
        this.reportToExchange = reportToExchange;
    }

    /**
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * @return the listId
     */
    public String getListId() {
        return listId;
    }

    /**
     * @param listId the listId to set
     */
    public void setListId(String listId) {
        this.listId = listId;
    }

    /**
     * @return the createdAt
     */
    public Date getCreatedAt() {
        return createdAt == null ? null : new Date(createdAt.getTime());
    }

    /**
     * @param createdAt the createdAt to set
     */
    void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the updatedAt
     */
    public Date getUpdatedAt() {
        return updatedAt == null ? null : new Date(updatedAt.getTime());
    }

    /**
     * @param updatedAt the updatedAt to set
     */
    void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        
        sb.append("ExecutionId=").append(executionId);
        sb.append(", OrderId=").append(orderId);
        sb.append(", SenderCompId").append(senderCompId);
        sb.append(", Side=").append(side);
        sb.append(", Symbol=").append(symbol);
        sb.append(", OrderQuantity=").append(orderQuantity);
        sb.append(", LastQuantity=").append(lastQuantity);
        sb.append(", LeavesQuantity=").append(leavesQuantity);
     
        return sb.toString();
    }
}
