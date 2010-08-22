constructor("DataUpload", function() {
    extend(RObject(), "DataUpload")
})

method("upload", "DataUpload", function(static, file, ...) { 
    JLog$setFile_by_String(squish(file, ".log"))
    JDataUpload$upload_by_StringArray(file)
})

generateCsvForUpload <- function(upload.databases, upload.tables, upload.fields, upload.values, upload.filename){
#Given the inputs, creates the csv file for upload
	
	if (length(upload.databases) !=1) if (length(upload.databases)!=length(upload.tables)) return(FALSE)
	if (length(upload.tables) != 1) if (length(upload.fields) != length(upload.tables)) return(FALSE)
	
	upload.header <- paste(upload.databases, upload.tables, sep="..")
	upload.header <- paste(upload.header, upload.fields, sep=":")
	
	if (is.null(nrow(upload.values))){	#single row values
		if (length(upload.fields) != length(upload.values)) return(FALSE)
		tempList = as.list(upload.values)
		names(tempList) = upload.header
		upload.df <- do.call(data.frame,tempList)
	}
	else {		#matrix of values
		if (length(upload.fields) != ncol(upload.values)) return(FALSE)
		upload.df <- as.data.frame(upload.values)
	}
	names(upload.df) <- upload.header
	write.table(upload.df, upload.filename, append=FALSE, quote=FALSE, row.names=FALSE, col.names=upload.header, sep=",", dec=".")
	return(TRUE)
}

uploadToDB <- function(upload.databases, upload.tables, upload.fields, upload.values, commitToDB=TRUE){
#generates a csv file from the inputs and uploads to SystemDB
#The commitToDB is used for testing, if set to false will generate the upload file and return the name of the file, but will not commit to the DB
	upload.filename <- tempfile(paste('uploadToDB',format(Sys.time(),"%Y%m%d_%H%M"),sep="_"), tmpdir=tempDirectory())	
	if(generateCsvForUpload(upload.databases, upload.tables, upload.fields, upload.values, upload.filename)==FALSE) return(FALSE)
	
	if (commitToDB){
		success <- DataUpload$upload(upload.filename)	
		rm(upload.filename, upload.tables, upload.fields, upload.values, upload.databases)
		return(success)
	}
	else
		return(upload.filename)
}