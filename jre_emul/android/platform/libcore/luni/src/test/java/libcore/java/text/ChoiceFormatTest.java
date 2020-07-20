package libcore.java.text;

import java.text.ChoiceFormat;
import junit.framework.TestCase;

/**
 */
public class ChoiceFormatTest extends TestCase {

    /**
     * Limits for {@link ChoiceFormat}, will be modified by some tests to ensure that ChoiceFormat
     * stores a copy of the arrays provided.
     */
    private final double[] limits = new double[] { 0, 1, 2, 3, 4 };

    /**
     * Format strings for {@link ChoiceFormat}, will be modified by some tests to ensure that
     * ChoiceFormat stores a copy of the arrays provided.
     */
    private final String[] formats = new String[] { "zero", "one", "a couple", "a few", "some" };

    public void testConstructor_doubleArray_StringArray() throws Exception {
        ChoiceFormat format = new ChoiceFormat(limits, formats);

        verifyChoiceFormatCopiesSuppliedArrays(format);
    }

    public void testSetChoices() throws Exception {
        ChoiceFormat format = new ChoiceFormat(new double[] { 0 }, new String[] { "" });
        assertEquals("", format.format(1.4));

        // Change the limits.
        format.setChoices(limits, formats);

        verifyChoiceFormatCopiesSuppliedArrays(format);
    }

    private void verifyChoiceFormatCopiesSuppliedArrays(ChoiceFormat format) {
        assertEquals("one", format.format(1.4));

        // Change the formats array and make sure that it doesn't affect the ChoiceFormat.
        formats[1] = "uno";
        assertEquals("ChoiceFormat doesn't make defensive copies of formats array",
                "one", format.format(1.4));

        // Change the limits array and make sure that it doesn't affect the ChoiceFormat.
        limits[2] = 1.2;
        assertEquals("ChoiceFormat doesn't make defensive copies of limits array",
                "one", format.format(1.4));
    }

    public void testGetLimits() throws Exception {
        ChoiceFormat format = new ChoiceFormat(limits, formats);
        assertEquals("some", format.format(100));

        // Get the limits array, change the contents and make sure it doesn't affect the behavior
        // of the format.
        double[] copiedLimits = format.getLimits();
        copiedLimits[4] = 200;
        assertEquals("ChoiceFormat doesn't return a copy of choiceLimits array",
                "some", format.format(100));
    }

    public void testGetFormats() throws Exception {
        ChoiceFormat format = new ChoiceFormat(limits, formats);
        assertEquals("zero", format.format(-4));

        // Get the formats array, change the contents and make sure it doesn't affect the behavior
        // of the format.
        Object[] copiedFormats = format.getFormats();
        copiedFormats[0] = "none or less";
        assertEquals("ChoiceFormat doesn't return a copy of choiceFormats array",
                "zero", format.format(-4));
    }
}
