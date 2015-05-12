#!/usr/bin/env groovy
package shredder.merrill;


import shredder.merrill.MerrillTba;

class MerrillDispatcher {

  static void main(args) {
      def cli = new CliBuilder(usage: 'groovy MerrillDispatcher -s subject -d dir -f filename [-o output dir]')
      cli.h(longOpt: 'help', 'usage information')
      cli.s(longOpt: 'subject', args: 1, required: true, 'email subject line')
      cli.d(longOpt: 'dir', args: 1, required: true, 'directory attachment was saved to')
      cli.f(longOpt: 'filename', args: 1, required: true, 'attachment filename')
      cli.o(longOpt: 'output', args: 1, required: false, 'output dir')

      def opt = cli.parse(args)

      if (!opt) return
      if (opt.h) cli.usage()

      //parse the subject to build the date value for the data
      //20071228 
      def regexStr = /\d{8}/
      def dateStr = ''
      opt.s.eachMatch(regexStr) {
          match -> dateStr = match[0]
      }
      if (dateStr.length() == 0) {
          println "No valid date in subject";
          return; // no valid date, do not process file
      }

      def outputDir = ''
      if (opt.o) {
          File dir = new File(opt.o);
          outputDir = dir.getAbsolutePath() + File.separator;
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
      
      if (opt.s =~ /Tba_prices_for_/) {
          println "calling shredder.merrill.MerrillTba.groovy"
          def gp = new MerrillTba();
          gp.convertFile(opt.d, srcFilename, dateStr, outputDir);
      }

  }

}