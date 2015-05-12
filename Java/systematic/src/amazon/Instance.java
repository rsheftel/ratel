package amazon;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import util.*;

public class Instance {

    private static final List<String> DOWN = list("shutting-down", "terminated");
    private final String instanceId;

    public Instance(String instanceId) {
        this.instanceId = instanceId;
    }
    
    @Override public String toString() {
        return id();
    }

    public void shutdown() {
        Tag response = AmazonEC2Request.response("TerminateInstances", map(
            "InstanceId.1", instanceId
        ));
        String status = response.child("instancesSet").child("item").child("shutdownState").text("name");
        bombUnless(DOWN.contains(status), 
            "unexpected status code during shutdown: " + status + "\n" + response);
    }

	public String id() {
		return instanceId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instanceId == null) ? 0 : instanceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Instance other = (Instance) obj;
		if (instanceId == null) {
			if (other.instanceId != null)
				return false;
		} else if (!instanceId.equals(other.instanceId))
			return false;
		return true;
	}

	
	
    
}