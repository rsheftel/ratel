package malbec.fer.rediplus;

import static malbec.jacob.JacobUtil.createRefVariant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.CancelRequest;
import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.jacob.rediplus.RediPlusCacheControl;
import malbec.jacob.rediplus.RediPlusOrder;
import malbec.util.EmailSettings;
import malbec.util.NamedThreadFactory;
import malbec.util.TaskService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jacob.com.DispatchEvents;
import com.jacob.com.InvocationProxy;
import com.jacob.com.Variant;

public class RediPlusDestination implements IOrderDestination, RediPlusDestinationMBean {

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private ScheduledFuture<?> monitorFuture;

    private String name;

    private String typeLibrary = "C:\\Program Files\\GS\\REDIPlus\\Primary\\Redi.tlb";

    private RediPlusServer rediPlusServer;

    private RediPlusCacheControl rediPlusCacheControl;

    private boolean connected;

    private Object lockObject = new Object();

    final private Logger log = LoggerFactory.getLogger(getClass());

    static {
        TaskService.getInstance().addExecutor("COMClient",
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("COMClientSchedule")));
    }

    public RediPlusDestination(String name, Properties config, EmailSettings emailSettings) {
        this.name = name;
        rediPlusServer = new RediPlusServer(name, config, emailSettings);
        // in case we move the file
        typeLibrary = config.getProperty("redi.tlb", this.typeLibrary);
        rediPlusCacheControl = new RediPlusCacheControl();

        // assign the event listener
        new DispatchEvents(rediPlusCacheControl, new CacheEventListener(rediPlusCacheControl, pcs),
                "REDI.QUERY", typeLibrary);
    }

    public String getName() {
        return name;
    }

    @Override
    public ITransportableOrder createOrder(Order order) {
        List<String> errors = new ArrayList<String>();

        RediPlusOrder rpo = rediPlusServer.createOrder(order, errors);

        return new RediPlusTransportableOrder(rediPlusServer, rpo, errors);
    }

    @Override
    public String getDestinationName() {
        return name;
    }

    @Override
    public void start() {
        if (monitorFuture == null || monitorFuture.isCancelled()) {
            // start a monitor that ensures the client is running when it should be
            ScheduledExecutorService executor = (ScheduledExecutorService) TaskService.getInstance()
                    .getExecutor("COMClient");
            monitorFuture = executor.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    synchronized (lockObject) {
                        if (!connected) {
                            rediPlusCacheControl.setUserID(rediPlusServer.getUserID());
                            rediPlusCacheControl.setPassword(rediPlusServer.getPassword());

                            Variant errorCode = createRefVariant();

                            Variant rt = rediPlusCacheControl.submit(new Variant("Message"), new Variant(
                                    "true"), errorCode);

                            connected = rt.getBoolean();
                            if (!connected) {
                                log.error("Unable to start RediListener:" + errorCode);
                            }
                        }
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stop() {
        // stop the session monitor from being called again
        if (monitorFuture != null) {
            monitorFuture.cancel(false);
            monitorFuture = null;

            Variant errorCode = createRefVariant();

            Variant rt = rediPlusCacheControl.revoke(errorCode);

            System.err.println("Redi revoke listener result=" + errorCode);
            if (rt.getBoolean()) {
                synchronized (lockObject) {
                    connected = false;
                }
            }
        }
    }

    @Override
    public List<String> validateOrder(Order order) {
        List<String> errors = new ArrayList<String>();
        rediPlusServer.createOrder(order, errors);

        return errors;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public static class CacheEventListener extends InvocationProxy {

        final private Logger log = LoggerFactory.getLogger(getClass());

        private RediPlusCacheControl rediPlusCacheControl;

        private PropertyChangeSupport pcs;

        private CacheEventListener(RediPlusCacheControl rediPlusCacheControl, PropertyChangeSupport pcs) {
            this.rediPlusCacheControl = rediPlusCacheControl;
            this.pcs = pcs;
        }

        public void cacheEvent(int action, int lastUpdatedRow) {
            // RefNum => ExecutionId
            // OrderId => BranchSequence + OmsRefCorrId + OmsRefLineId + OmsRefLineSeq
            String[] columnNames = { "Memo", "ClientData", "OrderRefKey", "Status", "LeavesQTY", "Time",
                "Date", "Type", "ExecPrice", "ExecLeaves", "ExecQuantity", "Symbol", "Side", "Leaves",
                "BranchSequence", "OmsRefCorrId", "OmsRefLineId", "OmsRefLineSeq", "RefNum", };

            if (action == 1) {
                // Get the entire data contents
                for (int i = 0; i < lastUpdatedRow; ++i) {
                    Map<String, String> record = getColumnsFromMessageTable(i, columnNames);
                    log.info(record.toString());
                    pcs.firePropertyChange(new PropertyChangeEvent(this, record.get("TYPE"), null, record));
                }
            } else {
                Map<String, String> record = getColumnsFromMessageTable(lastUpdatedRow, columnNames);
                log.info(record.toString());
                pcs.firePropertyChange(new PropertyChangeEvent(this, record.get("TYPE"), null, record));
            }
        }

        private Map<String, String> getColumnsFromMessageTable(int row, String[] columnNames) {
            Map<String, String> record = new HashMap<String, String>();

            Variant myValue = createRefVariant();
            Variant myError = createRefVariant();
            Variant rowVariant = new Variant(row);

            for (String columnName : columnNames) {
                String upcn = columnName.toUpperCase();
                try {
                    rediPlusCacheControl.getCell(rowVariant, new Variant(columnName), myValue, myError);
                    // if the object within the variant is a variant with a null the isNull does not work
                    if (myValue != null && !myValue.isNull() && myValue.toJavaObject() != null) {
                        record.put(upcn, myValue.toString());
                    } else {
                        record.put(upcn, null);
                        // TODO if we want to open a bug for Jacob, this is where we find out
                        // the info
                        // Object o = myValue.toJavaObject();
                    }
                } catch (Exception e) {
                    log.error("Unable to get value for row: " + row + ", column: " + columnName, e);
                }
            }
            return record;
        }

        @Override
        public Variant invoke(String methodName, Variant[] targetParameters) {
            if ("CacheEvent".equals(methodName)) {
                cacheEvent(targetParameters[0].getInt(), targetParameters[1].getInt());
            }

            return null;
        }

    }

    public boolean isListeningToMessages() {
        synchronized (lockObject) {
            return connected;
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean isActiveSession() {
        // TODO figure out how to deal with this
        return true;
    }

    @Override
    public ITransportableOrder createReplaceOrder(CancelReplaceRequest crr) {
        if (crr != null) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override
    public ITransportableOrder createCancelOrder(CancelRequest cancelRequest) {
        if (cancelRequest != null) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override
    public boolean isForceToTicket() {
        return rediPlusServer.isForceToTicket();
    }

    @Override
    public void setForceToTicket(boolean forceToTicket) {
        rediPlusServer.setForceToTicket(forceToTicket);
    }
    
}
