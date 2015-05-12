use strict;

my ($host, @commandLineArgs) = @ARGV;
open(COMMANDS, ">commands.txt") or die "can't open commands.txt: $!";
print COMMANDS "\\\\nysrv57\\NETLOGON\\fftwlogin.bat\n";
print COMMANDS join(" ", @commandLineArgs) . "\n";
print COMMANDS "echo RESULTCODE##%errorlevel%##\n";
close(COMMANDS) or die "cannot close ssh process: $!";
open(REMOTE, "ssh $host -l QFLive < commands.txt |") or die "can't open ssh to host $host: $!";
while(<REMOTE>) {
    print;
    if(/.*RESULTCODE##(.*)##.*/) {
        my $errlevel = $1;
        print "\ncommand result: $errlevel\n";
        exit $errlevel;
    }
}
