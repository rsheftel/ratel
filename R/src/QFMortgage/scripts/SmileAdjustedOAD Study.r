# TODO: Add comment
# 
# Author: rsheftel
###############################################################################

#This is a study for smile Adjusted OAD


###########################################################################################
#		Generate the dv01s
###########################################################################################

program = 'fncl'
coupons <- seq(4.5,8.5,0.5)
knotPoints <- seq(-3,3,0.5)
fileDir <- 'h:/Smile OAD Min Max MinDv01/'

#halfLife = 504
#maxPeriods = 504
#lagPeriods = 5
#endDate = "2007-06-01"
#dateRange <- c(as.POSIXct('2007-05-01 15:00:00'))

moddur <- TBAModifiedOAD(program,coupons)
moddur$setITMKnotPoints(knotPoints)
moddur$setCouponDateRange()

#Load in flat file informatino from JPMorgan "New" Models, Current coupon from internal
moddur$setTBAOAD(squish(fileDir,'OAD.csv'))
moddur$setTBASpreadDv01(squish(fileDir,'SpreadDuration.csv'))
moddur$setCC(squish(fileDir,'CurrentCoupon.csv'))
moddur$setOAS(squish(fileDir,'OAS.csv'))
moddur$generateITMOASs()

#Set the date range to start 1/1/1999 and go from merged data
dateRange <- index(moddur$.TBA$oas$itm[['0']][index(moddur$.TBA$oas$itm[['0']]) >= as.POSIXct('1999-01-01 15:00:00')])

knots.list <- list(cc=c(0), updn50=c(-0.5,0,0.5), updn100=c(-1,-0.5,0,0.5,1))
halfLifes <- c(5,20,60,120,252,10000)
maxPeriods <- c(20,60,120,252,504,10000)
lagPeriods <- 5

for (knots in names(knots.list)){
	for (halfLife in halfLifes){
		for (maxPeriod in maxPeriods){
			moddur$setSmileModelParameters(halfLife=halfLife, maxPeriods=maxPeriod, lagPeriods=lagPeriods)
			moddur$calculateDoasDccForITMs(dateRange=dateRange, knotPoints=knots.list[[knots]])
			moddur$generateDoasDccForCoupons(dOASdCC.min = -0.1, dOASdCC.max = 0.1)   #Min / Max = -100/100
			moddur$calculateSmileAdjustedDv01(minDv01=0.1)  #minDv01=NULL
			moddur$uploadSmileAdjustedDv01(uploadPath=squish("h:/Smile OAD/",knots,"/"),uploadMethod='file')			
		}
	}
}

###########################################################################################
#		Calculate the performance
###########################################################################################

perf <- TBAPerformance(program, coupons)
perf$setTsdbSources(tbaPriceSource="internal", tbaDv01Source='internal')
#Set up the data used for all
perf$setCouponDateRange()
perf$setTBAPrice()
perf$setSwapRates()
perf$setCurrentCoupon()
#Partial duration are the same for all as it is converted into percentage partials
perf$setPartialDurations()

for (knots in names(knots.list)){
	for (halfLife in halfLifes){
		for (maxPeriod in maxPeriods){
			print(squish('Calc performance for knots ',knots,' halfLife ',halfLife,' maxPeriods ', maxPeriod))
			filename.dv01 <- paste('TBA','dv01','smileModel','hl',halfLife,'mp',maxPeriod,sep="_")
			
			perf$setTBADv01(container=squish(fileDir,knots,'/',filename.dv01,'.csv'))
			perf$calculateExpectedPriceChanges()
			perf$calculateActualPriceChanges()
			perf$calculateCouponPerformances()
			perf$generateITMPerformances(ITMKnots=seq(-3,3,0.5))
			
			uploadFilename <- paste('TBA_Performance_coupon','smileModel','hl',halfLife,'mp',maxPeriod,sep="_")
			perf$uploadCouponPerformances(uploadFilename=uploadFilename,uploadPath=squish(fileDir,knots,'/'),uploadMethod='file')
			uploadFilename <- paste('TBA_Performance_itm','smileModel','hl',halfLife,'mp',maxPeriod,sep="_")			
			perf$uploadITMPerformances(uploadFilename=uploadFilename,uploadPath=squish(fileDir,knots,'/'),uploadMethod='file')			
		}
	}
}

###########################################################################################
#		Summary statistics on the performance
###########################################################################################

library(QFPorfolio)

quantile10 <- function(x) quantile(x,0.10)[[1]]
quantile25 <- function(x) quantile(x,0.25)[[1]]
quantile75 <- function(x) quantile(x,0.75)[[1]]
quantile90 <- function(x) quantile(x,0.90)[[1]]
MAE <- function(x) mean(abs(x))

result.matrix <- c('knots','halfLife','maxPeriods','itm','mean','stddev','MAE','skew','kurtosis','quantile10','quantile25','quantile75','quantile90')
function.list <- c(mean,sd,MAE,skewness,kurtosis,quantile10,quantile25,quantile75,quantile90)

ITMKnots=seq(-3,3,0.5)
for (knots in names(knots.list)){
	for (halfLife in halfLifes){
		for (maxPeriod in maxPeriods){
			uploadPath=squish(fileDir,knots,'/')
			perfFile <- paste('TBA_Performance_itm','smileModel','hl',halfLife,'mp',maxPeriod,sep="_")
			df <- read.csv(squish(uploadPath,perfFile,'.csv'))
			for (itm in seq_along(ITMKnots)){
				itm.vector <- na.omit(df[,itm+1])*32
				if(length(itm.vector) > 0){
					result.vector <- sapply(function.list, function(func) func(itm.vector))
					parameter.vector <- c(knots,halfLife,maxPeriod,ITMKnots[itm])
					result.matrix <- rbind(result.matrix, t(c(parameter.vector, result.vector)))	
				}
			}
		}
	}
}

result.df <- data.frame(result.matrix[2:nrow(result.matrix),])
colnames(result.df) <- result.matrix[1,]
write.csv(result.df, squish(fileDir,'resultCube.csv'),col.names=TRUE,row.names=FALSE)


###########################################################################################
#		For reference the unadjusted dv01s
###########################################################################################

halfLife <- 5
maxPeriod <- 5

#Can be set up as the usual problem, but constrict the min/max to 0 for smile
moddur <- TBAModifiedOAD(program,coupons)
moddur$setITMKnotPoints(knotPoints)
moddur$setCouponDateRange()
#Load in flat file informatino from JPMorgan "New" Models, Current coupon from internal
moddur$setTBAOAD(squish(fileDir,'OAD.csv'))
moddur$setTBASpreadDv01(squish(fileDir,'SpreadDuration.csv'))
moddur$setCC(squish(fileDir,'CurrentCoupon.csv'))
moddur$setOAS(squish(fileDir,'OAS.csv'))
moddur$generateITMOASs()
#Set the date range to start 1/1/1999 and go from merged data
dateRange <- index(moddur$.TBA$oas$itm[['0']][index(moddur$.TBA$oas$itm[['0']]) >= as.POSIXct('1999-01-01 15:00:00')])
#Set up the model
moddur$setSmileModelParameters(halfLife=halfLife, maxPeriods=maxPeriod, lagPeriods=5)
moddur$calculateDoasDccForITMs(dateRange=dateRange, knotPoints=c(0))
#This is the key line
moddur$generateDoasDccForCoupons(dOASdCC.min = 0, dOASdCC.max = 0)
moddur$calculateSmileAdjustedDv01(minDv01=NULL)
moddur$uploadSmileAdjustedDv01(uploadPath=squish("h:/Smile OAD/"),uploadMethod='file')			

#Now calculate the performance
perf <- TBAPerformance(program, coupons)
perf$setTsdbSources(tbaPriceSource="internal", tbaDv01Source='internal')
#Set up the data used for all
perf$setCouponDateRange()
perf$setTBAPrice()
perf$setSwapRates()
perf$setCurrentCoupon()
#Partial duration are the same for all as it is converted into percentage partials
perf$setPartialDurations()

filename.dv01 <- paste('TBA','dv01','smileModel','hl',halfLife,'mp',maxPeriod,sep="_")

perf$setTBADv01(container=squish(fileDir,filename.dv01,'.csv'))
perf$calculateExpectedPriceChanges()
perf$calculateActualPriceChanges()
perf$calculateCouponPerformances()
perf$generateITMPerformances(ITMKnots=seq(-3,3,0.5))

uploadFilename <- paste('TBA_Performance_baseModel_coupon','smileModel','hl',halfLife,'mp',maxPeriod,sep="_")
perf$uploadCouponPerformances(uploadFilename=uploadFilename,uploadPath=fileDir,uploadMethod='file')
uploadFilename <- paste('TBA_Performance_baseModel_itm','smileModel','hl',halfLife,'mp',maxPeriod,sep="_")			
perf$uploadITMPerformances(uploadFilename=uploadFilename,uploadPath=fileDir,uploadMethod='file')			

#Load and calculate the statistics

result.matrix <- c('knots','halfLife','maxPeriods','itm','mean','stddev','MAE','skew','kurtosis','quantile10','quantile25','quantile75','quantile90')
perfFile <- paste('TBA_Performance_baseModel_itm','smileModel','hl',halfLife,'mp',maxPeriod,sep="_")
df <- read.csv(squish(fileDir,perfFile,'.csv'))
for (itm in seq_along(ITMKnots)){
	itm.vector <- na.omit(df[,itm+1])*32
	if(length(itm.vector) > 0){
		result.vector <- sapply(function.list, function(func) func(itm.vector))
		parameter.vector <- c(knots,halfLife,maxPeriod,ITMKnots[itm])
		result.matrix <- rbind(result.matrix, t(c(parameter.vector, result.vector)))	
	}
}

result.df <- data.frame(result.matrix[2:nrow(result.matrix),])
colnames(result.df) <- result.matrix[1,]
write.csv(result.df, squish(fileDir,'resultCube_baseModel.csv'),col.names=TRUE,row.names=FALSE)


###########################################################################################
#		To Do
###########################################################################################

- Change the smile generator to do dOAS for the coupon, then map that to an avg(ITM)
- Min/Max dOASdCC
- MinDv01
- MinDv01 on raw OADs
- Hawver
- Partial durations, no negatives bring back from other points