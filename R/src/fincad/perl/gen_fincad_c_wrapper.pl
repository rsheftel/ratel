#! /usr/bin/perl -w

# Usage: gen_fincad_c_wrapper.pl /tp/fincad/samples/java/java/ShowAll.java > src/fincad.c
# Note:  The formatting in this file is awful, so that the .c file looks good.  Please don't kill me Saudi-style.  --knell
my @lists;

my %TYPE_MAP = (
    double => 'double',
    int => 'int',
    'double.table' => 'LPXLOPER'
);

my %FUNC_MAP = (
    double => 'doubleArg',
    int => 'intArg',
    'double.table' => 'doubleTableArg',
);

printHeader();

while(<>)
{
    next unless /public native static double\s*\[\]/;

    my ($func, $args) = ($_ =~ /(\w+)\s+\((.*)\)/);

    $args =~ s/double \[\]\[\]/double.table/g;
    my (@args) = split /,/, $args;

    my (%arg_types) = map { s/^ +//; s/ +$//; my (@a) = reverse split / /; @a } @args;
    my (@arg_list) = map { s/^ +//; s/ +$//; my (@a) = reverse split / /; $a[0] } @args;

    $func =~ s/_java//;
    print "SEXP ${func}_wrap(SEXP args)\n{";
    
    my $count = 0;
    
    foreach my $arg (@arg_list) {
        print"
    $TYPE_MAP{$arg_types{$arg}} $arg = $FUNC_MAP{$arg_types{$arg}}(\&args);";
        $count += ($arg_types{$arg} eq 'double.table' ? 3 : 1);
        print " // $count";    
    }
    
    $count++;
    
    print "
    LPXLOPER result = $func(" . join(", ", @arg_list) . ");
    SEXP rResult = processResult(result); // $count
    
    aaGlobalFreeAllA();
    UNPROTECT($count);\n";
    
    foreach my $arg (@arg_list) {
        next unless $arg_types{$arg} eq 'double.table';
        print "
    free($arg);"
    }
    
    print "\n
    return rResult;
}\n\n";
 
}

sub printHeader
{
    print <<END_HEADER;
#include <fc.h>
#include <R.h>
#include <Rinternals.h>
#include <Rdefines.h>

double doubleArg(SEXP *args)
{
    SEXP rArg;
    *args = CDR(*args); PROTECT(rArg = coerceVector(CAR(*args), REALSXP));
    double *arg = NUMERIC_POINTER(rArg);
    return *arg;
}

int intArg(SEXP *args)
{
    SEXP rArg;
    *args = CDR(*args); PROTECT(rArg = coerceVector(CAR(*args), INTSXP));
    int *arg = INTEGER_POINTER(rArg);
    return *arg;
}

// For double tables, we take three arguments:  (nrow, ncol, as.vector(as.matrix(df)) or (nrow, ncol, as.vector(matrix))
// Note:  counts as three protected objects!  Don't forget to free()!
LPXLOPER doubleTableArg(SEXP *args)
{
    int nrow = intArg(args);
    int ncol = intArg(args);
    SEXP rData;
    *args = CDR(*args); PROTECT(rData = coerceVector(CAR(*args), REALSXP));
    double *data = NUMERIC_POINTER(rData);

    if(nrow * ncol != length(rData))
        error("nrow * ncol != length(rData): ", nrow, ", ", ncol, ", ", length(rData));

    LPXLOPER retArray = (LPXLOPER) malloc((length(rData) + 1) * sizeof(XLOPER));
    retArray[0].xltype = 64;
    retArray[0].val.array.rows = nrow;
    retArray[0].val.array.columns = ncol;
    retArray[0].val.array.lparray = &retArray[1];

    for(int i = 0; i < nrow * ncol; i++) {
        (retArray->val.array.lparray + i)->xltype = 1;
        (retArray->val.array.lparray + i)->val.num = data[i];
    }

    return retArray;
}


// Here, we mimic what the Java wrapper does, so that the same code can be used to process both results
SEXP processResult(LPXLOPER result)
{
    SEXP rReturnResult;

    if ((result->xltype & 1) || (result->xltype & 256)) {
        PROTECT(rReturnResult = NEW_NUMERIC(2));
        double *returnResult = NUMERIC_POINTER(rReturnResult);
        returnResult[0] = (result->xltype & (1|256));
        returnResult[1] = result->val.num;
    } else if (result->xltype & 16) {
        PROTECT(rReturnResult = NEW_NUMERIC(2));
        double *returnResult = NUMERIC_POINTER(rReturnResult);
        returnResult[0] = 16;
        returnResult[1] = result->val.err;
    } else if (result->xltype & 64) {
        int count = result->val.array.rows * result->val.array.columns + 3;
        PROTECT(rReturnResult = NEW_NUMERIC(count));
        double *returnResult = NUMERIC_POINTER(rReturnResult);
        returnResult[0] = 64;
        returnResult[1] = result->val.array.rows;
        returnResult[2] = result->val.array.columns;
        for(int i = 3; i < count; i++) 
            returnResult[i] = (result->val.array.lparray + (i - 3))->val.num;
    } else {
        error("Unexpected return type: ", result->xltype);
    }

    return(rReturnResult);
}

SEXP getErrorString_wrap(SEXP args)
{
    int errorNum = intArg(&args); // 1
    char errorString[4096];
    GetErrorString(errorNum, 4096, errorString);
    SEXP rErrorString;
    PROTECT(rErrorString = allocVector(STRSXP, 1)); // 2
    SET_STRING_ELT(rErrorString, 0, mkChar(errorString));
    UNPROTECT(2);
    return rErrorString;
}

SEXP aaErrorHandlingEnable_wrap(SEXP args)
{
    int errorLevel = intArg(&args); // 1
    int result = aaErrorHandlingEnable(errorLevel);
    SEXP rReturnResult;
    PROTECT(rReturnResult = NEW_NUMERIC(2));
    double *returnResult = NUMERIC_POINTER(rReturnResult);
    returnResult[0] = 1;
    returnResult[1] = (double) result;
    UNPROTECT(2);
    return rReturnResult;
}

END_HEADER

}
