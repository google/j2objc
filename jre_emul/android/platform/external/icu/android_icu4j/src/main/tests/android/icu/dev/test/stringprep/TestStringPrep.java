/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2009, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/
package android.icu.dev.test.stringprep;

import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.StringPrep;
import android.icu.text.StringPrepParseException;

/**
 * @author ram
 *
 */
public class TestStringPrep extends TestFmwk {
    /*
       There are several special identifiers ("who") which need to be
       understood universally, rather than in the context of a particular
       DNS domain.  Some of these identifiers cannot be understood when an
       NFS client accesses the server, but have meaning when a local process
       accesses the file.  The ability to display and modify these
       permissions is permitted over NFS, even if none of the access methods
       on the server understands the identifiers.

        Who                    Description
       _______________________________________________________________

       "OWNER"                The owner of the file.
       "GROUP"                The group associated with the file.
       "EVERYONE"             The world.
       "INTERACTIVE"          Accessed from an interactive terminal.
       "NETWORK"              Accessed via the network.
       "DIALUP"               Accessed as a dialup user to the server.
       "BATCH"                Accessed from a batch job.
       "ANONYMOUS"            Accessed without any authentication.
       "AUTHENTICATED"        Any authenticated user (opposite of
                              ANONYMOUS)
       "SERVICE"              Access from a system service.

       To avoid conflict, these special identifiers are distinguish by an
       appended "@" and should appear in the form "xxxx@" (note: no domain
       name after the "@").  For example: ANONYMOUS@.
    */
    private String[] mixed_prep_data ={
        "OWNER@",
        "GROUP@",        
        "EVERYONE@",     
        "INTERACTIVE@",  
        "NETWORK@",      
        "DIALUP@",       
        "BATCH@",        
        "ANONYMOUS@",    
        "AUTHENTICATED@",
        "\u0930\u094D\u092E\u094D\u0915\u094D\u0937\u0947\u0924\u094D@slip129-37-118-146.nc.us.ibm.net",
        "\u0936\u094d\u0930\u0940\u092e\u0926\u094d@saratoga.pe.utexas.edu",
        "\u092d\u0917\u0935\u0926\u094d\u0917\u0940\u0924\u093e@dial-120-45.ots.utexas.edu",
        "\u0905\u0927\u094d\u092f\u093e\u092f@woo-085.dorms.waller.net",
        "\u0905\u0930\u094d\u091c\u0941\u0928@hd30-049.hil.compuserve.com",
        "\u0935\u093f\u0937\u093e\u0926@pem203-31.pe.ttu.edu",
        "\u092f\u094b\u0917@56K-227.MaxTNT3.pdq.net",
        "\u0927\u0943\u0924\u0930\u093e\u0937\u094d\u091f\u094d\u0930@dial-36-2.ots.utexas.edu",
        "\u0909\u0935\u093E\u091A\u0943@slip129-37-23-152.ga.us.ibm.net",
        "\u0927\u0930\u094d\u092e\u0915\u094d\u0937\u0947\u0924\u094d\u0930\u0947@ts45ip119.cadvision.com",
        "\u0915\u0941\u0930\u0941\u0915\u094d\u0937\u0947\u0924\u094d\u0930\u0947@sdn-ts-004txaustP05.dialsprint.net",
        "\u0938\u092e\u0935\u0947\u0924\u093e@bar-tnt1s66.erols.com",
        "\u092f\u0941\u092f\u0941\u0924\u094d\u0938\u0935\u0903@101.st-louis-15.mo.dial-access.att.net",
        "\u092e\u093e\u092e\u0915\u093e\u0903@h92-245.Arco.COM",
        "\u092a\u093e\u0923\u094d\u0921\u0935\u093e\u0936\u094d\u091a\u0948\u0935@dial-13-2.ots.utexas.edu",
        "\u0915\u093f\u092e\u0915\u0941\u0930\u094d\u0935\u0924@net-redynet29.datamarkets.com.ar",
        "\u0938\u0902\u091c\u0935@ccs-shiva28.reacciun.net.ve",
        "\u0c30\u0c18\u0c41\u0c30\u0c3e\u0c2e\u0c4d@7.houston-11.tx.dial-access.att.net",
        "\u0c35\u0c3f\u0c36\u0c4d\u0c35\u0c28\u0c3e\u0c27@ingw129-37-120-26.mo.us.ibm.net",
        "\u0c06\u0c28\u0c02\u0c26\u0c4d@dialup6.austintx.com",
        "\u0C35\u0C26\u0C4D\u0C26\u0C3F\u0C30\u0C3E\u0C1C\u0C41@dns2.tpao.gov.tr",
        "\u0c30\u0c3e\u0c1c\u0c40\u0c35\u0c4d@slip129-37-119-194.nc.us.ibm.net",
        "\u0c15\u0c36\u0c30\u0c2c\u0c3e\u0c26@cs7.dillons.co.uk.203.119.193.in-addr.arpa",
        "\u0c38\u0c02\u0c1c\u0c40\u0c35\u0c4d@swprd1.innovplace.saskatoon.sk.ca",
        "\u0c15\u0c36\u0c30\u0c2c\u0c3e\u0c26@bikini.bologna.maraut.it",
        "\u0c38\u0c02\u0c1c\u0c40\u0c2c\u0c4d@node91.subnet159-198-79.baxter.com",
        "\u0c38\u0c46\u0c28\u0c4d\u0c17\u0c41\u0c2a\u0c4d\u0c24@cust19.max5.new-york.ny.ms.uu.net",
        "\u0c05\u0c2e\u0c30\u0c47\u0c02\u0c26\u0c4d\u0c30@balexander.slip.andrew.cmu.edu",
        "\u0c39\u0c28\u0c41\u0c2e\u0c3e\u0c28\u0c41\u0c32@pool029.max2.denver.co.dynip.alter.net",
        "\u0c30\u0c35\u0c3f@cust49.max9.new-york.ny.ms.uu.net",
        "\u0c15\u0c41\u0c2e\u0c3e\u0c30\u0c4d@s61.abq-dialin2.hollyberry.com",
        "\u0c35\u0c3f\u0c36\u0c4d\u0c35\u0c28\u0c3e\u0c27@\u0917\u0928\u0947\u0936.sanjose.ibm.com",
        "\u0c06\u0c26\u0c3f\u0c24\u0c4d\u0c2f@www.\u00E0\u00B3\u00AF.com",
        "\u0C15\u0C02\u0C26\u0C4D\u0C30\u0C47\u0C17\u0C41\u0c32@www.\u00C2\u00A4.com",
        "\u0c36\u0c4d\u0c30\u0c40\u0C27\u0C30\u0C4D@www.\u00C2\u00A3.com",
        "\u0c15\u0c02\u0c1f\u0c2e\u0c36\u0c46\u0c1f\u0c4d\u0c1f\u0c3f@\u0025",
        "\u0c2e\u0c3e\u0c27\u0c35\u0c4d@\u005C\u005C",
        "\u0c26\u0c46\u0c36\u0c46\u0c1f\u0c4d\u0c1f\u0c3f@www.\u0021.com",
        "test@www.\u0024.com",
        "help@\u00C3\u00BC.com",
    };
    @Test
    public void TestNFS4MixedPrep(){
        for(int i=0; i< mixed_prep_data.length; i++){
            try{
                String src = mixed_prep_data[i];
                byte[] dest = NFS4StringPrep.mixed_prepare(src.getBytes("UTF-8"));
                String destString = new String(dest, "UTF-8");
                int destIndex = destString.indexOf('@');
                if(destIndex < 0){
                    errln("Delimiter @ disappeared from the output!");
                }
            }catch(Exception e){
                errln("mixed_prepare for string: " + mixed_prep_data[i] +" failed with " + e.toString());
            }
        } 
        /* test the error condition */
        {
            String src = "OWNER@oss.software.ibm.com";
            try{
                byte[] dest = NFS4StringPrep.mixed_prepare(src.getBytes("UTF-8"));
                if(dest!=null){
                    errln("Did not get the expected exception");
                }
            }catch(Exception e){
                logln("mixed_prepare for string: " + src +" passed with " + e.toString());
            }

         }
    }
    @Test
    public void TestCISPrep(){

        for(int i=0;i< (TestData.conformanceTestCases.length);i++){
            TestData.ConformanceTestCase testCase = TestData.conformanceTestCases[i];
            String src = testCase.input;
            Exception expected = testCase.expected;
            String expectedDest = testCase.output;
            try{
                byte[] dest =NFS4StringPrep.cis_prepare(src.getBytes("UTF-8"));
                String destString = new String(dest, "UTF-8");
                if(!expectedDest.equalsIgnoreCase(destString)){
                      errln("Did not get the expected output for nfs4_cis_prep at index " + i);
                }
            }catch(Exception e){
                if(!expected.equals(e)){
                    errln("Did not get the expected exception");
                }
            } 

        }
    }

    @Test
    public void TestCSPrep(){
        
        // Checking for bidi is turned off
        String src = "\uC138\uACC4\uC758\uBAA8\uB4E0\uC0AC\uB78C\uB4E4\uC774\u0644\u064A\u0647\uD55C\uAD6D\uC5B4\uB97C\uC774\uD574\uD55C\uB2E4\uBA74";
        try{
            NFS4StringPrep.cs_prepare(src.getBytes("UTF-8"), false);
        }catch(Exception e){
            errln("Got unexpected exception: " + e.toString());
        }
        
        // normalization is turned off
        try{
            src = "www.\u00E0\u00B3\u00AF.com";
            byte[] dest = NFS4StringPrep.cs_prepare(src.getBytes("UTF-8"), false);
            String destStr = new String(dest, "UTF-8");
            if(!src.equals(destStr)){
                errln("Did not get expected output. Expected: "+ prettify(src)+
                      " Got: " + prettify(destStr));
            }
        }catch(Exception e){
            errln("Got unexpected exception: " + e.toString());
        }
        
        // test case insensitive string
        try{
            src = "THISISATEST";
            byte[] dest = NFS4StringPrep.cs_prepare(src.getBytes("UTF-8"), false);
            String destStr = new String(dest, "UTF-8");
            if(!src.toLowerCase().equals(destStr)){
                errln("Did not get expected output. Expected: "+ prettify(src)+
                      " Got: " + prettify(destStr));
            }
        }catch(Exception e){
            errln("Got unexpected exception: " + e.toString());
        }
        // test case sensitive string
        try{
            src = "THISISATEST";
            byte[] dest = NFS4StringPrep.cs_prepare(src.getBytes("UTF-8"), true);
            String destStr = new String(dest, "UTF-8");
            if(!src.equals(destStr)){
                errln("Did not get expected output. Expected: "+ prettify(src)+
                      " Got: " + prettify(destStr));
            }
        }catch(Exception e){
            errln("Got unexpected exception: " + e.toString());
        }
    }
    
    @Test
    public void TestCoverage(){
        if (new StringPrepParseException("coverage", 0, "", 0,0) == null){
            errln("Construct StringPrepParseException(String, int, String, int, int)");
        }
    }
    
    /* Tests the method public static StringPrep getInstance(int profile) */
    @Test
    public void TestGetInstance(){
        // Tests when "if (profile < 0 || profile > MAX_PROFILE)" is true
        int[] neg_num_cases = {-100,-50,-10,-5,-2,-1};
        for(int i=0; i<neg_num_cases.length; i++){
            try{
                StringPrep.getInstance(neg_num_cases[i]);
                errln("StringPrep.getInstance(int) expected an exception for " +
                        "an invalid parameter of " + neg_num_cases[i]);
            } catch(Exception e){
            }
        }
        
        int[] max_profile_cases = {StringPrep.RFC4518_LDAP_CI+1, StringPrep.RFC4518_LDAP_CI+2, StringPrep.RFC4518_LDAP_CI+5, StringPrep.RFC4518_LDAP_CI+10};
        for(int i=0; i<max_profile_cases.length; i++){
            try{
                StringPrep.getInstance(max_profile_cases[i]);
                errln("StringPrep.getInstance(int) expected an exception for " +
                        "an invalid parameter of " + max_profile_cases[i]);
            } catch(Exception e){
            }
        }
        
        // Tests when "if (instance == null)", "if (stream != null)", "if (instance != null)", and "if (ref != null)" is true
        int[] cases = {0, 1, StringPrep.RFC4518_LDAP_CI};
        for(int i=0; i<cases.length; i++){
            try{
                StringPrep.getInstance(cases[i]);
            } catch(Exception e){
                errln("StringPrep.getInstance(int) did not expected an exception for " +
                        "an valid parameter of " + cases[i]);
            }
        }
    }
    
    /* Test the method public String prepare(String src, int options) */
    @Test
    public void TestPrepare() {
        StringPrep sp = StringPrep.getInstance(0);
        try {
            if (!(sp.prepare("dummy", 0)).equals("dummy")) {
                errln("StringPrep.prepare(String,int) was suppose to return " + "'dummy'");
            }
        } catch (Exception e) {
            errln("StringPrep.prepare(String,int) was not suppose to return " + "an exception.");
        }
    }
    
    /*
     * Tests the constructor public StringPrepParseException(String message, int error, String rules, int pos, int
     * lineNumber)
     */
    @Test
    public void TestStringPrepParseException() {
        Locale locales[] = {Locale.US, Locale.FRENCH, Locale.SIMPLIFIED_CHINESE};
        String rules = "This is a very odd little set of rules, just for testing, you know...";
        StringPrepParseException exceptions[] = new StringPrepParseException[locales.length];

        for (int i = 0; i < locales.length; i += 1) {
            exceptions[i] = new StringPrepParseException(locales[i].toString(), i, rules, i, i);
        }
    }
    
    /* Tests the method public boolean equals(Object other) for StringPrepParseException */
    @Test
    public void TestStringPrepParseExceptionEquals(){
        StringPrepParseException sppe = new StringPrepParseException("dummy",0,"dummy",0,0);
        StringPrepParseException sppe_clone = new StringPrepParseException("dummy",0,"dummy",0,0);
        StringPrepParseException sppe1 = new StringPrepParseException("dummy1",1,"dummy1",0,0);
        
        // Tests when "if(!(other instanceof StringPrepParseException))" is true
        if(sppe.equals(0)){
            errln("StringPrepParseException.equals(Object) is suppose to return false when " +
                    "passing integer '0'");
        }
        if(sppe.equals(0.0)){
            errln("StringPrepParseException.equals(Object) is suppose to return false when " +
                    "passing float/double '0.0'");
        }
        if(sppe.equals("0")){
            errln("StringPrepParseException.equals(Object) is suppose to return false when " +
                    "passing string '0'");
        }
        
        // Tests when "if(!(other instanceof StringPrepParseException))" is true
        if(!sppe.equals(sppe)){
            errln("StringPrepParseException.equals(Object) is suppose to return true when " +
            "comparing to the same object");
        }
        if(!sppe.equals(sppe_clone)){
            errln("StringPrepParseException.equals(Object) is suppose to return true when " +
            "comparing to the same initiated object");
        }
        if(sppe.equals(sppe1)){
            errln("StringPrepParseException.equals(Object) is suppose to return false when " +
            "comparing to another object that isn't the same");
        }
    }
    
    /* Tests the method public int getError() */
    @Test
    public void TestGetError(){
        for(int i=0; i < 5; i++){
            StringPrepParseException sppe = new StringPrepParseException("dummy",i,"dummy",0,0);
            if(sppe.getError() != i){
                errln("StringPrepParseExcpetion.getError() was suppose to return " + i + " but got " + sppe.getError());
            }
        }
    }
    
    /* Tests the private void setPreContext(char[] str, int pos) */
    @Test
    public void TestSetPreContext(){
        String WordAtLeast16Characters = "abcdefghijklmnopqrstuvwxyz";
        for(int i=0; i < 5; i++){
            try{
                @SuppressWarnings("unused")
                StringPrepParseException sppe = new StringPrepParseException("dummy",i,WordAtLeast16Characters,0,0);
                sppe = new StringPrepParseException(WordAtLeast16Characters,i,"dummy",0,0);
            } catch(Exception e){
                errln("StringPrepParseException.setPreContext was not suppose to return an exception");
            }
        }
    }
}
