#!/usr/bin/perl
use strict;
use File::stat;
use Crypt::SSLeay;
use HTTP::Request::Common;
use LWP::UserAgent;
use URI;
use Data::Dumper;
use Archive::Zip;
use Archive::Zip qw(:ERROR_CODES);
use XML::Simple;
use DBI;
use Error qw(:try);
$SIG{'__WARN__'} = \&warn_handler;    # install signal handler

sub warn_handler () {
	return;
}

# MAIN
print "LOG for Index composite\n";
my $user          = $ARGV[0];
my $passwd        = $ARGV[1];
my $dsn           = $ARGV[2];
my $family        = $ARGV[3];
my $retries       = $ARGV[4];
my $retryWaitTime = $ARGV[5];
my $webDate       = $ARGV[6];
my $ct            = 0;
my $done          = 0;
my $fileName      = "CredindexComposites.zip";

# Use Sybase driver because MS Sql Server doesn't support Linux Perl database connectivity
my $dbh =
  DBI->connect( "DBI:Sybase:server=$dsn", $user, $passwd,
	{ syb_show_eed => 1 } );
unless ($dbh) {
	die "Unable to connect to server $DBI::errstr";
}
print "connected to $dsn\n";

#Download is executed after midnight of the subsequent processing day unless a date has been passed
# i.e., Tuesday throught Saturday for Monday through Friday data.
if ( $webDate eq '' )    
{
	my $sqlStatement =
	  "select convert(varchar,dateadd(d,-1,getdate()),112) as webDate";
	my $sth = $dbh->prepare($sqlStatement)
	  || die "Error:$! Cannot prepare the SQL statement: $DBI::errstr";
	$sth->execute
	  || die "Error:$! Cannot execute the SQL statement: $DBI::errstr";
	$webDate = $sth->fetchrow_array;
}
print "retrieve $webDate\n";

while ( !$done && $ct <= $retries ) {
	my $oUa = LWP::UserAgent->new();
	$oUa->agent('curl/1.0');
	$oUa->proxy( 'https', "http://proxy.fftw.com:8080/" );

	my $oUri = URI->new('https://www.Markit.com/export.jsp');

	# Proxy details removed
	# $oRequest->proxy_authorization_basic($sUser, $sPass);
	print "Getting the zipped file from vendor's website for $webDate\n";
	my $oResponse = $oUa->post(
		$oUri,
		'Content_Type' => 'form-data',
		'Content'      => [
			'user'     => 'jeromeb',
			'password' => 'Bonjour1',
			'version'  => '4',
			'date'     => $webDate,
			'format'   => 'xml',
			'family'   => $family,
			'type'     => 'credindex',
			'report'   => 'Composites'
		]
	);

	if ( $oResponse->is_success() ) {
		open( "OUTFILE", ">Temp/$family$fileName" )
		  || die "Error:$! Cannot open Temp/$family$fileName\n";
		binmode OUTFILE;
		print OUTFILE $oResponse->content;
		close(OUTFILE) || die "Error:$! Cannot close Temp/$family$fileName\n";
		print "Got the zipped file from vendor's website --> $family\n";

	  # if size is below 1kb an error message rather than the data was returned.
		if ( stat("Temp/$family$fileName")->size < 1024 ) {
			open( "INFILE", "<Temp/$family$fileName" )
			  || die "Error:$! Cannot open Temp/$family$fileName\n";
			my @errortext = <INFILE>;
			print "Error downloading $family$fileName.  Message: @errortext";
			close(INFILE)
			  || die "Error:$! Cannot close Temp/$family$fileName\n";
			retry();
			next;
		}
		else {
			$done = 1;
		}
	}
	else

	  # http error has occurred.
	{
		print "http error: " . $oResponse->status_line . "\n";
		retry();
		next;
	}

}

# unzip the vendor file
my $zip = Archive::Zip->new("Temp/$family$fileName")
  || die "Error:$! Cannot create a Archive::Zip handle to $family$fileName\n";
my @xmlFiles = $zip->membersMatching('.xml');

# extract the file
my $unzipedString = $zip->contents( $xmlFiles[0] );
print "finished unzipping the vendor file Temp/$family$fileName\n";

#send it to xml parser
parseXMLWriteToDb($unzipedString);
print "DONE!!\n";

sub retry {
	if ( $ct < $retries ) {
		print "waiting $retryWaitTime seconds to retry ...\n";
		sleep($retryWaitTime);
		$ct++;
		print "retry $ct ...\n";
	}
	else {
		print "Process terminated after $retries attempts.\n";
		exit(-1);
	}
}

sub asString {
	my $in = shift;
	if ( $in eq "" ) {
		return "null";
	}
	else {
		$in =~ s/'/''/g;
		return "'" . $in . "'";
	}
}

sub asDate {
	my $in = shift;
	if ( $in eq "" ) {
		return "null";
	}
	else {
		return "'" . $in . "'";
	}
}

sub asNumber {
	my $in = shift;
	if ( $in eq "" ) {
		return "null";
	}
	else {
		return $in;
	}
}

sub parseXMLWriteToDb {

	# create object
	my $xml = new XML::Simple( KeyAttr => [] );

	my $xmlString = shift;

	# read XML file
	my $data        = $xml->XMLin($xmlString);
	my $idxCompSize = ( @{ $data->{row} } );
	( $idxCompSize > 5 )
	  || die "Error: Not enougth cds composites in file: $idxCompSize\n";

	# access XML data

	my $j = 0;
	my @issuersArr;
	my %issuersHash;

	# we may need to reload data for a particular date
	# remove data from the staging table
	print
	  "Removing data from Stage.dbo.T_Markit_Index_Composite\n";
	my $stmt ="delete Stage.dbo.T_Markit_Index_Composite";
	my $sth = $dbh->prepare($stmt)
	  || die "Error:$! Cannot prepare the SQL statement: $DBI::errstr";
	$sth->execute
	  || die "Error:$! Cannot execute the SQL statement: $DBI::errstr";

	print "Start writing to Stage.dbo.T_Markit_Index_Composite\n";
	foreach my $e ( @{ $data->{row} } ) {
		my $stmt = "insert Stage.dbo.T_Markit_Index_Composite(
	   headerName, headerVersion, headerDate, indexFamily, date, name
	   , series, version, term, redCode, indexID, maturity, onTheRun
	   , compositePrice, compositeSpread, modelPrice, modelSpread, depth) VALUES(";

		$stmt .= asString( $data->{header}->{name} ) . ",";
		$stmt .= asString( $data->{header}->{version} ) . ",";
		$stmt .= asDate( $data->{header}->{date} ) . ",";
		$stmt .= asString($family) . ",";
		$stmt .= asDate( $e->{Date} ) . ",";
		$stmt .= asString( $e->{Name} ) . ",";
		$stmt .= asNumber( $e->{Series} ) . ",";
		$stmt .= asNumber( $e->{Version} ) . ",";
		$stmt .= asString( $e->{Term} ) . ",";
		$stmt .= asString( $e->{REDCode} ) . ",";
		$stmt .= asString( $e->{IndexID} ) . ",";
		$stmt .= asDate( $e->{Maturity} ) . ",";
		$stmt .= asString( $e->{OnTheRun} ) . ",";
		$stmt .= asNumber( $e->{CompositePrice} ) . ",";
		$stmt .= asNumber( $e->{CompositeSpread} ) . ",";
		$stmt .= asNumber( $e->{ModelPrice} ) . ",";
		$stmt .= asNumber( $e->{ModelSpread} ) . ",";
		$stmt .= asNumber( $e->{Depth} ) . ")";
		my $sth = $dbh->prepare($stmt)
		  || die "Error:$! Cannot prepare the SQL statement: $DBI::errstr";
		$sth->execute
		  || die "Error:$! Cannot execute the SQL statement: $DBI::errstr";

	}
	# remove data for the load date
	print
	  "Removing $webDate data from TSDB.dbo.T_Markit_Index_Composite_Hist\n";
	my $stmt =
	    "delete TSDB.dbo.T_Markit_Index_Composite_Hist where indexFamily='"
	  . $family
	  . "' and date ='"
	  . $webDate . "'";
	my $sth = $dbh->prepare($stmt)
	  || die "Error:$! Cannot prepare the SQL statement: $DBI::errstr";
	$sth->execute
	  || die "Error:$! Cannot execute the SQL statement: $DBI::errstr";
	
	# load data into the historical table
	my $stmt = "insert TSDB.dbo.T_Markit_Index_Composite_Hist(
	   headerName, headerVersion, headerDate, indexFamily, date, name
	   , series, version, term, redCode, indexID, maturity, onTheRun
	   , compositePrice, compositeSpread, modelPrice, modelSpread, depth) select distinct
	   headerName, headerVersion, headerDate, indexFamily, date, name
	   , series, version, term, redCode, indexID, maturity, onTheRun
	   , compositePrice, compositeSpread, modelPrice, modelSpread, depth 
	   from Stage.dbo.T_Markit_Index_Composite";
	my $sth = $dbh->prepare($stmt)
	  || die "Error:$! Cannot prepare the SQL statement: $DBI::errstr";
	$sth->execute
	  || die "Error:$! Cannot execute the SQL statement: $DBI::errstr";
	     
	print "Finished writing to TSDB.dbo.T_Markit_Index_Composite_Hist table\n";
}

