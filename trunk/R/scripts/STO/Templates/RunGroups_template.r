#Required Libraries
library(QFSTO)
	
#Construct the object
	rg <- RunGroups(systemID=systemID.in)

#See what the parameters are in the underlying STO	
	rg$stoSetupObject()$parameters()
	
#RunGroup = nATRentry_1.25, set up
	rg$addFilter('ATRLen','>=','30')
	rg$addFilter('ATRLen','<=','35')
	rg$addFilter('nDays', '==', '10')
	rg$addFilter('nATRentry','==','1.25')
	rg$addFilter('exitATRmultiple','==','2.5')
	rg$addFilter('entryBarWindow','==','1')
	rg$addFilter('stopAfStep','>=','0.04')

#Look at the filter, how long, then add to the group			
	rg$filters()
	length(rg$filtersRuns())

	rg$addGroup('nATRentry_1.25','EquityFutures')

#Clear the filters and do another	
	rg$clearFilters()
	
	#RunGroup = nATRentry_1.5
	
	rg$addFilter('ATRLen','>=','30')
	rg$addFilter('ATRLen','<=','35')
	rg$addFilter('nDays', '==', '10')
	rg$addFilter('nATRentry','==','1.5')
	rg$addFilter('exitATRmultiple','==','2.5')
	rg$addFilter('entryBarWindow','==','1')
	rg$addFilter('stopAfStep','>=','0.04')
	
	rg$filters()
	length(rg$filtersRuns())
	
	rg$addGroup('nATRentry_1.5','EquityFutures')

#Now save the groups to the STO files.			
	rg$saveToFile()
	
# See all the group names
	rg$groupNames()	

# Run the run Groups reports
	rgr <- RunGroupsReport(rg)
	rgr$generateReports()
				
#If you close R and want to load the run groups back just:
	rg <- RunGroups(systemID=systemID.in)
	rg$loadFromFile()
