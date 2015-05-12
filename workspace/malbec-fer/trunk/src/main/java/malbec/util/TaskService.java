package malbec.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
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

    /**
     * Create an instance of a <code>ScheduledExecutorService</code> that has only 1 thread of execution.
     * 
     * The thread name and key are set to the name provided.
     * 
     * @param executorName
     */
    public void createAndAddSingleThreadScheduled(String executorName) {
        createAndAddSingleThreadScheduled(executorName, executorName);
    }

    /**
     * Create an instance of a <code>ScheduledExecutorService</code> that has only 1 thread of execution.
     * 
     * The thread name and key are set to the names provided.
     * 
     * @param executorName
     * @param threadName
     */
    public void createAndAddSingleThreadScheduled(String executorName, String threadName) {
        addExecutor(executorName, Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
            threadName)));
    }

}
