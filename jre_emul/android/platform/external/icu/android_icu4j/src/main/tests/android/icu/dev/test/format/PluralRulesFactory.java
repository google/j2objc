/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2013, Google Inc, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import android.icu.text.PluralRules;
import android.icu.text.PluralRules.PluralType;
import android.icu.util.ULocale;

/**
 * @author markdavis
 *
 */
public abstract class PluralRulesFactory extends PluralRules.Factory {

    static final PluralRulesFactory NORMAL = new PluralRulesFactoryVanilla();

    private PluralRulesFactory() {}

    static class PluralRulesFactoryVanilla extends PluralRulesFactory {
        @Override
        public boolean hasOverride(ULocale locale) {
            return false;
        }
        @Override
        public PluralRules forLocale(ULocale locale, PluralType ordinal) {
            return PluralRules.forLocale(locale, ordinal);
        }
        @Override
        public ULocale[] getAvailableULocales() {
            return PluralRules.getAvailableULocales();
        }
        @Override
        public ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable) {
            return PluralRules.getFunctionalEquivalent(locale, isAvailable);
        }
    }
}
