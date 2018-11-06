/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.NumberFormat;
import android.icu.util.Currency;
import android.icu.util.ULocale;

/**
 * @author rocketman
 *
 */
public class NumberFormatSpecificationTest extends TestFmwk {
    @Test
    public void TestBasicPatterns() {
        double num = 1234.567;
        assertEquals("", "1 234,57", formatFrWithPattern(num, "#,##0.##"));
        assertEquals("", "1234,57", formatFrWithPattern(num, "0.##"));
        assertEquals("", "1235", formatFrWithPattern(num, "0"));
        assertEquals("", "1 234,567", formatFrWithPattern(num, "#,##0.###"));
        assertEquals("", "1234,567", formatFrWithPattern(num, "###0.#####"));
        assertEquals("", "1234,5670", formatFrWithPattern(num, "###0.0000#"));
        assertEquals("", "01234,5670", formatFrWithPattern(num, "00000.0000"));
        assertEquals("", "1 234,57 €", formatFrWithPattern(num, "#,##0.00 ¤"));  
    }
    
    @Test
    public void TestNfSetters() {
        NumberFormat nf = nfWithPattern("#,##0.##");
        nf.setMaximumIntegerDigits(5);
        nf.setMinimumIntegerDigits(4);
        assertEquals("", "34 567,89", format(1234567.89, nf));
        assertEquals("", "0 034,56", format(34.56, nf));
        
    }
    
    @Test
    public void TestRounding() {
        assertEquals("", "1,0", formatFrWithPattern(1.25, "0.5"));
        assertEquals("", "2,0", formatFrWithPattern(1.75, "0.5"));
        assertEquals("", "-1,0", formatFrWithPattern(-1.25, "0.5"));
        assertEquals("", "-02,0", formatFrWithPattern(-1.75, "00.5"));
        assertEquals("", "0", formatFrWithPattern(2.0, "4"));
        assertEquals("", "8", formatFrWithPattern(6.0, "4"));
        assertEquals("", "8", formatFrWithPattern(10.0, "4"));
        assertEquals("", "99,90", formatFrWithPattern(99.0, "2.70"));
        assertEquals("", "273,00", formatFrWithPattern(272.0, "2.73"));
        assertEquals("", "1 03,60", formatFrWithPattern(104.0, "#,#3.70"));
    }
    
    @Test
    public void TestSignificantDigits() {
        assertEquals("", "1230", formatFrWithPattern(1234.0, "@@@"));
        assertEquals("", "1 234", formatFrWithPattern(1234.0, "@,@@@"));
        assertEquals("", "1 235 000", formatFrWithPattern(1234567.0, "@,@@@"));
        assertEquals("", "1 234 567", formatFrWithPattern(1234567.0, "@@@@,@@@"));
        assertEquals("", "12 34 567,00", formatFrWithPattern(1234567.0, "@@@@,@@,@@@"));
        assertEquals("", "12 34 567,0", formatFrWithPattern(1234567.0, "@@@@,@@,@@#"));
        assertEquals("", "12 34 567", formatFrWithPattern(1234567.0, "@@@@,@@,@##"));
        assertEquals("", "12 34 567", formatFrWithPattern(1234567.001, "@@@@,@@,@##"));
        assertEquals("", "12 34 567", formatFrWithPattern(1234567.001, "@@@@,@@,###"));
        assertEquals("", "1 200", formatFrWithPattern(1234.0, "#,#@@"));       
    }
    
    @Test
    public void TestScientificNotation() {
        assertEquals("", "1,23E4", formatFrWithPattern(12345.0, "0.00E0"));
        assertEquals("", "123,00E2", formatFrWithPattern(12300.0, "000.00E0"));
        assertEquals("", "123,0E2", formatFrWithPattern(12300.0, "000.0#E0"));
        assertEquals("", "123,0E2", formatFrWithPattern(12300.1, "000.0#E0"));
        assertEquals("", "123,01E2", formatFrWithPattern(12301.0, "000.0#E0"));
        assertEquals("", "123,01E+02", formatFrWithPattern(12301.0, "000.0#E+00"));
        assertEquals("", "12,3E3", formatFrWithPattern(12345.0, "##0.00E0"));
        assertEquals("", "12,300E3", formatFrWithPattern(12300.1, "##0.0000E0"));
        assertEquals("", "12,30E3", formatFrWithPattern(12300.1, "##0.000#E0"));
        assertEquals("", "12,301E3", formatFrWithPattern(12301.0, "##0.000#E0"));
        if (!logKnownIssue("11020", "Rounding does not work with scientific notation.")) {
            assertEquals("", "170,0E-3", formatFrWithPattern(0.17, "##0.000#E0"));
        }
    }
    
    @Test
    public void TestPercent() {
        assertEquals("", "57,3%", formatFrWithPattern(0.573, "0.0%"));
        assertEquals("", "%57,3", formatFrWithPattern(0.573, "%0.0"));
        assertEquals("", "p%p57,3", formatFrWithPattern(0.573, "p%p0.0"));
        assertEquals("", "p%p0,6", formatFrWithPattern(0.573, "p'%'p0.0"));
        assertEquals("", "%3,260", formatFrWithPattern(0.0326, "%@@@@"));
        assertEquals("", "%1 540", formatFrWithPattern(15.43, "%#,@@@"));
        assertEquals("", "%1 656,4", formatFrWithPattern(16.55, "%#,##4.1"));
        assertEquals("", "%16,3E3", formatFrWithPattern(162.55, "%##0.00E0"));  
    }
    
    @Test
    public void TestPerMilli() {
        assertEquals("", "573,0‰", formatFrWithPattern(0.573, "0.0‰"));
        assertEquals("", "‰573,0", formatFrWithPattern(0.573, "‰0.0"));
        assertEquals("", "p‰p573,0", formatFrWithPattern(0.573, "p‰p0.0"));
        assertEquals("", "p‰p0,6", formatFrWithPattern(0.573, "p'‰'p0.0"));
        assertEquals("", "‰32,60", formatFrWithPattern(0.0326, "‰@@@@"));
        assertEquals("", "‰15 400", formatFrWithPattern(15.43, "‰#,@@@"));
        assertEquals("", "‰16 551,7", formatFrWithPattern(16.55, "‰#,##4.1"));
        assertEquals("", "‰163E3", formatFrWithPattern(162.55, "‰##0.00E0"));
    }
    
    @Test
    public void TestPadding() {
        assertEquals("", "$***1 234", formatFrWithPattern(1234, "$**####,##0"));
        assertEquals("", "xxx$1 234", formatFrWithPattern(1234, "*x$####,##0"));
        assertEquals("", "1 234xxx$", formatFrWithPattern(1234, "####,##0*x$"));
        assertEquals("", "1 234$xxx", formatFrWithPattern(1234, "####,##0$*x"));
        assertEquals("", "ne1 234nx", formatFrWithPattern(-1234, "####,##0$*x;ne#n"));
        assertEquals("", "n1 234*xx", formatFrWithPattern(-1234, "####,##0$*x;n#'*'"));
        assertEquals("", "yyyy%432,6", formatFrWithPattern(4.33, "*y%4.2######"));
        if (!logKnownIssue("11025", "Padding broken when used with currencies")) {
            assertEquals("", "EUR *433,00", formatFrWithPattern(433.0, "¤¤ **####0.00"));
            assertEquals("", "EUR *433,00", formatFrWithPattern(433.0, "¤¤ **#######0"));
        }
        {
            DecimalFormatSymbols sym = new DecimalFormatSymbols(ULocale.FRANCE);
            DecimalFormat fmt = new DecimalFormat("¤¤ **#######0", sym);
            fmt.setCurrency(Currency.getInstance("JPY"));
            if (!logKnownIssue("11025", "Padding broken when used with currencies")) {
                assertEquals("", "JPY ****433", fmt.format(433.22)); 
            }
        }
        {
            DecimalFormatSymbols sym = new DecimalFormatSymbols(ULocale.US);
            DecimalFormat fmt = new DecimalFormat("¤¤ **#######0;¤¤ (#)", sym);
            assertEquals("", "USD (433.22)", fmt.format(-433.22));   
        }
        assertEquals("", "QU***43,3E-1", formatFrWithPattern(4.33, "QU**00.#####E0"));
        {
            DecimalFormatSymbols sym = new DecimalFormatSymbols(ULocale.FRANCE);
            sym.setExponentSeparator("EE");
            DecimalFormat fmt = new DecimalFormat("QU**00.#####E0", sym);
            assertEquals("", "QU**43,3EE-1", fmt.format(4.33));   
        }
        // padding cannot work as intended with scientific notation.
        assertEquals("", "QU**43,32E-1", formatFrWithPattern(4.332, "QU**00.#####E0"));
    }
    
    private static String formatFrWithPattern(double d, String pattern) {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(ULocale.FRANCE);
        DecimalFormat fmt = new DecimalFormat(pattern, sym);
        return fmt.format(d).replace('\u00a0', ' ');
    }
    
    private static NumberFormat nfWithPattern(String pattern) {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(ULocale.FRANCE);
        return new DecimalFormat(pattern, sym);
    }

    private static String format(double d, NumberFormat nf) {
        return nf.format(d).replace('\u00a0', ' ');
    }
   

}
