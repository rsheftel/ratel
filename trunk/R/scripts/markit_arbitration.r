library("GSFCore")
tsdb <- TimeSeriesDB()
cds.data <- tsdb$retrieveTimeSeriesByAttributeList(list(instrument="cds"), data.source = "internal", start = "2007-08-09")
new.cds.data <- tsdb$retrieveTimeSeriesByName(rownames(cds.data), data.source = "markit", start = "2007-08-10")
new.cds.data.no.times <- apply(new.cds.data, c(1,2), function(x) strip.times.zoo(x[[1]]))
colnames(new.cds.data.no.times) <- "internal"
tsdb$writeTimeSeries(new.cds.data.no.times)
