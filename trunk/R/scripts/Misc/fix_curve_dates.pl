#! /usr/bin/perl -pi 

if(m/Date/) {next;} 
($month, $day, $year, $rest) = m#^(\d+)/(\d+)/(\d+)(.*)#; 
$year = $year < 50 ? $year + 2000 : $year + 1900; 
$_ = sprintf("%02d/%02d/%d%s\n", $year,$month,$day,$rest)
