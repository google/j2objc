/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2005-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************

 *******************************************************************************
 */

package android.icu.dev.test.stringprep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.text.IDNA;
import android.icu.text.StringPrepParseException;
import android.icu.text.UTF16;

/**
 * @author limaoyu
 * 
 */
public class IDNAConformanceTest extends TestFmwk {
    @Test
    public void TestConformance() {

        TreeMap inputData = null;
        
        try {
            inputData = ReadInput.getInputData();
        } catch (UnsupportedEncodingException e) {
            errln(e.getMessage());
            return;
        } catch (IOException e) {
            errln(e.getMessage());
            return;
        }
        
        Set keyMap = inputData.keySet();
        for (Iterator iter = keyMap.iterator(); iter.hasNext();) {
            Long element = (Long) iter.next();
            HashMap tempHash = (HashMap) inputData.get(element);

            //get all attributes from input data
            String passfail = (String) tempHash.get("passfail");
            String desc = (String) tempHash.get("desc");
            String type = (String) tempHash.get("type");
            String namebase = (String) tempHash.get("namebase");
            String nameutf8 = (String) tempHash.get("nameutf8");
            String namezone = (String) tempHash.get("namezone");
            String failzone1 = (String) tempHash.get("failzone1");
            String failzone2 = (String) tempHash.get("failzone2");

            //they maybe includes <*> style unicode
            namebase = stringReplace(namebase);
            namezone = stringReplace(namezone);

            String result = null;
            boolean failed = false;

            if ("toascii".equals(tempHash.get("type"))) {
                
                //get the result
                try {
                    //by default STD3 rules are not used, but if the description
                    //includes UseSTD3ASCIIRules, we will set it.
                    if (desc.toLowerCase().indexOf(
                            "UseSTD3ASCIIRules".toLowerCase()) == -1) {
                        result = IDNA.convertIDNToASCII(namebase,
                                IDNA.ALLOW_UNASSIGNED).toString();
                    } else {
                        result = IDNA.convertIDNToASCII(namebase,
                                IDNA.USE_STD3_RULES).toString();
                    }
                } catch (StringPrepParseException e2) {
                    //errln(e2.getMessage());
                    failed = true;
                }
                
                
                if ("pass".equals(passfail)) {
                    if (!namezone.equals(result)) {
                        printInfo(desc, namebase, nameutf8, namezone,
                                failzone1, failzone2, result, type, passfail);                        
                        errln("\t pass fail standard is pass, but failed");
                    } else {
                        printInfo(desc, namebase, nameutf8, namezone,
                                failzone1, failzone2, result, type, passfail);
                        logln("\tpassed");
                    }
                }

                if ("fail".equals(passfail)) {
                    if (failed) {
                        printInfo(desc, namebase, nameutf8, namezone,
                                failzone1, failzone2, result, type, passfail);
                        logln("passed");
                    } else {
                        printInfo(desc, namebase, nameutf8, namezone,
                                failzone1, failzone2, result, type, passfail);                        
                        errln("\t pass fail standard is fail, but no exception thrown out");
                    }
                }
            } else if ("tounicode".equals(tempHash.get("type"))) {
                try {
                    //by default STD3 rules are not used, but if the description
                    //includes UseSTD3ASCIIRules, we will set it.
                    if (desc.toLowerCase().indexOf(
                            "UseSTD3ASCIIRules".toLowerCase()) == -1) {
                        result = IDNA.convertIDNToUnicode(namebase,
                                IDNA.ALLOW_UNASSIGNED).toString();
                    } else {
                        result = IDNA.convertIDNToUnicode(namebase,
                                IDNA.USE_STD3_RULES).toString();
                    }
                } catch (StringPrepParseException e2) {
                    //errln(e2.getMessage());
                    failed = true;
                }
                if ("pass".equals(passfail)) {
                    if (!namezone.equals(result)) {
                        printInfo(desc, namebase, nameutf8, namezone,
                                failzone1, failzone2, result, type, passfail);
                        
                        errln("\t Did not get the expected result. Expected: " + prettify(namezone) + " Got: " + prettify(result));
                    } else {
                        printInfo(desc, namebase, nameutf8, namezone,
                                failzone1, failzone2, result, type, passfail);                        
                        logln("\tpassed");
                    }
                }

                if ("fail".equals(passfail)) {
                    if (failed || namebase.equals(result)) {
                        printInfo(desc, namebase, nameutf8, namezone,
                                failzone1, failzone2, result, type, passfail);
                        
                        logln("\tpassed");
                    } else {
                        printInfo(desc, namebase, nameutf8, namezone,
                                failzone1, failzone2, result, type, passfail);
                        
                        errln("\t pass fail standard is fail, but no exception thrown out");
                    }
                }
            } else {
                continue;
            }
        }
    }

    /**
     * Print log message.
     * @param desc
     * @param namebase
     * @param nameutf8
     * @param namezone
     * @param failzone1
     * @param failzone2
     * @param result
     */
    private void printInfo(String desc, String namebase,
            String nameutf8, String namezone, String failzone1,
            String failzone2, String result, String type, String passfail) {
        logln("desc:\t" + desc);
        log("\t");
        logln("type:\t" + type);
        log("\t");
        logln("pass fail standard:\t" + passfail);
        log("\t");
        logln("namebase:\t" + namebase);
        log("\t");
        logln("nameutf8:\t" + nameutf8);
        log("\t");
        logln("namezone:\t" + namezone);
        log("\t");
        logln("failzone1:\t" + failzone1);
        log("\t");
        logln("failzone2:\t" + failzone2);
        log("\t");
        logln("result:\t" + result);
    }

    /**
     * Change unicode string from <00AD> to \u00AD, for the later is accepted
     * by Java
     * @param str String including <*> style unicode
     * @return \\u String
     */
    private static String stringReplace(String str) {

        StringBuffer result = new StringBuffer();
        char[] chars = str.toCharArray();
        StringBuffer sbTemp = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            if ('<' == chars[i]) {
                sbTemp = new StringBuffer();
                while ('>' != chars[i + 1]) {
                    sbTemp.append(chars[++i]);
                }
                /*
                 * The unicode sometimes is larger then \uFFFF, so have to use
                 * UTF16.
                 */
                int toBeInserted = Integer.parseInt(sbTemp.toString(), 16);
                if ((toBeInserted >> 16) == 0) {
                    result.append((char) toBeInserted);
                } else {
                    String utf16String = UTF16.valueOf(toBeInserted);
                    char[] charsTemp = utf16String.toCharArray();
                    for (int j = 0; j < charsTemp.length; j++) {
                        result.append((char) charsTemp[j]);
                    }
                }
            } else if ('>' == chars[i]) {//end when met with '>'
                continue;
            } else {
                result.append(chars[i]);
            }

        }
        return result.toString();
    }

    /**
     * This class is used to read test data from TestInput file.
     * 
     * @author limaoyu
     *  
     */
    public static class ReadInput {

        public static TreeMap getInputData() throws IOException,
                UnsupportedEncodingException {

            TreeMap result = new TreeMap();
            BufferedReader in = TestUtil.getDataReader("IDNATestInput.txt", "utf-8");
            try {
                String tempStr = null;
                int records = 0;
                boolean firstLine = true;
                HashMap hashItem = new HashMap();

                while ((tempStr = in.readLine()) != null) {
                    //ignore the first line if it's "====="
                    if (firstLine) {
                        if ("=====".equals(tempStr))
                            continue;
                        firstLine = false;
                    }

                    //Ignore empty line
                    if ("".equals(tempStr)) {
                        continue;
                    }

                    String attr = "";//attribute
                    String body = "";//value

                    //get attr and body from line input, and then set them into each hash item.
                    int postion = tempStr.indexOf(":");
                    if (postion > -1) {
                        attr = tempStr.substring(0, postion).trim();
                        body = tempStr.substring(postion + 1).trim();

                        //deal with combination lines when end with '\'
                        while (null != body && body.length() > 0
                                && '\\' == body.charAt(body.length() - 1)) {
                            body = body.substring(0, body.length() - 1);
                            body += "\n";
                            tempStr = in.readLine();
                            body += tempStr;
                        }
                    }
                    //push them to hash item
                    hashItem.put(attr, body);

                    //if met "=====", it means this item is finished
                    if ("=====".equals(tempStr)) {
                        //set them into result, using records number as key
                        result.put(new Long(records), hashItem);
                        //create another hash item and continue
                        hashItem = new HashMap();
                        records++;
                        continue;
                    }
                }
            } finally {
                in.close();
            }
            return result;
        }
    }
}
