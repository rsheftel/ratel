/**
 * 
 */
package malbec.redi.fix;

import static malbec.jacob.JacobUtil.createRefVariant;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import malbec.jacob.rediplus.RediPlusCacheControl;

import com.jacob.com.InvocationProxy;
import com.jacob.com.Variant;

public class CacheEvent extends InvocationProxy
{
    RediPlusCacheControl cc;

    static final String ACCOUNT_KEY = "ACCOUNTALIAS";
    
    public BlockingQueue<Map<String, String>> executions = new LinkedBlockingQueue<Map<String, String>>();

    public CacheEvent (RediPlusCacheControl cc)
    {
        this.cc = cc;
    }

    public void cacheEvent (Variant action, Variant row)
    {
        String[] columnNames =
        {
            "Memo", "ClientData", "OrderRefKey", "Status", "ExecLeaves", "Time", "EXECQUANTITY",
            "ExecPrice", "Type", "BranchSequence", "OmsRefCorrId", "OmsRefLineId", "OmsRefLineSeq",
            "RefNum", "Side", "Symbol", "ENTRYUSERID", "EXCHANGEDATE", "ACCOUNT", "AVGEXECPRICE",
            "CURRENCY", "LASTMARKET", "ORDSTAT", "ORDERTYPE", "EXECUTIONTYPE", ACCOUNT_KEY,
            "EXCHANGETYPE", "QUANTITY", "Broker", "MsgLine"
        };

        Variant myValue = createRefVariant();
        Variant myError = createRefVariant();

        if (action.getInt() == 1)
        {
            // Get the entire data contents
            for (int i = 0; i < row.getInt(); ++i)
            {
                Map<String, String> record = new HashMap<String, String>();
                for (String columnName : columnNames)
                {
                    try
                    {
                        // System.out.println("Retrieving row:" + i +
                        // ", column: "+ columnName);
                        cc.getCell(new Variant(i), new Variant(columnName), myValue, myError);
                        if (myValue != null && !myValue.isNull() && myValue.toJavaObject() != null) {
                            record.put(columnName, myValue.toString());
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                addIfValid(record);
            }
        }
        else
        {
            // cc.getCell(new Variant(row.getInt()), column, myValue, myError);
            int currentRow = row.getInt();

            Map<String, String> record = new HashMap<String, String>();
            for (String columnName : columnNames)
            {
                try
                {
                    // System.out.println("Retrieving row:" + i + ", column: "+
                    // columnName);
                    cc.getCell(new Variant(currentRow), new Variant(columnName), myValue, myError);
                    if (myValue != null && !myValue.isNull() && myValue.toJavaObject() != null) {
                        record.put(columnName, myValue.toString());
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            addIfValid(record);
        }
    }

    private void addIfValid (Map<String, String> record)
    {
        String type = record.get("Type");
        String exchange = record.get("EXCHANGETYPE");

        if (type.equalsIgnoreCase("execution") && !exchange.contains("SPD"))
        {
            record.put("POSDUP", "Y");
            if (!executions.offer(record)) {
                System.err.println("Failed to add item to queue: " + record);
            }
        }
    }

    @Override
    public Variant invoke (String methodName, Variant[] targetParameters)
    {
        // for some reason this does not call us directly
        if ("CacheEvent".equals(methodName))
        {
            cacheEvent(targetParameters[0], targetParameters[1]);
        }
        return null;
    }
}