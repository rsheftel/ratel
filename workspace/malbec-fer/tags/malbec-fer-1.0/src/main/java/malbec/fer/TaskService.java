package malbec.fer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class TaskService {

    private static TaskService instance = new TaskService();
    
    private Map<String, ScheduledExecutorService> executors = new HashMap<String, ScheduledExecutorService>();
    
    
    private TaskService() {
        
    }
    
    public static TaskService getInstance() {
        return instance;
    }
    
    public void addExecutor(String name, ScheduledExecutorService executor) {
        executors.put(name, executor);
    }
    
    public ScheduledExecutorService getExecutor(String name) {
        return executors.get(name);
    }
}
