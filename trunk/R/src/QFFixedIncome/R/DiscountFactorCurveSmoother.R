constructor("DiscountFactorCurveSmoother", function(dfCurve = NULL,...)
{
    this <- extend(RObject(),"DiscountFactorCurveSmoother",
        .dfCurve = dfCurve,
        .smoothingFreq = 3, # ensure that the returned discount factor curve has points with gaps no greater than about 3 months
        .method = 1, # linear forward rates
        .acc = 2, # output spot rates: actual/360
        .freq = 1 # output spot rates: annual compounding
    )
    return(this)
})

method("getSmoothedDiscountFactorCurve","DiscountFactorCurveSmoother",function(this,...)
{
    result <- fincad("aaDFCurve_SmoothFwdRate2",
        df_crv = this$.dfCurve,
        freq = this$.smoothingFreq,
        d_e = 0, # start smoothing from the first date
        d_t = 0, # stop smoothing on the last date
        meth_smooth = this$.method,
        rate_basis = this$.freq,
        acc_rate = this$.acc,
        table_type = 3 # returns date/discount factor/spot rate
    )
    result <- data.frame(result)
    colnames(result) <- c("date","df","spot")
    result[,1] = fincad.to.POSIXct(result[,1])
    return(result)
})