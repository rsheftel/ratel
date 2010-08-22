# System tests
# 
# Author: RSheftel
###############################################################################

library(SystemDB)

testdata <- squish(system.file("testdata", package="SystemDB"),'/PnlSetup/')

test.constructor <- function(){
	checkInherits(PnlSetup(), "PnlSetup")
}

test.setterGetters <- function(){
	pnl <- PnlSetup()
	checkSame(pnl$bloombergTag(),NULL)
	pnl$bloombergTag('QF.Test.Tag')
	checkSame(pnl$bloombergTag(),'QF.Test.Tag')
	checkSame(pnl$reportGroup(),NULL)
	pnl$reportGroup('report_daily_test')
	checkSame(pnl$reportGroup(),'report_daily_test')
	
	shouldBombMatching(pnl$bloombergTag('BadTag'), "Bad bloomberg tag name, must be of form 'QF.' : BadTag")
	shouldBombMatching(pnl$reportGroup('BadGroup'), "Bad report group name, must be of form 'report_daily_' : BadGroup")
}

test.status <- function(){
	pnl <- PnlSetup()
}

test.insertToPnlSetup <- function(){
	pnl <- PnlSetup()
	pnl$commitToDB(FALSE)
	
	#Test when bberg Tag already exists
	shouldBombMatching(pnl$insertPnlSetup(), 'Bloomberg Tag not set.')
	pnl$bloombergTag('QF.TestTag1')
	shouldBombMatching(pnl$insertPnlSetup(), 'Report Group not set.')
	pnl$reportGroup('report_daily_test')

	#Test private functions
	checkSame(pnl$.insertTag(),NULL)
	
	testfile <- pnl$.insertToAllTagsGroup()
	actual <- read.csv(testfile)
	expected <- data.frame(PerformanceDB..Tag_Group_Members_Tag.tag_group_id=4,  PerformanceDB..Tag_Group_Members_Tag.tag_id=45)
	checkSame(expected, actual)  
	
	testfile <- pnl$.insertReportGroup()
	actual <- read.csv(testfile)
	expected <- data.frame(PerformanceDB..Tag_Group.name='report_daily_test')
	checkSame(expected, actual)  
		
	#Change the report group to an actual one, then test.
	pnl$.reportGroup <- 'TestTagGroup1'	#this is for testing only, never really do this.
	testfile <- pnl$.insertTagToReportGroup()
	actual <- read.csv(testfile)
	expected <- data.frame(PerformanceDB..Tag_Group_Members_Tag.tag_group_id=1,  PerformanceDB..Tag_Group_Members_Tag.tag_id=45)
	checkSame(expected, actual)  
		
	#Confirm when a new tag it creates
	pnl$bloombergTag('QF.New')
	testfile <- pnl$.insertTag()
	actual <- read.csv(testfile)
	expected <- data.frame(PerformanceDB..Tag.name='QF.New')
	checkSame(expected, actual)  
}
