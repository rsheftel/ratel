#!/usr/bin/env groovy

/**
 * Based on the email subject dispatch to a specific groovy script
 */
class GoldmanDispatcher {

    static main(args) {

        def cli = new CliBuilder(usage: 'groovy GoldmanDispatcher -s subject -d dir -f filename [-o output dir]')
        cli.h(longOpt: 'help', 'usage information')
        cli.s(longOpt: 'subject', args: 1, required: true, 'email subject line')
        cli.d(longOpt: 'dir', args: 1, required: true, 'directory attachment was saved to')
        cli.f(longOpt: 'filename', args: 1, required: true, 'attachment filename')
        cli.o(longOpt: 'output', args: 1, required: false, 'output dir')

        def opt = cli.parse(args)

        if (!opt) return
        if (opt.h) cli.usage()

        //parse the subject to build the date value for the data
        def regexStr = /(\d\d|\d)[a-zA-Z]{3}\d\d/
        def dateStr = ''
        opt.s.eachMatch(regexStr) {
            match -> dateStr = match[0]
        }
        if (dateStr.length() == 0) {
            return; // no valid date, do not process file
        }

        def outputDir = ''
        if (opt.o) {
            outputDir = opt.o
        } else {
            outputDir = "/data/TSDB_upload/Today/"
        }

        // build the source file from the parameters
        def tmpFilename = opt.f
        if (opt.f.endsWith("\"")) {
            tmpFilename = opt.f.substring(0, opt.f.length() - 1)
        }
        // extract just the filename
        // (.*/)?(.*\..*)  // until I can figure out how to get groovy to accept '/' use this
        // http://jira.codehaus.org/browse/GROOVY-1698
        def srcFilename = new File(tmpFilename).getName()

        // Determine which script we should dispatch to
        if (opt.s =~ /GS Mortgage Passthru Sector Pricing/) {
            println "calling GoldmanParse.groovy"
            def gp = new GoldmanParse();
            gp.convertFile(opt.d, srcFilename, dateStr, outputDir);
        } else if (opt.s =~ /Goldman Sachs Vol and Forward Data/) {
            println "calling GoldmanFXParse.groovy"
            def gp = new GoldmanFXParse();
            gp.convertFile(opt.d, srcFilename, dateStr, outputDir);
        }
    }
}