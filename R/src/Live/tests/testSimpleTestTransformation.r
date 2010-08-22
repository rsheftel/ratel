library(Live)

testConstructor <- function() {
    t <- SimpleTestTransformation("SP.1C")
    checkInherits(t, "SimpleTestTransformation")
    inputs <- t$inputs()
    needs(inputs = "list(SeriesDefinition)")
    checkLength(inputs, 1)
    checkSame("MARKETDATA:||:SP.1C:||:LastPrice", strings(inputs))
}

testUpdate <- function() {
    t <- SimpleTestTransformation("SP.1C")
    valueIn <- 1234.56
    outputs <- t$update("MARKETDATA:||:SP.1C:||:LastPrice:||:1234.56:||:TRUE")
    checkLength(outputs, 1)
    checkSame("TEST:||:SP.1C:||:BID:||:1234.560000000000", strings(outputs))
}



