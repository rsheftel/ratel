
tri.filename <- 'C:/Users/Ryan Sheftel/Documents/My Dropbox/fncl_cc_tri.csv'
		
raw <- read.csv(tri.filename)

tri <- xts(raw[,2],as.POSIXct(raw[,1]))

