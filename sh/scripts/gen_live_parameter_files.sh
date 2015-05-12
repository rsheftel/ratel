#! /bin/sh

rm /data/STProcess/Environ/Params/* 
cd /home/simdata/svn/systematic/Java/systematic/lib
java -classpath ./\* systemdb.qworkbench.LiveParameterFileGenerator /data/STProcess/Environ/Params
