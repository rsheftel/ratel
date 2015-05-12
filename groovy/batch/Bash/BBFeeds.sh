#!/bin/sh

java -Xbootclasspath/a:$HOME/batch/fftw-lib/lib/jtds-1.2.2.jar -Xmx256m -XX:MaxPermSize=128m -Djdbc.drivers=net.sourceforge.jtds.jdbc.Driver -jar $HOME/batch/fftw-lib/fftw-feeds-1.6.jar com.fftw.bloomberg.tsdbfeeds.TSDBFeed jdbc:jtds:sqlserver://SQLPRODTS:2433/BloombergFeedDB sim_load Simload5878 $1