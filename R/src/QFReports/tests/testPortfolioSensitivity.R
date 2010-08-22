library(QFReports)

outputDir <- squish(system.file("testdata", package="QFReports"),'/PortfolioSensitivity')
curvesDir <- squish(outputDir, '/curves')

test.PortfolioSensitivity.constructor <- function(){
	shouldBombMatching(PortfolioSensitivity(), 'groupName is NULL')
	shouldBombMatching(PortfolioSensitivity('TestExposure'), 'curvesDir is NULL')
	shouldBombMatching(PortfolioSensitivity('TestExposure', curvesDir), 'outputDir is NULL')
	checkInherits(PortfolioSensitivity('TestExposure', curvesDir, outputDir), "PortfolioSensitivity")
	checkSame('TestExposure', PortfolioSensitivity('TestExposure', curvesDir, outputDir)$.groupName)
	checkSame(squish(outputDir, '/'), PortfolioSensitivity('TestExposure', curvesDir, outputDir)$.outputDir)
}

test.PortfolioSensitivity.shiftWeightsAndRisks <- function(){
	ps <- PortfolioSensitivity('TestExposure', curvesDir, outputDir)
	
	metricList <- list(AnnualizedNetProfit, KRatio)
	output <- ps$shiftWeights(metricList, 0.1, TRUE)
	checkSame(c('level', 'percent'), names(output))
	checkSame(1147.253338, round(as.numeric(output[[1]][2,1]),6))
	checkSame(0.00133988, round(as.numeric(output[[1]][2,2]),8))
	checkSame(8.973403, round(as.numeric(output[[2]][3,1]),6))
	checkSame(-0.06669964, round(as.numeric(output[[2]][3,2]),8))
	
	resultFile <- squish(outputDir, '/LevelSensitivities.html')
	targetFile <- squish(outputDir, '/LevelSensitivitiesTarget.html')
	hWriterFileMatches(resultFile, targetFile)
	
	shifts <- c(-0.1, 0, 0.1)
	output <- ps$shiftRisk(CalmarRatio, shifts, changes = TRUE, saveFile = TRUE)
	targetRow1 <- c(-0.00570171, 0, 0.00460601)
	targetRow2 <- c(0.00513531, 0, -0.00433652)
	checkSame(targetRow1, round(as.numeric(output[1,]),8))
	
	resultFile <- squish(outputDir, '/RiskShiftSensitivitiesCalmarRatio.html')
	targetFile <- squish(outputDir, '/RiskShiftSensitivitiesCalmarRatioTarget.html')
	hWriterFileMatches(resultFile, targetFile)
	
}
