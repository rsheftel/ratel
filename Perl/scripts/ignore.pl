#! /usr/bin/perl -w

my ($file) = @ARGV;

run_cmd("mv $file $file.bak");
run_cmd("svn rm --force $file");
my $temp = "/tmp/ignores.$ENV{USER}";
run_cmd("svn propget svn:ignore . > $temp");
run_cmd("echo $file >> $temp");
run_cmd("svn propset -F $temp svn:ignore .");
run_cmd("mv $file.bak $file");

sub run_cmd {
    my ($cmd) = @_;
    system($cmd) and die "Error running command:\n\t$cmd\n$!";
}
