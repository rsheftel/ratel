package activemq.jmx;

public class ThreadCountInfo {

    public int peakThreadCount;
    public int threadCount;

    public String toString() {
        return "peakThread=" + peakThreadCount + ", currentThreads=" + threadCount;
    }
}
