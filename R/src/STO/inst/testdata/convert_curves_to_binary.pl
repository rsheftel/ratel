#! /usr/bin/perl -w

use strict;
use Time::Local;

$| = 1;

my($fromDir, $toDir, $marketFilter) = @ARGV;

mkdir $toDir or die "Cannot mkdir $toDir: $!"
    unless -e $toDir;

opendir(DIR, $fromDir);
my(@markets) = grep /^[^\.]/, readdir(DIR);
@markets = grep(/$marketFilter/, @markets) if $marketFilter;
closedir(DIR);
foreach my $market (@markets) {
    opendir(DIR, "$fromDir/$market");
    mkdir "$toDir/$market" or die "Cannot mkdir $toDir/$market: $!" unless -e "$toDir/$market";
    my(@run_files) = grep /^[^\.]/, readdir(DIR);
    print("\n$market ");
    my $count = 0;
    foreach my $run_file (@run_files) {
        print(".") if $count++ % 100 == 0;
        my $filename = "$fromDir/$market/$run_file";
        open(INFILE, $filename) or die "Cannot open file $filename: $!";
        <INFILE>;  # skip header
        my $outfilename = "$toDir/$market/$run_file";
        $outfilename =~ s/.csv$/.bin/;
        open(OUTFILE, ">$outfilename") or die "Cannot open file for write $filename: $!";
        while(<INFILE>) {
            my($date, undef, $pnl, undef, $position) = split /,/;
            $date =~ s/"//g;
            my ($mon, $mday, $year) = split /\//, $date;
            $date = timelocal(0,0,0,$mday,$mon-1,$year);
            syswrite(OUTFILE, pack("d2", $date, $pnl));
            if($position =~ /NA/) {
                syswrite(OUTFILE, pack("C8", 0xA2, 0x07, 0x00, 0x00, 0x00, 0x00, 0xF8, 0x7F));
            } else {
                syswrite(OUTFILE, pack("d", $position));
            }
        }
        close(OUTFILE);
        close(INFILE);
    }
}

