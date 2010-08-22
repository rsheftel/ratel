# Hypothesis Testing
# 
# Author: RSheftel
###############################################################################

constructor("HypothesisTests", function(){
	this <- extend(RObject(), "HypothesisTests")
	if (inStaticConstructor(this)) return(this)
	return(this)
})

method("signTest", "HypothesisTests", function(static, testData, conf.level=0.95, ...){
#Performs a two-tailed test on the test data to accept or reject the hypothesis that time series have the same signs.
#Use this as a more blunt test of correlation between time series than correlation.

	needs(testData="zoo|matrix|data.frame")
	testData <- as.matrix(testData)
	
	if(NCOL(testData)!=2) fail ('Can only hanlde 2 column series')
	
	success.count <- sum(sign(testData[,1])==sign(testData[,2]))
	trials.count <- NROW(testData)
	
	return(binom.test(success.count, trials.count, p=0.5, alternative="two.sided", conf.level=conf.level))
})

method("ksTest", "HypothesisTests", function(static,x,y,alternative,...){
	needs(x = 'numeric',y = 'numeric',alternative = 'character')
	ks = ks.test(x,y,alternative = alternative)
	data.frame(				
		PValue = round(as.numeric(ks$p.value),4),
		NumObsX = NROW(x),
		NumObsY = NROW(y),
		NumObsXDivNumObsY = 100 * round(NROW(x)/NROW(y),4)
	)
})