#Daily job to calculate the following for mortgage TBAs and save to tsdb:
#   Current Coupon
#   30d fwd price
#   45d fwd price

library(QFMortgage)
dataDateTime <- dateTimeFromArguments(commandArgs(),hour=15)


programs <- c('fncl','fglmc','gnsf','fnci','fgci')

for (program in programs){
	print(squish('Program : ',program))
	tbaObj <- TBAGrid(program, TBA$couponVector(program,'all'), dataDateTime)
	tbaObj$setPricesFromTSDB()
	tbaObj$setSettleDatesFromTSDB()
	tbaObj$writeTSDBuploadFile(uploadMethod='direct')
}
print("Done.")
