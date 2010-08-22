cat("\n\nTest cases for IvyDB object\n\n")

library("GSFCore")

ivydb <- IvyDB()

testConstructor <- function() {
    checkTrue( is(ivydb, "IvyDB") )
}

testLookupSecurityID <- function() {
    checkEquals(105169, ivydb$lookupSecurityID(ticker = "ge"))

    # The ticker 'S' changed from Sears to Sprint after Sears was acquired in 2005.  
    # securityID 110002 is Sears, 110433 is Sprint.  We should get back Sprint.
    checkEquals(110433, ivydb$lookupSecurityID(ticker = "s"))
}

testLookupSecurityIDWithClass <- function() {
    checkEquals(102109, ivydb$lookupSecurityID(ticker = "brk", class = "b"))
    shouldBomb(ivydb$lookupSecurityID(ticker = "brk"))
}

testLookupTicker <- function() {
    checkEquals("ge", ivydb$lookupTicker(security.id = 105169))
    checkEquals("brk", ivydb$lookupTicker(security.id = 102108))
}

testRetrieveImpliedVol <- function() {
    aapl.vol <- ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        expiry = "30d",
        option_type = "call",
        quote_type = "close",
        quote_convention = "vol_ln",
        quote_side = "mid",
        instrument = "cm_equity_option"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01")

    checkResults(aapl.vol, 503, 208.212709218264, 0.280393987894058, 0.594530999660492)
}

testRetrieveDelta <- function() {
    aapl.delta <- ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        expiry = "30d",
        option_type = "call",
        quote_type = "close",
        quote_convention = "delta",
        quote_side = "mid",
        instrument = "cm_equity_option"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01")

    checkResults(aapl.delta, 503, 263.399778962135, 0.51603102684021, 0.533959984779358)
}

testRetrieveStrike <- function() {
    aapl.strike <- ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        expiry = "30d",
        option_type = "call",
        quote_type = "strike",
        instrument = "cm_equity_option"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01")

    checkResults(aapl.strike, 503, 31092.30835, 34.2178535461426, 92.2174530029297)
}

testRetrieveClosePrice <- function() {
    aapl.close <- ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        quote_type = "close",
        quote_convention = "price",
        quote_side = "mid",
        instrument = "equity"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01")

    checkResults(aapl.close, 503, 30978.7145, 34.1300010681152, 91.8099975585938)
}

testRetrieveTotalReturn <- function() {
    aapl.total.return <- ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        quote_type = "close",
        quote_convention = "total_return",
        quote_side = "mid",
        instrument = "equity"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01")

    checkResults(aapl.total.return, 503, 1.117796, -0.092105, 0.1183)
}

testRetrieveTotalReturnFactor <- function() {
    aapl.total.return.factor <- ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        quote_type = "close",
        quote_convention = "total_return_factor",
        quote_side = "mid",
        instrument = "equity"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01")

    checkResults(aapl.total.return.factor, 503, 1936, 2, 4)
}

testRetrieveSharesOutstanding <- function() {
    aapl.shares <- ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        quote_type = "shares_outstanding",
        instrument = "equity"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01")

    checkResults(aapl.shares, 503, 406312926, 402058, 859274)
}

testRetrieveVolume <- function() {
    aapl.volume <- ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        quote_type = "volume",
        instrument = "equity"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01")

    checkResults(aapl.volume, 503, 13419543522, 6321863, 98538510)
}

checkResults <- function(data, length.target, sum.target, min.target, max.target) {
    checkTrue(is(data, "zoo"))
    checkSame(length(data), length.target)
    checkSame(sum(data), sum.target)
    checkSame(min(data), min.target)
    checkSame(max(data), max.target)
}

testBadOptionSecurityIDException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = "aapl",
        expiry = "30d",
        option_type = "call",
        quote_type = "strike",
        instrument = "cm_equity_option"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))
}

testBadOptionExpiryException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        expiry = 30,
        option_type = "call",
        quote_type = "strike",
        instrument = "cm_equity_option"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))
}

testBadOptionOptionTypeException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        expiry = 30,
        option_type = "C",
        quote_type = "strike",
        instrument = "cm_equity_option"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))
}

testBadOptionQuoteTypeException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        expiry = "30d",
        option_type = "call",
        quote_type = "asdf",
        instrument = "cm_equity_option"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))
}

testBadOptionInstrumentException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        expiry = "30d",
        option_type = "call",
        quote_type = "strike",
        instrument = "equity_opt"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))
}

testBadOptionQuoteConvetionException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        expiry = "30d",
        option_type = "call",
        quote_type = "close",
        quote_convention = "vol",
        quote_side = "mid",
        instrument = "cm_equity_option"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))
}

testBadOptionQuoteSideException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        expiry = "30d",
        option_type = "call",
        quote_type = "close",
        quote_convention = "vol_ln",
        quote_side = "ask",
        instrument = "cm_equity_option"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))

}

testBadEquityQuoteTypeException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        quote_type = "open",
        quote_convention = "price",
        quote_side = "mid",
        instrument = "equity"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))
}

testBadEquityQuoteConventionException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        quote_type = "close",
        quote_convention = "spread",
        quote_side = "mid",
        instrument = "equity"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))
}

testBadEquityQuoteSideException <- function() {
    shouldBomb(ivydb$retrieveOneTimeSeriesByAttributeList(attributes = list(
        security_id = 101594,
        quote_type = "close",
        quote_convention = "price",
        quote_side = "ask",
        instrument = "equity"
    ), data.source = "ivydb", start = "2005-01-01", end = "2007-01-01"))
}

