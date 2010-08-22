#! /tp/bin/Rscript --no-site-file --no-init-file

library("GSFCore")
status <- TimeSeriesBatchUpload$run()
quit(save="no", status = status)

