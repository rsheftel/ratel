library(QFMortgage)

testFrontSettle <- function() {
    checkSame(as.POSIXct("2008/04/14"), TBA$frontSettle("fncl", "2008/04/09"))
    checkSame(as.POSIXct("2008/05/13"), TBA$frontSettle("fncl", "2008/04/10"))
    checkSame(as.POSIXct("2008/04/10"), TBA$frontNotificationDate("fncl", "2008/04/09"))
    checkSame(as.POSIXct("2008/05/09"), TBA$frontNotificationDate("fncl", "2008/04/10"))
}

test.startEndDatesForCoupons <- function(){
	checkSame(as.POSIXct("2003/04/01"), TBA$startEndDatesForCoupons('fncl',c(5,5.5))$startDates[1])
	checkSame(as.POSIXct("1992/07/23"), TBA$startEndDatesForCoupons('fncl',c(7))$startDates)
	checkSame(as.POSIXct("2001/12/31"), TBA$startEndDatesForCoupons('fncl',c(7,8.5))$endDates[2])
	checkSame(as.POSIXct("2003/06/01"), TBA$startEndDatesForCoupons('fncl',c(8.0,4.5))$endDates[1])
}

test.couponVector <- function(){
	checkSame(seq(3.5,8.5,0.5), TBA$couponVector('fncl'))
	checkSame(seq(3.5,8.0,0.5), TBA$couponVector('fnci',group='all'))
	checkSame('Not a valid program', TBA$couponVector('xxx'))
	
	checkSame(seq(4.0,6.5,0.5), TBA$couponVector('fncl',group='active'))
	checkSame(seq(4.0,6.0,0.5), TBA$couponVector('fnci',group='active'))
	
	checkSame(TBA$couponVector('fncl'), TBA$couponVector('fglmc'))
	checkSame(TBA$couponVector('fncl'), TBA$couponVector('gnsf'))
	checkSame(TBA$couponVector('fnci'), TBA$couponVector('fgci'))
}

test.cut <- function(){
	testzoo <- zoo(1:5,as.POSIXct(c('1900-01-01','1970-12-31','2003-01-01','2004-01-01','2090-01-01')))
	checkSame(testzoo[2:3], TBA$cut(testzoo,'fncl',7.5))
	checkSame(testzoo[4:5], TBA$cut(testzoo,'fncl',4.5))
	checkSame(testzoo[4:5], TBA$cut(testzoo,'fncl',c(4.5,6)))
	checkSame(testzoo[4], TBA$cut(testzoo,'fncl',c(4.5,7)))
	checkSameLooking(zoo(NULL), TBA$cut(testzoo,'fncl',c(3.5)))
	checkSameLooking(zoo(NULL), TBA$cut(testzoo,'fncl',c(4,8.5)))
}
