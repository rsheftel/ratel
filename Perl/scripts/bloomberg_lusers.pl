#! /usr/bin/perl -w

use strict;
use Time::Local;

my ($email, $asof) = @ARGV;
$asof =~ s/\D+//g;
my(@users) = qw(eknell rsheftel dhorowitz jbourgeois imcdonald);
my $directory = "/data/TSDB_upload/Archive/$asof";
my @lusers;
foreach my $user (@users) {
    my (@files) = backtick("find $directory -user $user -name 'Bberg*'"); 
    push @lusers, $user unless @files;
}
my $subject = "Bloomberg spreadsheet failures for $asof";
open EMAIL, "|java -classpath $ENV{MAIN}/Java/systematic/lib/\\* mail.Email -to $email -type problem -subject '$subject'" 
    or die "Cannot open emailer process: $!"; 
print EMAIL "The following users do not have working Bloomberg spreadsheets for $asof:\n\n";
print EMAIL join("\n", @lusers) . "\n";
close EMAIL or die "Cannot close emailer Process: $!";

sub backtick {
    my ($command) = @_;
    my (@result) = `$command`;
    my $rc = $? >> 8;
    die "failure running command: $command\nreturn code: $rc\n$!" if $rc;
    return @result;
}
