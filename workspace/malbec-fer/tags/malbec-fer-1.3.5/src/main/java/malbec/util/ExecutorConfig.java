package malbec.util;

import java.util.concurrent.TimeUnit;

public class ExecutorConfig {

    private long initialDelay;
    private long period;
    private TimeUnit timeUnit;

    public ExecutorConfig(long initialDelay, long period, TimeUnit timeUnit) {
        super();
        this.initialDelay = initialDelay;
        this.period = period;
        this.timeUnit = timeUnit;
    }

    /**
     * @return the initialDelay
     */
    public long getInitialDelay() {
        return initialDelay;
    }

    /**
     * @param initialDelay
     *            the initialDelay to set
     */
    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    /**
     * @return the period
     */
    public long getPeriod() {
        return period;
    }

    /**
     * @param period
     *            the period to set
     */
    public void setPeriod(long period) {
        this.period = period;
    }

    /**
     * @return the timeUnit
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * @param timeUnit
     *            the timeUnit to set
     */
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("initialDelay=").append(initialDelay);
        sb.append(", period=").append(period);
        sb.append(", timeUnit=").append(timeUnit);

        return sb.toString();
    }

    public ExecutorConfig asLargestTimeUnit() {
        long tmpInitial = initialDelay;
        long tmpPeriod = period;
        switch (timeUnit) {
            case MILLISECONDS:
                tmpInitial = initialDelay / 1000;
                tmpPeriod = period / 1000;
                if (tmpInitial * 1000 == initialDelay && tmpPeriod * 1000 == period) {
                    return new ExecutorConfig(tmpInitial, tmpPeriod, TimeUnit.SECONDS).asLargestTimeUnit();
                }
                break;
            case SECONDS:
                tmpInitial = initialDelay / 60;
                tmpPeriod = period / 60;
                if (tmpInitial * 60 == initialDelay && tmpPeriod * 60 == period) {
                    return new ExecutorConfig(tmpInitial, tmpPeriod, TimeUnit.MINUTES).asLargestTimeUnit();
                }
                break;
            case MINUTES:
                tmpInitial = initialDelay / 60;
                tmpPeriod = period / 60;
                if (tmpInitial * 60 == initialDelay && tmpPeriod * 60 == period) {
                    return new ExecutorConfig(tmpInitial, tmpPeriod, TimeUnit.HOURS).asLargestTimeUnit();
                }
                break;
        }

        return new ExecutorConfig(initialDelay, period, timeUnit);
    }

    public ExecutorConfig inTimeUnit(TimeUnit timeUnit) {
        long tmpInitial = initialDelay;
        long tmpPeriod = period;

        switch (this.timeUnit) {
            case SECONDS:
                tmpInitial = convertSecondsToMillis(tmpInitial);
                tmpPeriod = convertSecondsToMillis(tmpPeriod);
                break;
            case MINUTES:
                tmpInitial = convertMinutesToMillis(tmpInitial);
                tmpPeriod = convertMinutesToMillis(tmpPeriod);
                break;

            case HOURS:
                tmpInitial = convertHoursToMillis(tmpInitial);
                tmpPeriod = convertHoursToMillis(tmpPeriod);
                break;
            case MILLISECONDS: // nothing to do
                break;
            default:
                throw new IllegalStateException("Cannot convert from " + this.timeUnit);
        }
        
        // we should be in milliseconds now
        switch (timeUnit) {
            case MILLISECONDS:
                return new ExecutorConfig(tmpInitial, tmpPeriod, TimeUnit.MILLISECONDS);
            case SECONDS:
                tmpInitial = convertMillisToSeconds(initialDelay);
                tmpPeriod = convertMillisToSeconds(period);
                return new ExecutorConfig(tmpInitial, tmpPeriod, TimeUnit.SECONDS);
            case MINUTES:
                tmpInitial = convertMillisToMinutes(initialDelay);
                tmpPeriod = convertMillisToMinutes(period);
                return new ExecutorConfig(tmpInitial, tmpPeriod, TimeUnit.MINUTES);
            case HOURS:
                tmpInitial = convertMillisToHours(initialDelay);
                tmpPeriod = convertMillisToHours(period);
                return new ExecutorConfig(tmpInitial, tmpPeriod, TimeUnit.MINUTES);
        }
        return new ExecutorConfig(initialDelay, period, timeUnit);
    }

    private static long convertMillisToSeconds(long mills) {
        return mills / 1000;
    }

    private static long convertMillisToMinutes(long mills) {
        return convertMillisToSeconds(mills) / 60;
    }

    private static long convertMillisToHours(long mills) {
        return convertMillisToMinutes(mills) / 60;
    }

    private static long convertSecondsToMillis(long seconds) {
        return seconds * 1000;
    }

    private static long convertMinutesToMillis(long minutes) {
        return convertSecondsToMillis(minutes * 60);
    }

    private static long convertHoursToMillis(long hours) {
        return convertMinutesToMillis(hours * 24);
    }

}
