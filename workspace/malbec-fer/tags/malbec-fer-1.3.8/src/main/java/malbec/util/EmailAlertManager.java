package malbec.util;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

public class EmailAlertManager {

    private EmailSettings emailSettings;

    private long defaultInterval = 1000 * 60 * 30; // 30 Minutes

    private Map<String, DateTime> eventTimeMap = new HashMap<String, DateTime>();
    private Map<String, Long> eventIntervalMap = new HashMap<String, Long>();

    public EmailAlertManager(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
    }

    public DateTime send(String event, String subject, String body) {
        return send(event, emailSettings.getErrorToList(), subject, body);
    }
    
    public DateTime send(String event, String toList, String subject, String body) {
        String eventKey = event.toUpperCase();

        synchronized (eventTimeMap) {
            DateTime lastEventTime = lastEventTime(eventKey);
            DateTime now = new DateTime();

            if (shouldSendEmail(eventKey, lastEventTime, now.getMillis())) {
                EmailSettings settings = new EmailSettings(emailSettings.getAsProperties());
                settings.setErrorToAddress(toList);

                EmailSender es = new EmailSender(settings);
                es.sendMessage(subject, body);

                lastEventTime = now;
                updateEventTime(eventKey, lastEventTime);
            }

            return lastEventTime;
        }
    }

    private DateTime lastEventTime(String eventKey) {
        return eventTimeMap.get(eventKey);
    }

    private void updateEventTime(String eventKey, DateTime now) {
        eventTimeMap.put(eventKey, now);
    }

    private boolean shouldSendEmail(String eventKey, DateTime lastEventTime, long currentMillis) {
        if (lastEventTime != null) {
            long interval = getEventInterval(eventKey);
            DateTime nextEmailtime = lastEventTime.plusMillis((int) interval);
            return (nextEmailtime.getMillis() <= currentMillis);
        }

        return true;
    }

    public long getEventInterval(String event) {
        String eventKey = event.toUpperCase();
        synchronized (eventIntervalMap) {
            Long interval = eventIntervalMap.get(eventKey);
            if (interval != null) {
                return interval;
            } else {
                return defaultInterval;
            }
        }
    }

    public void setDefaultInterval(int interval) {
        defaultInterval = interval;
    }

    public long getDefaultInterval() {
        return defaultInterval;
    }

    public void setEventInterval(String event, long interval) {
        String eventKey = event.toUpperCase();
        
        synchronized (eventIntervalMap) {
            eventIntervalMap.put(eventKey, interval);
        }
    }

}
