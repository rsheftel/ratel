package futures;

import static util.Dates.*;

import java.util.*;

import util.*;

public class FridayBeforeThirdWedExpiry extends Expiry {

	@Override public Date expiration(YearMonth ym) {
		return fridayBeforeThirdWed(ym, "lnb");
	}

}
