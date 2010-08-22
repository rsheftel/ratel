constructor("TestTransformation", function(name = NULL, ...) {
    this <- extend(Transformation(), "TestTransformation", .name = name)
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, name = "character")
    this
})

method(".outputSeries", "TestTransformation", function(this, ...) {
    list(
        testOut=SeriesDefinition("TEST", this$.name, "Output"),
        Timestamp=SeriesDefinition("TEST", this$.name, "Timestamp")
    )
})

method(".inputSeries", "TestTransformation", function(this, ...) {
    list(testIn = SeriesDefinition("TEST", this$.name, "Value"))
})

method("initialize", "TestTransformation", function(this, ...) {
    if (this$.name != "INIT_FAILURE") return("SUCCESS")
    "FAILED INIT"
})

method("skipUpdate", "TestTransformation", function(this, inputs, ...) {
    this$.name == "SKIP_ZEROES" && this$value(inputs, "testIn") == 0
})

method("outputValues", "TestTransformation", function(this, inputs, ...) {
    value <- this$value(inputs, "testIn")
    list(this$outputs()$testOut$valueString(value), this$outputs()$Timestamp$now())
})




