package malbec.fer;

import static malbec.util.StringUtils.upperCaseOrNull;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import malbec.util.DateTimeUtil;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Represent an order within the system.
 * 
 */
@Entity
@Table(name = "FixOrder")
public class Order {

    private Long id;

    /**
     * OrderID from the counter-party
     */
    private String orderID;

    /**
     * REDI only supports 16 characters for this (maybe others have the same restriction)
     */
    private String clientOrderID;
    private LocalDate orderDate;
    private String symbol;
    private String side;
    private String orderType;
    private BigDecimal quantity;
    private String timeInForce;
    private Date transactionTime;
    private BigDecimal limitPrice;
    private BigDecimal stopPrice;
    private String exchange;

    private String securityType;
    
    private String account;
    
    private String strategy;

    private String platform;

    private String clientHostname;
    private String clientUserID;
    private String clientAppName;
    
    private String status;
    private String replyTo;

    private String securityIDSource;
    private String message;
    
    private Date createdAt;
    private Date updatedAt;
    
    

    public Order() {
        transactionTime = new Date();
        orderDate = LocalDate.fromDateFields(transactionTime);
        timeInForce = "DAY";
        status = "NEW";

        try {
            clientHostname = upperCaseOrNull(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            clientHostname = "UnknownHost";
        }
    }

    public Order(String clientOrderID, String symbol, String securityType, String side, String orderType,
            BigDecimal quantity, String destination) {
        this();

        setClientOrderID(upperCaseOrNull(clientOrderID));
        this.symbol = symbol;
        this.securityType = securityType;
        this.side = side;
        this.orderType = orderType;
        this.quantity = quantity;
        this.platform = destination;
    }

    public Order(Map<String, String> fields) {
        this();
        for (Map.Entry<String, String> entry : fields.entrySet()) {

            String key = entry.getKey().toUpperCase();

            if ("TIMEINFORCE".equals(key)) {
                timeInForce = upperCaseOrNull(entry.getValue());
            } else if ("ORDERTYPE".equals(key)) {
                orderType = upperCaseOrNull(entry.getValue());
            } else if ("SIDE".equals(key)) {
                side = upperCaseOrNull(entry.getValue());
            } else if ("LIMITPRICE".equals(key)) {
                limitPrice = bigDecimalOrNull(entry.getValue());
            } else if ("STOPPRICE".equals(key)) {
                stopPrice = bigDecimalOrNull(entry.getValue());
            } else if ("CLIENTORDERID".equals(key)) {
                setClientOrderID(upperCaseOrNull(entry.getValue()));
            } else if ("QUANTITY".equals(key)) {
                quantity = bigDecimalOrNull(entry.getValue());
            } else if ("SYMBOL".equals(key)) {
                symbol = upperCaseOrNull(entry.getValue());
            } else if ("PLATFORM".equals(key)) {
                platform = upperCaseOrNull(entry.getValue());
            } else if ("EXCHANGE".equals(key)) {
                exchange = upperCaseOrNull(entry.getValue());
            } else if ("SECURITYTYPE".equals(key)) {
                securityType = upperCaseOrNull(entry.getValue());
            } else if ("SecurityIDSource".equalsIgnoreCase(key)) {
                securityIDSource = entry.getValue();
            } else if ("ReplyTo".equalsIgnoreCase(key)) {
                replyTo = entry.getValue();
            } else if ("TRANSACTIONTIME".equalsIgnoreCase(key)) {
                DateTime dt = DateTimeUtil.guessDateTime(entry.getValue());
                if (dt != null) {
                    transactionTime = dt.toDate(); 
                } 
            } else if ("ORDERDATE".equalsIgnoreCase(key)) {
                DateTime dt = DateTimeUtil.guessDateTime(entry.getValue());
                if (dt != null) {
                    orderDate = dt.toLocalDate(); 
                } 
            } else if ("CLIENTHOSTNAME".equalsIgnoreCase(key)) {
                clientHostname = entry.getValue();
            } else if ("CLIENTUSERID".equalsIgnoreCase(key)) {
                clientUserID = entry.getValue();
            } else if ("CLIENTAPPNAME".equalsIgnoreCase(key)) {
                clientAppName = entry.getValue();
            }  else if ("ACCOUNT".equalsIgnoreCase(key)) {
                account = entry.getValue();
            } else if ("STRATEGY".equalsIgnoreCase(key)) {
                strategy = entry.getValue();
            } else if ("STATUS".equalsIgnoreCase(key)) {
                status = entry.getValue();
            } else if ("MESSAGE".equalsIgnoreCase(key)) {
                message = entry.getValue();
            }


        }
    }

    public Map<String, String> toMap() {
        Map<String, String> fieldMap = new HashMap<String, String>();

        if (timeInForce != null) {
            fieldMap.put("TIMEINFORCE", timeInForce);
        }
        if (orderType != null) {
            fieldMap.put("ORDERTYPE", orderType);
        }
        if (side != null) {
            fieldMap.put("SIDE", side);
        }
        if (limitPrice != null) {
            fieldMap.put("LIMITPRICE", limitPrice.toPlainString());
        }
        if (stopPrice != null) {
            fieldMap.put("STOPPRICE", stopPrice.toPlainString());
        }
        if (clientOrderID != null) {
            fieldMap.put("CLIENTORDERID", clientOrderID);
        }
        if (quantity != null) {
            fieldMap.put("QUANTITY", quantity.toPlainString());
        }
        if (symbol != null) {
            fieldMap.put("SYMBOL", symbol);
        }
        if (platform != null) {
            fieldMap.put("PLATFORM", platform);
        }
        if (exchange != null) {
            fieldMap.put("EXCHANGE", exchange);
        }
        if (securityType != null) {
            fieldMap.put("SECURITYTYPE", securityType);
        }
        if (securityIDSource != null) {
            fieldMap.put("SecurityIDSource", securityIDSource);
        }
        if (replyTo != null) {
            fieldMap.put("ReplyTo", replyTo);
        }

        if (transactionTime != null) {
            fieldMap.put("TRANSACTIONTIME", DateTimeUtil.format(new DateTime(transactionTime)));
        }
        if (orderDate != null) {
            fieldMap.put("ORDERDATE", orderDate.toString());
        }
        if (clientHostname != null) {
            fieldMap.put("CLIENTHOSTNAME", clientHostname);
        }
        if (clientUserID != null) {
            fieldMap.put("CLIENTUSERID", clientUserID);
        }
        if (clientAppName != null) {
            fieldMap.put("CLIENTAPPNAME", clientAppName);
        }
        if (account != null) {
            fieldMap.put("ACCOUNT", account);
        }
        if (strategy != null) {
            fieldMap.put("STRATEGY", strategy);
        }
        if (status != null) {
            fieldMap.put("STATUS", status);
        }
        if (message != null) {
            fieldMap.put("MESSAGE", message);
        }


        return fieldMap;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    @Column(updatable = false, insertable = false, name = "CreatedAt")
    @Generated(GenerationTime.INSERT)
    Date getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Column(updatable = false, insertable = false, name = "UpdatedAt")
    @Generated(GenerationTime.ALWAYS)
    Date getUpdatedAt() {
        return updatedAt;
    }

    @Column(name = "OrderID")
    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    @Column(name = "ClientOrderID", length = 16)
    public String getClientOrderID() {
        return clientOrderID;
    }

    public void setClientOrderID(String clientOrderID) {
        if (clientOrderID != null) {
            clientOrderID = clientOrderID.replaceAll(" ", "");
            clientOrderID = clientOrderID.replaceAll("%", "");
            int maxLength = Math.min(16, clientOrderID.length());
            this.clientOrderID = clientOrderID.substring(0, maxLength);
        } else {
            this.clientOrderID = null;
        }
    }

    @Column(name = "OrderDate")
    public Date getOrderDate() {
        return orderDate.toDateTimeAtStartOfDay().toDate();
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = LocalDate.fromDateFields(orderDate);
    }

    @Column(name = "Symbol")
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Column(name = "Side")
    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    @Column(name = "OrderType")
    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    @Column(name = "Quantity")
    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Column(name = "TimeInForce")
    public String getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    @Column(name = "Platform")
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Column(name = "ClientHostname")
    public String getClientHostname() {
        return clientHostname;
    }

    public void setClientHostname(String clientHostname) {
        this.clientHostname = clientHostname;
    }

    @Column(name = "ClientUserID")
    public String getClientUserID() {
        return clientUserID;
    }

    public void setClientUserID(String clientUserID) {
        this.clientUserID = clientUserID;
    }

    @Column(name = "ClientAppName")
    public String getClientAppName() {
        return clientAppName;
    }

    public void setClientAppName(String clientAppName) {
        this.clientAppName = clientAppName;
    }

    /**
     * @return the replyTo
     */
    @Column(name = "ReplyTo")
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * @param replyTo the replyTo to set
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    @Column(name = "TransactionTime")
    public Date getTransactionTime() {
        return new Date(transactionTime.getTime());
    }

    public void setTransactionTime(Date transactionTime) {
        if (transactionTime != null) {
            this.transactionTime = new Date(transactionTime.getTime());
        } else {
            transactionTime = null;
        }
    }

    @Column(name = "LimitPrice")
    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }

    @Column(name = "StopPrice")
    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    @Column(name = "Status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "Exchange")
    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    /**
     * @return the securityIDSource
     */
    @Column(name = "SecurityIDSource", length = 5)
    public String getSecurityIDSource() {
        return securityIDSource;
    }

    /**
     * @param securityIDSource
     *                the securityIDSource to set
     */
    public void setSecurityIDSource(String securityIDSource) {
        this.securityIDSource = securityIDSource;
    }

    @Column(name = "SecurityType")
    public String getSecurityType() {
        return securityType;
    }

    /**
     * @return the account
     */
    @Column(name = "Account")
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
     * @return the strategy
     */
    @Column(name = "Strategy")
    public String getStrategy() {
        return strategy;
    }

    /**
     * @param strategy the strategy to set
     */
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public void setSecurityType(String securityType) {
        this.securityType = upperCaseOrNull(securityType);
    }

    void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return the message
     */
    @Column(name = "Message")
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("OrderID=").append(orderID);
        sb.append(", ClientOrderID=").append(clientOrderID);
        sb.append(", ClientHostname=").append(clientHostname);
        sb.append(", ClientUserID=").append(clientUserID);
        sb.append(", ClientAppName=").append(clientAppName);
        sb.append(", Symbol=").append(symbol);
        sb.append(", Side=").append(side);
        sb.append(", OrderType=").append(orderType);
        sb.append(", Quantity=").append(quantity);
        sb.append(", Platform=").append(platform);
        sb.append(", Strategy=").append(strategy);
        sb.append(", Account=").append(account);
        sb.append(", OrderDate=").append(orderDate);
        sb.append(", LimitPrice=").append(valueOrEmpty(limitPrice));
        sb.append(", StopPrice=").append(valueOrEmpty(stopPrice));
        sb.append(", Status=").append(status);

        return sb.toString();
    }

    private BigDecimal bigDecimalOrNull(String value) {
        if (value != null) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {}
        }
        return null;
    }

    private String valueOrEmpty(Object value) {
        if (value != null) {
            return value.toString();
        }
        return "";
    }

}
