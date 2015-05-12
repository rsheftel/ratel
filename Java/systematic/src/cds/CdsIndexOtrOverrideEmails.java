package cds;

import static com.malbec.tsdb.markit.CdsIndexOtrOverride.*;
import static mail.Email.*;
import static schedule.JobStatus.*;
import static util.Dates.*;

import java.util.*;

import mail.*;
import schedule.*;
import schedule.JobTable.*;

import com.malbec.tsdb.markit.CdsIndexOtrOverride.*;

public class CdsIndexOtrOverrideEmails implements Schedulable {

    @Override public JobStatus run(Date asOf, Job job) {
        doEmails(asOf, job.recipients());
        return SUCCESS;
    }

    public void doEmails(Date asOf, String recipients) {
        Email email = notification("OTR OVERRIDES in effect for 7 days after " + ymdHuman(asOf), "");
        for(OverrideRow override : OVERRIDE.overriddenNear(asOf)) 
            email.append(override.shortString() + "\n");
        if (email.hasContent())
            email.sendTo(recipients);
    }

}
