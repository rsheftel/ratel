library(Live)

testConstructor <- function() {
    t <- TestTransformation("SP.1C")
    checkInherits(t, c("TestTransformation", "Transformation"))
    inputs <- t$inputs()
    needs(inputs = "list(SeriesDefinition)")
    checkLength(inputs, 1)
    checkSame("TEST:||:SP.1C:||:Value", strings(inputs))
}

testUpdate <- function() {
    t <- TestTransformation("SP.1C")
    outputs <- t$update("TEST:||:SP.1C:||:Value:||:1234.56:||:TRUE", quiet=TRUE)
    checkLength(outputs, 2)
    checkSame("TEST:||:SP.1C:||:Output:||:1234.560000000000", first(strings(outputs)))
}

testInitializeFailureCausesPermanentDisabilityAndErrorsOnce <- function() { 
    t <- TestTransformation("INIT_FAILURE")
    outputs <- t$update("TEST:||:INIT_FAILURE:||:Value:||:1234.56:||:TRUE", quiet=TRUE)
    checkLength(outputs, 2)
    checkSame("TEST:||:INIT_FAILURE:||:Output:||:ERROR:FAILED INIT", first(strings(outputs)))
    timestamp <- second(strings(outputs))
    checkFalse(matches("ERROR", timestamp))
    outputs <- t$update("TEST:||:INIT_FAILURE:||:Value:||:1234.56:||:TRUE", quiet=TRUE)
    checkLength(outputs, 0)
    checkTrue(t$isDisabled())
}

testUnparsableInputsResultsInNoOutputs <- function() { 
    t <- TestTransformation("FOO")
    outputs <- t$update("TEST:||:FOO:||:Value:||:xxx:||:TRUE", quiet=TRUE)
    checkLength(outputs, 0)
}

testSkipUpdateWorks <- function() { 
    t <- TestTransformation("SKIP_ZEROES")
    outputs <- t$update("TEST:||:SKIP_ZEROES:||:Value:||:1234:||:TRUE", quiet=TRUE)
    checkLength(outputs, 2)
    checkSame("TEST:||:SKIP_ZEROES:||:Output:||:1234.000000000000", first(strings(outputs)))
    outputs <- t$update("TEST:||:SKIP_ZEROES:||:Value:||:0:||:TRUE", quiet=TRUE)
    checkLength(outputs, 0)
    outputs <- t$update("TEST:||:SKIP_ZEROES:||:Value:||:1234:||:TRUE", quiet=TRUE)
    checkLength(outputs, 2)

}


