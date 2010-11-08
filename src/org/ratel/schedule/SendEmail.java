package org.ratel.schedule;

import static org.ratel.mail.Email.*;
import static org.ratel.schedule.JobStatus.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.mail.*;
import org.ratel.schedule.JobTable.*;

public class SendEmail implements Schedulable {


    @Override public JobStatus run(Date asOf, Job item) {
        Email email = notification(
            item.parameter("subject") + ", " + paren(item.name()), 
            ""
        );
        item.send(email);
        return SUCCESS;
    }
}
