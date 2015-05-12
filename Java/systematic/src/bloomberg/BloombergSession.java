package bloomberg;

import static java.lang.Math.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Sequence.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import util.*;

import com.bloomberglp.blpapi.*;
import com.bloomberglp.blpapi.Event.*;

public class BloombergSession {
    //private static final String DEV = "nysrv39";
    private static final String PROD = "192.168.11.31";
    private static final String SERVER_API_HOST = PROD;
    private static final String DATA_SERVICE_NAME = "//blp/refdata";
    private static final String AUTH_SERVICE_NAME = "//blp/apiauth";
    private static final String LIVE_SERVICE_NAME = "//blp/mktdata";
    private static BloombergSession sessionDONOTREFERENCE;

    private final Session session;
    private static final Map<String, BloombergListener> listeners = emptyMap();
    
    public BloombergSession(Session session) {
        this.session = session;
    }

    public static BloombergSession session() {
        if(sessionDONOTREFERENCE != null) return sessionDONOTREFERENCE;
        SessionOptions options = new SessionOptions();
        options.setServerHost(SERVER_API_HOST);
        final ExecutorService threadPool = Executors.newFixedThreadPool(20);
        EventHandler eventHandler = new EventHandler() {
            @Override public void processEvent(final Event event, Session session) {
                threadPool.submit(new Runnable() {
                    @Override public void run() { process(event); }
                });
            }
        };
        sessionDONOTREFERENCE = new BloombergSession(new Session(options, eventHandler));
        sessionDONOTREFERENCE.start();
        return sessionDONOTREFERENCE;
    }
    
    private static void process(Event event) {
        MessageIterator i = event.messageIterator();
        
        while(i.hasNext()) {
            Message m = i.next();
            for(int idIndex : zeroTo(m.numCorrelationIds())) {
                try {
                    CorrelationID correlationId = m.correlationIDAt(idIndex);
                    if (correlationId == null) continue;
                    String id = correlationId.isObject() 
                        ? (String) correlationId.object() 
                        : "NOT_OURS:" + correlationId.toString(); 
                    if(listeners.containsKey(id)) {
                        if (verbose()) 
                            Log.info("received " + event.eventType() + ": " + m);
                        listeners.get(id).onMessage(m);
                    } else if (verbose()) 
                        info("skipping event: " + event.eventType() + " message:\n" + m);
                } catch(Exception e) {
                    err("caught while processing message (dropping)", e);
                }
            }
        }
    }

    private void start() {
        try {
            bombUnless(session.start(), "session.start() failed");
            openService(DATA_SERVICE_NAME);
            openService(AUTH_SERVICE_NAME);
            openService(LIVE_SERVICE_NAME);
        } catch(Exception e) {
            throw bomb("exception occurred starting session: ", e);
        }
    }

    private void openService(String name) throws InterruptedException, IOException {
        bombUnless(session.openService(name), "openService returned false");
    }

    public Service authService() {
        return service(AUTH_SERVICE_NAME);
    }

    public Service dataService() {
        return service(DATA_SERVICE_NAME);
    }

    private Service service(String name) {
        return session.getService(name);
    }

    public List<Event> responses(List<Request> requests) {
        List<Event> result = empty();
        List<Request> copy = copy(requests);
        while (!copy.isEmpty()) { 
            int count = min(100, copy.size());
            result.addAll(someResponses(copy.subList(0, count)));
            copy = copy.subList(count, copy.size());
        }
        return result;
    }

    private List<Event> someResponses(List<Request> requests) {
        EventQueue eventQueue = new EventQueue();
        try {
            for(Request request : requests)
                session.sendRequest(request, eventQueue, null);
            return responses(requests.size(), eventQueue);
        } catch (Exception e) {
            throw bomb("some request failed and now its time to add some debugging here.", e);
        }
    }

    private List<Event> responses(int numExpected, EventQueue eventQueue) throws InterruptedException {
        List<Event> result = empty();
        while (numExpected > 0) {
            Event nextEvent = eventQueue.nextEvent();
            if (verbose()) info("received: " + the(nextEvent));
            result.add(nextEvent);
            if(nextEvent.eventType().equals(EventType.RESPONSE)) numExpected--;
        }
        return result;
    }

    public UserHandle user() {
        return session.createUserHandle();
    }
    
    public static Element element(Element e, String key) {
        requireKey(e, key);
        return e.getElement(key);
    }
    
    public static boolean hasValue(Element e, String key) {
        return e.hasElement(key);
    }
    
    public static String string(Element e, String key) {
        requireKey(e, key);
        return e.getElementAsString(key);
    }
    
    public static long number(Element e, String key) {
        requireKey(e, key);
        return e.getElementAsInt64(key);
    }
    
    public static double numeric(Element e, String key) {
        requireKey(e, key);
        return e.getElementAsFloat64(key);
    }
    
    public static Map<String, String> values(Element e, String security) {
        Map<String, String> result = emptyMap();
        if (e.name().toString().equals("SubscriptionStarted")) return result;
        if (e.name().toString().equals("SubscriptionFailure")) {
            try {
                Element reason = element(e, "reason");
                Log.info("subscription failed for security " + security + " " + string(reason, "category") + " " + string(reason, "description"));
            } catch (RuntimeException e1) {
                Log.info("subscription mega failed for security " + security, e1);
            }
            return result;
        }
        ElementIterator i = e.elementIterator();
        while(i.hasNext()) {
            Element next = i.next();
            if(next.numValues() == 0) continue;
            if(next.numValues() > 1) throw bomb("numValues > 1: " + next);
            result.put(next.name().toString(), next.getValueAsString());
        }
        return result;
    }
    
    public static List<Element> elements(Element e, String key) {
        return elements(element(e, key));
    }
    
    public static List<Element> elements(Element e) {
        List<Element> result = empty();
        for (int i : zeroTo(e.numValues()))
            result.add(e.getValueAsElement(i));
        return result;
    }
    
    public static Date time(Element e, String key) {
        requireKey(e, key);
        Calendar calendar = e.getElementAsDatetime(key).calendar();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        return Dates.date(calendar);
    }
    
    public static Date date(Element e, String key) {
    	requireKey(e, key);
    	return Dates.date(e.getElementAsDatetime(key).toString());
    }

	private static void requireKey(Element e, String key) {
		bombUnless(e.hasElement(key), key + " not found in element " + e);
	}
    
    public static Date todayAt(Element e, String key) {
        return Dates.todayAt(hhMmSs(time(e, key)));
    }
    
    public static Datetime datetime(Date d) {
        Calendar c = calendar(d);
        Datetime result = new Datetime(
            c.get(Calendar.YEAR), 
            c.get(Calendar.MONTH) + 1, 
            c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            c.get(Calendar.SECOND),
            c.get(Calendar.MILLISECOND)
        );
        result.setTimezoneOffsetMinutes(TimeZone.getTimeZone("America/New_York").getOffset(d.getTime()) / 60000);
        return result;
    }
    
    public static Element element(Message m, String key) {
        bombUnless(m.hasElement(key), key + " not found in message " + m);
        return m.getElement(key);
    }
    

    public Authorization requestAuthorization(Request authRequest) {
        EventQueue queue = new EventQueue();
        try {
            session.sendAuthorizationRequest(authRequest, user(), queue, null);
            Event event = queue.nextEvent();
            return new Authorization(the(event));
        } catch (Exception e) {
            throw bomb("", e);
        }
    }

    public static Message the(Event event) {
        MessageIterator i = event.messageIterator();
        bombUnless(i.hasNext(), "odd - there is no message to get");
        Message message = i.next();
        bombIf(i.hasNext(), "odd - there is more than one message");
        return message;
    }

    public CorrelationID subscribe(final String security, String fields, final BloombergListener listener) {
        String correlationId = guid(false);
        SubscriptionList subscriptionList = new SubscriptionList();
        CorrelationID result = new CorrelationID(correlationId);
        subscriptionList.add(new Subscription(security, fields, result));
        info("subscribing " + correlationId + " to " + security + ": " + fields + ">" + listener);
        listeners.put(correlationId, listener);
        try {
            session.subscribe(subscriptionList);
        } catch (IOException e) {
            throw bomb("caught exception subscribing to " + security + ", " + fields, e);
        }
        return result;
    }

    public void unsubscribe(CorrelationID correlationId) {
        try {
            session.unsubscribe(correlationId);
        } catch(IOException e) {
            throw bomb("caught exception unsubscribing to id " + correlationId, e);
        }
    }

}
