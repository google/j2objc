/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
 
package android.icu.dev.test.collator;

import java.util.Locale;
import java.util.Random;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;


/**
 * 
 * CollationCreationMethodTest checks to ensure that the collators act the same whether they are created by choosing a 
 * locale and loading the data from file, or by using rules.
 * 
 * @author Brian Rower - IBM - August 2008
 *
 */
public class CollationCreationMethodTest extends TestFmwk 
{
    @Test
    public void TestRuleVsLocaleCreationMonkey()
    {
        //create a RBC from a collator reader by reading in a locale collation file
        //also create one simply from a rules string (which should be 
        //pulled from the locale collation file)
        //and then do crazy monkey testing on it to make sure they are the same.
        int x,y,z;
        Random r = createRandom();
        String randString1;
        CollationKey key1;
        CollationKey key2;


        Locale[] locales = Collator.getAvailableLocales();

        RuleBasedCollator localeCollator;
        RuleBasedCollator ruleCollator;

        for(z = 0; z < 60; z++)
        {
            x = r.nextInt(locales.length);
            Locale locale = locales[x];

            try
            {
                //this is making the assumption that the only type of collator that will be made is RBC
                localeCollator = (RuleBasedCollator)Collator.getInstance(locale);
                logln("Rules for " + locale + " are: " + localeCollator.getRules());
                ruleCollator = new RuleBasedCollator(localeCollator.getRules());
            } 
            catch (Exception e) 
            {
                warnln("ERROR: in creation of collator of locale " + locale.getDisplayName() + ": " + e);
                return;
            }

            //do it several times for each collator
            int n = 3;
            for(y = 0; y < n; y++)
            {

                randString1 = generateNewString(r);

                key1 = localeCollator.getCollationKey(randString1);
                key2 = ruleCollator.getCollationKey(randString1);
               
                report(locale.getDisplayName(), randString1, key1, key2);
            }
        }
    }

    private String generateNewString(Random r)
    {
        int maxCodePoints = 40;
        byte[] c = new byte[r.nextInt(maxCodePoints)*2]; //two bytes for each code point
        int x;
        int z;
        String s = "";

        for(x = 0; x < c.length/2; x = x + 2) //once around for each UTF-16 character
        {
            z = r.nextInt(0x7fff); //the code point...

            c[x + 1] = (byte)z;
            c[x] = (byte)(z >>> 4);
        }
        try
        {
            s = new String(c, "UTF-16BE");
        }
        catch(Exception e)
        {
            warnln("Error creating random strings");
        }
        return s;
    }

    private void report(String localeName, String string1, CollationKey k1, CollationKey k2) 
    {
        if (!k1.equals(k2)) 
        {
            StringBuilder msg = new StringBuilder();
            msg.append("With ").append(localeName).append(" collator\n and input string: ").append(string1).append('\n');
            msg.append(" failed to produce identical keys on both collators\n");
            msg.append("  localeCollator key: ").append(CollationTest.prettify(k1)).append('\n');
            msg.append("  ruleCollator   key: ").append(CollationTest.prettify(k2)).append('\n');
            // Android patch: Add --omitCollationRules to genrb.
            logln(msg.toString());
            // Android patch end.
        }
    }
}
