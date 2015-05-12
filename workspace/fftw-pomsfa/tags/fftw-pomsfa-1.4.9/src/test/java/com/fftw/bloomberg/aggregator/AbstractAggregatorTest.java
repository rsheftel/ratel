package com.fftw.bloomberg.aggregator;

import org.apache.commons.dbcp.BasicDataSource;
import org.testng.annotations.BeforeMethod;

import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

import malbec.pomsfa.fix.AbstractFixTest;

public abstract class AbstractAggregatorTest extends AbstractFixTest
{
    @BeforeMethod(groups =
    {
        "unittest"
    })
    public void SetupMappings ()
    {
        initializeFix2CmfUtil();
    }

    protected void initializeFix2CmfUtil ()
    {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
        ds.setUrl("jdbc:jtds:sqlserver://SQLDEVTS:2433/BADB");
        ds.setPassword("Sim5878");
        ds.setUsername("sim");
        Fix2CmfUtil f2c = new Fix2CmfUtil();
        f2c.setDataSource(ds);

        Fix2CmfUtil.initializeMaps();
        Fix2CmfUtil.clearStrategyMapping();
        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.TradingScreen, 1, "NDAYBRK",
            "FUTURES", "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.REDI, 1, "CPNSWAPF", "FUTURES",
            "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.REDI, 1, "FADERCLS", "FUTURES",
            "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.REDI, 1, "NBARFADE", "FUTURES",
        "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.REDI, 1, "MSCO", "EQUITY",
            "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.TradeWeb, 58, "CSFD", "FUTURES",
            "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.TradingScreen, 58, "ES", "EQUITY",
            "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.TradingScreen, 58, "FS", "FUTURES",
            "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.TradingScreen, 58, "FXS", "FX",
            "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.TradeStation, 1, "1TS001", "FUTURES",
            "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.TradeStation, 1, "17281450", "EQUITY",
        "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.Passport, 1, "052866142A", "FUTURES",
        "TEST-STRATEGY");

        Fix2CmfUtil.addStrategyAccountMapping(TradingPlatform.Passport, 1, "38C0731", "EQUITY",
        "TEST-STRATEGY");
        
    }

}
