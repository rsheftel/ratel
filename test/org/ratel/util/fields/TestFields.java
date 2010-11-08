package org.ratel.util.fields;

import static org.ratel.util.Dates.*;
import org.ratel.systemdb.data.*;
import org.ratel.util.*;

public class TestFields extends Asserts {

    DateFieldName TIMESTAMP = FieldName.dateName("QTimestamp");
    DateFieldName TIMESTAMP_DEFAULT = FieldName.dateName("QTimestampD", date("1970/01/01"));
    IntFieldName MILLIS = FieldName.intName("Qms");
    IntFieldName MILLIS_DEFAULT = FieldName.intName("QmsD", 0);
    StringFieldName WORLD = FieldName.stringName("qwhirld");
    StringFieldName HELLO_DEFAULT = FieldName.stringName("qwhirldefault", "hello!");
    LongFieldName LOOONG = FieldName.longName("QmsL");
    LongFieldName LOOONG_DEFAULT = FieldName.longName("QmsDL", 0L);
    DoubleFieldName DOUBLE = FieldName.doubleName("QmsDouble");
    DoubleFieldName DOUBLE_DEFAULT = FieldName.doubleName("QmsDoubleDefault", -97.3);
    
    private NewFields fields = new NewFields();

    public void testFieldsParse() throws Exception {
        Fields fields2 = Fields.parse("foo=");
        assertEquals("", fields2.get("foo"));
    }
    
    private <T> void assertWriteRead(FieldName<T> name, T into, T expectedOut) {
        name.putInto(fields, into);
        assertEquals(expectedOut, name.from(fields));
    }
    
    public void testNewFields() throws Exception {
        assertEmpty(fields);
        freezeNow("2009/09/09 03:00:00");
        
        assertReadFails(TIMESTAMP, "F-QTimestamp not found in");
        assertWriteRead(TIMESTAMP, now(), now());
        assertWriteRead(TIMESTAMP, date("2008/07/17 23:59:59"), date("2008/07/17 23:59:59"));
        assertPutFails(TIMESTAMP, null, "F-QTimestamp does not allow null");

        assertValue(TIMESTAMP_DEFAULT, date("1970/01/01"));
        assertWriteRead(TIMESTAMP_DEFAULT, now(), now());
        assertWriteRead(TIMESTAMP_DEFAULT, date("2008/07/17 23:59:59"), date("2008/07/17 23:59:59"));
        assertWriteRead(TIMESTAMP_DEFAULT, null, date("1970/01/01"));
        
        assertReadFails(MILLIS, "F-Qms not found in");
        assertWriteRead(MILLIS, 17, 17);

        assertValue(MILLIS_DEFAULT, 0);
        assertWriteRead(MILLIS_DEFAULT, null, 0);
        
        assertNoDefault(WORLD, "hello");
        assertPutFails(WORLD, "", "F-qwhirld does not allow null, and ");

        assertValue(HELLO_DEFAULT, "hello!");
        assertWriteRead(HELLO_DEFAULT, null, "hello!");
        assertWriteRead(HELLO_DEFAULT, "", "hello!");
        assertWriteRead(HELLO_DEFAULT, "goodbye", "goodbye");
        
        assertReadFails(LOOONG, "F-QmsL not found in");
        assertWriteRead(LOOONG, 17L, 17L);
        assertWriteRead(LOOONG, Long.MAX_VALUE, Long.MAX_VALUE);

        assertValue(LOOONG_DEFAULT, 0L);
        assertWriteRead(LOOONG_DEFAULT, null, 0L);
        
        assertReadFails(DOUBLE, "F-QmsDouble not found in");
        assertWriteRead(DOUBLE, 17.0, 17.0);
        assertWriteRead(DOUBLE, Double.MAX_VALUE, Double.MAX_VALUE);

        assertValue(DOUBLE_DEFAULT, -97.3);
        assertWriteRead(DOUBLE_DEFAULT, null, -97.3);
        
//        String message = "QmsL=88|qwhirld=world";
//        NewFields parsed = NewFields.parse(message);
//        assertEquals("world", WORLD.from(parsed));
//        assertMatches(17L, LOOONG.from(parsed));
        
        // test case for throwing a cast exception or format problem
        // isCorrectType question for the fields
        // how do we know the types? we have to be told about the names ahead of time
        // or fall back to StringFieldNames if we don't know about the name when we parse.
    }

    private <T> void assertNoDefault(FieldName<T> field, T expected) {
        assertReadFails(field, field + " not found in");
        assertPutFails(field, null, field + " does not allow null");
        assertWriteRead(field, expected, expected);
    }
    
    private <T> void assertValue(FieldName<T> name, T expected) {
        assertEquals(expected, name.from(fields));
    }

    private <T> void assertPutFails(FieldName<T> name, T value, String errorRegex) {
        try {
            name.putInto(fields, value);
            fail("put should have fauiled!");
        } catch (Exception e) {
            assertMatches(errorRegex, e);
        }
    }

    private <T> void assertReadFails(FieldName<T> name, String errorRegex) {
        try {
            name.from(fields);
            fail("no value here!");
        } catch (Exception e) {
            assertMatches(errorRegex, e);
        }

    }
    
    
    
}
