package com.fftw.bloomberg.rtf.messages;

import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import org.joda.time.LocalDate;

/**
 *
 */
public class DefaultRtfMessageTradeBody implements RtfMessageTradeBody {
    private char messageType;
    private String rawMessage;

    private LocalDate messageDate;

    private int messageSequenceNumber;

    private BBSecurityIDFlag securityIdFlag;
    private String securityId;

    private String account;
    private BBProductCode productCode;
    //private String bloombergId;
    private LocalDate tradeDate;

    private Integer level1TagId;
    private String level1TagName;
    private Integer level2TagId;
    private String level2TagName;
    private Integer level3TagId;
    private String level3TagName;
    private Integer level4TagId;
    private String level4TagName;
    private Integer level5TagId;
    private String level5TagName;
    private Integer level6TagId;
    private String level6TagName;

    private String cfd;

    public DefaultRtfMessageTradeBody(char messageType) {
        this.messageType = messageType;
    }

    public char getMessageType() {
        return messageType;
    }

    public boolean hasRawMessage() {
        return true;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public LocalDate getMessageDate() {
        return messageDate;
    }

    public int getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public BBSecurityIDFlag getSecurityIdFlag() {
        return securityIdFlag;
    }

    public String getSecurityId() {
        return securityId;
    }

    public String getAccount() {
        return account;
    }

    public BBProductCode getProductCode() {
        return productCode;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public int getLevel1TagId() {
        return level1TagId;
    }

    public String getLevel1TagName() {
        return level1TagName;
    }

    public int getLevel2TagId() {
        return level2TagId;
    }

    public String getLevel2TagName() {
        return level2TagName;
    }

    public int getLevel3TagId() {
        return level3TagId;
    }

    public String getLevel3TagName() {
        return level3TagName;
    }

    public int getLevel4TagId() {
        return level4TagId;
    }

    public String getLevel4TagName() {
        return level4TagName;
    }

    public int getLevel5TagId() {
        return level5TagId;
    }

    public String getLevel5TagName() {
        return level5TagName;
    }

    public int getLevel6TagId() {
        return level6TagId;
    }

    public String getLevel6TagName() {
        return level6TagName;
    }

    public String getCfd() {
        return cfd;
    }

    public String toString() {
        switch (messageType) {
            case '1':
                return "Trade Feed";
            case '2':
                return "New Security";
            case '3':
                return "Price Feed";
            case '4':
                return "Position Feed";
            case '5':
                return "End of Day Record";
            case '8':
                return "Counter-party Feed";
            default:
                return "Unknown message type: " + messageType;
        }
    }
}
