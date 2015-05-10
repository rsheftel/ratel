/**
 * Created by IntelliJ IDEA.
 * User: dgordon
 * Date: Apr 28, 2009
 * Time: 12:26:52 PM
 * IvyDBFetch.groovy
 * A wrapper for Option Metrics' IvyDBFetch.bat.  It will retry until the file sought is received.
 * The file location and name, the retry attempts and the wait period between retry attempts will
 * be passed from the command line.
 */

import java.text.SimpleDateFormat

def cli = new CliBuilder(usage: 'groovy FetchIvyDB.groovy -cdprw[h]')
cli.h(longOpt: 'help', 'usage information')
cli.c(longOpt: 'command', args: 1, required: true, 'download process')
cli.d(longOpt: 'directory', args: 1, required: true, 'directory')
cli.p(longOpt: 'fileprefix', args: 1, required: true, 'file name prefix')
cli.s(longOpt: 'filesuffix', args: 1, required: true, 'file name suffix')
cli.r(longOpt: 'retries', args: 1, type: int, 'retry attemps')
cli.w(longOpt: 'waittime', args: 1, type: int, 'wait time (seconds) between retries')

def opt = cli.parse(args)
if (!opt) return
if (opt.h) cli.usage

def cmd = opt.c
def dir = opt.d
def pfx = opt.p
def sfx = opt.s
def retry = new Integer(opt.r)
def wait = new Integer(opt.w) * 1000 /* millisecond conversion */

SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd")
Calendar cal = Calendar.getInstance()
def date = sdf.format(cal.getTime())
def fn = pfx + date + sfx
def dirfile = dir + fn
File  file
def filesize

println("Downloading to " + dirfile)
/* Run download */
Systemcall(cmd)

/* Get downloaded file size.  This will be used to verify that the entire file was downloaded */
fileStat= new File("fileStats.txt")
fileStat.splitEachLine(/\s+/) {
    filesize = it[4]
}

/* Retry if file wasn't available or was empty or is not the same size as the file
on the ftp server retry until retry count is exhausted */
file = new File(dirfile)
while (retry != 0 && ((!file.exists()) || (file.length()==0) || ((String)file.length() != filesize))) {
    println("Retry download...")
    sleep(wait)
    Systemcall(cmd)
    fileStat.splitEachLine(/\s+/) {
        filesize = it[4]
    }
    retry--
}
if (!file.exists() || (file.length()==0)) {
    /* send email that file wasn't available*/
    println(fn + " is not available")
    MailMessageAll(fn + " is not available")
} else {
    println("Option Metrics " + fn + " was downloaded to " + dirfile)
    MailMessageDBA("Option Metrics " + fn + " was downloaded to " + dirfile)
}

def Systemcall(args) throws IOException, InterruptedException {
    def line
    Process command
    println args.toString()
    command = Runtime.getRuntime().exec(args.toString())

    BufferedReader Resultset = new BufferedReader(
            new InputStreamReader(
                    command.getInputStream())
    )

    while ((line = Resultset.readLine()) != null) {
        System.out.println(line)
    }
    System.out.println("Finished command")
    return command.waitFor()

}

def MailMessageAll(def msg) {
    def ant = new AntBuilder()
    def mailserver = "mail.fftw.com"
    def fromaddress = "SQLPRODTS@fftw.com"
    def subj = "Option Metrics IvyDB File Download"
    def loadMsg = msg

    ant.mail(mailhost: mailserver, subject: subj) {
        from(address: fromaddress)
        to(address: 'dgordon@fftw.com')
        to(address: 'klam@fftw.com')
        to(address: 'eric.knell@malbecpartners.com')
        to(address: 'jerome.bourgeois@malbecpartners.com')
        to(address: 'david.horowitz@malbecpartners.com')
        message(loadMsg)
    }
}

def MailMessageDBA(def msg) {
    def ant = new AntBuilder()
    def mailserver = "mail.fftw.com"
    def fromaddress = "SQLPRODTS@fftw.com"
    def subj = "Option Metrics IvyDB File Download"
    def loadMsg = msg

    ant.mail(mailhost: mailserver, subject: subj) {
        from(address: fromaddress)
        to(address: 'dgordon@fftw.com')
        to(address: 'klam@fftw.com')
        to(address: 'ayi@fftw.com')
        message(loadMsg)
    }
}



