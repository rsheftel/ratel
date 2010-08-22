constructor("RollingRegressionBuilder", function(
    window,
    generatePDF = FALSE,
    pathPDF = "u:/Market Systems/",
    mfrowPDF = c(3,3),
	weighting = NULL
){
   this <- extend(RObject(), "RollingRegressionBuilder")
   if(!inStaticConstructor(this)){
        needs(window = "numeric")
        this$.window = window
        this$.generatePDF = generatePDF
        this$.pathPDF = pathPDF
        this$.mfrowPDF = mfrowPDF
        this$.countPDF = 0
		this$.weighting = weighting		
   }   
   this  
})

method("useWeights", "RollingRegressionBuilder", function(this,...)
{
	!is.null(this$.weighting)
})

method("runLinearModel", "RollingRegressionBuilder", function(this,dataZoo,stringFormula,...)
{
    needs(dataZoo = "zoo",stringFormula = "character")
    
    if(this$.window > NROW(dataZoo))throw("Not enough data for the rolling window")
    
    initialDataZoo <- dataZoo
    
    for (i in this$.window:NROW(dataZoo)){
		startInt <- 1 + i - this$.window
		endInt <- i		
		if(this$useWeights()){
			dataZoo <- initialDataZoo[max(1,1 + endInt - 4 * this$.window):endInt,]		
		}else{
			dataZoo <- initialDataZoo[startInt:endInt,]
		}

		olsResult <- lm(as.formula(stringFormula),y = TRUE,x = TRUE)
		
		if(this$useWeights()){
			weights <- this$getWeights(NROW(olsResult$residuals))
			olsResult <- lm(as.formula(stringFormula),y = TRUE,x = TRUE,weights = weights)
		}
		olsSummary <- summary(olsResult)

        residualZoo <- this$calcResidual(olsSummary) 
        nbFactors <- NCOL(olsResult$x)
        
        if(i == this$.window){
			if(this$useWeights()){
				nres <- i - NROW(residualZoo)			
			}else{
				nres <- this$.window - NROW(residualZoo)					
			}            			
            if(nres != 0)warning(paste("Lost ",nres," observation(s) in model specification in RollingRegressionBuilder$runLinearModel()",sep = ""))
			this$.outputObj <- RegressionOutputs(regressionDates = index(initialDataZoo)[this$.window:NROW(initialDataZoo)],nbFactors = nbFactors)
        }

        coeff <- this$calcCoeff(olsSummary) 
        r2 <- this$calcR2(olsResult,residualZoo)

        if(NROW(coeff)!=0 && !is.na(olsSummary$r.squared) && !is.na(r2)){       
            (this$.outputObj)$storeOneDate(startInt,".coefficients",coeff[,1])
            (this$.outputObj)$storeOneDate(startInt,".pValues",coeff[,4])        
            this$.outputObj$storeOneDate(startInt,".r2Adj",this$calcR2Adj(olsSummary,r2))
            this$.outputObj$storeOneDate(startInt,".r2",r2)
            this$.outputObj$storeOneDate(startInt,".residual",last(residualZoo))
            this$.outputObj$storeOneDate(startInt,".sd",this$calcSigma(olsSummary))
            this$.outputObj$storeOneDate(startInt,".zScore",(this$.outputObj$.residual[startInt] - mean(residualZoo))/this$.outputObj$.sd[startInt]) 
            this$.outputObj$storeOneDate(startInt,".factorRank",this$calcFactorRank(olsResult))
            
            if(this$.generatePDF && nbFactors == 2)this$exportBivariatePDF(dataZoo,nbFactors,olsResult$x[,nbFactors],olsResult$y,coeff,this$.outputObj$.sd[startInt],r2)
 
        }else{
            warning(paste("No fit on ",last(index(dataZoo)),sep = ""))
        }
    }
    
    this$.countPDF <- 0
    try(dev.off(),silent = TRUE)
    
    return(this$.outputObj)
})

method("getWeights", "RollingRegressionBuilder", function(this,nbDates,...)
{  
	assert(this$.weighting %in% c('halfLife'))
	(0.5^(1/this$.window))^(nbDates:1)
})


######################################### Transformed Data ##################################################

method("calcR2", "RollingRegressionBuilder", function(this,olsResult,residualZoo,...)
{  
    cor(olsResult$y,olsResult$y-residualZoo)^2
})
                
method("calcResidual", "RollingRegressionBuilder", function(this,olsSummary,...)
{
    olsSummary$residual
})

method("calcCoeff", "RollingRegressionBuilder", function(this,olsSummary,...)
{  
    olsSummary$coefficients
})

method("calcSigma", "RollingRegressionBuilder", function(this,olsSummary,...)
{  
    olsSummary$sigma
})

method("calcR2Adj", "RollingRegressionBuilder", function(this,olsSummary,r2,...)
{  
    olsSummary$adj.r.squared
})

method("calcFactorRank", "RollingRegressionBuilder", function(this,olsResult,...)
{  
    factorRank <- NULL
    for(j in 1:NCOL(olsResult$x))factorRank <- c(factorRank,match(last(olsResult$x[,j]),sort(olsResult$x[,j])))
})

######################################### PDF Export function ######################################################

method("exportBivariatePDF", "RollingRegressionBuilder", function(this,dataZoo,nbFactors,x,y,coeff,sigma,r2,...)
{
    nbFactors <- 2
    dates <- as.character(index(dataZoo))
    nDates <- NROW(dates)
    coeffs <- coeff[,1]
    pch <- 23
    sd <- sigma

    if(dev.cur()==1){
        pdf(
            paste(this$.pathPDF,last(dates),"_",this$.window,".pdf",sep = ""),
            paper="special",width=10,height=10
        )
        this$.countPDF = 0
        par(mfrow = this$.mfrowPDF)
    }

        main <- paste(last(dates),"(",'OLS',"): R2 = ",round(r2*100,0),"%, Slope = ",round(coeffs[nbFactors],3),sep = "")
        plot(x,y,type="n",ylab = "Y",xlab = "X",main = main)
        for (i in 1:(nDates-1)){       
            if (i < nDates/4){
                points(x[i],y[i], pch = pch, col = "black")
            }else if(i < nDates/2){	
                points(x[i],y[i], pch = pch, col = "grey")
            }else if(i < 3*nDates/4){
                points(x[i],y[i], pch = pch, col = "orange")
            }else{
                points(x[i],y[i], pch = pch, col = "red")
            }
        }
        points(x[nDates],y[nDates], pch = pch, bg = "red", cex = 2)
        abline(coeffs[1],coeffs[2])
        abline(coeffs[1] + sd,coeffs[2],col = "grey")
        abline(coeffs[1] - sd,coeffs[2],col = "grey")
        
    this$.countPDF <- this$.countPDF + 1
    
    if(this$.countPDF == last(cumprod(this$.mfrowPDF)))dev.off()
})