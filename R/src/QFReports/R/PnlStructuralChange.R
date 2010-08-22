# TODO: Add comment
# 
# Author: jbourgeois
###############################################################################

constructor("PnlStructuralChange", function(groupName=NULL, curvesDirectory=NULL, curvesExtension="bin"){
	this <- extend(RObject(), "PnlStructuralChange",.groupName=groupName, .curvesDirectory=curvesDirectory, .curvesExtension=curvesExtension)
	constructorNeeds(this,groupName="character", curvesDirectory="character", curvesExtension="character")
	if (inStaticConstructor(this)) return(this)		
	return(this)
})

method("getCurves", "PnlStructuralChange", function(this,rangeType = NULL,...){
	CurveGroup(this$.groupName)$childCurves(this$.curvesDirectory, extension = this$.curvesExtension,range = NULL,rangeType = rangeType)	
})

method("getKSFrame", "PnlStructuralChange", function(this,curvesA,curvesB,alternative,colNames,...){			
	assert(length(curvesA) == length(curvesB))	
	data <- data.frame(Group=NULL,PValue=NULL,NumObsA=NULL,NumObsB =NULL,NumObsADivNumObsB=NULL)
	for(i in sequence(length(curvesA))){		
		getCurve <- function(curveList,i){curve <- as.numeric(curveList[[i]]$pnl()); curve[curve!=0]}
		curveA <- getCurve(curvesA,i); curveB <- getCurve(curvesB,i)
		if(length(curveA) > 0 && length(curveB) > 0)					
			data <- rbind(data,
				data.frame(names(curvesA)[i],HypothesisTests$ksTest(curveA,curveB,alternative))
			)
	}
	colnames(data) <- colNames	
	data
})