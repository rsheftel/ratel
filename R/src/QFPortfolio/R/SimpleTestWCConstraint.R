constructor("SimpleTestWCConstraint", function() {
    extend(WeightedCurvesConstraint(), "SimpleTestWCConstraint")
})

method("rejects", "SimpleTestWCConstraint", function(this, wc, ...) {
    needs(wc="WeightedCurves")
    if(all(wc$weights() <= 5)) return(FALSE)
    this$rejected("Weight > 5: ", commaSep(wc$weights()))
})
