## Test file for the Equity object
library("QFEquity")

testEquity <- function()
{
    this <- Equity('ge')
	checkSame(this$.ticker,'ge')
	checkSame(this$.securityId,105169)
	checkSame(this$.owner,'Jerome Bourgeois')
	checkSame(this$.bbrg,'GE Equity')	
	this <- Equity(securityId = 105169)
	checkSame(this$.ticker,'ge')
	checkSame(this$.securityId,105169)
}

testBloombergLiabilityTsNames <- function()
{
	checkSame(Equity('ge')$tsNameLiabilities(),'ivydb_105169_total_liabilities')
	checkSame(Equity('ge')$tsNameRaw('close'),'ivydb_105169_close_price_mid')
	checkSame(Equity('ge')$tsNameRaw('open'),'ivydb_105169_open_price_mid')
	checkSame(Equity('ge')$tsName(TRUE,'open'),'105169_open_tri_vLibor')
	checkSame(Equity('ge')$tsName(FALSE,'open'),'ivydb_105169_open_adj_price_mid')
	checkSame(Equity('ge')$tsName(FALSE,'close'),'ivydb_105169_close_adj_price_mid')
	checkSame(Equity('ge')$tsName(TRUE,'close'),'105169_tri_vLibor')
}