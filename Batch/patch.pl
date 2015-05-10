#patch.pl 
#Apply IvyDB patch files
#
#
@patchfile = glob("ptcivydb*zip");
foreach $pf( @patchfile )
{
	@filename=split(/\./,$pf);
	$datestring=$filename[1];
	print "Applying IvyDB patch for $datestring\n";
	`IvyDBPatch.bat $datestring`
}