## Test file for the TermStructure object
library("GSFAnalytics")

testTermStructure <- function()
{
    T <- TermStructure()
    checkEquals(TermStructure$cds,c("6m","1y","2y","3y","4y","5y","7y","10y","15y","20y","30y"))
    checkEquals(TermStructure$irs,c("18m", "2y", "3y", "4y","5y","6y","7y","8y","9y","10y","12y","15y","20y","25y","30y","40y"))
}
