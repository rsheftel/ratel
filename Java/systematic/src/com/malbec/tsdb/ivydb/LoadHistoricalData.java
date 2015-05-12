package com.malbec.tsdb.ivydb;

import static transformations.Constants.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import tsdb.*;
import db.*;
import file.*;

public class LoadHistoricalData {

    public static void main(String[] args) {
        doNotDebugSqlForever();
        QDirectory directory = new QDirectory(dataDirectory()).directory("eknell_test/ivydb_2009/time_series_data");
        QDirectory done = new QDirectory(dataDirectory()).directory("eknell_test/ivydb_2009/loaded");
        DataSource source = source("ivydb_2009");
        for (QFile file : directory.files(".*\\.csv")) {
            Csv csv = new Csv(file, true);
            TimeSeries series = series(second(csv.columns()));
            Observations observations = new Observations();
            for (List<String> record : csv.records())
                observations.set(date(first(record)), Double.parseDouble(second(record)));
            series.write(source, observations);
            Db.commit();
            file.moveTo(done);
        }
    }

}
