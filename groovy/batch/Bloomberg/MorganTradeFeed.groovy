#!/usr/bin/env groovy

import java.text.SimpleDateFormat

SEPARATOR = File.separator
Calendar today = Calendar.getInstance()
SimpleDateFormat gatewayDirFormat = new SimpleDateFormat("MM-dd-yy")
SimpleDateFormat archiveDirFormat = new SimpleDateFormat("MM-dd-yyyy")
SimpleDateFormat archiveFileFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS")
gatewayDirName = gatewayDirFormat.format(today.getTime())
archiveDirName = archiveDirFormat.format(today.getTime())


msDir = new File("\\\\nysrv31\\MorganStanley\\" + gatewayDirName)
//exit if there is no directory for the trade files(i.e. Sat, Sun)
if (!msDir.exists()){
    System.exit(0)
}
ftpShareDir = new File("\\\\nysrv31\\FTPShare\\MorganStanley\\")
archiveDir = new File("c:\\BloombergArchive\\MorganStanley\\" + archiveDirName)
if (!archiveDir.exists()){
    archiveDir.mkdir()
}

todayAllCdsFile = new File(archiveDir.absolutePath + SEPARATOR + "todayAllCdsTrades.csv")
todayAllFxFile = new File(archiveDir.absolutePath + SEPARATOR + "todayAllFxTrades.csv")
todayAllRepoFile = new File(archiveDir.absolutePath + SEPARATOR + "todayAllRepoTrades.csv")
if (!todayAllCdsFile.exists()){
    todayAllCdsFile.createNewFile()
}
if (!todayAllFxFile.exists()){
    todayAllFxFile.createNewFile()
}
if (!todayAllRepoFile.exists()){
    todayAllRepoFile.createNewFile()
}
csvFiles = []
msDir.eachFile{csvFiles << it}
Calendar now = Calendar.getInstance()
csvFiles.each{
    csv ->
    	//CDS file
    	if(csv.name.equalsIgnoreCase("CDSTrades.csv")){
			def archiveFile = new File (archiveDir.absolutePath + SEPARATOR + "Cds_Malbec_" + archiveFileFormat.format(now.getTime()) + ".csv")
        	createDiffFiles(todayAllCdsFile, csv, archiveFile)
	    }
    	//FX file
    	else if (csv.name.equalsIgnoreCase("FXTrades.csv")){
			def archiveFile = new File (archiveDir.absolutePath + SEPARATOR + "Fx_Malbec_" + archiveFileFormat.format(now.getTime()) + ".csv")
        	createDiffFiles(todayAllFxFile, csv, archiveFile)
    	}
    	//REPO file
		else if (csv.name.equalsIgnoreCase("RepoTrades.csv")){
		    def archiveFile = new File (archiveDir.absolutePath + SEPARATOR + "Repos_Malbec_" + archiveFileFormat.format(now.getTime()) + ".csv")
        	createDiffFiles(todayAllRepoFile, csv, archiveFile)
    	}
}

def createDiffFiles(File todayAllFile, File csvFile, File outputFile){
   	def todayAllLines = []
	def csvTradesLines = []
	todayAllFile.eachLine{todayAllLines << it}
	csvFile.eachLine{csvTradesLines << it}
	def todayAllSize = todayAllLines.size()
	def csvSize = csvTradesLines.size()
	for (i in 0 ..< csvSize){
	    if (i <= todayAllSize){
	        if (!csvTradesLines[i].equalsIgnoreCase(todayAllLines[i])){
	            outputFile.append(csvTradesLines[i] + "\n")	    
	        }
	    }
	    else{
	        outputFile.append(csvTradesLines[i] + "\n")
	    }
	}
	copyFile(csvFile, todayAllFile)
	//Do the encryption with Morgan Stanley's public key and put in share directory
	if (executeCommand("cmd /c gpg -se -r 75299CFA -a --passphrase-fd 0 < MalbecPassPhrase.txt " + outputFile.absolutePath) == 0){
		def gpgFile = new File(outputFile.absolutePath + ".asc")
		def ftpShareFile = new File(ftpShareDir.absolutePath + SEPARATOR + gpgFile.name)
		copyFile(gpgFile, ftpShareFile)
	}
}

def copyFile(File source, File destination) {
    def reader = source.newReader()//need to support binary
    destination.withWriter { writer ->
        writer << reader
    }
    reader.close()
}

def int executeCommand(String commandToExecute){
    println commandToExecute
    Process command = Runtime.getRuntime().exec(commandToExecute)
    //Need to wait until the system command has finished
    BufferedReader Resultset = new BufferedReader(
        new InputStreamReader (
            command.getInputStream()));
	String line;
	while ((line = Resultset.readLine()) != null) {
		println(line);
	}
	println "Finished command" 
	return command.waitFor()
}
    
