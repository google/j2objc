/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2015, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.impl.Utility;

/**
 * A collection of methods to run the data driven number format test suite.
 */
public class DataDrivenNumberFormatTestUtility {
    
    /**
     * Base class for code under test.
     */
    public static abstract class CodeUnderTest {
        
        /**
         * Returns the ID of the code under test. This ID is used to identify
         * tests that are known to fail for this particular code under test.
         * This implementation returns null which means that by default all
         * tests should work with this code under test.
         * @return 'J' means ICU4J, 'K' means JDK
         */
        public Character Id() {
            return null;
        }
        
        /**
         *  Runs a single formatting test. On success, returns null.
         *  On failure, returns the error. This implementation just returns null.
         *  Subclasses should override.
         *  @param tuple contains the parameters of the format test.
         */
        public String format(NumberFormatTestData tuple) {
            return null;
        }
        
        /**
         *  Runs a single toPattern test. On success, returns null.
         *  On failure, returns the error. This implementation just returns null.
         *  Subclasses should override.
         *  @param tuple contains the parameters of the format test.
         */
        public String toPattern(NumberFormatTestData tuple) {
            return null;
        }
        
        /**
         *  Runs a single parse test. On success, returns null.
         *  On failure, returns the error. This implementation just returns null.
         *  Subclasses should override.
         *  @param tuple contains the parameters of the format test.
         */
        public String parse(NumberFormatTestData tuple) {
            return null;
        }
        
        /**
         *  Runs a single parse currency test. On success, returns null.
         *  On failure, returns the error. This implementation just returns null.
         *  Subclasses should override.
         *  @param tuple contains the parameters of the format test.
         */
        public String parseCurrency(NumberFormatTestData tuple) {
            return null;
        }
        
        /**
         * Runs a single select test. On success, returns null.
         *  On failure, returns the error. This implementation just returns null.
         *  Subclasses should override.
         * @param tuple contains the parameters of the format test.
         */
        public String select(NumberFormatTestData tuple) {
            return null;
        }
    }
    
    private static enum RunMode {
        SKIP_KNOWN_FAILURES,
        INCLUDE_KNOWN_FAILURES     
    }
    
    private final CodeUnderTest codeUnderTest;
    private String fileLine = null;
    private int fileLineNumber = 0;
    private String fileTestName = "";   
    private NumberFormatTestData tuple = new NumberFormatTestData();
      
    /**
     * Runs all the tests in the data driven test suite against codeUnderTest.
     * @param fileName The name of the test file. A relative file name under
     *   com/ibm/icu/dev/data such as "data.txt"
     * @param codeUnderTest the code under test
     */
    
    static void runSuite(
            String fileName, CodeUnderTest codeUnderTest) {
        new DataDrivenNumberFormatTestUtility(codeUnderTest)
                .run(fileName, RunMode.SKIP_KNOWN_FAILURES);
    }
    
    /**
     * Runs every format test in data driven test suite including those
     * that are known to fail.
     * 
     * @param fileName The name of the test file. A relative file name under
     *   com/ibm/icu/dev/data such as "data.txt"
     * @param codeUnderTest the code under test
     */
    static void runFormatSuiteIncludingKnownFailures(
            String fileName, CodeUnderTest codeUnderTest) {
        new DataDrivenNumberFormatTestUtility(codeUnderTest)
                .run(fileName, RunMode.INCLUDE_KNOWN_FAILURES);
    }
    
    private DataDrivenNumberFormatTestUtility(
            CodeUnderTest codeUnderTest) {
        this.codeUnderTest = codeUnderTest;
    }
       
    private void run(String fileName, RunMode runMode) {
        Character codeUnderTestIdObj = codeUnderTest.Id();
        char codeUnderTestId =
                codeUnderTestIdObj == null ? 0 : Character.toUpperCase(codeUnderTestIdObj.charValue());
        BufferedReader in = null;
        try {
            in = TestUtil.getDataReader("numberformattestspecification.txt", "UTF-8");
            // read first line and remove BOM if present
            readLine(in);
            if (fileLine != null && fileLine.charAt(0) == '\uFEFF') {
                fileLine = fileLine.substring(1);
            }
            
            int state = 0;
            List<String> columnValues;
            List<String> columnNames = null;
            while (true) {
                if (fileLine == null || fileLine.length() == 0) {
                    if (!readLine(in)) {
                        break;
                    }
                    if (fileLine.length() == 0 && state == 2) {
                        state = 0;
                    }
                    continue;
                }
                if (fileLine.startsWith("//")) {
                    fileLine = null;
                    continue;
                }
                // Initial setup of test.
                if (state == 0) {
                    if (fileLine.startsWith("test ")) {
                        fileTestName = fileLine;
                        tuple = new NumberFormatTestData();
                    } else if (fileLine.startsWith("set ")) {
                        if (!setTupleField()) {
                            return;
                        }
                    } else if(fileLine.startsWith("begin")) {
                        state = 1;
                    } else {
                        showError("Unrecognized verb.");
                        return;
                    }
                // column specification
                } else if (state == 1) {
                    columnNames = splitBy((char) 0x09);
                    state = 2;
                // run the tests
                } else {
                    int columnNamesSize = columnNames.size();
                    columnValues = splitBy(columnNamesSize, (char) 0x09);
                    int columnValuesSize = columnValues.size();
                    for (int i = 0; i < columnValuesSize; ++i) {
                        if (!setField(columnNames.get(i), columnValues.get(i))) {
                            return;
                        }
                    }
                    for (int i = columnValuesSize; i < columnNamesSize; ++i) {
                        if (!clearField(columnNames.get(i))) {
                            return;
                        }
                    }
                    if (runMode == RunMode.INCLUDE_KNOWN_FAILURES
                            || !breaks(codeUnderTestId)) {
                        String errorMessage = isPass(tuple);
                        if (errorMessage != null) {
                            showError(errorMessage);
                        }
                    }
                }
                fileLine = null;
            }
        } catch (Exception e) {
            showError(e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean breaks(char code) {
       String breaks = tuple.breaks == null ? "" : tuple.breaks;
       return (breaks.toUpperCase().indexOf(code) != -1);
    }

    private static boolean isSpace(char c) {
        return (c == 0x09 || c == 0x20 || c == 0x3000);
    }
    
    private boolean setTupleField() {
        List<String> parts = splitBy(3, (char) 0x20);
        if (parts.size() < 3) {
            showError("Set expects 2 parameters");
            return false;
        }
        return setField(parts.get(1), parts.get(2));
    }
    
    private boolean setField(String name, String value) {
        try {
            tuple.setField(name,  Utility.unescape(value));
            return true;
        } catch (Exception e) {
            showError("No such field: " + name + ", or bad value: " + value);
            return false;
        }
    }
    
    private boolean clearField(String name) {
        try {
            tuple.clearField(name);
            return true;
        } catch (Exception e) {
            showError("Field cannot be clared: " + name);
            return false;
        }
    }
    
    private void showError(String message) {
        TestFmwk.errln(String.format("line %d: %s\n%s\n%s", fileLineNumber, Utility.escape(message), fileTestName,fileLine));
    }
   
    private List<String> splitBy(char delimiter) {
        return splitBy(Integer.MAX_VALUE, delimiter);
    }
      
    private List<String> splitBy(int max, char delimiter) {
        ArrayList<String> result = new ArrayList<String>();    
        int colIdx = 0;
        int colStart = 0;
        int len = fileLine.length();
        for (int idx = 0; colIdx < max - 1 && idx < len; ++idx) {
            char ch = fileLine.charAt(idx);
            if (ch == delimiter) {
                result.add(
                        fileLine.substring(colStart, idx));
                ++colIdx;
                colStart = idx + 1;
            }
        }
        result.add(fileLine.substring(colStart, len));
        return result;
    }  

    private boolean readLine(BufferedReader in) throws IOException {
        String line = in.readLine();
        if (line == null) {
            fileLine = null;
            return false;
        }
        ++fileLineNumber;
        // Strip trailing comments and spaces
        int idx = line.length();
        for (; idx > 0; idx--) {
            if (!isSpace(line.charAt(idx -1))) {
                break;
            }
        }
        fileLine = idx == 0 ? "" : line;
        return true;
    }
    
    private String isPass(NumberFormatTestData tuple) {
        StringBuilder result = new StringBuilder();
        if (tuple.format != null && tuple.output != null) {
            String errorMessage = codeUnderTest.format(tuple);
            if (errorMessage != null) {
                result.append(errorMessage);
            }
        } else if (tuple.toPattern != null || tuple.toLocalizedPattern != null) {
            String errorMessage = codeUnderTest.toPattern(tuple);
            if (errorMessage != null) {
                result.append(errorMessage);
            }
        } else if (tuple.parse != null && tuple.output != null && tuple.outputCurrency != null) {
            String errorMessage = codeUnderTest.parseCurrency(tuple);
            if (errorMessage != null) {
                result.append(errorMessage);
            }
        } else if (tuple.parse != null && tuple.output != null) {
            String errorMessage = codeUnderTest.parse(tuple);
            if (errorMessage != null) {
                result.append(errorMessage);
            }
        } else if (tuple.plural != null) {
            String errorMessage = codeUnderTest.select(tuple);
            if (errorMessage != null) {
                result.append(errorMessage);
            }
        } else {
            result.append("Unrecognized test type.");
        }
        if (result.length() > 0) {
            result.append(": ");
            result.append(tuple);
            return result.toString();
        }
        return null;
    }
}
