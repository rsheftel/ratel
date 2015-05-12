#!/usr/bin/env groovy
package shredder.jpmorgan;


import shredder.jpmorgan.JPMorganTba;

class JPMorganDispatcher {

    static void main(args) {
        def cli = new CliBuilder(usage: 'groovy JPMorganDispatcher -s subject -d dir -f filename [-o output dir]')
        cli.h(longOpt: 'help', 'usage information')
        cli.s(longOpt: 'subject', args: 1, required: true, 'email subject line')
        cli.d(longOpt: 'dir', args: 1, required: true, 'directory attachment was saved to')
        cli.f(longOpt: 'filename', args: 1, required: true, 'attachment filename')
        cli.o(longOpt: 'output', args: 1, required: false, 'output dir')

        def opt = cli.parse(args)

        if (!opt) return
        if (opt.h) cli.usage()

        //parse the subject to build the date value for the data
        //20090706
        def regexStr = /\d\d\d\d\d\d\d\d/
        def dateStr = ''
        opt.s.eachMatch(regexStr) {
            match -> dateStr = match[0]
        }
        if (dateStr.length() == 0) {
            println("No date in subject, must be TBA file - checking filename for load date")
            // check if the file has date
            opt.f.eachMatch(regexStr) {
                match -> dateStr = match[0]
            }
            if (dateStr.length() == 0) {
                println("Cannot determine load date")
                return; // cannot determine load date
            }
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

        if (opt.s =~ /TBA Prices/) {
            println "calling shredder.jpmorgan.JPMorganTba.groovy"
            def gp = new JPMorganTba();
            gp.convertFile(opt.d, srcFilename, dateStr, outputDir);
        } else if (opt.s =~ /TBA pass-through csv file as of/) {
            println "calling shredder.jpmorgan.JPMorganTbaSupplemental.groovy"
            def gp = new JPMorganTbaSupplemental();
            gp.convertFile(opt.d, srcFilename, dateStr, outputDir);
    } else if (opt.s =~ /JPMorgan Swap Curve for/) {
            println "calling shredder.jpmorgan.JPMorganSwap.groovy"
            def gp = new JPMorganSwap();
            gp.convertFile(opt.d, srcFilename, dateStr, outputDir);
        }
    }
}