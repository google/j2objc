/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 **********************************************************************
 * Copyright (c) 2011-2016, International Business Machines
 * Corporation and others.  All Rights Reserved.
 **********************************************************************
 * Author: John Emmons
 * Created: April 8 - 2011
 * Since: ICU 4.8
 **********************************************************************
 */

package android.icu.dev.test.util;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.util.Region;
import android.icu.util.Region.RegionType;

/**
 * @test
 * @summary General test of Regions
 */

public class RegionTest extends TestFmwk {
    String[][] knownRegions = {
            //   Code  , Numeric , Parent, Type, Containing Continent
            { "001", "001", null , "WORLD", null },
            { "002", "002", "001", "CONTINENT", null },
            { "003", "003", null,  "GROUPING", null },
            { "005", "005", "019", "SUBCONTINENT", "019" },
            { "009", "009", "001", "CONTINENT", null},
            { "011", "011", "002", "SUBCONTINENT", "002" },
            { "013", "013", "019", "SUBCONTINENT", "019" },
            { "014", "014", "002", "SUBCONTINENT", "002" },
            { "015", "015", "002", "SUBCONTINENT", "002" },
            { "017", "017", "002", "SUBCONTINENT", "002" },
            { "018", "018", "002", "SUBCONTINENT", "002" },
            { "019", "019", "001", "CONTINENT", null },
            { "021", "021", "019", "SUBCONTINENT", "019" },
            { "029", "029", "019", "SUBCONTINENT", "019" },
            { "030", "030", "142", "SUBCONTINENT", "142" },
            { "034", "034", "142", "SUBCONTINENT", "142" },
            { "035", "035", "142", "SUBCONTINENT", "142" },
            { "039", "039", "150", "SUBCONTINENT", "150"},
            { "053", "053", "009", "SUBCONTINENT", "009" },
            { "054", "054", "009", "SUBCONTINENT", "009" },
            { "057", "057", "009", "SUBCONTINENT", "009" },
            { "061", "061", "009", "SUBCONTINENT", "009" },
            { "142", "142", "001", "CONTINENT", null },
            { "143", "143", "142", "SUBCONTINENT", "142" },
            { "145", "145", "142", "SUBCONTINENT", "142" },
            { "150", "150", "001", "CONTINENT", null },
            { "151", "151", "150", "SUBCONTINENT", "150" },
            { "154", "154", "150", "SUBCONTINENT", "150" },
            { "155", "155", "150", "SUBCONTINENT", "150" },
            { "419", "419", null,  "GROUPING" , null},
            { "AC" , "-1" , "QO" , "TERRITORY", "009" },
            { "AD" , "020", "039", "TERRITORY", "150" },
            { "AE" , "784", "145", "TERRITORY", "142" },
            { "AF" , "004", "034", "TERRITORY", "142" },
            { "AG" , "028", "029", "TERRITORY", "019" },
            { "AI" , "660", "029", "TERRITORY", "019" },
            { "AL" , "008", "039", "TERRITORY", "150" },
            { "AM" , "051", "145", "TERRITORY", "142" },
            { "AN" , "530", null,  "DEPRECATED", null },
            { "AO" , "024", "017", "TERRITORY", "002" },
            { "AQ" , "010", "QO" , "TERRITORY", "009" },
            { "AR" , "032", "005", "TERRITORY", "019" },
            { "AS" , "016", "061", "TERRITORY", "009" },
            { "AT" , "040", "155", "TERRITORY", "150" },
            { "AU" , "036", "053", "TERRITORY", "009" },
            { "AW" , "533", "029", "TERRITORY", "019" },
            { "AX" , "248", "154", "TERRITORY", "150" },
            { "AZ" , "031", "145", "TERRITORY", "142" },
            { "BA" , "070", "039", "TERRITORY", "150" },
            { "BB" , "052", "029", "TERRITORY", "019" },
            { "BD" , "050", "034", "TERRITORY", "142" },
            { "BE" , "056", "155", "TERRITORY", "150" },
            { "BF" , "854", "011", "TERRITORY", "002" },
            { "BG" , "100", "151", "TERRITORY", "150" },
            { "BH" , "048", "145", "TERRITORY", "142" },
            { "BI" , "108", "014", "TERRITORY", "002" },
            { "BJ" , "204", "011", "TERRITORY", "002" },
            { "BL" , "652", "029", "TERRITORY", "019" },
            { "BM" , "060", "021", "TERRITORY", "019" },
            { "BN" , "096", "035", "TERRITORY", "142" },
            { "BO" , "068", "005", "TERRITORY", "019" },
            { "BQ" , "535", "029", "TERRITORY", "019" },
            { "BR" , "076", "005", "TERRITORY", "019" },
            { "BS" , "044", "029", "TERRITORY", "019" },
            { "BT" , "064", "034", "TERRITORY", "142" },
            { "BU" , "104", "035", "TERRITORY", "142" },
            { "BV" , "074", "QO" , "TERRITORY", "009" },
            { "BW" , "072", "018", "TERRITORY", "002" },
            { "BY" , "112", "151", "TERRITORY", "150" },
            { "BZ" , "084", "013", "TERRITORY", "019" },
            { "CA" , "124", "021", "TERRITORY", "019" },
            { "CC" , "166", "QO" , "TERRITORY", "009" },
            { "CD" , "180", "017", "TERRITORY", "002" },
            { "CF" , "140", "017", "TERRITORY", "002" },
            { "CG" , "178", "017", "TERRITORY", "002" },
            { "CH" , "756", "155", "TERRITORY", "150" },
            { "CI" , "384", "011", "TERRITORY", "002" },
            { "CK" , "184", "061", "TERRITORY", "009" },
            { "CL" , "152", "005", "TERRITORY", "019" },
            { "CM" , "120", "017", "TERRITORY", "002" },
            { "CN" , "156", "030", "TERRITORY", "142" },
            { "CO" , "170", "005", "TERRITORY", "019" },
            { "CP" , "-1" , "QO" , "TERRITORY", "009" },
            { "CR" , "188", "013", "TERRITORY", "019" },
            { "CU" , "192", "029", "TERRITORY", "019" },
            { "CV" , "132", "011", "TERRITORY", "002" },
            { "CW" , "531", "029", "TERRITORY", "019" },
            { "CX" , "162", "QO" , "TERRITORY", "009" },
            { "CY" , "196", "145", "TERRITORY", "142" },
            { "CZ" , "203", "151", "TERRITORY", "150" },
            { "DD" , "276", "155", "TERRITORY", "150" },
            { "DE" , "276", "155", "TERRITORY", "150" },
            { "DG" , "-1" , "QO" , "TERRITORY", "009" },
            { "DJ" , "262", "014", "TERRITORY", "002" },
            { "DK" , "208", "154", "TERRITORY", "150" },
            { "DM" , "212", "029", "TERRITORY", "019" },
            { "DO" , "214", "029", "TERRITORY", "019" },
            { "DZ" , "012", "015", "TERRITORY", "002" },
            { "EA" , "-1" , "015", "TERRITORY", "002" },
            { "EC" , "218", "005", "TERRITORY", "019" },
            { "EE" , "233", "154", "TERRITORY", "150" },
            { "EG" , "818", "015", "TERRITORY", "002" },
            { "EH" , "732", "015", "TERRITORY", "002" },
            { "ER" , "232", "014", "TERRITORY", "002" },
            { "ES" , "724", "039", "TERRITORY", "150" },
            { "ET" , "231", "014", "TERRITORY", "002" },
            { "EU" , "967", null,  "GROUPING", null },
            { "FI" , "246", "154", "TERRITORY", "150" },
            { "FJ" , "242", "054", "TERRITORY", "009" },
            { "FK" , "238", "005", "TERRITORY", "019" },
            { "FM" , "583", "057", "TERRITORY", "009" },
            { "FO" , "234", "154", "TERRITORY", "150" },
            { "FR" , "250", "155", "TERRITORY", "150" },
            { "FX" , "250", "155", "TERRITORY", "150" },
            { "GA" , "266", "017", "TERRITORY", "002" },
            { "GB" , "826", "154", "TERRITORY", "150" },
            { "GD" , "308", "029", "TERRITORY", "019" },
            { "GE" , "268", "145", "TERRITORY", "142" },
            { "GF" , "254", "005", "TERRITORY", "019" },
            { "GG" , "831", "154", "TERRITORY", "150" },
            { "GH" , "288", "011", "TERRITORY", "002" },
            { "GI" , "292", "039", "TERRITORY", "150" },
            { "GL" , "304", "021", "TERRITORY", "019" },
            { "GM" , "270", "011", "TERRITORY", "002" },
            { "GN" , "324", "011", "TERRITORY", "002" },
            { "GP" , "312", "029", "TERRITORY", "019" },
            { "GQ" , "226", "017", "TERRITORY", "002" },
            { "GR" , "300", "039", "TERRITORY", "150" },
            { "GS" , "239", "QO" , "TERRITORY", "009" },
            { "GT" , "320", "013", "TERRITORY", "019" },
            { "GU" , "316", "057", "TERRITORY", "009" },
            { "GW" , "624", "011", "TERRITORY", "002" },
            { "GY" , "328", "005", "TERRITORY", "019" },
            { "HK" , "344", "030", "TERRITORY", "142" },
            { "HM" , "334", "QO" , "TERRITORY", "009" },
            { "HN" , "340", "013", "TERRITORY", "019" },
            { "HR" , "191", "039", "TERRITORY", "150" },
            { "HT" , "332", "029", "TERRITORY", "019" },
            { "HU" , "348", "151", "TERRITORY", "150" },
            { "IC" , "-1" , "015", "TERRITORY", "002" },
            { "ID" , "360", "035", "TERRITORY", "142" },
            { "IE" , "372", "154", "TERRITORY", "150" },
            { "IL" , "376", "145", "TERRITORY", "142" },
            { "IM" , "833", "154", "TERRITORY", "150" },
            { "IN" , "356", "034", "TERRITORY", "142" },
            { "IO" , "086", "QO" , "TERRITORY", "009" },
            { "IQ" , "368", "145", "TERRITORY", "142" },
            { "IR" , "364", "034", "TERRITORY", "142" },
            { "IS" , "352", "154", "TERRITORY", "150" },
            { "IT" , "380", "039", "TERRITORY", "150" },
            { "JE" , "832", "154", "TERRITORY", "150" },
            { "JM" , "388", "029", "TERRITORY", "019" },
            { "JO" , "400", "145", "TERRITORY", "142" },
            { "JP" , "392", "030", "TERRITORY", "142" },
            { "KE" , "404", "014", "TERRITORY", "002" },
            { "KG" , "417", "143", "TERRITORY", "142" },
            { "KH" , "116", "035", "TERRITORY", "142" },
            { "KI" , "296", "057", "TERRITORY", "009" },
            { "KM" , "174", "014", "TERRITORY", "002" },
            { "KN" , "659", "029", "TERRITORY", "019" },
            { "KP" , "408", "030", "TERRITORY", "142" },
            { "KR" , "410", "030", "TERRITORY", "142" },
            { "KW" , "414", "145", "TERRITORY", "142" },
            { "KY" , "136", "029", "TERRITORY", "019" },
            { "KZ" , "398", "143", "TERRITORY", "142" },
            { "LA" , "418", "035", "TERRITORY", "142" },
            { "LB" , "422", "145", "TERRITORY", "142" },
            { "LC" , "662", "029", "TERRITORY", "019" },
            { "LI" , "438", "155", "TERRITORY", "150" },
            { "LK" , "144", "034", "TERRITORY", "142" },
            { "LR" , "430", "011", "TERRITORY", "002" },
            { "LS" , "426", "018", "TERRITORY", "002" },
            { "LT" , "440", "154", "TERRITORY", "150" },
            { "LU" , "442", "155", "TERRITORY", "150" },
            { "LV" , "428", "154", "TERRITORY", "150" },
            { "LY" , "434", "015", "TERRITORY", "002" },
            { "MA" , "504", "015", "TERRITORY", "002" },
            { "MC" , "492", "155", "TERRITORY", "150" },
            { "MD" , "498", "151", "TERRITORY", "150" },
            { "ME" , "499", "039", "TERRITORY", "150" },
            { "MF" , "663", "029", "TERRITORY", "019" },
            { "MG" , "450", "014", "TERRITORY", "002" },
            { "MH" , "584", "057", "TERRITORY", "009" },
            { "MK" , "807", "039", "TERRITORY", "150" },
            { "ML" , "466", "011", "TERRITORY", "002" },
            { "MM" , "104", "035", "TERRITORY", "142" },
            { "MN" , "496", "030", "TERRITORY", "142" },
            { "MO" , "446", "030", "TERRITORY", "142" },
            { "MP" , "580", "057", "TERRITORY", "009" },
            { "MQ" , "474", "029", "TERRITORY", "019" },
            { "MR" , "478", "011", "TERRITORY", "002" },
            { "MS" , "500", "029", "TERRITORY", "019" },
            { "MT" , "470", "039", "TERRITORY", "150" },
            { "MU" , "480", "014", "TERRITORY", "002" },
            { "MV" , "462", "034", "TERRITORY", "142" },
            { "MW" , "454", "014", "TERRITORY", "002" },
            { "MX" , "484", "013", "TERRITORY", "019"},
            { "MY" , "458", "035", "TERRITORY", "142" },
            { "MZ" , "508", "014", "TERRITORY", "002" },
            { "NA" , "516", "018", "TERRITORY", "002" },
            { "NC" , "540", "054", "TERRITORY", "009" },
            { "NE" , "562", "011", "TERRITORY", "002" },
            { "NF" , "574", "053", "TERRITORY", "009" },
            { "NG" , "566", "011", "TERRITORY", "002" },
            { "NI" , "558", "013", "TERRITORY", "019" },
            { "NL" , "528", "155", "TERRITORY", "150" },
            { "NO" , "578", "154", "TERRITORY", "150" },
            { "NP" , "524", "034", "TERRITORY", "142" },
            { "NR" , "520", "057", "TERRITORY", "009" },
            { "NT" , "536", null , "DEPRECATED", null },
            { "NU" , "570", "061", "TERRITORY", "009" },
            { "NZ" , "554", "053", "TERRITORY", "009" },
            { "OM" , "512", "145", "TERRITORY", "142" },
            { "PA" , "591", "013", "TERRITORY", "019" },
            { "PE" , "604", "005", "TERRITORY", "019" },
            { "PF" , "258", "061", "TERRITORY", "009" },
            { "PG" , "598", "054", "TERRITORY", "009" },
            { "PH" , "608", "035", "TERRITORY", "142" },
            { "PK" , "586", "034", "TERRITORY", "142" },
            { "PL" , "616", "151", "TERRITORY", "150" },
            { "PM" , "666", "021", "TERRITORY", "019" },
            { "PN" , "612", "061", "TERRITORY", "009" },
            { "PR" , "630", "029", "TERRITORY", "019" },
            { "PS" , "275", "145", "TERRITORY", "142" },
            { "PT" , "620", "039", "TERRITORY", "150" },
            { "PW" , "585", "057", "TERRITORY", "009" },
            { "PY" , "600", "005", "TERRITORY", "019" },
            { "QA" , "634", "145", "TERRITORY", "142" },
            { "QO" , "961", "009", "SUBCONTINENT", "009" },
            { "QU" , "967", null,  "GROUPING", null },
            { "RE" , "638", "014", "TERRITORY", "002" },
            { "RO" , "642", "151", "TERRITORY", "150" },
            { "RS" , "688", "039", "TERRITORY", "150" },
            { "RU" , "643", "151", "TERRITORY", "150" },
            { "RW" , "646", "014", "TERRITORY", "002" },
            { "SA" , "682", "145", "TERRITORY", "142" },
            { "SB" , "090", "054", "TERRITORY", "009" },
            { "SC" , "690", "014", "TERRITORY", "002" },
            { "SD" , "729", "015", "TERRITORY", "002" },
            { "SE" , "752", "154", "TERRITORY", "150" },
            { "SG" , "702", "035", "TERRITORY", "142" },
            { "SH" , "654", "011", "TERRITORY", "002" },
            { "SI" , "705", "039", "TERRITORY", "150" },
            { "SJ" , "744", "154", "TERRITORY", "150" },
            { "SK" , "703", "151", "TERRITORY", "150" },
            { "SL" , "694", "011", "TERRITORY", "002" },
            { "SM" , "674", "039", "TERRITORY", "150" },
            { "SN" , "686", "011", "TERRITORY", "002" },
            { "SO" , "706", "014", "TERRITORY", "002" },
            { "SR" , "740", "005", "TERRITORY", "019" },
            { "SS" , "728", "014", "TERRITORY", "002" },
            { "ST" , "678", "017", "TERRITORY", "002" },
            { "SU" , "810", null , "DEPRECATED" , null},
            { "SV" , "222", "013", "TERRITORY", "019" },
            { "SX" , "534", "029", "TERRITORY", "019" },
            { "SY" , "760", "145", "TERRITORY", "142" },
            { "SZ" , "748", "018", "TERRITORY", "002" },
            { "TA" , "-1" , "QO", "TERRITORY", "009" },
            { "TC" , "796", "029", "TERRITORY", "019" },
            { "TD" , "148", "017", "TERRITORY", "002" },
            { "TF" , "260", "QO" , "TERRITORY", "009" },
            { "TG" , "768", "011", "TERRITORY", "002" },
            { "TH" , "764", "035", "TERRITORY", "142" },
            { "TJ" , "762", "143", "TERRITORY", "142" },
            { "TK" , "772", "061", "TERRITORY", "009" },
            { "TL" , "626", "035", "TERRITORY", "142" },
            { "TM" , "795", "143", "TERRITORY", "142" },
            { "TN" , "788", "015", "TERRITORY", "002" },
            { "TO" , "776", "061", "TERRITORY", "009" },
            { "TP" , "626", "035", "TERRITORY", "142" },
            { "TR" , "792", "145", "TERRITORY", "142" },
            { "TT" , "780", "029", "TERRITORY", "019" },
            { "TV" , "798", "061", "TERRITORY", "009" },
            { "TW" , "158", "030", "TERRITORY", "142" },
            { "TZ" , "834", "014", "TERRITORY", "002" },
            { "UA" , "804", "151", "TERRITORY", "150" },
            { "UG" , "800", "014", "TERRITORY", "002" },
            { "UM" , "581", "QO" , "TERRITORY", "009" },
            { "US" , "840", "021", "TERRITORY", "019" },
            { "UY" , "858", "005", "TERRITORY", "019" },
            { "UZ" , "860", "143", "TERRITORY", "142" },
            { "VA" , "336", "039", "TERRITORY", "150" },
            { "VC" , "670", "029", "TERRITORY", "019" },
            { "VE" , "862", "005", "TERRITORY", "019" },
            { "VG" , "092", "029", "TERRITORY", "019" },
            { "VI" , "850", "029", "TERRITORY", "019" },
            { "VN" , "704", "035", "TERRITORY", "142" },
            { "VU" , "548", "054", "TERRITORY", "009" },
            { "WF" , "876", "061", "TERRITORY", "009" },
            { "WS" , "882", "061", "TERRITORY", "009" },
            { "YD" , "887", "145", "TERRITORY", "142" },
            { "YE" , "887", "145", "TERRITORY", "142" },
            { "YT" , "175", "014", "TERRITORY", "002" },
            { "ZA" , "710", "018", "TERRITORY", "002" },
            { "ZM" , "894", "014", "TERRITORY", "002" },
            { "ZR" , "180", "017", "TERRITORY", "002" },
            { "ZW" , "716", "014", "TERRITORY", "002" },
            { "ZZ" , "999", null , "UNKNOWN", null }
    };

    /**
     * Test for known regions.
     */    
    @Test
    public void TestKnownRegions() {

        for (String [] rd : knownRegions ) {
            try {
                Region r = Region.getInstance(rd[0]);
                int n = r.getNumericCode();
                int e = Integer.valueOf(rd[1]).intValue();
                if ( n != e ) {
                    errln("Numeric code mismatch for region " + r.toString() + ". Expected: " + e + " Got:" + n);
                }
                if (r.getType() != Region.RegionType.valueOf(rd[3])) {
                    errln("Expected region " + r.toString() + " to be of type " + rd[3] + ". Got:" + r.getType().toString());
                }
                int nc = Integer.valueOf(rd[1]).intValue();
                if ( nc > 0 ) {
                    Region ncRegion = Region.getInstance(nc);
                    if ( !ncRegion.equals(r) && nc != 891 ) { // 891 is special case - CS and YU both deprecated codes for region 891
                        errln("Creating region " + r.toString() + " by its numeric code returned a different region. Got: " + ncRegion.toString());
                    }
                }
            } catch (IllegalArgumentException ex ) {
                errln("Known region " + rd[0] + " was not recognized.");
            }
        }
    }

    @Test
    public void TestGetInstanceString() {
        String[][] testData = {
                //  Input ID, Expected ID, Expected Type
                { "DE", "DE", "TERRITORY" },  // Normal region
                { "QU", "EU", "GROUPING" },   // Alias to a grouping
                { "DD", "DE", "TERRITORY" },  // Alias to a deprecated region (East Germany) with single preferred value
                { "276", "DE", "TERRITORY" }, // Numeric code for Germany
                { "278", "DE", "TERRITORY" }, // Numeric code for East Germany (Deprecated)
                { "SU", "SU", "DEPRECATED" }, // Alias to a deprecated region with multiple preferred values
                { "AN", "AN", "DEPRECATED" }, // Deprecated region with multiple preferred values
                { "SVK", "SK", "TERRITORY" }  // 3-letter code - Slovakia
        };

        try {
            Region.getInstance(null);
            errln("Calling Region.get(null) should have thrown a NullPointerException, but didn't.");
        } catch ( NullPointerException ex ) {
            // Do nothing - we're supposed to get here.
        }

        try {
            Region.getInstance("BOGUS");
            errln("Calling Region.get(BOGUS) should have thrown a IllegalArgumentException, but didn't.");
        } catch ( IllegalArgumentException ex ) {
            // Do nothing - we're supposed to get here.
        }

        for (String [] data : testData) {
            String inputID = data[0];
            String expectedID = data[1];
            Region.RegionType expectedType = Region.RegionType.valueOf(data[2]);
            Region r = Region.getInstance(inputID);
            if ( !expectedID.equals(r.toString())) {
                errln("Unexpected region ID for Region.getInstance(\"" + inputID + "\"); Expected: " + expectedID + " Got: " + r.toString());
            }
            if ( !expectedType.equals(r.getType())) {
                errln("Unexpected region type for Region.getInstance(\"" + inputID + "\"); Expected: " + expectedType + " Got: " + r.getType());
            }
        }
    }

    @Test
    public void TestGetInstanceInt() {
        String[][] testData = {
                //  Input ID, Expected ID, Expected Type
                { "276", "DE", "TERRITORY" }, // Numeric code for Germany
                { "278", "DE", "TERRITORY" }, // Numeric code for East Germany (Deprecated)
                { "419", "419", "GROUPING" }, // Latin America
                { "736", "SD", "TERRITORY" }, // Sudan (pre-2011) - changed numeric code after South Sudan split off
                { "729", "SD", "TERRITORY" }, // Sudan (post-2011) - changed numeric code after South Sudan split off
        };

        try {
            Region.getInstance(-123);
            errln("Calling Region.get(-123) should have thrown a IllegalArgumentException, but didn't.");
        } catch ( IllegalArgumentException ex ) {
            // Do nothing - we're supposed to get here.
        }
        for (String [] data : testData) {
            String inputID = data[0];
            String expectedID = data[1];
            Region.RegionType expectedType = Region.RegionType.valueOf(data[2]);
            Region r = Region.getInstance(Integer.valueOf(inputID));
            if ( !expectedID.equals(r.toString())) {
                errln("Unexpected region ID for Region.getInstance(" + inputID + "); Expected: " + expectedID + " Got: " + r.toString());
            }
            if ( !expectedType.equals(r.getType())) {
                errln("Unexpected region type for Region.getInstance(" + inputID + "); Expected: " + expectedType + " Got: " + r.getType());
            }
        }

    }

    @Test
    public void TestGetContainedRegions() {        
        for (String [] rd : knownRegions ) {
            try {
                Region r = Region.getInstance(rd[0]);
                if (r.getType() == Region.RegionType.GROUPING) {
                    continue;
                }
                Set<Region> containedRegions = r.getContainedRegions();
                for (Region cr : containedRegions ) {
                    if ( cr.getContainingRegion() != r) {
                        errln("Region: " + r.toString() + " contains region: " + cr.toString() + ". Expected containing region of this region to be the original region, but got: " + 
                                ( cr.getContainingRegion() == null ? "NULL" : cr.getContainingRegion().toString()));
                    }
                }
            } catch (IllegalArgumentException ex ) {
                errln("Known region " + rd[0] + " was not recognized.");
            }
        }
    }

    @Test
    public void TestGetContainedRegionsWithType() {        
        for (String [] rd : knownRegions ) {
            try {
                Region r = Region.getInstance(rd[0]);
                if ( r.getType() != Region.RegionType.CONTINENT ) {
                    continue;
                }
                Set<Region> containedRegions = r.getContainedRegions(Region.RegionType.TERRITORY);
                for (Region cr : containedRegions ) {
                    if ( cr.getContainingRegion(Region.RegionType.CONTINENT) != r) {
                        errln("Continent: " + r.toString() + " contains territory: " + cr.toString() + ". Expected containing continent of this region to be the original continent, but got: " + 
                                ( cr.getContainingRegion() == null ? "NULL" : cr.getContainingRegion().toString()));
                    }
                }
            } catch (IllegalArgumentException ex ) {
                errln("Known region " + rd[0] + " was not recognized.");
            }
        }
    }

    @Test
    public void TestGetContainingRegionWithType() {        
        for (String [] rd : knownRegions ) {
            try {
                Region r = Region.getInstance(rd[0]);
                Region c = r.getContainingRegion(Region.RegionType.CONTINENT);
                if (rd[4] == null) {                   
                    if ( c != null) {
                        errln("Containing continent for " + r.toString() + " should have been NULL.  Got: " + c.toString());
                    }
                } else {
                    Region p = Region.getInstance(rd[4]);                   
                    if ( !p.equals(c)) {
                        errln("Expected containing continent of region " + r.toString() + " to be " + p.toString() + ". Got: " + ( c == null ? "NULL" : c.toString()) );
                    }
                }
            } catch (IllegalArgumentException ex ) {
                errln("Known region " + rd[0] + " was not recognized.");
            }
        }
    }

    @Test
    public void TestGetContainingRegion() {        
        for (String [] rd : knownRegions ) {
            try {
                Region r = Region.getInstance(rd[0]);
                Region c = r.getContainingRegion();
                if (rd[2] == null) {                   
                    if ( c != null) {
                        errln("Containing region for " + r.toString() + " should have been NULL.  Got: " + c.toString());
                    }
                } else {
                    Region p = Region.getInstance(rd[2]);                   
                    if ( !p.equals(c)) {
                        errln("Expected containing region of region " + r.toString() + " to be " + p.toString() + ". Got: " + ( c == null ? "NULL" : c.toString()) );
                    }
                }
            } catch (IllegalArgumentException ex ) {
                errln("Known region " + rd[0] + " was not recognized.");
            }
        }
    }

    @Test
    public void TestGetPreferredValues() {
        String[][] testData = {
                //  Input ID, Expected Preferred Values...
                { "AN", "CW", "SX", "BQ" }, // Netherlands Antilles
                { "CS", "RS", "ME" },     // Serbia & Montenegro
                { "FQ", "AQ", "TF" },     // French Southern and Antarctic Territories
                { "NT", "IQ", "SA" },     // Neutral Zone
                { "PC", "FM", "MH", "MP", "PW" }, // Pacific Islands Trust Territory
                { "SU", "RU", "AM", "AZ", "BY", "EE", "GE", "KZ", "KG", "LV", "LT", "MD", "TJ", "TM", "UA", "UZ" } // Soviet Union
        };

        for (String [] data : testData) {
            String inputID = data[0];
            try {
                Region r = Region.getInstance(inputID);
                List<Region> preferredValues = r.getPreferredValues();
                for ( int i = 1 ; i < data.length ; i++ ) {
                    try {
                        Region pvr = Region.getInstance(data[i]);
                        if ( !preferredValues.contains(pvr)) {
                            errln("Region.getPreferredValues() for region:" + inputID + "should have contained: " + pvr.toString() + "but it didn't.");
                        }
                    } catch (IllegalArgumentException ex ) {
                        errln("Known region " + data[i] + " was not recognized.");
                    }                    
                }
            } catch (IllegalArgumentException ex ) {
                errln("Known region " + inputID + " was not recognized.");
            }
        }
    }

    @Test
    public void TestContains() {        
        for (String [] rd : knownRegions ) {
            try {
                Region r = Region.getInstance(rd[0]);
                Region c = r.getContainingRegion();
                while ( c != null ) {
                    if ( !c.contains(r)) {
                        errln("Region " + c.toString() + "should have contained: " + r.toString() + "but it didn't.");
                    }
                    c = c.getContainingRegion();
                }
            } catch (IllegalArgumentException ex ) {
                errln("Known region " + rd[0] + " was not recognized.");
            }
        }
    }

    @Test
    public void TestAvailableTerritories() {
        // Test to make sure that the set of territories contained in World and the set of all available
        // territories are one and the same.
        Set<Region> availableTerritories = Region.getAvailable(RegionType.TERRITORY);
        Region world = Region.getInstance("001");
        Set<Region> containedInWorld = world.getContainedRegions(RegionType.TERRITORY);
        if ( !availableTerritories.equals(containedInWorld) ) {
            errln("Available territories and all territories contained in world should be the same set.\n" +
                    "Available          = " + availableTerritories.toString() + "\n" +
                    "Contained in World = " + containedInWorld.toString());
        }
    }
}
