# Author: jbourgeois
###############################################################################

library(SystemDB)

financialCenter <- "nyb"
destination <- c('team','daria.castagna@malbecpartners.com','wilton.cheung@malbecpartners.com')

args <- commandArgs()
args <- args[-(1:match("--args", args))]

dateList <- list(
	refBusinessDate = businessDaysAgo(1,args,financialCenter),
	lastBusinessDate = as.Date(args)
)

resFrame <- PerformanceDB$uploadGlobeOpDailyPnl(
	baseDir = squish(dataDirectory(),'PerformanceDB_upload'),
	uploadDir = 'Upload',
	archiveDir = 'Archive',
	fileName = squish('PNL_',as.character(dateList$refBusinessDate),'_To_',as.character(dateList$lastBusinessDate),'.csv'),
	TRUE,
	FALSE
)

dateRangeStr <- squish('from ',as.character(dateList$refBusinessDate),' to ',as.character(dateList$lastBusinessDate))

# Send an email and Exit if no file to load
if(is.null(resFrame)){
	sapply(destination,
		function(x)Mail$problem(subject='GlobeOp Pnl Upload File Not Found','')$sendTo(x)
	)
	quit(save="no", status = 1)
}

# Check if some records were not loaded
mailContent <- NULL
for (i in 1:NROW(resFrame)){
	if(!class(resFrame[,'result']) == 'logical' || !resFrame[,'result']){
		str <- as.character(resFrame[i,1:3])
		mailContent <- paste(
			mailContent,
			squish('No Update for ',str[1],'PNL ',dateRangeStr),
			sep = '\n'
		)
	}
}

if(!is.null(mailContent)){
	# Send error messages if some records were not loaded
	sapply(destination,
		function(x)Mail$problem(subject='GlobeOp Pnl Upload to PerformanceDB missing data',content=mailContent)$sendTo(x)
	)	
}else{
	# Send success message
	sapply(destination,
		function(x)Mail$notification(subject='GlobeOp Pnl Upload to PerformanceDB completed','')$sendTo(x)
	)	
}