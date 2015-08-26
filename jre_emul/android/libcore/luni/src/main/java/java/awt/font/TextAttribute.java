/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Ilya S. Okomin
 * @version $Revision$
 */
package java.awt.font;

import java.text.AttributedCharacterIterator.Attribute;

/**
 * The TextAttribute class defines attribute keys and attribute values
 * for text rendering. Each TextAttributes should have the following
 * information:
 * <ul>
 * <li>key name;</li>
 * <li>valid values;</li>
 * <li>relevant constants;</li>
 * <li>default action if the attribute is absent;</li>
 * <li>default action's description.</li>
 * </ul>
 */
public final class TextAttribute extends Attribute {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7744112784117861702L;

    /**
     * Instantiates a new TextAttribute with the specified name.
     *
     * @param name the name of TextAttribute.
     */
    protected TextAttribute(String name) {
        super(name);
    }

    /**
     * The BACKGROUND text attribute.
     */
    public static final TextAttribute BACKGROUND = new TextAttribute("background");

    /** The BIDI_EMBEDDING text attribute key. */
    public static final TextAttribute BIDI_EMBEDDING = new TextAttribute("bidi_embedding");

    /** The CHAR_REPLACEMENT text attribute key. */
    public static final TextAttribute CHAR_REPLACEMENT = new TextAttribute("char_replacement");

    /** The FAMILY text attribute key. */
    public static final TextAttribute FAMILY = new TextAttribute("family");

    /** The FONT text attribute key. */
    public static final TextAttribute FONT = new TextAttribute("font");

    /** The FOREGROUND text attribute key. */
    public static final TextAttribute FOREGROUND = new TextAttribute("foreground");

    /** The INPUT_METHOD_HIGHLIGHT text attribute key. */
    public static final TextAttribute INPUT_METHOD_HIGHLIGHT = new TextAttribute(
            "input method highlight");

    /** The INPUT_METHOD_UNDERLINE text attribute key. */
    public static final TextAttribute INPUT_METHOD_UNDERLINE = new TextAttribute(
            "input method underline");

    /** The JUSTIFICATION text attribute key. */
    public static final TextAttribute JUSTIFICATION = new TextAttribute("justification");

    /** The Constant JUSTIFICATION_indicates the full requested width. */
    public static final Float JUSTIFICATION_FULL = new Float(1.0f);

    /**
     * The Constant JUSTIFICATION_NONE indicates that line is not
     * allowed for justification.
     */
    public static final Float JUSTIFICATION_NONE = new Float(0.0f);

    /**
     * The KERNING text attribute key.
     *
     * @since 1.6
     */
    public static final TextAttribute KERNING = new TextAttribute("kerning");

    /** @since 1.6 */
    public static final Integer KERNING_ON = 1;

    /**
     * The LIGATURES text attribute key.
     *
     * @since 1.6
     */
    public static final TextAttribute LIGATURES = new TextAttribute("ligatures");

    /** @since 1.6 */
    public static final Integer LIGATURES_ON = 1;

    /** The NUMERIC_SHAPING text attribute. */
    public static final TextAttribute NUMERIC_SHAPING = new TextAttribute("numeric_shaping");

    /** The POSTURE text attribute. */
    public static final TextAttribute POSTURE = new TextAttribute("posture");

    /** The Constant POSTURE_REGULAR indicates regular posture. */
    public static final Float POSTURE_REGULAR = new Float(0.0f);

    /** The Constant POSTURE_OBLIQUE indicates italic posture. */
    public static final Float POSTURE_OBLIQUE = new Float(0.20f);

    /** The RUN_DIRECTION text attribute. */
    public static final TextAttribute RUN_DIRECTION = new TextAttribute("run_direction");

    /**
     * The Constant RUN_DIRECTION_LTR indicates left-to-right run
     * direction.
     */
    public static final Boolean RUN_DIRECTION_LTR = false;

    /**
     * The Constant RUN_DIRECTION_RTL indicates right-to-left run
     * direction.
     */
    public static final Boolean RUN_DIRECTION_RTL = true;

    /** The SIZE text attribute. */
    public static final TextAttribute SIZE = new TextAttribute("size");

    /** The STRIKETHROUGH text attribute. */
    public static final TextAttribute STRIKETHROUGH = new TextAttribute("strikethrough");

    /** The Constant STRIKETHROUGH_ON indicates a single strikethrough. */
    public static final Boolean STRIKETHROUGH_ON = true;

    /** The SUPERSCRIPT text attribute. */
    public static final TextAttribute SUPERSCRIPT = new TextAttribute("superscript");

    /** The Constant SUPERSCRIPT_SUB indicates a standard subscript. */
    public static final Integer SUPERSCRIPT_SUB = -1;

    /** The Constant SUPERSCRIPT_SUPER indicates a standard superscript. */
    public static final Integer SUPERSCRIPT_SUPER = 1;

    /** The SWAP_COLORS text attribute. */
    public static final TextAttribute SWAP_COLORS = new TextAttribute("swap_colors");

    /**
     * The Constant SWAP_COLORS_ON indicates a swap of foreground
     * and background.
     */
    public static final Boolean SWAP_COLORS_ON = true;

    /**
     * The TRACKING text attribute key.
     *
     * @since 1.6
     */
    public static final TextAttribute TRACKING = new TextAttribute("tracking");

    /** @since 1.6 */
    public static final Float TRACKING_LOOSE = 0.04f;
    /** @since 1.6 */
    public static final Float TRACKING_TIGHT = -0.04f;

    /** The TRANSFORM text attribute. */
    public static final TextAttribute TRANSFORM = new TextAttribute("transform");

    /** The Constant UNDERLINE text attribute. */
    public static final TextAttribute UNDERLINE = new TextAttribute("underline");

    /**
     * The Constant UNDERLINE_ON indicates a standard underline
     * at the roman baseline for roman text.
     */
    public static final Integer UNDERLINE_ON = 0;

    /**
     * The Constant UNDERLINE_LOW_ONE_PIXEL indicates a single
     * pixel solid low underline.
     */
    public static final Integer UNDERLINE_LOW_ONE_PIXEL = 1;

    /**
     * The Constant UNDERLINE_LOW_TWO_PIXEL indicates a double
     * pixel solid low underline.
     */
    public static final Integer UNDERLINE_LOW_TWO_PIXEL = 2;

    /**
     * The Constant UNDERLINE_LOW_DOTTED indicates a
     * single pixel dotted low underline.
     */
    public static final Integer UNDERLINE_LOW_DOTTED = 3;

    /**
     * The Constant UNDERLINE_LOW_GRAY indicates double pixel
     * gray low underline.
     */
    public static final Integer UNDERLINE_LOW_GRAY = 4;

    /**
     * The Constant UNDERLINE_LOW_DASHED indicates single pixel dashed
     * low underline.
     */
    public static final Integer UNDERLINE_LOW_DASHED = 5;

    /** The WEIGHT text attribute. */
    public static final TextAttribute WEIGHT = new TextAttribute("weight");

    /**
     * The Constant WEIGHT_EXTRA_LIGHT indicates the lightest
     * predefined weight.
     */
    public static final Float WEIGHT_EXTRA_LIGHT = new Float(0.5f);

    /**
     * The Constant WEIGHT_LIGHT indicates the standard light weight.
     */
    public static final Float WEIGHT_LIGHT = new Float(0.75f);

    /**
     * The Constant WEIGHT_DEMILIGHT indicates an intermediate weight
     * between LIGHT and STANDARD.
     */
    public static final Float WEIGHT_DEMILIGHT = new Float(0.875f);

    /**
     * The Constant WEIGHT_REGULAR indicates the standart weight.
     */
    public static final Float WEIGHT_REGULAR = new Float(1.0f);

    /**
     * The Constant WEIGHT_SEMIBOLD indicates a semi weight
     * of REGULAR.
     */
    public static final Float WEIGHT_SEMIBOLD = new Float(1.25f);

    /** The Constant WEIGHT_MEDIUM indicates average weight
     * between the REGULAR and BOLD. */
    public static final Float WEIGHT_MEDIUM = new Float(1.5f);

    /** The Constant WEIGHT_DEMIBOLD indicates
     * a lighter weight than BOLD. */
    public static final Float WEIGHT_DEMIBOLD = new Float(1.75f);

    /** The Constant WEIGHT_BOLD indicates the standard bold weight. */
    public static final Float WEIGHT_BOLD = new Float(2.0f);

    /** The Constant WEIGHT_HEAVY indicates a heavier weight than BOLD. */
    public static final Float WEIGHT_HEAVY = new Float(2.25f);

    /** The Constant WEIGHT_EXTRABOLD indicates an extra heavy weight. */
    public static final Float WEIGHT_EXTRABOLD = new Float(2.5f);

    /**
     * The Constant WEIGHT_ULTRABOLD indicates the heaviest predefined
     * weight.
     */
    public static final Float WEIGHT_ULTRABOLD = new Float(2.75f);

    /** The WIDTH text attribute. */
    public static final TextAttribute WIDTH = new TextAttribute("width");

    /**
     * The Constant WIDTH_CONDENSED indicates the most condensed
     * predefined width.
     */
    public static final Float WIDTH_CONDENSED = new Float(0.75f);

    /**
     * The Constant WIDTH_SEMI_CONDENSED indicates
     * a semi condensed width.
     */
    public static final Float WIDTH_SEMI_CONDENSED = new Float(0.875f);

    /**
     * The Constant WIDTH_REGULAR indicates the standard width.
     */
    public static final Float WIDTH_REGULAR = new Float(1.0f);

    /**
     * The Constant WIDTH_SEMI_EXTENDED indicates semi extended width.
     */
    public static final Float WIDTH_SEMI_EXTENDED = new Float(1.25f);

    /**
     * The Constant WIDTH_EXTENDED indicates extended width.
     */
    public static final Float WIDTH_EXTENDED = new Float(1.5f);

}
