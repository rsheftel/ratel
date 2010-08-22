#
#	Test of STOSetup class  (rsheftel)
#
#################################################################

library(QFSTO)
testdata <- squish(system.file("testdata", package="QFSTO"),'/STOSetup/')

setupBasic <- function(sto){
	sto$strategyClass("Test")
	sto$systemQClassName("TestQClass.c")
	sto$addPortfolios(filename=squish(testdata,'TestPortfolios.csv'))
	sto$addParameters(name='Param1',start=0,end=1,step=0.5)
	sto$addParameters(name='Param2',start=10,end=100,step=1)
}

test.constructor <- function(){
	shouldBomb(STOSetup())
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID")
	checkInherits(sto, "STOSetup")
}

test.basicGetters <- function(){
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=squish(testdata,"More"), stoID="NewID")
	checkSame(squish(testdata,"More"),sto$stoDirectory())
	checkSame("NewID", sto$stoID())
	checkSame("NewSystem", sto$system())
	checkSame("daily", sto$interval())
	checkSame("1.0", sto$version())
}

test.addMarkets <- function(){
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID")
	sto$addMarkets(markets='Market1')
	sto$addMarkets(markets=c('Market2','Market3'))
	checkSame(c('Market1','Market2','Market3'), sto$markets())
	
	sto$addMarkets(filename=squish(testdata,'MarketsList.csv'))
	checkSame(c('Market1','Market2','Market3','Market4','Market5','Market6'), sto$markets())
	
	shouldBomb(addMarkets())
	shouldBomb(addMarkets(markets='x', filename='y'))
	
	sto$addMarkets(markets='Market1')	#Adding the market again does nothing, its aleady there
	checkSame(c('Market1','Market2','Market3','Market4','Market5','Market6'), sto$markets())
}

test.addPortfolio <- function(){
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID")
	sto$addPortfolio(name='Portfolio1',markets='Market1')
	sto$addPortfolio(name='Portfolio2',markets=c('Market1','Market2'))
	
	checkSame(c('Portfolio1','Portfolio2'), sto$portfolioNames())
	checkSame(c('Market1','Market2'), sto$markets())
	
	shouldBomb(sto$addPortfolio(name='Portfolio3'))
	shouldBomb(sto$addPortfolio(name='Portfolio2',markets=c('Market2')))

	sto$addPortfolios(filename=squish(testdata,'TestPortfolios.csv'))
	
	checkSame(c('Portfolio1','Portfolio2','Portfolio3','Portfolio4'), sto$portfolioNames())
	checkSame(c('Market1','Market2','Market8','Market9'), sto$markets())
	
	shouldBomb(sto$addMarketsToPortfolio(portfolio='Portfolio999', markets=c('Market9','Market10')))
	sto$addMarketsToPortfolio(portfolio='Portfolio2', markets=c('Market9','Market10'))
	checkSame(c('Market1','Market2','Market9','Market10'),sto$portfolios()[['Portfolio2']])
}

test.addParameters <- function(){
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID")
	
	shouldBomb(sto$addParameters(name='Param1',start=0,stop=1,step=0.7))
	
	sto$addParameters(name='Param1',start=0,end=1,step=0.5)
	expected <- data.frame(name='Param1',start=0,end=1,step=0.5,count=3,total=3)
	checkSame(expected, sto$parameters())

	sto$addParameters(name='Param2',start=10,end=100,step=1)
	expected <- rbind(expected, data.frame(name='Param2',start=10,end=100,step=1,count=91,total=273))
	checkSame(expected, sto$parameters())
	
	shouldBombMatching(sto$addParameters(name='Param2',start=10,end=100,step=1),'Parameter already exists : Param2')
	
	sto$updateParameter(name='Param1',end=2)
	sto$updateParameter(name='Param1',start=1,step=1)
	
	expected <- data.frame(name='Param1',start=1,end=2,step=1,count=2,total=2)
	expected <- rbind(expected, data.frame(name='Param2',start=10,end=100,step=1,count=91,total=182))
	checkSame(expected, sto$parameters())
	
	shouldBomb(sto$updateParameter(name='Param999',end=2))
	
	#Add multiple parameters
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID")
	sto$addParameters(name=c('Param1','Param2'),start=c(1,10),end=c(2,100),step=c(1,1))
	expected <- data.frame(name='Param1',start=1,end=2,step=1,count=2,total=2)
	expected <- rbind(expected, data.frame(name='Param2',start=10,end=100,step=1,count=91,total=182))
	checkSame(expected, sto$parameters())
	
	#Add zero step parameter
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID")
	sto$addParameters(name="NoStep", start=10, end=10, step=0)
	expected <- data.frame(name='NoStep',start=10,end=10,step=0,count=1,total=1)
	checkSame(expected, sto$parameters())
}

test.createSTOFiles <- function(){
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID1")
	sto$addPortfolios(filename=squish(testdata,'TestPortfolios.csv'))
	sto$addParameters(name='Param1',start=0,end=1,step=0.5)
	sto$addParameters(name='Param2',start=10,end=100,step=1)
	
	sto$createSTOfiles()
	checkSame(TRUE, file.exists(squish(testdata,'NewID1/','CurvesBin')))
	checkSame(TRUE, file.exists(squish(testdata,'NewID1/','Metrics')))
	fileMatches(squish(testdata,'NewID1/Parameters/NewSystem_1.0_daily.csv'),squish(testdata,'NewSystem_1.0_daily.csv'))
	
	shouldBomb(sto$createSTOfiles()) #Cannot create twice
	#Cannot do anything after the STO files are created
	shouldBomb(sto$addPortfolios(filename=squish(testdata,'TestPortfolios.csv'))) 
	shouldBomb(sto$addMarkets(markets='Market100'))
	shouldBomb(sto$addMarketsToPortfolio(portfolio='Portfolio2', markets=c('Market9','Market10')))
	shouldBomb(sto$addParameters(name='Param100',start=0,end=1,step=0.5))
	shouldBomb(sto$updateParameter(name='Param1',end=2))
}

test.prepForSystemDB <- function(){
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID2")
	sto$strategyClass("Test")
	checkSame("Test",sto$strategyClass())
	sto$strategyClass("NotInDB")
	checkSame("NotInDB", sto$strategyClass())
	sto$systemQClassName("TestQClass.c")
	checkSame("TestQClass.c",sto$systemQClassName())
}

test.startEndDates <- function(){
	checkDates <- function(){
		names(expected.start) <- sto$markets()
		names(expected.end) <- sto$markets()
		checkSame(list(start=expected.start,end=expected.end), sto$startEndDates())
	}
	
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID2")
	setupBasic(sto)
	
	shouldBomb(sto$startEndDates(start=19000101, end=20001231))

	sto$startEndDates(start='19000101', end='20001231')
	expected.start  <- rep('19000101',4)
	expected.end	<- rep('20001231',4)
	checkDates()
	
	sto$startEndDates(start=as.POSIXct('2000-01-01'))
	expected.start <- rep('20000101',4)
	checkDates()
	
	sto$startEndDates(end=as.POSIXct('2010-06-30'))
	expected.end   <- rep('20100630',4)
	checkDates()
	
	namedVec <- c('19500405','19200615')
	names(namedVec) <- c('Market1','Market9')
	sto$startEndDates(start=namedVec,end=namedVec)
	expected.start[c(1,4)] <- namedVec
	expected.end[c(1,4)] <- namedVec
	checkDates()
	
	names(namedVec) <- c('Market1','BadMarket')
	shouldBombMatching(sto$startEndDates(start=namedVec),"Bad market names.")
	
	sto$startEndDates(filename=squish(testdata,'TestPortfolios.csv'))
	expected.start <- c('19500405','20000202','19950910','19000401')
	expected.end   <- c('19600808','20100630','19911110','19900501')
	checkDates()
	
	sto$clearDates()
	sto$startEndDates(start="19000515",end="19200915",filename=squish(testdata,'TestPortfolios.csv'))
	expected.start <- c('19000515','20000202','19950910','19000401')
	expected.end   <- c('19600808','19200915','19911110','19900501')
	checkDates()
	
	shouldBombMatching(sto$startEndDates(start=c('19000101','19900101')),"Vector of more than one date must be named.")
	shouldBombMatching(sto$startEndDates(end=c('19000101','19900101')),"Vector of more than one date must be named.")
}

test.portfolioWeights <- function(){
	sto <- STOSetup(system="TestSystem1", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID3")
	setupBasic(sto)
	checkSame('SumToOne',sto$portfolioWeights())
	sto$portfolioWeights(method='EachIsOne')
	checkSame('EachIsOne',sto$portfolioWeights())
	shouldBomb(sto$portfolioWeights(method="NotAMethod"))
}

test.uploadToSystemDB <- function(){
	sto <- STOSetup(system="TestSystem1", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID3")
	#Bombs before everything is set up it needs
	shouldBomb(sto$uploadToSystemDB(commitToDB=FALSE))
	
	sto <- STOSetup(system="TestSystem1", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID3")
	setupBasic(sto)
	sto$startEndDates(start="19000515",end="19200915",filename=squish(testdata,'TestPortfolios.csv'))
	sto$uploadToSystemDB(commitToDB=FALSE)
	
	#Cannot commit to DB twice
	shouldBomb(sto$uploadToSystemDB(commitToDB=FALSE))
	#Fails to try and change anything after upload
	shouldBomb(sto$addPortfolios(filename=squish(testdata,'TestPortfolios.csv'))) 
	shouldBomb(sto$addMarkets(markets='Market100'))
	shouldBomb(sto$addMarketsToPortfolio(portfolio='Portfolio2', markets=c('Market9','Market10')))
	shouldBomb(sto$addParameters(name='Param100',start=0,end=1,step=0.5))
	shouldBomb(sto$updateParameter(name='Param1',end=2))
}

test.uploadToSystemDBDuplicateStoID <- function(){
	sto <- STOSetup(system="TestSystem1", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID")
	setupBasic(sto)
	sto$startEndDates(start="19000515",end="19200915",filename=squish(testdata,'TestPortfolios.csv'))
	shouldBombMatching(sto$uploadToSystemDB(commitToDB=FALSE),'Cannot upload to SystemDB, stoID already exists: NewID')
}

test.systemID <- function(){
	sto <- STOSetup(system="TestSystem1", interval="daily", version="1.0", stoDirectory="v:/Temp/StoDir", stoID="StoID001")
	checkSame(128579, sto$systemID())
}

test.privateMethods <- function(){
	##########################################################
	# This is a test of private methods, no NOT use directly
	##########################################################
	
	sto <- STOSetup(system="TestSystem1", interval="daily", version="1.0", stoDirectory='NewStoDir', stoID="NewID3")
	setupBasic(sto)
	sto$startEndDates(start="19000515",end="19200915",filename=squish(testdata,'TestPortfolios.csv'))
	
	############################################################################	
	#Private methods to set up the parts of systemDB, do not use these
	
	sto$.commitToDB=FALSE
	outfile <- sto$.uploadParameters()
	fileMatches(outfile,squish(testdata,'SysDB_parameters.csv'))
	outfile <- sto$.uploadSystemStrategy()
	fileMatches(outfile,squish(testdata,'SysDB_systemStrategy.csv'))
	outfile <- sto$.uploadSystemDetails()
	fileMatches(outfile,squish(testdata,'SysDB_systemDetails.csv'))
	
	#Force change the markets for the next test to work
	sto$.markets <- c("TEST1",'TEST2','TEST3','TEST4','TEST5')
	outfile <- sto$.uploadMSIVs()
	fileMatches(outfile,squish(testdata,'SysDB_MSIVs.csv'))
	
	sto$clearDates()
	sto$startEndDates(start="19910203", end="20010405")
	sto$.markets <- c("TEST1",'TEST4','TEST5')
	outfile <- sto$.uploadMSIVBacktest()
	fileMatches(outfile,squish(testdata,'SysDB_MSIVBacktest.csv'))
	
	
	#Test with a system name that is new
	sto <- STOSetup(system="TestSystemNewSTO", interval="daily", version="1.0", stoDirectory='NewStoDir', stoID="NewID3")
	setupBasic(sto)
	sto$.commitToDB=FALSE
	outfile <- sto$.uploadStrategy()
	fileMatches(outfile,squish(testdata,'SysDB_strategyTables.csv'))
	outfile <- sto$.uploadSystem()
	fileMatches(outfile,squish(testdata,'SysDB_systemTable.csv'))	
}

test.private.uploadPortfolios <- function(){
	sto <- STOSetup(system="TestSystem1", interval="daily", version="1.0", stoDirectory='NewStoDir', stoID="NewID4")
	sto$strategyClass("Test")
	sto$systemQClassName("TestQClass.c")
	sto$addPortfolios(filename=squish(testdata,'TestPortfoliosInDB.csv'))
	sto$addParameters(name='Param1',start=0,end=1,step=0.5)
	sto$addParameters(name='Param2',start=10,end=100,step=1)
	
	outfile <- sto$.uploadPortfolioBacktest()
	fileMatches(outfile, squish(testdata,'SysDB_portfolioBacktest.csv'))
	sto$portfolioWeights(method="EachIsOne")
	outfile <- sto$.uploadPortfolioBacktest()
	fileMatches(outfile, squish(testdata,'SysDB_portfolioBacktest_EachIsOne.csv'))
}

test.constructFromDB <- function(){
	checkDates <- function(){
		names(expected.start) <- sto$markets()
		names(expected.end) <- sto$markets()
		checkSame(list(start=expected.start,end=expected.end), sto$startEndDates())
	}
	
	sto <- STOSetup(system="TestSystem5", interval="daily", version="1.0", stoDirectory="StoDir", stoID="NewID")
	
	#Must hand set set the stoDir
	sto$.stoDirectory <- leftStr(testdata,nchar(testdata)-1)
	
	sto$loadFromSystemDB()
	
	#This check is because of the hand set of the stoDir
	checkSame(leftStr(testdata,nchar(testdata)-1), sto$stoDirectory())
	checkSame("Test", sto$strategyClass())
	checkSame("TestQClass.c", sto$systemQClassName())
	checkSame('daily', sto$interval())
	checkSame('1.0', sto$version())
	checkSame('NewID', sto$stoID())
	checkInherits(sto$stoObject(),'STO')
	checkSame(c('TEST1','TEST2','TEST3','TEST4'), sto$markets())
	checkSame(c('Port1','Port2'), sto$portfolioNames())
	checkSame(list(Port1=c('TEST1','TEST2'), Port2=c('TEST3','TEST4')), sto$portfolios())
	
	expected.start <- c('20000101','20000101','19990630','19990630')
	expected.end   <- c('20090130','20051231','20090130','20090130')
	checkDates()
			
	expected <- data.frame(name=c('Param1','Param2'), start=c(1,2), end=c(3,8), step=c(1,2), count=c(3,4), total=c(3,12))
	checkSame(expected, sto$parameters())
}

test.constructFromSystemID <- function(){
	sto <- STOSetup(systemID=128579)
	checkSame(sto$system(),"TestSystem1")
	checkSame(sto$interval(),"daily")
	checkSame(sto$version(),"1.0")
	checkSame(sto$stoDirectory(),makeFilenameNative("v:/Temp/StoDir"))
	checkSame(sto$stoID(),"StoID001")
}