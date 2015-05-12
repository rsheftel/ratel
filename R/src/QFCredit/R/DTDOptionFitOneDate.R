constructor("DTDOptionFitOneDate", function(
	baseDate,tickerListDTD,pdfPath = NULL
,...){    
	library(quantreg)
    this <- extend(RObject(),"DTDOptionFitOneDate") 
    if(!inStaticConstructor(this)){	  
        this$.baseDate <- as.POSIXct(baseDate)
		this$.refDate <- businessDaysAgo(1,this$.baseDate)
		this$.refDateBuffer <- businessDaysAgo(5,this$.baseDate)
		this$.settleDate <- this$.baseDate
		this$.expiryDate <- Period$months(12 * 5)$advance(this$.baseDate)
		this$.dividendAnn <- 0
		this$.pathPDF <- pdfPath
		this$.tickerListDTD <- tickerListDTD
		this$.minDTD <- -6
		this$.maxDTD <- 6
		this$.epsilon <- 0.01
		this$.minOptionPrice <- 0.0001
		this$.maxOAS <- 0.15
		this$.minDataPoints <- 50
		this$.maxIt <- 100
		this$.seedVol <- 0.1
		this$.seedStrike <- 2
		this$.gravityPoints <- 20
		this$.maxVolMove <- 0.1
		this$.maxStrikeMove <- 0.5		
    }
    this
})

method("loadDataFromTSDB", "DTDOptionFitOneDate", function(this,dtds=NULL,oas=NULL,rates=NULL,loadData=TRUE,...){
	if(loadData){		
		dtds <- as.numeric(SingleNameCDS$multipleDTDSeries(this$.tickerListDTD,startDate=this$.baseDate,endDate=this$.baseDate))
		oas <- as.numeric(SingleNameCDS$multipleGenericSeries(this$.tickerListDTD,'spread',startDate=this$.baseDate,endDate=this$.baseDate))
		rates <- this$rates()				
	}
	strikes <- as.numeric(SingleNameCDS$dtdStrike(startDate=this$.refDateBuffer,endDate=this$.refDate))
	vols <- as.numeric(SingleNameCDS$dtdVol(startDate=this$.refDateBuffer,endDate=this$.refDate))
	if(NROW(strikes)!=0)strikes <- last(strikes)
	if(NROW(vols)!=0)vols <- last(vols)
	this$importData(dtds,oas,rates,strikes,vols)
})

method("rates", "DTDOptionFitOneDate", function(this,...){
	as.numeric(TimeSeriesDB()$retrieveOneTimeSeriesByName('irs_usd_rate_5y_mid','internal',start=this$.baseDate,end=this$.baseDate)/100)
})

method("importData", "DTDOptionFitOneDate", function(this,dtds,oas,rate,strike,vol,...){
	this$.dtds <- dtds
	this$.oas <- oas
	this$.rate <- rate
	this$.startVol <- vol
	this$.startStrike <- strike			
	if(NROW(this$.startVol)==0)this$.startVol <- this$.seedVol
	if(NROW(this$.startStrike)==0)this$.startStrike <- this$.seedStrike	
	print(squish("Vol: start = ",this$.startVol))
	print(squish("Strike: start = ",this$.startStrike))	
	TRUE
})

method("run", "DTDOptionFitOneDate", function(this,...){
	optiParameters <- this$optimize(this$.startStrike,this$.startVol)
	if(is.null(optiParameters))return(NULL)
	this$fit(optiParameters$Strike,optiParameters$Vol)	
})

method("optimize", "DTDOptionFitOneDate", function(this,startStrike,startVol,...)
{	
	paramStart <- c(startStrike,startVol)
	this$loadFitData()
	if(NROW(this$.dtdsValid)<this$.minDataPoints)return(NULL)
	opti <- optim(paramStart, this$optiFit, control = list(maxit = this$.maxIt,reltol = .Machine$double.eps))	
	list(Strike = opti$par[1],Vol = opti$par[2])	
})

method("optiFit", "DTDOptionFitOneDate", function(this,parameters,...)
{
	badFitValue <- 1000000
	if(any(parameters<=0) | abs(parameters[2] - this$.startVol) > this$.maxVolMove | abs(parameters[1] - this$.startStrike) > this$.maxStrikeMove)
		return(badFitValue)
	optionPrices <- as.numeric(mapply(this$calcFastOptionPrice,this$.dtdsFit,parameters[1],parameters[2])[1,])
	catchError <- try(scaleFit <- rq(this$.oasFit~optionPrices,tau = 0.5),silent = TRUE)
	if(class(catchError) == "try-error")return(NA)
	predicted <- as.numeric(predict(scaleFit,newdata = list(optionPrices)))
	if(any(predicted)<0)return(badFitValue)
	sum(abs(this$.oasFit - predicted))
})

method("calcFastOptionPrice", "DTDOptionFitOneDate", function(this,
	equityPrice,strike,vol
,...){
	fincad("aaBSG",
		price_u = equityPrice,
		ex = strike,
		d_exp = as.character(this$.expiryDate),
		d_v =  as.character(this$.settleDate),
		vlt = vol,
		rate_ann = this$.rate,
		cost_hldg = this$.dividendAnn,
		option_type = 2,
		stat = data.frame(1:8),
		acc_rate = 4,
		acc_cost_hldg = 4
	)
})

method("calcImpliedShiftedDTDFromOAS", "DTDOptionFitOneDate", function(this,oas,alpha,beta,strike,vol,...){
	optionPrice <- (oas - alpha)/beta
	fincad("aaBSG_iu", price = optionPrice,
			ex = strike,
			d_exp = as.character(this$.expiryDate),
			d_v = as.character(this$.settleDate),
			vlt = vol,
			rate_ann = this$.rate,
			cost_hldg = 0,
			option_type = 2
	)
})

method("calcImpliedShiftedDTDFromOptionPrice", "DTDOptionFitOneDate", function(this,optionPrice,strike,vol,...){
	fincad("aaBSG_iu", price = optionPrice,
		ex = strike,
		d_exp = as.character(this$.expiryDate),
		d_v = as.character(this$.settleDate),
		vlt = vol,
		rate_ann = this$.rate,
		cost_hldg = 0,
		option_type = 2
	)
})

method("uploadCreditIndexDelta", "DTDOptionFitOneDate", function(this,indexTicker,...)
{
	spreadTsName <- squish(indexTicker,'_market_spread_5y_otr')
	spread <- as.numeric(TimeSeriesDB()$retrieveOneTimeSeriesByName(spreadTsName,start = this$.baseDate,end = this$.baseDate,data.source = 'internal'))	
	coefficients <- SingleNameCDS$dtdCoefficients(this$.baseDate)
	this$.rate <- this$rates()
	if(NROW(coefficients)==0 || NROW(spread) == 0)return(NULL)
	indexDeltas <- this$getPriceDelta(oas = spread,alpha = coefficients[,'alpha'],beta = coefficients[,'beta'],strike = coefficients[,'strike'],vol = coefficients[,'vol'])
	attr <- as.list(TimeSeriesDB()$lookupAttributesForTimeSeries(spreadTsName))
	attr$quote_convention <- 'dtd_option_fit_delta'
	deltaTsName <- squish(leftStr(spreadTsName,nchar(spreadTsName)-20),attr$quote_convention,'_5y_otr')
	TimeSeriesDB()$createAndWriteOneTimeSeriesByName(zoo(indexDeltas,this$.baseDate),deltaTsName,'internal',attr)		
})

method("getPriceDelta", "DTDOptionFitOneDate", function(this,oas,alpha,beta,strike,vol,...)
{
	optionPrice <- (oas - alpha)/beta	
	minDtd <- this$.epsilon
	retry <- TRUE
	while(retry){
		maxOptionPrice <- this$calcFastOptionPrice(minDtd,strike,vol)[1,]
		if(optionPrice < this$.minOptionPrice) optionPrice <- this$.minOptionPrice
		if(optionPrice > maxOptionPrice) optionPrice <- maxOptionPrice
		error <-try(dtdShifted <- this$calcImpliedShiftedDTDFromOptionPrice(optionPrice,strike,vol),TRUE)
		retry <- class(error) == 'try-error'
		minDtd <- minDtd + 0.05
	}
	this$calcFastOptionPrice(dtdShifted,strike,vol)[2,]	* beta
})

method("shiftedDTDs", "DTDOptionFitOneDate", function(this,dtds = this$.dtds,...)
{
	this$.shift <- 0
	if(any(na.omit(dtds)<0))
		this$.shift <- - min(dtds,na.rm = TRUE) + this$.epsilon
	dtds + this$.shift
})

method("outlierFilter", "DTDOptionFitOneDate", function(this,dtds,oas,...)
{
	dtds >= this$.minDTD & dtds <= this$.maxDTD & oas <= this$.maxOAS
})

method("loadFitData", "DTDOptionFitOneDate", function(this,dtds = this$.dtds,...)
{	
	tickers <- this$.tickerListDTD
	dtdsShifted <- this$shiftedDTDs(dtds)
	oas <- this$.oas
	# tickers on which we can get deltas/rich cheaps
	naFilter <- !is.na(dtdsShifted) & !is.na(oas)
	this$.dtdsValid <- dtdsShifted[naFilter]
	this$.oasValid <- oas[naFilter]
	this$.tickersValid <- tickers[naFilter]
	# tickers that we use for the fit
	dtdsRawValid <- (this$.dtdsValid - this$.shift)
	outlierFilter <- this$outlierFilter(dtdsRawValid,this$.oasValid)
	this$.dtdsFit <- this$.dtdsValid[outlierFilter]
	this$.oasFit <- this$.oasValid[outlierFilter]
	this$.tickersFit <- this$.tickersValid[outlierFilter]
	# Piecewise quantile regression
	.fit <- lprq(this$.dtdsFit,this$.oasFit,h = 1,tau = 0.5,m=this$.gravityPoints)
	this$.dtdsFit <- c(.fit$xx)
	this$.oasFit <- c(.fit$fv)
	validPoints <- this$.dtdsFit > 0 & this$.oasFit > 0
	this$.dtdsFit <- this$.dtdsFit[validPoints]
	this$.oasFit <- this$.oasFit[validPoints]
	list(
		dtdsValid = this$.dtdsValid,oasValid = this$.oasValid,tickersValid = this$.tickersValid,
		dtdsFit = this$.dtdsFit,oasFit = this$.oasFit
	)
})

method("fit", "DTDOptionFitOneDate", function(this,strike,vol,dtds = this$.dtds,oas = this$.oas,...)
{		
	fitList <- this$loadFitData(dtds=dtds)
	optionData <- mapply(this$calcFastOptionPrice,fitList$dtdsValid,strike,vol)[1:2,]
	optionPricesFit <- mapply(this$calcFastOptionPrice,fitList$dtdsFit,strike,vol)[1,]
	optionPrices <- optionData[1,] 	
	scaleFitOLS <- rq(fitList$oasFit~optionPricesFit,tau = 0.5)	
	predictions <- as.numeric(predict(scaleFitOLS,newdata = list(optionPricesFit = optionPrices)))
	rSquare <- cor(fitList$oasValid,predictions)^2	
	alpha <- as.numeric(scaleFitOLS$coef)[1]	
	beta <- as.numeric(scaleFitOLS$coef)[2]
	# Get Value/Price deltas
	# OAS = alpha + beta * optionPrice(DTDShifted,Strike, Vol) so dOAS/dDTD = dOAS/dDTDShifted = beta * optionPrice delta			 					
	deltas.Value <- optionData[2,] * beta	
	deltas.Price <- as.numeric(sapply(fitList$oasValid,function(x){this$getPriceDelta(x,alpha,beta,strike,vol)}))		
	resList <- list(
		tickers = fitList$tickersValid, dtds = fitList$dtdsValid, oas = fitList$oasValid,
		r2 = rSquare,squares = sum(abs(fitList$oasValid - predictions)),
		fv = predictions,richCheaps = (fitList$oasValid-predictions),deltasValue =deltas.Value,deltasPrice =deltas.Price,			
		alpha = alpha,beta = beta,strike = strike,vol = vol,shift = this$.shift		
	)
	if(!is.null(this$.pathPDF))this$plotFit(fitList,resList)
	resList
})

method("plotFit", "DTDOptionFitOneDate", function(this,fitList,resList,...)
{
	plotFunc <- function(dtds,oas,fv,resList,title){
		plot(dtds,oas,ylab = 'oas',xlab = 'dtd',main = title)
		lineData <- cbind(dtds,fv)
		lines(lineData[order(lineData[,1]),],col = 2)
		abline(v = resList$strike)
	}
	bools <- this$outlierFilter(fitList$dtdsValid,fitList$oasValid)
	pdf(this$.pathPDF,paper="special",width=10,height=10)
		plotFunc(fitList$dtdsValid[bools],fitList$oasValid[bools],resList$fv[bools],resList,'Fit Subset')
		plotFunc(fitList$dtdsValid,fitList$oasValid,resList$fv,resList,'ALL')		
	dev.off()
})