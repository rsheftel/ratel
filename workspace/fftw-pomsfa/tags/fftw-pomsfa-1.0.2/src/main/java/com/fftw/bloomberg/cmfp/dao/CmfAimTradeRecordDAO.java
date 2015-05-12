package com.fftw.bloomberg.cmfp.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import javax.sql.DataSource;

import org.joda.time.LocalDate;

import com.fftw.bloomberg.cmfp.CmfAimTradeRecord;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.util.datetime.DateTimeUtil;

public class CmfAimTradeRecordDAO implements CmfTradeRecordDAO
{

    private DataSource ds;

    private static final String INSERT_SQL = "insert into TRADE_RECORD (CREATION_DATE, TRADE_SEQ_NUM, "
        + "STATUS, SECURITY_ID_FLAG, SECURITY_ID, TRADER_NAME, SIDE, QUANTITY, PRICE_DISPLAY, "
        + "PRICE, TRADING_STRATEGY, SETTLE_DATE, ACCOUNT, SOURCE_CODE, PRIME_BROKER, "
        + "TRANS_CODE, LAST_UPDATED) "
        + "values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE() ) ";

    // , RETURN_CODE, ERROR_MSG, ACK_RECEIVED, " +
    // "ACCEPT_REJECT_RECEIVED, PROCESSING_STATUS

    private static final String UPDATE_RECEIPT = "update TRADE_RECORD set ACK_RECEIVED = ?, "
        + "LAST_UPDATED=GETDATE() where CREATION_DATE=? and TRADE_SEQ_NUM=?";

    private static final String UPDATE_ACCEPT_REJECT = "update TRADE_RECORD set ACCEPT_REJECT_RECEIVED = ?, "
        + "RETURN_CODE=?, ERROR_MSG=?,LAST_UPDATED=GETDATE() "
        + "where CREATION_DATE=? and TRADE_SEQ_NUM=?";

    private static final String NEXT_SEQ_NO = "select count(TRADE_SEQ_NUM) + 1 from TRADE_RECORD where CREATION_DATE=?";

    public CmfAimTradeRecordDAO (DataSource ds)
    {
        this.ds = ds;
    }

    /**
     * Insert the record - generated the <tt>tradeSequenceNumber</tt>
     */
    public CmfTradeRecord insert (CmfTradeRecord newTradeRecord) throws SQLException
    {
        CmfAimTradeRecord aimRecord = (CmfAimTradeRecord)newTradeRecord;

        Connection con = null;

        try
        {
            con = ds.getConnection();

            con.setAutoCommit(false);
            // We can only have one thread inserting at a time.
            // We have have multiple insert methods, we may have to
            // make this logic smarter
            synchronized (CmfAimTradeRecordDAO.class)
            {
                Date jdbcDate = Date.valueOf(aimRecord.getCreationDate().toString());
                PreparedStatement ps = con.prepareStatement(NEXT_SEQ_NO);
                ps.setDate(1, jdbcDate);
                ResultSet nextNumberRS = ps.executeQuery();

                int nextNumber = -1;
                while (nextNumberRS.next())
                {
                    nextNumber = nextNumberRS.getInt(1);
                }
                ps.close();

                if (nextNumber == -1)
                {
                    throw new SQLException("Unable to generate TradeSequenceNumber");
                }
                aimRecord.setTradeSeqNum(nextNumber);

                ps = con.prepareStatement(INSERT_SQL);
                ps.setDate(1, jdbcDate);
                ps.setInt(2, nextNumber);
                ps.setInt(3, newTradeRecord.getStatus());
                ps.setInt(4, newTradeRecord.getSecurityIdFlag());
                ps.setString(5, newTradeRecord.getSecurityId());
                ps.setString(6, newTradeRecord.getTraderName());
                ps.setInt(7, newTradeRecord.getSide());
                ps.setDouble(8, newTradeRecord.getQuantity());
                ps.setInt(9, newTradeRecord.getPriceQuoteDisplay());
                ps.setDouble(10, newTradeRecord.getPriceQuote());
                ps.setString(11, newTradeRecord.getTradingStrategy());
                ps.setDate(12, Date.valueOf(DateTimeUtil.getLocalDate(
                    newTradeRecord.getSettleDate()).toString()));
                ps.setString(13, newTradeRecord.getAccount());
                ps.setInt(14, newTradeRecord.getSourceCode());
                ps.setString(15, newTradeRecord.getPrimeBroker());
                ps.setString(16, newTradeRecord.getTransactionCode());

                ps.executeUpdate();
                con.commit();
                ps.close();
            }

            return aimRecord;
        }
        finally
        {
            closeConnection(con);
        }
    }

    public CmfTradeRecord updateReceipt (CmfTradeRecord updatedTradeRecord) throws SQLException
    {
        CmfAimTradeRecord aimRecord = (CmfAimTradeRecord)updatedTradeRecord;
        Connection con = null;
        try
        {
            con = ds.getConnection();
            con.setAutoCommit(false);
            Date jdbcDate = Date.valueOf(aimRecord.getCreationDate().toString());
            PreparedStatement ps = con.prepareStatement(UPDATE_RECEIPT);
            ps.setTimestamp(1, new Timestamp(updatedTradeRecord.getReceiptTimestamp().getMillis()));

            // Where parameters
            ps.setDate(2, jdbcDate);
            ps.setInt(3, updatedTradeRecord.getTradeSeqNum());

            int updateCount = ps.executeUpdate();
            con.commit();

            ps.close();

            if (updateCount >= 1)
            {
                return updatedTradeRecord;
            }
            else
            {
                return null;
            }
        }
        finally
        {
            closeConnection(con);
        }
    }

    private void closeConnection (Connection con) throws SQLException
    {
        if (con != null)
        {
            con.close();
        }
    }

    public CmfTradeRecord updateAcceptReject (CmfTradeRecord updatedTradeRecord)
        throws SQLException
    {
        CmfAimTradeRecord aimRecord = (CmfAimTradeRecord)updatedTradeRecord;
        Connection con = null;

        try
        {
            con = ds.getConnection();
            con.setAutoCommit(false);
            Date jdbcDate = Date.valueOf(aimRecord.getCreationDate().toString());
            PreparedStatement ps = con.prepareStatement(UPDATE_ACCEPT_REJECT);
            ps.setTimestamp(1, new Timestamp(updatedTradeRecord.getAcceptRejectTimestamp()
                .getMillis()));
            ps.setInt(2, updatedTradeRecord.getReturnCode());
            String errorMsg = updatedTradeRecord.getErrorMsg();

            if (errorMsg == null || errorMsg.trim().length() == 0)
            {
                ps.setNull(3, Types.VARCHAR);
            }
            else
            {
                ps.setString(3, errorMsg.trim());
            }

            // Where parameters
            ps.setDate(4, jdbcDate);
            ps.setInt(5, updatedTradeRecord.getTradeSeqNum());

            int updateCount = ps.executeUpdate();
            con.commit();

            ps.close();

            if (updateCount >= 1)
            {
                return updatedTradeRecord;
            }
            else
            {
                return null;
            }
        }
        finally
        {
            closeConnection(con);
        }
    }

    public CmfTradeRecord findByExecId (LocalDate date, String platform, String execId) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
