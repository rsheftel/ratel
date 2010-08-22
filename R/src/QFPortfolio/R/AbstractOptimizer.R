constructor("AbstractOptimizer", function() {
    this <- extend(RObject(), "AbstractOptimizer")
    if(inStaticConstructor(this)) return(this)
    this
})

method('curves', 'AbstractOptimizer', function(this,...){
	fail('curves() METHOD MUST BE IMPLEMENTED WHEN INHERITING FROM AbstractOptimizer()')		
})

method('optimize', 'AbstractOptimizer', function(this,...){
	fail('optimize() METHOD MUST BE IMPLEMENTED WHEN INHERITING FROM AbstractOptimizer()')
})

method('objectiveMetric', 'AbstractOptimizer', function(this,...){
	fail('objectiveMetric() METHOD MUST BE IMPLEMENTED WHEN INHERITING FROM AbstractOptimizer()')
})

method('objectiveFunction', 'AbstractOptimizer', function(this,...){
	fail('objectiveFunction() METHOD MUST BE IMPLEMENTED WHEN INHERITING FROM AbstractOptimizer()')
})

method('optimizationRoutine', 'AbstractOptimizer', function(this,...){
	fail('optimizationRoutine() METHOD MUST BE IMPLEMENTED WHEN INHERITING FROM AbstractOptimizer()')
})
