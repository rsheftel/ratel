package malbec.fer;

import static malbec.fer.util.OrderValidation.*;
import static malbec.util.StringUtils.upperCaseOrNull;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.joda.time.LocalDate;

import malbec.util.MessageUtil;

@Entity
@DiscriminatorValue("F")
public class CancelRequest extends Order {

    private String originalClientOrderId;
    
    private String originalUserOrderId;

    public CancelRequest() {
        // empty
    }
    
    public CancelRequest(String userOrderId, String origUserOrderId) {
        setUserOrderId(userOrderId);
        setClientOrderId(generateClientOrderId(new LocalDate(), CancelRequest.class, userOrderId));
        setOriginalUserOrderId(origUserOrderId);
    }

    public CancelRequest(Map<String, String> crMap) {
        super(crMap);

        for (Map.Entry<String, String> entry : crMap.entrySet()) {
            String key = entry.getKey().toUpperCase();

            if ("OriginalClientOrderId".equalsIgnoreCase(key)) {
                originalClientOrderId = upperCaseOrNull(entry.getValue());
            }
            if ("OriginalUserOrderId".equalsIgnoreCase(key)) {
                originalUserOrderId = upperCaseOrNull(entry.getValue());
            }

        }
        
        // These need to be ClientOrderId
        if (getClientOrderId() != null) {
            String clientOrderId = getClientOrderId();
            char type = clientOrderId.charAt(9);
            if ('1' != type) {
                throw new IllegalArgumentException("ClientOrderId not generated correctly.  Wrong type '"+ type+"'.");
            }
        }
    }

    public Map<String, String> toMap() {
        Map<String, String> asMap = super.toMap();

        // TODO should ClientOrderId be part of the XML?
        if (originalClientOrderId != null) {
            MessageUtil.setOriginalClientOrderId(asMap, originalClientOrderId);
        }

        if (originalUserOrderId != null) {
            MessageUtil.setOriginalUserOrderId(asMap, originalUserOrderId);
        }
        
        return asMap;
    }
    
    /**
     * We assume that this is generated correctly, since it is for an existing order.
     * 
     * @param origClientOrderId
     */
    public void setOriginalClientOrderId(String origClientOrderId) {
        this.originalClientOrderId = origClientOrderId;
    }

    @Column(name = "OriginalClientOrderID", length = 16)
    public String getOriginalClientOrderId() {
        return originalClientOrderId;
    }

    public void setOriginalUserOrderId(String origUserOrderId) {
        this.originalUserOrderId = origUserOrderId;
    }

    @Column(name = "OriginalUserOrderID", length = 16)
    public String getOriginalUserOrderId() {
        return originalUserOrderId;
    }
    
    /* (non-Javadoc)
     * @see malbec.fer.Order#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(super.toString());
        sb.append(", OriginalClientOrderId=").append(originalClientOrderId);
        sb.append(", OriginalUserOrderId=").append(originalUserOrderId);
        
        return sb.toString();
    }

}
