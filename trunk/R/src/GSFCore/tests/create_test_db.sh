#! /bin/sh

cd ../../../SQL
dropdb ${USER}_tsdb
createdb ${USER}_tsdb
psql -q -d ${USER}_tsdb -f create_time_series_db.sql
psql -q -d ${USER}_tsdb -f populate_time_series_db.sql
echo "COPY time_series_data FROM '${PWD}/test_time_series_data.txt';" | psql -q -d ${USER}_tsdb
