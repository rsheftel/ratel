package systemdb.live;

import java.util.*;

import jms.*;
import systemdb.data.*;
import systemdb.metadata.*;
import util.*;
import static util.Arguments.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;
import static util.Times.*;

public class TomahawkControl {

    public static void main(String[] in) {
        Arguments args = arguments(in, list("command", "hostname"));
        String command = args.string("command").toUpperCase();
        String hostname = args.get("hostname", "N/A").toUpperCase();
        QTopic topic = new QTopic("TOMAHAWK." + command, false);
        doNotDebugSqlForever();
        Set<String> uniqueSystems = emptySet();
        for(LiveSystem system : MsivLiveHistory.LIVE.systems()) uniqueSystems.add(system.systemName());
        for(String system : uniqueSystems) {
            info("sending to " + commaSep(system, command));
            Fields fields = new Fields();
            fields.put("System", system);
            fields.put("Hostname", hostname);
            topic.send(fields);
            if(command.equals("RESTART")) sleepSeconds(5);
        }
        Channel.closeResources();
    }
    
}
