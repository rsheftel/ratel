package com.fftw.bloomberg.cmfp.dao;

import java.sql.Date;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;

public class CmfTradeDaoTest extends TestCase
{

    /**
     * Test the logic for the date calculation.
     * 
     * Only test the execution of the two different SQL statements.
     */
    public void testSequenceNumber ()
    {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
        bds.setUrl("jdbc:jtds:sqlserver://SQLDEVTS:2433/BADB");
//        bds.setUrl("jdbc:jtds:sqlserver://SQLPRODTS:2433/BADB");
        bds.setUsername("sim");
        bds.setPassword("Sim5878");

//        select count(TRADE_SEQ_NUM) + 1 from TRADE_RECORD where CREATION_DATE in ('2008-09-28', '2008-09-29')
//        select count(TRADE_SEQ_NUM) + 1 from TRADE_RECORD where CREATION_DATE in ('2008-09-28')
//        select count(TRADE_SEQ_NUM) + 1 from TRADE_RECORD where CREATION_DATE in ('2008-09-29')
        
        CmfAimTradeRecordJdbcDAO dao = new CmfAimTradeRecordJdbcDAO();
        dao.setDataSource(bds);

        int nextSequence = dao.calculateNextSequenceNumber(new Date(2008 - 1900, 9 - 1, 28));
        assertTrue("Failed to get starting sequence number", nextSequence > 0);

        nextSequence = dao.calculateNextSequenceNumber(new Date(2008 - 1900, 9 - 1, 29));

        assertTrue("Failed to get starting sequence number", nextSequence > 0);

        nextSequence = dao.calculateNextSequenceNumber(new Date(2008 - 1900, 9 - 1, 30));
        assertTrue("Failed to get starting sequence number", nextSequence > 0);
    }
}
