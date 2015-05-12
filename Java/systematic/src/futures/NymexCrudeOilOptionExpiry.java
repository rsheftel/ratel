package futures;

import static util.Dates.*;
import static util.RunCalendar.*;

import java.util.*;

import util.*;

public class NymexCrudeOilOptionExpiry extends Expiry {

    @Override public Date expiration(YearMonth ym) {
        Date d = monthsAgo(1, daysAhead(24, ym.first()));
        if(!NYB.isValid(d)) d = businessDaysAgo(1, d, "nyb");
        return businessDaysAgo(3, d, "nyb");
    }

}
