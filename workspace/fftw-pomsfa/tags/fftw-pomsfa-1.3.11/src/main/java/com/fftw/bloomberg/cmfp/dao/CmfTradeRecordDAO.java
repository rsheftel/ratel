package com.fftw.bloomberg.cmfp.dao;

import java.sql.SQLException;

import org.joda.time.LocalDate;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;

/**
 * Class used to persist <code>CmfTradeRecord</code>s.
 * 
 * 
 */
public interface CmfTradeRecordDAO
{

    CmfTradeRecord insert (CmfTradeRecord newTradeRecord) throws SQLException;

    CmfTradeRecord updateReceipt (CmfTradeRecord updatedTradeRecord) throws SQLException;

    CmfTradeRecord updateAcceptReject (CmfTradeRecord updatedTradeRecord) throws SQLException;

    CmfTradeRecord findByExecId(LocalDate date, String senderId, String execId) throws SQLException;
}
