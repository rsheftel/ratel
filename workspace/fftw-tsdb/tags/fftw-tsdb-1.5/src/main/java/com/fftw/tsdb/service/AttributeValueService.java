package com.fftw.tsdb.service;

import com.fftw.tsdb.dao.GenericDao;
import com.fftw.tsdb.domain.Ccy;
import com.fftw.tsdb.domain.GeneralAttributeValue;
import com.fftw.tsdb.domain.Ticker;
import com.fftw.tsdb.domain.cds.CdsTicker;

public interface AttributeValueService
{
    GenericDao<Ticker, Long> getTickerDao ();

    GenericDao<GeneralAttributeValue, Long> getGeneralAttributeValueDao ();

    Ticker createOrGetTicker (String value, String description);

    CdsTicker createOrGetCdsTicker (String value, Ccy ccy, GeneralAttributeValue docClause,
        Ticker ticker, GeneralAttributeValue tier);

    Ccy getCcy (String value);

    GeneralAttributeValue createOrGetGeneralAttributeValue (String value);

    GeneralAttributeValue getGeneralAttributeValue (String value);
}
