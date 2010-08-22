library("GSFAnalytics")
library("STO")

STODirectory <- "V:/Market Systems/Linked Market Systems/FX/StrategyFXCarry/STO"

Strategy <- "FXCarry"
StrategyVersion <- "1.0" 
siv <- SIV(Strategy,"daily",StrategyVersion)
STO_id <- "FXCarryV2"
Interval <- "Daily"
param.space <- ParameterSpace(
LeadBars = c(100,100,0),
StopLoss = c(5000,13000,1000),
Trigger = c(0.4,0.75,0.05),
TriggerCushion = c(0.1,0.1,0),
RecoveryAmount = c(5000,19000,1000),
RecoveryPeriod = c(10,100,10),
MaxTrigger = c(3.5,3.5,0),
TradeSize = c(1,1,0)
)

fileName <-  paste(STODirectory,"/",STO_id,"/Parameters/",Strategy,"_",StrategyVersion,"_",Interval,".csv",sep="")
print(fileName)

param.space$writeCSV(fileName)


