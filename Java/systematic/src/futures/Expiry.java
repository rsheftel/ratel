package futures;

import static tsdb.Attribute.*;
import static util.Errors.*;

import java.util.*;

import tsdb.*;
import util.*;

public abstract class Expiry {
	public static final String THIRD_WED_LESS_TWO = "ThirdWedMinusTwoLondonBizDays";
	public static final String THIRD_FRIDAY_NYB_MODIFIED_FOLLOWING = "ThirdFridayNybModifiedFollowing";
	public static final String FRIDAY_BEFORE_THIRD_WED = "FridayBeforeThirdWed";
	
	public boolean isExpired(YearMonth ym, Date d) {
		return d.after(expiration(ym));
	}
	public abstract Date expiration(YearMonth ym);
	public void addTo(AttributeValues attributes, YearMonth ym) {
		attributes.add(EXPIRY_DATE.value(expiration(ym)).createIfNeeded());
	}
	
	public static Expiry lookup(String expiry, Option option) {
		if (expiry.equals("Table")) return option.expiryLookupTable();
		return lookup(expiry);
	}
	static Expiry lookup(String expiry) {
		if (expiry.equals("None")) return new NoneExpiry();
		if (expiry.equals(THIRD_WED_LESS_TWO)) return new ThirdWedMinusTwoLondonBizDaysExpiry();
		if (expiry.equals(FRIDAY_BEFORE_THIRD_WED)) return new FridayBeforeThirdWedExpiry();
		if (expiry.equals(THIRD_FRIDAY_NYB_MODIFIED_FOLLOWING)) return new ThirdFridayNybModifiedFollowing();
		try {
            return (Expiry) Class.forName(expiry).getConstructor().newInstance();
        } catch (Exception e) {
            throw bomb("failed looking up expiry for " + expiry, e);
        }
	}
}