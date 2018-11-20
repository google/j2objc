/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007-2010, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

// Copyright 2006 Google Inc.  All Rights Reserved.

package android.icu.dev.test.duration;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.duration.impl.XMLRecordReader;
import android.icu.impl.duration.impl.XMLRecordWriter;

public class DataReadWriteTest extends TestFmwk {
    // strip line ends and trailing spaces
    private String normalize(String str) {
        StringBuffer sb = new StringBuffer();
        boolean inLine = true;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (inLine && c == ' ') {
                continue;
            }
            if (c == '\n') {
                inLine = true;
                continue;
            }
            inLine = false;
            sb.append("" + c);
        }
        return sb.toString();
    }

    @Test
    public void testOpenClose() {
        StringWriter sw = new StringWriter();
        XMLRecordWriter xrw = new XMLRecordWriter(sw);
        assertTrue(null, xrw.open("Test"));
        assertTrue(null, xrw.close());
        xrw.flush();
        String str = sw.toString();
        assertEquals(null, "<Test></Test>", normalize(str));

        StringReader sr = new StringReader(str);
        XMLRecordReader xrr = new XMLRecordReader(sr);
        assertTrue(null, xrr.open("Test"));
        assertTrue(null, xrr.close());
    }

    @Test
    public void testBool() {
        StringWriter sw = new StringWriter();
        XMLRecordWriter xrw = new XMLRecordWriter(sw);
        xrw.bool("x", true);
        xrw.bool("y", false);
        xrw.flush();
        String str = sw.toString();
        assertEquals(null, "<x>true</x><y>false</y>", normalize(str));

        StringReader sr = new StringReader(str);
        XMLRecordReader xrr = new XMLRecordReader(sr);
        assertTrue(null, xrr.bool("x"));
        assertFalse(null, xrr.bool("y"));
    }

    @Test
    public void testBoolArray() {
        boolean[][] datas = {
            {},
            { true },
            { true, false },
            { true, false, true },
        };
    
        String[] targets = {
            "<testList></testList>",
            "<testList><test>true</test></testList>",
            "<testList><test>true</test><test>false</test></testList>",
            "<testList><test>true</test><test>false</test>" +
            "<test>true</test></testList>",
        };

        for (int j = 0; j < datas.length; ++j) {
            boolean[] data = datas[j];
            String target = targets[j];

            StringWriter sw = new StringWriter();
            XMLRecordWriter xrw = new XMLRecordWriter(sw);
            xrw.boolArray("test", data);
            xrw.flush();
            String str = sw.toString();
            assertEquals("" + j, target, normalize(str));

            StringReader sr = new StringReader(str);
            XMLRecordReader xrr = new XMLRecordReader(sr);
            boolean[] out = xrr.boolArray("test");

            assertNotNull("" + j, out);
            assertEquals("" + j, data.length, out.length);
            for (int i = 0; i < data.length; ++i) {
                assertEquals("" + j + "/" + i, data[i], out[i]);
            }
        }
    }

    @Test
    public void testCharacter() {
        StringWriter sw = new StringWriter();
        XMLRecordWriter xrw = new XMLRecordWriter(sw);
        xrw.character("x", 'a');
        xrw.character("y", 'b');
        xrw.flush();
        String str = sw.toString();
        assertEquals(null, "<x>a</x><y>b</y>", normalize(str));

        StringReader sr = new StringReader(str);
        XMLRecordReader xrr = new XMLRecordReader(sr);
        assertEquals(null, 'a', xrr.character("x"));
        assertEquals(null, 'b', xrr.character("y"));
    }

    @Test
    public void testCharacterArray() {
        char[][] datas = {
            {},
            { 'a' },
            { 'a', 'b' },
            { 'a', 'b', 'c' },
        };

        String[] targets = {
            "<testList></testList>",
            "<testList><test>a</test></testList>",
            "<testList><test>a</test><test>b</test></testList>",
            "<testList><test>a</test><test>b</test>" +
            "<test>c</test></testList>",
        };

        for (int j = 0; j < datas.length; ++j) {
            char[] data = datas[j];
            String target = targets[j];

            StringWriter sw = new StringWriter();
            XMLRecordWriter xrw = new XMLRecordWriter(sw);
            xrw.characterArray("test", data);
            xrw.flush();
            String str = sw.toString();
            assertEquals("" + j, target, normalize(str));

            StringReader sr = new StringReader(str);
            XMLRecordReader xrr = new XMLRecordReader(sr);
            char[] out = xrr.characterArray("test");

            assertNotNull("" + j, out);
            assertEquals("" + j, data.length, out.length);
            for (int i = 0; i < data.length; ++i) {
                assertEquals("" + j + "/" + i, data[i], out[i]);
            }
        }
    }

    @Test
    public void testNamedIndex() {
        StringWriter sw = new StringWriter();
        XMLRecordWriter xrw = new XMLRecordWriter(sw);
        String[] names = { "zero", "one" };

        xrw.namedIndex("x", names, 0);
        xrw.namedIndex("y", names, 1);
        xrw.flush();
        String str = sw.toString();
        assertEquals(null, "<x>zero</x><y>one</y>", normalize(str));

        StringReader sr = new StringReader(str);
        XMLRecordReader xrr = new XMLRecordReader(sr);
        assertEquals(null, 0, xrr.namedIndex("x", names));
        assertEquals(null, 1, xrr.namedIndex("y", names));
    }

    @Test
    public void testNamedIndexArray() {
        String[] names = { "zero", "one" };
        byte[][] datas = {
            {},
            { 0 },
            { 1, 0 },
            { 0, 1, 0 },
        };

        String[] targets = {
            "<testList></testList>",
            "<testList><test>zero</test></testList>",
            "<testList><test>one</test><test>zero</test></testList>",
            "<testList><test>zero</test><test>one</test>" +
            "<test>zero</test></testList>",
        };

        for (int j = 0; j < datas.length; ++j) {
            byte[] data = datas[j];
            String target = targets[j];

            StringWriter sw = new StringWriter();
            XMLRecordWriter xrw = new XMLRecordWriter(sw);
            xrw.namedIndexArray("test", names, data);
            xrw.flush();
            String str = sw.toString();
            assertEquals("" + j, target, normalize(str));

            StringReader sr = new StringReader(str);
            XMLRecordReader xrr = new XMLRecordReader(sr);
            byte[] out = xrr.namedIndexArray("test", names);

            assertNotNull("" + j, out);
            assertEquals("" + j, data.length, out.length);
            for (int i = 0; i < data.length; ++i) {
                assertEquals("" + j + "/" + i, data[i], out[i]);
            }
        }
    }

    @Test
    public void testString() {
        StringWriter sw = new StringWriter();
        XMLRecordWriter xrw = new XMLRecordWriter(sw);

        String s = " This is <a> &&\t test. ";
        String s1 = " This is <a> && test. ";
        String t = " This is &lt;a> &amp;&amp; test. ";
        xrw.string("x", s);
        xrw.flush();
        String str = sw.toString();
        assertEquals("\n'" + normalize(str) + "' = \n'<x>" + t + "</x>", "<x>"
                + t + "</x>", normalize(str));

        StringReader sr = new StringReader(str);
        XMLRecordReader xrr = new XMLRecordReader(sr);
        String res = xrr.string("x");
        assertEquals("\n'" + res + "' == \n'" + s1 + "'", s1, res);
    }

    @Test
    public void testStringArray() {
        String s1 = "";
        String s2 = " ";
        String s3 = "This is a test";
        String s4 = "  It is\n   only  a test\t  ";
        String s4x = " It is only a test ";

        String[][] datas = { 
            {},
            { s1 },
            { s2, s1 },
            { s3, s2, s1 },
            { s3, null, s1, null },
            { s4, s1, s3, s2 }
        };

        String[] targets = {
            "<testList></testList>",
            "<testList><test>" + s1 + "</test></testList>",
            "<testList><test>" + s2 + "</test><test>" + s1 + "</test></testList>",
            "<testList><test>" + s3 + "</test><test>" + s2 + 
                "</test><test>" + s1 + "</test></testList>",
            "<testList><test>" + s3 + "</test><test>Null</test><test>" + s1 + 
                "</test><test>Null</test></testList>",
            "<testList><test>" + s4x + "</test><test>" + s1 + 
                "</test><test>" + s3 + "</test><test>" + s2 + "</test></testList>",
        };

        for (int j = 0; j < datas.length; ++j) {
            String[] data = datas[j];
            String target = targets[j];

            StringWriter sw = new StringWriter();
            XMLRecordWriter xrw = new XMLRecordWriter(sw);
            xrw.stringArray("test", data);
            xrw.flush();
            String str = sw.toString();
            assertEquals("" + j + " '" + str + "'", target, normalize(str));

            StringReader sr = new StringReader(str);
            XMLRecordReader xrr = new XMLRecordReader(sr);
            String[] out = xrr.stringArray("test");

            assertNotNull("" + j, out);
            assertEquals("" + j, data.length, out.length);
            for (int i = 0; i < data.length; ++i) {
                String standin = data[i];
                if (s4.equals(standin)) {
                    standin = s4x;
                }
                assertEquals("" + j + "/" + i + " '" + out[i] + "'", standin,
                        out[i]);
            }
        }
    }

    @Test
    public void testStringTable() {
        String s1 = "";
        String s2 = " ";
        String s3 = "This is a test";
        String s4 = "It is only a test";

        String[][] table = { 
            {},
            { s1 },
            { s2, s1 },
            { s3, s2, s1 },
            null,
            { s4, s1, s3, s2 }
        };

        String target = "<testTable>" +
            "<testList></testList>" +
            "<testList><test></test></testList>" +
            "<testList><test> </test><test></test></testList>" +
            "<testList><test>This is a test</test><test> </test>" + 
                "<test></test></testList>" +
            "<testList>Null</testList>" +
            "<testList><test>It is only a test</test><test></test>" +
                "<test>This is a test</test><test> </test></testList>" +
            "</testTable>";

        StringWriter sw = new StringWriter();
        XMLRecordWriter xrw = new XMLRecordWriter(sw);
        xrw.stringTable("test", table);
        xrw.flush();
        String str = sw.toString();
        assertEquals("'" + str + "'", target, normalize(str));
    }

    @Test
    public void testOmittedFields() {
        StringWriter sw = new StringWriter();
        XMLRecordWriter xrw = new XMLRecordWriter(sw);
        xrw.open("omit");
        xrw.bool("x", true);
        xrw.bool("y", false);
        xrw.close();
        xrw.flush();
        String str = sw.toString();

        StringReader sr = new StringReader(str);
        XMLRecordReader xrr = new XMLRecordReader(sr);
        assertTrue(null, xrr.open("omit"));
        assertTrue(null, xrr.bool("x"));
        assertEquals(null, '\uffff', xrr.character("z"));
        assertFalse(null, xrr.bool("y"));
        assertTrue(null, xrr.close());
    }
}
