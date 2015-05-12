package futures;

import static util.Dates.*;
import static util.RunCalendar.*;

import java.util.*;

import util.*;

public class ThirdFridayNybModifiedFollowing extends Expiry {

    @Override public Date expiration(YearMonth ym) {
        Date d = thirdFriday(ym);
        if(NYB.isValid(d)) return d;
        return NYB.priorDay(d); 
    }

}
