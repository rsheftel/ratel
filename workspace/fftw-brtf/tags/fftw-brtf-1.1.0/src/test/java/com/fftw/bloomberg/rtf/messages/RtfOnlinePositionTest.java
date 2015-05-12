package com.fftw.bloomberg.rtf.messages;

import com.fftw.bloomberg.types.BBFractionIndicator;
import com.fftw.util.Filter;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

    @Test(groups =
            {
                    "unittest"
                    })
    public void testAggretable() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");


        assert bp.aggregatable(other) : "Positions are aggregatble";
        assert other.aggregatable(bp) : "Positions are aggregatble";

        RtfOnlinePosition otherFail = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level9");
        assert !bp.aggregatable(otherFail) : "Positions are NOT aggregatble";
        assert !otherFail.aggregatable(bp) : "Positions are NOT aggregatble";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testAggrete() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");

        bp.setOpenPosition(new BigDecimal("78.907"));
        bp.setCurrentPosition(new BigDecimal("12.5"));
        bp.setTotalBuyVolume(new BigDecimal("6888.0"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);


        RtfOnlinePosition result = bp.aggregate(other);

        assert (result.getOpenPosition().compareTo(new BigDecimal("78.907")) == 0
                && result.getCurrentPosition().compareTo(new BigDecimal("12.5")) == 0
                && result.getRealizedPL().compareTo(new BigDecimal("67690")) == 0
                && result.getTotalBuyVolume().compareTo(new BigDecimal("6888")) == 0
                && result.getTotalSellVolume().compareTo(new BigDecimal("-6888")) == 0
                && result.getTotalNumberOfBuys() == 4
                && result.getTotalNumberOfSells() == 3
                && result.getCurrentAvgCost().compareTo(BigDecimal.ZERO) == 0
                && result.getOpenAverageCost().compareTo(BigDecimal.ZERO) == 0
        ) : "Failed to aggregate";

        result = other.aggregate(bp);
        assert (result.getOpenPosition().compareTo(new BigDecimal("78.907")) == 0
                && result.getCurrentPosition().compareTo(new BigDecimal("12.5")) == 0
                && result.getRealizedPL().compareTo(new BigDecimal("67690")) == 0
                && result.getTotalBuyVolume().compareTo(new BigDecimal("6888")) == 0
                && result.getTotalSellVolume().compareTo(new BigDecimal("-6888")) == 0
                && result.getTotalNumberOfBuys() == 4
                && result.getTotalNumberOfSells() == 3
                && result.getCurrentAvgCost().compareTo(BigDecimal.ZERO) == 0
                && result.getOpenAverageCost().compareTo(BigDecimal.ZERO) == 0
        ) : "Failed to aggregate";


        other.setOpenPosition(new BigDecimal("78.900"));
        other.setCurrentPosition(new BigDecimal("12.39"));
        other.setRealizedPL(new BigDecimal("67960"));
        other.setTotalBuyVolume(new BigDecimal("6887"));
        other.setTotalSellVolume(new BigDecimal("-6878"));


        other.setTotalNumberOfBuys(7);
        other.setTotalNumberOfSells(9);

        result = other.aggregate(bp);
        assert (result.getOpenPosition().compareTo(new BigDecimal("157.807")) == 0
                && result.getCurrentPosition().compareTo(new BigDecimal("24.89")) == 0
                && result.getRealizedPL().compareTo(new BigDecimal("135650")) == 0
                && result.getTotalBuyVolume().compareTo(new BigDecimal("13775")) == 0
                && result.getTotalSellVolume().compareTo(new BigDecimal("-13766")) == 0
                && result.getTotalNumberOfBuys() == 11
                && result.getTotalNumberOfSells() == 12
                && result.getCurrentAvgCost().compareTo(BigDecimal.ZERO) == 0
                && result.getOpenAverageCost().compareTo(BigDecimal.ZERO) == 0
        ) : "Failed to aggregate";

        // screw up the results
        result.setTotalBuyVolume(new BigDecimal("34"));
        assert !(result.getOpenPosition().compareTo(new BigDecimal("157.807")) == 0
                && result.getCurrentPosition().compareTo(new BigDecimal("24.89")) == 0
                && result.getRealizedPL().compareTo(new BigDecimal("135650")) == 0
                && result.getTotalBuyVolume().compareTo(new BigDecimal("13775")) == 0
                && result.getTotalSellVolume().compareTo(new BigDecimal("-13766")) == 0
                && result.getTotalNumberOfBuys() == 11
                && result.getTotalNumberOfSells() == 12
                && result.getCurrentAvgCost().compareTo(BigDecimal.ZERO) == 0
                && result.getOpenAverageCost().compareTo(BigDecimal.ZERO) == 0
        ) : "Aggregate compare failed";

        RtfOnlinePosition otherFail = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level9");
        assert !bp.aggregatable(otherFail) : "Positions are NOT aggregatble";
        assert !otherFail.aggregatable(bp) : "Positions are NOT aggregatble";
    }
}
