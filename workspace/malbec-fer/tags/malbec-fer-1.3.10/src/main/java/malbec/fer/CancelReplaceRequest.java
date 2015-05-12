package malbec.fer;

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represent a Cancel/Replace Request
 * 
 * The only difference (currently) from a cancel is the FixMessageType (Discriminator)
 */
@Entity
@DiscriminatorValue("G")
public class CancelReplaceRequest extends CancelRequest {

    public CancelReplaceRequest() {
        
    }

    public CancelReplaceRequest(Map<String, String> map) {
        super(map);
    }
}
