library(fincad)
library(GSFCore)
library(RUnit)
library(RJDBC)
library(GSFAnalytics)
library(STO)
library(Live)

outputFieldList <- c(
        LastZScore = "LastZScore",
        LastResidual = "LastResidual",
        LastSd = "LastSd",        
        LastR2Adj = "LastR2Adj",                
        LastR2 = "LastR2",                       
        LastAlphaPVal = "LastAlphaPVal", 
        LastBetaPVal = "LastBetaPVal",         
        LastAlpha = "LastAlpha",                
        LastBeta = "LastBeta",                        
        LastSlope = "LastSlope",                              
        LastHedgeLag = "LastHedgeLag",                                       
        LastHedgeLead = "LastHedgeLead",                                              
        LastDailyTri = "LastDailyTri",                                                    
        LastTri = "LastTri",
        LastAlphaFactorRank = "LastAlphaFactorRank",
        LastBetaFactorRank = "LastBetaFactorRank",        
        Timestamp = "Timestamp",
        HighPrice = "HighPrice",
        LastPrice = "LastPrice",
        LastVolume = "LastVolume",
        LowPrice = "LowPrice",
        OpenPrice = "OpenPrice",
        MDTimestamp = "Timestamp"

)

trans.tsdb.tsdb <- RollingRegressionTransformation(
        market = "PTT10.ESIG",
        nameLead = "tsdb:gm_snrfor_usd_mr_tri_daily_5y",nameLag = "tsdb:f_snrfor_usd_mr_tri_daily_5y",
        gissingSeriesLead = "CDS:||:gm_snrfor_usd_mr_5y:||:LastPrice",gissingSeriesLag = "CDS:||:f_snrfor_usd_mr_5y:||:LastPrice",
        updateRuleLead = "percent:0.2", updateRuleLag = "absolute:1",
        regressionWindow = 20
)

trans.csi.tsdb <- RollingRegressionTransformation(
        market = "PTT20.ESIG",
        nameLead = "csi:ES",nameLag = "tsdb:cdx-na-ig_tri_daily_5y_otr",
        gissingSeriesLead = "FUTURE:||:ES.1C:||:LastPrice",gissingSeriesLag = "CDS:||:CDXNAIG_snrfor_usd_xr_5y:||:LastPrice",
        updateRuleLead = "percent:0.2", updateRuleLag = "absolute:1",
        regressionWindow = 20
)

trans.tsdb.csi <- RollingRegressionTransformation(
        market = "PTT10.ESIG",
        nameLead = "tsdb:cdx-na-ig_tri_daily_5y_otr",nameLag = "csi:ES",
        gissingSeriesLead = "CDS:||:CDXNAIG_snrfor_usd_xr_5y:||:LastPrice",gissingSeriesLag = "FUTURE:||:ES.1C:||:LastPrice",
        updateRuleLead = "percent:0.2", updateRuleLag = "absolute:1",
        regressionWindow = 20
)
              
trans.tsdb.csi$setDate(as.POSIXct("2007/11/30"))
trans.csi.tsdb$setDate(as.POSIXct("2007/11/30"))
trans.tsdb.tsdb$setDate(as.POSIXct("2007/11/30"))

testRollingRegressionInputsOutputs <- function() {
       
    checkInherits(trans.tsdb.csi, "RollingRegressionTransformation")
    
    checkSame(list(
            SeriesDefinition("CDS","CDXNAIG_snrfor_usd_xr_5y", "LastPrice"), 
            SeriesDefinition("FUTURE","ES.1C", "LastPrice")
        ),
        noNames(trans.tsdb.csi$inputs())
    )
    checkSame(c(
            SeriesDefinition$from("ROLLINGREGRESSION", "PTT10.ESIG",outputFieldList[1:17]),
            SeriesDefinition$from("MARKETDATA", "PTT10.ESIG",outputFieldList[18:23])
        ),
        trans.tsdb.csi$outputs()
    )
    
    checkSame(list(
            SeriesDefinition("FUTURE","ES.1C", "LastPrice"),
            SeriesDefinition("CDS","CDXNAIG_snrfor_usd_xr_5y", "LastPrice")
        ),
        noNames(trans.csi.tsdb$inputs())
    )
    checkSame(
        c(
            SeriesDefinition$from("ROLLINGREGRESSION", "PTT20.ESIG",outputFieldList[1:17]),
            SeriesDefinition$from("MARKETDATA", "PTT20.ESIG",outputFieldList[18:23])
        ),
        trans.csi.tsdb$outputs()
    )
}

testDates <- function() {
    checkSame(trans.tsdb.csi$.date, as.POSIXct("2007/11/30"))
    checkSame(trans.tsdb.csi$.lastCloseDate, as.POSIXct("2007/11/29"))    
}

test.isBigChange <- function(){
    checkSame(TRUE,RollingRegressionTransformation$isBigChange(50,60,"absolute:5"))
    checkSame(TRUE,RollingRegressionTransformation$isBigChange(50,40,"absolute:5"))    
    checkSame(FALSE,RollingRegressionTransformation$isBigChange(50,54,"absolute:5"))    
    checkSame(TRUE,RollingRegressionTransformation$isBigChange(100,111,"percent:0.1"))    
    checkSame(TRUE,RollingRegressionTransformation$isBigChange(100,89,"percent:0.1"))        
    checkSame(FALSE,RollingRegressionTransformation$isBigChange(100,109,"percent:0.1"))        
    shouldBombMatching(RollingRegressionTransformation$isBigChange(100,109,"junk:0.1"), "Unknown update rule")
}

test.initialize <- function(){
    checkSame("SUCCESS",trans.tsdb.tsdb$initialize())
    checkSame(trans.tsdb.tsdb$.lastLead,-0.8789358)
    checkSame(trans.tsdb.tsdb$.lastLag,-0.677927469756995)
    checkSame(class(trans.tsdb.tsdb$.lastCloseTri),"numeric")
}

test.MissingCloseData <- function() {
    trans.Bad <- RollingRegressionTransformation(
        market = "junk",
        nameLead = "tsdb:gm_snrfor_usd_mr_tri_daily_5y",nameLag = "tsdb:f_snrfor_usd_mr_tri_daily_5y",
        gissingSeriesLead = "CDS:||:gm_snrfor_usd_mr_5y:||:LastPrice",gissingSeriesLag = "CDS:||:f_snrfor_usd_mr_5y:||:LastPrice",
        updateRuleLead = "percent:0.2", updateRuleLag = "absolute:1",
        regressionWindow = 20
    )

    shouldBombMatching(trans.Bad$initialize(),"no time series for junk_rolling_regression_1.0_tri")
    
    trans.Bad <- RollingRegressionTransformation(
        market = "PTT10.ESIG",
        nameLead = "tsdb:gmm_snrfor_usd_mr_tri_daily_5y",nameLag = "tsdb:f_snrfor_usd_mr_tri_daily_5y",
        gissingSeriesLead = "CDS:||:gm_snrfor_usd_mr_5y:||:LastPrice",gissingSeriesLag = "CDS:||:f_snrfor_usd_mr_5y:||:LastPrice",
        updateRuleLead = "percent:0.2", updateRuleLag = "absolute:1",
        regressionWindow = 20
    )
    shouldBombMatching(trans.Bad$initialize(),"no time series for gmm_snrfor_usd_mr_tri_daily_5y")    
}

test.getCloseData <- function(){
    result <- trans.tsdb.tsdb$getCloseData()  
    
    checkShape(result, 21, 2, colnames = c("closeLead", "closeLag"))
    checkSameLooking(first(index(result)),as.POSIXct("2007-10-30"))
    checkSameLooking(last(index(result)),as.POSIXct("2007-11-29"))
    checkSame(last(result[,"closeLag"]),-0.677927469756995)
    checkSame(first(result[,"closeLead"]),0.0379970308639912)
    
    trans.tsdb.tsdb$.regressionWindow <- 10000000
    on.exit( { trans.tsdb.tsdb$.regressionWindow <- 20 }  )
    shouldBombMatching(trans.tsdb.tsdb$getCloseData(),"Not enough data to process transformation")
}


testUpdate <- function() {

    trans.tsdb.tsdb <- RollingRegressionTransformation(
        market = "PTT10.ESIG",
        nameLead = "tsdb:gm_snrfor_usd_mr_tri_daily_5y",nameLag = "tsdb:f_snrfor_usd_mr_tri_daily_5y",
        gissingSeriesLead = "CDS:||:gm_snrfor_usd_mr_5y:||:LastPrice",gissingSeriesLag = "CDS:||:f_snrfor_usd_mr_5y:||:LastPrice",
        updateRuleLead = "percent:0.2", updateRuleLag = "absolute:1",
        regressionWindow = 20
    )

    trans.tsdb.tsdb$setDate(as.POSIXct("2007/11/30"))

    # Not triggering but initialized
    
    inputs <- list(
        gissingSeriesLead = "CDS:||:gm_snrfor_usd_mr_5y:||:LastPrice:||:89.33:||:TRUE",
        gissingSeriesLag = "CDS:||:f_snrfor_usd_mr_5y:||:LastPrice:||:88.63:||:TRUE"
    )
    
    outputs <- trans.tsdb.tsdb$update(inputs, quiet=FALSE)
    expectedResidual <- "ROLLINGREGRESSION:||:PTT10.ESIG:||:LastResidual:||:0.016938318318"
    checkSame(outputs[[8]], expectedResidual)

    # Triggering only lead
    
    inputs <- list(
        gissingSeriesLead = "CDS:||:gm_snrfor_usd_mr_5y:||:LastPrice:||:150:||:TRUE",
        gissingSeriesLag = "CDS:||:f_snrfor_usd_mr_5y:||:LastPrice:||:88.63:||:FALSE"
    )
    
    outputs <- trans.tsdb.tsdb$update(inputs, quiet=FALSE)
    expectedResidual <- "ROLLINGREGRESSION:||:PTT10.ESIG:||:LastResidual:||:-0.073972657687"
    checkSame(outputs[[8]], expectedResidual)
    
    # Triggering only lag
    
    inputs <- list(
        gissingSeriesLead = "CDS:||:gm_snrfor_usd_mr_5y:||:LastPrice:||:150:||:TRUE",
        gissingSeriesLag = "CDS:||:f_snrfor_usd_mr_5y:||:LastPrice:||:90:||:TRUE"
    )
    
    outputs <- trans.tsdb.tsdb$update(inputs, quiet=FALSE)
    expectedResidual <- "ROLLINGREGRESSION:||:PTT10.ESIG:||:LastResidual:||:-0.072053736148"
    checkSame(outputs[[8]], expectedResidual)

    # Unchanged
    
    outputs <- trans.tsdb.tsdb$update(inputs, quiet=FALSE)
    checkSame(outputs,list())
  
    # changing lag but not enough to trigger recalc
    
    inputs <- list(
        gissingSeriesLead = "CDS:||:gm_snrfor_usd_mr_5y:||:LastPrice:||:150:||:TRUE",
        gissingSeriesLag = "CDS:||:f_snrfor_usd_mr_5y:||:LastPrice:||:90.9:||:TRUE"
    )
    
    outputs <- trans.tsdb.tsdb$update(inputs, quiet=FALSE)
    checkSame(outputs,list())
    
    # changing enough to trigger recalc
    
    inputs <- list(
         gissingSeriesLead = "CDS:||:gm_snrfor_usd_mr_5y:||:LastPrice:||:150:||:TRUE",
         gissingSeriesLag = "CDS:||:f_snrfor_usd_mr_5y:||:LastPrice:||:91.1:||:TRUE"
    )
    
    outputs <- trans.tsdb.tsdb$update(inputs, quiet=FALSE)
    expectedResidual <- "ROLLINGREGRESSION:||:PTT10.ESIG:||:LastResidual:||:-0.070512996227"    
    checkSame(outputs[[8]], expectedResidual)
}

testESTY <- function()
{
    futuresDataTest <- RollingRegressionTransformation(
        market = "PTT10.ESTY",
        nameLead = "bbg:es.1c",nameLag = "bbg:ty.1c",
        gissingSeriesLead = "MARKETDATA:||:ES.1C:||:LastPrice",gissingSeriesLag = "MARKETDATA:||:TY.1C:||:LastPrice",
        updateRuleLead = "percent:0.005", updateRuleLag = "percent:0.005",
        regressionWindow = 20,
        timeStamp = "16:15:00"
    )	
	
    #futuresDataTest$setDate(as.POSIXct("2009/07/14"))

    inputs <- list(
        gissingSeriesLag = "MARKETDATA:||:TY.1C:||:LastPrice:||:117.718:||:TRUE",
        gissingSeriesLead = "MARKETDATA:||:ES.1C:||:LastPrice:||:901.5:||:TRUE"
    )
    
    #outputs <- futuresDataTest$update(inputs, quiet=FALSE)	
}



