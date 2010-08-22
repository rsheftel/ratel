# jbourgeois
###############################################################################

library(QFReports)

runDate 	<- dateTimeFromArguments(commandArgs())
filename	<- squish(dataDirectory(),'/STProcess/RightEdge/Portfolio/',format(runDate,'%Y%m%d'),'/reports/','DailyPnlStructuralChangeReport')
curve.filename <- squish(dataDirectory(),'STProcess/RightEdge/Portfolio/',format(runDate,'%Y%m%d'),'/curves')

this <- PnlStructuralChange('AllSystemsQ',curve.filename)
curvesLive <- this$getCurves('live')
curvesBacktest <- this$getCurves('backtest')

ksColnames <- c('Group','PValue','NObsLive','NObsBacktest','ObsRatio (%)') 

report <- PnlStructuralChangeReport('PnlStructuralChange',filename)
report$underlying()$addTitle('Pnl Structural Change Report')
report$underlying()$addPlotLink("Plots")
report$addKSGrid('KS Grid - H0: Live = Backtest',curvesLive,curvesBacktest,'two.sided',ksColnames)
report$addKSGrid('KS Grid - H0: Live under-performed Backtest',curvesLive,curvesBacktest,'less',ksColnames)
report$addKSGrid('KS Grid - H0: Live out-performed Backtest',curvesLive,curvesBacktest,'greater',ksColnames)
report$addPlot(curvesLive,curvesBacktest)
report$underlying()$closeConnection()
report$underlying()$email('Pnl Structural Change Report - ',runDate)