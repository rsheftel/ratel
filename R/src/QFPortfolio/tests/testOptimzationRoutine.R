testOptimzationRoutine <- function()
{
	optimRoutine <- OptimizationRoutine(type="constrOptim")
	optimRoutine <- OptimizationRoutine(type="simAnnealing")
	shouldBomb(OptimizationRoutine(type="businessTime"))
	
	optimRoutine <- OptimizationRoutine(type="constrOptim")
	checkSame(optimRoutine$params(),list(ndeps=10^-3, fnscale=-1, reltol = 10^-3))
	
	checkTrue(is.null(optimRoutine$upperWeights()))
	shouldBomb(optimRoutine$upperWeights("BusinessTime"))
	upper <- c(1,1)
	optimRoutine$upperWeights(upper=upper)
	checkSame(optimRoutine$upperWeights(),upper)
	
	checkTrue(is.null(optimRoutine$lowerWeights()))
	shouldBomb(optimRoutine$lowerWeights("BusinessTime"))
	lower <- c(0,0)
	optimRoutine$lowerWeights(lower=lower)
	checkSame(optimRoutine$lowerWeights(),lower)
	
	objFct <- function(w) {return((w[1]^2+w[2]^2)^(1/2) - (w[1]^2+w[2]^2)^(3/2))}
	start <- c(0.5,0.5)
	ans <- optimRoutine$optimize(start=start,objective=objFct)
	x <- 1/sqrt(3)
	checkSame(round(ans$value,3),round(x-x^3,3))
}

test.metricAllocation <- function(){
	checkInherits(OptimizationRoutine(type='metricAllocation'),"OptimizationRoutine")
	opr <- OptimizationRoutine(type='metricAllocation')
	params <- list(arbitrary=1, x=list(a=1,b=2), y=Diversity(groupName='TestPortfolio1'))
	opr$params(params)
	checkSame(opr$params(), params)
}
