/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License

package android.icu.text;

import android.icu.lang.UCharacter;

/**
 * Bidi Layout Transformation Engine.
 *
 * @author Lina Kemmel
 *
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public class BidiTransform
{
    /**
     * <code>{@link Order}</code> indicates the order of text.
     * <p>
     * This bidi transformation engine supports all possible combinations (4 in
     * total) of input and output text order:
     * <ul>
     * <li>{logical input, visual output}: unless the output direction is RTL,
     * this corresponds to a normal operation of the Bidi algorithm as
     * described in the Unicode Technical Report and implemented by
     * <code>{@link Bidi}</code> when the reordering mode is set to
     * <code>Bidi#REORDER_DEFAULT</code>. Visual RTL mode is not supported by
     * <code>{@link Bidi}</code> and is accomplished through reversing a visual
     * LTR string,</li>
     * <li>{visual input, logical output}: unless the input direction is RTL,
     * this corresponds to an "inverse bidi algorithm" in
     * <code>{@link Bidi}</code> with the reordering mode set to
     * <code>{@link Bidi#REORDER_INVERSE_LIKE_DIRECT}</code>. Visual RTL mode
     * is not not supported by <code>{@link Bidi}</code> and is accomplished
     * through reversing a visual LTR string,</li>
     * <li>{logical input, logical output}: if the input and output base
     * directions mismatch, this corresponds to the <code>{@link Bidi}</code>
     * implementation with the reordering mode set to
     * <code>{@link Bidi#REORDER_RUNS_ONLY}</code>; and if the input and output
     * base directions are identical, the transformation engine will only
     * handle character mirroring and Arabic shaping operations without
     * reordering,</li>
     * <li>{visual input, visual output}: this reordering mode is not supported
     * by the <code>{@link Bidi}</code> engine; it implies character mirroring,
     * Arabic shaping, and - if the input/output base directions mismatch -
     * string reverse operations.</li>
     * </ul>
     *
     * @see Bidi#setInverse
     * @see Bidi#setReorderingMode
     * @see Bidi#REORDER_DEFAULT
     * @see Bidi#REORDER_INVERSE_LIKE_DIRECT
     * @see Bidi#REORDER_RUNS_ONLY
     * @hide draft / provisional / internal are hidden on Android
     */
    public enum Order {
        /**
         * Constant indicating a logical order.
         *
         * @hide draft / provisional / internal are hidden on Android
         */
        LOGICAL,
        /**
         * Constant indicating a visual order.
         *
         * @hide draft / provisional / internal are hidden on Android
         */
        VISUAL;
    }

    /**
     * <code>{@link Mirroring}</code> indicates whether or not characters with
     * the "mirrored" property in RTL runs should be replaced with their
     * mirror-image counterparts.
     *
     * @see Bidi#DO_MIRRORING
     * @see Bidi#setReorderingOptions
     * @see Bidi#writeReordered
     * @see Bidi#writeReverse
     * @hide draft / provisional / internal are hidden on Android
     */
    public enum Mirroring {
        /**
         * Constant indicating that character mirroring should not be
         * performed.
         *
         * @hide draft / provisional / internal are hidden on Android
         */
        OFF,
        /**
         * Constant indicating that character mirroring should be performed.
         * <p>
         * This corresponds to calling <code>{@link Bidi#writeReordered}</code>
         * or <code>{@link Bidi#writeReverse}</code> with the
         * <code>{@link Bidi#DO_MIRRORING}</code> option bit set.
         *
         * @hide draft / provisional / internal are hidden on Android
         */
        ON;
    }

    private Bidi bidi;
    private String text;
    private int reorderingOptions;
    private int shapingOptions;

    /**
     * <code>{@link BidiTransform}</code> default constructor.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public BidiTransform()
    {
    }

    /**
     * Performs transformation of text from the bidi layout defined by the
     * input ordering scheme to the bidi layout defined by the output ordering
     * scheme, and applies character mirroring and Arabic shaping operations.
     * <p>
     * In terms of <code>{@link Bidi}</code> class, such a transformation
     * implies:
     * <ul>
     * <li>calling <code>{@link Bidi#setReorderingMode}</code> as needed (when
     * the reordering mode is other than normal),</li>
     * <li>calling <code>{@link Bidi#setInverse}</code> as needed (when text
     * should be transformed from a visual to a logical form),</li>
     * <li>resolving embedding levels of each character in the input text by
     * calling <code>{@link Bidi#setPara}</code>,</li>
     * <li>reordering the characters based on the computed embedding levels,
     * also performing character mirroring as needed, and streaming the result
     * to the output, by calling <code>{@link Bidi#writeReordered}</code>,</li>
     * <li>performing Arabic digit and letter shaping on the output text by
     * calling <code>{@link ArabicShaping#shape}</code>.</li>
     * </ul><p>
     * An "ordering scheme" encompasses the base direction and the order of
     * text, and these characteristics must be defined by the caller for both
     * input and output explicitly .<p>
     * There are 36 possible combinations of {input, output} ordering schemes,
     * which are partially supported by <code>{@link Bidi}</code> already.
     * Examples of the currently supported combinations:
     * <ul>
     * <li>{Logical LTR, Visual LTR}: this is equivalent to calling
     * <code>{@link Bidi#setPara}</code> with
     * <code>paraLevel == {@link Bidi#LTR}</code>,</li>
     * <li>{Logical RTL, Visual LTR}: this is equivalent to calling
     * <code>{@link Bidi#setPara}</code> with
     * <code>paraLevel == {@link Bidi#RTL}</code>,</li>
     * <li>{Logical Default ("Auto") LTR, Visual LTR}: this is equivalent to
     * calling <code>{@link Bidi#setPara}</code> with
     * <code>paraLevel == {@link Bidi#LEVEL_DEFAULT_LTR}</code>,</li>
     * <li>{Logical Default ("Auto") RTL, Visual LTR}: this is equivalent to
     * calling <code>{@link Bidi#setPara}</code> with
     * <code>paraLevel == {@link Bidi#LEVEL_DEFAULT_RTL}</code>,</li>
     * <li>{Visual LTR, Logical LTR}: this is equivalent to
     * calling <code>{@link Bidi#setInverse}(true)</code> and then
     * <code>{@link Bidi#setPara}</code> with
     * <code>paraLevel == {@link Bidi#LTR}</code>,</li>
     * <li>{Visual LTR, Logical RTL}: this is equivalent to calling
     * <code>{@link Bidi#setInverse}(true)</code> and then
     * <code>{@link Bidi#setPara}</code> with
     * <code>paraLevel == {@link Bidi#RTL}</code>.</li>
     * </ul><p>
     * All combinations that involve the Visual RTL scheme are unsupported by
     * <code>{@link Bidi}</code>, for instance:
     * <ul>
     * <li>{Logical LTR, Visual RTL},</li>
     * <li>{Visual RTL, Logical RTL}.</li>
     * </ul>
     * <p>Example of usage of the transformation engine:</p>
     * <pre>
     * BidiTransform bidiTransform = new BidiTransform();
     * String in = "abc \u06f0123"; // "abc \\u06f0123"
     * // Run a transformation.
     * String out = bidiTransform.transform(in,
     *          Bidi.LTR, Order.VISUAL,
     *          Bidi.RTL, Order.LOGICAL,
     *          Mirroring.OFF,
     *          ArabicShaping.DIGITS_AN2EN | ArabicShaping.DIGIT_TYPE_AN_EXTENDED);
     * // Result: "0123 abc".
     * // Do something with out.
     * out = out.replace('0', '4');
     * // Result: "4123 abc".
     * // Run a reverse transformation.
     * String inNew = bidiTransform.transform(out,
     *          Bidi.RTL, Order.LOGICAL,
     *          Bidi.LTR, Order.VISUAL,
     *          Mirroring.OFF,
     *          ArabicShaping.DIGITS_EN2AN | ArabicShaping.DIGIT_TYPE_AN_EXTENDED);
     * // Result: "abc \\u06f4\\u06f1\\u06f2\\u06f3"
     * </pre>
     *
     * @param text An input character sequence that the Bidi layout
     *        transformations will be performed on.
     * @param inParaLevel A base embedding level of the input as defined in
     *        <code>{@link Bidi#setPara(String, byte, byte[])}</code>
     *        documentation for the <code>paraLevel</code> parameter.
     * @param inOrder An order of the input, which can be one of the
     *        <code>{@link Order}</code> values.
     * @param outParaLevel A base embedding level of the output as defined in
     *        <code>{@link Bidi#setPara(String, byte, byte[])}</code>
     *        documentation for the <code>paraLevel</code> parameter.
     * @param outOrder An order of the output, which can be one of the
     *        <code>{@link Order}</code> values.
     * @param doMirroring Indicates whether or not to perform character
     *        mirroring, and can accept one of the
     *        <code>{@link Mirroring}</code> values.
     * @param shapingOptions Arabic digit and letter shaping options defined in
     *        the <code>{@link ArabicShaping}</code> documentation.
     *        <p><strong>Note:</strong> Direction indicator options are
     *        computed by the transformation engine based on the effective
     *        ordering schemes, so user-defined direction indicators will be
     *        ignored.
     * @return The output string, which is the result of the layout
     *        transformation.
     * @throws IllegalArgumentException if <code>text</code>,
     *        <code>inOrder</code>, <code>outOrder</code>, or
     *        <code>doMirroring</code> parameter is <code>null</code>.
     * @hide draft / provisional / internal are hidden on Android
     */
    public String transform(CharSequence text,
            byte inParaLevel, Order inOrder,
            byte outParaLevel, Order outOrder,
            Mirroring doMirroring, int shapingOptions)
    {
        if (text == null || inOrder == null || outOrder == null || doMirroring == null) {
            throw new IllegalArgumentException();
        }
        this.text = text.toString();

        byte[] levels = {inParaLevel, outParaLevel};
        resolveBaseDirection(levels);

        ReorderingScheme currentScheme = findMatchingScheme(levels[0], inOrder,
                levels[1], outOrder);
        if (currentScheme != null) {
            this.bidi = new Bidi();
            this.reorderingOptions = Mirroring.ON.equals(doMirroring)
                    ? Bidi.DO_MIRRORING : Bidi.REORDER_DEFAULT;

             /* Ignore TEXT_DIRECTION_* flags, as we apply our own depending on the
                text scheme at the time shaping is invoked. */
            this.shapingOptions = shapingOptions & ~ArabicShaping.TEXT_DIRECTION_MASK;
            currentScheme.doTransform(this);
        }
        return this.text;
    }

    /**
     * When the direction option is
     * <code>{@link Bidi#LEVEL_DEFAULT_LTR}</code> or
     * <code>{@link Bidi#LEVEL_DEFAULT_RTL}</code>, resolves the base
     * direction according to that of the first strong directional character in
     * the text.
     *
     * @param levels Byte array, where levels[0] is an input level levels[1] is
     *        an output level. Resolved levels override these.
     */
    private void resolveBaseDirection(byte[] levels) {
        if (Bidi.IsDefaultLevel(levels[0])) {
            byte level = Bidi.getBaseDirection(text);
            levels[0] = level != Bidi.NEUTRAL ? level
                : levels[0] == Bidi.LEVEL_DEFAULT_RTL ? Bidi.RTL : Bidi.LTR;
        } else {
            levels[0] &= 1;
        }
        if (Bidi.IsDefaultLevel(levels[1])) {
            levels[1] = levels[0];
        } else {
            levels[1] &= 1;
        }
    }

    /**
     * Finds a valid <code>{@link ReorderingScheme}</code> matching the
     * caller-defined scheme.
     *
     * @return A valid <code>ReorderingScheme</code> object or null
     */
    private ReorderingScheme findMatchingScheme(byte inLevel, Order inOrder,
            byte outLevel, Order outOrder) {
        for (ReorderingScheme scheme : ReorderingScheme.values()) {
            if (scheme.matches(inLevel, inOrder, outLevel, outOrder)) {
                return scheme;
            }
        }
        return null;
    }

    /**
     * Performs bidi resolution of text.
     *
     * @param level Base embedding level
     * @param options Reordering options
     */
    private void resolve(byte level, int options) {
        bidi.setInverse((options & Bidi.REORDER_INVERSE_LIKE_DIRECT) != 0);
        bidi.setReorderingMode(options);
        bidi.setPara(text, level, null);
    }

    /**
     * Performs basic reordering of text (Logical LTR or RTL to Visual LTR).
     *
     */
    private void reorder() {
        text = bidi.writeReordered(reorderingOptions);
        reorderingOptions = Bidi.REORDER_DEFAULT;
    }

    /**
     * Performs string reverse.
     */
    private void reverse() {
        text = Bidi.writeReverse(text, Bidi.OPTION_DEFAULT);
    }

    /**
     * Performs character mirroring without reordering. When this method is
     * called, <code>{@link #text}</code> should be in a Logical form.
     */
    private void mirror() {
        if ((reorderingOptions & Bidi.DO_MIRRORING) == 0) {
            return;
        }
        StringBuffer sb = new StringBuffer(text);
        byte[] levels = bidi.getLevels();
        for (int i = 0, n = levels.length; i < n;) {
            int ch = UTF16.charAt(sb, i);
            if ((levels[i] & 1) != 0) {
                UTF16.setCharAt(sb, i, UCharacter.getMirror(ch));
            }
            i += UTF16.getCharCount(ch);
        }
        text = sb.toString();
        reorderingOptions &= ~Bidi.DO_MIRRORING;
    }

    /**
     * Performs digit and letter shaping
     *
     * @param digitsDir Digit shaping option that indicates whether the text
     *      should be treated as logical or visual.
     * @param lettersDir Letter shaping option that indicates whether the text
     *      should be treated as logical or visual form (can mismatch the digit
     *      option).
     */
    private void shapeArabic(int digitsDir, int lettersDir) {
        if (digitsDir == lettersDir) {
            shapeArabic(shapingOptions | digitsDir);
        } else {
            /* Honor all shape options other than letters (not necessarily digits
               only) */
            shapeArabic((shapingOptions & ~ArabicShaping.LETTERS_MASK) | digitsDir);

            /* Honor all shape options other than digits (not necessarily letters
               only) */
            shapeArabic((shapingOptions & ~ArabicShaping.DIGITS_MASK) | lettersDir);
        }
    }

    /**
     * Performs digit and letter shaping
     *
     * @param options Shaping options covering both letters and digits
     */
    private void shapeArabic(int options) {
        if (options != 0) {
            ArabicShaping shaper = new ArabicShaping(options);
            try {
                text = shaper.shape(text);
            } catch(ArabicShapingException e) {
            }
        }
    }

    private enum ReorderingScheme {
        LOG_LTR_TO_VIS_LTR {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsLTR(inLevel) && IsLogical(inOrder)
                        && IsLTR(outLevel) && IsVisual(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_LOGICAL);
                transform.resolve(Bidi.LTR, Bidi.REORDER_DEFAULT);
                transform.reorder();
            }
        },
        LOG_RTL_TO_VIS_LTR {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsRTL(inLevel) && IsLogical(inOrder)
                        && IsLTR(outLevel) && IsVisual(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.resolve(Bidi.RTL, Bidi.REORDER_DEFAULT);
                transform.reorder();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_VISUAL_LTR);
            }
        },
        LOG_LTR_TO_VIS_RTL {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsLTR(inLevel) && IsLogical(inOrder)
                        && IsRTL(outLevel) && IsVisual(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_LOGICAL);
                transform.resolve(Bidi.LTR, Bidi.REORDER_DEFAULT);
                transform.reorder();
                transform.reverse();
            }
        },
        LOG_RTL_TO_VIS_RTL {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsRTL(inLevel) && IsLogical(inOrder)
                        && IsRTL(outLevel) && IsVisual(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.resolve(Bidi.RTL, Bidi.REORDER_DEFAULT);
                transform.reorder();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_VISUAL_LTR);
                transform.reverse();
            }
        },
        VIS_LTR_TO_LOG_RTL {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsLTR(inLevel) && IsVisual(inOrder)
                        && IsRTL(outLevel) && IsLogical(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_VISUAL_LTR);
                transform.resolve(Bidi.RTL, Bidi.REORDER_INVERSE_LIKE_DIRECT);
                transform.reorder();
            }
        },
        VIS_RTL_TO_LOG_RTL {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsRTL(inLevel) && IsVisual(inOrder)
                        && IsRTL(outLevel) && IsLogical(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.reverse();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_VISUAL_LTR);
                transform.resolve(Bidi.RTL, Bidi.REORDER_INVERSE_LIKE_DIRECT);
                transform.reorder();
            }
        },
        VIS_LTR_TO_LOG_LTR {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsLTR(inLevel) && IsVisual(inOrder)
                        && IsLTR(outLevel) && IsLogical(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.resolve(Bidi.LTR, Bidi.REORDER_INVERSE_LIKE_DIRECT);
                transform.reorder();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_LOGICAL);
            }
        },
        VIS_RTL_TO_LOG_LTR {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsRTL(inLevel) && IsVisual(inOrder)
                        && IsLTR(outLevel) && IsLogical(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.reverse();
                transform.resolve(Bidi.LTR, Bidi.REORDER_INVERSE_LIKE_DIRECT);
                transform.reorder();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_LOGICAL);
            }
        },
        LOG_LTR_TO_LOG_RTL {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsLTR(inLevel) && IsLogical(inOrder)
                        && IsRTL(outLevel) && IsLogical(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_LOGICAL);
                transform.resolve(Bidi.LTR, Bidi.REORDER_DEFAULT);
                transform.mirror();
                transform.resolve(Bidi.LTR, Bidi.REORDER_RUNS_ONLY);
                transform.reorder();
            }
        },
        LOG_RTL_TO_LOG_LTR {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsRTL(inLevel) && IsLogical(inOrder)
                        && IsLTR(outLevel) && IsLogical(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.resolve(Bidi.RTL, Bidi.REORDER_DEFAULT);
                transform.mirror();
                transform.resolve(Bidi.RTL, Bidi.REORDER_RUNS_ONLY);
                transform.reorder();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_LOGICAL);
            }
        },
        VIS_LTR_TO_VIS_RTL {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsLTR(inLevel) && IsVisual(inOrder)
                        && IsRTL(outLevel) && IsVisual(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.resolve(Bidi.LTR, Bidi.REORDER_DEFAULT);
                transform.mirror();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_VISUAL_LTR);
                transform.reverse();
            }
        },
        VIS_RTL_TO_VIS_LTR {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsRTL(inLevel) && IsVisual(inOrder)
                        && IsLTR(outLevel) && IsVisual(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.reverse();
                transform.resolve(Bidi.LTR, Bidi.REORDER_DEFAULT);
                transform.mirror();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_VISUAL_LTR);
            }
        },
        LOG_LTR_TO_LOG_LTR {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsLTR(inLevel) && IsLogical(inOrder)
                        && IsLTR(outLevel) && IsLogical(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.resolve(Bidi.LTR, Bidi.REORDER_DEFAULT);
                transform.mirror();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_LOGICAL);
            }
        },
        LOG_RTL_TO_LOG_RTL {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsRTL(inLevel) && IsLogical(inOrder)
                        && IsRTL(outLevel) && IsLogical(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.resolve(Bidi.RTL, Bidi.REORDER_DEFAULT);
                transform.mirror();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_VISUAL_LTR, ArabicShaping.TEXT_DIRECTION_LOGICAL);
            }
        },
        VIS_LTR_TO_VIS_LTR {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsLTR(inLevel) && IsVisual(inOrder)
                        && IsLTR(outLevel) && IsVisual(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.resolve(Bidi.LTR, Bidi.REORDER_DEFAULT);
                transform.mirror();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_VISUAL_LTR);
            }
        },
        VIS_RTL_TO_VIS_RTL {
            @Override
            boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder) {
                return IsRTL(inLevel) && IsVisual(inOrder)
                        && IsRTL(outLevel) && IsVisual(outOrder);
            }
            @Override
            void doTransform(BidiTransform transform) {
                transform.reverse();
                transform.resolve(Bidi.LTR, Bidi.REORDER_DEFAULT);
                transform.mirror();
                transform.shapeArabic(ArabicShaping.TEXT_DIRECTION_LOGICAL, ArabicShaping.TEXT_DIRECTION_VISUAL_LTR);
                transform.reverse();
            }
        };

        /**
         * Indicates whether this scheme matches another one in terms of
         * equality of base direction and ordering scheme.
         *
         * @param inLevel Base level of the input text
         * @param inOrder Order of the input text
         * @param outLevel Base level of the output text
         * @param outOrder Order of the output text
         *
         * @return <code>true</code> if it's a match, <code>false</code>
         * otherwise
         */
        abstract boolean matches(byte inLevel, Order inOrder, byte outLevel, Order outOrder);

        /**
         * Performs a series of bidi layout transformations unique for the current
         * scheme.

         * @param transform Bidi transformation engine
         */
        abstract void doTransform(BidiTransform transform);
    }

    /**
     * Is level LTR? convenience method

     * @param level Embedding level
     */
    private static boolean IsLTR(byte level) {
        return (level & 1) == 0;
    }

    /**
     * Is level RTL? convenience method

     * @param level Embedding level
     */
    private static boolean IsRTL(byte level) {
        return (level & 1) == 1;
    }

    /**
     * Is order logical? convenience method

     * @param level Order value
     */
    private static boolean IsLogical(Order order) {
        return Order.LOGICAL.equals(order);
    }

    /**
     * Is order visual? convenience method

     * @param level Order value
     */
    private static boolean IsVisual(Order order) {
        return Order.VISUAL.equals(order);
    }

}
