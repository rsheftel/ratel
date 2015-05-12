package mail;

import static mail.EmailAliasesTable.*;
import static mail.Email.*;
import static mail.EmailAddress.*;
import static util.Objects.*;

import java.io.*;
import java.util.*;

import javax.mail.internet.*;

import mail.MockEmailer.*;
import db.*;
import file.*;

public class TestEmail extends DbTestCase {
	private QFile fooCsv = new QFile("foo.csv");

	public void functestSimpleEmail() throws Exception {
		Email message = problem("I am a subject", "I am the message");
		EmailAddress address = new EmailAddress("jeff");
		message.sendTo(address);
	}
	
	public void functestEmailWithAttachments() throws Exception {
		Email message = problem("I am a subject", "I am the message\n\n");
		EmailAddress address = new EmailAddress("jeff.bay@malbecpartners.com");
		fooCsv.create("carry on my wayward son.\nthere'll be a piece when you are done.\n");
		message.attach(fooCsv);
		message.sendTo(address);
	}
	
	public void functestWeird() throws Exception {
	    emailer.reset();
	    Email email = problem("test subject", "test message");
	    email.attach(new QFile("C:/SVN/R/lib/GSFCore/testdata/time_series_defs.csv"));
	    email.sendTo(US);
    
    }
	
	public void testEmailIsSent() throws Exception {
		emailer.allowMessages();
		functestSimpleEmail();
		emailer.requireSent(1);
		Sent first = emailer.first();
		assertMatches("PROBLEM: I am a subject", first.subject);
		assertEquals("I am the message", first.message);
		assertEquals("jeff.bay@malbecpartners.com", first(first.sentTo));
	}
	
	public void testAliases() throws Exception {
	    emailer.allowMessages();
	    Email email = problem("test", "test");
        email.sendTo("eric");
	    emailer.sent().hasReceiver("eric.sux@malbecpartners.com");
	    emailer.clear();
	    email.sendTo("team,us");
	    HashSet<String> names = new HashSet<String>(emailer.sent().sentTo);
	    Set<String> expected = set(
	        "sim_team@malbecpartners.gmail.com",
	        "jeff.bay@malbecpartners.com",
	        "eric.sux@malbecpartners.com"
	    );
	    assertEquals(expected, names);
	    		
    }

	@Override protected void setUp() throws Exception {
	    super.setUp();
	    EMAILS.clear();
        EMAILS.insert("team", "sim_team@malbecpartners.gmail.com");
        EMAILS.insert("us", "jeff.bay@malbecpartners.com,eric.sux@malbecpartners.com");
        EMAILS.insert("eric", "eric.sux@malbecpartners.com");
        EMAILS.insert("jeff", "jeff.bay@malbecpartners.com");
        EMAILS.insert("nobody", "nobody@foo.com");
	}
	
	@Override protected void tearDown() throws Exception {
		fooCsv.deleteIfExists();
		super.tearDown();
	}
	
	public void testEmailWithAttachments() throws Exception {
		emailer.allowMessages();

		Email message = problem("I am a subject", "I am the message");
		EmailAddress address = new EmailAddress("jeff.bay@malbecpartners.com");
		fooCsv.create("i am an \nattachment\n");
		message.attach(fooCsv);
		message.sendTo(address);

		MimeMessage msg = emailer.sentMime(); 
		MimeMultipart content = (MimeMultipart) msg.getContent();
		assertEquals(2, content.getCount());
		assertMatches("I am a subject", msg.getSubject());
		assertEquals("I am the message", content.getBodyPart(0).getContent());
		InputStream inStream = (InputStream) content.getBodyPart(1).getContent();
		assertEquals("i am an \nattachment\n", QFile.text(new InputStreamReader(inStream)));
		assertEquals("jeff.bay@malbecpartners.com", first(msg.getAllRecipients()).toString());
	}
}
