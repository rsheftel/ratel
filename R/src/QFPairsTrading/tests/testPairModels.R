## Test file for the PairModels object
library(QFPairsTrading)


rm(list = ls())

test.PairModels.constructor <- function(){
    this <- PairModels()
    assert(all(c("PairModels","PairRollingRegression") %in% class(this)))
}
