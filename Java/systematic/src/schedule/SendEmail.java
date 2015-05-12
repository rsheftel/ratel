package schedule;

import static mail.Email.*;
import static schedule.JobStatus.*;
import static util.Strings.*;

import java.util.*;

import mail.*;
import schedule.JobTable.*;

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
