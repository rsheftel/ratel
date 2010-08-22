#! /usr/bin/perl -w

use strict;
use Date::Manip;

my($dir, $nMarkets, $nRuns) = @ARGV;

mkdir $dir or die "Cannot mkdir $dir: $!"
    unless -e $dir;

my $prefix = "foo_1_daily_mkt";

foreach my $mkt (1..$nMarkets) {
    my $filename = "$dir/$prefix$mkt.csv";
    open(METRICS, ">$filename") or die "Cannot open file $filename: $!";
    print METRICS "run,TSAvgBarsEvenTrade,TSAvgBarsLosTrade,TSAvgBarWinTrade,TSGrossLoss,TSGrossProfit,TSLargestLosTrade,TSLargestWinTrade,TSMaxConsecLosers,TSMaxConsecWinners,TSMaxContractsHeld,TSClosePositionProfit,TSNumEvenTrades,TSNumLosTrades,TSNumWinTrades,TSPercentProfit,TSTotalTrades,TSTotalBarsEvenTrades,TSTotalBarsLosTrades,TSTotalBarsWinTrades,TSOpenPositionProfit,TSMaxIDDrawDown\n";

    print "\n$mkt";
        
    foreach my $run (1..$nRuns) {
        print ".";
        my @metrics = map { rand(2000) - 1000 } (1..21);
        print METRICS join(',', $run, @metrics);
        print METRICS "\n";

    }
    close(METRICS) or die "Cannot close file $filename: $!";
}


