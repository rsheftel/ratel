package transformations;
import static util.Dates.*;
import static util.Times.*;

public class ServerHeartBeat extends Thread {

	private final int seconds;
	private final String record;

	public ServerHeartBeat(int seconds, String record) {
		this.seconds = seconds;
		this.record = record;
	}

	public void runOnce() {
		SeriesDefinition d = new SeriesDefinition("CONTROL", record, "TimeStamp");
		LiveTransformation.publisher().publish(d, yyyyMmDdHhMmSs(now()));
	}
	
	@Override public void run() {
		while(true) {
			runOnce();
			sleepSeconds(seconds);
		}
	}

}
