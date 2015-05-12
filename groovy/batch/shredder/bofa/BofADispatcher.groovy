#!/usr/bin/env groovy
package shredder.bofa;

import shredder.bofa.BofATba
import javax.mail.Session
import javax.mail.internet.MimeMessage
import java.nio.charset.Charset;


class BofADispatcher {

    static void main(args) {
        def cli = new CliBuilder(usage: 'groovy BofADispatcher -s subject -d dir -f filename [-o output dir]')
        cli.h(longOpt: 'help', 'usage information')
        cli.s(longOpt: 'subject', args: 1, required: true, 'email subject line')
        cli.d(longOpt: 'dir', args: 1, required: true, 'directory attachment was saved to')
        cli.f(longOpt: 'filename', args: 1, required: false, 'attachment filename')
        cli.e(longOpt: 'expression', args: 1, required: false, 'attachment filename expression')
        cli.o(longOpt: 'output', args: 1, required: false, 'output dir')

        def opt = cli.parse(args)

        if (!opt) return
        if (opt.h) cli.usage()
        if (opt.e == null && opt.f == null) {
            cli.usage();
        }

        //parse the subject to build the date value for the data
        //02/02/2009 
        def regexStr = /(\d\d|\d)\/(\d\d|\d)\/\d\d\d\d/
        def dateStr = ''

        // if we forward from Notes the subject will be correct, otherwise it
        // is UTF-8 encoded
        String subject = convertSubject(opt.s);
        println "Input subject: " + opt.s
        println "Converted subject: " + subject;
        
        subject.eachMatch(regexStr) {
            match -> dateStr = match[0]
        }
        if (dateStr.length() == 0) {
            println "No valid date in subject";
            return; // no valid date, do not process file
        }

        def outputDir;
        if (opt.o) {
            File dir = new File(opt.o);
            outputDir = dir.getAbsolutePath() + File.separator;
        } else {
            outputDir = "/data/TSDB_upload/Today/"
        }

        if (subject =~ /BAS TBA Closing Prices for/) {
            def filesToProcess = [];
            def tmpFilename;
            // build the source file from the parameters
            if (opt.e) {
                def pattern = ~/$opt.e/;
                // we are only interesed in two of the files
                // 15_Yr_Closes*.xls
                // 30_Yr_Closes*.xls
                def basedir = new File(opt.d);
//                filesToProcess.addAll(basedir.listFiles().grep(~/.*(15|30)_Yr_Close.*xls/))
                println("using pattern: " + pattern)
                println("using basedir: " + basedir)
                filesToProcess.addAll(basedir.listFiles().grep(pattern))
            } else if (opt.f.endsWith("\"")) {
                tmpFilename = opt.f.substring(0, opt.f.length() - 1)
                filesToProcess.add(new File(tmpFilename));
            } else {
                filesToProcess.add(new File(opt.f));
            }

            println "calling shredder.bofa.BofATba.groovy"
            // extract just the filename
            // (.*/)?(.*\..*)  // until I can figure out how to get groovy to accept '/' use this
            // http://jira.codehaus.org/browse/GROOVY-1698
            println("Files to process: " + filesToProcess.size());
            for (File fileToProcess: filesToProcess) {
                def srcFilename = fileToProcess.getName()
                println("Processing file: " + srcFilename)
                def shredder = new BofATba();
                shredder.convertFile(opt.d, srcFilename, dateStr, outputDir);
            }
        }
    }

    private static String convertSubject(String encodedSubject) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        message.setSubject(encodedSubject, "UTF-8");
        
        return message.getSubject();
    }
}