/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
*******************************************************************************
* Copyright (C) 2003-2016, International Business Machines Corporation and
* others. All Rights Reserved.
*******************************************************************************
*/

package android.icu.text;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

import android.icu.impl.ICUData;
import android.icu.impl.ICULocaleService;
import android.icu.impl.ICULocaleService.LocaleKeyFactory;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUService;
import android.icu.impl.ICUService.Factory;
import android.icu.impl.coll.CollationLoader;
import android.icu.impl.coll.CollationTailoring;
import android.icu.text.Collator.CollatorFactory;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.Output;
import android.icu.util.ULocale;

final class CollatorServiceShim extends Collator.ServiceShim {

    @Override
    Collator getInstance(ULocale locale) {
    // use service cache, it's faster than instantiation
//          if (service.isDefault()) {
//              return new RuleBasedCollator(locale);
//          }
        try {
            ULocale[] actualLoc = new ULocale[1];
            Collator coll = (Collator)service.get(locale, actualLoc);
            if (coll == null) {
                ///CLOVER:OFF
                //Can't really change coll after it's been initialized
                throw new MissingResourceException("Could not locate Collator data", "", "");
                ///CLOVER:ON
            }
            return (Collator) coll.clone();
        }
        catch (CloneNotSupportedException e) {
        ///CLOVER:OFF
            throw new ICUCloneNotSupportedException(e);
        ///CLOVER:ON
        }
    }

    @Override
    Object registerInstance(Collator collator, ULocale locale) {
        // Set the collator locales while registering so that getInstance()
        // need not guess whether the collator's locales are already set properly
        // (as they are by the data loader).
        collator.setLocale(locale, locale);
        return service.registerObject(collator, locale);
    }

    @Override
    Object registerFactory(CollatorFactory f) {
        class CFactory extends LocaleKeyFactory {
            CollatorFactory delegate;

            CFactory(CollatorFactory fctry) {
                super(fctry.visible());
                this.delegate = fctry;
            }

            @Override
            public Object handleCreate(ULocale loc, int kind, ICUService srvc) {
                Object coll = delegate.createCollator(loc);
                return coll;
            }

            @Override
            public String getDisplayName(String id, ULocale displayLocale) {
                ULocale objectLocale = new ULocale(id);
                return delegate.getDisplayName(objectLocale, displayLocale);
            }

            @Override
            public Set<String> getSupportedIDs() {
                return delegate.getSupportedLocaleIDs();
            }
        }

        return service.registerFactory(new CFactory(f));
    }

    @Override
    boolean unregister(Object registryKey) {
        return service.unregisterFactory((Factory)registryKey);
    }

    @Override
    Locale[] getAvailableLocales() {
        // TODO rewrite this to just wrap getAvailableULocales later
        Locale[] result;
        if (service.isDefault()) {
            result = ICUResourceBundle.getAvailableLocales(ICUData.ICU_COLLATION_BASE_NAME,
                    ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        } else {
            result = service.getAvailableLocales();
        }
        return result;
    }

    @Override
    ULocale[] getAvailableULocales() {
        ULocale[] result;
        if (service.isDefault()) {
            result = ICUResourceBundle.getAvailableULocales(ICUData.ICU_COLLATION_BASE_NAME,
                    ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        } else {
            result = service.getAvailableULocales();
        }
        return result;
    }

    @Override
    String getDisplayName(ULocale objectLocale, ULocale displayLocale) {
        String id = objectLocale.getName();
        return service.getDisplayName(id, displayLocale);
    }

    private static class CService extends ICULocaleService {
        CService() {
            super("Collator");

            class CollatorFactory extends ICUResourceBundleFactory {
                CollatorFactory() {
                    super(ICUData.ICU_COLLATION_BASE_NAME);
                }

                @Override
                protected Object handleCreate(ULocale uloc, int kind, ICUService srvc) {
                    return makeInstance(uloc);
                }
            }

            this.registerFactory(new CollatorFactory());
            markDefault();
        }

        /**
         * makeInstance() returns an appropriate Collator for any locale.
         * It falls back to root if there is no specific data.
         *
         * <p>Without this override, the service code would fall back to the default locale
         * which is not desirable for an algorithm with a good Unicode default,
         * like collation.
         */
        @Override
        public String validateFallbackLocale() {
            return "";
        }

        ///CLOVER:OFF
        // The following method can not be reached by testing
        @Override
        protected Object handleDefault(Key key, String[] actualIDReturn) {
            if (actualIDReturn != null) {
                actualIDReturn[0] = "root";
            }
            try {
                return makeInstance(ULocale.ROOT);
            }
            catch (MissingResourceException e) {
                return null;
            }
        }
        ///CLOVER:ON
    }

    // Ported from C++ Collator::makeInstance().
    private static final Collator makeInstance(ULocale desiredLocale) {
        Output<ULocale> validLocale = new Output<ULocale>(ULocale.ROOT);
        CollationTailoring t =
            CollationLoader.loadTailoring(desiredLocale, validLocale);
        return new RuleBasedCollator(t, validLocale.value);
    }

    private static ICULocaleService service = new CService();
}
