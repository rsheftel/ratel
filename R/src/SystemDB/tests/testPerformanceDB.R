# PerformanceDB tests
# 
# Author: RSheftel
###############################################################################

library(SystemDB)

test.constructor <- function(){
	pdb <- PerformanceDB()
	checkInherits(pdb, "PerformanceDB")
}

test.tagList <- function(){
	tags <- PerformanceDB$tags()
	checkSame(TRUE, all(c("Test.Tag1","Test.Tag2") %in% tags))
}

test.sourceList <- function(){
	sources <- PerformanceDB$sources()
	checkSame(TRUE, all(c("Test_source1","Test_source2") %in% sources))
}

test.groupList <- function(){
	groups <- PerformanceDB$groups()
	checkSame(TRUE, all(c("TestTagGroup1") %in% groups))
}

test.IDs <- function(){
	shouldBomb(PerformanceDB$tagID("BadTag"))
	shouldBomb(PerformanceDB$sourceID("BadSource"))
	checkSame("2", PerformanceDB$tagID("Test.Tag1"))
	checkSame("3", PerformanceDB$sourceID("Test_source1"))
}

test.getPositionEquityCurve <- function(){
	curve <- PerformanceDB$getPositionEquityCurve(source="Test_source1",tag="Test.Tag1")
	expected <- zoo(c(12345,55,55,1212),as.POSIXct(c('1980-01-02','1980-01-03','1980-01-04','2009-01-09')))
	checkInherits(curve,"PositionEquityCurve")
	checkSameLooking(expected, curve$pnl())
	
	curve <- PerformanceDB$getPositionEquityCurve(source="Test_source1",tag="Test.Tag1",range=Range("1980-01-03","1980-12-31"))
	expected <- zoo(c(55,55),as.POSIXct(c('1980-01-03','1980-01-04')))
	checkSameLooking(expected, curve$pnl())
	
	#No data for a source or tag returns NULL
	checkSame(NULL, PerformanceDB$getPositionEquityCurve(source="Test_source3",tag="Test.Tag2"))	 
}

test.insertPnL <- function(){
	shouldBombMatching(PerformanceDB$insertPnL(	startDate=as.POSIXct('1990-01-01'),
												endDate=as.POSIXct('1990-01-02'),
												source="Test_source2",
												tag="BadTag",
												pnl=99,
												commitToDB=FALSE),
						'Not a valid tag: BadTag')
	
	shouldBombMatching(PerformanceDB$insertPnL(	startDate=as.POSIXct('1990-01-01'),
												endDate=as.POSIXct('1990-01-02'),
												source="BadSource",
												tag="Test.Tag2",
												pnl=99,
												commitToDB=FALSE),
						'Not a valid source: BadSource')
					
	uploadFile <- PerformanceDB$insertPnL(	startDate=as.POSIXct('1990-01-01'),
											endDate=as.POSIXct('1990-01-02'),
											source="Test_source2",
											tag="Test.Tag2",
											pnl=99,
											commitToDB=FALSE)
										
	expected <- data.frame(	PerformanceDB..Pnl.startDate = 19900101,
							PerformanceDB..Pnl.endDate = 19900102,
							PerformanceDB..Pnl.source_id = 4,
							PerformanceDB..Pnl.tag_id = 3,
							PerformanceDB..Pnl.pnl = 99)
										
	checkSame(expected, read.csv(uploadFile))
}

test.updatePnL <- function(){
	uploadFile <- PerformanceDB$updatePnL(startDate=as.POSIXct('1990-01-01'),
											endDate=as.POSIXct('1990-01-02'),
											source="Test_source2",
											tag="Test.Tag2",
											pnl=99,
											commitToDB=FALSE)
	
		#Note that when reading the csv, the '*' are turned into '.'
	expected <- data.frame(	PerformanceDB..Pnl.startDate. = 19900101,
							PerformanceDB..Pnl.endDate. = 19900102,
							PerformanceDB..Pnl.source_id. = 4,
							PerformanceDB..Pnl.tag_id. = 3,
							PerformanceDB..Pnl.pnl = 99)
	
	checkSame(expected, read.csv(uploadFile))
}

test.tagGroup <- function(){
	checkSame(c("Test.Tag1","Test.Tag2"), PerformanceDB$tagsForTagGroup("TestTagGroup1"))
}

test.InvestorData <- function(){
	checkSame(TRUE, "Test.AUM1" %in% PerformanceDB$investorDataNames())	

	aum <- PerformanceDB$investorData(name="Test.AUM1")
	expected <- zoo(c(5,10,11,12,13,14),as.POSIXct(c('1970-12-30','1980-01-02','1980-01-04','1980-01-05','1980-01-06','1980-01-07')))
	checkSameLooking(expected, aum)
	
	aum <- PerformanceDB$investorData(name="Test.AUM1", range=Range(start='1980-01-03', end='1980-01-06'))
	expected <- zoo(c(11,12,13),as.POSIXct(c('1980-01-04','1980-01-05','1980-01-06')))
	checkSameLooking(expected, aum)
}

test.uploadGlobeOpDailyPnl <- function(){
	if(!isWindows()) return(TRUE)
	baseDir = squish(dataDirectory(),'PerformanceDB_upload')
	uploadDir = 'Upload'
	archiveDir = 'Archive'	
	# No file
	checkSame(NULL,PerformanceDB()$uploadGlobeOpDailyPnl(baseDir,uploadDir,archiveDir,'doesnotexist.csv'))
	# File is here
	exampleFrame <- data.frame(
		TAG = c('QF.NDayBreak','QF.DTD_USD'),
		DailyPNL = c(250,-120),
		MonthlyPNL = c(0,50)
	)
	fileName <- squish(baseDir,'/',uploadDir,'/PNL_2009-04-03_To_2009-04-04.csv')
	write.table(exampleFrame,fileName,sep = ',',col.names = TRUE,quote = FALSE,row.names = FALSE)
	checkSame(TRUE,file.exists(fileName))
	resultFrame <- PerformanceDB()$uploadGlobeOpDailyPnl(baseDir,uploadDir,archiveDir,'PNL_2009-04-03_To_2009-04-04.csv')
	checkShape(resultFrame,2,2,colnames = c('tag','result'))
	checkSame(resultFrame[1,1],'QF.NDayBreak')
	checkSame(FALSE,file.exists(fileName))
	fileName <- squish(baseDir,'/',archiveDir,'/PNL_2009-04-03_To_2009-04-04.csv')
	checkSame(TRUE,file.exists(fileName))
	file.remove(fileName)
	checkSame(FALSE,file.exists(fileName))
}

test.insertTag <- function(){
	testfile <- PerformanceDB$insertTag('NewTag', commitToDB=FALSE)
	actual <- read.csv(testfile)
	expected <- data.frame(PerformanceDB..Tag.name='NewTag')
	checkSame(expected, actual)  
}


test.insertTagGroup <- function(){
	testfile <- PerformanceDB$insertTagGroup('NewTagGroup', commitToDB=FALSE)
	actual <- read.csv(testfile)
	expected <- data.frame(PerformanceDB..Tag_Group.name='NewTagGroup')
	checkSame(expected, actual)  
}

test.insertTagToTagGroup <- function(){
	testfile <- PerformanceDB$insertTagToTagGroup('Test.Tag1','TestTagGroup1')
	actual <- read.csv(testfile)
	expected <- data.frame(PerformanceDB..Tag_Group_Members_Tag.tag_group_id=1,  PerformanceDB..Tag_Group_Members_Tag.tag_id=2)
	checkSame(expected, actual)  
	
}
