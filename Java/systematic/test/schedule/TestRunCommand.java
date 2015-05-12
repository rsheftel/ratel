package schedule;

import static schedule.JobStatus.*;
import static schedule.JobTable.*;
import static transformations.Constants.*;
import static util.Dates.*;
import static util.Objects.*;
import schedule.JobTable.*;
import util.*;
import file.*;

public class TestRunCommand extends AbstractJobTest {
    public void testRunCommand() throws Exception {
        assertEquals(SUCCESS, runCommand("echo foo").status(now()));
        emailer.allowMessages();
        assertEquals(FAILED, runCommand("eco foo").status(now()));
        assertEquals(FAILED, runCommand("test/schedule/fail.bat").status(now()));
        emailer.requireSent(2);
    }

    @Override public void tearDown() throws Exception {
        super.tearDown();
        Log.setToSystem();
        output.deleteIfExists();
    }
    
    QFile output = new QFile("output");
    
    @Override public void setUp() throws Exception {
        super.setUp();
        output.deleteIfExists();
    }
    
    public void testLogFileIncludesRunCommandAndOutput() throws Exception {
        Log.setFile(output);
        freezeNow("2008/03/08 14:56:43");
        assertTrue(runCommand("echo hellow world [[DATE]]").status(now()).isSuccess());
        String logged = output.text();
        assertMatches("echo hellow world \\[\\[DATE\\]\\]: hellow world 2008/03/08", logged);
        assertMatches("running command: echo hellow world 2008/03/08", logged);
    }
    

    public void testRunCommandHandlesQuotingProperlyOnWindows() throws Exception {
        if (!isWindows()) return;
        runCommand("echo \"foo ' ' bar\"");
    }

    public void testRunWithQuotesOnUnix() throws Exception {
        if (isWindows()) return;
        assertEquals(SUCCESS, runCommand("(cd ~; ls -a | grep .bash)").status(now()));
    }

    private Job runCommand(String command) {
        Job job = JOBS.insert(command, new RunCommand(), THREE_PM);
        job.setParameters(map("command", command));
        job.run(now());
        return job;
    }

}
