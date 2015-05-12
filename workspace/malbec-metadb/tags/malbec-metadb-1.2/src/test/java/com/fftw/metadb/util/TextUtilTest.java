package com.fftw.metadb.util;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Date;

/**
 * TextUtil Tester.
 */
public class TextUtilTest {

    private static final String STRING_MESSAGE = "key1=value1|key2=value2|key3=value3";
    private static final Map<String, String> MAP_MESSAGE = new LinkedHashMap<String, String>();

    @BeforeTest(groups = { "unittest" })
    public void setUp() {
        MAP_MESSAGE.put("key1", "value1");
        MAP_MESSAGE.put("key2", "value2");
        MAP_MESSAGE.put("key3", "value3");
    }

    @Test(groups = { "unittest" })
    public void testExtractRecord() {
        Map<String, String> result = MessageUtil.extractRecord(STRING_MESSAGE, "\\|",
                new LinkedHashMap<String, String>());

        assert MessageUtil.compareMaps(MAP_MESSAGE, result) : "Parsed string incorrectly";
    }

    @Test(groups = { "unittest" })
    public void testCreateRecord() {

        String recordString = MessageUtil.createRecord(MAP_MESSAGE, "|");

        assert STRING_MESSAGE.equals(recordString) : "Created record incorrectly";
    }

    @Test(groups = { "unittest" })
    @SuppressWarnings("deprecation")
    public void testFormatDate() {
        // This routine is bad, it adds 1900 to the year, so I must remove it
        Date testDate = new Date(108, 05, 20);
        String formattedDate = MessageUtil.formatDate(testDate);

        assert "2008/06/20 00:00:00".equals(formattedDate) : "Formatted date incorrectly";
    }

}
