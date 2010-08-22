#! /tp/bin/Rscript --no-site-file --no-init-file

args <- commandArgs()
args <- args[-(1:match("--args", args))]

wait.time.minutes <- as.numeric(args)

print("Job Running...")

Sys.sleep(wait.time.minutes * 60)

print("Job Done")
quit(save="no", status = 0)