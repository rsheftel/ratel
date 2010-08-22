#! /usr/local/bin/r -v

# Annoying, but some of the libraries print to STDOUT when they initialize.  We need STDOUT to be clean.
sink(stderr())
library("GSFCore")
sink()

args <- getopt("gt:s:m:n:d:", argv)

query <- list()

time_series_name <- args$t
data_source_name <- args$s
start_date       <- args$m
end_date         <- args$n

tsdb_name        <- if(!is.null(args$d)) { args$d } else { formals(TimeSeriesDB)$dbname }

tsdb <- TimeSeriesDB(dbname = tsdb_name)

if(args$g) { # get
    ts_array <- tsdb$retrieveTimeSeriesByName(name = time_series_name, data_source = data_source_name, start = start_date, end = end_date)
    for(ts in rownames(ts_array)) {
        for(ds in colnames(ts_array)) {
            if(!is.null(ts_array[[ts, ds]])) {
                cat("@", ts, ",", ds, "\n", sep = "")
                write.csv(ts_array[[ts, ds]])
                cat("\n")
            }
        }
    }
}
