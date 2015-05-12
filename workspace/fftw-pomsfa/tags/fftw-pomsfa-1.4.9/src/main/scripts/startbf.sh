#!/bin/sh

# Start the pomsfa
#
FA_HOME=`pwd`
FA_LIBS=${FA_HOME}/lib

JAR_LIB=`ls ${FA_LIBS}/*.jar`

# build CLASSPATH from the lib directory
LOCAL_CLASSPATH='.'

for name in ${JAR_LIB}
do
echo $name
	LOCAL_CLASSPATH=${LOCAL_CLASSPATH}:$name
done

echo ${LOCAL_CLASSPATH}

# export our classpath
CLASSPATH=LOCAL_CLASSPATH:CLASSPATH


java -classpath ${CLASSPATH} com.fftw.bloomberg.aggregator.AggregatorDriver

