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
use Error qw(:try);
use DBI;
$SIG{'__WARN__'} = \&warn_handler;    # install signal handler

sub warn_handler () {
	return;
}

# MAIN
print "LOG for CDS composite\n";
my $user          = $ARGV[0];
my $passwd        = $ARGV[1];
my $dsn           = $ARGV[2];
my $retries       = $ARGV[3];
my $retryWaitTime = $ARGV[4];
my $webDate       = $ARGV[5];
my $ct            = 0;
my $done          = 0;

# use Sybase driver because MS Sql Server doesn't support Linux Perl database connectivity
my $dbh =
  DBI->connect( "DBI:Sybase:server=$dsn", $user, $passwd,
	{ syb_show_eed => 1 } );
unless ($dbh) {
	die "Unable to connect to server $DBI::errstr";
}
print "connected to $dsn\n";

# Download is executed after midnight of the subsequent processing day unless a specific date is requested
# i.e., Tuesday throught Saturday for Monday through Friday data.
if ( $webDate eq '' ) {
	my $sqlStatement =
	  "select convert(varchar,dateadd(d,-1,getdate()),112) as webDate";
	my $sth = $dbh->prepare($sqlStatement)
	  || die "Error:$! Cannot prepare the SQL statement: $DBI::errstr";
	$sth->execute
	  || die "Error:$! Cannot execute the SQL statement: $DBI::errstr";
	$webDate = $sth->fetchrow_array;
}
print "retrieve $webDate file\n";

while ( !$done && $ct <= $retries ) {
	my $oUa = LWP::UserAgent->new();
	$oUa->agent('curl/1.0');
	$oUa->proxy( 'https', "http://proxy.fftw.com:8080/" );

	my $oUri = URI->new('https://www.Markit.com/export.jsp');

	# Proxy details removed
	# $oRequest->proxy_authorization_basic($sUser, $sPass);
	print "Getting the file from vendor's website\n";
	my $oResponse = $oUa->post(
		$oUri,
		'Content_Type' => 'form-data',
		'Content'      => [
			'user'     => 'jeromeb',
			'password' => 'Bonjour1',
			'version'  => '5',
			'date'     => $webDate,
			'format'   => 'xml',
			'type'     => 'cds',
			'report'   => 'Composites'
		]
	);

	if ( $oResponse->is_success() ) {
		open( "CdsC", ">Temp/cdsComposites.zip" );
		binmode CdsC;
		print CdsC $oResponse->content;
		close(CdsC);
		print "Got the zip file from vendor's website\n";

	  # if size is below 1kb an error message rather than the data was returned.
		if ( stat("Temp/cdsComposites.zip")->size < 1024 ) {
			open( "INFILE", "<Temp/cdsComposites.zip" )
			  || die "Error:$! Cannot open Temp/cdsComposites.zip\n";
			my @errortext = <INFILE>;
			print "Error downloading cdsComposites.zip.  Message: @errortext";
			close(INFILE)
			  || die "Error:$! Cannot close Temp/cdsComposites.zip\n";
			retry();
			next;
		}
		else {
			$done = 1;
		}
	}
	else

	  # http error
	{
		print "http error: " . $oResponse->status_line . "\n";
		retry();
		next;
	}

}

# unzip the vendor file
my $zip      = Archive::Zip->new("Temp/cdsComposites.zip");
my @xmlFiles = $zip->membersMatching('.xml');

# extract the file
my $unzipedString = $zip->contents( $xmlFiles[0] );

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
	my $xml = new XML::Simple( KeyAttr => [], ForceArray => 1 );

	my $xmlString = shift;

	# read XML file
	my $data        = $xml->XMLin($xmlString);
	my $cdsCompSize = ( @{ $data->{row} } );
	( $cdsCompSize > 20000 )
	  || die
"Error: Not enougth cds composites in file(Should be more than 20000): $cdsCompSize\n";

	# access XML data

	my $j = 0;
	my @issuersArr;
	my %issuersHash;
	my $stmt;

	# remove data from the history table for the load date
	print "Removing $webDate data from TSDB.dbo.T_Markit_Cds_Composite_Hist\n";
	my $stmt =
	    "delete TSDB.dbo.T_Markit_Cds_Composite_Hist where date ='" . $webDate
	  . "'";
	my $sth = $dbh->prepare($stmt)
	  || die "Error:$! Cannot prepare the SQL statement: $DBI::errstr";
	$sth->execute
	  || die "Error:$! Cannot execute the SQL statement: $DBI::errstr";

	# load data into staging table
	print "Start writing to TSDB.dbo.T_Markit_Cds_Composite_Hist table\n";
	my %cdsKey;

	foreach my $e ( @{ $data->{row} } ) {

# remove duplicate keyed records
# add each key set to a hash and bypass a subsequent record with the same key set
		my $KEY =
		    asString( $e->{Ticker}[0] )
		  . asString( $e->{Tier}[0] )
		  . asString( $e->{Ccy}[0] )
		  . asString( $e->{DocClause}[0] )
		  . asString( $e->{Date}[0] );

		if ( exists( $cdsKey {$KEY} ) ) {
			next;
		}
		else {
			$cdsKey{$KEY} = '';
			my $stmt =
"INSERT INTO TSDB.dbo.T_Markit_Cds_Composite_Hist(headerName, headerVersion, avRating
   , ccy, compositeDepth5y, compositeLevel10y, compositeLevel15y, compositeLevel1y, compositeLevel20y
   , compositeLevel2y, compositeLevel30y, compositeLevel3y, compositeLevel4y, compositeLevel5y, compositeLevel6m
   , compositeLevel7y, compositeLevelRecovery, contributor, date, docClause, impliedRating, recovery, redCode
   , region, sector, shortName, spread10y, spread15y, spread1y, spread20y, spread2y, spread30y, spread3y, spread4y
   , spread5y, spread6m, spread7y, ticker, tier, rating10y,rating15y,rating1y,rating20y,rating2y,rating30y 
   , rating3y,rating4y,rating5y,rating6m,rating7y,country,compositeCurveRating) VALUES(";
			$stmt .= asString( $data->{header}[0]->{name}[0] ) . ",";
			$stmt .= asString( $data->{header}[0]->{version}[0] ) . ",";

			$stmt .= asString( $e->{AvRating}[0] ) . ",";
			$stmt .= asString( $e->{Ccy}[0] ) . ",";
			$stmt .= asString( $e->{CompositeDepth5y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel10y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel15y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel1y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel20y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel2y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel30y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel3y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel4y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel5y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel6m}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevel7y}[0] ) . ",";
			$stmt .= asString( $e->{CompositeLevelRecovery}[0] ) . ",";
			$stmt .= asString( $e->{Contributor}[0] ) . ",";
			$stmt .= asString( $e->{Date}[0] ) . ",";
			$stmt .= asString( $e->{DocClause}[0] ) . ",";
			$stmt .= asString( $e->{ImpliedRating}[0] ) . ",";
			$stmt .= asNumber( $e->{Recovery}[0] ) . ",";
			$stmt .= asString( $e->{RedCode}[0] ) . ",";
			$stmt .= asString( $e->{Region}[0] ) . ",";
			$stmt .= asString( $e->{Sector}[0] ) . ",";
			$stmt .= asString( $e->{ShortName}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread10y}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread15y}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread1y}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread20y}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread2y}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread30y}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread3y}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread4y}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread5y}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread6m}[0] ) . ",";
			$stmt .= asNumber( $e->{Spread7y}[0] ) . ",";
			$stmt .= asString( $e->{Ticker}[0] ) . ",";
			$stmt .= asString( $e->{Tier}[0] ) . ",";
			$stmt .= asString( $e->{Rating10y}[0] ) . ",";
			$stmt .= asString( $e->{Rating15y}[0] ) . ",";
			$stmt .= asString( $e->{Rating1y}[0] ) . ",";
			$stmt .= asString( $e->{Rating20y}[0] ) . ",";
			$stmt .= asString( $e->{Rating2y}[0] ) . ",";
			$stmt .= asString( $e->{Rating30y}[0] ) . ",";
			$stmt .= asString( $e->{Rating3y}[0] ) . ",";
			$stmt .= asString( $e->{Rating4y}[0] ) . ",";
			$stmt .= asString( $e->{Rating5y}[0] ) . ",";
			$stmt .= asString( $e->{Rating6m}[0] ) . ",";
			$stmt .= asString( $e->{Rating7y}[0] ) . ",";
			$stmt .= asString( $e->{Country}[0] ) . ",";
			$stmt .= asString( $e->{CompositeCurveRating}[0] ) . ")";

			my $sth = $dbh->prepare($stmt)
			  || die "Error:$! Cannot prepare the SQL statement: $DBI::errstr";
			$sth->execute
			  || die "Error:$! Cannot execute the SQL statement: $DBI::errstr";
		}
	}
	print "Finished writing to TSDB.dbo.T_Markit_Cds_Composite_Hist table\n";

}    
