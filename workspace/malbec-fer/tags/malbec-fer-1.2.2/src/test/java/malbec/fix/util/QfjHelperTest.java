package malbec.fix.util;

import static org.testng.Assert.*;

import java.util.Properties;

import malbec.fer.fix.AbstractFixTest;

import org.testng.annotations.Test;

import quickfix.FileStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

public class QfjHelperTest {

	@Test(groups = { "unittest" })
	public void testQfjSettingsCreate() throws Exception {
		Properties config = AbstractFixTest.createInitiatorSession();
		SessionID sessionId = QfjHelper.createSessionId(config);
		
		SessionSettings settings = QfjHelper.createSessionSettings(config);
		boolean pathExists = settings.isSetting(sessionId, FileStoreFactory.SETTING_FILE_STORE_PATH);
		assertFalse(pathExists, "Path set when should be null");
		
		config.setProperty(FileStoreFactory.SETTING_FILE_STORE_PATH, "C:\\temp");
		SessionSettings fileSettings = QfjHelper.createSessionSettings(config);
		String path = fileSettings.getString(sessionId, FileStoreFactory.SETTING_FILE_STORE_PATH);
		assertTrue (path.contains(config.getProperty("TargetCompID")), "Failed to add target comp to directory");
	}
	
	@Test(groups = { "unittest" })
	public void testSessionIdCreate() {
		Properties config = AbstractFixTest.createInitiatorSession();
		SessionID sessionId = QfjHelper.createSessionId(config);
		
		assertEquals(sessionId.getBeginString(), "FIX.4.4", "Failed to assign begin string");
		assertEquals(sessionId.getTargetCompID(), "UNIT_TEST_SERVER", "Failed to assign TargetCompID");
		assertEquals(sessionId.getSenderCompID(), "UNIT_TEST_CLIENT", "Failed to assign SenderCompID");
	}
}
