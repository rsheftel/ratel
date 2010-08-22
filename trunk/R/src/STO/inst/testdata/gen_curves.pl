#! /usr/bin/perl -w

use strict;
use Date::Manip;

my($dir, $nMarkets, $nRuns, $nDays) = @ARGV;

mkdir $dir or die "Cannot mkdir $dir: $!"
    unless -e $dir;

my $prefix = "foo_1_daily_mkt";

my $date = ParseDate("01/01/1990");
my $delta = ParseDateDelta("+1d");
my @dates;

foreach my $day (1..$nDays) {
    $date = DateCalc($date, $delta);
    my $dateStr = UnixDate($date, "%m/%d/%Y");
    push(@dates, $dateStr);
    print "$day\n" if $day % 100 == 99
}


foreach my $mkt (1..$nMarkets) {
    my $mktdir = "$dir/$prefix$mkt";
    mkdir $mktdir or die "Cannot mkdir $mktdir: $!"
        unless -e $mktdir;

    print "\n$mkt";
        
    foreach my $run (1..$nRuns) {
        print ".";
        my $runFile = "$mktdir/run_$run.csv";
        open(RUN, ">$runFile") or die "Cannot open file $runFile: $!";
        print RUN "date,time,pnl,equity,position\n";

        my $pnl = 0;
        my $equity = 0;

        foreach my $day (1..$nDays) {
            $pnl = int(rand(200000)) - 100000;
            my $position = int(rand(200)) - 100;
            $equity += $pnl;
            my $dateStr = $dates[$day-1];

            print RUN "$dateStr,1700.000000,$pnl,$equity,$position\n";
        }

        close(RUN) or die "Cannot close file $runFile: $!";
    }
}
