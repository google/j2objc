/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2016, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

/* FOOD FOR THOUGHT: currently the reordering modes are a mixture of
 * algorithm for direct BiDi, algorithm for inverse Bidi and the bizarre
 * concept of RUNS_ONLY which is a double operation.
 * It could be advantageous to divide this into 3 concepts:
 * a) Operation: direct / inverse / RUNS_ONLY
 * b) Direct algorithm: default / NUMBERS_SPECIAL / GROUP_NUMBERS_WITH_L
 * c) Inverse algorithm: default / INVERSE_LIKE_DIRECT / NUMBERS_SPECIAL
 * This would allow combinations not possible today like RUNS_ONLY with
 * NUMBERS_SPECIAL.
 * Also allow to set INSERT_MARKS for the direct step of RUNS_ONLY and
 * REMOVE_CONTROLS for the inverse step.
 * Not all combinations would be supported, and probably not all do make sense.
 * This would need to document which ones are supported and what are the
 * fallbacks for unsupported combinations.
 */

//TODO: make sample program do something simple but real and complete

package android.icu.text;

import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import java.lang.reflect.Array;
import java.text.AttributedCharacterIterator;
import java.util.Arrays;

import android.icu.impl.UBiDiProps;
import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterDirection;
import android.icu.lang.UProperty;

/**
 *
 * <h2>Bidi algorithm for ICU</h2>
 *
 * This is an implementation of the Unicode Bidirectional Algorithm. The
 * algorithm is defined in the <a
 * href="http://www.unicode.org/unicode/reports/tr9/">Unicode Standard Annex #9</a>.
 * <p>
 *
 * Note: Libraries that perform a bidirectional algorithm and reorder strings
 * accordingly are sometimes called "Storage Layout Engines". ICU's Bidi and
 * shaping (ArabicShaping) classes can be used at the core of such "Storage
 * Layout Engines".
 *
 * <h3>General remarks about the API:</h3>
 *
 * The &quot;limit&quot; of a sequence of characters is the position just after
 * their last character, i.e., one more than that position.
 * <p>
 *
 * Some of the API methods provide access to &quot;runs&quot;. Such a
 * &quot;run&quot; is defined as a sequence of characters that are at the same
 * embedding level after performing the Bidi algorithm.
 *
 * <h3>Basic concept: paragraph</h3>
 * A piece of text can be divided into several paragraphs by characters
 * with the Bidi class <code>Block Separator</code>. For handling of
 * paragraphs, see:
 * <ul>
 * <li>{@link #countParagraphs}
 * <li>{@link #getParaLevel}
 * <li>{@link #getParagraph}
 * <li>{@link #getParagraphByIndex}
 * </ul>
 *
 * <h3>Basic concept: text direction</h3>
 * The direction of a piece of text may be:
 * <ul>
 * <li>{@link #LTR}
 * <li>{@link #RTL}
 * <li>{@link #MIXED}
 * <li>{@link #NEUTRAL}
 * </ul>
 *
 * <h3>Basic concept: levels</h3>
 *
 * Levels in this API represent embedding levels according to the Unicode
 * Bidirectional Algorithm.
 * Their low-order bit (even/odd value) indicates the visual direction.<p>
 *
 * Levels can be abstract values when used for the
 * <code>paraLevel</code> and <code>embeddingLevels</code>
 * arguments of <code>setPara()</code>; there:
 * <ul>
 * <li>the high-order bit of an <code>embeddingLevels[]</code>
 * value indicates whether the using application is
 * specifying the level of a character to <i>override</i> whatever the
 * Bidi implementation would resolve it to.</li>
 * <li><code>paraLevel</code> can be set to the
 * pseudo-level values <code>LEVEL_DEFAULT_LTR</code>
 * and <code>LEVEL_DEFAULT_RTL</code>.</li>
 * </ul>
 *
 * <p>The related constants are not real, valid level values.
 * <code>DEFAULT_XXX</code> can be used to specify
 * a default for the paragraph level for
 * when the <code>setPara()</code> method
 * shall determine it but there is no
 * strongly typed character in the input.<p>
 *
 * Note that the value for <code>LEVEL_DEFAULT_LTR</code> is even
 * and the one for <code>LEVEL_DEFAULT_RTL</code> is odd,
 * just like with normal LTR and RTL level values -
 * these special values are designed that way. Also, the implementation
 * assumes that MAX_EXPLICIT_LEVEL is odd.
 *
 * <b>See Also:</b>
 * <ul>
 * <li>{@link #LEVEL_DEFAULT_LTR}
 * <li>{@link #LEVEL_DEFAULT_RTL}
 * <li>{@link #LEVEL_OVERRIDE}
 * <li>{@link #MAX_EXPLICIT_LEVEL}
 * <li>{@link #setPara}
 * </ul>
 *
 * <h3>Basic concept: Reordering Mode</h3>
 * Reordering mode values indicate which variant of the Bidi algorithm to
 * use.
 *
 * <b>See Also:</b>
 * <ul>
 * <li>{@link #setReorderingMode}
 * <li>{@link #REORDER_DEFAULT}
 * <li>{@link #REORDER_NUMBERS_SPECIAL}
 * <li>{@link #REORDER_GROUP_NUMBERS_WITH_R}
 * <li>{@link #REORDER_RUNS_ONLY}
 * <li>{@link #REORDER_INVERSE_NUMBERS_AS_L}
 * <li>{@link #REORDER_INVERSE_LIKE_DIRECT}
 * <li>{@link #REORDER_INVERSE_FOR_NUMBERS_SPECIAL}
 * </ul>
 *
 * <h3>Basic concept: Reordering Options</h3>
 * Reordering options can be applied during Bidi text transformations.
 *
 * <b>See Also:</b>
 * <ul>
 * <li>{@link #setReorderingOptions}
 * <li>{@link #OPTION_DEFAULT}
 * <li>{@link #OPTION_INSERT_MARKS}
 * <li>{@link #OPTION_REMOVE_CONTROLS}
 * <li>{@link #OPTION_STREAMING}
 * </ul>
 *
 * <h4> Sample code for the ICU Bidi API </h4>
 *
 * <h5>Rendering a paragraph with the ICU Bidi API</h5>
 *
 * This is (hypothetical) sample code that illustrates how the ICU Bidi API
 * could be used to render a paragraph of text. Rendering code depends highly on
 * the graphics system, therefore this sample code must make a lot of
 * assumptions, which may or may not match any existing graphics system's
 * properties.
 *
 * <p>
 * The basic assumptions are:
 *
 * <ul>
 * <li>Rendering is done from left to right on a horizontal line.</li>
 * <li>A run of single-style, unidirectional text can be rendered at once.
 * </li>
 * <li>Such a run of text is passed to the graphics system with characters
 * (code units) in logical order.</li>
 * <li>The line-breaking algorithm is very complicated and Locale-dependent -
 * and therefore its implementation omitted from this sample code.</li>
 * </ul>
 *
 * <pre>
 *
 *  package android.icu.dev.test.bidi;
 *
 *  import android.icu.text.Bidi;
 *  import android.icu.text.BidiRun;
 *
 *  public class Sample {
 *
 *      static final int styleNormal = 0;
 *      static final int styleSelected = 1;
 *      static final int styleBold = 2;
 *      static final int styleItalics = 4;
 *      static final int styleSuper=8;
 *      static final int styleSub = 16;
 *
 *      static class StyleRun {
 *          int limit;
 *          int style;
 *
 *          public StyleRun(int limit, int style) {
 *              this.limit = limit;
 *              this.style = style;
 *          }
 *      }
 *
 *      static class Bounds {
 *          int start;
 *          int limit;
 *
 *          public Bounds(int start, int limit) {
 *              this.start = start;
 *              this.limit = limit;
 *          }
 *      }
 *
 *      static int getTextWidth(String text, int start, int limit,
 *                              StyleRun[] styleRuns, int styleRunCount) {
 *          // simplistic way to compute the width
 *          return limit - start;
 *      }
 *
 *      // set limit and StyleRun limit for a line
 *      // from text[start] and from styleRuns[styleRunStart]
 *      // using Bidi.getLogicalRun(...)
 *      // returns line width
 *      static int getLineBreak(String text, Bounds line, Bidi para,
 *                              StyleRun styleRuns[], Bounds styleRun) {
 *          // dummy return
 *          return 0;
 *      }
 *
 *      // render runs on a line sequentially, always from left to right
 *
 *      // prepare rendering a new line
 *      static void startLine(byte textDirection, int lineWidth) {
 *          System.out.println();
 *      }
 *
 *      // render a run of text and advance to the right by the run width
 *      // the text[start..limit-1] is always in logical order
 *      static void renderRun(String text, int start, int limit,
 *                            byte textDirection, int style) {
 *      }
 *
 *      // We could compute a cross-product
 *      // from the style runs with the directional runs
 *      // and then reorder it.
 *      // Instead, here we iterate over each run type
 *      // and render the intersections -
 *      // with shortcuts in simple (and common) cases.
 *      // renderParagraph() is the main function.
 *
 *      // render a directional run with
 *      // (possibly) multiple style runs intersecting with it
 *      static void renderDirectionalRun(String text, int start, int limit,
 *                                       byte direction, StyleRun styleRuns[],
 *                                       int styleRunCount) {
 *          int i;
 *
 *          // iterate over style runs
 *          if (direction == Bidi.LTR) {
 *              int styleLimit;
 *              for (i = 0; i &lt; styleRunCount; ++i) {
 *                  styleLimit = styleRuns[i].limit;
 *                  if (start &lt; styleLimit) {
 *                      if (styleLimit &gt; limit) {
 *                          styleLimit = limit;
 *                      }
 *                      renderRun(text, start, styleLimit,
 *                                direction, styleRuns[i].style);
 *                      if (styleLimit == limit) {
 *                          break;
 *                      }
 *                      start = styleLimit;
 *                  }
 *              }
 *          } else {
 *              int styleStart;
 *
 *              for (i = styleRunCount-1; i &gt;= 0; --i) {
 *                  if (i &gt; 0) {
 *                      styleStart = styleRuns[i-1].limit;
 *                  } else {
 *                      styleStart = 0;
 *                  }
 *                  if (limit &gt;= styleStart) {
 *                      if (styleStart &lt; start) {
 *                          styleStart = start;
 *                      }
 *                      renderRun(text, styleStart, limit, direction,
 *                                styleRuns[i].style);
 *                      if (styleStart == start) {
 *                          break;
 *                      }
 *                      limit = styleStart;
 *                  }
 *              }
 *          }
 *      }
 *
 *      // the line object represents text[start..limit-1]
 *      static void renderLine(Bidi line, String text, int start, int limit,
 *                             StyleRun styleRuns[], int styleRunCount) {
 *          byte direction = line.getDirection();
 *          if (direction != Bidi.MIXED) {
 *              // unidirectional
 *              if (styleRunCount &lt;= 1) {
 *                  renderRun(text, start, limit, direction, styleRuns[0].style);
 *              } else {
 *                  renderDirectionalRun(text, start, limit, direction,
 *                                       styleRuns, styleRunCount);
 *              }
 *          } else {
 *              // mixed-directional
 *              int count, i;
 *              BidiRun run;
 *
 *              try {
 *                  count = line.countRuns();
 *              } catch (IllegalStateException e) {
 *                  e.printStackTrace();
 *                  return;
 *              }
 *              if (styleRunCount &lt;= 1) {
 *                  int style = styleRuns[0].style;
 *
 *                  // iterate over directional runs
 *                  for (i = 0; i &lt; count; ++i) {
 *                      run = line.getVisualRun(i);
 *                      renderRun(text, run.getStart(), run.getLimit(),
 *                                run.getDirection(), style);
 *                  }
 *              } else {
 *                  // iterate over both directional and style runs
 *                  for (i = 0; i &lt; count; ++i) {
 *                      run = line.getVisualRun(i);
 *                      renderDirectionalRun(text, run.getStart(),
 *                                           run.getLimit(), run.getDirection(),
 *                                           styleRuns, styleRunCount);
 *                  }
 *              }
 *          }
 *      }
 *
 *      static void renderParagraph(String text, byte textDirection,
 *                                  StyleRun styleRuns[], int styleRunCount,
 *                                  int lineWidth) {
 *          int length = text.length();
 *          Bidi para = new Bidi();
 *          try {
 *              para.setPara(text,
 *                           textDirection != 0 ? Bidi.LEVEL_DEFAULT_RTL
 *                                              : Bidi.LEVEL_DEFAULT_LTR,
 *                           null);
 *          } catch (Exception e) {
 *              e.printStackTrace();
 *              return;
 *          }
 *          byte paraLevel = (byte)(1 &amp; para.getParaLevel());
 *          StyleRun styleRun = new StyleRun(length, styleNormal);
 *
 *          if (styleRuns == null || styleRunCount &lt;= 0) {
 *              styleRuns = new StyleRun[1];
 *              styleRunCount = 1;
 *              styleRuns[0] = styleRun;
 *          }
 *          // assume styleRuns[styleRunCount-1].limit&gt;=length
 *
 *          int width = getTextWidth(text, 0, length, styleRuns, styleRunCount);
 *          if (width &lt;= lineWidth) {
 *              // everything fits onto one line
 *
 *              // prepare rendering a new line from either left or right
 *              startLine(paraLevel, width);
 *
 *              renderLine(para, text, 0, length, styleRuns, styleRunCount);
 *          } else {
 *              // we need to render several lines
 *              Bidi line = new Bidi(length, 0);
 *              int start = 0, limit;
 *              int styleRunStart = 0, styleRunLimit;
 *
 *              for (;;) {
 *                  limit = length;
 *                  styleRunLimit = styleRunCount;
 *                  width = getLineBreak(text, new Bounds(start, limit),
 *                                       para, styleRuns,
 *                                       new Bounds(styleRunStart, styleRunLimit));
 *                  try {
 *                      line = para.setLine(start, limit);
 *                  } catch (Exception e) {
 *                      e.printStackTrace();
 *                      return;
 *                  }
 *                  // prepare rendering a new line
 *                  // from either left or right
 *                  startLine(paraLevel, width);
 *
 *                  if (styleRunStart &gt; 0) {
 *                      int newRunCount = styleRuns.length - styleRunStart;
 *                      StyleRun[] newRuns = new StyleRun[newRunCount];
 *                      System.arraycopy(styleRuns, styleRunStart, newRuns, 0,
 *                                       newRunCount);
 *                      renderLine(line, text, start, limit, newRuns,
 *                                 styleRunLimit - styleRunStart);
 *                  } else {
 *                      renderLine(line, text, start, limit, styleRuns,
 *                                 styleRunLimit - styleRunStart);
 *                  }
 *                  if (limit == length) {
 *                      break;
 *                  }
 *                  start = limit;
 *                  styleRunStart = styleRunLimit - 1;
 *                  if (start &gt;= styleRuns[styleRunStart].limit) {
 *                      ++styleRunStart;
 *                  }
 *              }
 *          }
 *      }
 *
 *      public static void main(String[] args)
 *      {
 *          renderParagraph("Some Latin text...", Bidi.LTR, null, 0, 80);
 *          renderParagraph("Some Hebrew text...", Bidi.RTL, null, 0, 60);
 *      }
 *  }
 *
 * </pre>
 *
 * @hide Only a subset of ICU is exposed in Android
 */

/*
 * General implementation notes:
 *
 * Throughout the implementation, there are comments like (W2) that refer to
 * rules of the BiDi algorithm, in this example to the second rule of the
 * resolution of weak types.
 *
 * For handling surrogate pairs, where two UChar's form one "abstract" (or UTF-32)
 * character according to UTF-16, the second UChar gets the directional property of
 * the entire character assigned, while the first one gets a BN, a boundary
 * neutral, type, which is ignored by most of the algorithm according to
 * rule (X9) and the implementation suggestions of the BiDi algorithm.
 *
 * Later, adjustWSLevels() will set the level for each BN to that of the
 * following character (UChar), which results in surrogate pairs getting the
 * same level on each of their surrogates.
 *
 * In a UTF-8 implementation, the same thing could be done: the last byte of
 * a multi-byte sequence would get the "real" property, while all previous
 * bytes of that sequence would get BN.
 *
 * It is not possible to assign all those parts of a character the same real
 * property because this would fail in the resolution of weak types with rules
 * that look at immediately surrounding types.
 *
 * As a related topic, this implementation does not remove Boundary Neutral
 * types from the input, but ignores them wherever this is relevant.
 * For example, the loop for the resolution of the weak types reads
 * types until it finds a non-BN.
 * Also, explicit embedding codes are neither changed into BN nor removed.
 * They are only treated the same way real BNs are.
 * As stated before, adjustWSLevels() takes care of them at the end.
 * For the purpose of conformance, the levels of all these codes
 * do not matter.
 *
 * Note that this implementation modifies the dirProps
 * after the initial setup, when applying X5c (replace FSI by LRI or RLI),
 * X6, N0 (replace paired brackets by L or R).
 *
 * In this implementation, the resolution of weak types (W1 to W6),
 * neutrals (N1 and N2), and the assignment of the resolved level (In)
 * are all done in one single loop, in resolveImplicitLevels().
 * Changes of dirProp values are done on the fly, without writing
 * them back to the dirProps array.
 *
 *
 * This implementation contains code that allows to bypass steps of the
 * algorithm that are not needed on the specific paragraph
 * in order to speed up the most common cases considerably,
 * like text that is entirely LTR, or RTL text without numbers.
 *
 * Most of this is done by setting a bit for each directional property
 * in a flags variable and later checking for whether there are
 * any LTR characters or any RTL characters, or both, whether
 * there are any explicit embedding codes, etc.
 *
 * If the (Xn) steps are performed, then the flags are re-evaluated,
 * because they will then not contain the embedding codes any more
 * and will be adjusted for override codes, so that subsequently
 * more bypassing may be possible than what the initial flags suggested.
 *
 * If the text is not mixed-directional, then the
 * algorithm steps for the weak type resolution are not performed,
 * and all levels are set to the paragraph level.
 *
 * If there are no explicit embedding codes, then the (Xn) steps
 * are not performed.
 *
 * If embedding levels are supplied as a parameter, then all
 * explicit embedding codes are ignored, and the (Xn) steps
 * are not performed.
 *
 * White Space types could get the level of the run they belong to,
 * and are checked with a test of (flags&MASK_EMBEDDING) to
 * consider if the paragraph direction should be considered in
 * the flags variable.
 *
 * If there are no White Space types in the paragraph, then
 * (L1) is not necessary in adjustWSLevels().
 */

public class Bidi {

    static class Point {
        int pos;    /* position in text */
        int flag;   /* flag for LRM/RLM, before/after */
    }

    static class InsertPoints {
        int size;
        int confirmed;
        Point[] points = new Point[0];
    }

    static class Opening {
        int   position;                 /* position of opening bracket */
        int   match;                    /* matching char or -position of closing bracket */
        int   contextPos;               /* position of last strong char found before opening */
        short flags;                    /* bits for L or R/AL found within the pair */
        byte  contextDir;               /* L or R according to last strong char before opening */
    }

    static class IsoRun {
        int   contextPos;               /* position of char determining context */
        short start;                    /* index of first opening entry for this run */
        short limit;                    /* index after last opening entry for this run */
        byte  level;                    /* level of this run */
        byte  lastStrong;               /* bidi class of last strong char found in this run */
        byte  lastBase;                 /* bidi class of last base char found in this run */
        byte  contextDir;               /* L or R to use as context for following openings */
    }

    static class BracketData {
        Opening[] openings = new Opening[SIMPLE_OPENINGS_COUNT];
        int   isoRunLast;               /* index of last used entry */
        /* array of nested isolated sequence entries; can never excess UBIDI_MAX_EXPLICIT_LEVEL
           + 1 for index 0, + 1 for before the first isolated sequence */
        IsoRun[]  isoRuns = new IsoRun[MAX_EXPLICIT_LEVEL+2];
        boolean   isNumbersSpecial;     /*reordering mode for NUMBERS_SPECIAL */
    }

    static class Isolate {
        int   startON;
        int   start1;
        short stateImp;
        short state;
    }

    /** Paragraph level setting<p>
     *
     * Constant indicating that the base direction depends on the first strong
     * directional character in the text according to the Unicode Bidirectional
     * Algorithm. If no strong directional character is present,
     * then set the paragraph level to 0 (left-to-right).<p>
     *
     * If this value is used in conjunction with reordering modes
     * <code>REORDER_INVERSE_LIKE_DIRECT</code> or
     * <code>REORDER_INVERSE_FOR_NUMBERS_SPECIAL</code>, the text to reorder
     * is assumed to be visual LTR, and the text after reordering is required
     * to be the corresponding logical string with appropriate contextual
     * direction. The direction of the result string will be RTL if either
     * the rightmost or leftmost strong character of the source text is RTL
     * or Arabic Letter, the direction will be LTR otherwise.<p>
     *
     * If reordering option <code>OPTION_INSERT_MARKS</code> is set, an RLM may
     * be added at the beginning of the result string to ensure round trip
     * (that the result string, when reordered back to visual, will produce
     * the original source text).
     * @see #REORDER_INVERSE_LIKE_DIRECT
     * @see #REORDER_INVERSE_FOR_NUMBERS_SPECIAL
     */
    public static final byte LEVEL_DEFAULT_LTR = (byte)0x7e;

    /** Paragraph level setting<p>
     *
     * Constant indicating that the base direction depends on the first strong
     * directional character in the text according to the Unicode Bidirectional
     * Algorithm. If no strong directional character is present,
     * then set the paragraph level to 1 (right-to-left).<p>
     *
     * If this value is used in conjunction with reordering modes
     * <code>REORDER_INVERSE_LIKE_DIRECT</code> or
     * <code>REORDER_INVERSE_FOR_NUMBERS_SPECIAL</code>, the text to reorder
     * is assumed to be visual LTR, and the text after reordering is required
     * to be the corresponding logical string with appropriate contextual
     * direction. The direction of the result string will be RTL if either
     * the rightmost or leftmost strong character of the source text is RTL
     * or Arabic Letter, or if the text contains no strong character;
     * the direction will be LTR otherwise.<p>
     *
     * If reordering option <code>OPTION_INSERT_MARKS</code> is set, an RLM may
     * be added at the beginning of the result string to ensure round trip
     * (that the result string, when reordered back to visual, will produce
     * the original source text).
     * @see #REORDER_INVERSE_LIKE_DIRECT
     * @see #REORDER_INVERSE_FOR_NUMBERS_SPECIAL
     */
    public static final byte LEVEL_DEFAULT_RTL = (byte)0x7f;

    /**
     * Maximum explicit embedding level.
     * (The maximum resolved level can be up to <code>MAX_EXPLICIT_LEVEL+1</code>).
     */
    public static final byte MAX_EXPLICIT_LEVEL = 125;

    /**
     * Bit flag for level input.
     * Overrides directional properties.
     */
    public static final byte LEVEL_OVERRIDE = (byte)0x80;

    /**
     * Special value which can be returned by the mapping methods when a
     * logical index has no corresponding visual index or vice-versa. This may
     * happen for the logical-to-visual mapping of a Bidi control when option
     * <code>OPTION_REMOVE_CONTROLS</code> is
     * specified. This can also happen for the visual-to-logical mapping of a
     * Bidi mark (LRM or RLM) inserted by option
     * <code>OPTION_INSERT_MARKS</code>.
     * @see #getVisualIndex
     * @see #getVisualMap
     * @see #getLogicalIndex
     * @see #getLogicalMap
     * @see #OPTION_INSERT_MARKS
     * @see #OPTION_REMOVE_CONTROLS
     */
    public static final int MAP_NOWHERE = -1;

    /**
     * Left-to-right text.
     * <ul>
     * <li>As return value for <code>getDirection()</code>, it means
     *     that the source string contains no right-to-left characters, or
     *     that the source string is empty and the paragraph level is even.
     * <li>As return value for <code>getBaseDirection()</code>, it
     *     means that the first strong character of the source string has
     *     a left-to-right direction.
     * </ul>
     */
    public static final byte LTR = 0;

    /**
     * Right-to-left text.
     * <ul>
     * <li>As return value for <code>getDirection()</code>, it means
     *     that the source string contains no left-to-right characters, or
     *     that the source string is empty and the paragraph level is odd.
     * <li>As return value for <code>getBaseDirection()</code>, it
     *     means that the first strong character of the source string has
     *     a right-to-left direction.
     * </ul>
     */
    public static final byte RTL = 1;

    /**
     * Mixed-directional text.
     * <p>As return value for <code>getDirection()</code>, it means
     *    that the source string contains both left-to-right and
     *    right-to-left characters.
     */
    public static final byte MIXED = 2;

    /**
     * No strongly directional text.
     * <p>As return value for <code>getBaseDirection()</code>, it means
     *    that the source string is missing or empty, or contains neither
     *    left-to-right nor right-to-left characters.
     */
    public static final byte NEUTRAL = 3;

    /**
     * option bit for writeReordered():
     * keep combining characters after their base characters in RTL runs
     *
     * @see #writeReordered
     */
    public static final short KEEP_BASE_COMBINING = 1;

    /**
     * option bit for writeReordered():
     * replace characters with the "mirrored" property in RTL runs
     * by their mirror-image mappings
     *
     * @see #writeReordered
     */
    public static final short DO_MIRRORING = 2;

    /**
     * option bit for writeReordered():
     * surround the run with LRMs if necessary;
     * this is part of the approximate "inverse Bidi" algorithm
     *
     * <p>This option does not imply corresponding adjustment of the index
     * mappings.
     *
     * @see #setInverse
     * @see #writeReordered
     */
    public static final short INSERT_LRM_FOR_NUMERIC = 4;

    /**
     * option bit for writeReordered():
     * remove Bidi control characters
     * (this does not affect INSERT_LRM_FOR_NUMERIC)
     *
     * <p>This option does not imply corresponding adjustment of the index
     * mappings.
     *
     * @see #writeReordered
     * @see #INSERT_LRM_FOR_NUMERIC
     */
    public static final short REMOVE_BIDI_CONTROLS = 8;

    /**
     * option bit for writeReordered():
     * write the output in reverse order
     *
     * <p>This has the same effect as calling <code>writeReordered()</code>
     * first without this option, and then calling
     * <code>writeReverse()</code> without mirroring.
     * Doing this in the same step is faster and avoids a temporary buffer.
     * An example for using this option is output to a character terminal that
     * is designed for RTL scripts and stores text in reverse order.
     *
     * @see #writeReordered
     */
    public static final short OUTPUT_REVERSE = 16;

    /** Reordering mode: Regular Logical to Visual Bidi algorithm according to Unicode.
     * @see #setReorderingMode
     */
    public static final short REORDER_DEFAULT = 0;

    /** Reordering mode: Logical to Visual algorithm which handles numbers in
     * a way which mimicks the behavior of Windows XP.
     * @see #setReorderingMode
     */
    public static final short REORDER_NUMBERS_SPECIAL = 1;

    /** Reordering mode: Logical to Visual algorithm grouping numbers with
     * adjacent R characters (reversible algorithm).
     * @see #setReorderingMode
     */
    public static final short REORDER_GROUP_NUMBERS_WITH_R = 2;

    /** Reordering mode: Reorder runs only to transform a Logical LTR string
     * to the logical RTL string with the same display, or vice-versa.<br>
     * If this mode is set together with option
     * <code>OPTION_INSERT_MARKS</code>, some Bidi controls in the source
     * text may be removed and other controls may be added to produce the
     * minimum combination which has the required display.
     * @see #OPTION_INSERT_MARKS
     * @see #setReorderingMode
     */
    public static final short REORDER_RUNS_ONLY = 3;

    /** Reordering mode: Visual to Logical algorithm which handles numbers
     * like L (same algorithm as selected by <code>setInverse(true)</code>.
     * @see #setInverse
     * @see #setReorderingMode
     */
    public static final short REORDER_INVERSE_NUMBERS_AS_L = 4;

    /** Reordering mode: Visual to Logical algorithm equivalent to the regular
     * Logical to Visual algorithm.
     * @see #setReorderingMode
     */
    public static final short REORDER_INVERSE_LIKE_DIRECT = 5;

    /** Reordering mode: Inverse Bidi (Visual to Logical) algorithm for the
     * <code>REORDER_NUMBERS_SPECIAL</code> Bidi algorithm.
     * @see #setReorderingMode
     */
    public static final short REORDER_INVERSE_FOR_NUMBERS_SPECIAL = 6;

    /*  Number of values for reordering mode. */
    static final short REORDER_COUNT = 7;

    /* Reordering mode values must be ordered so that all the regular logical to
     * visual modes come first, and all inverse Bidi modes come last.
     */
    static final short REORDER_LAST_LOGICAL_TO_VISUAL =
            REORDER_NUMBERS_SPECIAL;

    /**
     * Option value for <code>setReorderingOptions</code>:
     * disable all the options which can be set with this method
     * @see #setReorderingOptions
     */
    public static final int OPTION_DEFAULT = 0;

    /**
     * Option bit for <code>setReorderingOptions</code>:
     * insert Bidi marks (LRM or RLM) when needed to ensure correct result of
     * a reordering to a Logical order
     *
     * <p>This option must be set or reset before calling
     * <code>setPara</code>.
     *
     * <p>This option is significant only with reordering modes which generate
     * a result with Logical order, specifically.
     * <ul>
     *   <li><code>REORDER_RUNS_ONLY</code></li>
     *   <li><code>REORDER_INVERSE_NUMBERS_AS_L</code></li>
     *   <li><code>REORDER_INVERSE_LIKE_DIRECT</code></li>
     *   <li><code>REORDER_INVERSE_FOR_NUMBERS_SPECIAL</code></li>
     * </ul>
     *
     * <p>If this option is set in conjunction with reordering mode
     * <code>REORDER_INVERSE_NUMBERS_AS_L</code> or with calling
     * <code>setInverse(true)</code>, it implies option
     * <code>INSERT_LRM_FOR_NUMERIC</code> in calls to method
     * <code>writeReordered()</code>.
     *
     * <p>For other reordering modes, a minimum number of LRM or RLM characters
     * will be added to the source text after reordering it so as to ensure
     * round trip, i.e. when applying the inverse reordering mode on the
     * resulting logical text with removal of Bidi marks
     * (option <code>OPTION_REMOVE_CONTROLS</code> set before calling
     * <code>setPara()</code> or option
     * <code>REMOVE_BIDI_CONTROLS</code> in
     * <code>writeReordered</code>), the result will be identical to the
     * source text in the first transformation.
     *
     * <p>This option will be ignored if specified together with option
     * <code>OPTION_REMOVE_CONTROLS</code>. It inhibits option
     * <code>REMOVE_BIDI_CONTROLS</code> in calls to method
     * <code>writeReordered()</code> and it implies option
     * <code>INSERT_LRM_FOR_NUMERIC</code> in calls to method
     * <code>writeReordered()</code> if the reordering mode is
     * <code>REORDER_INVERSE_NUMBERS_AS_L</code>.
     *
     * @see #setReorderingMode
     * @see #setReorderingOptions
     * @see #INSERT_LRM_FOR_NUMERIC
     * @see #REMOVE_BIDI_CONTROLS
     * @see #OPTION_REMOVE_CONTROLS
     * @see #REORDER_RUNS_ONLY
     * @see #REORDER_INVERSE_NUMBERS_AS_L
     * @see #REORDER_INVERSE_LIKE_DIRECT
     * @see #REORDER_INVERSE_FOR_NUMBERS_SPECIAL
     */
    public static final int OPTION_INSERT_MARKS = 1;

    /**
     * Option bit for <code>setReorderingOptions</code>:
     * remove Bidi control characters
     *
     * <p>This option must be set or reset before calling
     * <code>setPara</code>.
     *
     * <p>This option nullifies option
     * <code>OPTION_INSERT_MARKS</code>. It inhibits option
     * <code>INSERT_LRM_FOR_NUMERIC</code> in calls to method
     * <code>writeReordered()</code> and it implies option
     * <code>REMOVE_BIDI_CONTROLS</code> in calls to that method.
     *
     * @see #setReorderingMode
     * @see #setReorderingOptions
     * @see #OPTION_INSERT_MARKS
     * @see #INSERT_LRM_FOR_NUMERIC
     * @see #REMOVE_BIDI_CONTROLS
     */
    public static final int OPTION_REMOVE_CONTROLS = 2;

    /**
     * Option bit for <code>setReorderingOptions</code>:
     * process the output as part of a stream to be continued
     *
     * <p>This option must be set or reset before calling
     * <code>setPara</code>.
     *
     * <p>This option specifies that the caller is interested in processing
     * large text object in parts. The results of the successive calls are
     * expected to be concatenated by the caller. Only the call for the last
     * part will have this option bit off.
     *
     * <p>When this option bit is on, <code>setPara()</code> may process
     * less than the full source text in order to truncate the text at a
     * meaningful boundary. The caller should call
     * <code>getProcessedLength()</code> immediately after calling
     * <code>setPara()</code> in order to determine how much of the source
     * text has been processed. Source text beyond that length should be
     * resubmitted in following calls to <code>setPara</code>. The
     * processed length may be less than the length of the source text if a
     * character preceding the last character of the source text constitutes a
     * reasonable boundary (like a block separator) for text to be continued.<br>
     * If the last character of the source text constitutes a reasonable
     * boundary, the whole text will be processed at once.<br>
     * If nowhere in the source text there exists
     * such a reasonable boundary, the processed length will be zero.<br>
     * The caller should check for such an occurrence and do one of the following:
     * <ul><li>submit a larger amount of text with a better chance to include
     *         a reasonable boundary.</li>
     *     <li>resubmit the same text after turning off option
     *         <code>OPTION_STREAMING</code>.</li></ul>
     * In all cases, this option should be turned off before processing the last
     * part of the text.
     *
     * <p>When the <code>OPTION_STREAMING</code> option is used, it is
     * recommended to call <code>orderParagraphsLTR(true)</code> before calling
     * <code>setPara()</code> so that later paragraphs may be concatenated to
     * previous paragraphs on the right.
     *
     * @see #setReorderingMode
     * @see #setReorderingOptions
     * @see #getProcessedLength
     */
    public static final int OPTION_STREAMING = 4;

    /*
     *   Comparing the description of the Bidi algorithm with this implementation
     *   is easier with the same names for the Bidi types in the code as there.
     *   See UCharacterDirection
     */
    static final byte L   = UCharacterDirection.LEFT_TO_RIGHT;                  /*  0 */
    static final byte R   = UCharacterDirection.RIGHT_TO_LEFT;                  /*  1 */
    static final byte EN  = UCharacterDirection.EUROPEAN_NUMBER;                /*  2 */
    static final byte ES  = UCharacterDirection.EUROPEAN_NUMBER_SEPARATOR;      /*  3 */
    static final byte ET  = UCharacterDirection.EUROPEAN_NUMBER_TERMINATOR;     /*  4 */
    static final byte AN  = UCharacterDirection.ARABIC_NUMBER;                  /*  5 */
    static final byte CS  = UCharacterDirection.COMMON_NUMBER_SEPARATOR;        /*  6 */
    static final byte B   = UCharacterDirection.BLOCK_SEPARATOR;                /*  7 */
    static final byte S   = UCharacterDirection.SEGMENT_SEPARATOR;              /*  8 */
    static final byte WS  = UCharacterDirection.WHITE_SPACE_NEUTRAL;            /*  9 */
    static final byte ON  = UCharacterDirection.OTHER_NEUTRAL;                  /* 10 */
    static final byte LRE = UCharacterDirection.LEFT_TO_RIGHT_EMBEDDING;        /* 11 */
    static final byte LRO = UCharacterDirection.LEFT_TO_RIGHT_OVERRIDE;         /* 12 */
    static final byte AL  = UCharacterDirection.RIGHT_TO_LEFT_ARABIC;           /* 13 */
    static final byte RLE = UCharacterDirection.RIGHT_TO_LEFT_EMBEDDING;        /* 14 */
    static final byte RLO = UCharacterDirection.RIGHT_TO_LEFT_OVERRIDE;         /* 15 */
    static final byte PDF = UCharacterDirection.POP_DIRECTIONAL_FORMAT;         /* 16 */
    static final byte NSM = UCharacterDirection.DIR_NON_SPACING_MARK;           /* 17 */
    static final byte BN  = UCharacterDirection.BOUNDARY_NEUTRAL;               /* 18 */
    static final byte FSI = UCharacterDirection.FIRST_STRONG_ISOLATE;           /* 19 */
    static final byte LRI = UCharacterDirection.LEFT_TO_RIGHT_ISOLATE;          /* 20 */
    static final byte RLI = UCharacterDirection.RIGHT_TO_LEFT_ISOLATE;          /* 21 */
    static final byte PDI = UCharacterDirection.POP_DIRECTIONAL_ISOLATE;        /* 22 */
    static final byte ENL = PDI + 1;    /* EN after W7 */                       /* 23 */
    static final byte ENR = ENL + 1;    /* EN not subject to W7 */              /* 24 */

    /**
     * Value returned by <code>BidiClassifier</code> when there is no need to
     * override the standard Bidi class for a given code point.
     *
     * <p>This constant is deprecated; use UCharacter.getIntPropertyMaxValue(UProperty.BIDI_CLASS)+1 instead.
     *
     * @see BidiClassifier
     * @deprecated ICU 58 The numeric value may change over time, see ICU ticket #12420.
     */
    @Deprecated
    public static final int CLASS_DEFAULT = UCharacterDirection.CHAR_DIRECTION_COUNT;

    /* number of paras entries allocated initially */
    static final int SIMPLE_PARAS_COUNT = 10;
    /* number of isolate run entries for paired brackets allocated initially */
    static final int SIMPLE_OPENINGS_COUNT = 20;

    private static final char CR = '\r';
    private static final char LF = '\n';

    static final int LRM_BEFORE = 1;
    static final int LRM_AFTER = 2;
    static final int RLM_BEFORE = 4;
    static final int RLM_AFTER = 8;

    /* flags for Opening.flags */
    static final byte FOUND_L = (byte)DirPropFlag(L);
    static final byte FOUND_R = (byte)DirPropFlag(R);

    /*
     * The following bit is used for the directional isolate status.
     * Stack entries corresponding to isolate sequences are greater than ISOLATE.
     */
    static final int ISOLATE = 0x0100;


    /*
     * reference to parent paragraph object (reference to self if this object is
     * a paragraph object); set to null in a newly opened object; set to a
     * real value after a successful execution of setPara or setLine
     */
    Bidi                paraBidi;

    final UBiDiProps    bdp;

    /* character array representing the current text */
    char[]              text;

    /* length of the current text */
    int                 originalLength;

    /* if the option OPTION_STREAMING is set, this is the length of
     * text actually processed by <code>setPara</code>, which may be shorter
     * than the original length. Otherwise, it is identical to the original
     * length.
     */
    int                 length;

    /* if option OPTION_REMOVE_CONTROLS is set, and/or Bidi
     * marks are allowed to be inserted in one of the reordering modes, the
     * length of the result string may be different from the processed length.
     */
    int                 resultLength;

    /* indicators for whether memory may be allocated after construction */
    boolean             mayAllocateText;
    boolean             mayAllocateRuns;

    /* arrays with one value per text-character */
    byte[]              dirPropsMemory = new byte[1];
    byte[]              levelsMemory = new byte[1];
    byte[]              dirProps;
    byte[]              levels;

    /* are we performing an approximation of the "inverse Bidi" algorithm? */
    boolean             isInverse;

    /* are we using the basic algorithm or its variation? */
    int                 reorderingMode;

    /* bitmask for reordering options */
    int                 reorderingOptions;

    /* must block separators receive level 0? */
    boolean             orderParagraphsLTR;

    /* the paragraph level */
    byte                paraLevel;
    /* original paraLevel when contextual */
    /* must be one of DEFAULT_xxx or 0 if not contextual */
    byte                defaultParaLevel;

    /* context data */
    String              prologue;
    String              epilogue;

    /* the following is set in setPara, used in processPropertySeq */

    ImpTabPair          impTabPair;  /* reference to levels state table pair */
    /* the overall paragraph or line directionality*/
    byte                direction;

    /* flags is a bit set for which directional properties are in the text */
    int                 flags;

    /* lastArabicPos is index to the last AL in the text, -1 if none */
    int                 lastArabicPos;

    /* characters after trailingWSStart are WS and are */
    /* implicitly at the paraLevel (rule (L1)) - levels may not reflect that */
    int                 trailingWSStart;

    /* fields for paragraph handling, set in getDirProps() */
    int                 paraCount;
    int[]               paras_limit = new int[SIMPLE_PARAS_COUNT];
    byte[]              paras_level = new byte[SIMPLE_PARAS_COUNT];

    /* fields for line reordering */
    int                 runCount;     /* ==-1: runs not set up yet */
    BidiRun[]           runsMemory = new BidiRun[0];
    BidiRun[]           runs;

    /* for non-mixed text, we only need a tiny array of runs (no allocation) */
    BidiRun[]           simpleRuns = {new BidiRun()};

    /* fields for managing isolate sequences */
    Isolate[]           isolates;
    /* maximum or current nesting depth of isolate sequences */
    /* Within resolveExplicitLevels() and checkExplicitLevels(), this is the maximal
       nesting encountered.
       Within resolveImplicitLevels(), this is the index of the current isolates
       stack entry. */
    int                 isolateCount;

    /* mapping of runs in logical order to visual order */
    int[]               logicalToVisualRunsMap;
    /* flag to indicate that the map has been updated */
    boolean             isGoodLogicalToVisualRunsMap;

    /* customized class provider */
    BidiClassifier      customClassifier = null;

    /* for inverse Bidi with insertion of directional marks */
    InsertPoints        insertPoints = new InsertPoints();

    /* for option OPTION_REMOVE_CONTROLS */
    int                 controlCount;

    /*
     * Sometimes, bit values are more appropriate
     * to deal with directionality properties.
     * Abbreviations in these method names refer to names
     * used in the Bidi algorithm.
     */
    static int DirPropFlag(byte dir) {
        return (1 << dir);
    }

    boolean testDirPropFlagAt(int flag, int index) {
        return ((DirPropFlag(dirProps[index]) & flag) != 0);
    }

    static final int DirPropFlagMultiRuns = DirPropFlag((byte)31);

    /* to avoid some conditional statements, use tiny constant arrays */
    static final int DirPropFlagLR[] = { DirPropFlag(L), DirPropFlag(R) };
    static final int DirPropFlagE[] = { DirPropFlag(LRE), DirPropFlag(RLE) };
    static final int DirPropFlagO[] = { DirPropFlag(LRO), DirPropFlag(RLO) };

    static final int DirPropFlagLR(byte level) { return DirPropFlagLR[level & 1]; }
    static final int DirPropFlagE(byte level)  { return DirPropFlagE[level & 1]; }
    static final int DirPropFlagO(byte level)  { return DirPropFlagO[level & 1]; }
    static final byte DirFromStrong(byte strong) { return strong == L ? L : R; }
    static final byte NoOverride(byte level) { return (byte)(level & ~LEVEL_OVERRIDE); }

    /*  are there any characters that are LTR or RTL? */
    static final int MASK_LTR =
        DirPropFlag(L)|DirPropFlag(EN)|DirPropFlag(ENL)|DirPropFlag(ENR)|DirPropFlag(AN)|DirPropFlag(LRE)|DirPropFlag(LRO)|DirPropFlag(LRI);
    static final int MASK_RTL = DirPropFlag(R)|DirPropFlag(AL)|DirPropFlag(RLE)|DirPropFlag(RLO)|DirPropFlag(RLI);

    static final int MASK_R_AL = DirPropFlag(R)|DirPropFlag(AL);
    static final int MASK_STRONG_EN_AN = DirPropFlag(L)|DirPropFlag(R)|DirPropFlag(AL)|DirPropFlag(EN)|DirPropFlag(AN);
    /* explicit embedding codes */
    static final int MASK_EXPLICIT = DirPropFlag(LRE)|DirPropFlag(LRO)|DirPropFlag(RLE)|DirPropFlag(RLO)|DirPropFlag(PDF);
    static final int MASK_BN_EXPLICIT = DirPropFlag(BN)|MASK_EXPLICIT;

    /* explicit isolate codes */
    static final int MASK_ISO = DirPropFlag(LRI)|DirPropFlag(RLI)|DirPropFlag(FSI)|DirPropFlag(PDI);

    /* paragraph and segment separators */
    static final int MASK_B_S = DirPropFlag(B)|DirPropFlag(S);

    /* all types that are counted as White Space or Neutral in some steps */
    static final int MASK_WS = MASK_B_S|DirPropFlag(WS)|MASK_BN_EXPLICIT|MASK_ISO;

    /* types that are neutrals or could becomes neutrals in (Wn) */
    static final int MASK_POSSIBLE_N = DirPropFlag(ON)|DirPropFlag(CS)|DirPropFlag(ES)|DirPropFlag(ET)|MASK_WS;

    /*
     * These types may be changed to "e",
     * the embedding type (L or R) of the run,
     * in the Bidi algorithm (N2)
     */
    static final int MASK_EMBEDDING = DirPropFlag(NSM)|MASK_POSSIBLE_N;

    /*
     *  the dirProp's L and R are defined to 0 and 1 values in UCharacterDirection.java
     */
    static byte GetLRFromLevel(byte level)
    {
        return (byte)(level & 1);
    }

    static boolean IsDefaultLevel(byte level)
    {
        return ((level & LEVEL_DEFAULT_LTR) == LEVEL_DEFAULT_LTR);
    }

    static boolean IsBidiControlChar(int c)
    {
        /* check for range 0x200c to 0x200f (ZWNJ, ZWJ, LRM, RLM) or
                           0x202a to 0x202e (LRE, RLE, PDF, LRO, RLO) */
        return (((c & 0xfffffffc) == 0x200c) || ((c >= 0x202a) && (c <= 0x202e))
                                             || ((c >= 0x2066) && (c <= 0x2069)));
    }

    void verifyValidPara()
    {
        if (!(this == this.paraBidi)) {
            throw new IllegalStateException();
        }
    }

    void verifyValidParaOrLine()
    {
        Bidi para = this.paraBidi;
        /* verify Para */
        if (this == para) {
            return;
        }
        /* verify Line */
        if ((para == null) || (para != para.paraBidi)) {
            throw new IllegalStateException();
        }
    }

    void verifyRange(int index, int start, int limit)
    {
        if (index < start || index >= limit) {
            throw new IllegalArgumentException("Value " + index +
                      " is out of range " + start + " to " + limit);
        }
    }

    /**
     * Allocate a <code>Bidi</code> object.
     * Such an object is initially empty. It is assigned
     * the Bidi properties of a piece of text containing one or more paragraphs
     * by <code>setPara()</code>
     * or the Bidi properties of a line within a paragraph by
     * <code>setLine()</code>.<p>
     * This object can be reused.<p>
     * <code>setPara()</code> and <code>setLine()</code> will allocate
     * additional memory for internal structures as necessary.
     */
    public Bidi()
    {
        this(0, 0);
    }

    /**
     * Allocate a <code>Bidi</code> object with preallocated memory
     * for internal structures.
     * This method provides a <code>Bidi</code> object like the default constructor
     * but it also preallocates memory for internal structures
     * according to the sizings supplied by the caller.<p>
     * The preallocation can be limited to some of the internal memory
     * by setting some values to 0 here. That means that if, e.g.,
     * <code>maxRunCount</code> cannot be reasonably predetermined and should not
     * be set to <code>maxLength</code> (the only failproof value) to avoid
     * wasting  memory, then <code>maxRunCount</code> could be set to 0 here
     * and the internal structures that are associated with it will be allocated
     * on demand, just like with the default constructor.
     *
     * @param maxLength is the maximum text or line length that internal memory
     *        will be preallocated for. An attempt to associate this object with a
     *        longer text will fail, unless this value is 0, which leaves the allocation
     *        up to the implementation.
     *
     * @param maxRunCount is the maximum anticipated number of same-level runs
     *        that internal memory will be preallocated for. An attempt to access
     *        visual runs on an object that was not preallocated for as many runs
     *        as the text was actually resolved to will fail,
     *        unless this value is 0, which leaves the allocation up to the implementation.<br><br>
     *        The number of runs depends on the actual text and maybe anywhere between
     *        1 and <code>maxLength</code>. It is typically small.
     *
     * @throws IllegalArgumentException if maxLength or maxRunCount is less than 0
     */
    public Bidi(int maxLength, int maxRunCount)
    {
        /* check the argument values */
        if (maxLength < 0 || maxRunCount < 0) {
            throw new IllegalArgumentException();
        }

        /* reset the object, all reference variables null, all flags false,
           all sizes 0.
           In fact, we don't need to do anything, since class members are
           initialized as zero when an instance is created.
         */
        /*
        mayAllocateText = false;
        mayAllocateRuns = false;
        orderParagraphsLTR = false;
        paraCount = 0;
        runCount = 0;
        trailingWSStart = 0;
        flags = 0;
        paraLevel = 0;
        defaultParaLevel = 0;
        direction = 0;
        */
        /* get Bidi properties */
        bdp = UBiDiProps.INSTANCE;

        /* allocate memory for arrays as requested */
        if (maxLength > 0) {
            getInitialDirPropsMemory(maxLength);
            getInitialLevelsMemory(maxLength);
        } else {
            mayAllocateText = true;
        }

        if (maxRunCount > 0) {
            // if maxRunCount == 1, use simpleRuns[]
            if (maxRunCount > 1) {
                getInitialRunsMemory(maxRunCount);
            }
        } else {
            mayAllocateRuns = true;
        }
    }

    /*
     * We are allowed to allocate memory if object==null or
     * mayAllocate==true for each array that we need.
     *
     * Assume sizeNeeded>0.
     * If object != null, then assume size > 0.
     */
    private Object getMemory(String label, Object array, Class<?> arrayClass,
            boolean mayAllocate, int sizeNeeded)
    {
        int len = Array.getLength(array);

        /* we have at least enough memory and must not allocate */
        if (sizeNeeded == len) {
            return array;
        }
        if (!mayAllocate) {
            /* we must not allocate */
            if (sizeNeeded <= len) {
                return array;
            }
            throw new OutOfMemoryError("Failed to allocate memory for "
                                       + label);
        }
        /* we may try to grow or shrink */
        /* FOOD FOR THOUGHT: when shrinking it should be possible to avoid
           the allocation altogether and rely on this.length */
        try {
            return Array.newInstance(arrayClass, sizeNeeded);
        } catch (Exception e) {
            throw new OutOfMemoryError("Failed to allocate memory for "
                                       + label);
        }
    }

    /* helper methods for each allocated array */
    private void getDirPropsMemory(boolean mayAllocate, int len)
    {
        Object array = getMemory("DirProps", dirPropsMemory, Byte.TYPE, mayAllocate, len);
        dirPropsMemory = (byte[]) array;
    }

    void getDirPropsMemory(int len)
    {
        getDirPropsMemory(mayAllocateText, len);
    }

    private void getLevelsMemory(boolean mayAllocate, int len)
    {
        Object array = getMemory("Levels", levelsMemory, Byte.TYPE, mayAllocate, len);
        levelsMemory = (byte[]) array;
    }

    void getLevelsMemory(int len)
    {
        getLevelsMemory(mayAllocateText, len);
    }

    private void getRunsMemory(boolean mayAllocate, int len)
    {
        Object array = getMemory("Runs", runsMemory, BidiRun.class, mayAllocate, len);
        runsMemory = (BidiRun[]) array;
    }

    void getRunsMemory(int len)
    {
        getRunsMemory(mayAllocateRuns, len);
    }

    /* additional methods used by constructor - always allow allocation */
    private void getInitialDirPropsMemory(int len)
    {
        getDirPropsMemory(true, len);
    }

    private void getInitialLevelsMemory(int len)
    {
        getLevelsMemory(true, len);
    }

    private void getInitialRunsMemory(int len)
    {
        getRunsMemory(true, len);
    }

    /**
     * Modify the operation of the Bidi algorithm such that it
     * approximates an "inverse Bidi" algorithm. This method
     * must be called before <code>setPara()</code>.
     *
     * <p>The normal operation of the Bidi algorithm as described
     * in the Unicode Technical Report is to take text stored in logical
     * (keyboard, typing) order and to determine the reordering of it for visual
     * rendering.
     * Some legacy systems store text in visual order, and for operations
     * with standard, Unicode-based algorithms, the text needs to be transformed
     * to logical order. This is effectively the inverse algorithm of the
     * described Bidi algorithm. Note that there is no standard algorithm for
     * this "inverse Bidi" and that the current implementation provides only an
     * approximation of "inverse Bidi".
     *
     * <p>With <code>isInversed</code> set to <code>true</code>,
     * this method changes the behavior of some of the subsequent methods
     * in a way that they can be used for the inverse Bidi algorithm.
     * Specifically, runs of text with numeric characters will be treated in a
     * special way and may need to be surrounded with LRM characters when they are
     * written in reordered sequence.
     *
     * <p>Output runs should be retrieved using <code>getVisualRun()</code>.
     * Since the actual input for "inverse Bidi" is visually ordered text and
     * <code>getVisualRun()</code> gets the reordered runs, these are actually
     * the runs of the logically ordered output.
     *
     * <p>Calling this method with argument <code>isInverse</code> set to
     * <code>true</code> is equivalent to calling <code>setReorderingMode</code>
     * with argument <code>reorderingMode</code>
     * set to <code>REORDER_INVERSE_NUMBERS_AS_L</code>.<br>
     * Calling this method with argument <code>isInverse</code> set to
     * <code>false</code> is equivalent to calling <code>setReorderingMode</code>
     * with argument <code>reorderingMode</code>
     * set to <code>REORDER_DEFAULT</code>.
     *
     * @param isInverse specifies "forward" or "inverse" Bidi operation.
     *
     * @see #setPara
     * @see #writeReordered
     * @see #setReorderingMode
     * @see #REORDER_INVERSE_NUMBERS_AS_L
     * @see #REORDER_DEFAULT
     */
    public void setInverse(boolean isInverse) {
        this.isInverse = (isInverse);
        this.reorderingMode = isInverse ? REORDER_INVERSE_NUMBERS_AS_L
                : REORDER_DEFAULT;
    }

    /**
     * Is this <code>Bidi</code> object set to perform the inverse Bidi
     * algorithm?
     * <p>Note: calling this method after setting the reordering mode with
     * <code>setReorderingMode</code> will return <code>true</code> if the
     * reordering mode was set to
     * <code>REORDER_INVERSE_NUMBERS_AS_L</code>, <code>false</code>
     * for all other values.
     *
     * @return <code>true</code> if the <code>Bidi</code> object is set to
     * perform the inverse Bidi algorithm by handling numbers as L.
     *
     * @see #setInverse
     * @see #setReorderingMode
     * @see #REORDER_INVERSE_NUMBERS_AS_L
     */
    public boolean isInverse() {
        return isInverse;
    }

    /**
     * Modify the operation of the Bidi algorithm such that it implements some
     * variant to the basic Bidi algorithm or approximates an "inverse Bidi"
     * algorithm, depending on different values of the "reordering mode".
     * This method must be called before <code>setPara()</code>, and stays in
     * effect until called again with a different argument.
     *
     * <p>The normal operation of the Bidi algorithm as described in the Unicode
     * Standard Annex #9 is to take text stored in logical (keyboard, typing)
     * order and to determine how to reorder it for visual rendering.
     *
     * <p>With the reordering mode set to a value other than
     * <code>REORDER_DEFAULT</code>, this method changes the behavior of some of
     * the subsequent methods in a way such that they implement an inverse Bidi
     * algorithm or some other algorithm variants.
     *
     * <p>Some legacy systems store text in visual order, and for operations
     * with standard, Unicode-based algorithms, the text needs to be transformed
     * into logical order. This is effectively the inverse algorithm of the
     * described Bidi algorithm. Note that there is no standard algorithm for
     * this "inverse Bidi", so a number of variants are implemented here.
     *
     * <p>In other cases, it may be desirable to emulate some variant of the
     * Logical to Visual algorithm (e.g. one used in MS Windows), or perform a
     * Logical to Logical transformation.
     *
     * <ul>
     * <li>When the Reordering Mode is set to
     * <code>REORDER_DEFAULT</code>,
     * the standard Bidi Logical to Visual algorithm is applied.</li>
     *
     * <li>When the reordering mode is set to
     * <code>REORDER_NUMBERS_SPECIAL</code>,
     * the algorithm used to perform Bidi transformations when calling
     * <code>setPara</code> should approximate the algorithm used in Microsoft
     * Windows XP rather than strictly conform to the Unicode Bidi algorithm.
     * <br>
     * The differences between the basic algorithm and the algorithm addressed
     * by this option are as follows:
     * <ul>
     *   <li>Within text at an even embedding level, the sequence "123AB"
     *   (where AB represent R or AL letters) is transformed to "123BA" by the
     *   Unicode algorithm and to "BA123" by the Windows algorithm.</li>
     *
     *   <li>Arabic-Indic numbers (AN) are handled by the Windows algorithm just
     *   like regular numbers (EN).</li>
     * </ul></li>
     *
     * <li>When the reordering mode is set to
     * <code>REORDER_GROUP_NUMBERS_WITH_R</code>,
     * numbers located between LTR text and RTL text are associated with the RTL
     * text. For instance, an LTR paragraph with content "abc 123 DEF" (where
     * upper case letters represent RTL characters) will be transformed to
     * "abc FED 123" (and not "abc 123 FED"), "DEF 123 abc" will be transformed
     * to "123 FED abc" and "123 FED abc" will be transformed to "DEF 123 abc".
     * This makes the algorithm reversible and makes it useful when round trip
     * (from visual to logical and back to visual) must be achieved without
     * adding LRM characters. However, this is a variation from the standard
     * Unicode Bidi algorithm.<br>
     * The source text should not contain Bidi control characters other than LRM
     * or RLM.</li>
     *
     * <li>When the reordering mode is set to
     * <code>REORDER_RUNS_ONLY</code>,
     * a "Logical to Logical" transformation must be performed:
     * <ul>
     * <li>If the default text level of the source text (argument
     * <code>paraLevel</code> in <code>setPara</code>) is even, the source text
     * will be handled as LTR logical text and will be transformed to the RTL
     * logical text which has the same LTR visual display.</li>
     * <li>If the default level of the source text is odd, the source text
     * will be handled as RTL logical text and will be transformed to the
     * LTR logical text which has the same LTR visual display.</li>
     * </ul>
     * This mode may be needed when logical text which is basically Arabic or
     * Hebrew, with possible included numbers or phrases in English, has to be
     * displayed as if it had an even embedding level (this can happen if the
     * displaying application treats all text as if it was basically LTR).
     * <br>
     * This mode may also be needed in the reverse case, when logical text which
     * is basically English, with possible included phrases in Arabic or Hebrew,
     * has to be displayed as if it had an odd embedding level.
     * <br>
     * Both cases could be handled by adding LRE or RLE at the head of the
     * text, if the display subsystem supports these formatting controls. If it
     * does not, the problem may be handled by transforming the source text in
     * this mode before displaying it, so that it will be displayed properly.
     * <br>
     * The source text should not contain Bidi control characters other than LRM
     * or RLM.</li>
     *
     * <li>When the reordering mode is set to
     * <code>REORDER_INVERSE_NUMBERS_AS_L</code>, an "inverse Bidi"
     * algorithm is applied.
     * Runs of text with numeric characters will be treated like LTR letters and
     * may need to be surrounded with LRM characters when they are written in
     * reordered sequence (the option <code>INSERT_LRM_FOR_NUMERIC</code> can
     * be used with method <code>writeReordered</code> to this end. This mode
     * is equivalent to calling <code>setInverse()</code> with
     * argument <code>isInverse</code> set to <code>true</code>.</li>
     *
     * <li>When the reordering mode is set to
     * <code>REORDER_INVERSE_LIKE_DIRECT</code>, the "direct" Logical to
     * Visual Bidi algorithm is used as an approximation of an "inverse Bidi"
     * algorithm. This mode is similar to mode
     * <code>REORDER_INVERSE_NUMBERS_AS_L</code> but is closer to the
     * regular Bidi algorithm.
     * <br>
     * For example, an LTR paragraph with the content "FED 123 456 CBA" (where
     * upper case represents RTL characters) will be transformed to
     * "ABC 456 123 DEF", as opposed to "DEF 123 456 ABC"
     * with mode <code>REORDER_INVERSE_NUMBERS_AS_L</code>.<br>
     * When used in conjunction with option
     * <code>OPTION_INSERT_MARKS</code>, this mode generally
     * adds Bidi marks to the output significantly more sparingly than mode
     * <code>REORDER_INVERSE_NUMBERS_AS_L</code>.<br> with option
     * <code>INSERT_LRM_FOR_NUMERIC</code> in calls to
     * <code>writeReordered</code>.</li>
     *
     * <li>When the reordering mode is set to
     * <code>REORDER_INVERSE_FOR_NUMBERS_SPECIAL</code>, the Logical to Visual
     * Bidi algorithm used in Windows XP is used as an approximation of an "inverse
     * Bidi" algorithm.
     * <br>
     * For example, an LTR paragraph with the content "abc FED123" (where
     * upper case represents RTL characters) will be transformed to
     * "abc 123DEF.</li>
     * </ul>
     *
     * <p>In all the reordering modes specifying an "inverse Bidi" algorithm
     * (i.e. those with a name starting with <code>REORDER_INVERSE</code>),
     * output runs should be retrieved using <code>getVisualRun()</code>, and
     * the output text with <code>writeReordered()</code>. The caller should
     * keep in mind that in "inverse Bidi" modes the input is actually visually
     * ordered text and reordered output returned by <code>getVisualRun()</code>
     * or <code>writeReordered()</code> are actually runs or character string
     * of logically ordered output.<br>
     * For all the "inverse Bidi" modes, the source text should not contain
     * Bidi control characters other than LRM or RLM.
     *
     * <p>Note that option <code>OUTPUT_REVERSE</code> of
     * <code>writeReordered</code> has no useful meaning and should not be used
     * in conjunction with any value of the reordering mode specifying "inverse
     * Bidi" or with value <code>REORDER_RUNS_ONLY</code>.
     *
     * @param reorderingMode specifies the required variant of the Bidi
     *                       algorithm.
     *
     * @see #setInverse
     * @see #setPara
     * @see #writeReordered
     * @see #INSERT_LRM_FOR_NUMERIC
     * @see #OUTPUT_REVERSE
     * @see #REORDER_DEFAULT
     * @see #REORDER_NUMBERS_SPECIAL
     * @see #REORDER_GROUP_NUMBERS_WITH_R
     * @see #REORDER_RUNS_ONLY
     * @see #REORDER_INVERSE_NUMBERS_AS_L
     * @see #REORDER_INVERSE_LIKE_DIRECT
     * @see #REORDER_INVERSE_FOR_NUMBERS_SPECIAL
     */
    public void setReorderingMode(int reorderingMode) {
        if ((reorderingMode < REORDER_DEFAULT) ||
            (reorderingMode >= REORDER_COUNT))
            return;                     /* don't accept a wrong value */
        this.reorderingMode = reorderingMode;
        this.isInverse =
            reorderingMode == REORDER_INVERSE_NUMBERS_AS_L;
    }

    /**
     * What is the requested reordering mode for a given Bidi object?
     *
     * @return the current reordering mode of the Bidi object
     *
     * @see #setReorderingMode
     */
    public int getReorderingMode() {
        return this.reorderingMode;
    }

    /**
     * Specify which of the reordering options should be applied during Bidi
     * transformations.
     *
     * @param options A combination of zero or more of the following
     * reordering options:
     * <code>OPTION_DEFAULT</code>, <code>OPTION_INSERT_MARKS</code>,
     * <code>OPTION_REMOVE_CONTROLS</code>, <code>OPTION_STREAMING</code>.
     *
     * @see #getReorderingOptions
     * @see #OPTION_DEFAULT
     * @see #OPTION_INSERT_MARKS
     * @see #OPTION_REMOVE_CONTROLS
     * @see #OPTION_STREAMING
     */
    public void setReorderingOptions(int options) {
        if ((options & OPTION_REMOVE_CONTROLS) != 0) {
            this.reorderingOptions = options & ~OPTION_INSERT_MARKS;
        } else {
            this.reorderingOptions = options;
        }
    }

    /**
     * What are the reordering options applied to a given Bidi object?
     *
     * @return the current reordering options of the Bidi object
     *
     * @see #setReorderingOptions
     */
    public int getReorderingOptions() {
        return this.reorderingOptions;
    }

    /**
     * Get the base direction of the text provided according to the Unicode
     * Bidirectional Algorithm. The base direction is derived from the first
     * character in the string with bidirectional character type L, R, or AL.
     * If the first such character has type L, LTR is returned. If the first
     * such character has type R or AL, RTL is returned. If the string does
     * not contain any character of these types, then NEUTRAL is returned.
     * This is a lightweight function for use when only the base direction is
     * needed and no further bidi processing of the text is needed.
     * @param paragraph the text whose paragraph level direction is needed.
     * @return LTR, RTL, NEUTRAL
     * @see #LTR
     * @see #RTL
     * @see #NEUTRAL
     */
    public static byte getBaseDirection(CharSequence paragraph) {
        if (paragraph == null || paragraph.length() == 0) {
            return NEUTRAL;
        }

        int length = paragraph.length();
        int c;// codepoint
        byte direction;

        for (int i = 0; i < length; ) {
            // U16_NEXT(paragraph, i, length, c) for C++
            c = UCharacter.codePointAt(paragraph, i);
            direction = UCharacter.getDirectionality(c);
            if (direction == UCharacterDirection.LEFT_TO_RIGHT) {
                return LTR;
            } else if (direction == UCharacterDirection.RIGHT_TO_LEFT
                || direction == UCharacterDirection.RIGHT_TO_LEFT_ARABIC) {
                return RTL;
            }

            i = UCharacter.offsetByCodePoints(paragraph, i, 1);// set i to the head index of next codepoint
        }
        return NEUTRAL;
    }

/* perform (P2)..(P3) ------------------------------------------------------- */

    /**
     * Returns the directionality of the first strong character
     * after the last B in prologue, if any.
     * Requires prologue!=null.
     */
    private byte firstL_R_AL() {
        byte result = ON;
        for (int i = 0; i < prologue.length(); ) {
            int uchar = prologue.codePointAt(i);
            i += Character.charCount(uchar);
            byte dirProp = (byte)getCustomizedClass(uchar);
            if (result == ON) {
                if (dirProp == L || dirProp == R || dirProp == AL) {
                    result = dirProp;
                }
            } else {
                if (dirProp == B) {
                    result = ON;
                }
            }
        }
        return result;
    }

    /*
     * Check that there are enough entries in the arrays paras_limit and paras_level
     */
    private void checkParaCount() {
        int[] saveLimits;
        byte[] saveLevels;
        int count = paraCount;
        if (count <= paras_level.length)
            return;
        int oldLength = paras_level.length;
        saveLimits = paras_limit;
        saveLevels = paras_level;
        try {
            paras_limit = new int[count * 2];
            paras_level = new byte[count * 2];
        } catch (Exception e) {
            throw new OutOfMemoryError("Failed to allocate memory for paras");
        }
        System.arraycopy(saveLimits, 0, paras_limit, 0, oldLength);
        System.arraycopy(saveLevels, 0, paras_level, 0, oldLength);
    }

    /*
     * Get the directional properties for the text, calculate the flags bit-set, and
     * determine the paragraph level if necessary (in paras_level[i]).
     * FSI initiators are also resolved and their dirProp replaced with LRI or RLI.
     * When encountering an FSI, it is initially replaced with an LRI, which is the
     * default. Only if a strong R or AL is found within its scope will the LRI be
     * replaced by an RLI.
     */
    static final int NOT_SEEKING_STRONG = 0;        /* 0: not contextual paraLevel, not after FSI */
    static final int SEEKING_STRONG_FOR_PARA = 1;   /* 1: looking for first strong char in para */
    static final int SEEKING_STRONG_FOR_FSI = 2;    /* 2: looking for first strong after FSI */
    static final int LOOKING_FOR_PDI = 3;           /* 3: found strong after FSI, looking for PDI */

    private void getDirProps()
    {
        int i = 0, i0, i1;
        flags = 0;          /* collect all directionalities in the text */
        int uchar;
        byte dirProp;
        byte defaultParaLevel = 0;   /* initialize to avoid compiler warnings */
        boolean isDefaultLevel = IsDefaultLevel(paraLevel);
        /* for inverse Bidi, the default para level is set to RTL if there is a
           strong R or AL character at either end of the text                */
        boolean isDefaultLevelInverse=isDefaultLevel &&
                (reorderingMode == REORDER_INVERSE_LIKE_DIRECT ||
                 reorderingMode == REORDER_INVERSE_FOR_NUMBERS_SPECIAL);
        lastArabicPos = -1;
        int controlCount = 0;
        boolean removeBidiControls = (reorderingOptions & OPTION_REMOVE_CONTROLS) != 0;

        byte state;
        byte lastStrong = ON;           /* for default level & inverse Bidi */
    /* The following stacks are used to manage isolate sequences. Those
       sequences may be nested, but obviously never more deeply than the
       maximum explicit embedding level.
       lastStack is the index of the last used entry in the stack. A value of -1
       means that there is no open isolate sequence.
       lastStack is reset to -1 on paragraph boundaries. */
    /* The following stack contains the position of the initiator of
       each open isolate sequence */
        int[] isolateStartStack= new int[MAX_EXPLICIT_LEVEL+1];
    /* The following stack contains the last known state before
       encountering the initiator of an isolate sequence */
        byte[] previousStateStack = new byte[MAX_EXPLICIT_LEVEL+1];
        int  stackLast=-1;

        if ((reorderingOptions & OPTION_STREAMING) != 0)
            length = 0;
        defaultParaLevel = (byte)(paraLevel & 1);

        if (isDefaultLevel) {
            paras_level[0] = defaultParaLevel;
            lastStrong = defaultParaLevel;
            if (prologue != null &&                        /* there is a prologue */
                (dirProp = firstL_R_AL()) != ON) {     /* with a strong character */
                if (dirProp == L)
                    paras_level[0] = 0;             /* set the default para level */
                else
                    paras_level[0] = 1;             /* set the default para level */
                state = NOT_SEEKING_STRONG;
            } else {
                state = SEEKING_STRONG_FOR_PARA;
            }
        } else {
            paras_level[0] = paraLevel;
            state = NOT_SEEKING_STRONG;
        }
        /* count paragraphs and determine the paragraph level (P2..P3) */
        /*
         * see comment on constant fields:
         * the LEVEL_DEFAULT_XXX values are designed so that
         * their low-order bit alone yields the intended default
         */

        for (i = 0; i < originalLength; /* i is incremented in the loop */) {
            i0 = i;                     /* index of first code unit */
            uchar = UTF16.charAt(text, 0, originalLength, i);
            i += UTF16.getCharCount(uchar);
            i1 = i - 1; /* index of last code unit, gets the directional property */

            dirProp = (byte)getCustomizedClass(uchar);
            flags |= DirPropFlag(dirProp);
            dirProps[i1] = dirProp;
            if (i1 > i0) {     /* set previous code units' properties to BN */
                flags |= DirPropFlag(BN);
                do {
                    dirProps[--i1] = BN;
                } while (i1 > i0);
            }
            if (removeBidiControls && IsBidiControlChar(uchar)) {
                controlCount++;
            }
            if (dirProp == L) {
                if (state == SEEKING_STRONG_FOR_PARA) {
                    paras_level[paraCount - 1] = 0;
                    state = NOT_SEEKING_STRONG;
                }
                else if (state == SEEKING_STRONG_FOR_FSI) {
                    if (stackLast <= MAX_EXPLICIT_LEVEL) {
                        /* no need for next statement, already set by default */
                        /* dirProps[isolateStartStack[stackLast]] = LRI; */
                        flags |= DirPropFlag(LRI);
                    }
                    state = LOOKING_FOR_PDI;
                }
                lastStrong = L;
                continue;
            }
            if (dirProp == R || dirProp == AL) {
                if (state == SEEKING_STRONG_FOR_PARA) {
                    paras_level[paraCount - 1] = 1;
                    state = NOT_SEEKING_STRONG;
                }
                else if (state == SEEKING_STRONG_FOR_FSI) {
                    if (stackLast <= MAX_EXPLICIT_LEVEL) {
                        dirProps[isolateStartStack[stackLast]] = RLI;
                        flags |= DirPropFlag(RLI);
                    }
                    state = LOOKING_FOR_PDI;
                }
                lastStrong = R;
                if (dirProp == AL)
                    lastArabicPos = i - 1;
                continue;
            }
            if (dirProp >= FSI && dirProp <= RLI) { /* FSI, LRI or RLI */
                stackLast++;
                if (stackLast <= MAX_EXPLICIT_LEVEL) {
                    isolateStartStack[stackLast] = i - 1;
                    previousStateStack[stackLast] = state;
                }
                if (dirProp == FSI) {
                    dirProps[i-1] = LRI;    /* default if no strong char */
                    state = SEEKING_STRONG_FOR_FSI;
                }
                else
                    state = LOOKING_FOR_PDI;
                continue;
            }
            if (dirProp == PDI) {
                if (state == SEEKING_STRONG_FOR_FSI) {
                    if (stackLast <= MAX_EXPLICIT_LEVEL) {
                        /* no need for next statement, already set by default */
                        /* dirProps[isolateStartStack[stackLast]] = LRI; */
                        flags |= DirPropFlag(LRI);
                    }
                }
                if (stackLast >= 0) {
                    if (stackLast <= MAX_EXPLICIT_LEVEL)
                        state = previousStateStack[stackLast];
                    stackLast--;
                }
                continue;
            }
            if (dirProp == B) {
                if (i < originalLength && uchar == CR && text[i] == LF) /* do nothing on the CR */
                    continue;
                paras_limit[paraCount - 1] = i;
                if (isDefaultLevelInverse && lastStrong == R)
                    paras_level[paraCount - 1] = 1;
                if ((reorderingOptions & OPTION_STREAMING) != 0) {
                /* When streaming, we only process whole paragraphs
                   thus some updates are only done on paragraph boundaries */
                   length = i;          /* i is index to next character */
                   this.controlCount = controlCount;
                }
                if (i < originalLength) {       /* B not last char in text */
                    paraCount++;
                    checkParaCount();   /* check that there is enough memory for a new para entry */
                    if (isDefaultLevel) {
                        paras_level[paraCount - 1] = defaultParaLevel;
                        state = SEEKING_STRONG_FOR_PARA;
                        lastStrong = defaultParaLevel;
                    } else {
                        paras_level[paraCount - 1] = paraLevel;
                        state = NOT_SEEKING_STRONG;
                    }
                    stackLast = -1;
                }
                continue;
            }
        }
        /* +Ignore still open isolate sequences with overflow */
        if (stackLast > MAX_EXPLICIT_LEVEL) {
            stackLast = MAX_EXPLICIT_LEVEL;
            state=SEEKING_STRONG_FOR_FSI;   /* to be on the safe side */
        }
        /* Resolve direction of still unresolved open FSI sequences */
        while (stackLast >= 0) {
            if (state == SEEKING_STRONG_FOR_FSI) {
                /* no need for next statement, already set by default */
                /* dirProps[isolateStartStack[stackLast]] = LRI; */
                flags |= DirPropFlag(LRI);
                break;
            }
            state = previousStateStack[stackLast];
            stackLast--;
        }
        /* When streaming, ignore text after the last paragraph separator */
        if ((reorderingOptions & OPTION_STREAMING) != 0) {
            if (length < originalLength)
                paraCount--;
        } else {
            paras_limit[paraCount - 1] = originalLength;
            this.controlCount = controlCount;
        }
        /* For inverse bidi, default para direction is RTL if there is
           a strong R or AL at either end of the paragraph */
        if (isDefaultLevelInverse && lastStrong == R) {
            paras_level[paraCount - 1] = 1;
        }
        if (isDefaultLevel) {
            paraLevel = paras_level[0];
        }
        /* The following is needed to resolve the text direction for default level
           paragraphs containing no strong character */
        for (i = 0; i < paraCount; i++)
            flags |= DirPropFlagLR(paras_level[i]);

        if (orderParagraphsLTR && (flags & DirPropFlag(B)) != 0) {
            flags |= DirPropFlag(L);
        }
    }

    /* determine the paragraph level at position index */
    byte GetParaLevelAt(int pindex)
    {
        if (defaultParaLevel == 0 || pindex < paras_limit[0])
            return paraLevel;
        int i;
        for (i = 1; i < paraCount; i++)
            if (pindex < paras_limit[i])
                break;
        if (i >= paraCount)
            i = paraCount - 1;
        return paras_level[i];
    }

    /* Functions for handling paired brackets ----------------------------------- */

    /* In the isoRuns array, the first entry is used for text outside of any
       isolate sequence.  Higher entries are used for each more deeply nested
       isolate sequence. isoRunLast is the index of the last used entry.  The
       openings array is used to note the data of opening brackets not yet
       matched by a closing bracket, or matched but still susceptible to change
       level.
       Each isoRun entry contains the index of the first and
       one-after-last openings entries for pending opening brackets it
       contains.  The next openings entry to use is the one-after-last of the
       most deeply nested isoRun entry.
       isoRun entries also contain their current embedding level and the last
       encountered strong character, since these will be needed to resolve
       the level of paired brackets.  */

    private void bracketInit(BracketData bd) {
        bd.isoRunLast = 0;
        bd.isoRuns[0] = new IsoRun();
        bd.isoRuns[0].start = 0;
        bd.isoRuns[0].limit = 0;
        bd.isoRuns[0].level = GetParaLevelAt(0);
        bd.isoRuns[0].lastStrong = bd.isoRuns[0].lastBase = bd.isoRuns[0].contextDir = (byte)(GetParaLevelAt(0) & 1);
        bd.isoRuns[0].contextPos = 0;
        bd.openings = new Opening[SIMPLE_OPENINGS_COUNT];
        bd.isNumbersSpecial = reorderingMode == REORDER_NUMBERS_SPECIAL ||
                              reorderingMode == REORDER_INVERSE_FOR_NUMBERS_SPECIAL;
    }

    /* paragraph boundary */
    private void bracketProcessB(BracketData bd, byte level) {
        bd.isoRunLast = 0;
        bd.isoRuns[0].limit = 0;
        bd.isoRuns[0].level = level;
        bd.isoRuns[0].lastStrong = bd.isoRuns[0].lastBase = bd.isoRuns[0].contextDir = (byte)(level & 1);
        bd.isoRuns[0].contextPos = 0;
    }

    /* LRE, LRO, RLE, RLO, PDF */
    private void bracketProcessBoundary(BracketData bd, int lastCcPos,
                                        byte contextLevel, byte embeddingLevel) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        if ((DirPropFlag(dirProps[lastCcPos]) & MASK_ISO) != 0) /* after an isolate */
            return;
        if (NoOverride(embeddingLevel) > NoOverride(contextLevel))  /* not a PDF */
            contextLevel = embeddingLevel;
        pLastIsoRun.limit = pLastIsoRun.start;
        pLastIsoRun.level = embeddingLevel;
        pLastIsoRun.lastStrong = pLastIsoRun.lastBase = pLastIsoRun.contextDir = (byte)(contextLevel & 1);
        pLastIsoRun.contextPos = lastCcPos;
    }

    /* LRI or RLI */
    private void bracketProcessLRI_RLI(BracketData bd, byte level) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        short lastLimit;
        pLastIsoRun.lastBase = ON;
        lastLimit = pLastIsoRun.limit;
        bd.isoRunLast++;
        pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        if (pLastIsoRun == null)
            pLastIsoRun = bd.isoRuns[bd.isoRunLast] = new IsoRun();
        pLastIsoRun.start = pLastIsoRun.limit = lastLimit;
        pLastIsoRun.level = level;
        pLastIsoRun.lastStrong = pLastIsoRun.lastBase = pLastIsoRun.contextDir = (byte)(level & 1);
        pLastIsoRun.contextPos = 0;
    }

    /* PDI */
    private void bracketProcessPDI(BracketData bd) {
        IsoRun pLastIsoRun;
        bd.isoRunLast--;
        pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        pLastIsoRun.lastBase = ON;
    }

    /* newly found opening bracket: create an openings entry */
    private void bracketAddOpening(BracketData bd, char match, int position) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        Opening pOpening;
        if (pLastIsoRun.limit >= bd.openings.length) {  /* no available new entry */
            Opening[] saveOpenings = bd.openings;
            int count;
            try {
                count = bd.openings.length;
                bd.openings = new Opening[count * 2];
            } catch (Exception e) {
                throw new OutOfMemoryError("Failed to allocate memory for openings");
            }
            System.arraycopy(saveOpenings, 0, bd.openings, 0, count);
        }
        pOpening = bd.openings[pLastIsoRun.limit];
        if (pOpening == null)
            pOpening = bd.openings[pLastIsoRun.limit]= new Opening();
        pOpening.position = position;
        pOpening.match = match;
        pOpening.contextDir = pLastIsoRun.contextDir;
        pOpening.contextPos = pLastIsoRun.contextPos;
        pOpening.flags = 0;
        pLastIsoRun.limit++;
    }

    /* change N0c1 to N0c2 when a preceding bracket is assigned the embedding level */
    private void fixN0c(BracketData bd, int openingIndex, int newPropPosition, byte newProp) {
        /* This function calls itself recursively */
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        Opening qOpening;
        int k, openingPosition, closingPosition;
        for (k = openingIndex+1; k < pLastIsoRun.limit; k++) {
            qOpening = bd.openings[k];
            if (qOpening.match >= 0)    /* not an N0c match */
                continue;
            if (newPropPosition < qOpening.contextPos)
                break;
            if (newPropPosition >= qOpening.position)
                continue;
            if (newProp == qOpening.contextDir)
                break;
            openingPosition = qOpening.position;
            dirProps[openingPosition] = newProp;
            closingPosition = -(qOpening.match);
            dirProps[closingPosition] = newProp;
            qOpening.match = 0;                                 /* prevent further changes */
            fixN0c(bd, k, openingPosition, newProp);
            fixN0c(bd, k, closingPosition, newProp);
        }
    }

    /* process closing bracket; return L or R if N0b or N0c, ON if N0d */
    private byte bracketProcessClosing(BracketData bd, int openIdx, int position) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        Opening pOpening, qOpening;
        byte direction;
        boolean stable;
        byte newProp;
        pOpening = bd.openings[openIdx];
        direction = (byte)(pLastIsoRun.level & 1);
        stable = true;          /* assume stable until proved otherwise */

        /* The stable flag is set when brackets are paired and their
           level is resolved and cannot be changed by what will be
           found later in the source string.
           An unstable match can occur only when applying N0c, where
           the resolved level depends on the preceding context, and
           this context may be affected by text occurring later.
           Example: RTL paragraph containing:  abc[(latin) HEBREW]
           When the closing parenthesis is encountered, it appears
           that N0c1 must be applied since 'abc' sets an opposite
           direction context and both parentheses receive level 2.
           However, when the closing square bracket is processed,
           N0b applies because of 'HEBREW' being included within the
           brackets, thus the square brackets are treated like R and
           receive level 1. However, this changes the preceding
           context of the opening parenthesis, and it now appears
           that N0c2 must be applied to the parentheses rather than
           N0c1. */

            if ((direction == 0 && (pOpening.flags & FOUND_L) > 0) ||
                (direction == 1 && (pOpening.flags & FOUND_R) > 0)) {   /* N0b */
                newProp = direction;
            }
            else if ((pOpening.flags & (FOUND_L | FOUND_R)) != 0) {     /* N0c */
                    /* it is stable if there is no preceding text or in
                       conditions too complicated and not worth checking */
                    stable = (openIdx == pLastIsoRun.start);
                if (direction != pOpening.contextDir)
                    newProp = pOpening.contextDir;                      /* N0c1 */
                else
                    newProp = direction;                                /* N0c2 */
            } else {
            /* forget this and any brackets nested within this pair */
            pLastIsoRun.limit = (short)openIdx;
            return ON;                                                  /* N0d */
        }
        dirProps[pOpening.position] = newProp;
        dirProps[position] = newProp;
        /* Update nested N0c pairs that may be affected */
        fixN0c(bd, openIdx, pOpening.position, newProp);
        if (stable) {
            pLastIsoRun.limit = (short)openIdx; /* forget any brackets nested within this pair */
            /* remove lower located synonyms if any */
            while (pLastIsoRun.limit > pLastIsoRun.start &&
                   bd.openings[pLastIsoRun.limit - 1].position == pOpening.position)
                pLastIsoRun.limit--;
        } else {
            int k;
            pOpening.match = -position;
            /* neutralize lower located synonyms if any */
            k = openIdx - 1;
            while (k >= pLastIsoRun.start &&
                   bd.openings[k].position == pOpening.position)
                bd.openings[k--].match = 0;
            /* neutralize any unmatched opening between the current pair;
               this will also neutralize higher located synonyms if any */
            for (k = openIdx + 1; k < pLastIsoRun.limit; k++) {
                qOpening =bd.openings[k];
                if (qOpening.position >= position)
                    break;
                if (qOpening.match > 0)
                    qOpening.match = 0;
            }
        }
        return newProp;
    }

    /* handle strong characters, digits and candidates for closing brackets */
    private void bracketProcessChar(BracketData bd, int position) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        byte dirProp, newProp;
        byte level;
        dirProp = dirProps[position];
        if (dirProp == ON) {
            char c, match;
            int idx;
            /* First see if it is a matching closing bracket. Hopefully, this is
               more efficient than checking if it is a closing bracket at all */
            c = text[position];
            for (idx = pLastIsoRun.limit - 1; idx >= pLastIsoRun.start; idx--) {
                if (bd.openings[idx].match != c)
                    continue;
                /* We have a match */
                newProp = bracketProcessClosing(bd, idx, position);
                if(newProp == ON) {         /* N0d */
                    c = 0;          /* prevent handling as an opening */
                    break;
                }
                pLastIsoRun.lastBase = ON;
                pLastIsoRun.contextDir = newProp;
                pLastIsoRun.contextPos = position;
                level = levels[position];
                if ((level & LEVEL_OVERRIDE) != 0) {    /* X4, X5 */
                    short flag;
                    int i;
                    newProp = (byte)(level & 1);
                    pLastIsoRun.lastStrong = newProp;
                    flag = (short)DirPropFlag(newProp);
                    for (i = pLastIsoRun.start; i < idx; i++)
                        bd.openings[i].flags |= flag;
                    /* matching brackets are not overridden by LRO/RLO */
                    levels[position] &= ~LEVEL_OVERRIDE;
                }
                /* matching brackets are not overridden by LRO/RLO */
                levels[bd.openings[idx].position] &= ~LEVEL_OVERRIDE;
                return;
            }
            /* We get here only if the ON character is not a matching closing
               bracket or it is a case of N0d */
            /* Now see if it is an opening bracket */
            if (c != 0)
                match = (char)UCharacter.getBidiPairedBracket(c); /* get the matching char */
            else
                match = 0;
            if (match != c &&               /* has a matching char */
                UCharacter.getIntPropertyValue(c, UProperty.BIDI_PAIRED_BRACKET_TYPE) ==
                    /* opening bracket */         UCharacter.BidiPairedBracketType.OPEN) {
                /* special case: process synonyms
                   create an opening entry for each synonym */
                if (match == 0x232A) {      /* RIGHT-POINTING ANGLE BRACKET */
                    bracketAddOpening(bd, (char)0x3009, position);
                }
                else if (match == 0x3009) { /* RIGHT ANGLE BRACKET */
                    bracketAddOpening(bd, (char)0x232A, position);
                }
                bracketAddOpening(bd, match, position);
            }
        }
        level = levels[position];
        if ((level & LEVEL_OVERRIDE) != 0) {    /* X4, X5 */
            newProp = (byte)(level & 1);
            if (dirProp != S && dirProp != WS && dirProp != ON)
                dirProps[position] = newProp;
            pLastIsoRun.lastBase = newProp;
            pLastIsoRun.lastStrong = newProp;
            pLastIsoRun.contextDir = newProp;
            pLastIsoRun.contextPos = position;
        }
        else if (dirProp <= R || dirProp == AL) {
            newProp = DirFromStrong(dirProp);
            pLastIsoRun.lastBase = dirProp;
            pLastIsoRun.lastStrong = dirProp;
            pLastIsoRun.contextDir = newProp;
            pLastIsoRun.contextPos = position;
        }
        else if(dirProp == EN) {
            pLastIsoRun.lastBase = EN;
            if (pLastIsoRun.lastStrong == L) {
                newProp = L;                    /* W7 */
                if (!bd.isNumbersSpecial)
                    dirProps[position] = ENL;
                pLastIsoRun.contextDir = L;
                pLastIsoRun.contextPos = position;
            }
            else {
                newProp = R;                    /* N0 */
                if (pLastIsoRun.lastStrong == AL)
                    dirProps[position] = AN;    /* W2 */
                else
                    dirProps[position] = ENR;
                pLastIsoRun.contextDir = R;
                pLastIsoRun.contextPos = position;
            }
        }
        else if (dirProp == AN) {
            newProp = R;                        /* N0 */
            pLastIsoRun.lastBase = AN;
            pLastIsoRun.contextDir = R;
            pLastIsoRun.contextPos = position;
        }
        else if (dirProp == NSM) {
            /* if the last real char was ON, change NSM to ON so that it
               will stay ON even if the last real char is a bracket which
               may be changed to L or R */
            newProp = pLastIsoRun.lastBase;
            if (newProp == ON)
                dirProps[position] = newProp;
        }
        else {
            newProp = dirProp;
            pLastIsoRun.lastBase = dirProp;
        }
        if (newProp <= R || newProp == AL) {
            int i;
            short flag = (short)DirPropFlag(DirFromStrong(newProp));
            for (i = pLastIsoRun.start; i < pLastIsoRun.limit; i++)
                if (position > bd.openings[i].position)
                    bd.openings[i].flags |= flag;
        }
    }

    /* perform (X1)..(X9) ------------------------------------------------------- */

    /* determine if the text is mixed-directional or single-directional */
    private byte directionFromFlags() {
        /* if the text contains AN and neutrals, then some neutrals may become RTL */
        if (!((flags & MASK_RTL) != 0 ||
              ((flags & DirPropFlag(AN)) != 0 &&
               (flags & MASK_POSSIBLE_N) != 0))) {
            return LTR;
        } else if ((flags & MASK_LTR) == 0) {
            return RTL;
        } else {
            return MIXED;
        }
    }

    /*
 * Resolve the explicit levels as specified by explicit embedding codes.
 * Recalculate the flags to have them reflect the real properties
 * after taking the explicit embeddings into account.
 *
 * The BiDi algorithm is designed to result in the same behavior whether embedding
 * levels are externally specified (from "styled text", supposedly the preferred
 * method) or set by explicit embedding codes (LRx, RLx, PDF, FSI, PDI) in the plain text.
 * That is why (X9) instructs to remove all not-isolate explicit codes (and BN).
 * However, in a real implementation, the removal of these codes and their index
 * positions in the plain text is undesirable since it would result in
 * reallocated, reindexed text.
 * Instead, this implementation leaves the codes in there and just ignores them
 * in the subsequent processing.
 * In order to get the same reordering behavior, positions with a BN or a not-isolate
 * explicit embedding code just get the same level assigned as the last "real"
 * character.
 *
 * Some implementations, not this one, then overwrite some of these
 * directionality properties at "real" same-level-run boundaries by
 * L or R codes so that the resolution of weak types can be performed on the
 * entire paragraph at once instead of having to parse it once more and
 * perform that resolution on same-level-runs.
 * This limits the scope of the implicit rules in effectively
 * the same way as the run limits.
 *
 * Instead, this implementation does not modify these codes, except for
 * paired brackets whose properties (ON) may be replaced by L or R.
 * On one hand, the paragraph has to be scanned for same-level-runs, but
 * on the other hand, this saves another loop to reset these codes,
 * or saves making and modifying a copy of dirProps[].
 *
 *
 * Note that (Pn) and (Xn) changed significantly from version 4 of the BiDi algorithm.
 *
 *
 * Handling the stack of explicit levels (Xn):
 *
 * With the BiDi stack of explicit levels, as pushed with each
 * LRE, RLE, LRO, RLO, LRI, RLI and FSI and popped with each PDF and PDI,
 * the explicit level must never exceed MAX_EXPLICIT_LEVEL.
 *
 * In order to have a correct push-pop semantics even in the case of overflows,
 * overflow counters and a valid isolate counter are used as described in UAX#9
 * section 3.3.2 "Explicit Levels and Directions".
 *
 * This implementation assumes that MAX_EXPLICIT_LEVEL is odd.
 *
 * Returns the direction
 *
 */
    private byte resolveExplicitLevels() {
        int i = 0;
        byte dirProp;
        byte level = GetParaLevelAt(0);
        byte dirct;
        isolateCount = 0;

        /* determine if the text is mixed-directional or single-directional */
        dirct = directionFromFlags();

        /* we may not need to resolve any explicit levels */
        if (dirct != MIXED) {
            /* not mixed directionality: levels don't matter - trailingWSStart will be 0 */
            return dirct;
        }
        if (reorderingMode > REORDER_LAST_LOGICAL_TO_VISUAL) {
            /* inverse BiDi: mixed, but all characters are at the same embedding level */
            /* set all levels to the paragraph level */
            int paraIndex, start, limit;
            for (paraIndex = 0; paraIndex < paraCount; paraIndex++) {
                if (paraIndex == 0)
                    start = 0;
                else
                    start = paras_limit[paraIndex - 1];
                limit = paras_limit[paraIndex];
                level = paras_level[paraIndex];
                for (i = start; i < limit; i++)
                    levels[i] =level;
            }
            return dirct;               /* no bracket matching for inverse BiDi */
        }
        if ((flags & (MASK_EXPLICIT | MASK_ISO)) == 0) {
            /* no embeddings, set all levels to the paragraph level */
            /* we still have to perform bracket matching */
            int paraIndex, start, limit;
            BracketData bracketData = new BracketData();
            bracketInit(bracketData);
            for (paraIndex = 0; paraIndex < paraCount; paraIndex++) {
                if (paraIndex == 0)
                    start = 0;
                else
                    start = paras_limit[paraIndex-1];
                limit = paras_limit[paraIndex];
                level = paras_level[paraIndex];
                for (i = start; i < limit; i++) {
                    levels[i] = level;
                    dirProp = dirProps[i];
                    if (dirProp == BN)
                        continue;
                    if (dirProp == B) {
                        if ((i + 1) < length) {
                            if (text[i] == CR && text[i + 1] == LF)
                                continue;   /* skip CR when followed by LF */
                            bracketProcessB(bracketData, level);
                        }
                        continue;
                    }
                    bracketProcessChar(bracketData, i);
                }
            }
            return dirct;
        }
        /* continue to perform (Xn) */

        /* (X1) level is set for all codes, embeddingLevel keeps track of the push/pop operations */
        /* both variables may carry the LEVEL_OVERRIDE flag to indicate the override status */
        byte embeddingLevel = level, newLevel;
        byte previousLevel = level; /* previous level for regular (not CC) characters */
        int lastCcPos = 0;          /* index of last effective LRx,RLx, PDx */

        /* The following stack remembers the embedding level and the ISOLATE flag of level runs.
           stackLast points to its current entry. */
        short[] stack = new short[MAX_EXPLICIT_LEVEL + 2];  /* we never push anything >= MAX_EXPLICIT_LEVEL
                                                               but we need one more entry as base */
        int stackLast = 0;
        int overflowIsolateCount = 0;
        int overflowEmbeddingCount = 0;
        int validIsolateCount = 0;
        BracketData bracketData = new BracketData();
        bracketInit(bracketData);
        stack[0] = level;       /* initialize base entry to para level, no override, no isolate */

        /* recalculate the flags */
        flags = 0;

        for (i = 0; i < length; i++) {
            dirProp = dirProps[i];
            switch (dirProp) {
            case LRE:
            case RLE:
            case LRO:
            case RLO:
                /* (X2, X3, X4, X5) */
                flags |= DirPropFlag(BN);
                levels[i] = previousLevel;
                if (dirProp == LRE || dirProp == LRO)
                    /* least greater even level */
                    newLevel = (byte)((embeddingLevel+2) & ~(LEVEL_OVERRIDE | 1));
                else
                    /* least greater odd level */
                    newLevel = (byte)((NoOverride(embeddingLevel) + 1) | 1);
                if (newLevel <= MAX_EXPLICIT_LEVEL && overflowIsolateCount == 0 &&
                                                      overflowEmbeddingCount == 0) {
                    lastCcPos = i;
                    embeddingLevel = newLevel;
                    if (dirProp == LRO || dirProp == RLO)
                        embeddingLevel |= LEVEL_OVERRIDE;
                    stackLast++;
                    stack[stackLast] = embeddingLevel;
                    /* we don't need to set LEVEL_OVERRIDE off for LRE and RLE
                       since this has already been done for newLevel which is
                       the source for embeddingLevel.
                     */
                } else {
                    if (overflowIsolateCount == 0)
                        overflowEmbeddingCount++;
                }
                break;
            case PDF:
                /* (X7) */
                flags |= DirPropFlag(BN);
                levels[i] = previousLevel;
                /* handle all the overflow cases first */
                if (overflowIsolateCount > 0) {
                    break;
                }
                if (overflowEmbeddingCount > 0) {
                    overflowEmbeddingCount--;
                    break;
                }
                if (stackLast > 0 && stack[stackLast] < ISOLATE) {   /* not an isolate entry */
                    lastCcPos = i;
                    stackLast--;
                    embeddingLevel = (byte)stack[stackLast];
                }
                break;
            case LRI:
            case RLI:
                flags |= DirPropFlag(ON) | DirPropFlagLR(embeddingLevel);
                levels[i] = NoOverride(embeddingLevel);
                if (NoOverride(embeddingLevel) != NoOverride(previousLevel)) {
                    bracketProcessBoundary(bracketData, lastCcPos,
                                           previousLevel, embeddingLevel);
                    flags |= DirPropFlagMultiRuns;
                }
                previousLevel = embeddingLevel;
                /* (X5a, X5b) */
                if (dirProp == LRI)
                    /* least greater even level */
                    newLevel=(byte)((embeddingLevel+2)&~(LEVEL_OVERRIDE|1));
                else
                    /* least greater odd level */
                    newLevel=(byte)((NoOverride(embeddingLevel)+1)|1);
                if (newLevel <= MAX_EXPLICIT_LEVEL && overflowIsolateCount == 0
                                                   && overflowEmbeddingCount == 0) {
                    flags |= DirPropFlag(dirProp);
                    lastCcPos = i;
                    validIsolateCount++;
                    if (validIsolateCount > isolateCount)
                        isolateCount = validIsolateCount;
                    embeddingLevel = newLevel;
                    /* we can increment stackLast without checking because newLevel
                       will exceed UBIDI_MAX_EXPLICIT_LEVEL before stackLast overflows */
                    stackLast++;
                    stack[stackLast] = (short)(embeddingLevel + ISOLATE);
                    bracketProcessLRI_RLI(bracketData, embeddingLevel);
                } else {
                    /* make it WS so that it is handled by adjustWSLevels() */
                    dirProps[i] = WS;
                    overflowIsolateCount++;
                }
                break;
            case PDI:
                if (NoOverride(embeddingLevel) != NoOverride(previousLevel)) {
                    bracketProcessBoundary(bracketData, lastCcPos,
                                           previousLevel, embeddingLevel);
                    flags |= DirPropFlagMultiRuns;
                }
                /* (X6a) */
                if (overflowIsolateCount > 0) {
                    overflowIsolateCount--;
                    /* make it WS so that it is handled by adjustWSLevels() */
                    dirProps[i] = WS;
                }
                else if (validIsolateCount > 0) {
                    flags |= DirPropFlag(PDI);
                    lastCcPos = i;
                    overflowEmbeddingCount = 0;
                    while (stack[stackLast] < ISOLATE)  /* pop embedding entries */
                        stackLast--;                    /* until the last isolate entry */
                    stackLast--;                        /* pop also the last isolate entry */
                    validIsolateCount--;
                    bracketProcessPDI(bracketData);
                } else
                    /* make it WS so that it is handled by adjustWSLevels() */
                    dirProps[i] = WS;
                embeddingLevel = (byte)(stack[stackLast] & ~ISOLATE);
                flags |= DirPropFlag(ON) | DirPropFlagLR(embeddingLevel);
                previousLevel = embeddingLevel;
                levels[i] = NoOverride(embeddingLevel);
                break;
            case B:
                flags |= DirPropFlag(B);
                levels[i] = GetParaLevelAt(i);
                if ((i + 1) < length) {
                    if (text[i] == CR && text[i + 1] == LF)
                        break;          /* skip CR when followed by LF */
                    overflowEmbeddingCount = overflowIsolateCount = 0;
                    validIsolateCount = 0;
                    stackLast = 0;
                    previousLevel = embeddingLevel = GetParaLevelAt(i + 1);
                    stack[0] = embeddingLevel;   /* initialize base entry to para level, no override, no isolate */
                    bracketProcessB(bracketData, embeddingLevel);
                }
                break;
            case BN:
                /* BN, LRE, RLE, and PDF are supposed to be removed (X9) */
                /* they will get their levels set correctly in adjustWSLevels() */
                levels[i] = previousLevel;
                flags |= DirPropFlag(BN);
                break;
            default:
                /* all other types are normal characters and get the "real" level */
                if (NoOverride(embeddingLevel) != NoOverride(previousLevel)) {
                    bracketProcessBoundary(bracketData, lastCcPos,
                                           previousLevel, embeddingLevel);
                    flags |= DirPropFlagMultiRuns;
                    if ((embeddingLevel & LEVEL_OVERRIDE) != 0)
                        flags |= DirPropFlagO(embeddingLevel);
                    else
                        flags |= DirPropFlagE(embeddingLevel);
                }
                previousLevel = embeddingLevel;
                levels[i] = embeddingLevel;
                bracketProcessChar(bracketData, i);
                /* the dirProp may have been changed in bracketProcessChar() */
                flags |= DirPropFlag(dirProps[i]);
                break;
            }
        }
        if ((flags & MASK_EMBEDDING) != 0) {
            flags |= DirPropFlagLR(paraLevel);
        }
        if (orderParagraphsLTR && (flags & DirPropFlag(B)) != 0) {
            flags |= DirPropFlag(L);
        }
        /* again, determine if the text is mixed-directional or single-directional */
        dirct = directionFromFlags();

        return dirct;
    }

    /*
     * Use a pre-specified embedding levels array:
     *
     * Adjust the directional properties for overrides (->LEVEL_OVERRIDE),
     * ignore all explicit codes (X9),
     * and check all the preset levels.
     *
     * Recalculate the flags to have them reflect the real properties
     * after taking the explicit embeddings into account.
     */
    private byte checkExplicitLevels() {
        byte dirProp;
        int i;
        int isolateCount = 0;

        this.flags = 0;     /* collect all directionalities in the text */
        byte level;
        this.isolateCount = 0;

        for (i = 0; i < length; ++i) {
            level = levels[i];
            dirProp = dirProps[i];
            if (dirProp == LRI || dirProp == RLI) {
                isolateCount++;
                if (isolateCount > this.isolateCount)
                    this.isolateCount = isolateCount;
            }
            else if (dirProp == PDI)
                isolateCount--;
            else if (dirProp == B)
                isolateCount = 0;
            if ((level & LEVEL_OVERRIDE) != 0) {
                /* keep the override flag in levels[i] but adjust the flags */
                level &= ~LEVEL_OVERRIDE;     /* make the range check below simpler */
                flags |= DirPropFlagO(level);
            } else {
                /* set the flags */
                flags |= DirPropFlagE(level) | DirPropFlag(dirProp);
            }
            if ((level < GetParaLevelAt(i) &&
                    !((0 == level) && (dirProp == B))) ||
                    (MAX_EXPLICIT_LEVEL < level)) {
                /* level out of bounds */
                throw new IllegalArgumentException("level " + level +
                                                   " out of bounds at " + i);
            }
        }
        if ((flags & MASK_EMBEDDING) != 0)
            flags |= DirPropFlagLR(paraLevel);
        /* determine if the text is mixed-directional or single-directional */
        return directionFromFlags();
    }

    /*********************************************************************/
    /* The Properties state machine table                                */
    /*********************************************************************/
    /*                                                                   */
    /* All table cells are 8 bits:                                       */
    /*      bits 0..4:  next state                                       */
    /*      bits 5..7:  action to perform (if > 0)                       */
    /*                                                                   */
    /* Cells may be of format "n" where n represents the next state      */
    /* (except for the rightmost column).                                */
    /* Cells may also be of format "_(x,y)" where x represents an action */
    /* to perform and y represents the next state.                       */
    /*                                                                   */
    /*********************************************************************/
    /* Definitions and type for properties state tables                  */
    /*********************************************************************/
    private static final int IMPTABPROPS_COLUMNS = 16;
    private static final int IMPTABPROPS_RES = IMPTABPROPS_COLUMNS - 1;
    private static short GetStateProps(short cell) {
        return (short)(cell & 0x1f);
    }
    private static short GetActionProps(short cell) {
        return (short)(cell >> 5);
    }

    private static final short groupProp[] =          /* dirProp regrouped */
    {
        /*  L   R   EN  ES  ET  AN  CS  B   S   WS  ON  LRE LRO AL  RLE RLO PDF NSM BN  FSI LRI RLI PDI ENL ENR */
            0,  1,  2,  7,  8,  3,  9,  6,  5,  4,  4,  10, 10, 12, 10, 10, 10, 11, 10, 4,  4,  4,  4,  13, 14
    };
    private static final short _L  = 0;
    private static final short _R  = 1;
    private static final short _EN = 2;
    private static final short _AN = 3;
    private static final short _ON = 4;
    private static final short _S  = 5;
    private static final short _B  = 6; /* reduced dirProp */

    /*********************************************************************/
    /*                                                                   */
    /*      PROPERTIES  STATE  TABLE                                     */
    /*                                                                   */
    /* In table impTabProps,                                             */
    /*      - the ON column regroups ON and WS, FSI, RLI, LRI and PDI    */
    /*      - the BN column regroups BN, LRE, RLE, LRO, RLO, PDF         */
    /*      - the Res column is the reduced property assigned to a run   */
    /*                                                                   */
    /* Action 1: process current run1, init new run1                     */
    /*        2: init new run2                                           */
    /*        3: process run1, process run2, init new run1               */
    /*        4: process run1, set run1=run2, init new run2              */
    /*                                                                   */
    /* Notes:                                                            */
    /*  1) This table is used in resolveImplicitLevels().                */
    /*  2) This table triggers actions when there is a change in the Bidi*/
    /*     property of incoming characters (action 1).                   */
    /*  3) Most such property sequences are processed immediately (in    */
    /*     fact, passed to processPropertySeq().                         */
    /*  4) However, numbers are assembled as one sequence. This means    */
    /*     that undefined situations (like CS following digits, until    */
    /*     it is known if the next char will be a digit) are held until  */
    /*     following chars define them.                                  */
    /*     Example: digits followed by CS, then comes another CS or ON;  */
    /*              the digits will be processed, then the CS assigned   */
    /*              as the start of an ON sequence (action 3).           */
    /*  5) There are cases where more than one sequence must be          */
    /*     processed, for instance digits followed by CS followed by L:  */
    /*     the digits must be processed as one sequence, and the CS      */
    /*     must be processed as an ON sequence, all this before starting */
    /*     assembling chars for the opening L sequence.                  */
    /*                                                                   */
    /*                                                                   */
    private static final short impTabProps[][] =
    {
/*                        L,     R,    EN,    AN,    ON,     S,     B,    ES,    ET,    CS,    BN,   NSM,    AL,   ENL,   ENR,   Res */
/* 0 Init        */ {     1,     2,     4,     5,     7,    15,    17,     7,     9,     7,     0,     7,     3,    18,    21,   _ON },
/* 1 L           */ {     1,  32+2,  32+4,  32+5,  32+7, 32+15, 32+17,  32+7,  32+9,  32+7,     1,     1,  32+3, 32+18, 32+21,    _L },
/* 2 R           */ {  32+1,     2,  32+4,  32+5,  32+7, 32+15, 32+17,  32+7,  32+9,  32+7,     2,     2,  32+3, 32+18, 32+21,    _R },
/* 3 AL          */ {  32+1,  32+2,  32+6,  32+6,  32+8, 32+16, 32+17,  32+8,  32+8,  32+8,     3,     3,     3, 32+18, 32+21,    _R },
/* 4 EN          */ {  32+1,  32+2,     4,  32+5,  32+7, 32+15, 32+17, 64+10,    11, 64+10,     4,     4,  32+3,    18,    21,   _EN },
/* 5 AN          */ {  32+1,  32+2,  32+4,     5,  32+7, 32+15, 32+17,  32+7,  32+9, 64+12,     5,     5,  32+3, 32+18, 32+21,   _AN },
/* 6 AL:EN/AN    */ {  32+1,  32+2,     6,     6,  32+8, 32+16, 32+17,  32+8,  32+8, 64+13,     6,     6,  32+3,    18,    21,   _AN },
/* 7 ON          */ {  32+1,  32+2,  32+4,  32+5,     7, 32+15, 32+17,     7, 64+14,     7,     7,     7,  32+3, 32+18, 32+21,   _ON },
/* 8 AL:ON       */ {  32+1,  32+2,  32+6,  32+6,     8, 32+16, 32+17,     8,     8,     8,     8,     8,  32+3, 32+18, 32+21,   _ON },
/* 9 ET          */ {  32+1,  32+2,     4,  32+5,     7, 32+15, 32+17,     7,     9,     7,     9,     9,  32+3,    18,    21,   _ON },
/*10 EN+ES/CS    */ {  96+1,  96+2,     4,  96+5, 128+7, 96+15, 96+17, 128+7,128+14, 128+7,    10, 128+7,  96+3,    18,    21,   _EN },
/*11 EN+ET       */ {  32+1,  32+2,     4,  32+5,  32+7, 32+15, 32+17,  32+7,    11,  32+7,    11,    11,  32+3,    18,    21,   _EN },
/*12 AN+CS       */ {  96+1,  96+2,  96+4,     5, 128+7, 96+15, 96+17, 128+7,128+14, 128+7,    12, 128+7,  96+3, 96+18, 96+21,   _AN },
/*13 AL:EN/AN+CS */ {  96+1,  96+2,     6,     6, 128+8, 96+16, 96+17, 128+8, 128+8, 128+8,    13, 128+8,  96+3,    18,    21,   _AN },
/*14 ON+ET       */ {  32+1,  32+2, 128+4,  32+5,     7, 32+15, 32+17,     7,    14,     7,    14,    14,  32+3,128+18,128+21,   _ON },
/*15 S           */ {  32+1,  32+2,  32+4,  32+5,  32+7,    15, 32+17,  32+7,  32+9,  32+7,    15,  32+7,  32+3, 32+18, 32+21,    _S },
/*16 AL:S        */ {  32+1,  32+2,  32+6,  32+6,  32+8,    16, 32+17,  32+8,  32+8,  32+8,    16,  32+8,  32+3, 32+18, 32+21,    _S },
/*17 B           */ {  32+1,  32+2,  32+4,  32+5,  32+7, 32+15,    17,  32+7,  32+9,  32+7,    17,  32+7,  32+3, 32+18, 32+21,    _B },
/*18 ENL         */ {  32+1,  32+2,    18,  32+5,  32+7, 32+15, 32+17, 64+19,    20, 64+19,    18,    18,  32+3,    18,    21,    _L },
/*19 ENL+ES/CS   */ {  96+1,  96+2,    18,  96+5, 128+7, 96+15, 96+17, 128+7,128+14, 128+7,    19, 128+7,  96+3,    18,    21,    _L },
/*20 ENL+ET      */ {  32+1,  32+2,    18,  32+5,  32+7, 32+15, 32+17,  32+7,    20,  32+7,    20,    20,  32+3,    18,    21,    _L },
/*21 ENR         */ {  32+1,  32+2,    21,  32+5,  32+7, 32+15, 32+17, 64+22,    23, 64+22,    21,    21,  32+3,    18,    21,   _AN },
/*22 ENR+ES/CS   */ {  96+1,  96+2,    21,  96+5, 128+7, 96+15, 96+17, 128+7,128+14, 128+7,    22, 128+7,  96+3,    18,    21,   _AN },
/*23 ENR+ET      */ {  32+1,  32+2,    21,  32+5,  32+7, 32+15, 32+17,  32+7,    23,  32+7,    23,    23,  32+3,    18,    21,   _AN }
    };

    /*********************************************************************/
    /* The levels state machine tables                                   */
    /*********************************************************************/
    /*                                                                   */
    /* All table cells are 8 bits:                                       */
    /*      bits 0..3:  next state                                       */
    /*      bits 4..7:  action to perform (if > 0)                       */
    /*                                                                   */
    /* Cells may be of format "n" where n represents the next state      */
    /* (except for the rightmost column).                                */
    /* Cells may also be of format "_(x,y)" where x represents an action */
    /* to perform and y represents the next state.                       */
    /*                                                                   */
    /* This format limits each table to 16 states each and to 15 actions.*/
    /*                                                                   */
    /*********************************************************************/
    /* Definitions and type for levels state tables                      */
    /*********************************************************************/
    private static final int IMPTABLEVELS_COLUMNS = _B + 2;
    private static final int IMPTABLEVELS_RES = IMPTABLEVELS_COLUMNS - 1;
    private static short GetState(byte cell) { return (short)(cell & 0x0f); }
    private static short GetAction(byte cell) { return (short)(cell >> 4); }

    private static class ImpTabPair {
        byte[][][] imptab;
        short[][] impact;

        ImpTabPair(byte[][] table1, byte[][] table2,
                   short[] act1, short[] act2) {
            imptab = new byte[][][] {table1, table2};
            impact = new short[][] {act1, act2};
        }
    }

    /*********************************************************************/
    /*                                                                   */
    /*      LEVELS  STATE  TABLES                                        */
    /*                                                                   */
    /* In all levels state tables,                                       */
    /*      - state 0 is the initial state                               */
    /*      - the Res column is the increment to add to the text level   */
    /*        for this property sequence.                                */
    /*                                                                   */
    /* The impact arrays for each table of a pair map the local action   */
    /* numbers of the table to the total list of actions. For instance,  */
    /* action 2 in a given table corresponds to the action number which  */
    /* appears in entry [2] of the impact array for that table.          */
    /* The first entry of all impact arrays must be 0.                   */
    /*                                                                   */
    /* Action 1: init conditional sequence                               */
    /*        2: prepend conditional sequence to current sequence        */
    /*        3: set ON sequence to new level - 1                        */
    /*        4: init EN/AN/ON sequence                                  */
    /*        5: fix EN/AN/ON sequence followed by R                     */
    /*        6: set previous level sequence to level 2                  */
    /*                                                                   */
    /* Notes:                                                            */
    /*  1) These tables are used in processPropertySeq(). The input      */
    /*     is property sequences as determined by resolveImplicitLevels. */
    /*  2) Most such property sequences are processed immediately        */
    /*     (levels are assigned).                                        */
    /*  3) However, some sequences cannot be assigned a final level till */
    /*     one or more following sequences are received. For instance,   */
    /*     ON following an R sequence within an even-level paragraph.    */
    /*     If the following sequence is R, the ON sequence will be       */
    /*     assigned basic run level+1, and so will the R sequence.       */
    /*  4) S is generally handled like ON, since its level will be fixed */
    /*     to paragraph level in adjustWSLevels().                       */
    /*                                                                   */

    private static final byte impTabL_DEFAULT[][] = /* Even paragraph level */
        /*  In this table, conditional sequences receive the lower possible level
            until proven otherwise.
        */
    {
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 : init       */ {     0,     1,     0,     2,     0,     0,     0,  0 },
        /* 1 : R          */ {     0,     1,     3,     3,  0x14,  0x14,     0,  1 },
        /* 2 : AN         */ {     0,     1,     0,     2,  0x15,  0x15,     0,  2 },
        /* 3 : R+EN/AN    */ {     0,     1,     3,     3,  0x14,  0x14,     0,  2 },
        /* 4 : R+ON       */ {     0,  0x21,  0x33,  0x33,     4,     4,     0,  0 },
        /* 5 : AN+ON      */ {     0,  0x21,     0,  0x32,     5,     5,     0,  0 }
    };

    private static final byte impTabR_DEFAULT[][] = /* Odd  paragraph level */
        /*  In this table, conditional sequences receive the lower possible level
            until proven otherwise.
        */
    {
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 : init       */ {     1,     0,     2,     2,     0,     0,     0,  0 },
        /* 1 : L          */ {     1,     0,     1,     3,  0x14,  0x14,     0,  1 },
        /* 2 : EN/AN      */ {     1,     0,     2,     2,     0,     0,     0,  1 },
        /* 3 : L+AN       */ {     1,     0,     1,     3,     5,     5,     0,  1 },
        /* 4 : L+ON       */ {  0x21,     0,  0x21,     3,     4,     4,     0,  0 },
        /* 5 : L+AN+ON    */ {     1,     0,     1,     3,     5,     5,     0,  0 }
    };

    private static final short[] impAct0 = {0,1,2,3,4};

    private static final ImpTabPair impTab_DEFAULT = new ImpTabPair(
            impTabL_DEFAULT, impTabR_DEFAULT, impAct0, impAct0);

    private static final byte impTabL_NUMBERS_SPECIAL[][] = { /* Even paragraph level */
        /* In this table, conditional sequences receive the lower possible
           level until proven otherwise.
        */
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 : init       */ {     0,     2,  0x11,  0x11,     0,     0,     0,  0 },
        /* 1 : L+EN/AN    */ {     0,  0x42,     1,     1,     0,     0,     0,  0 },
        /* 2 : R          */ {     0,     2,     4,     4,  0x13,  0x13,     0,  1 },
        /* 3 : R+ON       */ {     0,  0x22,  0x34,  0x34,     3,     3,     0,  0 },
        /* 4 : R+EN/AN    */ {     0,     2,     4,     4,  0x13,  0x13,     0,  2 }
    };
    private static final ImpTabPair impTab_NUMBERS_SPECIAL = new ImpTabPair(
            impTabL_NUMBERS_SPECIAL, impTabR_DEFAULT, impAct0, impAct0);

    private static final byte impTabL_GROUP_NUMBERS_WITH_R[][] = {
        /* In this table, EN/AN+ON sequences receive levels as if associated with R
           until proven that there is L or sor/eor on both sides. AN is handled like EN.
        */
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 init         */ {     0,     3,  0x11,  0x11,     0,     0,     0,  0 },
        /* 1 EN/AN        */ {  0x20,     3,     1,     1,     2,  0x20,  0x20,  2 },
        /* 2 EN/AN+ON     */ {  0x20,     3,     1,     1,     2,  0x20,  0x20,  1 },
        /* 3 R            */ {     0,     3,     5,     5,  0x14,     0,     0,  1 },
        /* 4 R+ON         */ {  0x20,     3,     5,     5,     4,  0x20,  0x20,  1 },
        /* 5 R+EN/AN      */ {     0,     3,     5,     5,  0x14,     0,     0,  2 }
    };
    private static final byte impTabR_GROUP_NUMBERS_WITH_R[][] = {
        /*  In this table, EN/AN+ON sequences receive levels as if associated with R
            until proven that there is L on both sides. AN is handled like EN.
        */
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 init         */ {     2,     0,     1,     1,     0,     0,     0,  0 },
        /* 1 EN/AN        */ {     2,     0,     1,     1,     0,     0,     0,  1 },
        /* 2 L            */ {     2,     0,  0x14,  0x14,  0x13,     0,     0,  1 },
        /* 3 L+ON         */ {  0x22,     0,     4,     4,     3,     0,     0,  0 },
        /* 4 L+EN/AN      */ {  0x22,     0,     4,     4,     3,     0,     0,  1 }
    };
    private static final ImpTabPair impTab_GROUP_NUMBERS_WITH_R = new
            ImpTabPair(impTabL_GROUP_NUMBERS_WITH_R,
                       impTabR_GROUP_NUMBERS_WITH_R, impAct0, impAct0);

    private static final byte impTabL_INVERSE_NUMBERS_AS_L[][] = {
        /* This table is identical to the Default LTR table except that EN and AN
           are handled like L.
        */
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 : init       */ {     0,     1,     0,     0,     0,     0,     0,  0 },
        /* 1 : R          */ {     0,     1,     0,     0,  0x14,  0x14,     0,  1 },
        /* 2 : AN         */ {     0,     1,     0,     0,  0x15,  0x15,     0,  2 },
        /* 3 : R+EN/AN    */ {     0,     1,     0,     0,  0x14,  0x14,     0,  2 },
        /* 4 : R+ON       */ {  0x20,     1,  0x20,  0x20,     4,     4,  0x20,  1 },
        /* 5 : AN+ON      */ {  0x20,     1,  0x20,  0x20,     5,     5,  0x20,  1 }
    };
    private static final byte impTabR_INVERSE_NUMBERS_AS_L[][] = {
        /* This table is identical to the Default RTL table except that EN and AN
           are handled like L.
        */
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 : init       */ {     1,     0,     1,     1,     0,     0,     0,  0 },
        /* 1 : L          */ {     1,     0,     1,     1,  0x14,  0x14,     0,  1 },
        /* 2 : EN/AN      */ {     1,     0,     1,     1,     0,     0,     0,  1 },
        /* 3 : L+AN       */ {     1,     0,     1,     1,     5,     5,     0,  1 },
        /* 4 : L+ON       */ {  0x21,     0,  0x21,  0x21,     4,     4,     0,  0 },
        /* 5 : L+AN+ON    */ {     1,     0,     1,     1,     5,     5,     0,  0 }
    };
    private static final ImpTabPair impTab_INVERSE_NUMBERS_AS_L = new ImpTabPair
            (impTabL_INVERSE_NUMBERS_AS_L, impTabR_INVERSE_NUMBERS_AS_L,
             impAct0, impAct0);

    private static final byte impTabR_INVERSE_LIKE_DIRECT[][] = {  /* Odd  paragraph level */
        /*  In this table, conditional sequences receive the lower possible level
            until proven otherwise.
        */
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 : init       */ {     1,     0,     2,     2,     0,     0,     0,  0 },
        /* 1 : L          */ {     1,     0,     1,     2,  0x13,  0x13,     0,  1 },
        /* 2 : EN/AN      */ {     1,     0,     2,     2,     0,     0,     0,  1 },
        /* 3 : L+ON       */ {  0x21,  0x30,     6,     4,     3,     3,  0x30,  0 },
        /* 4 : L+ON+AN    */ {  0x21,  0x30,     6,     4,     5,     5,  0x30,  3 },
        /* 5 : L+AN+ON    */ {  0x21,  0x30,     6,     4,     5,     5,  0x30,  2 },
        /* 6 : L+ON+EN    */ {  0x21,  0x30,     6,     4,     3,     3,  0x30,  1 }
    };
    private static final short[] impAct1 = {0,1,13,14};
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT = new ImpTabPair(
            impTabL_DEFAULT, impTabR_INVERSE_LIKE_DIRECT, impAct0, impAct1);

    private static final byte impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS[][] = {
        /* The case handled in this table is (visually):  R EN L
         */
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 : init       */ {     0,  0x63,     0,     1,     0,     0,     0,  0 },
        /* 1 : L+AN       */ {     0,  0x63,     0,     1,  0x12,  0x30,     0,  4 },
        /* 2 : L+AN+ON    */ {  0x20,  0x63,  0x20,     1,     2,  0x30,  0x20,  3 },
        /* 3 : R          */ {     0,  0x63,  0x55,  0x56,  0x14,  0x30,     0,  3 },
        /* 4 : R+ON       */ {  0x30,  0x43,  0x55,  0x56,     4,  0x30,  0x30,  3 },
        /* 5 : R+EN       */ {  0x30,  0x43,     5,  0x56,  0x14,  0x30,  0x30,  4 },
        /* 6 : R+AN       */ {  0x30,  0x43,  0x55,     6,  0x14,  0x30,  0x30,  4 }
    };
    private static final byte impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS[][] = {
        /* The cases handled in this table are (visually):  R EN L
                                                            R L AN L
        */
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 : init       */ {  0x13,     0,     1,     1,     0,     0,     0,  0 },
        /* 1 : R+EN/AN    */ {  0x23,     0,     1,     1,     2,  0x40,     0,  1 },
        /* 2 : R+EN/AN+ON */ {  0x23,     0,     1,     1,     2,  0x40,     0,  0 },
        /* 3 : L          */ {     3,     0,     3,  0x36,  0x14,  0x40,     0,  1 },
        /* 4 : L+ON       */ {  0x53,  0x40,     5,  0x36,     4,  0x40,  0x40,  0 },
        /* 5 : L+ON+EN    */ {  0x53,  0x40,     5,  0x36,     4,  0x40,  0x40,  1 },
        /* 6 : L+AN       */ {  0x53,  0x40,     6,     6,     4,  0x40,  0x40,  3 }
    };
    private static final short[] impAct2 = {0,1,2,5,6,7,8};
    private static final short[] impAct3 = {0,1,9,10,11,12};
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT_WITH_MARKS =
            new ImpTabPair(impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS,
                           impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS, impAct2, impAct3);

    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL = new ImpTabPair(
            impTabL_NUMBERS_SPECIAL, impTabR_INVERSE_LIKE_DIRECT, impAct0, impAct1);

    private static final byte impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS[][] = {
        /*  The case handled in this table is (visually):  R EN L
        */
        /*                         L,     R,    EN,    AN,    ON,     S,     B, Res */
        /* 0 : init       */ {     0,  0x62,     1,     1,     0,     0,     0,  0 },
        /* 1 : L+EN/AN    */ {     0,  0x62,     1,     1,     0,  0x30,     0,  4 },
        /* 2 : R          */ {     0,  0x62,  0x54,  0x54,  0x13,  0x30,     0,  3 },
        /* 3 : R+ON       */ {  0x30,  0x42,  0x54,  0x54,     3,  0x30,  0x30,  3 },
        /* 4 : R+EN/AN    */ {  0x30,  0x42,     4,     4,  0x13,  0x30,  0x30,  4 }
    };
    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS = new
            ImpTabPair(impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS,
                       impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS, impAct2, impAct3);

    private static class LevState {
        byte[][] impTab;                /* level table pointer          */
        short[] impAct;                 /* action map array             */
        int startON;                    /* start of ON sequence         */
        int startL2EN;                  /* start of level 2 sequence    */
        int lastStrongRTL;              /* index of last found R or AL  */
        int runStart;                   /* start position of the run    */
        short state;                    /* current state                */
        byte runLevel;                  /* run level before implicit solving */
    }

    /*------------------------------------------------------------------------*/

    static final int FIRSTALLOC = 10;
    /*
     *  param pos:     position where to insert
     *  param flag:    one of LRM_BEFORE, LRM_AFTER, RLM_BEFORE, RLM_AFTER
     */
    private void addPoint(int pos, int flag)
    {
        Point point = new Point();

        int len = insertPoints.points.length;
        if (len == 0) {
            insertPoints.points = new Point[FIRSTALLOC];
            len = FIRSTALLOC;
        }
        if (insertPoints.size >= len) { /* no room for new point */
            Point[] savePoints = insertPoints.points;
            insertPoints.points = new Point[len * 2];
            System.arraycopy(savePoints, 0, insertPoints.points, 0, len);
        }
        point.pos = pos;
        point.flag = flag;
        insertPoints.points[insertPoints.size] = point;
        insertPoints.size++;
    }

    private void setLevelsOutsideIsolates(int start, int limit, byte level)
    {
        byte dirProp;
        int  isolateCount = 0, k;
        for (k = start; k < limit; k++) {
            dirProp = dirProps[k];
            if (dirProp == PDI)
                isolateCount--;
            if (isolateCount == 0)
                levels[k] = level;
            if (dirProp == LRI || dirProp == RLI)
                isolateCount++;
        }
    }

    /* perform rules (Wn), (Nn), and (In) on a run of the text ------------------ */

    /*
     * This implementation of the (Wn) rules applies all rules in one pass.
     * In order to do so, it needs a look-ahead of typically 1 character
     * (except for W5: sequences of ET) and keeps track of changes
     * in a rule Wp that affect a later Wq (p<q).
     *
     * The (Nn) and (In) rules are also performed in that same single loop,
     * but effectively one iteration behind for white space.
     *
     * Since all implicit rules are performed in one step, it is not necessary
     * to actually store the intermediate directional properties in dirProps[].
     */

    private void processPropertySeq(LevState levState, short _prop,
            int start, int limit) {
        byte cell;
        byte[][] impTab = levState.impTab;
        short[] impAct = levState.impAct;
        short oldStateSeq,actionSeq;
        byte level, addLevel;
        int start0, k;

        start0 = start;                 /* save original start position */
        oldStateSeq = levState.state;
        cell = impTab[oldStateSeq][_prop];
        levState.state = GetState(cell);        /* isolate the new state */
        actionSeq = impAct[GetAction(cell)];    /* isolate the action */
        addLevel = impTab[levState.state][IMPTABLEVELS_RES];

        if (actionSeq != 0) {
            switch (actionSeq) {
            case 1:                     /* init ON seq */
                levState.startON = start0;
                break;

            case 2:                     /* prepend ON seq to current seq */
                start = levState.startON;
                break;

            case 3:                     /* EN/AN after R+ON */
                level = (byte)(levState.runLevel + 1);
                setLevelsOutsideIsolates(levState.startON, start0, level);
                break;

            case 4:                     /* EN/AN before R for NUMBERS_SPECIAL */
                level = (byte)(levState.runLevel + 2);
                setLevelsOutsideIsolates(levState.startON, start0, level);
                break;

            case 5:                     /* L or S after possible relevant EN/AN */
                /* check if we had EN after R/AL */
                if (levState.startL2EN >= 0) {
                    addPoint(levState.startL2EN, LRM_BEFORE);
                }
                levState.startL2EN = -1;  /* not within previous if since could also be -2 */
                /* check if we had any relevant EN/AN after R/AL */
                if ((insertPoints.points.length == 0) ||
                        (insertPoints.size <= insertPoints.confirmed)) {
                    /* nothing, just clean up */
                    levState.lastStrongRTL = -1;
                    /* check if we have a pending conditional segment */
                    level = impTab[oldStateSeq][IMPTABLEVELS_RES];
                    if ((level & 1) != 0 && levState.startON > 0) { /* after ON */
                        start = levState.startON;   /* reset to basic run level */
                    }
                    if (_prop == _S) {              /* add LRM before S */
                        addPoint(start0, LRM_BEFORE);
                        insertPoints.confirmed = insertPoints.size;
                    }
                    break;
                }
                /* reset previous RTL cont to level for LTR text */
                for (k = levState.lastStrongRTL + 1; k < start0; k++) {
                    /* reset odd level, leave runLevel+2 as is */
                    levels[k] = (byte)((levels[k] - 2) & ~1);
                }
                /* mark insert points as confirmed */
                insertPoints.confirmed = insertPoints.size;
                levState.lastStrongRTL = -1;
                if (_prop == _S) {           /* add LRM before S */
                    addPoint(start0, LRM_BEFORE);
                    insertPoints.confirmed = insertPoints.size;
                }
                break;

            case 6:                     /* R/AL after possible relevant EN/AN */
                /* just clean up */
                if (insertPoints.points.length > 0)
                    /* remove all non confirmed insert points */
                    insertPoints.size = insertPoints.confirmed;
                levState.startON = -1;
                levState.startL2EN = -1;
                levState.lastStrongRTL = limit - 1;
                break;

            case 7:                     /* EN/AN after R/AL + possible cont */
                /* check for real AN */
                if ((_prop == _AN) && (dirProps[start0] == AN) &&
                (reorderingMode != REORDER_INVERSE_FOR_NUMBERS_SPECIAL))
                {
                    /* real AN */
                    if (levState.startL2EN == -1) { /* if no relevant EN already found */
                        /* just note the rightmost digit as a strong RTL */
                        levState.lastStrongRTL = limit - 1;
                        break;
                    }
                    if (levState.startL2EN >= 0)  { /* after EN, no AN */
                        addPoint(levState.startL2EN, LRM_BEFORE);
                        levState.startL2EN = -2;
                    }
                    /* note AN */
                    addPoint(start0, LRM_BEFORE);
                    break;
                }
                /* if first EN/AN after R/AL */
                if (levState.startL2EN == -1) {
                    levState.startL2EN = start0;
                }
                break;

            case 8:                     /* note location of latest R/AL */
                levState.lastStrongRTL = limit - 1;
                levState.startON = -1;
                break;

            case 9:                     /* L after R+ON/EN/AN */
                /* include possible adjacent number on the left */
                for (k = start0-1; k >= 0 && ((levels[k] & 1) == 0); k--) {
                }
                if (k >= 0) {
                    addPoint(k, RLM_BEFORE);    /* add RLM before */
                    insertPoints.confirmed = insertPoints.size; /* confirm it */
                }
                levState.startON = start0;
                break;

            case 10:                    /* AN after L */
                /* AN numbers between L text on both sides may be trouble. */
                /* tentatively bracket with LRMs; will be confirmed if followed by L */
                addPoint(start0, LRM_BEFORE);   /* add LRM before */
                addPoint(start0, LRM_AFTER);    /* add LRM after  */
                break;

            case 11:                    /* R after L+ON/EN/AN */
                /* false alert, infirm LRMs around previous AN */
                insertPoints.size=insertPoints.confirmed;
                if (_prop == _S) {          /* add RLM before S */
                    addPoint(start0, RLM_BEFORE);
                    insertPoints.confirmed = insertPoints.size;
                }
                break;

            case 12:                    /* L after L+ON/AN */
                level = (byte)(levState.runLevel + addLevel);
                for (k=levState.startON; k < start0; k++) {
                    if (levels[k] < level) {
                        levels[k] = level;
                    }
                }
                insertPoints.confirmed = insertPoints.size;   /* confirm inserts */
                levState.startON = start0;
                break;

            case 13:                    /* L after L+ON+EN/AN/ON */
                level = levState.runLevel;
                for (k = start0-1; k >= levState.startON; k--) {
                    if (levels[k] == level+3) {
                        while (levels[k] == level+3) {
                            levels[k--] -= 2;
                        }
                        while (levels[k] == level) {
                            k--;
                        }
                    }
                    if (levels[k] == level+2) {
                        levels[k] = level;
                        continue;
                    }
                    levels[k] = (byte)(level+1);
                }
                break;

            case 14:                    /* R after L+ON+EN/AN/ON */
                level = (byte)(levState.runLevel+1);
                for (k = start0-1; k >= levState.startON; k--) {
                    if (levels[k] > level) {
                        levels[k] -= 2;
                    }
                }
                break;

            default:                        /* we should never get here */
                throw new IllegalStateException("Internal ICU error in processPropertySeq");
            }
        }
        if ((addLevel) != 0 || (start < start0)) {
            level = (byte)(levState.runLevel + addLevel);
            if (start >= levState.runStart) {
                for (k = start; k < limit; k++) {
                    levels[k] = level;
                }
            } else {
                setLevelsOutsideIsolates(start, limit, level);
            }
        }
    }

    /**
     * Returns the directionality of the last strong character at the end of the prologue, if any.
     * Requires prologue!=null.
     */
    private byte lastL_R_AL() {
        for (int i = prologue.length(); i > 0; ) {
            int uchar = prologue.codePointBefore(i);
            i -= Character.charCount(uchar);
            byte dirProp = (byte)getCustomizedClass(uchar);
            if (dirProp == L) {
                return _L;
            }
            if (dirProp == R || dirProp == AL) {
                return _R;
            }
            if(dirProp == B) {
                return _ON;
            }
        }
        return _ON;
    }

    /**
     * Returns the directionality of the first strong character, or digit, in the epilogue, if any.
     * Requires epilogue!=null.
     */
    private byte firstL_R_AL_EN_AN() {
        for (int i = 0; i < epilogue.length(); ) {
            int uchar = epilogue.codePointAt(i);
            i += Character.charCount(uchar);
            byte dirProp = (byte)getCustomizedClass(uchar);
            if (dirProp == L) {
                return _L;
            }
            if (dirProp == R || dirProp == AL) {
                return _R;
            }
            if (dirProp == EN) {
                return _EN;
            }
            if (dirProp == AN) {
                return _AN;
            }
        }
        return _ON;
    }

    private void resolveImplicitLevels(int start, int limit, short sor, short eor)
    {
        byte dirProp;
        LevState levState = new LevState();
        int i, start1, start2;
        short oldStateImp, stateImp, actionImp;
        short gprop, resProp, cell;
        boolean inverseRTL;
        short nextStrongProp = R;
        int nextStrongPos = -1;

        /* check for RTL inverse Bidi mode */
        /* FOOD FOR THOUGHT: in case of RTL inverse Bidi, it would make sense to
         * loop on the text characters from end to start.
         * This would need a different properties state table (at least different
         * actions) and different levels state tables (maybe very similar to the
         * LTR corresponding ones.
         */
        inverseRTL=((start<lastArabicPos) && ((GetParaLevelAt(start) & 1)>0) &&
                    (reorderingMode == REORDER_INVERSE_LIKE_DIRECT  ||
                     reorderingMode == REORDER_INVERSE_FOR_NUMBERS_SPECIAL));
        /* initialize for property and levels state table */
        levState.startL2EN = -1;        /* used for INVERSE_LIKE_DIRECT_WITH_MARKS */
        levState.lastStrongRTL = -1;    /* used for INVERSE_LIKE_DIRECT_WITH_MARKS */
        levState.runStart = start;
        levState.runLevel = levels[start];
        levState.impTab = impTabPair.imptab[levState.runLevel & 1];
        levState.impAct = impTabPair.impact[levState.runLevel & 1];
        if (start == 0 && prologue != null) {
            byte lastStrong = lastL_R_AL();
            if (lastStrong != _ON) {
                sor = lastStrong;
            }
        }
        /* The isolates[] entries contain enough information to
           resume the bidi algorithm in the same state as it was
           when it was interrupted by an isolate sequence. */
        if (dirProps[start] == PDI) {
            levState.startON = isolates[isolateCount].startON;
            start1 = isolates[isolateCount].start1;
            stateImp = isolates[isolateCount].stateImp;
            levState.state = isolates[isolateCount].state;
            isolateCount--;
        } else {
            levState.startON = -1;
            start1 = start;
            if (dirProps[start] == NSM)
                stateImp = (short)(1 + sor);
            else
                stateImp = 0;
            levState.state = 0;
            processPropertySeq(levState, sor, start, start);
        }
        start2 = start;                 /* to make the Java compiler happy */

        for (i = start; i <= limit; i++) {
            if (i >= limit) {
                int k;
                for (k = limit - 1;
                     k > start &&
                         (DirPropFlag(dirProps[k]) & MASK_BN_EXPLICIT) != 0;
                     k--);
                dirProp = dirProps[k];
                if (dirProp == LRI || dirProp == RLI)
                    break;  /* no forced closing for sequence ending with LRI/RLI */
                gprop = eor;
            } else {
                byte prop, prop1;
                prop = dirProps[i];
                if (prop == B)
                    isolateCount = -1;  /* current isolates stack entry == none */
                if (inverseRTL) {
                    if (prop == AL) {
                        /* AL before EN does not make it AN */
                        prop = R;
                    } else if (prop == EN) {
                        if (nextStrongPos <= i) {
                            /* look for next strong char (L/R/AL) */
                            int j;
                            nextStrongProp = R;     /* set default */
                            nextStrongPos = limit;
                            for (j = i+1; j < limit; j++) {
                                prop1 = dirProps[j];
                                if (prop1 == L || prop1 == R || prop1 == AL) {
                                    nextStrongProp = prop1;
                                    nextStrongPos = j;
                                    break;
                                }
                            }
                        }
                        if (nextStrongProp == AL) {
                            prop = AN;
                        }
                    }
                }
                gprop = groupProp[prop];
            }
            oldStateImp = stateImp;
            cell = impTabProps[oldStateImp][gprop];
            stateImp = GetStateProps(cell);     /* isolate the new state */
            actionImp = GetActionProps(cell);   /* isolate the action */
            if ((i == limit) && (actionImp == 0)) {
                /* there is an unprocessed sequence if its property == eor   */
                actionImp = 1;                  /* process the last sequence */
            }
            if (actionImp != 0) {
                resProp = impTabProps[oldStateImp][IMPTABPROPS_RES];
                switch (actionImp) {
                case 1:             /* process current seq1, init new seq1 */
                    processPropertySeq(levState, resProp, start1, i);
                    start1 = i;
                    break;
                case 2:             /* init new seq2 */
                    start2 = i;
                    break;
                case 3:             /* process seq1, process seq2, init new seq1 */
                    processPropertySeq(levState, resProp, start1, start2);
                    processPropertySeq(levState, _ON, start2, i);
                    start1 = i;
                    break;
                case 4:             /* process seq1, set seq1=seq2, init new seq2 */
                    processPropertySeq(levState, resProp, start1, start2);
                    start1 = start2;
                    start2 = i;
                    break;
                default:            /* we should never get here */
                    throw new IllegalStateException("Internal ICU error in resolveImplicitLevels");
                }
            }
        }

        /* flush possible pending sequence, e.g. ON */
        if (limit == length && epilogue != null) {
            byte firstStrong = firstL_R_AL_EN_AN();
            if (firstStrong != _ON) {
                eor = firstStrong;
            }
        }

        /* look for the last char not a BN or LRE/RLE/LRO/RLO/PDF */
        for (i = limit - 1;
             i > start &&
                 (DirPropFlag(dirProps[i]) & MASK_BN_EXPLICIT) != 0;
             i--);
        dirProp = dirProps[i];
        if ((dirProp == LRI || dirProp == RLI) && limit < length) {
            isolateCount++;
            if (isolates[isolateCount] == null)
                isolates[isolateCount] = new Isolate();
            isolates[isolateCount].stateImp = stateImp;
            isolates[isolateCount].state = levState.state;
            isolates[isolateCount].start1 = start1;
            isolates[isolateCount].startON = levState.startON;
        }
        else
            processPropertySeq(levState, eor, limit, limit);
    }

    /* perform (L1) and (X9) ---------------------------------------------------- */

    /*
     * Reset the embedding levels for some non-graphic characters (L1).
     * This method also sets appropriate levels for BN, and
     * explicit embedding types that are supposed to have been removed
     * from the paragraph in (X9).
     */
    private void adjustWSLevels() {
        int i;

        if ((flags & MASK_WS) != 0) {
            int flag;
            i = trailingWSStart;
            while (i > 0) {
                /* reset a sequence of WS/BN before eop and B/S to the paragraph paraLevel */
                while (i > 0 && ((flag = DirPropFlag(dirProps[--i])) & MASK_WS) != 0) {
                    if (orderParagraphsLTR && (flag & DirPropFlag(B)) != 0) {
                        levels[i] = 0;
                    } else {
                        levels[i] = GetParaLevelAt(i);
                    }
                }

                /* reset BN to the next character's paraLevel until B/S, which restarts above loop */
                /* here, i+1 is guaranteed to be <length */
                while (i > 0) {
                    flag = DirPropFlag(dirProps[--i]);
                    if ((flag & MASK_BN_EXPLICIT) != 0) {
                        levels[i] = levels[i + 1];
                    } else if (orderParagraphsLTR && (flag & DirPropFlag(B)) != 0) {
                        levels[i] = 0;
                        break;
                    } else if ((flag & MASK_B_S) != 0){
                        levels[i] = GetParaLevelAt(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Set the context before a call to setPara().<p>
     *
     * setPara() computes the left-right directionality for a given piece
     * of text which is supplied as one of its arguments. Sometimes this piece
     * of text (the "main text") should be considered in context, because text
     * appearing before ("prologue") and/or after ("epilogue") the main text
     * may affect the result of this computation.<p>
     *
     * This function specifies the prologue and/or the epilogue for the next
     * call to setPara(). If successive calls to setPara()
     * all need specification of a context, setContext() must be called
     * before each call to setPara(). In other words, a context is not
     * "remembered" after the following successful call to setPara().<p>
     *
     * If a call to setPara() specifies DEFAULT_LTR or
     * DEFAULT_RTL as paraLevel and is preceded by a call to
     * setContext() which specifies a prologue, the paragraph level will
     * be computed taking in consideration the text in the prologue.<p>
     *
     * When setPara() is called without a previous call to
     * setContext, the main text is handled as if preceded and followed
     * by strong directional characters at the current paragraph level.
     * Calling setContext() with specification of a prologue will change
     * this behavior by handling the main text as if preceded by the last
     * strong character appearing in the prologue, if any.
     * Calling setContext() with specification of an epilogue will change
     * the behavior of setPara() by handling the main text as if followed
     * by the first strong character or digit appearing in the epilogue, if any.<p>
     *
     * Note 1: if <code>setContext</code> is called repeatedly without
     *         calling <code>setPara</code>, the earlier calls have no effect,
     *         only the last call will be remembered for the next call to
     *         <code>setPara</code>.<p>
     *
     * Note 2: calling <code>setContext(null, null)</code>
     *         cancels any previous setting of non-empty prologue or epilogue.
     *         The next call to <code>setPara()</code> will process no
     *         prologue or epilogue.<p>
     *
     * Note 3: users must be aware that even after setting the context
     *         before a call to setPara() to perform e.g. a logical to visual
     *         transformation, the resulting string may not be identical to what it
     *         would have been if all the text, including prologue and epilogue, had
     *         been processed together.<br>
     * Example (upper case letters represent RTL characters):<br>
     * &nbsp;&nbsp;prologue = "<code>abc DE</code>"<br>
     * &nbsp;&nbsp;epilogue = none<br>
     * &nbsp;&nbsp;main text = "<code>FGH xyz</code>"<br>
     * &nbsp;&nbsp;paraLevel = LTR<br>
     * &nbsp;&nbsp;display without prologue = "<code>HGF xyz</code>"
     *             ("HGF" is adjacent to "xyz")<br>
     * &nbsp;&nbsp;display with prologue = "<code>abc HGFED xyz</code>"
     *             ("HGF" is not adjacent to "xyz")<br>
     *
     * @param prologue is the text which precedes the text that
     *        will be specified in a coming call to setPara().
     *        If there is no prologue to consider,
     *        this parameter can be <code>null</code>.
     *
     * @param epilogue is the text which follows the text that
     *        will be specified in a coming call to setPara().
     *        If there is no epilogue to consider,
     *        this parameter can be <code>null</code>.
     *
     * @see #setPara
     */
    public void setContext(String prologue, String epilogue) {
        this.prologue = prologue != null && prologue.length() > 0 ? prologue : null;
        this.epilogue = epilogue != null && epilogue.length() > 0 ? epilogue : null;
    }

    private void setParaSuccess() {
        prologue = null;                /* forget the last context */
        epilogue = null;
        paraBidi = this;                /* mark successful setPara */
    }

    int Bidi_Min(int x, int y) {
        return x < y ? x : y;
    }

    int Bidi_Abs(int x) {
        return x >= 0 ? x : -x;
    }

    void setParaRunsOnly(char[] parmText, byte parmParaLevel) {
        int[] visualMap;
        String visualText;
        int saveLength, saveTrailingWSStart;
        byte[] saveLevels;
        byte saveDirection;
        int i, j, visualStart, logicalStart,
            oldRunCount, runLength, addedRuns, insertRemove,
            start, limit, step, indexOddBit, logicalPos,
            index, index1;
        int saveOptions;

        reorderingMode = REORDER_DEFAULT;
        int parmLength = parmText.length;
        if (parmLength == 0) {
            setPara(parmText, parmParaLevel, null);
            reorderingMode = REORDER_RUNS_ONLY;
            return;
        }
        /* obtain memory for mapping table and visual text */
        saveOptions = reorderingOptions;
        if ((saveOptions & OPTION_INSERT_MARKS) > 0) {
            reorderingOptions &= ~OPTION_INSERT_MARKS;
            reorderingOptions |= OPTION_REMOVE_CONTROLS;
        }
        parmParaLevel &= 1;             /* accept only 0 or 1 */
        setPara(parmText, parmParaLevel, null);
        /* we cannot access directly levels since it is not yet set if
         * direction is not MIXED
         */
        saveLevels = new byte[this.length];
        System.arraycopy(getLevels(), 0, saveLevels, 0, this.length);
        saveTrailingWSStart = trailingWSStart;

        visualText = writeReordered(DO_MIRRORING);
        visualMap = getVisualMap();
        this.reorderingOptions = saveOptions;
        saveLength = this.length;
        saveDirection=this.direction;

        this.reorderingMode = REORDER_INVERSE_LIKE_DIRECT;
        parmParaLevel ^= 1;
        setPara(visualText, parmParaLevel, null);
        BidiLine.getRuns(this);
        /* check if some runs must be split, count how many splits */
        addedRuns = 0;
        oldRunCount = this.runCount;
        visualStart = 0;
        for (i = 0; i < oldRunCount; i++, visualStart += runLength) {
            runLength = runs[i].limit - visualStart;
            if (runLength < 2) {
                continue;
            }
            logicalStart = runs[i].start;
            for (j = logicalStart+1; j < logicalStart+runLength; j++) {
                index = visualMap[j];
                index1 = visualMap[j-1];
                if ((Bidi_Abs(index-index1)!=1) || (saveLevels[index]!=saveLevels[index1])) {
                    addedRuns++;
                }
            }
        }
        if (addedRuns > 0) {
            getRunsMemory(oldRunCount + addedRuns);
            if (runCount == 1) {
                /* because we switch from UBiDi.simpleRuns to UBiDi.runs */
                runsMemory[0] = runs[0];
            } else {
                System.arraycopy(runs, 0, runsMemory, 0, runCount);
            }
            runs = runsMemory;
            runCount += addedRuns;
            for (i = oldRunCount; i < runCount; i++) {
                if (runs[i] == null) {
                    runs[i] = new BidiRun(0, 0, (byte)0);
                }
            }
        }
        /* split runs which are not consecutive in source text */
        int newI;
        for (i = oldRunCount-1; i >= 0; i--) {
            newI = i + addedRuns;
            runLength = i==0 ? runs[0].limit :
                               runs[i].limit - runs[i-1].limit;
            logicalStart = runs[i].start;
            indexOddBit = runs[i].level & 1;
            if (runLength < 2) {
                if (addedRuns > 0) {
                    runs[newI].copyFrom(runs[i]);
                }
                logicalPos = visualMap[logicalStart];
                runs[newI].start = logicalPos;
                runs[newI].level = (byte)(saveLevels[logicalPos] ^ indexOddBit);
                continue;
            }
            if (indexOddBit > 0) {
                start = logicalStart;
                limit = logicalStart + runLength - 1;
                step = 1;
            } else {
                start = logicalStart + runLength - 1;
                limit = logicalStart;
                step = -1;
            }
            for (j = start; j != limit; j += step) {
                index = visualMap[j];
                index1 = visualMap[j+step];
                if ((Bidi_Abs(index-index1)!=1) || (saveLevels[index]!=saveLevels[index1])) {
                    logicalPos = Bidi_Min(visualMap[start], index);
                    runs[newI].start = logicalPos;
                    runs[newI].level = (byte)(saveLevels[logicalPos] ^ indexOddBit);
                    runs[newI].limit = runs[i].limit;
                    runs[i].limit -= Bidi_Abs(j - start) + 1;
                    insertRemove = runs[i].insertRemove & (LRM_AFTER|RLM_AFTER);
                    runs[newI].insertRemove = insertRemove;
                    runs[i].insertRemove &= ~insertRemove;
                    start = j + step;
                    addedRuns--;
                    newI--;
                }
            }
            if (addedRuns > 0) {
                runs[newI].copyFrom(runs[i]);
            }
            logicalPos = Bidi_Min(visualMap[start], visualMap[limit]);
            runs[newI].start = logicalPos;
            runs[newI].level = (byte)(saveLevels[logicalPos] ^ indexOddBit);
        }

//    cleanup1:
        /* restore initial paraLevel */
        this.paraLevel ^= 1;
//    cleanup2:
        /* restore real text */
        this.text = parmText;
        this.length = saveLength;
        this.originalLength = parmLength;
        this.direction=saveDirection;
        this.levels = saveLevels;
        this.trailingWSStart = saveTrailingWSStart;
        if (runCount > 1) {
            this.direction = MIXED;
        }
//    cleanup3:
        this.reorderingMode = REORDER_RUNS_ONLY;
    }

    /**
     * Perform the Unicode Bidi algorithm. It is defined in the
     * <a href="http://www.unicode.org/unicode/reports/tr9/">Unicode Standard Annex #9</a>,
     * version 13,
     * also described in The Unicode Standard, Version 4.0 .<p>
     *
     * This method takes a piece of plain text containing one or more paragraphs,
     * with or without externally specified embedding levels from <i>styled</i>
     * text and computes the left-right-directionality of each character.<p>
     *
     * If the entire text is all of the same directionality, then
     * the method may not perform all the steps described by the algorithm,
     * i.e., some levels may not be the same as if all steps were performed.
     * This is not relevant for unidirectional text.<br>
     * For example, in pure LTR text with numbers the numbers would get
     * a resolved level of 2 higher than the surrounding text according to
     * the algorithm. This implementation may set all resolved levels to
     * the same value in such a case.<p>
     *
     * The text can be composed of multiple paragraphs. Occurrence of a block
     * separator in the text terminates a paragraph, and whatever comes next starts
     * a new paragraph. The exception to this rule is when a Carriage Return (CR)
     * is followed by a Line Feed (LF). Both CR and LF are block separators, but
     * in that case, the pair of characters is considered as terminating the
     * preceding paragraph, and a new paragraph will be started by a character
     * coming after the LF.
     *
     * Although the text is passed here as a <code>String</code>, it is
     * stored internally as an array of characters. Therefore the
     * documentation will refer to indexes of the characters in the text.
     *
     * @param text contains the text that the Bidi algorithm will be performed
     *        on. This text can be retrieved with <code>getText()</code> or
     *        <code>getTextAsString</code>.<br>
     *
     * @param paraLevel specifies the default level for the text;
     *        it is typically 0 (LTR) or 1 (RTL).
     *        If the method shall determine the paragraph level from the text,
     *        then <code>paraLevel</code> can be set to
     *        either <code>LEVEL_DEFAULT_LTR</code>
     *        or <code>LEVEL_DEFAULT_RTL</code>; if the text contains multiple
     *        paragraphs, the paragraph level shall be determined separately for
     *        each paragraph; if a paragraph does not include any strongly typed
     *        character, then the desired default is used (0 for LTR or 1 for RTL).
     *        Any other value between 0 and <code>MAX_EXPLICIT_LEVEL</code>
     *        is also valid, with odd levels indicating RTL.
     *
     * @param embeddingLevels (in) may be used to preset the embedding and override levels,
     *        ignoring characters like LRE and PDF in the text.
     *        A level overrides the directional property of its corresponding
     *        (same index) character if the level has the
     *        <code>LEVEL_OVERRIDE</code> bit set.<br><br>
     *        Except for that bit, it must be
     *        <code>paraLevel&lt;=embeddingLevels[]&lt;=MAX_EXPLICIT_LEVEL</code>,
     *        with one exception: a level of zero may be specified for a
     *        paragraph separator even if <code>paraLevel&gt;0</code> when multiple
     *        paragraphs are submitted in the same call to <code>setPara()</code>.<br><br>
     *        <strong>Caution: </strong>A reference to this array, not a copy
     *        of the levels, will be stored in the <code>Bidi</code> object;
     *        the <code>embeddingLevels</code>
     *        should not be modified to avoid unexpected results on subsequent
     *        Bidi operations. However, the <code>setPara()</code> and
     *        <code>setLine()</code> methods may modify some or all of the
     *        levels.<br><br>
     *        <strong>Note:</strong> the <code>embeddingLevels</code> array must
     *        have one entry for each character in <code>text</code>.
     *
     * @throws IllegalArgumentException if the values in embeddingLevels are
     *         not within the allowed range
     *
     * @see #LEVEL_DEFAULT_LTR
     * @see #LEVEL_DEFAULT_RTL
     * @see #LEVEL_OVERRIDE
     * @see #MAX_EXPLICIT_LEVEL
     */
    public void setPara(String text, byte paraLevel, byte[] embeddingLevels)
    {
        if (text == null) {
            setPara(new char[0], paraLevel, embeddingLevels);
        } else {
            setPara(text.toCharArray(), paraLevel, embeddingLevels);
        }
    }

    /**
     * Perform the Unicode Bidi algorithm. It is defined in the
     * <a href="http://www.unicode.org/unicode/reports/tr9/">Unicode Standard Annex #9</a>,
     * version 13,
     * also described in The Unicode Standard, Version 4.0 .<p>
     *
     * This method takes a piece of plain text containing one or more paragraphs,
     * with or without externally specified embedding levels from <i>styled</i>
     * text and computes the left-right-directionality of each character.<p>
     *
     * If the entire text is all of the same directionality, then
     * the method may not perform all the steps described by the algorithm,
     * i.e., some levels may not be the same as if all steps were performed.
     * This is not relevant for unidirectional text.<br>
     * For example, in pure LTR text with numbers the numbers would get
     * a resolved level of 2 higher than the surrounding text according to
     * the algorithm. This implementation may set all resolved levels to
     * the same value in such a case.<p>
     *
     * The text can be composed of multiple paragraphs. Occurrence of a block
     * separator in the text terminates a paragraph, and whatever comes next starts
     * a new paragraph. The exception to this rule is when a Carriage Return (CR)
     * is followed by a Line Feed (LF). Both CR and LF are block separators, but
     * in that case, the pair of characters is considered as terminating the
     * preceding paragraph, and a new paragraph will be started by a character
     * coming after the LF.
     *
     * The text is stored internally as an array of characters. Therefore the
     * documentation will refer to indexes of the characters in the text.
     *
     * @param chars contains the text that the Bidi algorithm will be performed
     *        on. This text can be retrieved with <code>getText()</code> or
     *        <code>getTextAsString</code>.<br>
     *
     * @param paraLevel specifies the default level for the text;
     *        it is typically 0 (LTR) or 1 (RTL).
     *        If the method shall determine the paragraph level from the text,
     *        then <code>paraLevel</code> can be set to
     *        either <code>LEVEL_DEFAULT_LTR</code>
     *        or <code>LEVEL_DEFAULT_RTL</code>; if the text contains multiple
     *        paragraphs, the paragraph level shall be determined separately for
     *        each paragraph; if a paragraph does not include any strongly typed
     *        character, then the desired default is used (0 for LTR or 1 for RTL).
     *        Any other value between 0 and <code>MAX_EXPLICIT_LEVEL</code>
     *        is also valid, with odd levels indicating RTL.
     *
     * @param embeddingLevels (in) may be used to preset the embedding and
     *        override levels, ignoring characters like LRE and PDF in the text.
     *        A level overrides the directional property of its corresponding
     *        (same index) character if the level has the
     *        <code>LEVEL_OVERRIDE</code> bit set.<br><br>
     *        Except for that bit, it must be
     *        <code>paraLevel&lt;=embeddingLevels[]&lt;=MAX_EXPLICIT_LEVEL</code>,
     *        with one exception: a level of zero may be specified for a
     *        paragraph separator even if <code>paraLevel&gt;0</code> when multiple
     *        paragraphs are submitted in the same call to <code>setPara()</code>.<br><br>
     *        <strong>Caution: </strong>A reference to this array, not a copy
     *        of the levels, will be stored in the <code>Bidi</code> object;
     *        the <code>embeddingLevels</code>
     *        should not be modified to avoid unexpected results on subsequent
     *        Bidi operations. However, the <code>setPara()</code> and
     *        <code>setLine()</code> methods may modify some or all of the
     *        levels.<br><br>
     *        <strong>Note:</strong> the <code>embeddingLevels</code> array must
     *        have one entry for each character in <code>text</code>.
     *
     * @throws IllegalArgumentException if the values in embeddingLevels are
     *         not within the allowed range
     *
     * @see #LEVEL_DEFAULT_LTR
     * @see #LEVEL_DEFAULT_RTL
     * @see #LEVEL_OVERRIDE
     * @see #MAX_EXPLICIT_LEVEL
     */
    public void setPara(char[] chars, byte paraLevel, byte[] embeddingLevels)
    {
        /* check the argument values */
        if (paraLevel < LEVEL_DEFAULT_LTR) {
            verifyRange(paraLevel, 0, MAX_EXPLICIT_LEVEL + 1);
        }
        if (chars == null) {
            chars = new char[0];
        }

        /* special treatment for RUNS_ONLY mode */
        if (reorderingMode == REORDER_RUNS_ONLY) {
            setParaRunsOnly(chars, paraLevel);
            return;
        }

        /* initialize the Bidi object */
        this.paraBidi = null;          /* mark unfinished setPara */
        this.text = chars;
        this.length = this.originalLength = this.resultLength = text.length;
        this.paraLevel = paraLevel;
        this.direction = (byte)(paraLevel & 1);
        this.paraCount = 1;

        /* Allocate zero-length arrays instead of setting to null here; then
         * checks for null in various places can be eliminated.
         */
        dirProps = new byte[0];
        levels = new byte[0];
        runs = new BidiRun[0];
        isGoodLogicalToVisualRunsMap = false;
        insertPoints.size = 0;          /* clean up from last call */
        insertPoints.confirmed = 0;     /* clean up from last call */

        /*
         * Save the original paraLevel if contextual; otherwise, set to 0.
         */
        defaultParaLevel = IsDefaultLevel(paraLevel) ? paraLevel : 0;

        if (length == 0) {
            /*
             * For an empty paragraph, create a Bidi object with the paraLevel and
             * the flags and the direction set but without allocating zero-length arrays.
             * There is nothing more to do.
             */
            if (IsDefaultLevel(paraLevel)) {
                this.paraLevel &= 1;
                defaultParaLevel = 0;
            }
            flags = DirPropFlagLR(paraLevel);
            runCount = 0;
            paraCount = 0;
            setParaSuccess();
            return;
        }

        runCount = -1;

        /*
         * Get the directional properties,
         * the flags bit-set, and
         * determine the paragraph level if necessary.
         */
        getDirPropsMemory(length);
        dirProps = dirPropsMemory;
        getDirProps();
        /* the processed length may have changed if OPTION_STREAMING is set */
        trailingWSStart = length;  /* the levels[] will reflect the WS run */

        /* are explicit levels specified? */
        if (embeddingLevels == null) {
            /* no: determine explicit levels according to the (Xn) rules */
            getLevelsMemory(length);
            levels = levelsMemory;
            direction = resolveExplicitLevels();
        } else {
            /* set BN for all explicit codes, check that all levels are 0 or paraLevel..MAX_EXPLICIT_LEVEL */
            levels = embeddingLevels;
            direction = checkExplicitLevels();
        }

        /* allocate isolate memory */
        if (isolateCount > 0) {
            if (isolates == null || isolates.length < isolateCount)
                isolates = new Isolate[isolateCount + 3];   /* keep some reserve */
        }
        isolateCount = -1;              /* current isolates stack entry == none */

        /*
         * The steps after (X9) in the Bidi algorithm are performed only if
         * the paragraph text has mixed directionality!
         */
        switch (direction) {
        case LTR:
            /* all levels are implicitly at paraLevel (important for getLevels()) */
            trailingWSStart = 0;
            break;
        case RTL:
            /* all levels are implicitly at paraLevel (important for getLevels()) */
            trailingWSStart = 0;
            break;
        default:
            /*
             *  Choose the right implicit state table
             */
            switch(reorderingMode) {
            case REORDER_DEFAULT:
                this.impTabPair = impTab_DEFAULT;
                break;
            case REORDER_NUMBERS_SPECIAL:
                this.impTabPair = impTab_NUMBERS_SPECIAL;
                break;
            case REORDER_GROUP_NUMBERS_WITH_R:
                this.impTabPair = impTab_GROUP_NUMBERS_WITH_R;
                break;
            case REORDER_RUNS_ONLY:
                /* we should never get here */
                throw new InternalError("Internal ICU error in setPara");
                /* break; */
            case REORDER_INVERSE_NUMBERS_AS_L:
                this.impTabPair = impTab_INVERSE_NUMBERS_AS_L;
                break;
            case REORDER_INVERSE_LIKE_DIRECT:
                if ((reorderingOptions & OPTION_INSERT_MARKS) != 0) {
                    this.impTabPair = impTab_INVERSE_LIKE_DIRECT_WITH_MARKS;
                } else {
                    this.impTabPair = impTab_INVERSE_LIKE_DIRECT;
                }
                break;
            case REORDER_INVERSE_FOR_NUMBERS_SPECIAL:
                if ((reorderingOptions & OPTION_INSERT_MARKS) != 0) {
                    this.impTabPair = impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS;
                } else {
                    this.impTabPair = impTab_INVERSE_FOR_NUMBERS_SPECIAL;
                }
                break;
            }
            /*
             * If there are no external levels specified and there
             * are no significant explicit level codes in the text,
             * then we can treat the entire paragraph as one run.
             * Otherwise, we need to perform the following rules on runs of
             * the text with the same embedding levels. (X10)
             * "Significant" explicit level codes are ones that actually
             * affect non-BN characters.
             * Examples for "insignificant" ones are empty embeddings
             * LRE-PDF, LRE-RLE-PDF-PDF, etc.
             */
            if (embeddingLevels == null && paraCount <= 1 &&
                (flags & DirPropFlagMultiRuns) == 0) {
                resolveImplicitLevels(0, length,
                        GetLRFromLevel(GetParaLevelAt(0)),
                        GetLRFromLevel(GetParaLevelAt(length - 1)));
            } else {
                /* sor, eor: start and end types of same-level-run */
                int start, limit = 0;
                byte level, nextLevel;
                short sor, eor;

                /* determine the first sor and set eor to it because of the loop body (sor=eor there) */
                level = GetParaLevelAt(0);
                nextLevel = levels[0];
                if (level < nextLevel) {
                    eor = GetLRFromLevel(nextLevel);
                } else {
                    eor = GetLRFromLevel(level);
                }

                do {
                    /* determine start and limit of the run (end points just behind the run) */

                    /* the values for this run's start are the same as for the previous run's end */
                    start = limit;
                    level = nextLevel;
                    if ((start > 0) && (dirProps[start - 1] == B)) {
                        /* except if this is a new paragraph, then set sor = para level */
                        sor = GetLRFromLevel(GetParaLevelAt(start));
                    } else {
                        sor = eor;
                    }

                    /* search for the limit of this run */
                    while ((++limit < length) &&
                           ((levels[limit] == level) ||
                            ((DirPropFlag(dirProps[limit]) & MASK_BN_EXPLICIT) != 0))) {}

                    /* get the correct level of the next run */
                    if (limit < length) {
                        nextLevel = levels[limit];
                    } else {
                        nextLevel = GetParaLevelAt(length - 1);
                    }

                    /* determine eor from max(level, nextLevel); sor is last run's eor */
                    if (NoOverride(level) < NoOverride(nextLevel)) {
                        eor = GetLRFromLevel(nextLevel);
                    } else {
                        eor = GetLRFromLevel(level);
                    }

                    /* if the run consists of overridden directional types, then there
                       are no implicit types to be resolved */
                    if ((level & LEVEL_OVERRIDE) == 0) {
                        resolveImplicitLevels(start, limit, sor, eor);
                    } else {
                        /* remove the LEVEL_OVERRIDE flags */
                        do {
                            levels[start++] &= ~LEVEL_OVERRIDE;
                        } while (start < limit);
                    }
                } while (limit  < length);
            }

            /* reset the embedding levels for some non-graphic characters (L1), (X9) */
            adjustWSLevels();

            break;
        }
        /* add RLM for inverse Bidi with contextual orientation resolving
         * to RTL which would not round-trip otherwise
         */
        if ((defaultParaLevel > 0) &&
            ((reorderingOptions & OPTION_INSERT_MARKS) != 0) &&
            ((reorderingMode == REORDER_INVERSE_LIKE_DIRECT) ||
             (reorderingMode == REORDER_INVERSE_FOR_NUMBERS_SPECIAL))) {
            int start, last;
            byte level;
            byte dirProp;
            for (int i = 0; i < paraCount; i++) {
                last = paras_limit[i] - 1;
                level = paras_level[i];
                if (level == 0)
                    continue;           /* LTR paragraph */
                start = i == 0 ? 0 : paras_limit[i - 1];
                for (int j = last; j >= start; j--) {
                    dirProp = dirProps[j];
                    if (dirProp == L) {
                        if (j < last) {
                            while (dirProps[last] == B) {
                                last--;
                            }
                        }
                        addPoint(last, RLM_BEFORE);
                        break;
                    }
                    if ((DirPropFlag(dirProp) & MASK_R_AL) != 0) {
                        break;
                    }
                }
            }
        }

        if ((reorderingOptions & OPTION_REMOVE_CONTROLS) != 0) {
            resultLength -= controlCount;
        } else {
            resultLength += insertPoints.size;
        }
        setParaSuccess();
    }

    /**
     * Perform the Unicode Bidi algorithm on a given paragraph, as defined in the
     * <a href="http://www.unicode.org/unicode/reports/tr9/">Unicode Standard Annex #9</a>,
     * version 13,
     * also described in The Unicode Standard, Version 4.0 .<p>
     *
     * This method takes a paragraph of text and computes the
     * left-right-directionality of each character. The text should not
     * contain any Unicode block separators.<p>
     *
     * The RUN_DIRECTION attribute in the text, if present, determines the base
     * direction (left-to-right or right-to-left). If not present, the base
     * direction is computed using the Unicode Bidirectional Algorithm,
     * defaulting to left-to-right if there are no strong directional characters
     * in the text. This attribute, if present, must be applied to all the text
     * in the paragraph.<p>
     *
     * The BIDI_EMBEDDING attribute in the text, if present, represents
     * embedding level information. Negative values from -1 to -62 indicate
     * overrides at the absolute value of the level. Positive values from 1 to
     * 62 indicate embeddings. Where values are zero or not defined, the base
     * embedding level as determined by the base direction is assumed.<p>
     *
     * The NUMERIC_SHAPING attribute in the text, if present, converts European
     * digits to other decimal digits before running the bidi algorithm. This
     * attribute, if present, must be applied to all the text in the paragraph.
     *
     * If the entire text is all of the same directionality, then
     * the method may not perform all the steps described by the algorithm,
     * i.e., some levels may not be the same as if all steps were performed.
     * This is not relevant for unidirectional text.<br>
     * For example, in pure LTR text with numbers the numbers would get
     * a resolved level of 2 higher than the surrounding text according to
     * the algorithm. This implementation may set all resolved levels to
     * the same value in such a case.<p>
     *
     * @param paragraph a paragraph of text with optional character and
     *        paragraph attribute information
     */
    public void setPara(AttributedCharacterIterator paragraph)
    {
        byte paraLvl;
        Boolean runDirection = (Boolean) paragraph.getAttribute(TextAttribute.RUN_DIRECTION);
        if (runDirection == null) {
            paraLvl = LEVEL_DEFAULT_LTR;
        } else {
            paraLvl = (runDirection.equals(TextAttribute.RUN_DIRECTION_LTR)) ?
                        LTR : RTL;
        }

        byte[] lvls = null;
        int len = paragraph.getEndIndex() - paragraph.getBeginIndex();
        byte[] embeddingLevels = new byte[len];
        char[] txt = new char[len];
        int i = 0;
        char ch = paragraph.first();
        while (ch != AttributedCharacterIterator.DONE) {
            txt[i] = ch;
            Integer embedding = (Integer) paragraph.getAttribute(TextAttribute.BIDI_EMBEDDING);
            if (embedding != null) {
                byte level = embedding.byteValue();
                if (level == 0) {
                    /* no-op */
                } else if (level < 0) {
                    lvls = embeddingLevels;
                    embeddingLevels[i] = (byte)((0 - level) | LEVEL_OVERRIDE);
                } else {
                    lvls = embeddingLevels;
                    embeddingLevels[i] = level;
                }
            }
            ch = paragraph.next();
            ++i;
        }

        NumericShaper shaper = (NumericShaper) paragraph.getAttribute(TextAttribute.NUMERIC_SHAPING);
        if (shaper != null) {
            shaper.shape(txt, 0, len);
        }
        setPara(txt, paraLvl, lvls);
    }

    /**
     * Specify whether block separators must be allocated level zero,
     * so that successive paragraphs will progress from left to right.
     * This method must be called before <code>setPara()</code>.
     * Paragraph separators (B) may appear in the text.  Setting them to level zero
     * means that all paragraph separators (including one possibly appearing
     * in the last text position) are kept in the reordered text after the text
     * that they follow in the source text.
     * When this feature is not enabled, a paragraph separator at the last
     * position of the text before reordering will go to the first position
     * of the reordered text when the paragraph level is odd.
     *
     * @param ordarParaLTR specifies whether paragraph separators (B) must
     * receive level 0, so that successive paragraphs progress from left to right.
     *
     * @see #setPara
     */
    public void orderParagraphsLTR(boolean ordarParaLTR) {
        orderParagraphsLTR = ordarParaLTR;
    }

    /**
     * Is this <code>Bidi</code> object set to allocate level 0 to block
     * separators so that successive paragraphs progress from left to right?
     *
     * @return <code>true</code> if the <code>Bidi</code> object is set to
     *         allocate level 0 to block separators.
     */
    public boolean isOrderParagraphsLTR() {
        return orderParagraphsLTR;
    }

    /**
     * Get the directionality of the text.
     *
     * @return a value of <code>LTR</code>, <code>RTL</code> or <code>MIXED</code>
     *         that indicates if the entire text
     *         represented by this object is unidirectional,
     *         and which direction, or if it is mixed-directional.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     *
     * @see #LTR
     * @see #RTL
     * @see #MIXED
     */
    public byte getDirection()
    {
        verifyValidParaOrLine();
        return direction;
    }

    /**
     * Get the text.
     *
     * @return A <code>String</code> containing the text that the
     *         <code>Bidi</code> object was created for.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     *
     * @see #setPara
     * @see #setLine
     */
    public String getTextAsString()
    {
        verifyValidParaOrLine();
        return new String(text);
    }

    /**
     * Get the text.
     *
     * @return A <code>char</code> array containing the text that the
     *         <code>Bidi</code> object was created for.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     *
     * @see #setPara
     * @see #setLine
     */
    public char[] getText()
    {
        verifyValidParaOrLine();
        return text;
    }

    /**
     * Get the length of the text.
     *
     * @return The length of the text that the <code>Bidi</code> object was
     *         created for.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     */
    public int getLength()
    {
        verifyValidParaOrLine();
        return originalLength;
    }

    /**
     * Get the length of the source text processed by the last call to
     * <code>setPara()</code>. This length may be different from the length of
     * the source text if option <code>OPTION_STREAMING</code> has been
     * set.
     * <br>
     * Note that whenever the length of the text affects the execution or the
     * result of a method, it is the processed length which must be considered,
     * except for <code>setPara</code> (which receives unprocessed source text)
     * and <code>getLength</code> (which returns the original length of the
     * source text).<br>
     * In particular, the processed length is the one to consider in the
     * following cases:
     * <ul>
     * <li>maximum value of the <code>limit</code> argument of
     * <code>setLine</code></li>
     * <li>maximum value of the <code>charIndex</code> argument of
     * <code>getParagraph</code></li>
     * <li>maximum value of the <code>charIndex</code> argument of
     * <code>getLevelAt</code></li>
     * <li>number of elements in the array returned by <code>getLevels</code>
     * </li>
     * <li>maximum value of the <code>logicalStart</code> argument of
     * <code>getLogicalRun</code></li>
     * <li>maximum value of the <code>logicalIndex</code> argument of
     * <code>getVisualIndex</code></li>
     * <li>number of elements returned by <code>getLogicalMap</code></li>
     * <li>length of text processed by <code>writeReordered</code></li>
     * </ul>
     *
     * @return The length of the part of the source text processed by
     *         the last call to <code>setPara</code>.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     *
     * @see #setPara
     * @see #OPTION_STREAMING
     */
    public int getProcessedLength() {
        verifyValidParaOrLine();
        return length;
    }

    /**
     * Get the length of the reordered text resulting from the last call to
     * <code>setPara()</code>. This length may be different from the length
     * of the source text if option <code>OPTION_INSERT_MARKS</code>
     * or option <code>OPTION_REMOVE_CONTROLS</code> has been set.
     * <br>
     * This resulting length is the one to consider in the following cases:
     * <ul>
     * <li>maximum value of the <code>visualIndex</code> argument of
     * <code>getLogicalIndex</code></li>
     * <li>number of elements returned by <code>getVisualMap</code></li>
     * </ul>
     * Note that this length stays identical to the source text length if
     * Bidi marks are inserted or removed using option bits of
     * <code>writeReordered</code>, or if option
     * <code>REORDER_INVERSE_NUMBERS_AS_L</code> has been set.
     *
     * @return The length of the reordered text resulting from
     *         the last call to <code>setPara</code>.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     *
     * @see #setPara
     * @see #OPTION_INSERT_MARKS
     * @see #OPTION_REMOVE_CONTROLS
     * @see #REORDER_INVERSE_NUMBERS_AS_L
     */
    public int getResultLength() {
        verifyValidParaOrLine();
        return resultLength;
    }

    /* paragraphs API methods ------------------------------------------------- */

    /**
     * Get the paragraph level of the text.
     *
     * @return The paragraph level. If there are multiple paragraphs, their
     *         level may vary if the required paraLevel is LEVEL_DEFAULT_LTR or
     *         LEVEL_DEFAULT_RTL.  In that case, the level of the first paragraph
     *         is returned.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     *
     * @see #LEVEL_DEFAULT_LTR
     * @see #LEVEL_DEFAULT_RTL
     * @see #getParagraph
     * @see #getParagraphByIndex
     */
    public byte getParaLevel()
    {
        verifyValidParaOrLine();
        return paraLevel;
    }

    /**
     * Get the number of paragraphs.
     *
     * @return The number of paragraphs.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     */
    public int countParagraphs()
    {
        verifyValidParaOrLine();
        return paraCount;
    }

    /**
     * Get a paragraph, given the index of this paragraph.
     *
     * This method returns information about a paragraph.<p>
     *
     * @param paraIndex is the number of the paragraph, in the
     *        range <code>[0..countParagraphs()-1]</code>.
     *
     * @return a BidiRun object with the details of the paragraph:<br>
     *        <code>start</code> will receive the index of the first character
     *        of the paragraph in the text.<br>
     *        <code>limit</code> will receive the limit of the paragraph.<br>
     *        <code>embeddingLevel</code> will receive the level of the paragraph.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if paraIndex is not in the range
     *        <code>[0..countParagraphs()-1]</code>
     *
     * @see android.icu.text.BidiRun
     */
    public BidiRun getParagraphByIndex(int paraIndex)
    {
        verifyValidParaOrLine();
        verifyRange(paraIndex, 0, paraCount);

        Bidi bidi = paraBidi;             /* get Para object if Line object */
        int paraStart;
        if (paraIndex == 0) {
            paraStart = 0;
        } else {
            paraStart = bidi.paras_limit[paraIndex - 1];
        }
        BidiRun bidiRun = new BidiRun();
        bidiRun.start = paraStart;
        bidiRun.limit = bidi.paras_limit[paraIndex];
        bidiRun.level = GetParaLevelAt(paraStart);
        return bidiRun;
    }

    /**
     * Get a paragraph, given a position within the text.
     * This method returns information about a paragraph.<br>
     * Note: if the paragraph index is known, it is more efficient to
     * retrieve the paragraph information using getParagraphByIndex().<p>
     *
     * @param charIndex is the index of a character within the text, in the
     *        range <code>[0..getProcessedLength()-1]</code>.
     *
     * @return a BidiRun object with the details of the paragraph:<br>
     *        <code>start</code> will receive the index of the first character
     *        of the paragraph in the text.<br>
     *        <code>limit</code> will receive the limit of the paragraph.<br>
     *        <code>embeddingLevel</code> will receive the level of the paragraph.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if charIndex is not within the legal range
     *
     * @see android.icu.text.BidiRun
     * @see #getParagraphByIndex
     * @see #getProcessedLength
     */
    public BidiRun getParagraph(int charIndex)
    {
        verifyValidParaOrLine();
        Bidi bidi = paraBidi;             /* get Para object if Line object */
        verifyRange(charIndex, 0, bidi.length);
        int paraIndex;
        for (paraIndex = 0; charIndex >= bidi.paras_limit[paraIndex]; paraIndex++) {
        }
        return getParagraphByIndex(paraIndex);
    }

    /**
     * Get the index of a paragraph, given a position within the text.<p>
     *
     * @param charIndex is the index of a character within the text, in the
     *        range <code>[0..getProcessedLength()-1]</code>.
     *
     * @return The index of the paragraph containing the specified position,
     *         starting from 0.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if charIndex is not within the legal range
     *
     * @see android.icu.text.BidiRun
     * @see #getProcessedLength
     */
    public int getParagraphIndex(int charIndex)
    {
        verifyValidParaOrLine();
        Bidi bidi = paraBidi;             /* get Para object if Line object */
        verifyRange(charIndex, 0, bidi.length);
        int paraIndex;
        for (paraIndex = 0; charIndex >= bidi.paras_limit[paraIndex]; paraIndex++) {
        }
        return paraIndex;
    }

    /**
     * Set a custom Bidi classifier used by the UBA implementation for Bidi
     * class determination.
     *
     * @param classifier A new custom classifier. This can be null.
     *
     * @see #getCustomClassifier
     */
    public void setCustomClassifier(BidiClassifier classifier) {
        this.customClassifier = classifier;
    }

    /**
     * Gets the current custom class classifier used for Bidi class
     * determination.
     *
     * @return An instance of class <code>BidiClassifier</code>
     *
     * @see #setCustomClassifier
     */
    public BidiClassifier getCustomClassifier() {
        return this.customClassifier;
    }

    /**
     * Retrieves the Bidi class for a given code point.
     * <p>If a <code>BidiClassifier</code> is defined and returns a value
     * other than <code>CLASS_DEFAULT=UCharacter.getIntPropertyMaxValue(UProperty.BIDI_CLASS)+1</code>,
     * that value is used; otherwise the default class determination mechanism is invoked.
     *
     * @param c The code point to get a Bidi class for.
     *
     * @return The Bidi class for the character <code>c</code> that is in effect
     *         for this <code>Bidi</code> instance.
     *
     * @see BidiClassifier
     */
    public int getCustomizedClass(int c) {
        int dir;

        if (customClassifier == null ||
                (dir = customClassifier.classify(c)) == Bidi.CLASS_DEFAULT) {
            dir = bdp.getClass(c);
        }
        if (dir >= UCharacterDirection.CHAR_DIRECTION_COUNT)
            dir = ON;
        return dir;
    }

    /**
     * <code>setLine()</code> returns a <code>Bidi</code> object to
     * contain the reordering information, especially the resolved levels,
     * for all the characters in a line of text. This line of text is
     * specified by referring to a <code>Bidi</code> object representing
     * this information for a piece of text containing one or more paragraphs,
     * and by specifying a range of indexes in this text.<p>
     * In the new line object, the indexes will range from 0 to <code>limit-start-1</code>.<p>
     *
     * This is used after calling <code>setPara()</code>
     * for a piece of text, and after line-breaking on that text.
     * It is not necessary if each paragraph is treated as a single line.<p>
     *
     * After line-breaking, rules (L1) and (L2) for the treatment of
     * trailing WS and for reordering are performed on
     * a <code>Bidi</code> object that represents a line.<p>
     *
     * <strong>Important: </strong>the line <code>Bidi</code> object may
     * reference data within the global text <code>Bidi</code> object.
     * You should not alter the content of the global text object until
     * you are finished using the line object.
     *
     * @param start is the line's first index into the text.
     *
     * @param limit is just behind the line's last index into the text
     *        (its last index +1).
     *
     * @return a <code>Bidi</code> object that will now represent a line of the text.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code>
     * @throws IllegalArgumentException if start and limit are not in the range
     *         <code>0&lt;=start&lt;limit&lt;=getProcessedLength()</code>,
     *         or if the specified line crosses a paragraph boundary
     *
     * @see #setPara
     * @see #getProcessedLength
     */
    public Bidi setLine(int start, int limit)
    {
        verifyValidPara();
        verifyRange(start, 0, limit);
        verifyRange(limit, 0, length+1);
        if (getParagraphIndex(start) != getParagraphIndex(limit - 1)) {
            /* the line crosses a paragraph boundary */
            throw new IllegalArgumentException();
        }
        return BidiLine.setLine(this, start, limit);
    }

    /**
     * Get the level for one character.
     *
     * @param charIndex the index of a character.
     *
     * @return The level for the character at <code>charIndex</code>.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if charIndex is not in the range
     *         <code>0&lt;=charIndex&lt;getProcessedLength()</code>
     *
     * @see #getProcessedLength
     */
    public byte getLevelAt(int charIndex)
    {
        verifyValidParaOrLine();
        verifyRange(charIndex, 0, length);
        return BidiLine.getLevelAt(this, charIndex);
    }

    /**
     * Get an array of levels for each character.<p>
     *
     * Note that this method may allocate memory under some
     * circumstances, unlike <code>getLevelAt()</code>.
     *
     * @return The levels array for the text,
     *         or <code>null</code> if an error occurs.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     */
    public byte[] getLevels()
    {
        verifyValidParaOrLine();
        if (length <= 0) {
            return new byte[0];
        }
        return BidiLine.getLevels(this);
    }

    /**
     * Get a logical run.
     * This method returns information about a run and is used
     * to retrieve runs in logical order.<p>
     * This is especially useful for line-breaking on a paragraph.
     *
     * @param logicalPosition is a logical position within the source text.
     *
     * @return a BidiRun object filled with <code>start</code> containing
     *        the first character of the run, <code>limit</code> containing
     *        the limit of the run, and <code>embeddingLevel</code> containing
     *        the level of the run.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if logicalPosition is not in the range
     *         <code>0&lt;=logicalPosition&lt;getProcessedLength()</code>
     *
     * @see android.icu.text.BidiRun
     * @see android.icu.text.BidiRun#getStart()
     * @see android.icu.text.BidiRun#getLimit()
     * @see android.icu.text.BidiRun#getEmbeddingLevel()
     */
    public BidiRun getLogicalRun(int logicalPosition)
    {
        verifyValidParaOrLine();
        verifyRange(logicalPosition, 0, length);
        return BidiLine.getLogicalRun(this, logicalPosition);
    }

    /**
     * Get the number of runs.
     * This method may invoke the actual reordering on the
     * <code>Bidi</code> object, after <code>setPara()</code>
     * may have resolved only the levels of the text. Therefore,
     * <code>countRuns()</code> may have to allocate memory,
     * and may throw an exception if it fails to do so.
     *
     * @return The number of runs.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     */
    public int countRuns()
    {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        return runCount;
    }

    /**
     *
     * Get a <code>BidiRun</code> object according to its index. BidiRun methods
     * may be used to retrieve the run's logical start, length and level,
     * which can be even for an LTR run or odd for an RTL run.
     * In an RTL run, the character at the logical start is
     * visually on the right of the displayed run.
     * The length is the number of characters in the run.<p>
     * <code>countRuns()</code> is normally called
     * before the runs are retrieved.
     *
     * <p>
     *  Example:
     * <pre>
     *  Bidi bidi = new Bidi();
     *  String text = "abc 123 DEFG xyz";
     *  bidi.setPara(text, Bidi.RTL, null);
     *  int i, count=bidi.countRuns(), logicalStart, visualIndex=0, length;
     *  BidiRun run;
     *  for (i = 0; i &lt; count; ++i) {
     *      run = bidi.getVisualRun(i);
     *      logicalStart = run.getStart();
     *      length = run.getLength();
     *      if (Bidi.LTR == run.getEmbeddingLevel()) {
     *          do { // LTR
     *              show_char(text.charAt(logicalStart++), visualIndex++);
     *          } while (--length &gt; 0);
     *      } else {
     *          logicalStart += length;  // logicalLimit
     *          do { // RTL
     *              show_char(text.charAt(--logicalStart), visualIndex++);
     *          } while (--length &gt; 0);
     *      }
     *  }
     * </pre>
     * <p>
     * Note that in right-to-left runs, code like this places
     * second surrogates before first ones (which is generally a bad idea)
     * and combining characters before base characters.
     * <p>
     * Use of <code>{@link #writeReordered}</code>, optionally with the
     * <code>{@link #KEEP_BASE_COMBINING}</code> option, can be considered in
     * order to avoid these issues.
     *
     * @param runIndex is the number of the run in visual order, in the
     *        range <code>[0..countRuns()-1]</code>.
     *
     * @return a BidiRun object containing the details of the run. The
     *         directionality of the run is
     *         <code>LTR==0</code> or <code>RTL==1</code>,
     *         never <code>MIXED</code>.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if <code>runIndex</code> is not in
     *         the range <code>0&lt;=runIndex&lt;countRuns()</code>
     *
     * @see #countRuns()
     * @see android.icu.text.BidiRun
     * @see android.icu.text.BidiRun#getStart()
     * @see android.icu.text.BidiRun#getLength()
     * @see android.icu.text.BidiRun#getEmbeddingLevel()
     */
    public BidiRun getVisualRun(int runIndex)
    {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(runIndex, 0, runCount);
        return BidiLine.getVisualRun(this, runIndex);
    }

    /**
     * Get the visual position from a logical text position.
     * If such a mapping is used many times on the same
     * <code>Bidi</code> object, then calling
     * <code>getLogicalMap()</code> is more efficient.
     * <p>
     * The value returned may be <code>MAP_NOWHERE</code> if there is no
     * visual position because the corresponding text character is a Bidi
     * control removed from output by the option
     * <code>OPTION_REMOVE_CONTROLS</code>.
     * <p>
     * When the visual output is altered by using options of
     * <code>writeReordered()</code> such as <code>INSERT_LRM_FOR_NUMERIC</code>,
     * <code>KEEP_BASE_COMBINING</code>, <code>OUTPUT_REVERSE</code>,
     * <code>REMOVE_BIDI_CONTROLS</code>, the visual position returned may not
     * be correct. It is advised to use, when possible, reordering options
     * such as {@link #OPTION_INSERT_MARKS} and {@link #OPTION_REMOVE_CONTROLS}.
     * <p>
     * Note that in right-to-left runs, this mapping places
     * second surrogates before first ones (which is generally a bad idea)
     * and combining characters before base characters.
     * Use of <code>{@link #writeReordered}</code>, optionally with the
     * <code>{@link #KEEP_BASE_COMBINING}</code> option can be considered instead
     * of using the mapping, in order to avoid these issues.
     *
     * @param logicalIndex is the index of a character in the text.
     *
     * @return The visual position of this character.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if <code>logicalIndex</code> is not in
     *         the range <code>0&lt;=logicalIndex&lt;getProcessedLength()</code>
     *
     * @see #getLogicalMap
     * @see #getLogicalIndex
     * @see #getProcessedLength
     * @see #MAP_NOWHERE
     * @see #OPTION_REMOVE_CONTROLS
     * @see #writeReordered
     */
    public int getVisualIndex(int logicalIndex)
    {
        verifyValidParaOrLine();
        verifyRange(logicalIndex, 0, length);
        return BidiLine.getVisualIndex(this, logicalIndex);
    }


    /**
     * Get the logical text position from a visual position.
     * If such a mapping is used many times on the same
     * <code>Bidi</code> object, then calling
     * <code>getVisualMap()</code> is more efficient.
     * <p>
     * The value returned may be <code>MAP_NOWHERE</code> if there is no
     * logical position because the corresponding text character is a Bidi
     * mark inserted in the output by option
     * <code>OPTION_INSERT_MARKS</code>.
     * <p>
     * This is the inverse method to <code>getVisualIndex()</code>.
     * <p>
     * When the visual output is altered by using options of
     * <code>writeReordered()</code> such as <code>INSERT_LRM_FOR_NUMERIC</code>,
     * <code>KEEP_BASE_COMBINING</code>, <code>OUTPUT_REVERSE</code>,
     * <code>REMOVE_BIDI_CONTROLS</code>, the logical position returned may not
     * be correct. It is advised to use, when possible, reordering options
     * such as {@link #OPTION_INSERT_MARKS} and {@link #OPTION_REMOVE_CONTROLS}.
     *
     * @param visualIndex is the visual position of a character.
     *
     * @return The index of this character in the text.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if <code>visualIndex</code> is not in
     *         the range <code>0&lt;=visualIndex&lt;getResultLength()</code>
     *
     * @see #getVisualMap
     * @see #getVisualIndex
     * @see #getResultLength
     * @see #MAP_NOWHERE
     * @see #OPTION_INSERT_MARKS
     * @see #writeReordered
     */
    public int getLogicalIndex(int visualIndex)
    {
        verifyValidParaOrLine();
        verifyRange(visualIndex, 0, resultLength);
        /* we can do the trivial cases without the runs array */
        if (insertPoints.size == 0 && controlCount == 0) {
            if (direction == LTR) {
                return visualIndex;
            }
            else if (direction == RTL) {
                return length - visualIndex - 1;
            }
        }
        BidiLine.getRuns(this);
        return BidiLine.getLogicalIndex(this, visualIndex);
    }

    /**
     * Get a logical-to-visual index map (array) for the characters in the
     * <code>Bidi</code> (paragraph or line) object.
     * <p>
     * Some values in the map may be <code>MAP_NOWHERE</code> if the
     * corresponding text characters are Bidi controls removed from the visual
     * output by the option <code>OPTION_REMOVE_CONTROLS</code>.
     * <p>
     * When the visual output is altered by using options of
     * <code>writeReordered()</code> such as <code>INSERT_LRM_FOR_NUMERIC</code>,
     * <code>KEEP_BASE_COMBINING</code>, <code>OUTPUT_REVERSE</code>,
     * <code>REMOVE_BIDI_CONTROLS</code>, the visual positions returned may not
     * be correct. It is advised to use, when possible, reordering options
     * such as {@link #OPTION_INSERT_MARKS} and {@link #OPTION_REMOVE_CONTROLS}.
     * <p>
     * Note that in right-to-left runs, this mapping places
     * second surrogates before first ones (which is generally a bad idea)
     * and combining characters before base characters.
     * Use of <code>{@link #writeReordered}</code>, optionally with the
     * <code>{@link #KEEP_BASE_COMBINING}</code> option can be considered instead
     * of using the mapping, in order to avoid these issues.
     *
     * @return an array of <code>getProcessedLength()</code>
     *        indexes which will reflect the reordering of the characters.<br><br>
     *        The index map will result in
     *        <code>indexMap[logicalIndex]==visualIndex</code>, where
     *        <code>indexMap</code> represents the returned array.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     *
     * @see #getVisualMap
     * @see #getVisualIndex
     * @see #getProcessedLength
     * @see #MAP_NOWHERE
     * @see #OPTION_REMOVE_CONTROLS
     * @see #writeReordered
     */
    public int[] getLogicalMap()
    {
        /* countRuns() checks successful call to setPara/setLine */
        countRuns();
        if (length <= 0) {
            return new int[0];
        }
        return BidiLine.getLogicalMap(this);
    }

    /**
     * Get a visual-to-logical index map (array) for the characters in the
     * <code>Bidi</code> (paragraph or line) object.
     * <p>
     * Some values in the map may be <code>MAP_NOWHERE</code> if the
     * corresponding text characters are Bidi marks inserted in the visual
     * output by the option <code>OPTION_INSERT_MARKS</code>.
     * <p>
     * When the visual output is altered by using options of
     * <code>writeReordered()</code> such as <code>INSERT_LRM_FOR_NUMERIC</code>,
     * <code>KEEP_BASE_COMBINING</code>, <code>OUTPUT_REVERSE</code>,
     * <code>REMOVE_BIDI_CONTROLS</code>, the logical positions returned may not
     * be correct. It is advised to use, when possible, reordering options
     * such as {@link #OPTION_INSERT_MARKS} and {@link #OPTION_REMOVE_CONTROLS}.
     *
     * @return an array of <code>getResultLength()</code>
     *        indexes which will reflect the reordering of the characters.<br><br>
     *        The index map will result in
     *        <code>indexMap[visualIndex]==logicalIndex</code>, where
     *        <code>indexMap</code> represents the returned array.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     *
     * @see #getLogicalMap
     * @see #getLogicalIndex
     * @see #getResultLength
     * @see #MAP_NOWHERE
     * @see #OPTION_INSERT_MARKS
     * @see #writeReordered
     */
    public int[] getVisualMap()
    {
        /* countRuns() checks successful call to setPara/setLine */
        countRuns();
        if (resultLength <= 0) {
            return new int[0];
        }
        return BidiLine.getVisualMap(this);
    }

    /**
     * This is a convenience method that does not use a <code>Bidi</code> object.
     * It is intended to be used for when an application has determined the levels
     * of objects (character sequences) and just needs to have them reordered (L2).
     * This is equivalent to using <code>getLogicalMap()</code> on a
     * <code>Bidi</code> object.
     *
     * @param levels is an array of levels that have been determined by
     *        the application.
     *
     * @return an array of <code>levels.length</code>
     *        indexes which will reflect the reordering of the characters.<p>
     *        The index map will result in
     *        <code>indexMap[logicalIndex]==visualIndex</code>, where
     *        <code>indexMap</code> represents the returned array.
     */
    public static int[] reorderLogical(byte[] levels)
    {
        return BidiLine.reorderLogical(levels);
    }

    /**
     * This is a convenience method that does not use a <code>Bidi</code> object.
     * It is intended to be used for when an application has determined the levels
     * of objects (character sequences) and just needs to have them reordered (L2).
     * This is equivalent to using <code>getVisualMap()</code> on a
     * <code>Bidi</code> object.
     *
     * @param levels is an array of levels that have been determined by
     *        the application.
     *
     * @return an array of <code>levels.length</code>
     *        indexes which will reflect the reordering of the characters.<p>
     *        The index map will result in
     *        <code>indexMap[visualIndex]==logicalIndex</code>, where
     *        <code>indexMap</code> represents the returned array.
     */
    public static int[] reorderVisual(byte[] levels)
    {
        return BidiLine.reorderVisual(levels);
    }

    /**
     * Invert an index map.
     * The index mapping of the argument map is inverted and returned as
     * an array of indexes that we will call the inverse map.
     *
     * @param srcMap is an array whose elements define the original mapping
     * from a source array to a destination array.
     * Some elements of the source array may have no mapping in the
     * destination array. In that case, their value will be
     * the special value <code>MAP_NOWHERE</code>.
     * All elements must be &gt;=0 or equal to <code>MAP_NOWHERE</code>.
     * Some elements in the source map may have a value greater than the
     * srcMap.length if the destination array has more elements than the
     * source array.
     * There must be no duplicate indexes (two or more elements with the
     * same value except <code>MAP_NOWHERE</code>).
     *
     * @return an array representing the inverse map.
     *         This array has a number of elements equal to 1 + the highest
     *         value in <code>srcMap</code>.
     *         For elements of the result array which have no matching elements
     *         in the source array, the corresponding elements in the inverse
     *         map will receive a value equal to <code>MAP_NOWHERE</code>.
     *         If element with index i in <code>srcMap</code> has a value k different
     *         from <code>MAP_NOWHERE</code>, this means that element i of
     *         the source array maps to element k in the destination array.
     *         The inverse map will have value i in its k-th element.
     *         For all elements of the destination array which do not map to
     *         an element in the source array, the corresponding element in the
     *         inverse map will have a value equal to <code>MAP_NOWHERE</code>.
     *
     * @see #MAP_NOWHERE
     */
    public static int[] invertMap(int[] srcMap)
    {
        if (srcMap == null) {
            return null;
        } else {
            return BidiLine.invertMap(srcMap);
        }
    }

    /*
     * Fields and methods for compatibility with java.text.bidi (Sun implementation)
     */

    /**
     * Constant indicating base direction is left-to-right.
     */
    public static final int DIRECTION_LEFT_TO_RIGHT = LTR;

    /**
     * Constant indicating base direction is right-to-left.
     */
    public static final int DIRECTION_RIGHT_TO_LEFT = RTL;

    /**
     * Constant indicating that the base direction depends on the first strong
     * directional character in the text according to the Unicode Bidirectional
     * Algorithm. If no strong directional character is present, the base
     * direction is left-to-right.
     */
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = LEVEL_DEFAULT_LTR;

    /**
     * Constant indicating that the base direction depends on the first strong
     * directional character in the text according to the Unicode Bidirectional
     * Algorithm. If no strong directional character is present, the base
     * direction is right-to-left.
     */
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = LEVEL_DEFAULT_RTL;

    /**
     * Create Bidi from the given paragraph of text and base direction.
     *
     * @param paragraph a paragraph of text
     * @param flags a collection of flags that control the algorithm. The
     *        algorithm understands the flags DIRECTION_LEFT_TO_RIGHT,
     *        DIRECTION_RIGHT_TO_LEFT, DIRECTION_DEFAULT_LEFT_TO_RIGHT, and
     *        DIRECTION_DEFAULT_RIGHT_TO_LEFT. Other values are reserved.
     * @see #DIRECTION_LEFT_TO_RIGHT
     * @see #DIRECTION_RIGHT_TO_LEFT
     * @see #DIRECTION_DEFAULT_LEFT_TO_RIGHT
     * @see #DIRECTION_DEFAULT_RIGHT_TO_LEFT
     */
    public Bidi(String paragraph, int flags)
    {
        this(paragraph.toCharArray(), 0, null, 0, paragraph.length(), flags);
    }

    /**
     * Create Bidi from the given paragraph of text.<p>
     *
     * The RUN_DIRECTION attribute in the text, if present, determines the base
     * direction (left-to-right or right-to-left). If not present, the base
     * direction is computed using the Unicode Bidirectional Algorithm,
     * defaulting to left-to-right if there are no strong directional characters
     * in the text. This attribute, if present, must be applied to all the text
     * in the paragraph.<p>
     *
     * The BIDI_EMBEDDING attribute in the text, if present, represents
     * embedding level information. Negative values from -1 to -62 indicate
     * overrides at the absolute value of the level. Positive values from 1 to
     * 62 indicate embeddings. Where values are zero or not defined, the base
     * embedding level as determined by the base direction is assumed.<p>
     *
     * The NUMERIC_SHAPING attribute in the text, if present, converts European
     * digits to other decimal digits before running the bidi algorithm. This
     * attribute, if present, must be applied to all the text in the paragraph.<p>
     *
     * Note: this constructor calls setPara() internally.
     *
     * @param paragraph a paragraph of text with optional character and
     *        paragraph attribute information
     */
    public Bidi(AttributedCharacterIterator paragraph)
    {
        this();
        setPara(paragraph);
    }

    /**
     * Create Bidi from the given text, embedding, and direction information.
     * The embeddings array may be null. If present, the values represent
     * embedding level information. Negative values from -1 to -61 indicate
     * overrides at the absolute value of the level. Positive values from 1 to
     * 61 indicate embeddings. Where values are zero, the base embedding level
     * as determined by the base direction is assumed.<p>
     *
     * Note: this constructor calls setPara() internally.
     *
     * @param text an array containing the paragraph of text to process.
     * @param textStart the index into the text array of the start of the
     *        paragraph.
     * @param embeddings an array containing embedding values for each character
     *        in the paragraph. This can be null, in which case it is assumed
     *        that there is no external embedding information.
     * @param embStart the index into the embedding array of the start of the
     *        paragraph.
     * @param paragraphLength the length of the paragraph in the text and
     *        embeddings arrays.
     * @param flags a collection of flags that control the algorithm. The
     *        algorithm understands the flags DIRECTION_LEFT_TO_RIGHT,
     *        DIRECTION_RIGHT_TO_LEFT, DIRECTION_DEFAULT_LEFT_TO_RIGHT, and
     *        DIRECTION_DEFAULT_RIGHT_TO_LEFT. Other values are reserved.
     *
     * @throws IllegalArgumentException if the values in embeddings are
     *         not within the allowed range
     *
     * @see #DIRECTION_LEFT_TO_RIGHT
     * @see #DIRECTION_RIGHT_TO_LEFT
     * @see #DIRECTION_DEFAULT_LEFT_TO_RIGHT
     * @see #DIRECTION_DEFAULT_RIGHT_TO_LEFT
     */
    public Bidi(char[] text,
            int textStart,
            byte[] embeddings,
            int embStart,
            int paragraphLength,
            int flags)
    {
        this();
        byte paraLvl;
        switch (flags) {
        case DIRECTION_LEFT_TO_RIGHT:
        default:
            paraLvl = LTR;
            break;
        case DIRECTION_RIGHT_TO_LEFT:
            paraLvl = RTL;
            break;
        case DIRECTION_DEFAULT_LEFT_TO_RIGHT:
            paraLvl = LEVEL_DEFAULT_LTR;
            break;
        case DIRECTION_DEFAULT_RIGHT_TO_LEFT:
            paraLvl = LEVEL_DEFAULT_RTL;
            break;
        }
        byte[] paraEmbeddings;
        if (embeddings == null) {
            paraEmbeddings = null;
        } else {
            paraEmbeddings = new byte[paragraphLength];
            byte lev;
            for (int i = 0; i < paragraphLength; i++) {
                lev = embeddings[i + embStart];
                if (lev < 0) {
                    lev = (byte)((- lev) | LEVEL_OVERRIDE);
                } else if (lev == 0) {
                    lev = paraLvl;
                    if (paraLvl > MAX_EXPLICIT_LEVEL) {
                        lev &= 1;
                    }
                }
                paraEmbeddings[i] = lev;
            }
        }
        if (textStart == 0 && embStart == 0 && paragraphLength == text.length) {
            setPara(text, paraLvl, paraEmbeddings);
        } else {
            char[] paraText = new char[paragraphLength];
            System.arraycopy(text, textStart, paraText, 0, paragraphLength);
            setPara(paraText, paraLvl, paraEmbeddings);
        }
    }

    /**
     * Create a Bidi object representing the bidi information on a line of text
     * within the paragraph represented by the current Bidi. This call is not
     * required if the entire paragraph fits on one line.
     *
     * @param lineStart the offset from the start of the paragraph to the start
     *        of the line.
     * @param lineLimit the offset from the start of the paragraph to the limit
     *        of the line.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code>
     * @throws IllegalArgumentException if lineStart and lineLimit are not in the range
     *         <code>0&lt;=lineStart&lt;lineLimit&lt;=getProcessedLength()</code>,
     *         or if the specified line crosses a paragraph boundary
     */
    public Bidi createLineBidi(int lineStart, int lineLimit)
    {
        return setLine(lineStart, lineLimit);
    }

    /**
     * Return true if the line is not left-to-right or right-to-left. This means
     * it either has mixed runs of left-to-right and right-to-left text, or the
     * base direction differs from the direction of the only run of text.
     *
     * @return true if the line is not left-to-right or right-to-left.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code>
     */
    public boolean isMixed()
    {
        return (!isLeftToRight() && !isRightToLeft());
    }

    /**
     * Return true if the line is all left-to-right text and the base direction
     * is left-to-right.
     *
     * @return true if the line is all left-to-right text and the base direction
     *         is left-to-right.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code>
     */
    public boolean isLeftToRight()
    {
        return (getDirection() == LTR && (paraLevel & 1) == 0);
    }

    /**
     * Return true if the line is all right-to-left text, and the base direction
     * is right-to-left
     *
     * @return true if the line is all right-to-left text, and the base
     *         direction is right-to-left
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code>
     */
    public boolean isRightToLeft()
    {
        return (getDirection() == RTL && (paraLevel & 1) == 1);
    }

    /**
     * Return true if the base direction is left-to-right
     *
     * @return true if the base direction is left-to-right
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     */
    public boolean baseIsLeftToRight()
    {
        return (getParaLevel() == LTR);
    }

    /**
     * Return the base level (0 if left-to-right, 1 if right-to-left).
     *
     * @return the base level
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     */
    public int getBaseLevel()
    {
        return getParaLevel();
    }

    /**
     * Return the number of level runs.
     *
     * @return the number of level runs
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     */
    public int getRunCount()
    {
        return countRuns();
    }

    /**
     * Compute the logical to visual run mapping
     */
     void getLogicalToVisualRunsMap()
     {
        if (isGoodLogicalToVisualRunsMap) {
            return;
        }
        int count = countRuns();
        if ((logicalToVisualRunsMap == null) ||
            (logicalToVisualRunsMap.length < count)) {
            logicalToVisualRunsMap = new int[count];
        }
        int i;
        long[] keys = new long[count];
        for (i = 0; i < count; i++) {
            keys[i] = ((long)(runs[i].start)<<32) + i;
        }
        Arrays.sort(keys);
        for (i = 0; i < count; i++) {
            logicalToVisualRunsMap[i] = (int)(keys[i] & 0x00000000FFFFFFFF);
        }
        isGoodLogicalToVisualRunsMap = true;
     }

    /**
     * Return the level of the nth logical run in this line.
     *
     * @param run the index of the run, between 0 and <code>countRuns()-1</code>
     *
     * @return the level of the run
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if <code>run</code> is not in
     *         the range <code>0&lt;=run&lt;countRuns()</code>
     */
    public int getRunLevel(int run)
    {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(run, 0, runCount);
        getLogicalToVisualRunsMap();
        return runs[logicalToVisualRunsMap[run]].level;
    }

    /**
     * Return the index of the character at the start of the nth logical run in
     * this line, as an offset from the start of the line.
     *
     * @param run the index of the run, between 0 and <code>countRuns()</code>
     *
     * @return the start of the run
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if <code>run</code> is not in
     *         the range <code>0&lt;=run&lt;countRuns()</code>
     */
    public int getRunStart(int run)
    {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(run, 0, runCount);
        getLogicalToVisualRunsMap();
        return runs[logicalToVisualRunsMap[run]].start;
    }

    /**
     * Return the index of the character past the end of the nth logical run in
     * this line, as an offset from the start of the line. For example, this
     * will return the length of the line for the last run on the line.
     *
     * @param run the index of the run, between 0 and <code>countRuns()</code>
     *
     * @return the limit of the run
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     * @throws IllegalArgumentException if <code>run</code> is not in
     *         the range <code>0&lt;=run&lt;countRuns()</code>
     */
    public int getRunLimit(int run)
    {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(run, 0, runCount);
        getLogicalToVisualRunsMap();
        int idx = logicalToVisualRunsMap[run];
        int len = idx == 0 ? runs[idx].limit :
                                runs[idx].limit - runs[idx-1].limit;
        return runs[idx].start + len;
    }

    /**
     * Return true if the specified text requires bidi analysis. If this returns
     * false, the text will display left-to-right. Clients can then avoid
     * constructing a Bidi object. Text in the Arabic Presentation Forms area of
     * Unicode is presumed to already be shaped and ordered for display, and so
     * will not cause this method to return true.
     *
     * @param text the text containing the characters to test
     * @param start the start of the range of characters to test
     * @param limit the limit of the range of characters to test
     *
     * @return true if the range of characters requires bidi analysis
     */
    public static boolean requiresBidi(char[] text,
            int start,
            int limit)
    {
        final int RTLMask = (1 << UCharacter.DIRECTIONALITY_RIGHT_TO_LEFT |
                1 << UCharacter.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC |
                1 << UCharacter.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING |
                1 << UCharacter.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE |
                1 << UCharacter.DIRECTIONALITY_ARABIC_NUMBER);

        for (int i = start; i < limit; ++i) {
            if (((1 << UCharacter.getDirection(text[i])) & RTLMask) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reorder the objects in the array into visual order based on their levels.
     * This is a utility method to use when you have a collection of objects
     * representing runs of text in logical order, each run containing text at a
     * single level. The elements at <code>index</code> from
     * <code>objectStart</code> up to <code>objectStart + count</code> in the
     * objects array will be reordered into visual order assuming
     * each run of text has the level indicated by the corresponding element in
     * the levels array (at <code>index - objectStart + levelStart</code>).
     *
     * @param levels an array representing the bidi level of each object
     * @param levelStart the start position in the levels array
     * @param objects the array of objects to be reordered into visual order
     * @param objectStart the start position in the objects array
     * @param count the number of objects to reorder
     */
    public static void reorderVisually(byte[] levels,
            int levelStart,
            Object[] objects,
            int objectStart,
            int count)
    {
        byte[] reorderLevels = new byte[count];
        System.arraycopy(levels, levelStart, reorderLevels, 0, count);
        int[] indexMap = reorderVisual(reorderLevels);
        Object[] temp = new Object[count];
        System.arraycopy(objects, objectStart, temp, 0, count);
        for (int i = 0; i < count; ++i) {
            objects[objectStart + i] = temp[indexMap[i]];
        }
    }

    /**
     * Take a <code>Bidi</code> object containing the reordering
     * information for a piece of text (one or more paragraphs) set by
     * <code>setPara()</code> or for a line of text set by <code>setLine()</code>
     * and return a string containing the reordered text.
     *
     * <p>The text may have been aliased (only a reference was stored
     * without copying the contents), thus it must not have been modified
     * since the <code>setPara()</code> call.
     *
     * This method preserves the integrity of characters with multiple
     * code units and (optionally) combining characters.
     * Characters in RTL runs can be replaced by mirror-image characters
     * in the returned string. Note that "real" mirroring has to be done in a
     * rendering engine by glyph selection and that for many "mirrored"
     * characters there are no Unicode characters as mirror-image equivalents.
     * There are also options to insert or remove Bidi control
     * characters; see the descriptions of the return value and the
     * <code>options</code> parameter, and of the option bit flags.
     *
     * @param options A bit set of options for the reordering that control
     *                how the reordered text is written.
     *                The options include mirroring the characters on a code
     *                point basis and inserting LRM characters, which is used
     *                especially for transforming visually stored text
     *                to logically stored text (although this is still an
     *                imperfect implementation of an "inverse Bidi" algorithm
     *                because it uses the "forward Bidi" algorithm at its core).
     *                The available options are:
     *                <code>DO_MIRRORING</code>,
     *                <code>INSERT_LRM_FOR_NUMERIC</code>,
     *                <code>KEEP_BASE_COMBINING</code>,
     *                <code>OUTPUT_REVERSE</code>,
     *                <code>REMOVE_BIDI_CONTROLS</code>,
     *                <code>STREAMING</code>
     *
     * @return The reordered text.
     *         If the <code>INSERT_LRM_FOR_NUMERIC</code> option is set, then
     *         the length of the returned string could be as large as
     *         <code>getLength()+2*countRuns()</code>.<br>
     *         If the <code>REMOVE_BIDI_CONTROLS</code> option is set, then the
     *         length of the returned string may be less than
     *         <code>getLength()</code>.<br>
     *         If none of these options is set, then the length of the returned
     *         string will be exactly <code>getProcessedLength()</code>.
     *
     * @throws IllegalStateException if this call is not preceded by a successful
     *         call to <code>setPara</code> or <code>setLine</code>
     *
     * @see #DO_MIRRORING
     * @see #INSERT_LRM_FOR_NUMERIC
     * @see #KEEP_BASE_COMBINING
     * @see #OUTPUT_REVERSE
     * @see #REMOVE_BIDI_CONTROLS
     * @see #OPTION_STREAMING
     * @see #getProcessedLength
     */
    public String writeReordered(int options)
    {
        verifyValidParaOrLine();
        if (length == 0) {
            /* nothing to do */
            return "";
        }
        return BidiWriter.writeReordered(this, options);
    }

    /**
     * Reverse a Right-To-Left run of Unicode text.
     *
     * This method preserves the integrity of characters with multiple
     * code units and (optionally) combining characters.
     * Characters can be replaced by mirror-image characters
     * in the destination buffer. Note that "real" mirroring has
     * to be done in a rendering engine by glyph selection
     * and that for many "mirrored" characters there are no
     * Unicode characters as mirror-image equivalents.
     * There are also options to insert or remove Bidi control
     * characters.
     *
     * This method is the implementation for reversing RTL runs as part
     * of <code>writeReordered()</code>. For detailed descriptions
     * of the parameters, see there.
     * Since no Bidi controls are inserted here, the output string length
     * will never exceed <code>src.length()</code>.
     *
     * @see #writeReordered
     *
     * @param src The RTL run text.
     *
     * @param options A bit set of options for the reordering that control
     *                how the reordered text is written.
     *                See the <code>options</code> parameter in <code>writeReordered()</code>.
     *
     * @return The reordered text.
     *         If the <code>REMOVE_BIDI_CONTROLS</code> option
     *         is set, then the length of the returned string may be less than
     *         <code>src.length()</code>. If this option is not set,
     *         then the length of the returned string will be exactly
     *         <code>src.length()</code>.
     *
     * @throws IllegalArgumentException if <code>src</code> is null.
     */
    public static String writeReverse(String src, int options)
    {
        /* error checking */
        if (src == null) {
            throw new IllegalArgumentException();
        }

        if (src.length() > 0) {
            return BidiWriter.writeReverse(src, options);
        } else {
            /* nothing to do */
            return "";
        }
    }

}
