
#Create and back populate the following vHedges:


    library(QFMortgage)

	startDate  <- as.POSIXct('1992-01-01')	
	endDate    <- as.POSIXct('2009-05-08')

	hedgesList <- c('vSwapPartials','vNoHedge','vTreasury10y','vFuturesTY','vFuturesTU_TY','vFuturesTU_FV_TY_US')

	programs 	<- c('fncl','fnci')
	sources <- c('model_jpmorgan_bondStudio2008','qfmodel_smithBreedan_vector1.0')

	source <- sources[[2]]
	
	for (program in programs){
		for (coupon in TBA$couponVector(program,'all')){
			tbaTri <- TBATRI(program,coupon)
			tbaTri$setDateRange(dataDate=endDate, startDate=startDate)
			tbaTri$setTsdbSources(tbaDurationSource=source)
			tbaTri$setTBADataFromTsdb()
			tbaTri$setSwapDataFromTsdb()
			tbaTri$setCashTreasuryDataFromTsdb()
			tbaTri$setTreasuryFuturesData()
			tbaTri$calculateTBAContinuousPriceChange()
			tbaTri$calculateTBAContinuousPriceLevel()
			for (hedges in hedgesList){	
				tbaTri$generateTRI(hedgeBasket=hedges)
				tbaTri$uploadTRItoTsdb(hedgeBasket=hedges,source=source,uploadPath='h:/temp/uploadTRI/sb/',uploadMethod='file')
			}
		}
	}
