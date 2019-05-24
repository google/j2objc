/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.util.Locale;

import android.icu.impl.SimpleFilteredSentenceBreakIterator;
import android.icu.util.ULocale;

/**
 * The BreakIteratorFilter is used to modify the behavior of a BreakIterator
 *  by constructing a new BreakIterator which suppresses certain segment boundaries.
 *  See  http://www.unicode.org/reports/tr35/tr35-general.html#Segmentation_Exceptions .
 *  For example, a typical English Sentence Break Iterator would break on the space
 *  in the string "Mr. Smith" (resulting in two segments),
 *  but with "Mr." as an exception, a filtered break iterator
 *  would consider the string "Mr. Smith" to be a single segment.
 *
 * <p>This class is not intended for public subclassing.
 *
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public abstract class FilteredBreakIteratorBuilder {

    /**
     * Construct a FilteredBreakIteratorBuilder based on sentence break exception rules in a locale.
     * The rules are taken from CLDR exception data for the locale,
     * see http://www.unicode.org/reports/tr35/tr35-general.html#Segmentation_Exceptions
     * This is the equivalent of calling createInstance(UErrorCode&amp;)
     * and then repeatedly calling addNoBreakAfter(...) with the contents
     * of the CLDR exception data.
     * @param where the locale.
     * @return the new builder
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final FilteredBreakIteratorBuilder getInstance(Locale where) {
        return new SimpleFilteredSentenceBreakIterator.Builder(where);
    }

    /**
     * Construct a FilteredBreakIteratorBuilder based on sentence break exception rules in a locale.
     * The rules are taken from CLDR exception data for the locale,
     * see http://www.unicode.org/reports/tr35/tr35-general.html#Segmentation_Exceptions
     * This is the equivalent of calling createInstance(UErrorCode&amp;)
     * and then repeatedly calling addNoBreakAfter(...) with the contents
     * of the CLDR exception data.
     * @param where the locale.
     * @return the new builder
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final FilteredBreakIteratorBuilder getInstance(ULocale where) {
        return new SimpleFilteredSentenceBreakIterator.Builder(where);
    }

    /**
     * Construct an empty FilteredBreakIteratorBuilder.
     * In this state, it will not suppress any segment boundaries.
     * @return the new builder
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final FilteredBreakIteratorBuilder getEmptyInstance() {
        return new SimpleFilteredSentenceBreakIterator.Builder();
    }

    /**
     * Suppress a certain string from being the end of a segment.
     * For example, suppressing "Mr.", then segments ending in "Mr." will not be returned
     * by the iterator.
     * @param str the string to suppress, such as "Mr."
     * @return true if the string was not present and now added,
     * false if the call was a no-op because the string was already being suppressed.
     * @hide draft / provisional / internal are hidden on Android
     */
    public abstract boolean suppressBreakAfter(CharSequence str);

    /**
     * Stop suppressing a certain string from being the end of the segment.
     * This function does not create any new segment boundaries, but only serves to un-do
     * the effect of earlier calls to suppressBreakAfter, or to un-do the effect of
     * locale data which may be suppressing certain strings.
     * @param str the str the string to unsuppress, such as "Mr."
     * @return true if the string was present and now removed,
     * false if the call was a no-op because the string was not being suppressed.
     * @hide draft / provisional / internal are hidden on Android
     */
    public abstract boolean unsuppressBreakAfter(CharSequence str);

    /**
     * Wrap (adopt) an existing break iterator in a new filtered instance.
     * Note that the wrappedBreakIterator is adopted by the new BreakIterator
     * and should no longer be used by the caller.
     * The FilteredBreakIteratorBuilder may be reused.
     * @param wrappedBreakIterator the break iterator to wrap
     * @return the new BreakIterator
     * @hide draft / provisional / internal are hidden on Android
     */
    public abstract BreakIterator wrapIteratorWithFilter(BreakIterator wrappedBreakIterator);

    /**
     * For subclass use
     * @deprecated internal to ICU
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected FilteredBreakIteratorBuilder() {
    }
}