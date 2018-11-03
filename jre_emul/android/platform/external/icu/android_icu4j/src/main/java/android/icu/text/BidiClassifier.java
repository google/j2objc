/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2000-2009, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
/* Written by Simon Montagu, Matitiahu Allouche
 * (ported from C code written by Markus W. Scherer)
 */

package android.icu.text;

/**
 * Overrides default Bidi class values with custom ones.
 *
 * <p>The override mechanism requires to define a subclass of
 * <code>BidiClassifier</code> which overrides the <code>classifier</code>
 * method to assign customized Bidi classes.</p>
 *
 * <p>This may be useful for assigning Bidi classes to PUA characters, or
 * for special application needs. For instance, an application may want to
 * handle all spaces like L or R characters (according to the base direction)
 * when creating the visual ordering of logical lines which are part of a report
 * organized in columns: there should not be interaction between adjacent
 * cells.</p>
 *
 * <p>To start using this customized
 * classifier with a Bidi object, it must be specified by calling the
 * <code>Bidi.setCustomClassifier</code> method; after that, the method
 * <code>classify</code> of the custom <code>BidiClassifier</code> will be
 * called by the UBA implementation any time the class of a character is
 * to be determined.</p>
 *
 * @see Bidi#setCustomClassifier
 * @hide Only a subset of ICU is exposed in Android
 */

public /*abstract*/ class BidiClassifier {

    /**
     * This object can be used for any purpose by the caller to pass
     * information to the BidiClassifier methods, and by the BidiClassifier
     * methods themselves.<br>
     * For instance, this object can be used to save a reference to
     * a previous custom BidiClassifier while setting a new one, so as to
     * allow chaining between them.
     */
    protected Object context;

    /**
     * @param context Context for this classifier instance.
     *                May be null.
     */
    public BidiClassifier(Object context) {
        this.context = context;
    }

    /**
     * Sets classifier context, which can be used either by a caller or
     * callee for various purposes.
     *
     * @param context Context for this classifier instance.
     *                May be null.
     */
    public void setContext(Object context) {
        this.context = context;
    }

    /**
     * Returns the current classifier context.
     */
    public Object getContext() {
        return this.context;
    }

    /**
     * Gets customized Bidi class for the code point <code>c</code>.
     * <p>
     * Default implementation, to be overridden.
     *
     * @param c Code point to be classified.
     * @return An integer representing directional property / Bidi class for the
     *         given code point <code>c</code>, or Bidi.CLASS_DEFAULT=UCharacter.getIntPropertyMaxValue(UProperty.BIDI_CLASS)+1
     *         to signify that there is no need to override the standard Bidi class for
     *         the given code point.
     * @see Bidi#CLASS_DEFAULT
     */
    public int classify(int c) {
        return Bidi.CLASS_DEFAULT;
    }
}
