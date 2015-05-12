package malbec.redi.fix;

import static malbec.jacob.JacobUtil.createRefVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import malbec.fer.mapping.DatabaseMapper;
import malbec.fix.server.FixServer;
import malbec.jacob.rediplus.RediPlusCacheControl;
import malbec.util.DateTimeUtil;
import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import quickfix.Message;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.Currency;
import quickfix.field.ExecBroker;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.MaturityMonthYear;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.PositionEffect;
import quickfix.field.PossResend;
import quickfix.field.Price;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityType;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix42.ExecutionReport;

import com.jacob.com.DispatchEvents;
import com.jacob.com.Variant;

public class RediToFix implements Runnable {

    private static final String[] FUTURES_ACCOUNT = { "C0778088-T", "NDAYBRK", "FNCC", "CPNSWAPF",
        "CPNSWAPD", "PORTHEDGE", "LIQINJ" };

    static List<String> futuresAccounts = new ArrayList<String>();

    private EmailSettings emailSettings = new EmailSettings();

    private FixServer fixServer;

    private DatabaseMapper dm;

    private String typeLibrary = "C:\\Program Files\\GS\\REDIPlus\\Primary\\Redi.tlb";

    private String userId;

    private String password;

    private RediPlusCacheControl ccc;

    private CacheEvent cacheEventHandler;

    private boolean running;

    public RediToFix(FixServer fixServer, EmailSettings emailSetting, DatabaseMapper dm, Properties config) {
        typeLibrary = config.getProperty("redi.tlb", this.typeLibrary);
        userId = config.getProperty("userId");
        password = config.getProperty("password");

        for (String account : FUTURES_ACCOUNT) {
            futuresAccounts.add(account);
        }

        this.dm = dm;
        this.fixServer = fixServer;

        ccc = new RediPlusCacheControl("REDI.QUERY");
        cacheEventHandler = new CacheEvent(ccc);
        new DispatchEvents(ccc, cacheEventHandler, "REDI.QUERY", typeLibrary);

        ccc.setUserID(userId);
        ccc.setPassword(password);

        // emailSettings.setFromAddress("RediPlus COM <RediCOMServer@fftw.com>");

        this.emailSettings = emailSetting;
    }

    public void start() {
        fixServer.start();
        Variant errorCode = createRefVariant();
        Variant rt = ccc.submit(new Variant("Message"), new Variant("true"), errorCode);
        System.out.println("Status=" + rt.toString());
        setRunning(true);

        new Thread(this, "RediToFix").start();
    }

    public void stop() {
        Variant errorCode = createRefVariant();
        Variant rt = ccc.revoke(errorCode);
        System.out.println("Return status:" + rt.toString());

        setRunning(false);
        fixServer.stop();
    }

    @Override
    public void run() {
        Map<String, String> er = null;

        while (isRunning()) {
            try {
                while ((er = cacheEventHandler.executions.poll()) != null) {
                    Message fixMessage = convertMapToMessage(er);
                    fixServer.sendFixMessage(fixMessage);
                    System.out.println(fixMessage);
                }
                Thread.sleep(100);
            } catch (Exception e) {
                EmailSender sender = new EmailSender(emailSettings.getAsProperties());
                sender.sendMessage("Exception on convert thread", StringUtils.exceptionToString(e));
                e.printStackTrace();
            }
        }
    }

    private Message convertMapToMessage(Map<String, String> erMap) {

        ExecutionReport er = new ExecutionReport();

        if (erMap.get("POSDUP") != null) {
            // This is an application level dup, not a protocol dup
            er.getHeader().setBoolean(PossResend.FIELD, true);
        }

        // er.set(new SenderCompID(erMap.get("ENTRYUSERID")));
        er.set(new Account(erMap.get(CacheEvent.ACCOUNT_KEY)));
        er.set(new AvgPx(Double.parseDouble(erMap.get("AVGEXECPRICE"))));

        // TODO ClientOrderID

        er.set(new ClOrdID(erMap.get("BranchSequence")));
        er.set(new Currency(erMap.get("CURRENCY")));
        er.set(new ExecID(erMap.get("RefNum")));

        er.set(new ExecTransType('0')); // New

        er.set(new LastPx(Double.parseDouble(erMap.get("ExecPrice"))));
        er.setDouble(LastQty.FIELD, Double.parseDouble(erMap.get("EXECQUANTITY")));

        er.set(new OrderID(erMap.get("BranchSequence") + erMap.get("OmsRefCorrId")
                + erMap.get("OmsRefLineId") + erMap.get("OmsRefLineSeq")));

        er.set(new OrdType('1'));
        er.set(new Price(Double.parseDouble(erMap.get("ExecPrice"))));

        convertSide(erMap, er);

        er.set(new Symbol(erMap.get("Symbol")));

        if (futuresSymbol(erMap)) {
            er.set(new SecurityType(SecurityType.FUTURE));
            er.setString(MaturityMonthYear.FIELD, getFuturesMaturityDate(erMap.get("Symbol")));
            convertSymbolIfRequired(erMap, er);
        }

        convertExecutionType(erMap, er);
        // TODO CumQty, LeavesQty

        convertQuantities(erMap, er);
        er.set(new ExecBroker(erMap.get("Broker")));

        er.set(getSecurityExchange(erMap));
        er.set(getTransactionTime(erMap));
        return er;
    }

    private void convertQuantities(Map<String, String> erMap, ExecutionReport er) {

        double orderQuantity = Double.parseDouble(erMap.get("QUANTITY"));

        double leaves = 0;
        // see if we can extract from the MsgLine
        String msgLine = erMap.get("MsgLine");
        int startPos = msgLine.indexOf("LVS");
        if (startPos != -1) {
            String leavesStr = msgLine.substring(startPos, msgLine.length());
            String[] parts = leavesStr.split(" ");
            leaves = Double.parseDouble(parts[1]);
        }

        er.set(new OrderQty(orderQuantity));
        er.set(new LeavesQty(leaves));
        er.set(new CumQty(orderQuantity - leaves));
    }

    private SecurityExchange getSecurityExchange(Map<String, String> erMap) {
        String exchange = erMap.get("EXCHANGETYPE");

        if ("CME".equalsIgnoreCase(exchange)) {
            return new SecurityExchange("2"); // use the RIC
        } else {
            return new SecurityExchange(exchange);
        }
    }

    private TransactTime getTransactionTime(Map<String, String> erMap) {
        String time = erMap.get("Time");
        String date = erMap.get("EXCHANGEDATE");

        DateTime dt = DateTimeUtil.getCurrentFromTime(time);
        LocalDate ld = DateTimeUtil.getDateFromJavaString(date);

        DateTime tt = new DateTime(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfMonth(), dt.getHourOfDay(),
                dt.getMinuteOfHour(), dt.getSecondOfMinute(), 0);

        return new TransactTime(tt.toDate());
    }

    private void convertSymbolIfRequired(Map<String, String> erMap, ExecutionReport er) {
        String futuresSymbolExtract = erMap.get("Symbol");

        String[] tmpSymbol = futuresSymbolExtract.split(" ");
        String futuresSymbol = tmpSymbol[0];

        String symbol = dm.reverseMapFuturesSymbol("REDI", futuresSymbol.substring(0, 2));

        if (symbol != null && symbol.trim().length() != 0) {
            String tmp = symbol + futuresSymbol.substring(2, futuresSymbol.length());
            er.set(new Symbol(tmp));
            System.out.println("DB - Replacing futures symbol - " + futuresSymbol + " with " + tmp);
            return;
        }

        // use the unconverted symbol
        er.set(new Symbol(futuresSymbol));
    }

    private boolean futuresSymbol(Map<String, String> erMap) {
        String accountType = dm.lookupAccountType("REDI", erMap.get(CacheEvent.ACCOUNT_KEY));
        
        return "FUTURES".equalsIgnoreCase(accountType);
    }

    private String getFuturesMaturityDate(String inSymbol) {
        String[] tmpSymbol = inSymbol.split(" ");
        String symbol = tmpSymbol[0];

        String monthStr = symbol.substring(symbol.length() - 2, symbol.length() - 1);
        String month = "00";

        if (monthStr.equalsIgnoreCase("F")) {
            month = "01"; // January
        }
        if (monthStr.equalsIgnoreCase("G")) {
            month = "02"; // February
        }
        if (monthStr.equalsIgnoreCase("H")) {
            month = "03"; // March
        }
        if (monthStr.equalsIgnoreCase("J")) {
            month = "04"; // April
        }
        if (monthStr.equalsIgnoreCase("K")) {
            month = "05"; // May
        }
        if (monthStr.equalsIgnoreCase("M")) {
            month = "06"; // June
        }
        if (monthStr.equalsIgnoreCase("N")) {
            month = "07"; // July
        }
        if (monthStr.equalsIgnoreCase("Q")) {
            month = "08"; // August
        }
        if (monthStr.equalsIgnoreCase("U")) {
            month = "09"; // September
        }
        if (monthStr.equalsIgnoreCase("V")) {
            month = "10"; // October
        }
        if (monthStr.equalsIgnoreCase("X")) {
            month = "11"; // November
        }
        if (monthStr.equalsIgnoreCase("Z")) {
            month = "12"; // December
        }

        String yearStr = symbol.substring(symbol.length() - 1, symbol.length());

        int year = 2000 + Integer.parseInt(yearStr);

        return String.valueOf(year) + month;
    }

    private void convertExecutionType(Map<String, String> erMap, ExecutionReport er) {
        String orderStatus = erMap.get("ORDSTAT");

        if ("PARTIAL".equalsIgnoreCase(orderStatus)) {
            er.set(new ExecType('1'));
            er.set(new OrdStatus('1'));
            return;
        }

        if ("COMPLETE".equalsIgnoreCase(orderStatus)) {
            er.set(new ExecType('2'));
            er.set(new OrdStatus('2'));
            return;
        }

    }

    private void convertSide(Map<String, String> erMap, ExecutionReport er) {

        String side = erMap.get("Side");

        if ("BUY".equalsIgnoreCase(side)) {
            er.set(new Side('1'));
            return;
        }

        if ("BUY COVER".equalsIgnoreCase(side)) {
            er.set(new Side('1'));
            er.setChar(PositionEffect.FIELD, 'C');
            return;
        }

        if ("SELL".equals(side)) {
            er.set(new Side('2'));
            return;
        }

        if ("SELL SHORT".equals(side)) {
            er.set(new Side('5'));
            return;
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

}
