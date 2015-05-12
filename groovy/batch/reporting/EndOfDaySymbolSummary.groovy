package reporting

import groovy.sql.Sql
import java.sql.Date
import org.joda.time.LocalDate
import org.joda.time.DateTime
import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


/**
 *
 */
public class EndOfDaySymbolSummary {
    
    static final String EMAIL_LIST = "Sim_Team@malbecpartners.com,Operations@malbecpartners.com,MFranz@fftw.com"
    //static final String EMAIL_LIST = "MFranz@fftw.com,mfranz@fftw.com"
    
    //static final String DB_SERVER_NAME = "SQLDEVTS"
    static final String DB_SERVER_NAME = "SQLPRODTS"
    
    static final String JDBC_URL = 'jdbc:jtds:sqlserver://' + DB_SERVER_NAME + ':2433/BADB'
    
    static final String SQL_GROUP_FIELDS = "SenderCompId, TradeDate, Symbol, ExecutingBroker "
    
    public static void main(args) {
        def cli = new CliBuilder(usage: 'groovy EndOfDaySymbolSummary [-d yyyymmdd]')
        cli.h(longOpt: 'help', 'usage information')
        cli.d(longOpt: 'date', args: 1, required: false, 'report date')
        //        cli.c(longOpt: 'consolidate', args: 1, required: false, 'consolidate as buy/buy cover and sell/sell short')
        cli.e(longOpt: 'email', args: 0, required: false, 'email warnings')
        cli.b(longOpt: 'broker', args: 1, required: false, 'for this broker only')
        cli.r(longOpt: 'report', args: 0, required: false, 'report via email')
        
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
        
        String selectedBroker = null;
        if (opt.b) {
            selectedBroker = opt.b;
        }
        def rp = new ReportingHelper();
        def queryStr = buildQuery(true, selectedBroker)
        
        List results = null;
        
        if (selectedBroker != null) {
            results = baDB.rows(queryStr, [dbDate, selectedBroker])
        } else {
            results = baDB.rows(queryStr, [dbDate])
        }
        
        Map<String, Map<String, String>> orderReport = new LinkedHashMap<String, Map<String, String>>()
        
        for (orderSummary in results) {
            def orderKey = orderSummary.SenderCompId + orderSummary.TradeDate + 
            orderSummary.Symbol + orderSummary.ExecutingBroker
            
            def order = orderReport.get(orderKey);
            if (order == null) {
                order = new HashMap<Integer, Integer>();
                orderReport.put(orderKey, order);
                order.put("Broker", orderSummary.ExecutingBroker)
                order.put("Symbol", orderSummary.Symbol)
            } 
            
            def reportSide = orderSummary.ReportSide
            
            order.put(reportSide, orderSummary.TotalOrderQuantity);
            order.put(reportSide+"tap", orderSummary.TotalAveragePrice)
            
        }
        
        // Report by Symbol
        def reportTitle = buildReportTitle(opt.b, today)
        
        StringBuilder reportBody = new StringBuilder(1024)
        
        reportBody.append(reportTitle).append("\n")
        
        if (orderReport.size() == 0) {
            reportBody.append("\n*** NOTHING TO REPORT ***")
            print(reportBody.toString())
            return;
        }
        reportBody.append("\n\nSymbol\tBroker\tBuys\tSells\tNet\tBuy AvgP\tSell AvgP")
        reportBody.append("\n")
        
        def allNetToZero = true;
        
        def currentSymbol = ""
        for (reportLine in orderReport) {
            Map entry = reportLine.getValue()
            
            def newSymbol = entry.get("Symbol")
            if (newSymbol == null || newSymbol == "null") {
                newSymbol = "Unknown"
            }
            if (currentSymbol != newSymbol) {
                currentSymbol = newSymbol
                reportBody.append(currentSymbol)
                reportBody.append("\n")
                
            }
            reportBody.append("\t" + entry.get("Broker"))
            int buys = ReportingHelper.getSideValue(entry, "Buy");
            double buyAveragePrice = ReportingHelper.getSideAveragePrice(entry, "Buy")
            
            int sells = ReportingHelper.getSideValue(entry, "Sell");
            double sellAveragePrice = ReportingHelper.getSideAveragePrice(entry, "Sell")
          
            
            if (allNetToZero && (buys - sells) != 0) {
                allNetToZero = false;
            }
            
            reportBody.append("\t" + buys + "\t" + sells + "\t" + (buys - sells))
            reportBody.append("\t" + String.format("%10.4f", buyAveragePrice / buys) + 
                    "\t" + String.format("%10.4f",sellAveragePrice / sells))
                    
            reportBody.append("\n")
        }
        
        print(reportBody)
        
        if (opt.e && !allNetToZero) {
            // email a warning somewhere
            reportBody.append("\nBroker trades do not NET to zero (0)")
            def ant = new AntBuilder()
            ant.mail(mailhost:'mail.fftw.com',
                    subject:reportTitle,
                    tolist:EMAIL_LIST) {
                from(address:'Daily Report Error <Report@malbecpartners.com>')
                to(address:EMAIL_LIST)
                message(reportBody.toString())
            }
        }
        // email the report
        if (opt.r) {
            //File attachment = File.createTempFile("symbol-summary", ".xls")
            File attachment = new File("symbol-summary_" + today.toString() + ".xls")
            attachment.append(reportBody.toString())
            def ant = new AntBuilder()
            ant.mail(mailhost:'mail.fftw.com',
                    subject:reportTitle,
                    files:attachment.getAbsolutePath(),
                    tolist:EMAIL_LIST) {
                from(address:'Daily Report <Report@malbecpartners.com>')
                
                message(reportBody.toString())
            }
            
            attachment.deleteOnExit()
        }
    }
    
    private static String buildQuery(Boolean consolidate, String broker) {
        
        StringBuilder sb = new StringBuilder(256);
        
        sb.append("Select ").append(SQL_GROUP_FIELDS)
        
        if (consolidate) {
            sb.append(", ").append(ReportingHelper.SQL_SIDE_CONSOLIDATION)
        } else {
            sb.append(", ").append(ReportingHelper.SQL_SIDE_MAP)
        }
        
        sb.append(" as ReportSide, ")
        sb.append(ReportingHelper.SQL_AGGREGATE)
        sb.append(ReportingHelper.SQL_ALL_WHERE)
        
        if (broker != null) {
            sb.append(ReportingHelper.SQL_AND_BROKER)
        } 
        
        // create the actual 'group by'
        sb.append(" group by ")
        sb.append(SQL_GROUP_FIELDS)
        
        if (consolidate) {
            sb.append(", ").append(ReportingHelper.SQL_SIDE_CONSOLIDATION)
        } else {
            sb.append(", ").append(ReportingHelper.SQL_SIDE_MAP)
        }
        
        return sb.toString()
    }
    
    static String buildReportTitle(Object broker, LocalDate reportDate) {
        StringBuilder sb = new StringBuilder(256)
        
        sb.append("Drop Copy Symbol ")
        if (broker) {
            sb.append(broker).append(" ")
        }
        
        sb.append("Report for ").append(reportDate)
        
        
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        
        sb.append("\tRun at ")
        sb.append(fmt.print(new DateTime()))
        
        return sb.toString()
    }
}
