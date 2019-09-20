/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License

package android.icu.dev.test.bidi;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.lang.UCharacter;
import android.icu.text.ArabicShaping;
import android.icu.text.Bidi;
import android.icu.text.BidiTransform;
import android.icu.text.BidiTransform.Mirroring;
import android.icu.text.BidiTransform.Order;

/**
 * Verify Bidi Layout Transformations
 *
 * @author Lina Kemmel
 *
 */
public class TestBidiTransform extends TestFmwk {

    static final char LATN_ZERO         = '\u0030';
    static final char ARAB_ZERO         = '\u0660';
    static final char MIN_HEB_LETTER    = '\u05d0';
    static final char MIN_ARAB_LETTER   = '\u0630'; // relevant to this test only
    static final char MIN_SHAPED_LETTER = '\ufeab'; // relevant to this test only


    private BidiTransform bidiTransform;
    private Bidi bidi;

    public TestBidiTransform() {}

    @Test
    public void testBidiTransform() {
        logln("\nEntering TestBidiTransform\n");

        bidi = new Bidi();
        bidiTransform = new BidiTransform();

        autoDirectionTest();
        allTransformOptionsTest();

        logln("\nExiting TestBidiTransform\n");
    }

    /**
     * Tests various combinations of base directions, with the input either
     * <code>Bidi.LEVEL_DEFAULT_LTR</code> or
     * <code>Bidi.LEVEL_DEFAULT_LTR</code>, and the output either
     * <code>Bidi.LEVEL_LTR</code> or <code>Bidi.LEVEL_RTL</code>. Order is
     * always <code>Order.LOGICAL</code> for the input and
     * <code>Order.VISUAL</code> for the output.
     */
    private void autoDirectionTest() {
        final String[] inTexts = {
            "abc \u05d0\u05d1",
            "... abc \u05d0\u05d1",
            "\u05d0\u05d1 abc",
            "... \u05d0\u05d1 abc",
            ".*^"
        };
        final byte[] inLevels = {
                Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_RTL
        };
        final byte[] outLevels = {
            Bidi.LTR, Bidi.RTL
        };
        logln("\nEntering autoDirectionTest\n");

        for (String inText : inTexts) {
            for (byte inLevel : inLevels) {
                for (byte outLevel : outLevels) {
                    String outText = bidiTransform.transform(inText, inLevel, Order.LOGICAL,
                            outLevel, Order.VISUAL, Mirroring.OFF, 0);
                    bidi.setPara(inText, inLevel, null);
                    String expectedText = bidi.writeReordered(Bidi.REORDER_DEFAULT);
                    if ((outLevel & 1) != 0) {
                        expectedText = Bidi.writeReverse(expectedText, Bidi.OUTPUT_REVERSE);
                    }
                    logResultsForDir(inText, outText, expectedText, inLevel, outLevel);
                }
            }
        }
        logln("\nExiting autoDirectionTest\n");
    }

    /**
     * This method covers:
     * <ul>
     * <li>all possible combinations of ordering schemes and <strong>explicit</strong>
     * base levels, applied to both input and output,</li>
     * <li>selected tests for auto direction (systematically, auto direction is
     * covered in a dedicated test) applied on both input and output,</li>
     * <li>all possible combinations of mirroring, digits and letters applied
     * to output only.</li>
     * </ul>
     */
    private void allTransformOptionsTest() {
        final String inText = "a[b]c \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 23\u0660 e\u0631456 f \ufeaf \u0661\u0662";

        final Object[][] testCases = {
            { Bidi.LTR, Order.LOGICAL, Bidi.LTR, Order.LOGICAL,
                    inText, // reordering without mirroring
                    "a[b]c \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 d \u0630 23\u0660 e\u0631456 f \ufeaf \u0661\u0662", // mirroring
                    "a[b]c \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u0662\u0663\u0660 e\u0631\u0664\u0665\u0666 f \ufeaf \u0661\u0662", // context digit shaping
                    "1: Logical LTR ==> Logical LTR" }, // message
            { Bidi.LTR, Order.LOGICAL, Bidi.LTR, Order.VISUAL,
                    "a[b]c 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 d 23\u0660 \u0630 e456\u0631 f \u0661\u0662 \ufeaf",
                    "a[b]c 1 \u05d4(\u05d3 \u05d2\u05d1)\u05d0 d 23\u0660 \u0630 e456\u0631 f \u0661\u0662 \ufeaf",
                    "a[b]c 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 d \u0662\u0663\u0660 \u0630 e\u0664\u0665\u0666\u0631 f \u0661\u0662 \ufeaf",
                    "2: Logical LTR ==> Visual LTR" },
            { Bidi.LTR, Order.LOGICAL, Bidi.RTL, Order.LOGICAL,
                    "\ufeaf \u0661\u0662 f \u0631e456 \u0630 23\u0660 d \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 a[b]c",
                    "\ufeaf \u0661\u0662 f \u0631e456 \u0630 23\u0660 d \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 a[b]c",
                    "\ufeaf \u0661\u0662 f \u0631e\u0664\u0665\u0666 \u0630 \u0662\u0663\u0660 d \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 a[b]c",
                    "3: Logical LTR ==> Logical RTL" },
            { Bidi.LTR, Order.LOGICAL, Bidi.RTL, Order.VISUAL,
                    "\ufeaf \u0662\u0661 f \u0631654e \u0630 \u066032 d \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 c]b[a",
                    "\ufeaf \u0662\u0661 f \u0631654e \u0630 \u066032 d \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 c]b[a",
                    "\ufeaf \u0662\u0661 f \u0631\u0666\u0665\u0664e \u0630 \u0660\u0663\u0662 d \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 c]b[a",
                    "4: Logical LTR ==> Visual RTL" },

            { Bidi.RTL, Order.LOGICAL, Bidi.RTL, Order.LOGICAL, inText,
                    "a[b]c \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 d \u0630 23\u0660 e\u0631456 f \ufeaf \u0661\u0662",
                    "a[b]c \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 23\u0660 e\u0631456 f \ufeaf \u0661\u0662",
                    "5: Logical RTL ==> Logical RTL" },
            { Bidi.RTL, Order.LOGICAL, Bidi.RTL, Order.VISUAL,
                    "c]b[a \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "c]b[a \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "c]b[a \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "6: Logical RTL ==> Visual RTL" },
            { Bidi.RTL, Order.LOGICAL, Bidi.LTR, Order.LOGICAL,
                    "\ufeaf \u0661\u0662 f 456\u0631e 23\u0630 \u0660 d 1 \u05d0(\u05d1\u05d2 \u05d3)\u05d4 a[b]c",
                    "\ufeaf \u0661\u0662 f 456\u0631e 23\u0630 \u0660 d 1 \u05d0)\u05d1\u05d2 \u05d3(\u05d4 a[b]c",
                    "\ufeaf \u0661\u0662 f 456\u0631e 23\u0630 \u0660 d 1 \u05d0(\u05d1\u05d2 \u05d3)\u05d4 a[b]c",
                    "7: Logical RTL ==> Logical LTR" },
            { Bidi.RTL, Order.LOGICAL, Bidi.LTR, Order.VISUAL,
                    "\u0661\u0662 \ufeaf f 456\u0631e 23\u0660 \u0630 d 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 a[b]c",
                    "\u0661\u0662 \ufeaf f 456\u0631e 23\u0660 \u0630 d 1 \u05d4(\u05d3 \u05d2\u05d1)\u05d0 a[b]c",
                    "\u0661\u0662 \ufeaf f 456\u0631e 23\u0660 \u0630 d 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 a[b]c",
                    "8: Logical RTL ==> Visual LTR" },

            { Bidi.LTR, Order.VISUAL, Bidi.LTR, Order.VISUAL, inText,
                    "a[b]c \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 d \u0630 23\u0660 e\u0631456 f \ufeaf \u0661\u0662",
                    "a[b]c \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u0662\u0663\u0660 e\u0631\u0664\u0665\u0666 f \ufeaf \u0661\u0662",
                    "9: Visual LTR ==> Visual LTR" },
            { Bidi.LTR, Order.VISUAL, Bidi.LTR, Order.LOGICAL,
                    "a[b]c 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 d 23\u0660 \u0630 e456\u0631 f \u0661\u0662 \ufeaf",
                    "a[b]c 1 \u05d4(\u05d3 \u05d2\u05d1)\u05d0 d 23\u0660 \u0630 e456\u0631 f \u0661\u0662 \ufeaf",
                    "a[b]c 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 d 23\u0660 \u0630 e456\u0631 f \u0661\u0662 \ufeaf",
                    "10: Visual LTR ==> Logical LTR" },
            { Bidi.LTR, Order.VISUAL, Bidi.RTL, Order.VISUAL,
                    "\u0662\u0661 \ufeaf f 654\u0631e \u066032 \u0630 d 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 c]b[a",
                    "\u0662\u0661 \ufeaf f 654\u0631e \u066032 \u0630 d 1 \u05d4(\u05d3 \u05d2\u05d1)\u05d0 c]b[a",
                    "\u0662\u0661 \ufeaf f \u0666\u0665\u0664\u0631e \u0660\u0663\u0662 \u0630 d 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 c]b[a",
                    "11: Visual LTR ==> Visual RTL" },
            { Bidi.LTR, Order.VISUAL, Bidi.RTL, Order.LOGICAL,
                    "\u0661\u0662 \ufeaf f 456\u0631e 23\u0660 \u0630 d 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 a[b]c",
                    "\u0661\u0662 \ufeaf f 456\u0631e 23\u0660 \u0630 d 1 \u05d4(\u05d3 \u05d2\u05d1)\u05d0 a[b]c",
                    "\u0661\u0662 \ufeaf f \u0664\u0665\u0666\u0631e \u0662\u0663\u0660 \u0630 d 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 a[b]c",
                    "12: Visual LTR ==> Logical RTL" },

            { Bidi.RTL, Order.VISUAL, Bidi.RTL, Order.VISUAL, inText,
                    "a[b]c \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 d \u0630 23\u0660 e\u0631456 f \ufeaf \u0661\u0662",
                    "a[b]c \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 23\u0660 e\u0631456 f \ufeaf \u0661\u0662",
                    "13: Visual RTL ==> Visual RTL" },
            { Bidi.RTL, Order.VISUAL, Bidi.RTL, Order.LOGICAL,
                    "c]b[a \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "c]b[a \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "c]b[a \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "14: Visual RTL ==> Logical RTL" },
            { Bidi.RTL, Order.VISUAL, Bidi.LTR, Order.VISUAL,
                    "\u0662\u0661 \ufeaf f 654\u0631e \u066032 \u0630 d 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 c]b[a",
                    "\u0662\u0661 \ufeaf f 654\u0631e \u066032 \u0630 d 1 \u05d4(\u05d3 \u05d2\u05d1)\u05d0 c]b[a",
                    "\u0662\u0661 \ufeaf f 654\u0631e \u066032 \u0630 d 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 c]b[a",
                    "15: Visual RTL ==> Visual LTR" },
            { Bidi.RTL, Order.VISUAL, Bidi.LTR, Order.LOGICAL,
                    "\ufeaf \u0662\u0661 f 654\u0631e \u066032 \u0630 d 1 \u05d0(\u05d1\u05d2 \u05d3)\u05d4 c]b[a",
                    "\ufeaf \u0662\u0661 f 654\u0631e \u066032 \u0630 d 1 \u05d0)\u05d1\u05d2 \u05d3(\u05d4 c]b[a",
                    "\ufeaf \u0662\u0661 f 654\u0631e \u066032 \u0630 d 1 \u05d0(\u05d1\u05d2 \u05d3)\u05d4 c]b[a",
                    "16: Visual RTL ==> Logical LTR" },

            { Bidi.LEVEL_DEFAULT_RTL, Order.LOGICAL, Bidi.LTR, Order.VISUAL,
                    "a[b]c 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 d 23\u0660 \u0630 e456\u0631 f \u0661\u0662 \ufeaf",
                    "a[b]c 1 \u05d4(\u05d3 \u05d2\u05d1)\u05d0 d 23\u0660 \u0630 e456\u0631 f \u0661\u0662 \ufeaf",
                    "a[b]c 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 d \u0662\u0663\u0660 \u0630 e\u0664\u0665\u0666\u0631 f \u0661\u0662 \ufeaf",
                    "17: Logical DEFAULT_RTL ==> Visual LTR" },
            { Bidi.RTL, Order.LOGICAL, Bidi.LEVEL_DEFAULT_LTR, Order.VISUAL,
                    "c]b[a \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "c]b[a \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "c]b[a \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "18: Logical RTL ==> Visual DEFAULT_LTR" },
            { Bidi.LEVEL_DEFAULT_LTR, Order.LOGICAL, Bidi.LTR, Order.VISUAL,
                    "a[b]c 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 d 23\u0660 \u0630 e456\u0631 f \u0661\u0662 \ufeaf",
                    "a[b]c 1 \u05d4(\u05d3 \u05d2\u05d1)\u05d0 d 23\u0660 \u0630 e456\u0631 f \u0661\u0662 \ufeaf",
                    "a[b]c 1 \u05d4)\u05d3 \u05d2\u05d1(\u05d0 d \u0662\u0663\u0660 \u0630 e\u0664\u0665\u0666\u0631 f \u0661\u0662 \ufeaf",
                    "19: Logical DEFAULT_LTR ==> Visual LTR" },
            { Bidi.RTL, Order.LOGICAL, Bidi.LEVEL_DEFAULT_RTL, Order.VISUAL,
                    "c]b[a \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "c]b[a \u05d0)\u05d1\u05d2 \u05d3(\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "c]b[a \u05d0(\u05d1\u05d2 \u05d3)\u05d4 1 d \u0630 \u066032 e\u0631654 f \ufeaf \u0662\u0661",
                    "20: Logical RTL ==> Visual DEFAULT_RTL" },
        };

        final int[] digits = {
                ArabicShaping.DIGITS_NOOP, ArabicShaping.DIGITS_EN2AN, ArabicShaping.DIGITS_AN2EN, ArabicShaping.DIGITS_EN2AN_INIT_AL
        };
        final int[] letters = {
                ArabicShaping.LETTERS_NOOP, ArabicShaping.LETTERS_SHAPE, ArabicShaping.LETTERS_UNSHAPE
        };

        char[] expectedChars;

        logln("\nEntering allTransformOptionsTest\n");

        // Test various combinations of base level, order, mirroring, digits and letters
        for (Object[] test : testCases) {
            expectedChars = ((String)test[5]).toCharArray();
            verifyResultsForAllOpts(test, inText, bidiTransform.transform(inText, (Byte)test[0], (Order)test[1],
                    (Byte)test[2], (Order)test[3], Mirroring.ON, 0), expectedChars, 0, 0);

            for (int digit : digits) {
                expectedChars = ((String)(digit == ArabicShaping.DIGITS_EN2AN_INIT_AL ? test[6] : test[4]))
                        .toCharArray();
                for (int letter : letters) {
                    verifyResultsForAllOpts(test, inText, bidiTransform.transform(inText, (Byte)test[0],
                            (Order)test[1], (Byte)test[2], (Order)test[3], Mirroring.OFF, digit | letter),
                            expectedChars, digit, letter);
                }
            }
        }
        logln("\nExiting allTransformOptionsTest\n");
    }

    private void logResultsForDir(String inText, String outText, String expected,
            byte inLevel, byte outLevel) {

        assertEquals("inLevel: " + inLevel + ", outLevel: " + outLevel
                /* TODO: BidiFwk#u16ToPseudo isn't good for us, needs an update to be used here */
                + "\ninText:   " + pseudoScript(inText) + "\noutText:  " + pseudoScript(outText)
                + "\nexpected: " + pseudoScript(expected) + "\n", expected, outText);
    }

    private void verifyResultsForAllOpts(Object[] test, String inText, String outText, char[] expectedChars, int digits, int letters) {
        switch (digits) {
            case ArabicShaping.DIGITS_AN2EN:
                shapeDigits(expectedChars, ARAB_ZERO, LATN_ZERO);
                break;
            case ArabicShaping.DIGITS_EN2AN:
                shapeDigits(expectedChars, LATN_ZERO, ARAB_ZERO);
                break;
            default:
                break;
        }
        switch (letters) {
            case ArabicShaping.LETTERS_SHAPE:
                shapeLetters(expectedChars, 0);
                break;
            case ArabicShaping.LETTERS_UNSHAPE:
                shapeLetters(expectedChars, 1);
                break;
            default:
                break;
        }
        String expected = new String(expectedChars);
        assertEquals("\nTest " + test[7] + "\ndigits: " + digits + ", letters: " + letters
                /* TODO: BidiFwk#u16ToPseudo isn't good for us, needs an update to be used here */
                + "\ninText:   " + pseudoScript(inText) + "\noutText:  " + pseudoScript(outText)
                + "\nexpected: " + pseudoScript(expected) + "\n", expected, outText);
    }

    /*
     * Using the following conventions:
     * AL unshaped: A-E
     * AL shaped: F-J
     * R:  K-Z
     * EN: 0-4
     * AN: 5-9
    */
    private static char substituteChar(char uch, char baseReal,
               char basePseudo, char max) {
        char dest = (char)(basePseudo + (uch - baseReal));
        return dest > max ? max : dest;
    }

    private static String pseudoScript(String text) {
        char[] uchars = text.toCharArray();
        for (int i = uchars.length; i-- > 0;) {
            char uch = uchars[i];
            switch (UCharacter.getDirectionality(uch)) {
                case UCharacter.RIGHT_TO_LEFT:
                    uchars[i] = substituteChar(uch, MIN_HEB_LETTER, 'K', 'Z');
                    break;
                case UCharacter.RIGHT_TO_LEFT_ARABIC:
                    if (uch > 0xFE00) {
                        uchars[i] = substituteChar(uch, MIN_SHAPED_LETTER, 'F', 'J');
                    } else {
                        uchars[i] = substituteChar(uch, MIN_ARAB_LETTER, 'A', 'E');
                    }
                    break;
                case UCharacter.ARABIC_NUMBER:
                    uchars[i] = substituteChar(uch, ARAB_ZERO, '5', '9');
                    break;
                default:
                    break;
            }
        }
        return new String(uchars);
    }

    private static void shapeDigits(char[] chars, char srcZero, char destZero) {
        for (int i = chars.length; i-- > 0;) {
            if (chars[i] >= srcZero && chars[i] <= srcZero + 9) {
                chars[i] = substituteChar(chars[i], srcZero, destZero, (char)(destZero + 9));
            }
        }
    }

    /*
     * TODO: the goal is not to thoroughly test ArabicShaping, so the test can be quite trivial,
     * but maybe still more sophisticated?
     */
    private static final String letters = "\u0630\ufeab\u0631\ufead\u0632\ufeaf";

    private static void shapeLetters(char[] chars, int indexParity) {
        for (int i = chars.length; i-- > 0;) {
            int index = letters.indexOf(chars[i]);
            if (index >= 0 && (index & 1) == indexParity) {
                chars[i] = letters.charAt(index ^ 1);
            }
        }
    }
}
