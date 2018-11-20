/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.text;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import android.icu.impl.IDNA2003;
import android.icu.impl.UTS46;

/**
 * Abstract base class for IDNA processing.
 * See http://www.unicode.org/reports/tr46/
 * and http://www.ietf.org/rfc/rfc3490.txt
 * <p>
 * The IDNA class is not intended for public subclassing.
 * <p>
 * The non-static methods implement UTS #46 and IDNA2008.
 * IDNA2008 is implemented according to UTS #46, see getUTS46Instance().
 * <p>
 * IDNA2003 is obsolete. The static methods implement IDNA2003. They are all deprecated.
 * <p>
 * IDNA2003 API Overview:
 * <p>
 * The static IDNA API methods implement the IDNA protocol as defined in the
 * <a href="http://www.ietf.org/rfc/rfc3490.txt">IDNA RFC</a>.
 * The draft defines 2 operations: ToASCII and ToUnicode. Domain labels 
 * containing non-ASCII code points are required to be processed by
 * ToASCII operation before passing it to resolver libraries. Domain names
 * that are obtained from resolver libraries are required to be processed by
 * ToUnicode operation before displaying the domain name to the user.
 * IDNA requires that implementations process input strings with 
 * <a href="http://www.ietf.org/rfc/rfc3491.txt">Nameprep</a>, 
 * which is a profile of <a href="http://www.ietf.org/rfc/rfc3454.txt">Stringprep</a> , 
 * and then with <a href="http://www.ietf.org/rfc/rfc3492.txt">Punycode</a>. 
 * Implementations of IDNA MUST fully implement Nameprep and Punycode; 
 * neither Nameprep nor Punycode are optional.
 * The input and output of ToASCII and ToUnicode operations are Unicode 
 * and are designed to be chainable, i.e., applying ToASCII or ToUnicode operations
 * multiple times to an input string will yield the same result as applying the operation
 * once.
 * ToUnicode(ToUnicode(ToUnicode...(ToUnicode(string)))) == ToUnicode(string) 
 * ToASCII(ToASCII(ToASCII...(ToASCII(string))) == ToASCII(string).
 * 
 * @author Ram Viswanadha, Markus Scherer
 */
public abstract class IDNA {
    /** 
     * Default options value: None of the other options are set.
     * For use in static worker and factory methods.
     */
    public static final int DEFAULT = 0;
    /** 
     * Option to allow unassigned code points in domain names and labels.
     * For use in static worker and factory methods.
     * <p>This option is ignored by the UTS46 implementation.
     * (UTS #46 disallows unassigned code points.)
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static final int ALLOW_UNASSIGNED = 1;
    /** 
     * Option to check whether the input conforms to the STD3 ASCII rules,
     * for example the restriction of labels to LDH characters
     * (ASCII Letters, Digits and Hyphen-Minus).
     * For use in static worker and factory methods.
     */
    public static final int USE_STD3_RULES = 2;
    /**
     * IDNA option to check for whether the input conforms to the BiDi rules.
     * For use in static worker and factory methods.
     * <p>This option is ignored by the IDNA2003 implementation.
     * (IDNA2003 always performs a BiDi check.)
     */
    public static final int CHECK_BIDI = 4;
    /**
     * IDNA option to check for whether the input conforms to the CONTEXTJ rules.
     * For use in static worker and factory methods.
     * <p>This option is ignored by the IDNA2003 implementation.
     * (The CONTEXTJ check is new in IDNA2008.)
     */
    public static final int CHECK_CONTEXTJ = 8;
    /**
     * IDNA option for nontransitional processing in ToASCII().
     * For use in static worker and factory methods.
     * <p>By default, ToASCII() uses transitional processing.
     * <p>This option is ignored by the IDNA2003 implementation.
     * (This is only relevant for compatibility of newer IDNA implementations with IDNA2003.)
     */
    public static final int NONTRANSITIONAL_TO_ASCII = 0x10;
    /**
     * IDNA option for nontransitional processing in ToUnicode().
     * For use in static worker and factory methods.
     * <p>By default, ToUnicode() uses transitional processing.
     * <p>This option is ignored by the IDNA2003 implementation.
     * (This is only relevant for compatibility of newer IDNA implementations with IDNA2003.)
     */
    public static final int NONTRANSITIONAL_TO_UNICODE = 0x20;
    /**
     * IDNA option to check for whether the input conforms to the CONTEXTO rules.
     * For use in static worker and factory methods.
     * <p>This option is ignored by the IDNA2003 implementation.
     * (The CONTEXTO check is new in IDNA2008.)
     * <p>This is for use by registries for IDNA2008 conformance.
     * UTS #46 does not require the CONTEXTO check.
     */
    public static final int CHECK_CONTEXTO = 0x40;

    /**
     * Returns an IDNA instance which implements UTS #46.
     * Returns an unmodifiable instance, owned by the caller.
     * Cache it for multiple operations, and delete it when done.
     * The instance is thread-safe, that is, it can be used concurrently.
     * <p>
     * UTS #46 defines Unicode IDNA Compatibility Processing,
     * updated to the latest version of Unicode and compatible with both
     * IDNA2003 and IDNA2008.
     * <p>
     * The worker functions use transitional processing, including deviation mappings,
     * unless NONTRANSITIONAL_TO_ASCII or NONTRANSITIONAL_TO_UNICODE
     * is used in which case the deviation characters are passed through without change.
     * <p>
     * Disallowed characters are mapped to U+FFFD.
     * <p>
     * Operations with the UTS #46 instance do not support the
     * ALLOW_UNASSIGNED option.
     * <p>
     * By default, the UTS #46 implementation allows all ASCII characters (as valid or mapped).
     * When the USE_STD3_RULES option is used, ASCII characters other than
     * letters, digits, hyphen (LDH) and dot/full stop are disallowed and mapped to U+FFFD.
     *
     * @param options Bit set to modify the processing and error checking.
     * @return the UTS #46 IDNA instance, if successful
     */
    public static IDNA getUTS46Instance(int options) {
        return new UTS46(options);
    }

    /**
     * Converts a single domain name label into its ASCII form for DNS lookup.
     * If any processing step fails, then info.hasErrors() will be true and
     * the result might not be an ASCII string.
     * The label might be modified according to the types of errors.
     * Labels with severe errors will be left in (or turned into) their Unicode form.
     *
     * @param label Input domain name label
     * @param dest Destination string object
     * @param info Output container of IDNA processing details.
     * @return dest
     */
    public abstract StringBuilder labelToASCII(CharSequence label, StringBuilder dest, Info info);

    /**
     * Converts a single domain name label into its Unicode form for human-readable display.
     * If any processing step fails, then info.hasErrors() will be true.
     * The label might be modified according to the types of errors.
     *
     * @param label Input domain name label
     * @param dest Destination string object
     * @param info Output container of IDNA processing details.
     * @return dest
     */
    public abstract StringBuilder labelToUnicode(CharSequence label, StringBuilder dest, Info info);

    /**
     * Converts a whole domain name into its ASCII form for DNS lookup.
     * If any processing step fails, then info.hasErrors() will be true and
     * the result might not be an ASCII string.
     * The domain name might be modified according to the types of errors.
     * Labels with severe errors will be left in (or turned into) their Unicode form.
     *
     * @param name Input domain name
     * @param dest Destination string object
     * @param info Output container of IDNA processing details.
     * @return dest
     */
    public abstract StringBuilder nameToASCII(CharSequence name, StringBuilder dest, Info info);

    /**
     * Converts a whole domain name into its Unicode form for human-readable display.
     * If any processing step fails, then info.hasErrors() will be true.
     * The domain name might be modified according to the types of errors.
     *
     * @param name Input domain name
     * @param dest Destination string object
     * @param info Output container of IDNA processing details.
     * @return dest
     */
    public abstract StringBuilder nameToUnicode(CharSequence name, StringBuilder dest, Info info);

    /**
     * Output container for IDNA processing errors.
     * The Info class is not suitable for subclassing.
     */
    public static final class Info {
        /**
         * Constructor.
         */
        public Info() {
            errors=EnumSet.noneOf(Error.class);
            labelErrors=EnumSet.noneOf(Error.class);
            isTransDiff=false;
            isBiDi=false;
            isOkBiDi=true;
        }
        /**
         * Were there IDNA processing errors?
         * @return true if there were processing errors
         */
        public boolean hasErrors() { return !errors.isEmpty(); }
        /**
         * Returns a set indicating IDNA processing errors.
         * @return set of processing errors (modifiable, and not null)
         */
        public Set<Error> getErrors() { return errors; }
        /**
         * Returns true if transitional and nontransitional processing produce different results.
         * This is the case when the input label or domain name contains
         * one or more deviation characters outside a Punycode label (see UTS #46).
         * <ul>
         * <li>With nontransitional processing, such characters are
         * copied to the destination string.
         * <li>With transitional processing, such characters are
         * mapped (sharp s/sigma) or removed (joiner/nonjoiner).
         * </ul>
         * @return true if transitional and nontransitional processing produce different results
         */
        public boolean isTransitionalDifferent() { return isTransDiff; }

        private void reset() {
            errors.clear();
            labelErrors.clear();
            isTransDiff=false;
            isBiDi=false;
            isOkBiDi=true;
        }

        private EnumSet<Error> errors, labelErrors;
        private boolean isTransDiff;
        private boolean isBiDi;
        private boolean isOkBiDi;
    }

    // The following protected methods give IDNA subclasses access to the private IDNAInfo fields.
    // The IDNAInfo also provides intermediate state that is publicly invisible,
    // avoiding the allocation of another worker object.
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static void resetInfo(Info info) {
        info.reset();
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static boolean hasCertainErrors(Info info, EnumSet<Error> errors) {
        return !info.errors.isEmpty() && !Collections.disjoint(info.errors, errors);
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static boolean hasCertainLabelErrors(Info info, EnumSet<Error> errors) {
        return !info.labelErrors.isEmpty() && !Collections.disjoint(info.labelErrors, errors);
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static void addLabelError(Info info, Error error) {
        info.labelErrors.add(error);
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static void promoteAndResetLabelErrors(Info info) {
        if(!info.labelErrors.isEmpty()) {
            info.errors.addAll(info.labelErrors);
            info.labelErrors.clear();
        }
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static void addError(Info info, Error error) {
        info.errors.add(error);
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static void setTransitionalDifferent(Info info) {
        info.isTransDiff=true;
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static void setBiDi(Info info) {
        info.isBiDi=true;
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static boolean isBiDi(Info info) {
        return info.isBiDi;
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static void setNotOkBiDi(Info info) {
        info.isOkBiDi=false;
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static boolean isOkBiDi(Info info) {
        return info.isOkBiDi;
    }

    /**
     * IDNA error bit set values.
     * When a domain name or label fails a processing step or does not meet the
     * validity criteria, then one or more of these error bits are set.
     */
    public static enum Error {
        /**
         * A non-final domain name label (or the whole domain name) is empty.
         */
        EMPTY_LABEL,
        /**
         * A domain name label is longer than 63 bytes.
         * (See STD13/RFC1034 3.1. Name space specifications and terminology.)
         * This is only checked in ToASCII operations, and only if the output label is all-ASCII.
         */
        LABEL_TOO_LONG,
        /**
         * A domain name is longer than 255 bytes in its storage form.
         * (See STD13/RFC1034 3.1. Name space specifications and terminology.)
         * This is only checked in ToASCII operations, and only if the output domain name is all-ASCII.
         */
        DOMAIN_NAME_TOO_LONG,
        /**
         * A label starts with a hyphen-minus ('-').
         */
        LEADING_HYPHEN,
        /**
         * A label ends with a hyphen-minus ('-').
         */
        TRAILING_HYPHEN,
        /**
         * A label contains hyphen-minus ('-') in the third and fourth positions.
         */
        HYPHEN_3_4,
        /**
         * A label starts with a combining mark.
         */
        LEADING_COMBINING_MARK,
        /**
         * A label or domain name contains disallowed characters.
         */
        DISALLOWED,
        /**
         * A label starts with "xn--" but does not contain valid Punycode.
         * That is, an xn-- label failed Punycode decoding.
         */
        PUNYCODE,
        /**
         * A label contains a dot=full stop.
         * This can occur in an input string for a single-label function.
         */
        LABEL_HAS_DOT,
        /**
         * An ACE label does not contain a valid label string.
         * The label was successfully ACE (Punycode) decoded but the resulting
         * string had severe validation errors. For example,
         * it might contain characters that are not allowed in ACE labels,
         * or it might not be normalized.
         */
        INVALID_ACE_LABEL,
        /**
         * A label does not meet the IDNA BiDi requirements (for right-to-left characters).
         */
        BIDI,
        /**
         * A label does not meet the IDNA CONTEXTJ requirements.
         */
        CONTEXTJ,
        /**
         * A label does not meet the IDNA CONTEXTO requirements for punctuation characters.
         * Some punctuation characters "Would otherwise have been DISALLOWED"
         * but are allowed in certain contexts. (RFC 5892)
         */
        CONTEXTO_PUNCTUATION,
        /**
         * A label does not meet the IDNA CONTEXTO requirements for digits.
         * Arabic-Indic Digits (U+066x) must not be mixed with Extended Arabic-Indic Digits (U+06Fx).
         */
        CONTEXTO_DIGITS
    }

    /**
     * Sole constructor. (For invocation by subclass constructors, typically implicit.)
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected IDNA() {
    }

    /* IDNA2003 API ------------------------------------------------------------- */

    /**
     * IDNA2003: This function implements the ToASCII operation as defined in the IDNA RFC.
     * This operation is done on <b>single labels</b> before sending it to something that expects
     * ASCII names. A label is an individual part of a domain name. Labels are usually
     * separated by dots; e.g." "www.example.com" is composed of 3 labels 
     * "www","example", and "com".
     *
     * @param src       The input string to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              StringPrepParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @throws StringPrepParseException When an error occurs for parsing a string.
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertToASCII(String src, int options)
        throws StringPrepParseException{
        UCharacterIterator iter = UCharacterIterator.getInstance(src);
        return convertToASCII(iter,options);
    }
    
    /**
     * IDNA2003: This function implements the ToASCII operation as defined in the IDNA RFC.
     * This operation is done on <b>single labels</b> before sending it to something that expects
     * ASCII names. A label is an individual part of a domain name. Labels are usually
     * separated by dots; e.g." "www.example.com" is composed of 3 labels 
     * "www","example", and "com".
     *
     * @param src       The input string as StringBuffer to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertToASCII(StringBuffer src, int options)
        throws StringPrepParseException{
        UCharacterIterator iter = UCharacterIterator.getInstance(src);
        return convertToASCII(iter,options);
    }
    
    /**
     * IDNA2003: This function implements the ToASCII operation as defined in the IDNA RFC.
     * This operation is done on <b>single labels</b> before sending it to something that expects
     * ASCII names. A label is an individual part of a domain name. Labels are usually
     * separated by dots; e.g." "www.example.com" is composed of 3 labels 
     * "www","example", and "com".
     *
     * @param src       The input string as UCharacterIterator to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertToASCII(UCharacterIterator src, int options)
                throws StringPrepParseException{
        return IDNA2003.convertToASCII(src, options);
    }

    /**
     * IDNA2003: Convenience function that implements the IDNToASCII operation as defined in the IDNA RFC.
     * This operation is done on complete domain names, e.g: "www.example.com". 
     * It is important to note that this operation can fail. If it fails, then the input 
     * domain name cannot be used as an Internationalized Domain Name and the application
     * should have methods defined to deal with the failure.
     * 
     * <b>Note:</b> IDNA RFC specifies that a conformant application should divide a domain name
     * into separate labels, decide whether to apply allowUnassigned and useSTD3ASCIIRules on each, 
     * and then convert. This function does not offer that level of granularity. The options once  
     * set will apply to all labels in the domain name
     *
     * @param src       The input string as UCharacterIterator to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertIDNToASCII(UCharacterIterator src, int options)
            throws StringPrepParseException{
        return convertIDNToASCII(src.getText(), options);          
    }
    
    /**
     * IDNA2003: Convenience function that implements the IDNToASCII operation as defined in the IDNA RFC.
     * This operation is done on complete domain names, e.g: "www.example.com". 
     * It is important to note that this operation can fail. If it fails, then the input 
     * domain name cannot be used as an Internationalized Domain Name and the application
     * should have methods defined to deal with the failure.
     * 
     * <b>Note:</b> IDNA RFC specifies that a conformant application should divide a domain name
     * into separate labels, decide whether to apply allowUnassigned and useSTD3ASCIIRules on each, 
     * and then convert. This function does not offer that level of granularity. The options once  
     * set will apply to all labels in the domain name
     *
     * @param src       The input string as a StringBuffer to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertIDNToASCII(StringBuffer src, int options)
            throws StringPrepParseException{
            return convertIDNToASCII(src.toString(), options);          
    }
    
    /**
     * IDNA2003: Convenience function that implements the IDNToASCII operation as defined in the IDNA RFC.
     * This operation is done on complete domain names, e.g: "www.example.com". 
     * It is important to note that this operation can fail. If it fails, then the input 
     * domain name cannot be used as an Internationalized Domain Name and the application
     * should have methods defined to deal with the failure.
     * 
     * <b>Note:</b> IDNA RFC specifies that a conformant application should divide a domain name
     * into separate labels, decide whether to apply allowUnassigned and useSTD3ASCIIRules on each, 
     * and then convert. This function does not offer that level of granularity. The options once  
     * set will apply to all labels in the domain name
     *
     * @param src       The input string to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertIDNToASCII(String src,int options)
            throws StringPrepParseException{
        return IDNA2003.convertIDNToASCII(src, options);
    }

    
    /**
     * IDNA2003: This function implements the ToUnicode operation as defined in the IDNA RFC.
     * This operation is done on <b>single labels</b> before sending it to something that expects
     * Unicode names. A label is an individual part of a domain name. Labels are usually
     * separated by dots; for e.g." "www.example.com" is composed of 3 labels 
     * "www","example", and "com".
     * 
     * @param src       The input string to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertToUnicode(String src, int options)
           throws StringPrepParseException{
        UCharacterIterator iter = UCharacterIterator.getInstance(src);
        return convertToUnicode(iter,options);
    }
    
    /**
     * IDNA2003: This function implements the ToUnicode operation as defined in the IDNA RFC.
     * This operation is done on <b>single labels</b> before sending it to something that expects
     * Unicode names. A label is an individual part of a domain name. Labels are usually
     * separated by dots; for e.g." "www.example.com" is composed of 3 labels 
     * "www","example", and "com".
     * 
     * @param src       The input string as StringBuffer to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertToUnicode(StringBuffer src, int options)
           throws StringPrepParseException{
        UCharacterIterator iter = UCharacterIterator.getInstance(src);
        return convertToUnicode(iter,options);
    }
       
    /**
     * IDNA2003: Function that implements the ToUnicode operation as defined in the IDNA RFC.
     * This operation is done on <b>single labels</b> before sending it to something that expects
     * Unicode names. A label is an individual part of a domain name. Labels are usually
     * separated by dots; for e.g." "www.example.com" is composed of 3 labels 
     * "www","example", and "com".
     * 
     * @param src       The input string as UCharacterIterator to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertToUnicode(UCharacterIterator src, int options)
           throws StringPrepParseException{
        return IDNA2003.convertToUnicode(src, options);
    }
    
    /**
     * IDNA2003: Convenience function that implements the IDNToUnicode operation as defined in the IDNA RFC.
     * This operation is done on complete domain names, e.g: "www.example.com". 
     *
     * <b>Note:</b> IDNA RFC specifies that a conformant application should divide a domain name
     * into separate labels, decide whether to apply allowUnassigned and useSTD3ASCIIRules on each, 
     * and then convert. This function does not offer that level of granularity. The options once  
     * set will apply to all labels in the domain name
     *
     * @param src       The input string as UCharacterIterator to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertIDNToUnicode(UCharacterIterator src, int options)
        throws StringPrepParseException{
        return convertIDNToUnicode(src.getText(), options);
    }
    
    /**
     * IDNA2003: Convenience function that implements the IDNToUnicode operation as defined in the IDNA RFC.
     * This operation is done on complete domain names, e.g: "www.example.com". 
     *
     * <b>Note:</b> IDNA RFC specifies that a conformant application should divide a domain name
     * into separate labels, decide whether to apply allowUnassigned and useSTD3ASCIIRules on each, 
     * and then convert. This function does not offer that level of granularity. The options once  
     * set will apply to all labels in the domain name
     *
     * @param src       The input string as StringBuffer to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertIDNToUnicode(StringBuffer src, int options)
        throws StringPrepParseException{
        return convertIDNToUnicode(src.toString(), options);
    }
    
    /**
     * IDNA2003: Convenience function that implements the IDNToUnicode operation as defined in the IDNA RFC.
     * This operation is done on complete domain names, e.g: "www.example.com". 
     *
     * <b>Note:</b> IDNA RFC specifies that a conformant application should divide a domain name
     * into separate labels, decide whether to apply allowUnassigned and useSTD3ASCIIRules on each, 
     * and then convert. This function does not offer that level of granularity. The options once  
     * set will apply to all labels in the domain name
     *
     * @param src       The input string to be processed
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return StringBuffer the converted String
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static StringBuffer convertIDNToUnicode(String src, int options)
            throws StringPrepParseException{
        return IDNA2003.convertIDNToUnicode(src, options);
    }
    
    /**
     * IDNA2003: Compare two IDN strings for equivalence.
     * This function splits the domain names into labels and compares them.
     * According to IDN RFC, whenever two labels are compared, they are 
     * considered equal if and only if their ASCII forms (obtained by 
     * applying toASCII) match using an case-insensitive ASCII comparison.
     * Two domain names are considered a match if and only if all labels 
     * match regardless of whether label separators match.
     * 
     * @param s1        First IDN string as StringBuffer
     * @param s2        Second IDN string as StringBuffer
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED    Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES      Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return 0 if the strings are equal, &gt; 0 if s1 &gt; s2 and &lt; 0 if s1 &lt; s2
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static int compare(StringBuffer s1, StringBuffer s2, int options)
        throws StringPrepParseException{
        if(s1==null || s2 == null){
            throw new IllegalArgumentException("One of the source buffers is null");
        }
        return IDNA2003.compare(s1.toString(), s2.toString(), options);
    }
    
    /**
     * IDNA2003: Compare two IDN strings for equivalence.
     * This function splits the domain names into labels and compares them.
     * According to IDN RFC, whenever two labels are compared, they are 
     * considered equal if and only if their ASCII forms (obtained by 
     * applying toASCII) match using an case-insensitive ASCII comparison.
     * Two domain names are considered a match if and only if all labels 
     * match regardless of whether label separators match.
     * 
     * @param s1        First IDN string 
     * @param s2        Second IDN string
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED    Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES      Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return 0 if the strings are equal, &gt; 0 if s1 &gt; s2 and &lt; 0 if s1 &lt; s2
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static int compare(String s1, String s2, int options) throws StringPrepParseException{
        if(s1==null || s2 == null){
            throw new IllegalArgumentException("One of the source buffers is null");
        }
        return IDNA2003.compare(s1, s2, options);
    }
    /**
     * IDNA2003: Compare two IDN strings for equivalence.
     * This function splits the domain names into labels and compares them.
     * According to IDN RFC, whenever two labels are compared, they are 
     * considered equal if and only if their ASCII forms (obtained by 
     * applying toASCII) match using an case-insensitive ASCII comparison.
     * Two domain names are considered a match if and only if all labels 
     * match regardless of whether label separators match.
     * 
     * @param s1        First IDN string as UCharacterIterator
     * @param s2        Second IDN string as UCharacterIterator
     * @param options   A bit set of options:
     *  - IDNA.DEFAULT              Use default options, i.e., do not process unassigned code points
     *                              and do not use STD3 ASCII rules
     *                              If unassigned code points are found the operation fails with 
     *                              ParseException.
     *
     *  - IDNA.ALLOW_UNASSIGNED     Unassigned values can be converted to ASCII for query operations
     *                              If this option is set, the unassigned code points are in the input 
     *                              are treated as normal Unicode code points.
     *                          
     *  - IDNA.USE_STD3_RULES       Use STD3 ASCII rules for host name syntax restrictions
     *                              If this option is set and the input does not satisfy STD3 rules,  
     *                              the operation will fail with ParseException
     * @return 0 if the strings are equal, &gt; 0 if i1 &gt; i2 and &lt; 0 if i1 &lt; i2
     * @deprecated ICU 55 Use UTS 46 instead via {@link #getUTS46Instance(int)}.
     * @hide original deprecated declaration
     */
    @Deprecated
    public static int compare(UCharacterIterator s1, UCharacterIterator s2, int options)
        throws StringPrepParseException{
        if(s1==null || s2 == null){
            throw new IllegalArgumentException("One of the source buffers is null");
        }
        return IDNA2003.compare(s1.getText(), s2.getText(), options);
    }
}
