constructor("OptimizationRoutine", function(type = NULL,...){
	this <- extend(RObject(), "OptimizationRoutine",
		.type = type,
		.upper = NULL,
		.lower = NULL,
		.listParams = NULL
	)
	constructorNeeds(this,type = "character")
	if(inStaticConstructor(this)) return(this)
	failUnless(any(this$.type==this$availableMethods()),"Not an allowed type")
	if (this$.type==c("constrOptim")) {
		this$.listParams <- list(ndeps=10^-3, fnscale=-1, reltol = 10^-3)
	}
	if (this$.type==c("nlminb")) {
		this$.listParams <- list(abs.tol=10^-4,rel.tol=10^-4,step.min=10^-2)
	}
	if (this$.type==c("simAnnealing")) {
		this$.listParams <- list(ndeps=10^-3, fnscale=-1, reltol=10^-6,maxit=20000,temp=2,tmax=100,trace=2)
	}
	if (this$.type==c("optim")){
		this$.listParams <- list(fnscale=-1, maxit=20000,factr=10^-4, trace=1)
	}
	
	this
})

method("params","OptimizationRoutine",function(this,listParams=NULL,...) {
	needs(listParams="list(numeric|character)?")
	if (is.null(listParams)) return(this$.listParams)
	this$.listParams <- listParams
})


method("upperWeights","OptimizationRoutine",function(this, upper=NULL,... ) {
	needs(upper="numeric?")
	if (is.null(upper)) return(this$.upper)
	this$.upper <- upper
})

method("lowerWeights","OptimizationRoutine",function(this, lower=NULL,... ) {
	needs(lower="numeric?")
	if (is.null(lower)) return(this$.lower)
	this$.lower <- lower
})

method("availableMethods","OptimizationRoutine",function(this,...) {
	return(c("constrOptim","simAnnealing","nlminb","optim","metricAllocation"))	
})

method("optimize","OptimizationRoutine",function(this, start=NULL, objectiveFunction=NULL,...) {
	if (this$.type=="constrOptim") {
		lower <- this$.lower
		upper <- this$.upper
		ui <- rbind(diag(length(upper)) * -1, diag(length(lower)))
		ci <- c(upper * -1, lower)
		res <- constrOptim(start, objectiveFunction, NULL, ui, ci, control=this$.listParams)
		return(res)
	}
	if (this$.type=="nlminb") {
		lower <- this$.lower
		upper <- this$.upper
		res <- nlminb(start, objectiveFunction, gradient= NULL, hessian = NULL, scale=1, control=this$.listParams, lower=lower, upper=upper)
		return(res)	
	}
	if (this$.type=="simAnnealing") {
		
		res <- optim(start,fn = objectiveFunction, gr=NULL,method="SANN",control=this$.listParams)
		return(res)
	}
	if (this$.type=="optim") {
		lower <- this$.lower
		upper <- this$.upper
		res <- optim(start,fn = objectiveFunction, gr=NULL, method="L-BFGS-B",lower=lower,upper=upper,control=this$.listParams)
		return(res)
	}
})
