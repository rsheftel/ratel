package futures;

import static util.Dates.*;

import java.util.*;

import util.*;

public class ThirdWedMinusTwoLondonBizDaysExpiry extends Expiry {

	@Override public Date expiration(YearMonth ym) {
		return thirdWedLessTwo(ym, "lnb");
	}

}
