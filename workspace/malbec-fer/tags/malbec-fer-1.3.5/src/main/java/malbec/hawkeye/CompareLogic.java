package malbec.hawkeye;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompareLogic {

    /**
     * Compare the two list to ensure they contain the same <tt>UserOrderId</tt>s.
     * 
     * @param tomahawkOrders
     * @param ferretOrders
     * @return
     */
    public static Map<String, List<String>> compareOrders(Collection<String> tomahawkOrders, Collection<String> ferretOrders) {
        List<String> missingFromTomahawk = findMissingFromTomahawk(tomahawkOrders, ferretOrders);
        List<String> missingFromFerret = findMissingFromFerret(ferretOrders, tomahawkOrders);

        Map<String, List<String>> missingOrders = new HashMap<String, List<String>>();

        if (missingFromFerret.size() > 0) {
            missingOrders.put("FERRET", missingFromFerret);
        }

        if (missingFromTomahawk.size() > 0) {
            missingOrders.put("TOMAHAWK", missingFromTomahawk);
        }

        return missingOrders;
    }

    private static List<String> findMissingFromTomahawk(Collection<String> liveOrderIds,
        Collection<String> ferretOrderIds) {

        List<String> missingFromTomahawk = new ArrayList<String>();

        for (String id : ferretOrderIds) {
            if (!liveOrderIds.contains(id)) {
                missingFromTomahawk.add(id);
            }
        }

        return missingFromTomahawk;
    }

    private static List<String> findMissingFromFerret(Collection<String> orderIds, Collection<String> tomahawkOrderIds) {
        List<String> missingFromFerret = new ArrayList<String>();

        for (String id : tomahawkOrderIds) {
            if (!orderIds.contains(id)) {
                missingFromFerret.add(id);
            }
        }

        return missingFromFerret;
    }

}
