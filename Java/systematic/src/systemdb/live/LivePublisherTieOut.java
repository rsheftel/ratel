package systemdb.live;

import static util.Objects.*;
import static util.Times.*;
import systemdb.data.*;
import util.*;
import jms.*;

public class LivePublisherTieOut {
    public static void main(String[] args) {
        Arguments arguments = Arguments.arguments(args, list("topic"));
        String topic = arguments.string("topic");
        QTopic spreadsheet = new QTopic(topic);
        QTopic server = new QTopic("SAPI_TEST." + topic);
        spreadsheet.register(new FieldsReceiver() {
            @Override public void onMessage(Fields fields) {
                Log.info("Excel: " + fields.text("LastPrice") + ", " + fields.text("LastVolume"));
            }
        });
        server.register(new FieldsReceiver() {
            @Override public void onMessage(Fields fields) {
                Log.info(" SAPI: " + fields.text("LastPrice") + ", " + fields.text("LastVolume"));
            }
        });
        sleepSeconds(Integer.MAX_VALUE);
    }
}
