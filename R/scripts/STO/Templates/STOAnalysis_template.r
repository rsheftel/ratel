
library(QFSTO)

#Construct the object with the SystemID
sa <- STOAnalysis(308580)

#Chose run all with a metric list to use for flip reports
sa$runAll(list(QKRatio, QCalmarRatio, QAnnualizedNetProfit))

