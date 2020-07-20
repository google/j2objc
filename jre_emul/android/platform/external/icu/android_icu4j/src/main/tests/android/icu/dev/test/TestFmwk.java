/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2015, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import android.icu.util.TimeZone;
import android.icu.util.ULocale;

/**
 * TestFmwk is a base class for tests that can be run conveniently from the
 * command line as well as under the Java test harness.
 * <p>
 * Sub-classes implement a set of methods named Test <something>. Each of these
 * methods performs some test. Test methods should indicate errors by calling
 * either err or errln. This will increment the errorCount field and may
 * optionally print a message to the log. Debugging information may also be
 * added to the log via the log and logln methods. These methods will add their
 * arguments to the log only if the test is being run in verbose mode.
 */
abstract public class TestFmwk extends AbstractTestLog {
    /**
     * The default time zone for all of our tests. Used in @Before
     */
    private final static TimeZone defaultTimeZone = TimeZone.getTimeZone("America/Los_Angeles");

    /**
     * The default locale used for all of our tests. Used in @Before
     */
    private final static Locale defaultLocale = Locale.US;

    private static final String EXHAUSTIVENESS = "ICU.exhaustive";
    private static final int DEFAULT_EXHAUSTIVENESS = 0;
    private static final int MAX_EXHAUSTIVENESS = 10;

    private static final String LOGGING_LEVEL = "ICU.logging";
    private static final int DEFAULT_LOGGING_LEVEL = 0;
    private static final int MAX_LOGGING_LEVEL = 3;

    public static final int LOGGING_NONE = 0;
    public static final int LOGGING_WARN = 1;
    public static final int LOGGING_INFO = 2;
    public static final int LOGGING_DEBUG = 3;

    private static final String SEED = "ICU.seed";
    private static final String SECURITY_POLICY = "ICU.securitypolicy";

    private static final TestParams testParams;
    static {
        testParams = TestParams.create();
    }

    protected TestFmwk() {
    }

    @Before
    public void testInitialize() {
        Locale.setDefault(defaultLocale);
        TimeZone.setDefault(defaultTimeZone);

        /* J2ObjC removed
        if (getParams().testSecurityManager != null) {
            System.setSecurityManager(getParams().testSecurityManager);
        } */
    }

    @After
    public void testTeardown() {
        /* J2ObjC removed
        if (getParams().testSecurityManager != null) {
            System.setSecurityManager(getParams().originalSecurityManager);
        } */
    }

    private static TestParams getParams() {
        //return paramsReference.get();
        return testParams;
    }

    protected static boolean isVerbose() {
        return getParams().getLoggingLevel() >= LOGGING_INFO;
    }

    /**
     * 0 = fewest tests, 5 is normal build, 10 is most tests
     */
    protected static int getExhaustiveness() {
        return getParams().inclusion;
    }

    protected static boolean isQuick() {
        return getParams().getInclusion() == 0;
    }

    // use this instead of new random so we get a consistent seed
    // for our tests
    protected Random createRandom() {
        return new Random(getParams().getSeed());
    }

    static final String ICU_TRAC_URL = "http://bugs.icu-project.org/trac/ticket/";
    static final String CLDR_TRAC_URL = "http://unicode.org/cldr/trac/ticket/";
    static final String CLDR_TICKET_PREFIX = "cldrbug:";

    /**
     * Log the known issue.
     * This method returns true unless -prop:logKnownIssue=no is specified
     * in the argument list.
     *
     * @param ticket A ticket number string. For an ICU ticket, use numeric characters only,
     * such as "10245". For a CLDR ticket, use prefix "cldrbug:" followed by ticket number,
     * such as "cldrbug:5013".
     * @param comment Additional comment, or null
     * @return true unless -prop:logKnownIssue=no is specified in the test command line argument.
     */
    protected static boolean logKnownIssue(String ticket, String comment) {
        if (!getBooleanProperty("logKnownIssue", true)) {
            return false;
        }

        StringBuffer descBuf = new StringBuffer();
        // TODO(junit) : what to do about this?
        //getParams().stack.appendPath(descBuf);
        if (comment != null && comment.length() > 0) {
            descBuf.append(" (" + comment + ")");
        }
        String description = descBuf.toString();

        String ticketLink = "Unknown Ticket";
        if (ticket != null && ticket.length() > 0) {
            boolean isCldr = false;
            ticket = ticket.toLowerCase(Locale.ENGLISH);
            if (ticket.startsWith(CLDR_TICKET_PREFIX)) {
                isCldr = true;
                ticket = ticket.substring(CLDR_TICKET_PREFIX.length());
            }
            ticketLink = (isCldr ? CLDR_TRAC_URL : ICU_TRAC_URL) + ticket;
        }

        if (getParams().knownIssues == null) {
            getParams().knownIssues = new TreeMap<String, List<String>>();
        }
        List<String> lines = getParams().knownIssues.get(ticketLink);
        if (lines == null) {
            lines = new ArrayList<String>();
            getParams().knownIssues.put(ticketLink, lines);
        }
        if (!lines.contains(description)) {
            lines.add(description);
        }

        return true;
    }

    protected static String getProperty(String key) {
        return getParams().getProperty(key);
    }

    protected static boolean getBooleanProperty(String key) {
        return getParams().getBooleanProperty(key);
    }

    protected static boolean getBooleanProperty(String key, boolean defVal) {
        return getParams().getBooleanProperty(key, defVal);
    }

    protected static int getIntProperty(String key, int defVal) {
        return getParams().getIntProperty(key, defVal);
    }

    protected static int getIntProperty(String key, int defVal, int maxVal) {
        return getParams().getIntProperty(key, defVal, maxVal);
    }

    protected static TimeZone safeGetTimeZone(String id) {
        TimeZone tz = TimeZone.getTimeZone(id);
        if (tz == null) {
            // should never happen
            errln("FAIL: TimeZone.getTimeZone(" + id + ") => null");
        }
        if (!tz.getID().equals(id)) {
            warnln("FAIL: TimeZone.getTimeZone(" + id + ") => " + tz.getID());
        }
        return tz;
    }


    // Utility Methods

    protected static String hex(char[] s){
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length; ++i) {
            if (i != 0) result.append(',');
            result.append(hex(s[i]));
        }
        return result.toString();
    }

    protected static String hex(byte[] s){
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length; ++i) {
            if (i != 0) result.append(',');
            result.append(hex(s[i]));
        }
        return result.toString();
    }

    protected static String hex(char ch) {
        StringBuffer result = new StringBuffer();
        String foo = Integer.toString(ch, 16).toUpperCase();
        for (int i = foo.length(); i < 4; ++i) {
            result.append('0');
        }
        return result + foo;
    }

    protected static String hex(int ch) {
        StringBuffer result = new StringBuffer();
        String foo = Integer.toString(ch, 16).toUpperCase();
        for (int i = foo.length(); i < 4; ++i) {
            result.append('0');
        }
        return result + foo;
    }

    protected static String hex(CharSequence s) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            if (i != 0)
                result.append(',');
            result.append(hex(s.charAt(i)));
        }
        return result.toString();
    }

    protected static String prettify(CharSequence s) {
        StringBuilder result = new StringBuilder();
        int ch;
        for (int i = 0; i < s.length(); i += Character.charCount(ch)) {
            ch = Character.codePointAt(s, i);
            if (ch > 0xfffff) {
                result.append("\\U00");
                result.append(hex(ch));
            } else if (ch > 0xffff) {
                result.append("\\U000");
                result.append(hex(ch));
            } else if (ch < 0x20 || 0x7e < ch) {
                result.append("\\u");
                result.append(hex(ch));
            } else {
                result.append((char) ch);
            }

        }
        return result.toString();
    }

    private static java.util.GregorianCalendar cal;

    /**
     * Return a Date given a year, month, and day of month. This is similar to
     * new Date(y-1900, m, d). It uses the default time zone at the time this
     * method is first called.
     *
     * @param year
     *            use 2000 for 2000, unlike new Date()
     * @param month
     *            use Calendar.JANUARY etc.
     * @param dom
     *            day of month, 1-based
     * @return a Date object for the given y/m/d
     */
    protected static synchronized java.util.Date getDate(int year, int month,
            int dom) {
        if (cal == null) {
            cal = new java.util.GregorianCalendar();
        }
        cal.clear();
        cal.set(year, month, dom);
        return cal.getTime();
    }

    private static class TestParams {

        private int inclusion;
        private long seed;
        private int loggingLevel;

        private String policyFileName;
        private SecurityManager testSecurityManager;
        private SecurityManager originalSecurityManager;

        private Map<String, List<String>> knownIssues;

        private Properties props;


        private TestParams() {
        }

        static TestParams create() {
            TestParams params = new TestParams();
            Properties props = System.getProperties();
            params.parseProperties(props);
            return params;
        }

        private void parseProperties(Properties props) {
            this.props = props;

            inclusion = getIntProperty(EXHAUSTIVENESS, DEFAULT_EXHAUSTIVENESS, MAX_EXHAUSTIVENESS);
            seed = getLongProperty(SEED, System.currentTimeMillis());
            loggingLevel = getIntProperty(LOGGING_LEVEL, DEFAULT_LOGGING_LEVEL, MAX_LOGGING_LEVEL);

            policyFileName = getProperty(SECURITY_POLICY);
            if (policyFileName != null) {
                String originalPolicyFileName = System.getProperty("java.security.policy");
                originalSecurityManager = System.getSecurityManager();
                System.setProperty("java.security.policy", policyFileName);
                Policy.getPolicy().refresh();
                testSecurityManager = new SecurityManager();
                System.setProperty("java.security.policy", originalPolicyFileName==null ? "" : originalPolicyFileName);
            }
        }

        public String getProperty(String key) {
            String val = null;
            if (key != null && key.length() > 0) {
                val = props.getProperty(key);
            }
            return val;
        }

        public boolean getBooleanProperty(String key) {
            return getBooleanProperty(key, false);
        }

        public boolean getBooleanProperty(String key, boolean defVal) {
            String s = getProperty(key);
            if (s == null) {
                return defVal;
            }
            if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equals("1")) {
                return true;
            }
            return false;
        }

        public int getIntProperty(String key, int defVal) {
            return getIntProperty(key, defVal, -1);
        }

        public int getIntProperty(String key, int defVal, int maxVal) {
            String s = getProperty(key);
            if (s == null) {
                return defVal;
            }
            return (maxVal == -1) ? Integer.valueOf(s) : Math.max(Integer.valueOf(s), maxVal);
        }

        public long getLongProperty(String key, long defVal) {
            String s = getProperty(key);
            if (s == null) {
                return defVal;
            }
            return Long.valueOf(s);
        }

        public int getInclusion() {
            return inclusion;
        }

        public long getSeed() {
            return seed;
        }

        public int getLoggingLevel() {
            return loggingLevel;
        }
    }

    /**
     * Check the given array to see that all the strings in the expected array
     * are present.
     *
     * @param msg
     *            string message, for log output
     * @param array
     *            array of strings to check
     * @param expected
     *            array of strings we expect to see, or null
     * @return the length of 'array', or -1 on error
     */
    protected static int checkArray(String msg, String array[], String expected[]) {
        int explen = (expected != null) ? expected.length : 0;
        if (!(explen >= 0 && explen < 31)) { // [sic] 31 not 32
            errln("Internal error");
            return -1;
        }
        int i = 0;
        StringBuffer buf = new StringBuffer();
        int seenMask = 0;
        for (; i < array.length; ++i) {
            String s = array[i];
            if (i != 0)
                buf.append(", ");
            buf.append(s);
            // check expected list
            for (int j = 0, bit = 1; j < explen; ++j, bit <<= 1) {
                if ((seenMask & bit) == 0) {
                    if (s.equals(expected[j])) {
                        seenMask |= bit;
                        logln("Ok: \"" + s + "\" seen");
                    }
                }
            }
        }
        logln(msg + " = [" + buf + "] (" + i + ")");
        // did we see all expected strings?
        if (((1 << explen) - 1) != seenMask) {
            for (int j = 0, bit = 1; j < expected.length; ++j, bit <<= 1) {
                if ((seenMask & bit) == 0) {
                    errln("\"" + expected[j] + "\" not seen");
                }
            }
        }
        return array.length;
    }

    /**
     * Check the given array to see that all the locales in the expected array
     * are present.
     *
     * @param msg
     *            string message, for log output
     * @param array
     *            array of locales to check
     * @param expected
     *            array of locales names we expect to see, or null
     * @return the length of 'array'
     */
    protected static int checkArray(String msg, Locale array[], String expected[]) {
        String strs[] = new String[array.length];
        for (int i = 0; i < array.length; ++i) {
            strs[i] = array[i].toString();
        }
        return checkArray(msg, strs, expected);
    }

    /**
     * Check the given array to see that all the locales in the expected array
     * are present.
     *
     * @param msg
     *            string message, for log output
     * @param array
     *            array of locales to check
     * @param expected
     *            array of locales names we expect to see, or null
     * @return the length of 'array'
     */
    protected static int checkArray(String msg, ULocale array[], String expected[]) {
        String strs[] = new String[array.length];
        for (int i = 0; i < array.length; ++i) {
            strs[i] = array[i].toString();
        }
        return checkArray(msg, strs, expected);
    }

    // JUnit-like assertions.

    protected static boolean assertTrue(String message, boolean condition) {
        return handleAssert(condition, message, "true", null);
    }

    protected static boolean assertFalse(String message, boolean condition) {
        return handleAssert(!condition, message, "false", null);
    }

    protected static boolean assertEquals(String message, boolean expected,
            boolean actual) {
        return handleAssert(expected == actual, message, String
                .valueOf(expected), String.valueOf(actual));
    }

    protected static boolean assertEquals(String message, long expected, long actual) {
        return handleAssert(expected == actual, message, String
                .valueOf(expected), String.valueOf(actual));
    }

    // do NaN and range calculations to precision of float, don't rely on
    // promotion to double
    protected static boolean assertEquals(String message, float expected,
            float actual, double error) {
        boolean result = Float.isInfinite(expected)
                ? expected == actual
                : !(Math.abs(expected - actual) > error); // handles NaN
        return handleAssert(result, message, String.valueOf(expected)
                + (error == 0 ? "" : " (within " + error + ")"), String
                .valueOf(actual));
    }

    protected static boolean assertEquals(String message, double expected,
            double actual, double error) {
        boolean result = Double.isInfinite(expected)
                ? expected == actual
                : !(Math.abs(expected - actual) > error); // handles NaN
        return handleAssert(result, message, String.valueOf(expected)
                + (error == 0 ? "" : " (within " + error + ")"), String
                .valueOf(actual));
    }

    protected static <T> boolean assertEquals(String message, T[] expected, T[] actual) {
        // Use toString on a List to get useful, readable messages
        String expectedString = expected == null ? "null" : Arrays.asList(expected).toString();
        String actualString = actual == null ? "null" : Arrays.asList(actual).toString();
        return assertEquals(message, expectedString, actualString);
    }

    protected static boolean assertEquals(String message, Object expected,
            Object actual) {
        boolean result = expected == null ? actual == null : expected
                .equals(actual);
        return handleAssert(result, message, stringFor(expected),
                stringFor(actual));
    }

    protected static boolean assertNotEquals(String message, Object expected,
            Object actual) {
        boolean result = !(expected == null ? actual == null : expected
                .equals(actual));
        return handleAssert(result, message, stringFor(expected),
                stringFor(actual), "not equal to", true);
    }

    protected boolean assertSame(String message, Object expected, Object actual) {
        return handleAssert(expected == actual, message, stringFor(expected),
                stringFor(actual), "==", false);
    }

    protected static boolean assertNotSame(String message, Object expected,
            Object actual) {
        return handleAssert(expected != actual, message, stringFor(expected),
                stringFor(actual), "!=", true);
    }

    protected static boolean assertNull(String message, Object actual) {
        return handleAssert(actual == null, message, null, stringFor(actual));
    }

    protected static boolean assertNotNull(String message, Object actual) {
        return handleAssert(actual != null, message, null, stringFor(actual),
                "!=", true);
    }

    protected static void fail() {
        fail("");
    }

    protected static void fail(String message) {
        if (message == null) {
            message = "";
        }
        if (!message.equals("")) {
            message = ": " + message;
        }
        errln(sourceLocation() + message);
    }

    private static boolean handleAssert(boolean result, String message,
            String expected, String actual) {
        return handleAssert(result, message, expected, actual, null, false);
    }

    public static boolean handleAssert(boolean result, String message,
            Object expected, Object actual, String relation, boolean flip) {
        if (!result || isVerbose()) {
            if (message == null) {
                message = "";
            }
            if (!message.equals("")) {
                message = ": " + message;
            }
            relation = relation == null ? ", got " : " " + relation + " ";
            if (result) {
                logln("OK " + message + ": "
                        + (flip ? expected + relation + actual : expected));
            } else {
                // assert must assume errors are true errors and not just warnings
                // so cannot warnln here
                errln(  message
                        + ": expected"
                        + (flip ? relation + expected : " " + expected
                                + (actual != null ? relation + actual : "")));
            }
        }
        return result;
    }

    private static final String stringFor(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + obj + '"';
        }
        return obj.getClass().getName() + "<" + obj + ">";
    }

    // Return the source code location of the caller located callDepth frames up the stack.
    protected static String sourceLocation() {
        // Walk up the stack to the first call site outside this file
        for (StackTraceElement st : new Throwable().getStackTrace()) {
            String source = st.getFileName();
            if (source != null && !source.equals("TestFmwk.java") && !source.equals("AbstractTestLog.java")) {
                String methodName = st.getMethodName();
                if (methodName != null &&
                       (methodName.startsWith("Test") || methodName.startsWith("test") || methodName.equals("main"))) {
                    return "(" + source + ":" + st.getLineNumber() + ") ";
                }
            }
        }
        throw new InternalError();
    }

    protected static boolean checkDefaultPrivateConstructor(String fullyQualifiedClassName) throws Exception {
        return checkDefaultPrivateConstructor(Class.forName(fullyQualifiedClassName));
    }

    protected static boolean checkDefaultPrivateConstructor(Class<?> classToBeTested) throws Exception {
        Constructor<?> constructor = classToBeTested.getDeclaredConstructor();

        // Check that the constructor is private.
        boolean isPrivate = Modifier.isPrivate(constructor.getModifiers());

        // Call the constructor for coverage.
        constructor.setAccessible(true);
        constructor.newInstance();

        if (!isPrivate) {
            errln("Default private constructor for class: " + classToBeTested.getName() + " is not private.");
        }
        return isPrivate;
    }

    /**
     * Tests the toString method on a private or hard-to-reach class.  Assumes constructor of the class does not
     * take any arguments.
     * @param fullyQualifiedClassName
     * @return The output of the toString method.
     * @throws Exception
     */
    protected static String invokeToString(String fullyQualifiedClassName) throws Exception {
        return invokeToString(fullyQualifiedClassName, new Class<?>[]{}, new Object[]{});
    }

    /**
     * Tests the toString method on a private or hard-to-reach class.  Assumes constructor of the class does not
     * take any arguments.
     * @param classToBeTested
     * @return The output of the toString method.
     * @throws Exception
     */
    protected static String invokeToString(Class<?> classToBeTested) throws Exception {
        return invokeToString(classToBeTested, new Class<?>[]{}, new Object[]{});
    }

    /**
     * Tests the toString method on a private or hard-to-reach class.  Allows you to specify the argument types for
     * the constructor.
     * @param fullyQualifiedClassName
     * @return The output of the toString method.
     * @throws Exception
     */
    protected static String invokeToString(String fullyQualifiedClassName,
            Class<?>[] constructorParamTypes, Object[] constructorParams) throws Exception {
        return invokeToString(Class.forName(fullyQualifiedClassName), constructorParamTypes, constructorParams);
    }

    /**
     * Tests the toString method on a private or hard-to-reach class.  Allows you to specify the argument types for
     * the constructor.
     * @param classToBeTested
     * @return The output of the toString method.
     * @throws Exception
     */
    protected static String invokeToString(Class<?> classToBeTested,
            Class<?>[] constructorParamTypes, Object[] constructorParams) throws Exception {
        Constructor<?> constructor = classToBeTested.getDeclaredConstructor(constructorParamTypes);
        constructor.setAccessible(true);
        Object obj = constructor.newInstance(constructorParams);
        Method toStringMethod = classToBeTested.getDeclaredMethod("toString");
        toStringMethod.setAccessible(true);
        return (String) toStringMethod.invoke(obj);
    }


    // End JUnit-like assertions

    // TODO (sgill): added to keep errors away
    /* (non-Javadoc)
     * @see android.icu.dev.test.TestLog#msg(java.lang.String, int, boolean, boolean)
     */
    //@Override
    protected static void msg(String message, int level, boolean incCount, boolean newln) {
        if (level == TestLog.WARN || level == TestLog.ERR) {
            Assert.fail(message);
        }
        // TODO(stuartg): turned off - causing OOM running under ant
//        while (level > 0) {
//            System.out.print(" ");
//            level--;
//        }
//        System.out.print(message);
//        if (newln) {
//            System.out.println();
//        }
    }

}
