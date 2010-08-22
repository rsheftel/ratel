library("GSFCore")
tsdb <- TImeSeriesDB()

cal.data <- read.csv("/home/eknell/calendar_data/CHARTAT.CSV", header = FALSE, col.names = c("date", "financial_center", "type"))

holidays <- sapply(unique(as.character(cal.data[["financial_center"]])), function(center) { df <- subset(cal.data, financial_center == center, c("date", "type
")); df <- subset(df, type != "w"); zoo(df[["type"]], order.by = as.POSIXct(strptime(df[["date"]], "%Y%m%d"))) }, simplify = FALSE)

tsdb$createTimeSeries("holidays_lnb", list( instrument = "holiday", financial_center = "lnb" ))
tsdb$createTimeSeries("holidays_nyb", list( instrument = "holiday", financial_center = "nyb" ))

holidays.zoo <- array(holidays, dim = c(2, 1), dimnames = list(c("holidays_lnb", "holidays_nyb"), "financialcalendar"))
tsdb$writeTimeSeries(holidays.zoo)

