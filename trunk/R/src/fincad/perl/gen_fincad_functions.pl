#! /usr/bin/perl

# Usage: gen_fincad_functions.pl /tp/fincad/samples/java/java/ShowAll.java > R/fincad_funcs.R

my @lists;

while(<>)
{
    next unless /public native static double\s*\[\]/;

    my ($func, $args) = ($_ =~ /(\w+)\s+\((.*)\)/);

    $args =~ s/double \[\]\[\]/double.table/g;
    my (@args) = split /,/, $args;

    my (@out_args) = map { s/^ +//; s/ +$//; my (@a) = reverse split / /; $a[1] = "\"$a[1]\""; join(" = ", @a) } @args;

    push @lists, "\n    $func = list( " . join(", ", @out_args) . " )";
}

print "fincad.funcs <- list(" . join(",", @lists) . "\n)\n";
