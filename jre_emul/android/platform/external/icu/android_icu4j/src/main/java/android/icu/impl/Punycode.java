/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.StringPrepParseException;
import android.icu.text.UTF16;

/**
 * Ported code from ICU punycode.c 
 * @author ram
 * @hide Only a subset of ICU is exposed in Android
 */
public final class Punycode {

    /* Punycode parameters for Bootstring */
    private static final int BASE           = 36;
    private static final int TMIN           = 1;
    private static final int TMAX           = 26;
    private static final int SKEW           = 38;
    private static final int DAMP           = 700;
    private static final int INITIAL_BIAS   = 72;
    private static final int INITIAL_N      = 0x80;
    
    /* "Basic" Unicode/ASCII code points */
    private static final char HYPHEN        = 0x2d;
    private static final char DELIMITER     = HYPHEN;
    
    private static final int ZERO           = 0x30;
    //private static final int NINE           = 0x39;
    
    private static final int SMALL_A        = 0x61;
    private static final int SMALL_Z        = 0x7a;
    
    private static final int CAPITAL_A      = 0x41;
    private static final int CAPITAL_Z      = 0x5a;

    private static int adaptBias(int delta, int length, boolean firstTime){
        if(firstTime){
            delta /=DAMP;
        }else{
            delta /=  2;
        }
        delta += delta/length;

        int count=0;
        for(; delta>((BASE-TMIN)*TMAX)/2; count+=BASE) {
            delta/=(BASE-TMIN);
        }

        return count+(((BASE-TMIN+1)*delta)/(delta+SKEW));         
    }

    /**
     * basicToDigit[] contains the numeric value of a basic code
     * point (for use in representing integers) in the range 0 to
     * BASE-1, or -1 if b is does not represent a value.
     */
    static final int[]    basicToDigit= new int[]{
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1, -1,
    
        -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
    
        -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
    
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    ///CLOVER:OFF
    private static char asciiCaseMap(char b, boolean uppercase) {
        if(uppercase) {
            if(SMALL_A<=b && b<=SMALL_Z) {
                b-=(SMALL_A-CAPITAL_A);
            }
        } else {
            if(CAPITAL_A<=b && b<=CAPITAL_Z) {
                b+=(SMALL_A-CAPITAL_A);
            }
        }
        return b;
    }    
    ///CLOVER:ON
    /**
     * digitToBasic() returns the basic code point whose value
     * (when used for representing integers) is d, which must be in the
     * range 0 to BASE-1. The lowercase form is used unless the uppercase flag is
     * nonzero, in which case the uppercase form is used.
     */
    private static char digitToBasic(int digit, boolean uppercase) {
        /*  0..25 map to ASCII a..z or A..Z */
        /* 26..35 map to ASCII 0..9         */
        if(digit<26) {
            if(uppercase) {
                return (char)(CAPITAL_A+digit);
            } else {
                return (char)(SMALL_A+digit);
            }
        } else {
            return (char)((ZERO-26)+digit);
        }
    }
    /**
     * Converts Unicode to Punycode.
     * The input string must not contain single, unpaired surrogates.
     * The output will be represented as an array of ASCII code points.
     * 
     * @param src The source of the String Buffer passed.
     * @param caseFlags The boolean array of case flags.
     * @return An array of ASCII code points.
     */
    public static StringBuilder encode(CharSequence src, boolean[] caseFlags) throws StringPrepParseException{
        int n, delta, handledCPCount, basicLength, bias, j, m, q, k, t, srcCPCount;
        char c, c2;
        int srcLength = src.length();
        int[] cpBuffer = new int[srcLength];
        StringBuilder dest = new StringBuilder(srcLength);
        /*
         * Handle the basic code points and
         * convert extended ones to UTF-32 in cpBuffer (caseFlag in sign bit):
         */
        srcCPCount=0;
        
        for(j=0; j<srcLength; ++j) {
            c=src.charAt(j);
            if(isBasic(c)) {
                cpBuffer[srcCPCount++]=0;
                dest.append(caseFlags!=null ? asciiCaseMap(c, caseFlags[j]) : c);
            } else {
                n=((caseFlags!=null && caseFlags[j])? 1 : 0)<<31L;
                if(!UTF16.isSurrogate(c)) {
                    n|=c;
                } else if(UTF16.isLeadSurrogate(c) && (j+1)<srcLength && UTF16.isTrailSurrogate(c2=src.charAt(j+1))) {
                    ++j;
                    
                    n|=UCharacter.getCodePoint(c, c2);
                } else {
                    /* error: unmatched surrogate */
                    throw new StringPrepParseException("Illegal char found",StringPrepParseException.ILLEGAL_CHAR_FOUND);
                }
                cpBuffer[srcCPCount++]=n;
            }
        }

        /* Finish the basic string - if it is not empty - with a delimiter. */
        basicLength=dest.length();
        if(basicLength>0) {
            dest.append(DELIMITER);
        }

        /*
         * handledCPCount is the number of code points that have been handled
         * basicLength is the number of basic code points
         * destLength is the number of chars that have been output
         */

        /* Initialize the state: */
        n=INITIAL_N;
        delta=0;
        bias=INITIAL_BIAS;

        /* Main encoding loop: */
        for(handledCPCount=basicLength; handledCPCount<srcCPCount; /* no op */) {
            /*
             * All non-basic code points < n have been handled already.
             * Find the next larger one:
             */
            for(m=0x7fffffff, j=0; j<srcCPCount; ++j) {
                q=cpBuffer[j]&0x7fffffff; /* remove case flag from the sign bit */
                if(n<=q && q<m) {
                    m=q;
                }
            }

            /*
             * Increase delta enough to advance the decoder's
             * <n,i> state to <m,0>, but guard against overflow:
             */
            if(m-n>(0x7fffffff-delta)/(handledCPCount+1)) {
                throw new IllegalStateException("Internal program error");
            }
            delta+=(m-n)*(handledCPCount+1);
            n=m;

            /* Encode a sequence of same code points n */
            for(j=0; j<srcCPCount; ++j) {
                q=cpBuffer[j]&0x7fffffff; /* remove case flag from the sign bit */
                if(q<n) {
                    ++delta;
                } else if(q==n) {
                    /* Represent delta as a generalized variable-length integer: */
                    for(q=delta, k=BASE; /* no condition */; k+=BASE) {

                        /** RAM: comment out the old code for conformance with draft-ietf-idn-punycode-03.txt   

                        t=k-bias;
                        if(t<TMIN) {
                            t=TMIN;
                        } else if(t>TMAX) {
                            t=TMAX;
                        }
                        */
                    
                        t=k-bias;
                        if(t<TMIN) {
                            t=TMIN;
                        } else if(k>=(bias+TMAX)) {
                            t=TMAX;
                        }

                        if(q<t) {
                            break;
                        }

                        dest.append(digitToBasic(t+(q-t)%(BASE-t), false));
                        q=(q-t)/(BASE-t);
                    }

                    dest.append(digitToBasic(q, (cpBuffer[j]<0)));
                    bias=adaptBias(delta, handledCPCount+1,(handledCPCount==basicLength));
                    delta=0;
                    ++handledCPCount;
                }
            }

            ++delta;
            ++n;
        }

        return dest;
    }
    
    private static boolean isBasic(int ch){
        return (ch < INITIAL_N);
    }
    ///CLOVER:OFF
    private static boolean isBasicUpperCase(int ch){
        return( CAPITAL_A<=ch && ch >= CAPITAL_Z);
    }
    ///CLOVER:ON
    private static boolean isSurrogate(int ch){
        return (((ch)&0xfffff800)==0xd800);
    }
    /**
     * Converts Punycode to Unicode.
     * The Unicode string will be at most as long as the Punycode string.
     * 
     * @param src The source of the string buffer being passed.
     * @param caseFlags The array of boolean case flags.
     * @return StringBuilder string.
     */
    public static StringBuilder decode(CharSequence src, boolean[] caseFlags) 
                               throws StringPrepParseException{
        int srcLength = src.length();
        StringBuilder dest = new StringBuilder(src.length());
        int n, i, bias, basicLength, j, in, oldi, w, k, digit, t,
                destCPCount, firstSupplementaryIndex, cpLength;
        char b;

        /*
         * Handle the basic code points:
         * Let basicLength be the number of input code points
         * before the last delimiter, or 0 if there is none,
         * then copy the first basicLength code points to the output.
         *
         * The following loop iterates backward.
         */
        for(j=srcLength; j>0;) {
            if(src.charAt(--j)==DELIMITER) {
                break;
            }
        }
        basicLength=destCPCount=j;

        for(j=0; j<basicLength; ++j) {
            b=src.charAt(j);
            if(!isBasic(b)) {
                throw new StringPrepParseException("Illegal char found", StringPrepParseException.INVALID_CHAR_FOUND);
            }
            dest.append(b);

            if(caseFlags!=null && j<caseFlags.length) {
                caseFlags[j]=isBasicUpperCase(b);
            }
        }

        /* Initialize the state: */
        n=INITIAL_N;
        i=0;
        bias=INITIAL_BIAS;
        firstSupplementaryIndex=1000000000;

        /*
         * Main decoding loop:
         * Start just after the last delimiter if any
         * basic code points were copied; start at the beginning otherwise.
         */
        for(in=basicLength>0 ? basicLength+1 : 0; in<srcLength; /* no op */) {
            /*
             * in is the index of the next character to be consumed, and
             * destCPCount is the number of code points in the output array.
             *
             * Decode a generalized variable-length integer into delta,
             * which gets added to i.  The overflow checking is easier
             * if we increase i as we go, then subtract off its starting
             * value at the end to obtain delta.
             */
            for(oldi=i, w=1, k=BASE; /* no condition */; k+=BASE) {
                if(in>=srcLength) {
                    throw new StringPrepParseException("Illegal char found", StringPrepParseException.ILLEGAL_CHAR_FOUND);
                }

                digit=basicToDigit[src.charAt(in++) & 0xFF];
                if(digit<0) {
                    throw new StringPrepParseException("Invalid char found", StringPrepParseException.INVALID_CHAR_FOUND);
                }
                if(digit>(0x7fffffff-i)/w) {
                    /* integer overflow */
                    throw new StringPrepParseException("Illegal char found", StringPrepParseException.ILLEGAL_CHAR_FOUND);
                }

                i+=digit*w;
                t=k-bias;
                if(t<TMIN) {
                    t=TMIN;
                } else if(k>=(bias+TMAX)) {
                    t=TMAX;
                }
                if(digit<t) {
                    break;
                }

                if(w>0x7fffffff/(BASE-t)) {
                    /* integer overflow */
                    throw new StringPrepParseException("Illegal char found", StringPrepParseException.ILLEGAL_CHAR_FOUND);
                }
                w*=BASE-t;
            }

            /*
             * Modification from sample code:
             * Increments destCPCount here,
             * where needed instead of in for() loop tail.
             */
            ++destCPCount;
            bias=adaptBias(i-oldi, destCPCount, (oldi==0));

            /*
             * i was supposed to wrap around from (incremented) destCPCount to 0,
             * incrementing n each time, so we'll fix that now:
             */
            if(i/destCPCount>(0x7fffffff-n)) {
                /* integer overflow */
                throw new StringPrepParseException("Illegal char found", StringPrepParseException.ILLEGAL_CHAR_FOUND);
            }

            n+=i/destCPCount;
            i%=destCPCount;
            /* not needed for Punycode: */
            /* if (decode_digit(n) <= BASE) return punycode_invalid_input; */

            if(n>0x10ffff || isSurrogate(n)) {
                /* Unicode code point overflow */
                throw new StringPrepParseException("Illegal char found", StringPrepParseException.ILLEGAL_CHAR_FOUND);
            }

            /* Insert n at position i of the output: */
            cpLength=Character.charCount(n);
            int codeUnitIndex;

            /*
             * Handle indexes when supplementary code points are present.
             *
             * In almost all cases, there will be only BMP code points before i
             * and even in the entire string.
             * This is handled with the same efficiency as with UTF-32.
             *
             * Only the rare cases with supplementary code points are handled
             * more slowly - but not too bad since this is an insertion anyway.
             */
            if(i<=firstSupplementaryIndex) {
                codeUnitIndex=i;
                if(cpLength>1) {
                    firstSupplementaryIndex=codeUnitIndex;
                } else {
                    ++firstSupplementaryIndex;
                }
            } else {
                codeUnitIndex=dest.offsetByCodePoints(firstSupplementaryIndex, i-firstSupplementaryIndex);
            }

            /* use the UChar index codeUnitIndex instead of the code point index i */
            if(caseFlags!=null && (dest.length()+cpLength)<=caseFlags.length) {
                if(codeUnitIndex<dest.length()) {
                    System.arraycopy(caseFlags, codeUnitIndex,
                                     caseFlags, codeUnitIndex+cpLength,
                                     dest.length()-codeUnitIndex);
                }
                /* Case of last character determines uppercase flag: */
                caseFlags[codeUnitIndex]=isBasicUpperCase(src.charAt(in-1));
                if(cpLength==2) {
                    caseFlags[codeUnitIndex+1]=false;
                }
            }
            if(cpLength==1) {
                /* BMP, insert one code unit */
                dest.insert(codeUnitIndex, (char)n);
            } else {
                /* supplementary character, insert two code units */
                dest.insert(codeUnitIndex, UTF16.getLeadSurrogate(n));
                dest.insert(codeUnitIndex+1, UTF16.getTrailSurrogate(n));
            }
            ++i;
        }
        return dest;
    }
}
