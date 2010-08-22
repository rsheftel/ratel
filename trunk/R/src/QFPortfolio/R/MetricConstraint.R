constructor("MetricConstraint", function(metric = NULL, min = NULL, max = NULL) {
    this <- extend(RObject(), "MetricConstraint", .metric = metric, .min = min, .max = max)
    constructorNeeds(this, metric="Metric", min = "numeric", max = "numeric")
    if(inStaticConstructor(this)) return(this)
    failUnless(min <= max, "must conform to min <= max, have ", min, " > ", max)
    this
})

method("isViable", "MetricConstraint", function(this, curve, ...) {
    needs(curve = "PositionEquityCurve")
    m <- curve$metric(this$.metric)
    res <- this$.min <= m && m <= this$.max
    if(!res) attr(res, "message") <- squish(this$as.character(), " rejects ", m)
    res
})

method("distance", "MetricConstraint", function(this, curve, ...) {
	needs(curve = "PositionEquityCurve")
    m <- curve$metric(this$.metric)
	if ((this$.max-this$.min)==0) {
		res <- (min(m-this$.min,0)^2 + max(m-this$.max,0)^2)
	}
	else res <- (min(m-this$.min,0)^2 + max(m-this$.max,0)^2)/(this$.max - this$.min)^2 
	if(res > 0) attr(res, "message") <- squish(this$as.character(), " rejects ", m)
    res	
})

method("lessThanOrEqual", "MetricConstraint", function(static, metric, max, ...) {
    MetricConstraint(metric, -Inf, max)
})

method("greaterThanOrEqual", "MetricConstraint", function(static, metric, min, ...) {
    MetricConstraint(metric, min, Inf)
})

method("as.character", "MetricConstraint", function(this, ...) {
    squish(this$.metric$as.character(), ": (", this$.min, ", ", this$.max, ")")
})

method('as.data.frame', 'MetricConstraint', function(this, ...){
	data.frame(MC.MetricName = this$.metric$as.character(), MC.Min = this$.min, MC.Max = this$.max)		
})

method('metric', 'MetricConstraint', function(this, ...){
	return(this$.metric)	
})

method('minimum', 'MetricConstraint', function(this, ...){
	return(this$.min)	
})

method('maximum', 'MetricConstraint', function(this, ...){
	return(this$.max)	
})
