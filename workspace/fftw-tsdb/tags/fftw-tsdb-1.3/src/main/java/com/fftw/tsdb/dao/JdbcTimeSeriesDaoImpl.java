package com.fftw.tsdb.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class JdbcTimeSeriesDaoImpl extends JdbcDaoSupport implements JdbcTimeSeriesDao
{
    //TODO maybe need to put primary key after data is inserted
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

}
