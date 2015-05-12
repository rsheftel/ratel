package com.fftw.bloomberg.rtf.messages;

import org.testng.annotations.*;
import org.joda.time.LocalDate;
import com.fftw.bloomberg.types.BBFractionIndicator;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.util.Filter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * RtfOnlinePosition Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created February 22, 2008
 * @since 1.0
 */
public class RtfOnlinePositionTest {
    private static String str1 = " 1USD/ZAR1    0          00          0 0               0 0               0 0               0 0               0 0               0   0   0EMF     10USD/ZAR1B2080           20080222            518                                     EM.EEA_ZAR_FX      511                                                FX      510                                               ZAR      239                                          EM_CEMEA                                                                                                                      NNMSPB  ";

    @Test(groups =
            {
                    "unittest"
                    })
    public void testValueOf() {

        RtfOnlinePosition position = RtfOnlinePosition.valueOf(new LocalDate(), 1, str1);

        assert position != null : "Problem parsing source string";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testConvertValue() {
        BigDecimal bd = RtfOnlinePosition.convertValue(BBFractionIndicator.One, 4, 2999);
        assert BigDecimal.valueOf(299.9).equals(bd) : "FractionIndicator not converted propery";

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Two, 4, 2999);
        assert BigDecimal.valueOf(29.99).equals(bd) : "FractionIndicator not converted propery";

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Three, 4, 2999);
        assert BigDecimal.valueOf(2.999).equals(bd) : "FractionIndicator not converted propery";

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Four, 4, 2999);
        assert BigDecimal.valueOf(.2999).equals(bd) : "FractionIndicator not converted propery";

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Three, 11, 2999);
        assert BigDecimal.valueOf(2.999).equals(bd) : "FractionIndicator not converted propery";

    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testFilter() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");
        Map<String, String> filterFields = new HashMap<String, String>();

        filterFields.put("Account", "UnitTest");
        Filter<RtfOnlinePosition> batchFilter = RtfOnlinePosition.createFilter(filterFields);

        assert batchFilter.accept(bp) == true : "Case insensitive field names - wrong!";

        filterFields.clear();
        filterFields.put("account", "unittest");
        batchFilter = RtfOnlinePosition.createFilter(filterFields);

        assert batchFilter.accept(bp) == false : "Case insensitive value - wrong!";

        filterFields.clear();
        filterFields.put("account", "UnitTest");
        batchFilter = RtfOnlinePosition.createFilter(filterFields);

        assert batchFilter.accept(bp) == true : "Filter did not select position based on account=QMF";

    }


}
