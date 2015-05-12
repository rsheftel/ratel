## Test file for the FXCreateForwardSeries object
library("QFFX")

testFXCreateForwardSeries <- function()
{
    conn <- SQLConnection()
    conn$init()

    FXSeriesCreate <- FXCreateForwardSeries(tsdb=TimeSeriesDB())
    test.fxfwd <- FXForwardGeneric(FXCurr$setByCross("kzt/sit"),"mid","2y")
    
    FXSeriesCreate$.tsdb$.deleteTimeSeries("kztsit_2y_rate_mid", are.you.sure = TRUE)
    conn$query("delete from ccy_pair where ccy_pair_name = 'kztsit'")
    checkTrue(!FXSeriesCreate$.tsdb$timeSeriesExists("kztsit_2y_rate_mid"))

    FXSeriesCreate$createFXForwardTimeSeriesIfNotExists(test.fxfwd)
    checkTrue(FXSeriesCreate$.tsdb$timeSeriesExists("kztsit_2y_rate_mid"))

    test.attributes <- FXSeriesCreate$.tsdb$lookupAttributesForTimeSeries(time.series = "kztsit_2y_rate_mid")
    checkEquals(as.character(test.attributes["kztsit_2y_rate_mid", "ccy_pair"]), "kztsit")
    checkEquals(as.character(test.attributes["kztsit_2y_rate_mid", "tenor"]), "2y")
    checkEquals(as.character(test.attributes["kztsit_2y_rate_mid", "quote_side"]), "mid")
    checkEquals(as.character(test.attributes["kztsit_2y_rate_mid", "quote_convention"]), "rate")
    checkEquals(as.character(test.attributes["kztsit_2y_rate_mid", "quote_type"]), "close")
    checkEquals(as.character(test.attributes["kztsit_2y_rate_mid", "instrument"]), "fxfwd")

    FXSeriesCreate$.tsdb$.deleteTimeSeries("kztsit_2y_rate_mid", are.you.sure = TRUE)
    conn$query("delete from ccy_pair where ccy_pair_name = 'kztsit'")
    conn$disconnect()
}
