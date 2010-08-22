constructor("TermStructure", function() {
    this <- extend(Object(), "TermStructure")
})

TermStructure$cds <- c("6m","1y","2y","3y","4y","5y","7y","10y","15y","20y","30y")
TermStructure$irs <- c("18m", "2y", "3y", "4y","5y","6y","7y","8y","9y","10y","12y","15y","20y","25y","30y","40y")
TermStructure$irs_eur <- c("18m", "2y", "3y", "4y","5y","6y","7y","8y","9y","10y","12y","15y","20y","25y","30y")
TermStructure$libor <- c("on","1w","2w","1m","2m","3m","4m","5m","6m","7m","8m","9m","10m","11m","12m")
TermStructure$euribor <- c("on","1w","2w","1m","2m","3m","4m","5m","6m","7m","8m","9m","10m","11m","12m")
TermStructure$us_treasury <- c("2y","5y","10y","30y")
TermStructure$all <- unique(c(TermStructure$libor,"18m","1y",TermStructure$irs))