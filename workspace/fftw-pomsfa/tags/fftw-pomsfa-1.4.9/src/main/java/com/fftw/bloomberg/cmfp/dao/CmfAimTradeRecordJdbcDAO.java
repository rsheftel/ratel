package com.fftw.bloomberg.cmfp.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import malbec.bloomberg.types.BBYellowKey;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.fftw.bloomberg.cmfp.CmfAimTradeRecord;
import com.fftw.bloomberg.cmfp.CmfConstants;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.util.datetime.DateTimeUtil;

/**
 * Springframework based DAO.
 * 
 */
@Transactional
public class CmfAimTradeRecordJdbcDAO implements CmfTradeRecordDAO
{

    private static final String INSERT_SQL = "insert into TRADE_RECORD (CREATION_DATE, TRADE_SEQ_NUM, "
        + "STATUS, SECURITY_ID_FLAG, SECURITY_ID, BROKER, SIDE, QUANTITY, PRICE_DISPLAY, "
        + "PRICE, TRADING_STRATEGY, SETTLE_DATE, ACCOUNT, SOURCE_CODE, PRIME_BROKER, "
        + "TRANS_CODE, EXEC_ID, PLATFORM_ID, PRODUCT_CODE, LAST_UPDATED) "
        + "values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE() ) ";

    private static final String UPDATE_RECEIPT = "update TRADE_RECORD set ACK_RECEIVED = ?, "
        + "LAST_UPDATED=GETDATE() where CREATION_DATE=? and TRADE_SEQ_NUM=?";

    private static final String UPDATE_ACCEPT_REJECT = "update TRADE_RECORD set ACCEPT_REJECT_RECEIVED = ?, "
        + "TICKET_NUMBER=?, RETURN_CODE=?, ERROR_MSG=?,LAST_UPDATED=GETDATE() "
        + "where CREATION_DATE=? and TRADE_SEQ_NUM=?";

    private static final String FIND_BY_EXEC_ID = "select * from TRADE_RECORD where CREATION_DATE=? "
        + "and PLATFORM_ID=? and EXEC_ID=?";

    private static final String NEXT_SEQ_NO_SAME_DATE = "select count(TRADE_SEQ_NUM) + 1 from TRADE_RECORD where CREATION_DATE=?";

    private static final String NEXT_SEQ_NO_TWO_DATES = "select count(TRADE_SEQ_NUM) + 1 from TRADE_RECORD where CREATION_DATE in (?, ?)";
    private static final String NEXT_SEQ_NO_THREE_DATES = "select count(TRADE_SEQ_NUM) + 1 from TRADE_RECORD where CREATION_DATE in (?, ?, ?)";

    private SimpleJdbcTemplate jdbcTemplate;

    public CmfAimTradeRecordJdbcDAO ()
    {
    }

    public void setDataSource (DataSource dataSource)
    {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    /**
     * Insert the record - generated the <tt>tradeSequenceNumber</tt>
     */
    public CmfTradeRecord insert (CmfTradeRecord newTradeRecord) throws SQLException
    {

        CmfAimTradeRecord aimRecord = (CmfAimTradeRecord)newTradeRecord;
        // We can only have one thread inserting at a time.
        // We have have multiple insert methods, we may have to
        // make this logic smarter
        synchronized (CmfAimTradeRecordJdbcDAO.class)
        {
            Date jdbcDate = Date.valueOf(aimRecord.getCreationDate().toString());

            int nextNumber = calculateNextSequenceNumber(jdbcDate);

            aimRecord.setTradeSeqNum(nextNumber);

            Object[] parameters = new Object[19];

            parameters[0] = jdbcDate;
            parameters[1] = nextNumber;
            parameters[2] = newTradeRecord.getStatus();
            parameters[3] = newTradeRecord.getSecurityIdFlag().getIDFlag();
            parameters[4] = newTradeRecord.getSecurityId();
            // parameters[5] = newTradeRecord.getTraderName();
            parameters[5] = newTradeRecord.getBroker();
            parameters[6] = newTradeRecord.getSide();
            parameters[7] = newTradeRecord.getQuantity();
            parameters[8] = newTradeRecord.getPriceQuoteDisplay();
            parameters[9] = newTradeRecord.getPriceQuote();
            parameters[10] = newTradeRecord.getTradingStrategy();
            parameters[11] = utcDate2SqlDate(newTradeRecord.getSettleDate());
            parameters[12] = newTradeRecord.getAccount();
            parameters[13] = newTradeRecord.getSourceCode();
            parameters[14] = newTradeRecord.getPrimeBroker();
            parameters[15] = newTradeRecord.getTransactionCode();
            parameters[16] = newTradeRecord.getExecutionID();
            parameters[17] = newTradeRecord.getTradingPlatform().getText();
            parameters[18] = newTradeRecord.getProductCode().getCode();

            jdbcTemplate.update(INSERT_SQL, parameters);
        }

        return aimRecord;
    }

    /**
     * Handle the logic to determine the next sequence number.
     * 
     * Saturday, Sunday and Monday are merged, all other days are individual.
     * 
     * @param jdbcDate
     * @return
     */
    int calculateNextSequenceNumber (Date jdbcDate)
    {

        // 1 - Monday
        // 6 - Saturday
        // 7 - Sunday
        LocalDate ld = new LocalDate(jdbcDate);

        if (ld.getDayOfWeek() == 1)
        {
            // date is Monday, include Saturday and Sunday
            LocalDate sunday = ld.minusDays(1);
            LocalDate saturday = ld.minusDays(2);

            return jdbcTemplate.queryForInt(NEXT_SEQ_NO_THREE_DATES, new Object[]
            {
                Date.valueOf(saturday.toString()), Date.valueOf(sunday.toString()), jdbcDate
            });
        }
        else if (ld.getDayOfWeek() == 7)
        {
            // date is Sunday, include Saturday
            LocalDate sunday = ld.minusDays(1);
            return jdbcTemplate.queryForInt(NEXT_SEQ_NO_TWO_DATES, new Object[]
            {
                Date.valueOf(sunday.toString()), jdbcDate
            });
        }
        else 
        { // all other days
            return jdbcTemplate.queryForInt(NEXT_SEQ_NO_SAME_DATE, new Object[]
            {
                jdbcDate
            });
        }
    }

    private Date utcDate2SqlDate (String utcDateStr)
    {
        return Date.valueOf(DateTimeUtil.getLocalDate(utcDateStr).toString());
    }

    public CmfTradeRecord updateReceipt (CmfTradeRecord updatedTradeRecord) throws SQLException
    {
        CmfAimTradeRecord aimRecord = (CmfAimTradeRecord)updatedTradeRecord;
        Date jdbcDate = Date.valueOf(aimRecord.getCreationDate().toString());

        Object[] parameters = new Object[3];

        parameters[0] = new Timestamp(updatedTradeRecord.getReceiptTimestamp().getMillis());

        // Where parameters
        parameters[1] = jdbcDate;
        parameters[2] = updatedTradeRecord.getTradeSeqNum();

        jdbcTemplate.update(UPDATE_RECEIPT, parameters);

        return aimRecord;
    }

    public CmfTradeRecord updateAcceptReject (CmfTradeRecord updatedTradeRecord)
        throws SQLException
    {
        CmfAimTradeRecord aimRecord = (CmfAimTradeRecord)updatedTradeRecord;

        Date jdbcDate = Date.valueOf(aimRecord.getCreationDate().toString());
        Object[] parameters = new Object[6];

        parameters[0] = new Timestamp(updatedTradeRecord.getAcceptRejectTimestamp().getMillis());
        parameters[1] = updatedTradeRecord.getTicketNumber();
        parameters[2] = updatedTradeRecord.getReturnCode();

        String errorMsg = updatedTradeRecord.getErrorMsg();

        if (errorMsg == null || errorMsg.trim().length() == 0)
        {
            parameters[3] = null;
        }
        else
        {
            parameters[3] = errorMsg.trim();
        }

        // Where parameters
        parameters[4] = jdbcDate;
        parameters[5] = updatedTradeRecord.getTradeSeqNum();

        jdbcTemplate.update(UPDATE_ACCEPT_REJECT, parameters);

        return aimRecord;
    }

    public CmfTradeRecord findByExecId (LocalDate date, String senderId, String execId)
        throws SQLException
    {

        Date jdbcDate = Date.valueOf(date.toString());
        List<CmfTradeRecord> results = jdbcTemplate.query(FIND_BY_EXEC_ID,
            new TradeRecordRowMapper(), new Object[]
            {
                jdbcDate, senderId, execId
            });
        if (results.size() > 0)
        {
            return results.get(0);
        }
        else
        {
            return null;
        }
    }

    private static class TradeRecordRowMapper implements ParameterizedRowMapper<CmfTradeRecord>
    {
        public CmfTradeRecord mapRow (ResultSet rs, int rowNum) throws SQLException
        {
            CmfTradeRecord tradeRecord = new CmfAimTradeRecord(CmfConstants.TRADE);

            tradeRecord.setTradeSeqNum(rs.getInt("TRADE_SEQ_NUM"));
            tradeRecord.setStatus(rs.getInt("STATUS"));
            tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.valueOf(rs.getInt("SECURITY_ID_FLAG")));
            tradeRecord.setSecurityId(rs.getString("SECURITY_ID"));
            tradeRecord.setTraderName(rs.getString("TRADER_NAME"));
            tradeRecord.setSide(rs.getInt("SIDE"));
            tradeRecord.setQuantity(rs.getDouble("QUANTITY"));
            tradeRecord.setPriceQuote(rs.getDouble("QUANTITY"));
            tradeRecord.setPriceQuoteDisplay(rs.getInt("PRICE_DISPLAY"));
            tradeRecord.setSettleDate(rs.getDate("SETTLE_DATE"));
            tradeRecord.setAccount(rs.getString("ACCOUNT"));
            tradeRecord.setSourceCode(rs.getInt("SOURCE_CODE"));
            tradeRecord.setPrimeBroker(rs.getString("PRIME_BROKER"));
            tradeRecord.setBroker(rs.getString("BROKER"));
            tradeRecord.setReturnCode(rs.getInt("RETURN_CODE"));
            tradeRecord.setErrorMsg(rs.getString("ERROR_MSG"));
            tradeRecord.setAcceptRejectTimestamp(new DateTime(rs
                .getTimestamp("ACCEPT_REJECT_RECEIVED")));
            tradeRecord.setAcceptRejectTimestamp(new DateTime(rs.getTimestamp("ACK_RECEIVED")));
            tradeRecord.setTradingStrategy(rs.getString("TRADING_STRATEGY"));
            tradeRecord.setExecutionID(rs.getString("EXEC_ID"));
            tradeRecord.setTradingPlatform(TradingPlatform.valueFor(rs.getString("PLATFORM_ID")));
            tradeRecord.setProductCode(BBYellowKey.valueOf(rs.getInt("PRODUCT_CODE")));

            return tradeRecord;
        }
    }
}
