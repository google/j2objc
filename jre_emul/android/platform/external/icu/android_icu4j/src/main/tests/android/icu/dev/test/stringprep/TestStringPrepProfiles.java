/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.stringprep;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.StringPrep;
import android.icu.text.StringPrepParseException;

/**
 * @author Michael Ow
 *
 */
public class TestStringPrepProfiles extends TestFmwk {
    /*
     * The format of the test cases should be the following:
     * {
     *     Profile name
     *     src string1
     *     expected result1
     *     src string2
     *     expected result2
     *     ...
     * } 
     * 
     * *Note: For expected failures add FAIL to beginning of the source string and for expected result use "FAIL".
     */
    private static String[][] testCases = {
        {
            "RFC4013_SASLPREP",
            "user:\u00A0\u0AC6\u1680\u00ADpassword1",
            "user: \u0AC6 password1"
        },
        {
            "RFC4011_MIB",
            "Policy\u034F\u200DBase\u0020d\u1806\u200C",
            "PolicyBase d"
        },
        {
            "RFC4505_TRACE",
            "Anony\u0020\u00A0mous\u3000\u0B9D\u034F\u00AD",
            "Anony\u0020\u00A0mous\u3000\u0B9D\u034F\u00AD"
        },
        {
            "RFC4518_LDAP",
            "Ldap\uFB01\u00ADTest\u0020\u00A0\u2062ing",
            "LdapfiTest  ing"
        },
        {
            "RFC4518_LDAP_CI",
            "Ldap\uFB01\u00ADTest\u0020\u00A0\u2062ing12345",
            "ldapfitest  ing12345"
        },
        {
            "RFC3920_RESOURCEPREP",
            "ServerXM\u2060\uFE00\uFE09PP s p ",
            "ServerXMPP s p "
        },
        {
            "RFC3920_NODEPREP",
            "Server\u200DXMPPGreEK\u03D0",
            "serverxmppgreek\u03B2"
        },
        {
            "RFC3722_ISCSI",
            "InternetSmallComputer\uFB01\u0032\u2075Interface",
            "internetsmallcomputerfi25interface",
            "FAILThisShouldFailBecauseOfThis\u002F",
            "FAIL"
        },
        {
            "RFC3530_NFS4_CS_PREP",
            "\u00ADUser\u2060Name@ \u06DDDOMAIN.com",
            "UserName@ \u06DDDOMAIN.com"
        },
        {
            "RFC3530_NFS4_CS_PREP_CI",
            "\u00ADUser\u2060Name@ \u06DDDOMAIN.com",
            "username@ \u06DDdomain.com"
        },
        {
            "RFC3530_NFS4_CIS_PREP",
            "AA\u200C\u200D @@DomAin.org",
            "aa @@domain.org"
        },
        {
            "RFC3530_NFS4_MIXED_PREP_PREFIX",
            "PrefixUser \u007F\uFB01End",
            "PrefixUser \u007FfiEnd"
        },
        {
            "RFC3530_NFS4_MIXED_PREP_SUFFIX",
            "SuffixDomain \u007F\uFB01EnD",
            "suffixdomain \u007Ffiend"
        }
    };
    
    private int getOptionFromProfileName(String profileName) {
        if (profileName.equals("RFC4013_SASLPREP")) {
            return StringPrep.RFC4013_SASLPREP;
        } else if (profileName.equals("RFC4011_MIB")) {
            return StringPrep.RFC4011_MIB;
        } else if (profileName.equals("RFC4505_TRACE")) {
            return StringPrep.RFC4505_TRACE;
        } else if (profileName.equals("RFC4518_LDAP")) {
            return StringPrep.RFC4518_LDAP;
        } else if (profileName.equals("RFC4518_LDAP_CI")) {
            return StringPrep.RFC4518_LDAP_CI;
        } else if (profileName.equals("RFC3920_RESOURCEPREP")) {
            return StringPrep.RFC3920_RESOURCEPREP;
        } else if (profileName.equals("RFC3920_NODEPREP")) {
            return StringPrep.RFC3920_NODEPREP;
        } else if (profileName.equals("RFC3722_ISCSI")) {
            return StringPrep.RFC3722_ISCSI;
        } else if (profileName.equals("RFC3530_NFS4_CS_PREP")) {
            return StringPrep.RFC3530_NFS4_CS_PREP;
        } else if (profileName.equals("RFC3530_NFS4_CS_PREP_CI")) {
            return StringPrep.RFC3530_NFS4_CS_PREP_CI;
        } else if (profileName.equals("RFC3530_NFS4_CIS_PREP")) {
            return StringPrep.RFC3530_NFS4_CIS_PREP;
        } else if (profileName.equals("RFC3530_NFS4_MIXED_PREP_PREFIX")) {
            return StringPrep.RFC3530_NFS4_MIXED_PREP_PREFIX;
        } else if (profileName.equals("RFC3530_NFS4_MIXED_PREP_SUFFIX")) {
            return StringPrep.RFC3530_NFS4_MIXED_PREP_SUFFIX;
        } 
        
        // Should not happen.
        return -1;
    }
    
    @Test
    public void TestProfiles() {
        String profileName = null;
        StringPrep sprep = null;
        String result = null;
        String src = null;
        String expected = null;
        
        for (int i = 0; i < testCases.length; i++) {
            for (int j = 0; j < testCases[i].length; j++) {
                if (j == 0) {
                    profileName = testCases[i][j];
                    
                    sprep = StringPrep.getInstance(getOptionFromProfileName(profileName));
                } else {
                    src = testCases[i][j];
                    expected = testCases[i][++j];
                    try {
                        result = sprep.prepare(src, StringPrep.ALLOW_UNASSIGNED);
                        if (src.startsWith("FAIL")) {
                            errln("Failed: Expected error for Test[" + i +"] Profile: " + profileName);
                        } else if (!result.equals(expected)) {
                            errln("Failed: Test[" + i + "] Result string does not match expected string for StringPrep test for profile: " + profileName);
                        }
                    } catch (StringPrepParseException ex) {
                        if (!src.startsWith("FAIL")) {
                            errln("Failed: Test[" + i + "] StringPrep profile " + profileName + " got error: " + ex);
                        }
                    }
                }
            }
        }
    }
}
