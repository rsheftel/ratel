package com.fftw.bloomberg.rtf.client;

import com.fftw.bloomberg.rtf.RealtimeApplication;
import com.fftw.bloomberg.rtf.RealtimeProtocolSession;
import com.fftw.bloomberg.rtf.RealtimeSessionConfig;
import com.fftw.bloomberg.rtf.RealtimeSessionFactory;
import com.fftw.bloomberg.rtf.messages.DefaultRtfMessage;
import com.fftw.bloomberg.rtf.messages.RtfHeader;
import com.fftw.bloomberg.rtf.messages.RtfMessage;
import com.fftw.bloomberg.rtf.types.RtfCommand;
import com.fftw.sbp.SocketInitiator;
import com.fftw.util.Emailer;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.calendar.WeeklyCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Properties;

/**
 * Client to connect with a Bloomberg Real-time Feed server
 */
public class RealTimeFeedClient {

    static final Logger logger = LoggerFactory.getLogger(RealTimeFeedClient.class);

    public static void main(String[] args) throws IOException, JMSException {
        // --- Basic flow ---
        // read in properties
        // create application with configuration
        // create message store
        // create log factory
        // create message factory
        // create initiator using previously created objects - this creates the session

        // Read in configuration
        // Create application
        // - read in batch file
        // - connect to topic so we can publish online messages
        // - publish the batch positions
        // - connect to Bloomberg to receive online position updates
        // - connect to command queue to listen for commands

//        Properties envProps = System.getProperties();
//        envProps.list(System.out);

        Properties props = new Properties();
        props.load(RealTimeFeedClient.class.getClassLoader().getResourceAsStream("realtimesession.properties"));

        LocalDate tradingDay = new LocalDate();
        LocalTime reloadTime = getReloadTime(props);

        if (args != null && args.length == 1) {
            tradingDay = DateTimeFormat.forPattern("yyyyMMdd").parseDateTime(args[0]).toLocalDate();
        }

        DateTime reloadDateTime = combineDateTime(tradingDay, reloadTime);

        if (reloadDateTime.isBeforeNow()) {
            // We are after the reload time, adjust
            tradingDay = tradingDay.plusDays(1);
        }

        RealtimeApplication app = new RealtimeApplication(props.getProperty("jms.brokerurl"));


        String batchPositionFilename = props.getProperty("positionFile.name");
        if (batchPositionFilename == null) {
            throw new IllegalArgumentException("Must specify a position.file");
        }

        Emailer mailer = new Emailer(props);
        app.setEmailer(mailer);
        app.setBatchFilename(batchPositionFilename);

        // initialize
        app.setCurrentTradingDay(tradingDay);
        app.setPreviousTradingDay(tradingDay, true);

        // start MBeans
        initializeMBeans(app);
        
        app.loadBatchFile();
        app.startPositionPublisher();

        app.publishBatchPositions();
        RealtimeProtocolSession realtimeSession = RealtimeSessionFactory.getInstance().createSession(app, props, new RealTimeFeedClientHandler());

        RealtimeSessionConfig sessionConfig = new RealtimeSessionConfig(props);
        final SocketInitiator initiator = new SocketInitiator(app, realtimeSession, sessionConfig, "RealtimeFeedTimer");
        initiator.start();

        app.startCommandListener(props.getProperty("jms.commandQueueName"));

        // set the schedular to pick up the new batch file after it is downloaded at 6:10 PM
        // There are issues with DST change that not all servers have the patch so in March
        // the file may be downloaded after we read it.
        configureBatchReload(app, reloadTime);

        // If we are here, we have been restarted, request all of the data for today
        // Wait 10 seconds to read any messages that may have been queued
        sleep(10000);
//        RtfMessage overRideRequest = new DefaultRtfMessage(new RtfHeader(RtfCommand.Override, new LocalDate(2008, 3, 11), 1), null);
        RtfMessage overRideRequest = new DefaultRtfMessage(new RtfHeader(RtfCommand.Override, tradingDay, 0), null);
        app.toApp(realtimeSession, overRideRequest);

        // Add the hook from the service wrapper to shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Stopping initiator");
                initiator.stop();
            }
        });
        // Loop until we need to exit
        runApplication();
    }

    private static void runApplication() {
        while (true) {
            sleep(30000);
        }
    }

    private static void initializeMBeans( RealtimeApplication app) {

        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("malbec:type=RealTimeApplication");

            mbs.registerMBean(app, name);
        } catch (Exception e) {
            logger.error("Unable to register MBean", e);

        }

    }

    private static LocalTime getReloadTime(Properties props) {
        String reloadTimeStr = props.getProperty("positionFile.reloadTime");
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
        LocalTime reloadTime = fmt.parseDateTime(reloadTimeStr).toLocalTime();

        return reloadTime;
    }

    /**
     * Setup a job trigger so that we reload the data from bloomberg.
     *
     * @param rtApp
     * @param reloadTime
     */
    private static void configureBatchReload(RealtimeApplication rtApp, LocalTime reloadTime) {

        try {

            // create a scheduler with only 1 thread and no persistence
            DirectSchedulerFactory.getInstance().createVolatileScheduler(1);

            Scheduler sched = DirectSchedulerFactory.getInstance().getScheduler();

            WeeklyCalendar weeklyCalendar = new WeeklyCalendar();
            sched.addCalendar("weekdays", weeklyCalendar, true, true);

            sched.start();

            JobDetail jobDetail = new JobDetail("myJob", null, ReloadBatchFileJob.class);
            jobDetail.getJobDataMap().put("application", rtApp);

            Trigger trigger = TriggerUtils.makeDailyTrigger(reloadTime.hourOfDay().get(), reloadTime.minuteOfHour().get());
            trigger.setCalendarName("weekdays");
            trigger.setName("ReloadBatchPositionTrigger");

            logger.info("Batch file reload will scheduled for: " + trigger.getFireTimeAfter(new Date()));

            sched.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            logger.error("Unable to start scheduler. ", e);
            throw new IllegalArgumentException("Schduler failed to start.", e);
        }

    }


    private static void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
        }
    }

    private static DateTime combineDateTime(LocalDate day, LocalTime time) {
        DateTime combined = new DateTime(day.getYear(), day.getMonthOfYear(), day.getDayOfMonth(), time.getHourOfDay(),
                time.getMinuteOfHour(), time.getSecondOfMinute(), time.getMillisOfSecond());

        return combined;
    }

    public static class ReloadBatchFileJob implements Job {


        public ReloadBatchFileJob() {

        }

        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

            try {
                // we must get the RealtimeApplication from the context
                JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
                RealtimeApplication application = (RealtimeApplication) jobDataMap.get("application");
                application.fromReloadTimer();
            } catch (Throwable t) {
                // This must catch everything, only JobExecutionException should be
                // thrown from here.
                logger.error("Error during execution of batch position reload.", t);
                throw new JobExecutionException(t);
            }
        }
    }
}
