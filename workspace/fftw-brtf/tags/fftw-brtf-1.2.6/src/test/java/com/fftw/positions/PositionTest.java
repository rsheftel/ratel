package com.fftw.positions;

import static org.testng.Assert.*;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.types.BBFuturesCategory;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.BBSecurityType;
import com.fftw.util.AbstractBaseTest;

public class PositionTest extends AbstractBaseTest {

    private static final String EQUITY_LONG = "01459200101        2.00IBM       INTL BUSINESS MACHINES CORP    459200101                           2.0000USD     N.A.                                            82.7300                  TEST            N.A.                  84.86UNITED STATES                                                                                                                                                                              N.A.                                                                                                XNYSIBM US      US4592001014                                              S                                                                                    0.00                              N.A.                             0.00Common Stock                20081212Computers                                            N.A.                                                                                                      N.A.                          TEST                                                      459200101                               TEST_                                             TEST..                                            TEST.                                                                 US                                                                                                                                               2.00               0.00               2.00           2.00                                    MSPB                                Common Stock";
    private static final String EQUITY_SHORT = "01459200101        2.00IBM       INTL BUSINESS MACHINES CORP    459200101                          -2.0000USD     N.A.                                            82.7300                  TEST            N.A.                  84.86UNITED STATES                                                                                                                                                                              N.A.                                                                                                XNYSIBM US      US4592001014                                              S                                                                                    0.00                              N.A.                             0.00Common Stock                20081212Computers                                            N.A.                                                                                                      N.A.                          TEST                                                      459200101                               TEST_                                             TEST..                                            TEST.                                                                 US                                                                                                                                              -2.00              -2.00               0.00          -2.00                                    MSPB                                Common Stock";
    private static final String CURRENCY_SHORT = "01ADH9            10.00ADH9      A$ CURRENCY FUT   Mar09       ADH9                           -800000.0000USD                                                     65.8900                  QMF     Futures                       67.80UNITED STATES                                                                                           20090316                        Chicago Mercantile Exchange               100000.00                                                                                                                                                                              S                                                                                    0.00                                                               0.00Future                      20090113                                             CURRENCIES                                                                                                                                      QF.NDayBreak                                              ADH9                                    Breakout                                          General                                           QUANTYS                                                               US                                                                                                                                         -800000.00         -800000.00               0.00     -800000.00                                    GSFUT                               Currency future.";
    private static final String CURRENCY_LONG = "01ADH9            10.00ADH9      A$ CURRENCY FUT   Mar09       ADH9                            100000.0000USD                                                                              TEST    Futures                            UNITED STATES                                                                                           20090317                        Chicago Mercantile Exchange               100000.00                                                                                                                                                                              S                                                                                    0.00                                                               0.00Future                      20081210                                             CURRENCIES                                                                                                                                      TEST                                                      ADH9                                    TEST_                                             TEST..                                            TEST.                                                                 US                                                                                                                                          100000.00          100000.00               0.00      100000.00                                    GSFUT                               Currency future.";
    private static final String COMMODITY_LONG = "01TUH9             1.00TUH9      US 2YR NOTE (CBT) Mar09       TUH9                          16600000.0000USD                                                    109.2031                  TEST    Futures                      109.20UNITED STATES              20101215                                0.92                                 20090331                        Chicago Board of Trade                    200000.00                                                                                                                                                                              S                                                                                    0.00                                                               0.00Future                      20090113                                             BOND FUTURES                                                                                                                                    TEST                                                      TUH9                0.88S       20101231TEST_                                             TEST..                                            TEST.                                                                 US                                                                                                                                           16600.00            1000.00               0.00    16600000.00                                    GSFUT            2.12           1.94Financial commodity future.";
    private static final String COMMODITY_SHORT = "01USH9             1.00USH9      US LONG BOND(CBT) Mar09       USH9                          -1100000.0000USD                                                    135.0000                  TEST    Futures                      134.83UNITED STATES              20241115                                1.15                                 20090320                        Chicago Board of Trade                    100000.00                                                                                                                                                                              S                                                                                    0.00                                                               0.00Future                      20090113                                             BOND FUTURES                                                                                                                                    TEST                                                      USH9                7.50S       20241115TEST_                                             TEST..                                            TEST.                                                                 US                                                                                                                                           -1100.00           -1100.00               0.00    -1100000.00                                    GSFUT            9.24          10.62Financial commodity future.";
    private static final String CURRENCY_IR_LONG = "01ESH9             1.00ESH9      EURO S  3MO LIFFE Mar09       ESH9                           1000000.0000CHF                                                                              TEST    Futures                            BRITAIN                                                                                                 20090317                        LIFFE                                    1000000.00                                                                                                                                                                              S                                                                                    0.00                                                               0.00Future                      20091209                                             INTEREST RATE                                                                                                                                   TEST                                                      ESH9                                    TEST_                                             TEST..                                            TEST.                                                                 GB                                                                                                                                            1000.00            1000.00               0.00     1000000.00                                    GSFUT                               Financial commodity future.";
    private static final String INDEX_LONG = "01ESH9             9.00ESH9      S&P500 EMINI FUT  Mar09       ESH9                                 5.0000USD                                                    891.0000                  TEST    Futures                      889.50UNITED STATES                                                                                           20090317                        Chicago Mercantile Exchange                   50.00                                                                                                                                                                              S                                                                                    0.00                                                               0.00Future                      20081209                                             STOCK INDICES                                                                                                                                   TEST                                                      ESH9                                    TEST_                                             TEST..                                            TEST.                                                                 US                                                                                                                                               5.00               5.00               0.00           5.00                                    GSFUT                               Physical index future.";

    
    private static final String EQUITY_LONG_LONG = "messageDate=2009-03-11, messageSequenceNumber=0, securityIdFlag=Cusip, securityId=459200101, currentAvgCost=9.078652, openAvgCost=9.3199997, realizedPL=0, currentPosition=10774, totalBuyVolume=10772, totalSellVolume=0, openPosition=2, totalNumberOfBuys=1, totalNumberOfSells=0, account=TEST, productCode=Equity, bloombergId=459200101, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=728, level1TagName=TEST, level2TagId=727, level2TagName=TEST_, level3TagId=726, level3TagName=TEST.., level4TagId=725, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=MSPB";
    private static final String EQUITY_LONG_SHORT = "messageDate=2009-03-11, messageSequenceNumber=0, securityIdFlag=Cusip, securityId=459200101, currentAvgCost=9.078652, openAvgCost=9.3199997, realizedPL=0, currentPosition=-200, totalBuyVolume=0, totalSellVolume=202, openPosition=2, totalNumberOfBuys=0, totalNumberOfSells=1, account=TEST, productCode=Equity, bloombergId=459200101, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=728, level1TagName=TEST, level2TagId=727, level2TagName=TEST_, level3TagId=726, level3TagName=TEST.., level4TagId=725, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=Y, cfd=N, primeBroker=MSPB";
    private static final String EQUITY_SHORT_LONG = "messageDate=2009-03-11, messageSequenceNumber=0, securityIdFlag=Cusip, securityId=459200101, currentAvgCost=9.078652, openAvgCost=9.3199997, realizedPL=0, currentPosition=10774, totalBuyVolume=10772, totalSellVolume=0, openPosition=-2, totalNumberOfBuys=1, totalNumberOfSells=0, account=TEST, productCode=Equity, bloombergId=459200101, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=728, level1TagName=TEST, level2TagId=727, level2TagName=TEST_, level3TagId=726, level3TagName=TEST.., level4TagId=725, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=MSPB";
    private static final String EQUITY_SHORT_SHORT = "messageDate=2009-03-11, messageSequenceNumber=0, securityIdFlag=Cusip, securityId=459200101, currentAvgCost=9.078652, openAvgCost=9.3199997, realizedPL=0, currentPosition=-204, totalBuyVolume=0, totalSellVolume=202, openPosition=-2, totalNumberOfBuys=0, totalNumberOfSells=1, account=TEST, productCode=Equity, bloombergId=459200101, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=728, level1TagName=TEST, level2TagId=727, level2TagName=TEST_, level3TagId=726, level3TagName=TEST.., level4TagId=725, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=Y, cfd=N, primeBroker=MSPB";

    private static final String CURRENCY_LONG_SHORT = "messageDate=2009-04-20, messageSequenceNumber=1, securityIdFlag=Cusip, securityId=ADH9, exchangeTicker=, currentAvgCost=63.835, openAvgCost=0, realizedPL=0, currentPosition=-300000, totalBuyVolume=0, totalSellVolume=400000, openPosition=1, totalNumberOfBuys=0, totalNumberOfSells=1, account=TEST, productCode=Currency, bloombergId=ADH9    M, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=852, level1TagName=TEST, level2TagId=789, level2TagName=TEST_, level3TagId=46, level3TagName=TEST.., level4TagId=5, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=Y, cfd=N, primeBroker=GSFUT";
    private static final String CURRENCY_LONG_LONG = "messageDate=2009-04-20, messageSequenceNumber=1, securityIdFlag=Cusip, securityId=ADH9, exchangeTicker=, currentAvgCost=63.835, openAvgCost=0, realizedPL=0, currentPosition=300000, totalBuyVolume=200000, totalSellVolume=0, openPosition=1, totalNumberOfBuys=1, totalNumberOfSells=0, account=TEST, productCode=Currency, bloombergId=ADH9    M, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=852, level1TagName=TEST, level2TagId=789, level2TagName=TEST_, level3TagId=46, level3TagName=TEST.., level4TagId=5, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=GSFUT";
    private static final String CURRENCY_SHORT_SHORT = "messageDate=2009-04-20, messageSequenceNumber=1, securityIdFlag=Cusip, securityId=ADH9, exchangeTicker=, currentAvgCost=63.835, openAvgCost=0, realizedPL=0, currentPosition=-900000, totalBuyVolume=0, totalSellVolume=100000, openPosition=-8, totalNumberOfBuys=0, totalNumberOfSells=1, account=QMF, productCode=Currency, bloombergId=ADH9    M, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=852, level1TagName=QF.NDayBreak, level2TagId=789, level2TagName=Breakout, level3TagId=46, level3TagName=General, level4TagId=5, level4TagName=QUANTYS, level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=Y, cfd=N, primeBroker=GSFUT";
    private static final String CURRENCY_SHORT_LONG = "messageDate=2009-04-20, messageSequenceNumber=1, securityIdFlag=Cusip, securityId=ADH9, exchangeTicker=, currentAvgCost=63.835, openAvgCost=0, realizedPL=0, currentPosition=300000, totalBuyVolume=1100000, totalSellVolume=0, openPosition=-8, totalNumberOfBuys=1, totalNumberOfSells=0, account=QMF, productCode=Currency, bloombergId=ADH9    M, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=852, level1TagName=QF.NDayBreak, level2TagId=789, level2TagName=Breakout, level3TagId=46, level3TagName=General, level4TagId=5, level4TagName=QUANTYS, level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=GSFUT";

    private static final String COMMODITY_SHORT_SHORT = "messageDate=2009-01-14, messageSequenceNumber=14, securityIdFlag=Cusip, securityId=USH9, currentAvgCost=134.8281, openAvgCost=134.8281, realizedPL=-22812.75, currentPosition=-12, totalBuyVolume=0, totalSellVolume=100000, openPosition=-11, totalNumberOfBuys=0, totalNumberOfSells=1, account=TEST, productCode=Commodity, bloombergId=USH9, strikePrice=0, tradeDate=2009-01-14, programType=, daysToSettle=, level1TagId=51, level1TagName=TEST, level2TagId=50, level2TagName=TEST_, level3TagId=46, level3TagName=TEST.., level4TagId=5, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=Y, cfd=N, primeBroker=GSFUT";

    private static final String INDEX_LONG_SHORT = "messageDate=2009-04-21, messageSequenceNumber=1, securityIdFlag=Cusip, securityId=ESH9, exchangeTicker=, currentAvgCost=717.25, openAvgCost=0, realizedPL=0, currentPosition=-3, totalBuyVolume=0, totalSellVolume=8, openPosition=5, totalNumberOfBuys=0, totalNumberOfSells=1, account=TEST, productCode=Index, bloombergId=ESH9    M, strikePrice=0, tradeDate=2009-03-02, programType=, daysToSettle=, level1TagId=788, level1TagName=TEST, level2TagId=789, level2TagName=TEST_, level3TagId=46, level3TagName=TEST.., level4TagId=5, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=GSFUT";

    private static final String CURRENCY_IR_BLAH = "messageDate=2008-12-01, messageSequenceNumber=1439, securityIdFlag=Cusip, securityId=ESH9, currentAvgCost=868.75, openAvgCost=895.25, realizedPL=66887.5, currentPosition=-73, totalBuyVolume=27, totalSellVolume=120, openPosition=20, totalNumberOfBuys=1, totalNumberOfSells=28, account=TEST, productCode=Index, bloombergId=ESH9    M, strikePrice=0, tradeDate=2008-12-01, programType=, daysToSettle=, level1TagId=796, level1TagName=TEST, level2TagId=795, level2TagName=TEST_, level3TagId=794, level3TagName=TEST.., level4TagId=5, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=GSFUT";

    
    private static final String EQUITY_ONLINE_1 = "messageDate=2009-03-11, messageSequenceNumber=0, securityIdFlag=Cusip, securityId=459200101, currentAvgCost=9.078652, openAvgCost=9.3199997, realizedPL=0, currentPosition=10774, totalBuyVolume=10774, totalSellVolume=0, openPosition=0, totalNumberOfBuys=1, totalNumberOfSells=0, account=TEST, productCode=Equity, bloombergId=459200101, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=728, level1TagName=TEST, level2TagId=727, level2TagName=TEST_, level3TagId=726, level3TagName=TEST.., level4TagId=725, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=MSPB";
    private static final String EQUITY_ONLINE_2 = "messageDate=2009-03-11, messageSequenceNumber=0, securityIdFlag=Cusip, securityId=459200101, currentAvgCost=9.078652, openAvgCost=9.3199997, realizedPL=0, currentPosition=10776, totalBuyVolume=10776, totalSellVolume=0, openPosition=0, totalNumberOfBuys=2, totalNumberOfSells=0, account=TEST, productCode=Equity, bloombergId=459200101, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=728, level1TagName=TEST, level2TagId=727, level2TagName=TEST_, level3TagId=726, level3TagName=TEST.., level4TagId=725, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=MSPB";
    
    private static final String FUTURE_ONLINE_1 = "messageDate=2009-03-11, messageSequenceNumber=24, securityIdFlag=Cusip, securityId=BPM9, currentAvgCost=137.24, openAvgCost=0, realizedPL=0, currentPosition=437500, totalBuyVolume=437500, totalSellVolume=0, openPosition=0, totalNumberOfBuys=1, totalNumberOfSells=0, account=TEST, productCode=Currency, bloombergId=BPM9    M, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=852, level1TagName=TEST, level2TagId=789, level2TagName=TEST_, level3TagId=46, level3TagName=TEST.., level4TagId=5, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=GSFUT";
    private static final String FUTURE_ONLINE_2 = "messageDate=2009-03-11, messageSequenceNumber=26, securityIdFlag=Cusip, securityId=BPM9, currentAvgCost=137.24, openAvgCost=0, realizedPL=0, currentPosition=375000, totalBuyVolume=437500, totalSellVolume=62500, openPosition=0, totalNumberOfBuys=1, totalNumberOfSells=1, account=TEST, productCode=Currency, bloombergId=BPM9    M, strikePrice=0, tradeDate=2009-03-11, programType=, daysToSettle=, level1TagId=852, level1TagName=TEST, level2TagId=789, level2TagName=TEST_, level3TagId=46, level3TagName=TEST.., level4TagId=5, level4TagName=TEST., level5TagId=, level5TagName=, level6TagId=, level6TagName=, short=N, cfd=N, primeBroker=GSFUT";
        
    @Test(groups = { "unittest" })
    public void testCreateEquityPositionLong() {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), EQUITY_LONG);
        // These are not populated logically
        assertEqualsBD(bp.getCurrentLongPosition(), 0);
        assertEqualsBD(bp.getCurrentShortPosition(), 2);
        assertEqualsBD(bp.getPrimaryTraderPosition(), 2);
        assertEqualsBD(bp.getFullCurrentNetPosition(), 2);
        assertEqualsBD(bp.getFullCurrentNetPositionWithoutComma(), 2);

        ISecurity security = bp.getSecurity();

        assertNotNull(security);
        assertEquals(security.getName(), "INTL BUSINESS MACHINES CORP");
        assertEquals(security.getSecurityId(), "459200101");
        assertEquals(security.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(security.getProductCode(), BBProductCode.Equity);
        assertEquals(security.getTicker(), "IBM");
        assertEquals(security.getSecurityType2(), BBSecurityType.CommonStock);

        Position position = bp.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof Equity);
        assertEqualsBD(position.getOpenPosition(), 2); // the position for start of day
        assertEqualsBD(position.getCurrentPosition(), 2);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 0);
        assertEquals(position.getIntradayPosition(), 0);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "MSPB");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertEquals(positionSecurity.getName(), "INTL BUSINESS MACHINES CORP");
        assertEquals(positionSecurity.getSecurityId(), "459200101");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Equity);
        assertEquals(positionSecurity.getTicker(), "IBM");
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.CommonStock);
    }

    @Test(groups = { "unittest" })
    public void testCreateEquityPositionShort() {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), EQUITY_SHORT);
        // These are not populated logically
        assertEqualsBD(bp.getCurrentLongPosition(), -2);
        assertEqualsBD(bp.getCurrentShortPosition(), 0);
        assertEqualsBD(bp.getPrimaryTraderPosition(), -2);
        assertEqualsBD(bp.getFullCurrentNetPosition(), -2);
        assertEqualsBD(bp.getFullCurrentNetPositionWithoutComma(), -2);

        ISecurity security = bp.getSecurity();

        assertNotNull(security);
        assertEquals(security.getName(), "INTL BUSINESS MACHINES CORP");
        assertEquals(security.getSecurityId(), "459200101");
        assertEquals(security.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(security.getProductCode(), BBProductCode.Equity);
        assertEquals(security.getTicker(), "IBM");
        assertEquals(security.getSecurityType2(), BBSecurityType.CommonStock);

        Position position = bp.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof Equity);
        assertEqualsBD(position.getOpenPosition(), -2); // the position for start of day
        assertEqualsBD(position.getCurrentPosition(), -2);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 0);
        assertEquals(position.getIntradayPosition(), 0);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "MSPB");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertEquals(positionSecurity.getName(), "INTL BUSINESS MACHINES CORP");
        assertEquals(positionSecurity.getSecurityId(), "459200101");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Equity);
        assertEquals(positionSecurity.getTicker(), "IBM");
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.CommonStock);
    }

    @Test(groups = { "unittest" })
    public void testAddEquityOnlinePositionLongLong() throws Exception {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), EQUITY_LONG);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(EQUITY_LONG_LONG);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);
        // assertEquals(newBatchPosition.getNumberOfBuys(), 279);
        // assertEquals(newBatchPosition.getNumberOfSells(), 0);
        // assertEquals(newBatchPosition.getFullCurrentNetPosition().intValue(), 10774);
        // assertEquals(newBatchPosition.getFullCurrentNetPositionWithoutComma().intValue(), 10774);
        // assertEquals(newBatchPosition.getPrimaryTraderPosition().intValue(), 10774);
        // assertEquals(newBatchPosition.getCurrentLongPosition().intValue(), 10774);
        // assertEquals(newBatchPosition.getCurrentShortPosition().intValue(), 0);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), 2); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), 10774);
        assertEquals(updatedPosition.getSharesSold(), 0);
        assertEquals(updatedPosition.getSharesBought(), 10772);
        assertEquals(updatedPosition.getIntradayPosition(), 10772);
        assertEquals(updatedPosition.getPositionType(), PositionType.Long);
    }

    @Test(groups = { "unittest" })
    public void testAddEquityOnlinePositionLongShort() throws Exception {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), EQUITY_LONG);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(EQUITY_LONG_SHORT);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), 2); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), -200);
        assertEquals(updatedPosition.getSharesSold(), 202);
        assertEquals(updatedPosition.getSharesBought(), 0);
        assertEquals(updatedPosition.getIntradayPosition(), -202);
        assertEquals(updatedPosition.getPositionType(), PositionType.Short);
    }

    @Test(groups = { "unittest" })
    public void testAddEquityOnlinePositionShortLong() throws Exception {

        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), EQUITY_SHORT);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(EQUITY_SHORT_LONG);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), -2); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), 10774);
        assertEquals(updatedPosition.getSharesSold(), 0);
        assertEquals(updatedPosition.getSharesBought(), 10772);
        assertEquals(updatedPosition.getIntradayPosition(), 10772);
        assertEquals(updatedPosition.getPositionType(), PositionType.Long);
    }

    @Test(groups = { "unittest" })
    public void testAddEquityOnlinePositionShortShort() throws Exception {

        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), EQUITY_SHORT);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(EQUITY_SHORT_SHORT);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), -2); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), -204);
        assertEquals(updatedPosition.getSharesSold(), 202);
        assertEquals(updatedPosition.getSharesBought(), 0);
        assertEquals(updatedPosition.getIntradayPosition(), -202);
        assertEquals(updatedPosition.getPositionType(), PositionType.Short);
    }

    @Test(groups = { "unittest" })
    public void testCreateCurrencyFuturesPositionShort() {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), CURRENCY_SHORT);
        // These are not populated logically
        assertEqualsBD(bp.getCurrentLongPosition(), -800000);
        assertEqualsBD(bp.getCurrentShortPosition(), 0);
        assertEqualsBD(bp.getPrimaryTraderPosition(), -800000);
        assertEqualsBD(bp.getFullCurrentNetPosition(), -800000);
        assertEqualsBD(bp.getFullCurrentNetPositionWithoutComma(), -800000);

        ISecurity security = bp.getSecurity();

        assertNotNull(security);
        assertTrue(security instanceof CurrencyFutures);
        assertEquals(security.getName(), "A$ CURRENCY FUT   Mar09");
        assertEquals(security.getSecurityId(), "ADH9");
        assertEquals(security.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(security.getProductCode(), BBProductCode.Currency);
        assertEquals(security.getTicker(), "ADH9");
        assertEquals(security.getSecurityType2(), BBSecurityType.Futures);

        Position position = bp.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof CurrencyFutures);
        assertEqualsBD(position.getOpenPosition(), -8); // the position for start of day
        assertEqualsBD(position.getCurrentPosition(), -8);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 0);
        assertEquals(position.getIntradayPosition(), 0);
        assertEquals(position.getAccount(), "QMF");
        assertEquals(position.getLevel1TagName(), "QF.NDayBreak");
        assertEquals(position.getLevel2TagName(), "Breakout");
        assertEquals(position.getLevel3TagName(), "General");
        assertEquals(position.getLevel4TagName(), "QUANTYS");
        assertEquals(position.getPrimeBroker(), "GSFUT");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertEquals(positionSecurity.getName(), "A$ CURRENCY FUT   Mar09");
        assertEquals(positionSecurity.getSecurityId(), "ADH9");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Currency);
        assertEquals(positionSecurity.getTicker(), "ADH9");
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.Futures);
        assertEquals(((IFuturesSecurity) positionSecurity).getFuturesCategory(),
            BBFuturesCategory.Currency);
    }

    @Test(groups = { "unittest" })
    public void testCreateCurrencyFuturesPositionLong() {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), CURRENCY_LONG);
        // These are not populated logically
        assertEqualsBD(bp.getCurrentLongPosition(), 100000);
        assertEqualsBD(bp.getCurrentShortPosition(), 0);
        assertEqualsBD(bp.getPrimaryTraderPosition(), 100000);
        assertEqualsBD(bp.getFullCurrentNetPosition(), 100000);
        assertEqualsBD(bp.getFullCurrentNetPositionWithoutComma(), 100000);

        ISecurity security = bp.getSecurity();

        assertNotNull(security);
        assertTrue(security instanceof CurrencyFutures);
        assertEquals(security.getName(), "A$ CURRENCY FUT   Mar09");
        assertEquals(security.getSecurityId(), "ADH9");
        assertEquals(security.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(security.getProductCode(), BBProductCode.Currency);
        assertEquals(security.getTicker(), "ADH9");
        assertEquals(security.getSecurityType2(), BBSecurityType.Futures);

        Position position = bp.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof CurrencyFutures);
        assertEqualsBD(position.getOpenPosition(), 1); // the position for start of day
        assertEqualsBD(position.getCurrentPosition(), 1);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 0);
        assertEquals(position.getIntradayPosition(), 0);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "GSFUT");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertEquals(positionSecurity.getName(), "A$ CURRENCY FUT   Mar09");
        assertEquals(positionSecurity.getSecurityId(), "ADH9");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Currency);
        assertEquals(positionSecurity.getTicker(), "ADH9");
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.Futures);
        assertEquals(((IFuturesSecurity) positionSecurity).getFuturesCategory(),
            BBFuturesCategory.Currency);
    }

    @Test(groups = { "unittest" })
    public void testAddCurrencyFuturesOnlinePositionLongShort() throws Exception {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), CURRENCY_LONG);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(CURRENCY_LONG_SHORT);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());
        assertEqualsBD(op.getOpenPosition(), bp.getOnlineOpenPosition());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), 1); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), -3);
        assertEquals(updatedPosition.getSharesSold(), 4);
        assertEquals(updatedPosition.getSharesBought(), 0);
        assertEquals(updatedPosition.getIntradayPosition(), -4);
        assertEquals(updatedPosition.getPositionType(), PositionType.Short);
    }

    @Test(groups = { "unittest" })
    public void testAddCurrencyFuturesOnlinePositionLongLong() throws Exception {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), CURRENCY_LONG);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(CURRENCY_LONG_LONG);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());
        assertEqualsBD(op.getOpenPosition(), bp.getOnlineOpenPosition());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), 1); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), 3);
        assertEquals(updatedPosition.getSharesSold(), 0);
        assertEquals(updatedPosition.getSharesBought(), 2);
        assertEquals(updatedPosition.getIntradayPosition(), 2);
        assertEquals(updatedPosition.getPositionType(), PositionType.Long);
    }

    @Test(groups = { "unittest" })
    public void testAddCurrencyFuturesOnlinePositionShortLong() throws Exception {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), CURRENCY_SHORT);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(CURRENCY_SHORT_LONG);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());
        assertEqualsBD(op.getOpenPosition(), bp.getOnlineOpenPosition());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), -8); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), 3);
        assertEquals(updatedPosition.getSharesSold(), 0);
        assertEquals(updatedPosition.getSharesBought(), 11);
        assertEquals(updatedPosition.getIntradayPosition(), 11);
        assertEquals(updatedPosition.getPositionType(), PositionType.Long);
    }

    @Test(groups = { "unittest" })
    public void testAddCurrencyFuturesOnlinePositionShortShort() throws Exception {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), CURRENCY_SHORT);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(CURRENCY_SHORT_SHORT);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());
        assertEqualsBD(op.getOpenPosition(), bp.getOnlineOpenPosition());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), -8); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), -9);
        assertEquals(updatedPosition.getSharesSold(), 1);
        assertEquals(updatedPosition.getSharesBought(), 0);
        assertEquals(updatedPosition.getIntradayPosition(), -1);
        assertEquals(updatedPosition.getPositionType(), PositionType.Short);
    }

    @Test(groups = { "unittest" })
    public void testCreateCommodityFuturesPositionLong() {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), COMMODITY_LONG);
        // These are not populated logically
        assertEqualsBD(bp.getCurrentLongPosition(), 1000);
        assertEqualsBD(bp.getCurrentShortPosition(), 0);
        assertEqualsBD(bp.getPrimaryTraderPosition(), 16600);
        assertEqualsBD(bp.getFullCurrentNetPosition(), 16600000);
        assertEqualsBD(bp.getFullCurrentNetPositionWithoutComma(), 16600000);

        ISecurity security = bp.getSecurity();

        assertNotNull(security);
        assertEquals(security.getName(), "US 2YR NOTE (CBT) Mar09");
        assertEquals(security.getSecurityId(), "TUH9");
        assertEquals(security.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(security.getProductCode(), BBProductCode.Commodity);
        assertEquals(security.getTicker(), "TUH9");
        assertEquals(security.getSecurityType2(), BBSecurityType.Futures);

        Position position = bp.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof CommodityFutures);
        assertEqualsBD(position.getOpenPosition(), 83); // the position for start of day
        assertEqualsBD(position.getCurrentPosition(), 83);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 0);
        assertEquals(position.getIntradayPosition(), 0);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "GSFUT");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertEquals(positionSecurity.getName(), "US 2YR NOTE (CBT) Mar09");
        assertEquals(positionSecurity.getSecurityId(), "TUH9");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Commodity);
        assertEquals(positionSecurity.getTicker(), "TUH9");
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.Futures);
        assertEquals(((IFuturesSecurity) positionSecurity).getFuturesCategory(),
            BBFuturesCategory.BondFutures);
    }

    @Test(groups = { "unittest" })
    public void testCreateCommodityFuturesPositionShort() {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), COMMODITY_SHORT);
        // These are not populated logically
        assertEqualsBD(bp.getCurrentLongPosition(), -1100);
        assertEqualsBD(bp.getCurrentShortPosition(), 0);
        assertEqualsBD(bp.getPrimaryTraderPosition(), -1100);
        assertEqualsBD(bp.getFullCurrentNetPosition(), -1100000);
        assertEqualsBD(bp.getFullCurrentNetPositionWithoutComma(), -1100000);

        ISecurity security = bp.getSecurity();

        assertNotNull(security);
        assertEquals(security.getName(), "US LONG BOND(CBT) Mar09");
        assertEquals(security.getSecurityId(), "USH9");
        assertEquals(security.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(security.getProductCode(), BBProductCode.Commodity);
        assertEquals(security.getTicker(), "USH9");
        assertEquals(security.getSecurityType2(), BBSecurityType.Futures);

        Position position = bp.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof CommodityFutures);
        assertEqualsBD(position.getOpenPosition(), -11); // the position for start of day
        assertEqualsBD(position.getCurrentPosition(), -11);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 0);
        assertEquals(position.getIntradayPosition(), 0);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "GSFUT");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertEquals(positionSecurity.getName(), "US LONG BOND(CBT) Mar09");
        assertEquals(positionSecurity.getSecurityId(), "USH9");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Commodity);
        assertEquals(positionSecurity.getTicker(), "USH9");
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.Futures);
        assertEquals(((IFuturesSecurity) positionSecurity).getFuturesCategory(),
            BBFuturesCategory.BondFutures);
    }

    @Test(groups = { "unittest" })
    public void testAddCommodityFuturesOnlinePositionShortShort() throws Exception {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), COMMODITY_SHORT);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(COMMODITY_SHORT_SHORT);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());
        assertEqualsBD(op.getOpenPosition(), bp.getOnlineOpenPosition());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), -11); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), -12);
        assertEquals(updatedPosition.getSharesSold(), 1);
        assertEquals(updatedPosition.getSharesBought(), 0);
        assertEquals(updatedPosition.getIntradayPosition(), -1);
        assertEquals(updatedPosition.getPositionType(), PositionType.Short);
    }

    /**
     * This is a currency future of an interest rate.
     */
    @Test(groups = { "unittest" })
    public void testCreateCurrencyInterestRateFuturesPositionShort() {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), CURRENCY_IR_LONG);
        // These are not populated logically
        assertEqualsBD(bp.getCurrentLongPosition(), 1000);
        assertEqualsBD(bp.getCurrentShortPosition(), 0);
        assertEqualsBD(bp.getPrimaryTraderPosition(), 1000);
        assertEqualsBD(bp.getFullCurrentNetPosition(), 1000000);
        assertEqualsBD(bp.getFullCurrentNetPositionWithoutComma(), 1000000);

        ISecurity security = bp.getSecurity();

        assertNotNull(security);
        assertEquals(security.getName(), "EURO S  3MO LIFFE Mar09");
        assertEquals(security.getSecurityId(), "ESH9");
        assertEquals(security.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(security.getProductCode(), BBProductCode.Commodity);
        assertEquals(security.getTicker(), "ESH9");
        assertEquals(security.getSecurityType2(), BBSecurityType.Futures);

        Position position = bp.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof CommodityFutures);
        assertEqualsBD(position.getOpenPosition(), 1); // the position for start of day
        assertEqualsBD(position.getCurrentPosition(), 1);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 0);
        assertEquals(position.getIntradayPosition(), 0);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "GSFUT");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertEquals(positionSecurity.getName(), "EURO S  3MO LIFFE Mar09");
        assertEquals(positionSecurity.getSecurityId(), "ESH9");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Commodity);
        assertEquals(positionSecurity.getTicker(), "ESH9");
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.Futures);
        assertEquals(((IFuturesSecurity) positionSecurity).getFuturesCategory(),
            BBFuturesCategory.InterestRate);
    }

    // TODO find an online record for ES currency interest rate
    // @Test(groups = { "unittest" })
    public void testAddCurrencyInterestRateFuturesOnlinePositionLongShort() throws Exception {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), CURRENCY_IR_LONG);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(CURRENCY_IR_BLAH);
        // RtfOnlinePosition op = RtfOnlinePosition.valueOf(new LocalDate(), 1, CURRENCY_IR_BLAH);
        // System.err.println(op);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());
        assertEqualsBD(op.getOpenPosition(), bp.getOnlineOpenPosition());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEquals(updatedPosition.getOpenPosition(), 5); // the position for start of day
        assertEquals(updatedPosition.getCurrentPosition(), -3);
        assertEquals(updatedPosition.getSharesSold(), 8);
        assertEquals(updatedPosition.getSharesBought(), 0);
        assertEquals(updatedPosition.getIntradayPosition(), -8);
        assertEquals(updatedPosition.getPositionType(), PositionType.Short);
    }

    @Test(groups = { "unittest" })
    public void testCreateIndexFuturesPositionLong() {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), INDEX_LONG);
        // These are not populated logically
        assertEqualsBD(bp.getCurrentLongPosition(), 5);
        assertEqualsBD(bp.getCurrentShortPosition(), 0);
        assertEqualsBD(bp.getPrimaryTraderPosition(), 5);
        assertEqualsBD(bp.getFullCurrentNetPosition(), 5);
        assertEqualsBD(bp.getFullCurrentNetPositionWithoutComma(), 5);

        ISecurity security = bp.getSecurity();

        assertNotNull(security);
        assertEquals(security.getName(), "S&P500 EMINI FUT  Mar09");
        assertEquals(security.getSecurityId(), "ESH9");
        assertEquals(security.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(security.getProductCode(), BBProductCode.Index);
        assertEquals(security.getTicker(), "ESH9");
        assertEquals(security.getSecurityType2(), BBSecurityType.Futures);

        Position position = bp.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof IndexFutures);
        assertEqualsBD(position.getOpenPosition(), 5); // the position for start of day
        assertEqualsBD(position.getCurrentPosition(), 5);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 0);
        assertEquals(position.getIntradayPosition(), 0);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "GSFUT");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertEquals(positionSecurity.getName(), "S&P500 EMINI FUT  Mar09");
        assertEquals(positionSecurity.getSecurityId(), "ESH9");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Index);
        assertEquals(positionSecurity.getTicker(), "ESH9");
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.Futures);
        assertEquals(((IFuturesSecurity) positionSecurity).getFuturesCategory(),
            BBFuturesCategory.StockIndex);
    }

    @Test(groups = { "unittest" })
    public void testAddIndexFuturesOnlinePositionLongShort() throws Exception {
        BatchPosition bp = BatchPosition.valueOf(new LocalDate(), INDEX_LONG);

        // Add an online record
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(INDEX_LONG_SHORT);

        assertNotNull(op);
        assertEquals(op.getSecurityId(), bp.getSecurityId());
        assertEquals(op.getAccount(), bp.getAccount());
        assertEquals(op.getLevel1TagName(), bp.getLevel1TagName());
        assertEquals(op.getLevel2TagName(), bp.getLevel2TagName());
        assertEquals(op.getLevel3TagName(), bp.getLevel3TagName());
        assertEquals(op.getLevel4TagName(), bp.getLevel4TagName());
        assertEquals(op.getProductCode(), bp.getProductCode());
        assertEquals(op.getPrimeBroker(), bp.getPrimeBroker());
        assertEqualsBD(op.getOpenPosition(), bp.getOnlineOpenPosition());

        BatchPosition newBatchPosition = bp.setOnlinePosition(op);

        assertNotNull(newBatchPosition);

        Position updatedPosition = newBatchPosition.getPosition();

        assertEqualsBD(updatedPosition.getOpenPosition(), 5); // the position for start of day
        assertEqualsBD(updatedPosition.getCurrentPosition(), -3);
        assertEquals(updatedPosition.getSharesSold(), 8);
        assertEquals(updatedPosition.getSharesBought(), 0);
        assertEquals(updatedPosition.getIntradayPosition(), -8);
        assertEquals(updatedPosition.getPositionType(), PositionType.Short);
    }

    @Test(groups = { "unittest" })
    public void testCreateBatchPositionFromOnlinePositionEquity() throws Exception {
        RtfOnlinePosition onlinePosition = RtfOnlinePosition.valueOfCommaString(EQUITY_ONLINE_1);
        onlinePosition.setContractSize(BigDecimal.ONE);
        
        assertEqualsBD(onlinePosition.getOpenPosition(), 0);
        assertEqualsBD(onlinePosition.getCurrentPosition(), 10774);

        BatchPosition batchPosition = BatchPosition.valueOf(onlinePosition);

        assertNotNull(batchPosition);

        assertEquals(batchPosition.getSecurityId(), onlinePosition.getSecurityId());
        assertEquals(batchPosition.getSecurityIdFlag(), onlinePosition.getSecurityIdFlag());
        assertEquals(batchPosition.getSecurityId(), onlinePosition.getSecurityId());
        assertEquals(batchPosition.getProductCode(), onlinePosition.getProductCode());
        assertEquals(batchPosition.getAccount(), onlinePosition.getAccount());

        assertEquals(batchPosition.getAccount(), onlinePosition.getAccount());

        assertEquals(batchPosition.getFullCurrentNetPosition(), onlinePosition.getCurrentPosition());
        assertEquals(batchPosition.getFullCurrentNetPositionWithoutComma(), onlinePosition
            .getCurrentPosition());
        assertEquals(batchPosition.getCurrentPosition(), onlinePosition.getCurrentPosition());

        assertEquals(batchPosition.getLevel1TagName(), onlinePosition.getLevel1TagName());
        assertEquals(batchPosition.getLevel2TagName(), onlinePosition.getLevel2TagName());
        assertEquals(batchPosition.getLevel3TagName(), onlinePosition.getLevel3TagName());
        assertEquals(batchPosition.getLevel4TagName(), onlinePosition.getLevel4TagName());
        
        Position position = batchPosition.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof Equity);
        assertEqualsBD(position.getOpenPosition(), 0);
        assertEqualsBD(position.getCurrentPosition(), 10774);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 10774);
        assertEquals(position.getIntradayPosition(), 10774);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "MSPB");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertNull(positionSecurity.getName());
        assertEquals(positionSecurity.getSecurityId(), "459200101");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Equity);
        assertNull(positionSecurity.getTicker());
        // We do not have this information when created from an online position
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.Unknown);
        
        // Add a second online record
        RtfOnlinePosition onlinePosition2 = RtfOnlinePosition.valueOfCommaString(EQUITY_ONLINE_2);
        onlinePosition2.setContractSize(BigDecimal.ONE);
        
        BatchPosition combinedPosition = batchPosition.setOnlinePosition(onlinePosition2);
        
        assertNotNull(combinedPosition);
        
        position = batchPosition.getPosition();
        
        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof Equity);
        assertEqualsBD(position.getOpenPosition(), 0);
        assertEqualsBD(position.getCurrentPosition(), 10776);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 10776);
        assertEquals(position.getIntradayPosition(), 10776);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "MSPB");
    }
    
    @Test(groups = { "unittest" })
    public void testCreateBatchPositionFromOnlinePositionFuture() throws Exception {
        RtfOnlinePosition onlinePosition = RtfOnlinePosition.valueOfCommaString(FUTURE_ONLINE_1);
        onlinePosition.setContractSize(new BigDecimal(62500));
        
        assertEqualsBD(onlinePosition.getOpenPosition(), 0);
        assertEqualsBD(onlinePosition.getCurrentPosition(), 437500);

        BatchPosition batchPosition = BatchPosition.valueOf(onlinePosition);

        assertNotNull(batchPosition);

        assertEquals(batchPosition.getSecurityId(), onlinePosition.getSecurityId());
        assertEquals(batchPosition.getSecurityIdFlag(), onlinePosition.getSecurityIdFlag());
        assertEquals(batchPosition.getSecurityId(), onlinePosition.getSecurityId());
        assertEquals(batchPosition.getProductCode(), onlinePosition.getProductCode());
        assertEquals(batchPosition.getAccount(), onlinePosition.getAccount());

        assertEquals(batchPosition.getAccount(), onlinePosition.getAccount());

        assertEquals(batchPosition.getFullCurrentNetPosition(), onlinePosition.getCurrentPosition());
        assertEquals(batchPosition.getFullCurrentNetPositionWithoutComma(), onlinePosition
            .getCurrentPosition());
        assertEquals(batchPosition.getCurrentPosition(), onlinePosition.getCurrentPosition());

        assertEquals(batchPosition.getLevel1TagName(), onlinePosition.getLevel1TagName());
        assertEquals(batchPosition.getLevel2TagName(), onlinePosition.getLevel2TagName());
        assertEquals(batchPosition.getLevel3TagName(), onlinePosition.getLevel3TagName());
        assertEquals(batchPosition.getLevel4TagName(), onlinePosition.getLevel4TagName());
        
        Position position = batchPosition.getPosition();

        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof CurrencyFutures, position.getSecurity().getClass().getName());
        assertEqualsBD(position.getOpenPosition(), 0);
        assertEqualsBD(position.getCurrentPosition(), 7);
        assertEquals(position.getSharesSold(), 0);
        assertEquals(position.getSharesBought(), 7);
        assertEquals(position.getIntradayPosition(), 7);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "GSFUT");

        ISecurity positionSecurity = position.getSecurity();

        assertNotNull(positionSecurity);
        assertNull(positionSecurity.getName());
        assertEquals(positionSecurity.getSecurityId(), "BPM9");
        assertEquals(positionSecurity.getSecurityIdFlag(), BBSecurityIDFlag.Cusip);
        assertEquals(positionSecurity.getProductCode(), BBProductCode.Currency);
        assertNull(positionSecurity.getTicker());
        // We are guessing the security type from the security id, if it has a root, then 
        // it is a futures
        assertEquals(positionSecurity.getSecurityType2(), BBSecurityType.Futures);
        
        // Add a second online record
        RtfOnlinePosition onlinePosition2 = RtfOnlinePosition.valueOfCommaString(FUTURE_ONLINE_2);
        onlinePosition2.setContractSize(new BigDecimal(62500));
        assertEqualsBD(onlinePosition2.getOpenPosition(), 0);
        
        BatchPosition combinedPosition = batchPosition.setOnlinePosition(onlinePosition2);
        
        assertNotNull(combinedPosition);
        assertEqualsBD(combinedPosition.getFullCurrentNetPositionWithoutComma(), onlinePosition2.getCurrentPosition());
        
        position = batchPosition.getPosition();
        
        assertNotNull(position);
        assertNotNull(position.getSecurity());
        assertTrue(position.getSecurity() instanceof CurrencyFutures);
        assertEqualsBD(position.getOpenPosition(), 0);
        assertEqualsBD(position.getCurrentPosition(), 6);
        assertEquals(position.getSharesSold(), 1);
        assertEquals(position.getSharesBought(), 7);
        assertEquals(position.getIntradayPosition(), 6);
        assertEquals(position.getAccount(), "TEST");
        assertEquals(position.getLevel1TagName(), "TEST");
        assertEquals(position.getLevel2TagName(), "TEST_");
        assertEquals(position.getLevel3TagName(), "TEST..");
        assertEquals(position.getLevel4TagName(), "TEST.");
        assertEquals(position.getPrimeBroker(), "GSFUT");
    }
    
    @Test(groups = { "unittest" })
    public void testAddEquityPositions() throws Exception {
        BatchPosition bp1 = BatchPosition.valueOf(new LocalDate(), EQUITY_LONG);
        BatchPosition bp2 = bp1.copy();
        
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(EQUITY_LONG_LONG);
        bp2.setOnlinePosition(op);
        
        Position p1 = bp1.getPosition();
        Position p2 = bp2.getPosition();
        
        AbstractPositionAggregationStrategy pas = new AbstractPositionAggregationStrategy() {

            @Override
            public Position convertToAggregate(Position item) {
                return item;
            }
        };
        
        Position p1p2 = pas.aggregate(p1, p2);
        
        assertNotNull(p1p2);
        assertEquals(p1p2.getAccount(), "TEST");
        assertEquals(p1p2.getLevel1TagName(), "TEST");
        assertEquals(p1p2.getLevel2TagName(), "TEST_");
        assertEquals(p1p2.getLevel3TagName(), "TEST..");
        assertEquals(p1p2.getLevel4TagName(), "TEST.");
        
        assertEqualsBD(p1p2.getOpenPosition(), 4);
        assertEqualsBD(p1p2.getCurrentPosition(), 10776);
        assertEquals(p1p2.getSharesBought(), 10772);
        assertEquals(p1p2.getSharesSold(), 0);
        assertEquals(p1p2.getIntradayPosition(), 10772);
        assertEquals(p1p2.getSecurity(), p1.getSecurity());
        assertEquals(p1p2.getSecurity(), p2.getSecurity());
    }
    
    @Test(groups = { "unittest" })
    public void testAddCommodityPositions() throws Exception {
        BatchPosition bp1 = BatchPosition.valueOf(new LocalDate(), COMMODITY_SHORT);
        BatchPosition bp2 = bp1.copy();
        
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(COMMODITY_SHORT_SHORT);
        bp2.setOnlinePosition(op);
        
        
        Position p1 = bp1.getPosition();
        Position p2 = bp2.getPosition();
        
        AbstractPositionAggregationStrategy pas = new AbstractPositionAggregationStrategy() {

            @Override
            public Position convertToAggregate(Position item) {
                return item;
            }
        };
        
        Position p1p2 = pas.aggregate(p1, p2);
        
        assertNotNull(p1p2);
        assertEquals(p1p2.getAccount(), "TEST");
        assertEquals(p1p2.getLevel1TagName(), "TEST");
        assertEquals(p1p2.getLevel2TagName(), "TEST_");
        assertEquals(p1p2.getLevel3TagName(), "TEST..");
        assertEquals(p1p2.getLevel4TagName(), "TEST.");
        
        assertEqualsBD(p1p2.getOpenPosition(), -22);
        assertEqualsBD(p1p2.getCurrentPosition(), -23);
        assertEquals(p1p2.getSharesBought(), 0);
        assertEquals(p1p2.getSharesSold(), 1);
        assertEquals(p1p2.getIntradayPosition(), -1);
        assertEquals(p1p2.getSecurity(), p1.getSecurity());
        assertEquals(p1p2.getSecurity(), p2.getSecurity());
    }
    
    @Test(groups = { "unittest" })
    public void testAddDifferentSecurityPositions() throws Exception {
        BatchPosition bp1 = BatchPosition.valueOf(new LocalDate(), COMMODITY_SHORT);
        
        BatchPosition bp2 = BatchPosition.valueOf(new LocalDate(), EQUITY_LONG);
        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(EQUITY_LONG_LONG);
        bp2.setOnlinePosition(op);
        
        
        Position p1 = bp1.getPosition();
        Position p2 = bp2.getPosition();
        
        AbstractPositionAggregationStrategy pas = new AbstractPositionAggregationStrategy() {

            @Override
            public Position convertToAggregate(Position item) {
                Position copy = item.copy();
                copy.setSecurity(null);

                return copy;
            }
        };
        
        Position p1p2 = pas.aggregate(p1, p2);
        
        assertNotNull(p1p2);
        assertEquals(p1p2.getAccount(), "TEST");
        assertEquals(p1p2.getLevel1TagName(), "TEST");
        assertEquals(p1p2.getLevel2TagName(), "TEST_");
        assertEquals(p1p2.getLevel3TagName(), "TEST..");
        assertEquals(p1p2.getLevel4TagName(), "TEST.");
        
        assertEqualsBD(p1p2.getOpenPosition(), -9);
        assertEqualsBD(p1p2.getCurrentPosition(), 10763);
        assertEquals(p1p2.getSharesBought(), 10772);
        assertEquals(p1p2.getSharesSold(), 0);
        assertEquals(p1p2.getIntradayPosition(), 10772);
        assertNotEquals(p1p2.getSecurity(), p1.getSecurity());
        assertNotEquals(p1p2.getSecurity(), p2.getSecurity());
    }

}
