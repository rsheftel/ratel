#
#	Test of PnLAnalysis class  (rsheftel)
#
#################################################################

library(QFReports)

testdata <- squish(system.file("testdata", package="QFReports"),'/PnLAnalysis/')

objectSetup <- function(){
	pnl <- PnlAnalysis()
	pnl$addPnlGroup("Group1",c("Test.Tag1"))
	pnl$addPnlGroup("Group2", PerformanceDB$tagsForTagGroup("TestTagGroup1"))
	return(pnl)
}

test.Constuctor <- function(){
	pnl <- PnlAnalysis()
	checkInherits(pnl,"PnlAnalysis")
}

test.dateRange <- function(){
	pnl <- PnlAnalysis(Range(start="1990-01-01", end="2001-12-31"))
	checkSame("1990/01/01 to 2001/12/31 23:59:59", pnl$dateRange())
}

test.addTags <- function(){
	pnl <- objectSetup()
	checkSame(c("Group1","Group2"), pnl$groupNames())
	expected <- list(Group1=c("Test.Tag1"),
					 Group2=c("Test.Tag1","Test.Tag2"))
	checkSame(expected, pnl$groupTags())
	checkSame(c("Test.Tag1","Test.Tag2"), pnl$groupTags("Group2"))
	checkSame(c('Test.Tag1','Test.Tag2'), pnl$tags())
}

test.loadPnl <- function(){
	pnl <- objectSetup()
	pnl$loadPnl(source="Test_source1")
	shouldBomb(pnl$loadPnl("Test_source1"))
	
	expected <- zoo(c(12345,55,55,1212), as.POSIXct(c("1980-01-02",'1980-01-03','1980-01-04','2009-01-09')))
	checkSameLooking(expected, pnl$groupCurve("Group1","Test_source1")$pnl())
	
	shouldBombMatching(pnl$addPnlGroup("Group2",'tag1'),"Cannot add PnlGroup once Pnl has been loaded for any source.")
	
	expected <- zoo(c(12345,55,9953,1212), as.POSIXct(c("1980-01-02",'1980-01-03','1980-01-04','2009-01-09')))
	checkSameLooking(expected, pnl$groupCurve("Group2","Test_source1")$pnl())	
	
	checkSame("Test_source1", pnl$sources()) 
	
	pnl$loadPnl(source='Test_source3')
	expected <- zoo(c(10000,-55,99,-12), as.POSIXct(c("1980-01-02",'1980-01-04','1980-01-05','2009-01-09')))
	checkSameLooking(expected, pnl$groupCurve("Group1","Test_source3")$pnl())
	checkSameLooking(expected, pnl$groupCurve("Group2","Test_source3")$pnl())
}

test.dollarPnL <- function(){
	pnl <- objectSetup()
	pnl$loadPnl(source="Test_source1")
	expected1 <- zoo(c(12345,55,55,1212), as.POSIXct(c("1980-01-02",'1980-01-03','1980-01-04','2009-01-09')))
	expected2 <- zoo(c(12345,55,9953,1212), as.POSIXct(c("1980-01-02",'1980-01-03','1980-01-04','2009-01-09')))
	expected <- merge(expected1,expected2,all=TRUE)
	expected <- cbind(expected,rowSums(expected,na.rm=TRUE))
	colnames(expected) <- c('Group1','Group2','Total')
	
	checkSame(expected, pnl$dailyPnl(source="Test_source1"))
	checkSame(cumsum(expected), pnl$dailyEquity(source="Test_source1"))
	
	checkSame(Range$after("1980-01-03")$cut(expected), pnl$dailyPnl(source="Test_source1",range=Range$after("1980-01-03")))
	shouldBombMatching(pnl$dailyPnl(source="BadSource"),"Source not loaded : BadSource")
	
	equity.eom <- Interval$MONTHLY$collapse(expected,sum)
	index(equity.eom) <- format(index(equity.eom),"%Y %b") 
	checkSame(equity.eom, pnl$monthlyPnl(source="Test_source1"))
}

test.dailyReturns <- function(){
	pnl <- PnlAnalysis()
	pnl$addPnlGroup("Group3",c("Test.Tag3"))
	pnl$loadPnl(source="Test_source2")
	shouldBombMatching(pnl$dailyReturns(source="Test_source2"),"Cannot calculate returns before AUM loaded.")		

	pnl$loadAUM("Test.AUM1")
	expected <- zoo(c(5,10,11,12,13,14),as.POSIXct(c('1979-12-30','1980-01-02','1980-01-04','1980-01-05','1980-01-06','1980-01-07')))
	checkSame(expected, pnl$aum())
	
	expected <- zoo(c(0.8,0.9,0.9090909,0.9166667), as.POSIXct(c('1980-01-03','1980-01-04','1980-01-05','1980-01-07')))
	checkSameLooking(expected, round(pnl$dailyReturns(source="Test_source2"),7))
}

test.monthlyReturns <- function(){	
	pnl <- objectSetup()
	pnl$loadPnl(source="Test_source1")
	shouldBombMatching(pnl$monthlyReturns(source="Test_source1"),"Cannot calculate returns before AUM loaded.")
	
	pnl$loadAUM("Test.AUM1")
	expected <- zoo(c(5,14),c("1979 Dec","1980 Jan"))
	checkSame(expected, pnl$aum(endOfMonth=TRUE))
	
	expected1 <- zoo(c(2491,86.5714),c("1980 Jan", "2009 Jan"))
	expected2 <- zoo(c(4470.6,86.5714),c("1980 Jan", "2009 Jan"))
	expected3 <- zoo(c(6961.6,173.1429),c("1980 Jan", "2009 Jan"))
	expected <- merge.zoo(expected1,expected2,expected3)
	colnames(expected) <- c('Group1','Group2','Total')
	
	checkSame(expected, round(pnl$monthlyReturns(source="Test_source1"),4))	
}

test.differenceGrid <- function(){
	checkDf <- function(expected, result){
		checkSame(NCOL(expected), NCOL(result))
		for (x in 1:NCOL(result))
			checkSame(as.vector(expected[,x]), as.vector(result[,x]))
	}
	
	pnl <- objectSetup()
	pnl$loadPnl(source="Test_source1")
	pnl$loadPnl(source="Test_source3")
	
	expected <- data.frame(Group=c('Group1','Group2','TOTAL'),Test_source1=c(55,9953,10008),Test_source3=c(-55,-55,-110),Diff=c(110,10008,10118))
	result <- pnl$pnlDifference(sources=c('Test_source1','Test_source3'),range=Range('1980-01-04','1980-01-04'))
	checkDf(expected, result)
	
	#If the range is more than one date, it sums over the period
	expected <- data.frame(Group=c('Group1','Group2','TOTAL'),Test_source1=c(13667,23565,37232),Test_source3=c(10032,10032,20064),Diff=c(3635,13533,17168))
	result <- pnl$pnlDifference(sources=c('Test_source1','Test_source3'))
	checkDf(expected, result)
	
	#Just a list of groups
	expected <- data.frame(Group=c('Group1','TOTAL'),Test_source1=c(1212,1212),Test_source3=c(-12,-12),Diff=c(1224,1224))
	result <- pnl$pnlDifference(groups='Group1',sources=c('Test_source1','Test_source3'),range=Range$after('1980-01-05'))
	checkDf(expected,result)
	
	shouldBombMatching(pnl$pnlDifference(sources=c('JustOneSource')),'Sources input must be a vector of 2 sources.')
}

test.differenceZoo <- function(){
	pnl <- objectSetup()
	pnl$loadPnl(source="Test_source1")
	pnl$loadPnl(source="Test_source3")
	
	dates <- as.POSIXct(c('1980-01-02','1980-01-03','1980-01-04','1980-01-05','2009-01-09'))
	source1.tag1 <- zoo(c(12345,55,55,NA,1212),dates)
	source2.tag1 <- zoo(c(10000,NA,-55,99,-12),dates)
	diff.tag1 <- source1.tag1 - source2.tag1
	cumDiff.tag1 <- cumsum(na.omit(diff.tag1))
	expected.group1 <- merge(source1.tag1,source2.tag1,diff.tag1,cumDiff.tag1)
	colnames(expected.group1) <- c('Test_source1','Test_source3','Diff','CumDiff') 
			
	#Just one tag returns a zoo
	checkSame(expected.group1, pnl$pnlDifferenceZoo(groups='Group1',sources=c('Test_source1','Test_source3')))
	
	#multiple groups returns a list
	source1.tag2 <- zoo(c(12345,55,9953,NA),dates[1:4])
	source2.tag2 <- zoo(c(10000,NA,-55,99),dates[1:4])
	diff.tag2 <- source1.tag2 - source2.tag2
	cumDiff.tag2 <- cumsum(na.omit(diff.tag2))
	expected.group2 <- merge(source1.tag2,source2.tag2,diff.tag2,cumDiff.tag2) 
	colnames(expected.group2) <- c('Test_source1','Test_source3','Diff','CumDiff')
	
	expected <- list(Group1=expected.group1[1:4,], Group2=expected.group2)
	checkSame(expected, pnl$pnlDifferenceZoo(sources=c('Test_source1','Test_source3'),range=Range$before('2009-01-01')))

	#Replace NA with zero
	source1.tag1[is.na(source1.tag1)] <- 0
	source2.tag1[is.na(source2.tag1)] <- 0
	diff.tag1 <- source1.tag1 - source2.tag1
	cumDiff.tag1 <- cumsum(na.omit(diff.tag1))
	expected.group1 <- merge(source1.tag1,source2.tag1,diff.tag1,cumDiff.tag1)
	colnames(expected.group1) <- c('Test_source1','Test_source3','Diff','CumDiff') 
	checkSame(expected.group1, pnl$pnlDifferenceZoo(groups='Group1',sources=c('Test_source1','Test_source3'),replaceNA=0))	
}
