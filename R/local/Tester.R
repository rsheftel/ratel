

constructor("TestMe", function(...) {
	extend(RObject(), "TestMe")
})

method("testAdd", "TestMe", function(this, a, b, ...){
 	return (a+b)
})
