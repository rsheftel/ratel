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
 $ToAddr='dgordon@fftw.com,dhgpager@fftw.com,jbourgeois@fftw.com,klam@fftw.com,rokonowitz@fftw.com';
 my $msg=$_[0];
 my $msg=MIME::Lite->new(
   From   =>"$DSN",
   To     =>"$ToAddr",
   Subject=>"$DSN IvyDB Load Status: $DT",
   Type   =>"text/html",
   Data   =>$msg ) or die "Error creating inline email: $!";
   $msg->send("smtp","MAIL");
  
}

sub dbConn {
	if ($connection->Sql($_[0])) { 
	  sendMail("Sql Error. Could not execute the following statement:\n $_[0]\n"); 
	 
	 ### Closing the database connection 
	 $connection->Close();
	
	 ### Print error message to log
	 print $connection->Error()."\n"; 
	 
	 ### Exiting the program 
	 exit(-1);
	}
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

# assert file uncompression directory location and executable
if ( -e $properties->getProperty(FD) )
{
	chdir $properties->getProperty(FD);
}
else
{
	print "Directory " . $properties->getProperty(FD) . " does not exist. Program terminating...\n";
	print LOG "Directory " . $properties->getProperty(FD) . " does not exist. Program terminating...\n";
	exit(-1);
}

if (! -e "c:\\apps\\bin\\7za.exe")
{
	print "Uncompression utility c:\\apps\\bin\\7za.exe does not exist. Program terminating...\n";
	print LOG "Uncompression utility c:\\apps\\bin\\7za.exe does not exist. Program terminating...\n";
	exit(-1);
}

$DSN=$properties->getProperty(DSN);
#
# get date
#
($DAY,$MONTH,$YEAR)=(localtime)[3,4,5];
$YYYY=$YEAR+1900;
$M=$MONTH+1;
if ($M<10) {$M='0'.$M};
if ($DAY<10){$DAY='0'.$DAY};
$DT=$YYYY.$M.$DAY;

$LF=$properties->getProperty(LF);
open(LOG,">${LF}") or die "failed to open ftp output file $LF: $!";
print "Downloading Option Metrics Data on $DT\n";
print LOG "Downloading Option Metrics Data on $DT\n";

$SA=$properties->getProperty(SA);
if ( $SA eq '')
{
	$SA=$DT;
}
print "Processing Date: $SA\n";
print LOG "Processing Date: $SA\n";

#
# format target file name
#

   	$FN='IVYDB.' . $SA . 'D.zip';
    print "Get Option Metrics  file: $FN\n";
	print LOG "Get Option Metrics file: $FN\n";

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
		exit(-1);
	}
}
	


@z=glob("*$SA*zip");
foreach $z(@z) {
# uncompress archive file
	print "Uncompressing $z\n";
	print LOG "Uncompressing $z\n";
	`c:\\apps\\bin\\7za.exe x $z`;
	print "Uncompressed $z\n";
	print LOG "Uncompressed $z\n";
	
#load data
	print "Loading $z data files\n";
	print LOG "Loading $z data files\n";
	fftwIvyDB::LoadIvyDB::Load(
	$properties->getProperty(DSN),
	$properties->getProperty(FD),
	$properties->getProperty(RPM),
	$properties->getProperty(CPM),
	$SA,
	*LOG,
	$properties->getProperty(SCR)
	);
	print "Loaded $z data files\n";
	print LOG "Loaded $z data files\n";
#compress files	
	print "Removing uncompressed data files to $z";
	print LOG "Removing uncompressed data files to $z";
 	`del *txt`;
	
}
close (LOG);
close (PROPS);
sendMail("IvyDB Load for $SA is complete\n");
exit(0);
