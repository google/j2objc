/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
* Copyright (c) 2002-2010, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Mark Davis
**********************************************************************
*/
package android.icu.dev.test.cldr;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.icu.dev.test.TestFmwk;
import android.icu.text.DateFormat;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.Currency;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;

/**
 * This is a test file that takes in the CLDR XML test files and test against
 * ICU4J. This test file is used to verify that ICU4J is implemented correctly.
 * As it stands, the test generates all the errors to the console by logging it.
 * The logging is only possible if "-v" or verbose is set as an argument.
 * This will allow users to know what problems occurred within CLDR and ICU.
 * Collator was disabled in this test file and therefore will be skipped.
 *
 * Instructions:
 * 1)   In order for this to work correctly, you must download the latest CLDR data
 *      in the form of XML. You must also set the CLDR directory using:
 *          -DCLDR_DIRECTORY=<top level of cldr>
 * 2)   You may also consider increasing the memory using -Xmx512m.
 * 3)   For speed purposes, you may consider creating a temporary directory for the
 *      CLDR cache using:
 *          -DCLDR_DTD_CACHE=<cldr cache directory>
 * 4)   You may use other environment variables to narrow down your tests using:
 *          -DXML_MATCH=".*"
 *              -DXML_MATCH="de.*"  (or whatever regex you want) to just test certain locales.
 *          -DTEST_MATCH="zone.*"   (or whatever regex you want) to just test collation, numbers, etc.
 *          -DZONE_MATCH="(?!America/Argentina).*"
 *              -DZONE_MATCH=".*Moscow.*" (to only test certain zones)

 * @author medavis
 * @author John Huan Vu (johnvu@us.ibm.com)
 */
public class TestCLDRVsICU extends TestFmwk {
    static final boolean DEBUG = false;

    // ULocale uLocale = ULocale.ENGLISH;
    // Locale oLocale = Locale.ENGLISH; // TODO Drop once ICU4J has ULocale everywhere
    // static PrintWriter log;
    SAXParser SAX;
    static Matcher LOCALE_MATCH, TEST_MATCH, ZONE_MATCH;
    static String CLDR_DIRECTORY;
    static {
        System.out.println();
        LOCALE_MATCH = getEnvironmentRegex("XML_MATCH", ".*");
        TEST_MATCH = getEnvironmentRegex("TEST_MATCH", ".*");
        ZONE_MATCH = getEnvironmentRegex("ZONE_MATCH", ".*");

        // CLDR_DIRECTORY is where all the CLDR XML test files are located
        // WARNING: THIS IS TEMPORARY DIRECTORY UNTIL THE FILES ARE STRAIGHTENED OUT
        CLDR_DIRECTORY = getEnvironmentString("CLDR_DIRECTORY", "C:\\Unicode-CVS2\\cldr\\");
        System.out.println();
    }

    private static Matcher getEnvironmentRegex(String key, String defaultValue) {
        return Pattern.compile(getEnvironmentString(key, defaultValue)).matcher("");
    }

    private static String getEnvironmentString(String key, String defaultValue) {
        String temp = System.getProperty(key);
        if (temp == null)
            temp = defaultValue;
        else
            System.out.print("-D" + key + "=\"" + temp + "\" ");
        return temp;
    }

    Set allLocales = new TreeSet();

    // TODO(user): seems to be failing with missing locales - maybe rewrite as parameterized
    @Ignore
    @Test
    public void TestFiles() throws SAXException, IOException {
        // only get ICU's locales
        Set s = new TreeSet();
        addLocales(NumberFormat.getAvailableULocales(), s);
        addLocales(DateFormat.getAvailableULocales(), s);

        // johnvu: Collator was originally disabled
        // addLocales(Collator.getAvailableULocales(), s);

        // filter, to make tracking down bugs easier
        for (Iterator it = s.iterator(); it.hasNext();) {
            String locale = (String) it.next();
            if (!LOCALE_MATCH.reset(locale).matches())
                continue;
            _test(locale);
        }
    }

    public void addLocales(ULocale[] list, Collection s) {
        for (int i = 0; i < list.length; ++i) {
            allLocales.add(list[i].toString());
            s.add(list[i].getLanguage());
        }
    }

    public String getLanguage(ULocale uLocale) {
        String result = uLocale.getLanguage();
        String script = uLocale.getScript();
        if (script.length() != 0)
            result += "_" + script;
        return result;
    }

    private void _test(String localeName) throws SAXException, IOException {
        // uLocale = new ULocale(localeName);
        // oLocale = uLocale.toLocale();

        File f = new File(CLDR_DIRECTORY, "test/" + localeName + ".xml");
        logln("Testing " + f.getCanonicalPath());
        SAX.parse(f, DEFAULT_HANDLER);
    }

    private static class ToHex {
        public String transliterate(String in) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < in.length(); ++i) {
                char c = in.charAt(i);
                sb.append("\\u");
                if (c < 1000) {
                    sb.append('0');
                    if (c < 100) {
                        sb.append('0');
                        if (c < 10) {
                            sb.append('0');
                        }
                    }
                }
                sb.append(Integer.toHexString((int) c));
            }
            return sb.toString();
        }
    }

    // static Transliterator toUnicode = Transliterator.getInstance("any-hex");
    private static final ToHex toUnicode = new ToHex();

    static public String showString(String in) {
        return "\u00AB" + in + "\u00BB (" + toUnicode.transliterate(in) + ")";
    }

    // ============ SAX Handler Infrastructure ============

    abstract public class Handler {
        Map settings = new TreeMap();
        String name;
        List currentLocales = new ArrayList();
        int failures = 0;

        void setName(String name) {
            this.name = name;
        }

        void set(String attributeName, String attributeValue) {
            // if (DEBUG) logln(attributeName + " => " + attributeValue);
            settings.put(attributeName, attributeValue);
        }

        void checkResult(String value) {
            if (settings.get("draft").equals("unconfirmed") || settings.get("draft").equals("provisional")) {
                return; // skip draft
            }
            ULocale ul = new ULocale("xx");
            try {
                for (int i = 0; i < currentLocales.size(); ++i) {
                    ul = (ULocale) currentLocales.get(i);
                    // loglnSAX("  Checking " + ul + "(" + ul.getDisplayName(ULocale.ENGLISH) + ")" + " for " + name);
                    handleResult(ul, value);
                    if (failures != 0) {
                        errln("\tTotal Failures: " + failures + "\t" + ul + "(" + ul.getDisplayName(ULocale.ENGLISH)
                                + ")");
                        failures = 0;
                    }
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                errln("Exception: Locale: " + ul + ",\tValue: <" + value + ">\r\n" + sw.toString());
            }
        }

        public void loglnSAX(String message) {
            String temp = message + "\t[" + name;
            for (Iterator it = settings.keySet().iterator(); it.hasNext();) {
                String attributeName = (String) it.next();
                String attributeValue = (String) settings.get(attributeName);
                temp += " " + attributeName + "=<" + attributeValue + ">";
            }
            logln(temp + "]");
        }

        int lookupValue(Object x, Object[] list) {
            for (int i = 0; i < list.length; ++i) {
                if (x.equals(list[i]))
                    return i;
            }
            loglnSAX("Unknown String: " + x);
            return -1;
        }

        abstract void handleResult(ULocale currentLocale, String value) throws Exception;

        /**
         * @param attributes
         */
        public void setAttributes(Attributes attributes) {
            String localeList = attributes.getValue("locales");
            String[] currentLocaleString = new String[50];
            android.icu.impl.Utility.split(localeList, ' ', currentLocaleString);
            currentLocales.clear();
            for (int i = 0; i < currentLocaleString.length; ++i) {
                if (currentLocaleString[i].length() == 0)
                    continue;
                if (allLocales.contains("")) {
                    logln("Skipping locale, not in ICU4J: " + currentLocaleString[i]);
                    continue;
                }
                currentLocales.add(new ULocale(currentLocaleString[i]));
            }
            if (DEBUG)
                logln("Setting locales: " + currentLocales);
        }
    }

    public Handler getHandler(String name, Attributes attributes) {
        if (DEBUG)
            logln("Creating Handler: " + name);
        Handler result = (Handler) RegisteredHandlers.get(name);
        if (result == null)
            logln("Unexpected test type: " + name);
        else {
            result.setAttributes(attributes);
        }
        return result;
    }

    public void addHandler(String name, Handler handler) {
        if (!TEST_MATCH.reset(name).matches())
            handler = new NullHandler();
        handler.setName(name);
        RegisteredHandlers.put(name, handler);
    }

    Map RegisteredHandlers = new HashMap();

    class NullHandler extends Handler {
        void handleResult(ULocale currentLocale, String value) throws Exception {
        }
    }

    // ============ Statics for Date/Number Support ============

    static TimeZone utc = TimeZone.getTimeZone("GMT");
    static DateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    {
        iso.setTimeZone(utc);
    }

    static int[] DateFormatValues = { -1, DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL };

    // The following are different data format types that are part of the parameters in CLDR
    static String[] DateFormatNames = { "none", "short", "medium", "long", "full" };

    // The following are different number types that are part of the parameters in CLDR
    static String[] NumberNames = { "standard", "integer", "decimal", "percent", "scientific", "GBP" };


    // ============ Handler for Collation ============
    static UnicodeSet controlsAndSpace = new UnicodeSet("[:cc:]");

    static String remove(String in, UnicodeSet toRemove) {
        int cp;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < in.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(in, i);
            if (!toRemove.contains(cp))
                UTF16.append(result, cp);
        }
        return result.toString();
    }

    {
        // johnvu: Collator was originally disabled
        // TODO (dougfelt) move this test
        /*
          addHandler("collation", new Handler() {
             public void handleResult(ULocale currentLocale, String value) {
                 Collator col = Collator.getInstance(currentLocale);
                 String lastLine = "";
                 int count = 0;
                 for (int pos = 0; pos < value.length();) {
                     int nextPos = value.indexOf('\n', pos);
                     if (nextPos < 0)
                         nextPos = value.length();
                     String line = value.substring(pos, nextPos);
                     line = remove(line, controlsAndSpace);  HACK for SAX
                     if (line.trim().length() != 0) {  HACK for SAX
                         int comp = col.compare(lastLine, line);
                         if (comp > 0) {
                             failures++;
                             errln("\tLine " + (count + 1) + "\tFailure: "
                                     + showString(lastLine) + " should be leq "
                                     + showString(line));
                         } else if (DEBUG) {
                             logln("OK: " + line);
                         }
                         lastLine = line;
                     }
                     pos = nextPos + 1;
                     count++;
                 }
             }
         });
        */

        // ============ Handler for Numbers ============
        addHandler("number", new Handler() {
            public void handleResult(ULocale locale, String result) {
                NumberFormat nf = null;
                double v = Double.NaN;
                for (Iterator it = settings.keySet().iterator(); it.hasNext();) {
                    String attributeName = (String) it.next();
                    String attributeValue = (String) settings.get(attributeName);

                    // Checks if the attribute name is a draft and whether
                    // or not it has been approved / contributed by CLDR yet
                    // otherwise, skips it because it is most likely rejected by ICU
                    if (attributeName.equals("draft")) {
                        if (attributeValue.indexOf("approved") == -1 && attributeValue.indexOf("contributed") == -1) {
                            break;
                        }
                        continue;
                    }

                    // Update the value to be checked
                    if (attributeName.equals("input")) {
                        v = Double.parseDouble(attributeValue);
                        continue;
                    }

                    // At this point, it must be a numberType
                    int index = lookupValue(attributeValue, NumberNames);

                    if (DEBUG)
                        logln("Getting number format for " + locale);
                    switch (index) {
                    case 0:
                        nf = NumberFormat.getInstance(locale);
                        break;
                    case 1:
                        nf = NumberFormat.getIntegerInstance(locale);
                        break;
                    case 2:
                        nf = NumberFormat.getNumberInstance(locale);
                        break;
                    case 3:
                        nf = NumberFormat.getPercentInstance(locale);
                        break;
                    case 4:
                        nf = NumberFormat.getScientificInstance(locale);
                        break;
                    default:
                        nf = NumberFormat.getCurrencyInstance(locale);
                        nf.setCurrency(Currency.getInstance(attributeValue));
                        break;
                    }
                    String temp = nf.format(v).trim();
                    result = result.trim(); // HACK because of SAX
                    if (!temp.equals(result)) {
                        logln("Number: Locale: " + locale +
                                "\n\tType: " + attributeValue +
                                "\n\tDraft: " + settings.get("draft") +
                                "\n\tCLDR: <" + result + ">" +
                                "\n\tICU: <" + temp + ">");
                    }

                }
            }
        });

        // ============ Handler for Dates ============
        addHandler("date", new Handler() {
            public void handleResult(ULocale locale, String result) throws ParseException {
                int dateFormat = 0;
                int timeFormat = 0;
                Date date = new Date();
                boolean approved = true;

                for (Iterator it = settings.keySet().iterator(); it.hasNext();) {
                    String attributeName = (String) it.next();
                    String attributeValue = (String) settings.get(attributeName);

                    // Checks if the attribute name is a draft and whether
                    // or not it has been approved / contributed by CLDR yet
                    // otherwise, skips it because it is most likely rejected by ICU
                    if (attributeName.equals("draft")) {
                        if (attributeValue.indexOf("approved") == -1 && attributeValue.indexOf("contributed") == -1) {
                            approved = false;
                            break;
                        }
                        continue;
                    }

                    // Update the value to be checked
                    if (attributeName.equals("input")) {
                        date = iso.parse(attributeValue);
                        continue;
                    }
                    // At this point, it must be either dateType or timeType
                    int index = lookupValue(attributeValue, DateFormatNames);
                    if (attributeName.equals("dateType"))
                        dateFormat = index;
                    else if (attributeName.equals("timeType"))
                        timeFormat = index;

                }

                // The attribute value must be approved in order to be checked,
                // if it hasn't been approved, it shouldn't be checked if it
                // matches with ICU
                if (approved) {
                    SimpleDateFormat dt = getDateFormat(locale, dateFormat, timeFormat);
                    dt.setTimeZone(utc);
                    String temp = dt.format(date).trim();
                    result = result.trim(); // HACK because of SAX
                    if (!temp.equals(result)) {
                        logln("DateTime: Locale: " + locale +
                                "\n\tDate: " + DateFormatNames[dateFormat] +
                                "\n\tTime: " + DateFormatNames[timeFormat] +
                                "\n\tDraft: " + settings.get("draft") +
                                "\n\tCLDR: <" + result + "> " +
                                "\n\tICU: <" + temp + ">");
                    }
                }
            }

            private SimpleDateFormat getDateFormat(ULocale locale, int dateFormat, int timeFormat) {
                if (DEBUG)
                    logln("Getting date/time format for " + locale);
                if (DEBUG && "ar_EG".equals(locale.toString())) {
                    logln("debug here");
                }
                DateFormat dt;
                if (dateFormat == 0) {
                    dt = DateFormat.getTimeInstance(DateFormatValues[timeFormat], locale);
                    if (DEBUG)
                        System.out.print("getTimeInstance");
                } else if (timeFormat == 0) {
                    dt = DateFormat.getDateInstance(DateFormatValues[dateFormat], locale);
                    if (DEBUG)
                        System.out.print("getDateInstance");
                } else {
                    dt = DateFormat.getDateTimeInstance(DateFormatValues[dateFormat], DateFormatValues[timeFormat],
                            locale);
                    if (DEBUG)
                        System.out.print("getDateTimeInstance");
                }
                if (DEBUG)
                    logln("\tinput:\t" + dateFormat + ", " + timeFormat + " => " + ((SimpleDateFormat) dt).toPattern());
                return (SimpleDateFormat) dt;
            }
        });

        // ============ Handler for Zones ============
        addHandler("zoneFields", new Handler() {
            String date = "";
            String zone = "";
            String parse = "";
            String pattern = "";

            public void handleResult(ULocale locale, String result) throws ParseException {
                for (Iterator it = settings.keySet().iterator(); it.hasNext();) {
                    String attributeName = (String) it.next();
                    String attributeValue = (String) settings.get(attributeName);
                    if (attributeName.equals("date")) {
                        date = attributeValue;
                    } else if (attributeName.equals("field")) {
                        pattern = attributeValue;
                    } else if (attributeName.equals("zone")) {
                        zone = attributeValue;
                    } else if (attributeName.equals("parse")) {
                        parse = attributeValue;
                    }
                }

                if (!ZONE_MATCH.reset(zone).matches()) return;
                Date dateValue = iso.parse(date);
                SimpleDateFormat field = new SimpleDateFormat(pattern, locale);
                field.setTimeZone(TimeZone.getTimeZone(zone));
                String temp = field.format(dateValue).trim();
                // SKIP PARSE FOR NOW
                result = result.trim(); // HACK because of SAX
                if (!temp.equals(result)) {
                    temp = field.format(dateValue).trim(); // call again for debugging
                    logln("Zone Format: Locale: " + locale
                            + "\n\tZone: " + zone
                            + "\n\tDate: " + date
                            + "\n\tField: " + pattern
                            + "\n\tParse: " + parse
                            + "\n\tDraft: " + settings.get("draft")
                            + "\n\tCLDR: <" + result
                            + ">\n\tICU: <" + temp + ">");
                }
            }
        });
    }

    // ============ Gorp for SAX ============

    {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            SAX = factory.newSAXParser();
        } catch (Exception e) {
            throw new IllegalArgumentException("SAXParserFacotry was unable to start.");
        }
    }

    DefaultHandler DEFAULT_HANDLER = new DefaultHandler() {
        static final boolean DEBUG = false;
        StringBuffer lastChars = new StringBuffer();
        // boolean justPopped = false;
        Handler handler;

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // data.put(new ContextStack(contextStack), lastChars);
            // lastChars = "";
            try {
                if (qName.equals("cldrTest")) {
                    // skip
                } else if (qName.equals("result") && handler != null) {
                    for (int i = 0; i < attributes.getLength(); ++i) {
                        handler.set(attributes.getQName(i), attributes.getValue(i));
                    }
                } else {
                    handler = getHandler(qName, attributes);
                    // handler.set("locale", uLocale.toString());
                }
                // if (DEBUG) logln("startElement:\t" + contextStack);
                // justPopped = false;
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            try {
                // if (DEBUG) logln("endElement:\t" + contextStack);
                if (qName.equals("result") && handler != null) {
                    handler.checkResult(lastChars.toString());
                } else if (qName.length() != 0) {
                    // logln("Unexpected contents of: " + qName + ", <" + lastChars + ">");
                }
                lastChars.setLength(0);
                // justPopped = true;
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        }

        // Have to hack around the fact that the character data might be in pieces
        public void characters(char[] ch, int start, int length) throws SAXException {
            try {
                String value = new String(ch, start, length);
                if (DEBUG)
                    logln("characters:\t" + value);
                lastChars.append(value);
                // justPopped = false;
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        }

        // just for debugging

        public void notationDecl(String name, String publicId, String systemId) throws SAXException {
            logln("notationDecl: " + name + ", " + publicId + ", " + systemId);
        }

        public void processingInstruction(String target, String data) throws SAXException {
            logln("processingInstruction: " + target + ", " + data);
        }

        public void skippedEntity(String name) throws SAXException {
            logln("skippedEntity: " + name);
        }

        public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
                throws SAXException {
            logln("unparsedEntityDecl: " + name + ", " + publicId + ", " + systemId + ", " + notationName);
        }
    };
}
