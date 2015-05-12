package reporting

import groovy.sql.Sql
import java.sql.Date
import org.joda.time.LocalDate
import org.joda.time.DateTime
import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


/**
 * todo - switches for aggregation and emails
 */
public class EndOfDayUnfilledOrders{
    
    //static final String DB_SERVER_NAME = "SQLDEVTS"
    static final String DB_SERVER_NAME = "SQLPRODTS"
    
    //static final String EMAIL_LIST = "Sim_Team@malbecpartners.com,Operations@malbecpartners.com,MFranz@fftw.com"
    static final String EMAIL_LIST = "MFranz@fftw.com"
    
    static final String JDBC_URL = 'jdbc:jtds:sqlserver://' + DB_SERVER_NAME + ':2433/BADB'
    
    static final String SQL_SENDER_COMP_IDS = "'TRAD', 'MSDW-PPT', 'REDIRPT', 'TRADEWEB', 'BLPDROP'"
    
        static final String SQL_SIDE_MAP = 
            "case Side when 1 then 'Buy' when 2 then 'Sell' when 3 then 'Buy Minus' " +
            "when 4 then 'Sell Plus' when 5 then 'Sell Short' when 6 then 'Sell Short Exempt' " +
            "when 7 then 'Undisclosed' when 8 then 'Crossed' end "
            
    // OrderStatus 1 - partial; 2 - filled            
    static final String QUERY_STR = "select distinct " +
    "x.SenderCompId, x.SenderSubId, x.OrderId, x.TradeDate, x.Symbol, x.ExecutingBroker, " +
    SQL_SIDE_MAP + " as ReportSide" +
    " from dbo.FixFill x " +
    "where x.OrderStatus = 1 and TradeDate=? " +
    "and SenderCompId in (" + SQL_SENDER_COMP_IDS + ") " +
    "and not exists (" +
    "select null " +
    "from dbo.FixFill y " +
    "where y.OrderStatus = 2 " +
    "and x.SenderCompId = y.SenderCompId " +
    "and isnull(x.SenderSubId,'X') = isnull(y.SenderSubId,'X') " +
    "and x.OrderId = y.OrderId "+
    "and x.TradeDate = y.TradeDate)"
    
    
    public static void main(args) {
        def cli = new CliBuilder(usage: 'groovy EndOfDayBrokerSummary [-d yyyymmdd]')
        cli.h(longOpt: 'help', 'usage information')
        cli.d(longOpt: 'date', args: 1, required: false, 'report date')
        cli.r(longOpt: 'report', args: 0, required: false, 'report warnings via email')
        
        def opt = cli.parse(args)
        
        if (!opt) {
            return
        }
        if (opt.h) cli.usage();
        
        LocalDate today = new LocalDate();
        if (opt.d) {
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
            today = dtf.parseDateTime(opt.d).toLocalDate();
        }
        
        DateMidnight dmd = today.toDateMidnight()
        Date dbDate = new Date(dmd.getMillis())
        
        def baDB = Sql.newInstance(JDBC_URL,
                'sim',
                'Sim5878',
                'net.sourceforge.jtds.jdbc.Driver')
        
        def queryStr = buildQuery()

        List results = baDB.rows(queryStr, [dbDate])
        
        Map<String, Map<String, String>> orderReport = new LinkedHashMap<String, Map<String, String>>()
        
        
        def reportTitle = buildReportTitle(today)
        
        StringBuilder reportBody = new StringBuilder(1024)
        
        reportBody.append(reportTitle).append("\n")
        
        if (results.size() == 0) {
            reportBody.append("\n*** NOTHING TO REPORT ***")
            print(reportBody.toString())
            return;
        }
        
        reportBody.append("\n")
        reportBody.append("Broker\tSymbol\tSide")
        reportBody.append("\n")
        
        for (reportLine in results) {
            reportBody.append(reportLine.ExecutingBroker).append("\t")
            reportBody.append(reportLine.Symbol).append("\t")
            reportBody.append(reportLine.ReportSide)
            reportBody.append("\n")
        }
        
        print(reportBody)
        
        if (opt.r) {
            File attachment = new File("unfilled_orders_" + today.toString() + ".xls")
            attachment.append(reportBody.toString())
            
            def ant = new AntBuilder()
            ant.mail(mailhost:'mail.fftw.com',subject:reportTitle,
                files:attachment.getAbsolutePath(),
                tolist:EMAIL_LIST) {
                from(address:'Daily Report Error <Report@malbecpartners.com>')
                message(reportBody.toString())
            }
        }
    }
    
    
    private static int getSideValue(Map record, String side) {
        if (record.containsKey(side)) {
            return record.get(side);
        }
        return 0;
    }
    
    private static String buildQuery() {
        
        StringBuilder sb = new StringBuilder(256);
        sb.append(QUERY_STR)
        
        return sb.toString()
    }
    
    static String buildReportTitle(LocalDate reportDate) {
        StringBuilder sb = new StringBuilder(256)
        
        sb.append("Unfilled Trades ")
        sb.append("Report for ").append(reportDate)
        
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        
        sb.append("\tRun at ")
        sb.append(fmt.print(new DateTime()))
        
        return sb.toString()
    }
}
