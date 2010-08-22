constructor("WeightedCurvesConstraint", function() {
    extend(RObject(), "WeightedCurvesConstraint")
})

method("rejects", "WeightedCurvesConstraint", function(this, wc, ...) {
    needs(wc="WeightedCurves")
    fail("subclasses of WeightedCurvesConstraint must implement rejects(wc)")
})

method("rejected", "WeightedCurvesConstraint", function(static, ...) {
    res <- TRUE
    attr(res, "message") <- squish(...)
    res
})

method("as.character", "WeightedCurvesConstraint", function(this, ...) {
	fail("Subclasses of WeightedCurvesConstraint must implement report as single vector output")
})

method('as.data.frame', 'WeightedCurvesConstraint', function(this,...){
	fail('Subclasses of WeightedCurvesConstraint must implement the as.data.frame method')		
})