#loadIvyDBDriver.pl 
# Caller process for loading Option Metrics data into the IvyDB database. Each compressed daily data set will be uncompressed, loaded
# and compressed

#
# packages
#
use Config::Properties;
use MIME::Lite;
use Net::SMTP;
use fftwIvyDB::LoadIvyDB;
use fftwFtp::Ftp;

#
# functions
#
sub sendMail {
 $ToAddr='dgordon@fftw.com';
 my $msg=$_[0];
 my $msg=MIME::Lite->new(
   From   =>"$DSN",
   To     =>"$ToAddr",
   Subject=>"$DSN IvyDB Load Status: $DT",
   Type   =>"text/html",
   Data   =>$msg ) or die "Error creating inline email: $!";
   $msg->send("smtp","MAIL");
  
}

#Main
#
# verify number of  parameters passed
# 
$numelements = $#ARGV + 1;
if ( $numelements < 1 )
{
 print "usage: LoadIvyDBDriver.pl <PropertiesFile> \n";
 exit;
};

open PROPS, "< $ARGV[0]" or die "unable to open configuration file $ARGV[0]";
my $properties = new Config::Properties();
$properties->load(*PROPS);

#
# get date
#
($DAY,$MONTH,$YEAR)=(localtime)[3,4,5];
$YYYY=$YEAR+1900;
$M=$MONTH+1;
$DT="$YYYY-$M-$DAY";

$LF=$properties->getProperty(LF);
open(LOG,">${LF}") or die "failed to open ftp output file $LF: $!";
print "Downloading Option Metrics Data on $DT\n";
print LOG "Downloading Option Metrics Data on $DT\n";


#
# derive the file date required if it wasn't passed explicitly
# ftp to the target server; get the most recent file available 
#
$SA=$properties->getProperty(SA);
if ( $SA eq '') {

		@descending = grep {/IVYDB/} sort { $b cmp $a } fftwFtp::Ftp::dir(
		$properties->getProperty(SN),
		$properties->getProperty(ID),
		$properties->getProperty(PW),
		$properties->getProperty(DIR),
		*LOG
	);
	$FN=$descending[0];
print "Get latest Option Metrics  file: $FN\n";
print LOG "Get latest Option Metrics  file: $FN\n";
} else {
#
# format target file name
#

   	$FN='IVYDB.' . $SA . 'D.zip';
    print "Get Option Metrics  file: $FN\n";
	print LOG "Get Option Metrics file: $FN\n";
}

#
# Get latest option metrics file from the ivydb ftp server
#

if ($properties->getProperty(FTP) eq 'Y' )
{
	 if (fftwFtp::Ftp::get(
	 $properties->getProperty(SN),
	 $properties->getProperty(DIR),
	 $properties->getProperty(FD),
	 *LOG,
	 $properties->getProperty(ID),
	 $properties->getProperty(PW),
	 $properties->getProperty(R),
	 $properties->getProperty(W),
	 $FN )) 
	 { 
		print "*$SA*.zip is not available on $SN after $R retries\n";
		print LOG "*$SA*.zip is not available on $SN after $R retries\n";
		sendMail("*$SA*.zip is not available on $SN after $R retries on $DT\n");
		close(LOG);
		exit;
	}
}
	

chdir $properties->getProperty(FD);
$SA=$properties->getProperty(SA);
@z=glob("*$SA*zip");
foreach $z(@z) {
# uncompress archive file
	print "Uncompressing $z\n";
	print LOG "Uncompressing $z\n";
	`7za x $z`;
	print "Uncompressed $z\n";
	print LOG "Uncompressed $z\n";
	
#load data
	print "Loading $z data files\n";
	print LOG "Loading $z data files\n";
	fftwIvyDB::LoadIvyDB::Load($properties->getProperty(DSN),
	$properties->getProperty(FD),
	$properties->getProperty(RPM),
	$properties->getProperty(CPM),
	$properties->getProperty(RD),
	$properties->getProperty(SA),
	*LOG);
	print "Loaded $z data files\n";
	print LOG "Loaded $z data files\n";
#compress files	
	print "Removing uncompressed data files to $z";
	print LOG "Removing uncompressed data files to $z";
 	`del *txt`;
	
}
close (LOG);
close (PROPS);