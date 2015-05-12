#! /usr/bin/perl -w 
use strict;

my $showPrivate = grep /-p/, @ARGV;
@ARGV = grep !/-p/, @ARGV;

my $fname = shift;

die("usage: rmethods [-p] <file>") if @ARGV || !$fname;
print "   " . "x" x 40 . "\n";
print "   $_" for `grep constructor $fname`;

print "   " . "x" x 40 . "\n";
print "   methods: $fname\n";

my @greplines = `grep method $fname`;
my @private = ();
for my $line (@greplines) {
   $line =~ s/\("/ /;
   $line =~ s/", .*this, /    /;
   $line =~ s/(, )?\.\.\./ /;
   $line =~ s/\) {//;
   $line =~ s/\)//;
   push(@private, $line), next if $line =~ /method \./;
   print "      $line";
}

if ($showPrivate and @private) {
   print "   " . "x" x 40 . "\n";
   print "   private methods: $fname\n";
   print "      $_" for @private 
}

@private = ();

print "   " . "x" x 40 . "\n";
print "   functions: $fname\n";
for my $line (`grep function $fname`) {
   next unless $line =~ /<-/; 
   #print "$line";
   $line =~ s/<- function\(//;
   $line =~ s/\) {//;
   push(@private, $line), next if $line =~ /^\s*\./;

   print "      FUN $line";
}

if ($showPrivate and @private) {
   print "   " . "x" x 40 . "\n";
   print "   private functions: $fname\n";
   print "      $_" for @private 
}


