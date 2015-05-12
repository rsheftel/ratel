#!/usr/bin/perl -w

use strict; 
my $command = "java -classpath $ENV{MAIN}/Java/systematic/lib/\\* @ARGV";
my $rc = system($command);
if ($rc >> 8 != 0) { die("java command failed:$!\n$command"); }
