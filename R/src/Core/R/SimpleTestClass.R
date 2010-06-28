constructor("SimpleTestClass", function(a = NULL, b = NULL) {
    this <- extend(RObject(), "SimpleTestClass", .a = a, .b = b)
    constructorNeeds(this, a = "numeric", b = "character?")
    this
})

method("as.character", "SimpleTestClass", function(this, ...) {
    squish("STC", this$.a, this$.b)
})

constructor("SimplerTestClass", function() {
    extend(SimpleTestClass(1), "SimplerTestClass")
})

constructor("NotSimpleTestClass", function(n) {
    extend(SimpleTestClass(1), "SimpleTestClass", .contained = SimpleTestClass(n)) 
})

constructor("NotSimple2", function(n) {
    extend(SimpleTestClass(n), "SimpleTestClass", .contained = SimpleTestClass(123)) 
})
