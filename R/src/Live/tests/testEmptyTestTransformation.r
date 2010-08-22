library(Live)

testEmpty <- function() {
    t <- EmptyTestTransformation("SP.1C")
    outputs <- t$update("MARKETDATA:||:SP.1C:||:LastPrice:||:1234.56:||:TRUE")
    checkLength(outputs, 0)
}



