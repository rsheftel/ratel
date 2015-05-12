#! /usr/bin/perl -w 
use strict;

my $name = shift;
die("usage: wipe <regex>") if @ARGV || !$name;

print "wiping $name\n";

my @screens = `screen -list`;
@screens = grep /$name/, @screens;

for my $line (@screens) {
   $line =~ /\s+(\d+)\.(\S+)\s+/;
   my $pid = $1;
   my $name = $2;
   `kill -9 $pid`; 
   `screen -wipe $name`;
   print "wiped $name after killing $pid\n";
}
