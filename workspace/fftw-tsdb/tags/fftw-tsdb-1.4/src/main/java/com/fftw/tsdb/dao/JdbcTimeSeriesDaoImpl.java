package com.fftw.tsdb.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import com.fftw.util.Util;

@Transactional
public class JdbcTimeSeriesDaoImpl extends JdbcDaoSupport implements JdbcTimeSeriesDao
{
    // TODO Maybe set to be read uncommitted for the big select
    public void bulkInsert (String bcpFile)
    {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        jdbcTemplate
            .execute("create table #time_series_upload(time_series_id INTEGER NOT NULL, data_source_id INTEGER NOT NULL, "
                + "observation_time CHAR(30) NOT NULL, observation_value FLOAT NOT NULL, "
                + "PRIMARY KEY(time_series_id, data_source_id, observation_time))");

        jdbcTemplate.execute("bulk insert #time_series_upload from '" + bcpFile + "'");

        jdbcTemplate
            .execute("update time_series_data set observation_value = #time_series_upload.observation_value "
                + "from #time_series_upload where "
                + "time_series_data.time_series_id = #time_series_upload.time_series_id and "
                + "time_series_data.data_source_id = #time_series_upload.data_source_id and "
                + "time_series_data.observation_time = #time_series_upload.observation_time");

        jdbcTemplate.execute("insert into time_series_data select * from #time_series_upload "
            + "where not exists (select 1 from time_series_data where "
            + "time_series_data.time_series_id = #time_series_upload.time_series_id and "
            + "time_series_data.data_source_id = #time_series_upload.data_source_id and "
            + "time_series_data.observation_time = #time_series_upload.observation_time)");

        jdbcTemplate.execute("drop table #time_series_upload");
    }

    public List<Long> getCDSArbitrationTimeSeriesIDs (Calendar calArbitration)
    {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        Date dateArbitration = calArbitration.getTime();
        Date dateArbitration1 = Util.addWeekDay(calArbitration, -1).getTime();
        Object[] arrParameters = new Object[]
        {
            dateArbitration1, dateArbitration
        };
        // Populate cds_ticker_universe
        jdbcTemplate.update("delete from cds_ticker_universe");
        jdbcTemplate
            .update(
                "insert into cds_ticker_universe select distinct "
                    + "attribute_value_id as cds_ticker_id from time_series_attribute_map where "
                    + "attribute_id = 17 and time_series_id in (select time_series_id from time_series_data where time_series_id in ("
                    + "select tsam1.time_series_id from time_series_attribute_map tsam1 where tsam1.attribute_id = 22 "
                    + "and tsam1.attribute_value_id in (219)) and data_source_id = 7 and "
                    + "observation_time >= ? and observation_time < ?)", arrParameters);

        // get the the time series Id for arbitration
        List listQueryResult = jdbcTemplate
            .queryForList(
                "select time_series_id from time_series_data where time_series_id in ("
                    + "select tsam1.time_series_id from time_series_attribute_map tsam1, time_series_attribute_map tsam2 "
                    + "where tsam1.time_series_id = tsam2.time_series_id and tsam1.attribute_id = 22 "
                    + "and tsam1.attribute_value_id in (219) and tsam2.attribute_id = 1 and "
                    + "tsam2.attribute_value_id in (18)) and data_source_id = 7 and "
                    + "observation_time >= ? and observation_time < ?", arrParameters);
        List<Long> listTimeSeriesIDs = new ArrayList<Long>();
        for (Iterator i = listQueryResult.iterator(); i.hasNext();)
        {
            Map mapRow = (Map)i.next();
            listTimeSeriesIDs.add(new Long(mapRow.get("time_series_id").toString()));
        }
        return listTimeSeriesIDs;
    }

    // see what tickers got created and got dropped
    public Map<String, List<String>> getCDSArbitrationTickerDiff (Calendar calArbitration)
    {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        Date dateArbitration = calArbitration.getTime();
        Date dateArbitration1 = Util.addWeekDay(calArbitration, -1).getTime();
        Object[] arrParameters = new Object[]
        {
            dateArbitration, dateArbitration1, dateArbitration1, dateArbitration
        };
        List listQueryResult = jdbcTemplate
            .queryForList(
                "select distinct 'created' as diffType, ticker from T_Markit_Cds_Composite_Hist where date = ? except "
                    + "select distinct 'created', ticker from T_Markit_Cds_Composite_Hist where date = ? "
                    + "union all select distinct 'dropped' as diffType, ticker from T_Markit_Cds_Composite_Hist where date = ? except "
                    + "select distinct 'dropped', ticker from T_Markit_Cds_Composite_Hist where date = ? order by 1",
                arrParameters);
        Map<String, List<String>> mapTickerDiff = new LinkedHashMap<String, List<String>>();
        for (Iterator i = listQueryResult.iterator(); i.hasNext();)
        {
            Map mapRow = (Map)i.next();
            String strDiffType = mapRow.get("diffType").toString();
            List<String> listTickers = mapTickerDiff.get(strDiffType);
            if (listTickers == null)
            {
                listTickers = new ArrayList<String>();
                mapTickerDiff.put(strDiffType, listTickers);
            }
            listTickers.add(mapRow.get("ticker").toString());
        }
        return mapTickerDiff;
    }
}