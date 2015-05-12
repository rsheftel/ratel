TSAvgBarsEvenTrade <- Metric("TSAvgBarsEvenTrade", metricMean)
TSAvgBarsLosTrade <- Metric("TSAvgBarsLosTrade", metricMean)
TSAvgBarWinTrade <- Metric("TSAvgBarWinTrade", metricMean)
TSGrossLoss <- Metric("TSGrossLoss", metricWeightedSum)
TSGrossProfit <- Metric("TSGrossProfit", metricWeightedSum)
TSLargestLosTrade <- Metric("TSLargestLosTrade", metricWeightedMin)
TSLargestWinTrade <- Metric("TSLargestWinTrade", metricWeightedMax)
TSMaxConsecLosers <- Metric("TSMaxConsecLosers", metricMax)
TSMaxConsecWinners <- Metric("TSMaxConsecWinners", metricMax)
TSMaxContractsHeld <- Metric("TSMaxContractsHeld", metricNA)
TSClosePositionProfit <- Metric("TSClosePositionProfit", metricWeightedSum)
TSNumEvenTrades <- Metric("TSNumEvenTrades", metricSum)
TSNumLosTrades <- Metric("TSNumLosTrades", metricSum)
TSNumWinTrades <- Metric("TSNumWinTrades", metricSum)
TSPercentProfit <- Metric("TSPercentProfit", metricTSPercentProfit)
TSTotalTrades <- Metric("TSTotalTrades", metricSum)
TSTotalBarsEvenTrades <- Metric("TSTotalBarsEvenTrades", metricSum)
TSTotalBarsLosTrades <- Metric("TSTotalBarsLosTrades", metricSum)
TSTotalBarsWinTrades <- Metric("TSTotalBarsWinTrades", metricSum)
TSOpenPositionProfit <- Metric("TSOpenPositionProfit", metricWeightedSum)
TSMaxIDDrawDown <- Metric("TSMaxIDDrawDown", metricNA)
TSNetProfit <- Metric("TSNetProfit", metricTSNetProfit)
TSAvgWinTrade <- Metric("TSAvgWinTrade", metricTSAvgWinTrade)
TSAvgLossTrade <- Metric("TSAvgLossTrade", metricTSAvgLossTrade)
TSWinLossRatio <- Metric("TSWinLossRatio", metricTSWinLossRatio)
TSAverageTrade <- Metric("TSAverageTrade", metricTSAverageTrade)
TSLargestLossMultAvgLoss <- Metric("TSLargestLossMultAvgLoss", metricTSLargestLossMultAvgLoss)
TSLargestLossPerGrossLoss <- Metric("TSLargestLossPerGrossLoss", metricTSLargestLossPerGrossLoss)
TSLargestWinMultAvgWin <- Metric("TSLargestWinMultAvgWin", metricTSLargestWinMultAvgWin)
TSLargestWinPerGrossProfit <- Metric("TSLargestWinPerGrossProfit", metricTSLargestWinPerGrossProfit)
TSLargestWinPerNetProfit <- Metric("TSLargestWinPerNetProfit", metricTSLargestWinPerNetProfit)
TSProfitFactor <- Metric("TSProfitFactor", metricTSProfitFactor)
TSExpectancy <- Metric("TSExpectancy", metricTSExpectancy)
TSExpectancyScore <- Metric("TSExpectancyScore", metricTSExpectancyScore)

###########################################################################################
#   ATG metrics
###########################################################################################
NetProfit <- Metric("NetProfit", pnlCurveCalcFunc(sum))
TotalTrades <- Metric("TotalTrades", curveCalcFunc(totalTrades))
WinningTrades <- Metric("WinningTrades", curveCalcFunc(winningTrades))
PercentProfit <- Metric("PercentProfit", metricPercentProfit)
AverageNetProfit <- Metric("AverageNetProfit", metricAverageNetProfit)
AverageBarsInTrade <- Metric("AverageBarsInTrade",curveCalcFunc(averageBarsInTrade))
NetProfitPerBar <- Metric("NetProfitPerBar", pnlCurveCalcFunc(mean))
StandardDeviationPnl <- Metric("StandardDeviationPnl", pnlCurveCalcFunc(sd))
MaxDrawDown <- Metric("MaxDrawDown", pnlCurveCalcFunc(maxDrawDown))
NetProfitMultMaxDrawdown <- Metric("NetProfitMultMaxDrawdown", metricNetProfitMultMaxDrawdown)
AverageDrawDown <- Metric("AverageDrawDown", pnlCurveCalcFunc(averageDrawDown))
AverageDrawDownTime <- Metric("AverageDrawDownTime", pnlCurveCalcFunc(averageDrawDownTime))
AverageDrawDownRecoveryTime <- Metric("AverageDrawDownRecoveryTime", pnlCurveCalcFunc(averageDrawDownRecoveryTime))
TenPercentileDrawDown <- Metric("TenPercentileDrawDown",pnlCurveCalcFunc(tenPercentileDrawDown))
ConditionalTenPercentileDrawDown <- Metric("ConditionalTenPercentileDrawDown",pnlCurveCalcFunc(conditionalTenPercentileDrawDown))
TwentyPercentileDrawDown <- Metric("TwentyPercentileDrawDown",pnlCurveCalcFunc(twentyPercentileDrawDown))
ConditionalTwentyPercentileDrawDown <- Metric("ConditionalTwentyPercentileDrawDown",pnlCurveCalcFunc(conditionalTwentyPercentileDrawDown))
KRatio <- Metric("KRatio", pnlCurveCalcFunc(kRatio))
SortinoRatio <- Metric("SortinoRatio", pnlCurveCalcFunc(sortinoRatio))
OmegaRatio <- Metric("OmegaRatio", pnlCurveCalcFunc(omegaRatio))
UpsidePotentialRatio <- Metric("UpsidePotentialRatio", pnlCurveCalcFunc(upsidePotentialRatio))
AnnualizedNetProfit <- Metric("AnnualizedNetProfit", curveCalcFunc(annualizedNetProfit))
CalmarRatio <- Metric("CalmarRatio", metricCalmarRatio)
DailyStandardDeviation <- Metric("DailyStandardDeviation",curveCalcFunc(dailyStandardDeviation))
DownSideDeviation <- Metric("DownSideDeviation",curveCalcFunc(downSideDeviation))
WeeklyStandardDeviation <- Metric("WeeklyStandardDeviation",curveCalcFunc(weeklyStandardDeviation))
MonthlyStandardDeviation <- Metric("MonthlyStandardDeviation",curveCalcFunc(monthlyStandardDeviation))
MaxDrawDownWeekly <- Metric("MaxDrawDownWeekly",curveCalcFunc(maxDrawDownWeekly))
MaxDrawDownMonthly <- Metric("MaxDrawDownMonthly",curveCalcFunc(maxDrawDownMonthly))
CalmarRatioWeekly <- Metric("CalmarRatioWeekly",metricCalmarRatioWeekly)
CalmarRatioMonthly <- Metric("CalmarRatioMonthly",metricCalmarRatioMonthly)
SharpeRatioDaily <- Metric("SharpeRatioDaily",metricSharpeRatioDaily)
SharpeRatioWeekly <- Metric("SharpeRatioWeekly",metricSharpeRatioWeekly)
SharpeRatioMonthly <- Metric("SharpeRatioMonthly",metricSharpeRatioMonthly)
ConditionalTwentyPercentileCalmarRatio <- Metric("ConditionalTwentyPercentileCalmarRatio",metricConditionalTwentyPercentileCalmarRatio)
ConditionalTenPercentileCalmarRatio <- Metric("ConditionalTenPercentileCalmarRatio",metricConditionalTenPercentileCalmarRatio)
Psi <- Metric("Psi", metricPsi)
PsiNu <- Metric('PsiNu', metricPsiNu)
ConditionalTenPercentileDailyVaR <- Metric("ConditionalTenPercentileDailyVaR",pnlCurveCalcFunc(conditionalTenPercentileDailyVaR))
ConditionalFivePercentileDailyVaR <- Metric("ConditionalFivePercentileDailyVaR",pnlCurveCalcFunc(conditionalFivePercentileDailyVaR))
ConditionalOnePercentileDailyVaR <- Metric("ConditionalOnePercentileDailyVaR",pnlCurveCalcFunc(conditionalOnePercentileDailyVaR))
AnnualizedDailyTurnover <- Metric("AnnualizedDailyTurnover",curveCalcFunc(annualizedDailyTurnover))
###########################################################################################
#   ATG High Frequency metrics
###########################################################################################

HFSignalSquaredMean <- Metric("HFSignalSquaredMean",metricNA)
HFReturnSquaredMean <- Metric("HFReturnSquaredMean",metricNA)
HFProfitBidAskRatio <- Metric("HFProfitBidAskRatio",metricNA)
HFReturnSignalProductMean <- Metric("HFReturnSignalProductMean",metricNA)
HFCountPositiveReturns <- Metric("HFCountPositiveReturns",metricNA)
HFCountNegativeReturns <- Metric("HFCountNegativeReturns",metricNA)
HFCountFlatReturns <- Metric("HFCountFlatReturns",metricNA)
HFCountAllReturns <- Metric("HFCountAllReturns",metricHFCountAllReturns)
HFHitRatio <- Metric("HFHitRatio",metricHFHitRatio)
HFWinLossRatio <- Metric("HFWinLossRatio",metricHFWinLossRatio)
HFReturnSignalR2 <- Metric("HFReturnSignalR2",metricHFReturnSignalR2)
HFReturnSignalIC <- Metric("HFReturnSignalIC",metricHFReturnSignalIC)
HFReturnSignalBeta <- Metric("HFReturnSignalBeta",metricHFReturnSignalBeta)


###########################################################################################
#   RightEdge built in metrics
###########################################################################################
REAverageBarsHeld <- Metric("REAverageBarsHeld", metricNA)
REAverageLosingBarsHeld <- Metric("REAverageLosingBarsHeld", metricNA)
REAverageLoss <- Metric("REAverageLoss", metricNA)
REAverageProfit <- Metric("REAverageProfit", metricNA)
REAverageWin <- Metric("REAverageWin", metricNA)
REAverageWinningBarsHeld <- Metric("REAverageWinningBarsHeld", metricNA)
REAverageWinPct <- Metric("REAverageWinPct", metricNA)
REConsecutiveLosing <- Metric("REConsecutiveLosing", metricNA)
REConsecutiveWinning <- Metric("REConsecutiveWinning", metricNA)
RELongLosingTrades <- Metric("RELongLosingTrades", metricNA)
RELongWinningTrades <- Metric("RELongWinningTrades", metricNA)
RELosingBarsHeld <- Metric("RELosingBarsHeld", metricNA)
RELosingTrades <- Metric("RELosingTrades", metricNA)
RELosingTradesPct <- Metric("RELosingTradesPct", metricNA)
REMaxAccountValue <- Metric("REMaxAccountValue", metricNA)
REMaxConsecutiveLosing <- Metric("REMaxConsecutiveLosing", metricNA)
REMaxConsecutiveWinning <- Metric("REMaxConsecutiveWinning", metricNA)
REMaxDrawDown <- Metric("REMaxDrawDown", metricNA)
REMaxLoss <- Metric("REMaxLoss", metricNA)
REMaxProfit <- Metric("REMaxProfit", metricNA)
RENetProfit <- Metric("RENetProfit", metricWeightedSum)
RENeutralTrades <- Metric("RENeutralTrades", metricNA)
RERealizedGrossLoss <- Metric("RERealizedGrossLoss", metricNA)
RERealizedGrossProfit <- Metric("RERealizedGrossProfit", metricNA)
RERealizedNetProfit <- Metric("RERealizedNetProfit", metricNA)
REShortLosingTrades <- Metric("REShortLosingTrades", metricNA)
REShortWinningTrades <- Metric("REShortWinningTrades", metricNA)
RETotalBarsHeld <- Metric("RETotalBarsHeld", metricNA)
RETotalFinishedTrades <- Metric("RETotalFinishedTrades", metricNA)
RETotalLossPct <- Metric("RETotalLossPct", metricNA)
RETotalProfitPct <- Metric("RETotalProfitPct", metricNA)
RETotalTrades <- Metric("RETotalTrades", metricNA)
RETotalWinPct <- Metric("RETotalWinPct", metricNA)
REUnrealizedNetProfit <- Metric("REUnrealizedNetProfit", metricNA)
REWinningBarsHeld <- Metric("REWinningBarsHeld", metricNA)
REWinningTrades <- Metric("REWinningTrades", metricNA)
REWinningTradesPct <- Metric("REWinningTradesPct", metricNA)

###########################################################################################
#   Q metrics
###########################################################################################

QAverageBarsHeld <- Metric("QAverageBarsHeld", metricNA)
QAverageLosingBarsHeld <- Metric("QAverageLosingBarsHeld", metricNA)
QAverageLoss <- Metric("QAverageLoss", metricQAverageLoss)
QAverageProfit <- Metric("QAverageProfit", metricQAverageProfit)
QAverageWin <- Metric("QAverageWin", metricQAverageWin)
QAverageWinningBarsHeld <- Metric("QAverageWinningBarsHeld", metricNA)
QLongLosingTrades <- Metric("QLongLosingTrades", metricSum)
QLongWinningTrades <- Metric("QLongWinningTrades", metricSum)
QLosingBarsHeld <- Metric("QLosingBarsHeld", metricSum)
QLosingTrades <- Metric("QLosingTrades", metricSum)
QLosingTradesPct <- Metric("QLosingTradesPct", metricQLosingTradesPct)
QMaxDrawdown <- Metric("QMaxDrawdown", pnlCurveCalcFunc(maxDrawDown))
QNetProfit <- Metric("QNetProfit", metricWeightedSum)
QNeutralTrades <- Metric("QNeutralTrades", metricSum)
QRealizedGrossLoss <- Metric("QRealizedGrossLoss", metricWeightedSum)
QRealizedGrossProfit <- Metric("QRealizedGrossProfit", metricWeightedSum)
QRealizedNetProfit <- Metric("QRealizedNetProfit", metricWeightedSum)
QShortLosingTrades <- Metric("QShortLosingTrades",  metricSum)
QShortWinningTrades <- Metric("QShortWinningTrades",  metricSum)
QTotalBarsHeld <- Metric("QTotalBarsHeld",  metricSum)
QTotalFinishedTrades <- Metric("QTotalFinishedTrades",  metricSum)
QTotalTrades <- Metric("QTotalTrades", metricSum)
QUnrealizedNetProfit <- Metric("QUnrealizedNetProfit", metricWeightedSum)
QWinningBarsHeld <- Metric("QWinningBarsHeld", metricSum)
QWinningTrades <- Metric("QWinningTrades", metricSum)
QWinningTradesPct <- Metric("QWinningTradesPct", metricQWinningTradesPct)
QLargestWinningTrade <- Metric("QLargestWinningTrade", metricWeightedMax)
QLargestLosingTrade <- Metric("QLargestLosingTrade", metricWeightedMin)
QMaxConsecutiveWinningTrades <- Metric("QMaxConsecutiveWinningTrades", metricNA)
QMaxConsecutiveLosingTrades <- Metric("QMaxConsecutiveLosingTrades", metricNA)
QAnnualizedNetProfit <- Metric("QAnnualizedNetProfit", metricNA)
QAverageDrawdown <- Metric("QAverageDrawdown", metricNA)
QAverageDrawdownRecoveryTime <- Metric("QAverageDrawdownRecoveryTime", metricNA)
QAverageDrawdownTime <- Metric("QAverageDrawdownTime", metricNA)
QAverageTrade <- Metric("QAverageTrade", metricNA)
QCalmarRatio <- Metric("QCalmarRatio", metricNA)
QConditionalTenPercentileCalmarRatio <- Metric("QConditionalTenPercentileCalmarRatio", metricNA)
QConditionalTwentyPercentileCalmarRatio <- Metric("QConditionalTwentyPercentileCalmarRatio", metricNA)
QConditionalTenPercentileDrawdown <- Metric("QConditionalTenPercentileDrawdown", metricNA)
QConditionalTwentyPercentileDrawdown <- Metric("QConditionalTwentyPercentileDrawdown", metricNA)
QDownsideDeviation <- Metric("QDownsideDeviation", metricNA)
QExpectancy <- Metric("QExpectancy", metricNA)
QExpectancyScore <- Metric("QExpectancyScore", metricNA)
QKRatio <- Metric("QKRatio", metricNA)
QLargestLossPerAverageLoss <- Metric("QLargestLossPerAverageLoss", metricNA)
QLargestLossPerGrossLoss <- Metric("QLargestLossPerGrossLoss", metricNA)
QLargestWinPerAverageWin <- Metric("QLargestWinPerAverageWin", metricNA)
QLargestWinPerGrossProfit <- Metric("QLargestWinPerGrossProfit", metricNA)
QLargestWinPerNetProfit <- Metric("QLargestWinPerNetProfit", metricNA)
QNetProfitPerMaxDrawdown <- Metric("QNetProfitPerMaxDrawdown", metricNA)
QOmegaRatio <- Metric("QOmegaRatio", metricNA)
QSharpeRatio <- Metric("QSharpeRatio", metricNA)
QSharpeRatioMonthly <- Metric("QSharpeRatioMonthly", metricNA)
QSharpeRatioWeekly <- Metric("QSharpeRatioWeekly", metricNA)
QSortinoRatio <- Metric("QSortinoRatio", metricNA)
QStandardDeviation <- Metric("QStandardDeviation", metricNA)
QStandardDeviationMonthly <- Metric("QStandardDeviationMonthly", metricNA)
QStandardDeviationWeekly <- Metric("QStandardDeviationWeekly", metricNA)
QTenPercentileDrawdown <- Metric("QTenPercentileDrawdown", metricNA)
QTradesPerBar <- Metric("QTradesPerBar", metricNA)
QTwentyPercentileDrawdown <- Metric("QTwentyPercentileDrawdown", metricNA)
QUpsidePotentialRatio <- Metric("QUpsidePotentialRatio", metricNA)
QWinLossRatio <- Metric("QWinLossRatio", metricNA)
QTotalSlippage <- Metric("QTotalSlippage", metricNA)
QAverageSlippagePerWinningTrade <- Metric("QAverageSlippagePerWinningTrade", metricNA)
QAverageSlippagePerLosingTrade <- Metric("QAverageSlippagePerLosingTrade", metricNA)


###########################################################################################
#   Metric lists
###########################################################################################
REGeneratedMetrics <- list(
        REAverageBarsHeld,
        REAverageLosingBarsHeld,
        REAverageLoss,
        REAverageProfit,
        REAverageWin,
        REAverageWinningBarsHeld,
        REAverageWinPct,
        REConsecutiveLosing,
        REConsecutiveWinning,
        RELongLosingTrades,
        RELongWinningTrades,
        RELosingBarsHeld,
        RELosingTrades,
        RELosingTradesPct,
        REMaxAccountValue,
        REMaxConsecutiveLosing,
        REMaxConsecutiveWinning,
        REMaxDrawDown,
        REMaxLoss,
        REMaxProfit,
        RENeutralTrades,
        RERealizedGrossLoss,
        RERealizedGrossProfit,
        RERealizedNetProfit,
        REShortLosingTrades,
        REShortWinningTrades,
        RETotalBarsHeld,
        RETotalFinishedTrades,
        RETotalLossPct,
        RETotalProfitPct,
        RETotalTrades,
        RETotalWinPct,
        REUnrealizedNetProfit,
        REWinningBarsHeld,
        REWinningTrades,
        REWinningTradesPct
)

QGeneratedMetrics <- list(
        QAverageBarsHeld,
        QAverageLosingBarsHeld,
        QAverageLoss,
        QAverageProfit,
        QAverageWin,
        QAverageWinningBarsHeld,
        QLongLosingTrades,
        QLongWinningTrades,
        QLosingBarsHeld,
        QLosingTrades,
        QLosingTradesPct,
        QMaxDrawdown,
        QNetProfit,
        QNeutralTrades,
        QRealizedGrossLoss,
        QRealizedGrossProfit,
        QRealizedNetProfit,
        QShortLosingTrades,
        QShortWinningTrades,
        QTotalBarsHeld,
        QTotalFinishedTrades,
        QTotalTrades,
        QUnrealizedNetProfit,
        QWinningBarsHeld,
        QWinningTrades,
        QWinningTradesPct,
        QLargestWinningTrade,
        QLargestLosingTrade,
        QMaxConsecutiveWinningTrades,
        QMaxConsecutiveLosingTrades
)


TSGeneratedMetrics <- list(
        TSAvgBarsEvenTrade,
        TSAvgBarsLosTrade,
        TSAvgBarWinTrade,
        TSGrossLoss,
        TSGrossProfit,
        TSLargestLosTrade,
        TSLargestWinTrade,
        TSMaxConsecLosers,
        TSMaxConsecWinners,
        TSMaxContractsHeld,
        TSClosePositionProfit,
        TSNumEvenTrades,
        TSNumLosTrades,
        TSNumWinTrades,
        TSPercentProfit,
        TSTotalTrades,
        TSTotalBarsEvenTrades,
        TSTotalBarsLosTrades,
        TSTotalBarsWinTrades,
        TSOpenPositionProfit,
        TSMaxIDDrawDown,
        TSNetProfit,
        TSExpectancy
)

NonTSGeneratedMetrics <- list(        
        NetProfit,
        TotalTrades,
        AverageNetProfit,
        PercentProfit,
        MaxDrawDown,
        AverageDrawDown,
        AverageDrawDownTime,
        AverageDrawDownRecoveryTime,
        KRatio,
        SortinoRatio,
        OmegaRatio,
        UpsidePotentialRatio,
        SharpeRatioDaily,
        SharpeRatioWeekly,
        SharpeRatioMonthly,
        TenPercentileDrawDown,
        ConditionalTenPercentileDrawDown,
        TSExpectancyScore,
        Psi
)

HighFrequencyMetrics <- list(
        NetProfit = NetProfit,
        KRatio = KRatio,        
        CalmarRatio = CalmarRatio,
        SharpeRatioDaily = SharpeRatioDaily,
        TotalTrades = TotalTrades, 
        HFProfitBidAskRatio = HFProfitBidAskRatio,
        HFHitRatio = HFHitRatio,    
        HFWinLossRatio = HFWinLossRatio,
        HFReturnSignalR2 = HFReturnSignalR2,
        HFReturnSignalBeta = HFReturnSignalBeta,
        HFReturnSignalIC = HFReturnSignalIC
)

.allMetricsInitialized <- TRUE