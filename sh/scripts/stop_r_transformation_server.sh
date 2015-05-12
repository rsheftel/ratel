#! /bin/sh 

script_name=`basename $0`
if [ "" == "$1" ]; then
    echo "usage: $script_name <group_name>"
    echo "usage: $script_name <group_name>" >> /logs/LiveTransformation/$script_name.log
    exit -1
fi

mv /logs/LiveTransformation/$1.CONTROL.log /logs/LiveTransformation/$1.CONTROL.log.old >> /logs/LiveTransformation/$script_name.log 2>&1
. ~/.bashrc > /logs/LiveTransformation/$1.CONTROL.log 2>&1
cd $main/Java/systematic >> /logs/LiveTransformation/$1.CONTROL.log 2>&1
java -classpath lib/\* gissing.ControlTransformation $1 >> /logs/LiveTransformation/$1.CONTROL.log 2>&1 &
