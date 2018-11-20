/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2006, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.sample;

// TODO(user) : turned off

public class ModuleTestSample /*extends ModuleTest*/ {
    ModuleTestSample(){
        //super("com/ibm/icu/dev/data/testdata/", "Test");
    }

    // standard loop, settings and cases
//    public void Test01() {
//    while (nextSettings()) {
//        logln("--------");
//        logln("String: " + settings.getString("aString"));
//        if (settings.isDefined("anInt")) {
//        logln("Int: " + settings.getInt("anInt"));
//        }
//        logln("Boolean: " + settings.getBoolean("aBoolean"));
//
//        while (nextCase()) {
//        logln("  ----");
//        logln("  StringArray: " + printArray(testcase.getStringArray("aStringArray")));
//        logln("  IntArray: " + printArray(testcase.getIntArray("anIntArray")));
//        logln("  BooleanArray: " + printArray(testcase.getBooleanArray("aBooleanArray")));
//        }
//    }
//    }
//
//    // loop with just cases
//    public void Test02() {
//    while (nextCase()) {
//        logln("----");
//        logln("String: " + testcase.getString("aString"));
//        logln("Int: " + testcase.getInt("anInt"));
//        logln("Boolean: " + testcase.getBoolean("aBoolean"));
//    }
//    }

    // no cases, just uses info for test
    public void Test03() {
//    DataMap info = testInfo();
//    if (info != null) {
////        logln(info.getString(TestDataModule.DESCRIPTION)); // standard
//        logln(info.getString("Extra")); // test-specific
//    }
//    return;
    }

    // no data, ModuleTest should not allow this to execute by default
    public void Test04() {
//    errln("Test04 should not execute!");
    }

    // special override of validateMethod allows Test05
    // to execute even though it has no data in the module
//    protected boolean validateMethod(String methodName) {
//    return methodName.equals("Test05") ? true : super.validateMethod(methodName);
//    }

    // no data, but override of validateMethod allows it to execute
    public void Test05() {
//    logln("Test05 executed.");
    }

//    // The test data contains an error in the third case.  When getInt("Data") is
//    // executed the error is logged and iteration stops.
//    public void Test06() {
//    while (nextCase()) {
//        logln("----");
//        logln("isGood: " + testcase.getString("IsGood"));
//        logln("  Data: " + testcase.getInt("Data"));
//    }
//    }
//
//    // The test using the data reports an error, which also automatically stops iteration.
//    public void Test07() {
//    while (nextSettings()) {
//        int value = settings.getInt("Value");
//        while (nextCase()) {
//        int factor = testcase.getInt("Factor");
//        float result = (float)value / factor;
//        if (result != (int)result) {
//            errln("the number '" + factor + "' is not a factor of the number '" + value + "'");
//        } else {
//            logln("'" + factor + "' is a factor of '" + value + "'");
//        }
//        }
//    }
//    }

//    // The number of data elements is incorrect
//    public void Test08() {
//    while (nextCase()) {
//        int one = testcase.getInt("One");
//        int two = testcase.getInt("Two");
//        int three = testcase.getInt("Three");
//        logln("got: " + one + ", " + two + ", " + three);
//    }
//    }
//
//    public void Test09() {
//        while (nextCase()) {
//            int radix = testcase.getInt("Radix");
//            int[] pow = testcase.getIntArray("Power");
//            int[] val = testcase.getIntArray("Value");
//            logln("radix: " + radix + " pow: " + printArray(pow) + " val: " + printArray(val));
//            for (int i = 0; i < pow.length; ++i) {
//                if (val[i] != (int)Math.pow(radix, pow[i])) {
//                    errln("radix: " + radix + " to power " + pow[i] + " != " + val[i]);
//                    break;
//                }
//            }
//        }
//    }

    // utility print functions to display the data from the resource
    String printArray(String[] a) {
    StringBuffer buf = new StringBuffer("String[] {");
    for (int i = 0; i < a.length; ++i) {
        if (i != 0) {
        buf.append(",");
        }
        buf.append(" " + a[i]);
    }
    buf.append(" }");
    return buf.toString();
    }

    String printArray(int[] a) {
    StringBuffer buf = new StringBuffer("int[] {");
    for (int i = 0; i < a.length; ++i) {
        if (i != 0) {
        buf.append(",");
        }
        buf.append(" " + a[i]);
    }
    buf.append(" }");
    return buf.toString();
    }

    String printArray(boolean[] a) {
    StringBuffer buf = new StringBuffer("boolean[] {");
    for (int i = 0; i < a.length; ++i) {
        if (i != 0) {
        buf.append(",");
        }
        buf.append(" " + a[i]);
    }
    buf.append(" }");
    return buf.toString();
    }

    /* (non-Javadoc)
     * @see android.icu.dev.test.ModuleTest#processModules()
     */
    protected void processModules() {
        // TODO Auto-generated method stub

    }
}
