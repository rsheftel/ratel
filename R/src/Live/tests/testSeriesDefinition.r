library(Live)

testSeriesDefUnparsedValueErrorMessage <- function() { 
    shouldBombMatching(
        SeriesDefinition$withValues("asdf:||:asdf:||:asdf:||:asdf:||:asdf"),
        "value in asdf:||:asdf:||:asdf:||:asdf:||:asdf could not be parsed"
    )
}
