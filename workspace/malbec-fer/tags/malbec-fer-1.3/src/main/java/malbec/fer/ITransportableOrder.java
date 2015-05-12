package malbec.fer;

import java.util.List;

public interface ITransportableOrder {

    /**
     * Transport the order to its destination
     * @return
     */
    boolean transport();
    
    /**
     * The errors that are associated with this order.
     * @return
     */
    List<String> errors();
}
