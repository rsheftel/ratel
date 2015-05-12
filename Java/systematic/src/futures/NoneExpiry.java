package futures;

import static util.Errors.*;

import java.util.*;

import tsdb.*;
import util.*;

public class NoneExpiry extends Expiry {
    
	@Override public boolean isExpired(YearMonth yearMonth, Date d) {
		return false;
	}

	@Override public Date expiration(YearMonth yearMonth) {
		throw bomb("NoneExpiry can't produce an expiration date!");
	}

	@Override public void addTo(AttributeValues attributes, YearMonth yearMonth) {}
	
	
}