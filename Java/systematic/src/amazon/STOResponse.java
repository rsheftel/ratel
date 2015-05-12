package amazon;

import static util.Dates.date;

import java.io.Serializable;
import java.util.Date;

import systemdb.data.Fields;
import util.Dates;

public class STOResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	final Fields fields;

    //public STOResponse(Date start, Date end, RequestStatus status) {
	public STOResponse(int runNumber, String instanceId, Date start, Date completionTime, int processId) {
        fields = new Fields();
        fields.put("RunNumber", runNumber);
        fields.put("InstanceId", instanceId);
        fields.put("StartTime", Dates.yyyyMmDdHhMmSsMmm(start));
        fields.put("CompletionTime", Dates.yyyyMmDdHhMmSsMmm(completionTime));
        fields.put("ProcessId", String.valueOf(processId));
    }

    public int runNumber() {
        return (int) fields.longg("RunNumber");
    }

    public enum RequestStatus {
        SUCCESS, FAILED
    }

	public String instanceId() {
		return fields.get("InstanceId");
	}

	public Date completedAt() {
		return fields.time("CompletionTime");
	}

	public String processId() {
		return fields.get("ProcessId");
	}

	public long runTimeMillis() {
		return date(fields.get("CompletionTime")).getTime() - date(fields.get("StartTime")).getTime();
	}


}
