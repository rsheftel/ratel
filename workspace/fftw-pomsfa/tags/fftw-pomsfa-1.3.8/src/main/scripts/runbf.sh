#!/bin/sh
# This starts and ensures that the POMS Feed Aggregator keeps running
#
# Make sure only one of us is running at a time.
#
PROG_LOCK_FILE=/tmp/poms.lock
SCRIPT_PID=$$
echo "script PID ${SCRIPT_PID}"
PROG_LOG_FILE=logs/console.log


# check if we should actually start the process

## This should probably be in a loop
if [ -f ${PROG_LOCK_FILE} ]; then
echo "Found file ${PROG_LOCK_FILE}"
  # the lock file already exists, so what to do?
  if [ "$(ps -p `cat ${PROG_LOCK_FILE}` | wc -l)" -gt 1 ]; then
    # process is still running
    echo "$0: lingering process `cat ${PROG_LOCK_FILE}`"
    # wait 30 seconds incase it is taking longer to shutdown
    echo "waiting 30 seconds before killing"
    sleep 30;
    kill -KILL `cat ${PROG_LOCK_FILE}`
    echo "kille process `cat ${PROG_LOCK_FILE}`"
  else
    # process not running, but lock file not deleted?
    echo " $0: orphan lock file warning. Lock file deleted."
    rm ${PROG_LOCK_FILE}
  fi
else
  echo "No cleanup needed"
fi


# Start the pomsfa
#

#
# Setup the program variables
#
# assume we are in a bin or script directory
FA_HOME=`pwd`/..
FA_LIBS=${FA_HOME}/lib

JAR_LIB=`ls ${FA_LIBS}/*.jar`
LOCAL_CLASSPATH=''

# build the classpath to run with
for name in ${JAR_LIB}
do
   LOCAL_CLASSPATH=${LOCAL_CLASSPATH}:$name
done

echo "would use this as classpath ${LOCAL_CLASSPATH}"

STARTUP_JAR=
# find the startup jar
POSSIBLE_JARS=`ls ${FA_HOME}/fftw-pomsfa*.jar`

for name in ${POSSIBLE_JARS}
do
   STARTUP_JAR=$name
   break; # take the first jar
done

start_job() {
	echo "java -jar ${STARTUP_JAR}"
	java -jar ${STARTUP_JAR}  >  ${PROG_LOG_FILE}-${TIME} 2>&1 & 
	PROG_ID=$!
	echo ${PROG_ID} > ${PROG_LOCK_FILE}
	echo "program PID ${PROG_ID}"
	# sleep for 1 second to give the process time to start and crash
	sleep 1;
}

# change to the home directory so all the logs and other directories are created correctly
cd ${FA_HOME}

start_job
attempt=1

# repeat for ever
while [ 1 ]
do
	sleep 60  # one minute
	echo "Checking"
	if [ ! "$(ps -p `cat ${PROG_LOCK_FILE}` | wc -l)" -gt 1 ]; then
		echo "process died!"
		if [ ${attempt} -gt 4 ]; then
			echo "Giving up! max attempts made to restart ${attempt}"
			echo "POMS Aggregator failed.  \nCheck log file." | mail -s "POMS Aggregator failed" Sim_Team@fftw.com,mfranz@fftw.com,klam@fftw.com
			exit 1
		fi
		start_job
		attempt=$[$attempt+1]
	else
	  	echo "Everything is ok"
  	fi	
done
