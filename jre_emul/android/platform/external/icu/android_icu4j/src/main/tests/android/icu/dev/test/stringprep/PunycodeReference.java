/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2007, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/

/*
 * 
Disclaimer and license

    Regarding this entire document or any portion of it (including
    the pseudocode and C code), the author makes no guarantees and
    is not responsible for any damage resulting from its use.  The
    author grants irrevocable permission to anyone to use, modify,
    and distribute it in any way that does not diminish the rights
    of anyone else to use, modify, and distribute it, provided that
    redistributed derivative works do not contain misleading author or
    version information.  Derivative works need not be licensed under
    similar terms.

punycode.c 0.4.0 (2001-Nov-17-Sat)
http://www.cs.berkeley.edu/~amc/idn/
Adam M. Costello
http://www.nicemice.net/amc/
*/

package android.icu.dev.test.stringprep;
import android.icu.text.StringPrepParseException;
import android.icu.text.UCharacterIterator;
import android.icu.text.UTF16;

/**
 * The implementation is direct port of C code in the RFC
 */

public final class PunycodeReference {
    /*** punycode status codes */
    public static final int punycode_success=0;
    public static final int punycode_bad_input=1;   /* Input is invalid.                       */
    public static final int punycode_big_output=2;  /* Output would exceed the space provided. */
    public static final int punycode_overflow =3;    /* Input needs wider integers to process.  */
    
    /*** Bootstring parameters for Punycode ***/
    private static final int base = 36;
    private static final int tmin = 1;
    private static final int tmax = 26;
    private static final int skew = 38;
    private static final int damp = 700;
    private static final int initial_bias = 72;
    private static final int initial_n = 0x80;
    private static final int delimiter = 0x2D;
    
    
//    private static final long UNSIGNED_INT_MASK = 0xffffffffL;
    
    /* basic(cp) tests whether cp is a basic code point: */
    private static boolean basic(int cp){
        return (char)(cp) < 0x80;
    }

    /* delim(cp) tests whether cp is a delimiter: */
    private static boolean delim(int cp){
        return ((cp) == delimiter);
    }

    /* decode_digit(cp) returns the numeric value of a basic code */
    /* point (for use in representing integers) in the range 0 to */
    /* base-1, or base if cp is does not represent a value.       */

    private static int decode_digit(int cp)
    {
      return  cp - 48 < 10 ? cp - 22 :  cp - 65 < 26 ? cp - 65 :
              cp - 97 < 26 ? cp - 97 :  base;
    }

    /* encode_digit(d,flag) returns the basic code point whose value      */
    /* (when used for representing integers) is d, which needs to be in   */
    /* the range 0 to base-1.  The lowercase form is used unless flag is  */
    /* nonzero, in which case the uppercase form is used.  The behavior   */
    /* is undefined if flag is nonzero and digit d has no uppercase form. */

    private static char encode_digit(int d, int flag)
    {
      return (char) (d + 22 + (75 * ((d < 26) ? 1 : 0) - (((flag != 0) ? 1 :0) << 5)));
      /*  0..25 map to ASCII a..z or A..Z */
      /* 26..35 map to ASCII 0..9         */
    }

    /* flagged(bcp) tests whether a basic code point is flagged */
    /* (uppercase).  The behavior is undefined if bcp is not a  */
    /* basic code point.                                        */

    private static boolean flagged(int bcp){
         return ((bcp) - 65 < 26);
    }

    /* encode_basic(bcp,flag) forces a basic code point to lowercase */
    /* if flag is zero, uppercase if flag is nonzero, and returns    */
    /* the resulting code point.  The code point is unchanged if it  */
    /* is caseless.  The behavior is undefined if bcp is not a basic */
    /* code point.                                                   */

    private static char encode_basic(int bcp, int flag)
    {
      bcp -= (((bcp - 97) < 26) ? 1 :0 ) << 5;
      boolean mybcp = (bcp - 65 < 26);
      return (char) (bcp + (((flag==0) && mybcp ) ? 1 : 0 ) << 5);
    }

    /*** Platform-specific constants ***/

    /* maxint is the maximum value of a punycode_uint variable: */
    private static long maxint = 0xFFFFFFFFL;
    /* Because maxint is unsigned, -1 becomes the maximum value. */

    /*** Bias adaptation function ***/

    private static int adapt(int delta, int numpoints, boolean firsttime ){
      int k;

      delta = (firsttime==true) ? delta / damp : delta >> 1;
      /* delta >> 1 is a faster way of doing delta / 2 */
      delta += delta / numpoints;

      for (k = 0;  delta > ((base - tmin) * tmax) / 2;  k += base) {
        delta /= base - tmin;
      }

      return k + (base - tmin + 1) * delta / (delta + skew);
    }

    /*** Main encode function ***/

    public static final int encode(   int input_length,
                                      int input[],
                                      char[] case_flags,
                                      int[] output_length,
                                      char output[] ){
      int delta, h, b, out, max_out, bias, j, q, k, t;
      long m,n;
      /* Initialize the state: */

      n = initial_n;
      delta = out = 0;
      max_out = output_length[0];
      bias = initial_bias;

      /* Handle the basic code points: */

      for (j = 0;  j < input_length;  ++j) {
        if (basic(input[j])) {
          if (max_out - out < 2) return punycode_big_output;
          output[out++] = (char)
            (case_flags!=null ?  encode_basic(input[j], case_flags[j]) : input[j]);
        }
        /* else if (input[j] < n) return punycode_bad_input; */
        /* (not needed for Punycode with unsigned code points) */
      }

      h = b = out;

      /* h is the number of code points that have been handled, b is the  */
      /* number of basic code points, and out is the number of characters */
      /* that have been output.                                           */

      if (b > 0) output[out++] = delimiter;

      /* Main encoding loop: */

      while (h < input_length) {
        /* All non-basic code points < n have been     */
        /* handled already.  Find the next larger one: */

        for (m = maxint, j = 0;  j < input_length;  ++j) {
          /* if (basic(input[j])) continue; */
          /* (not needed for Punycode) */
          if (input[j] >= n && input[j] < m) m = input[j];
        }

        /* Increase delta enough to advance the decoder's    */
        /* <n,i> state to <m,0>, but guard against overflow: */

        if (m - n > (maxint - delta) / (h + 1)) return punycode_overflow;
        delta += (m - n) * (h + 1);
        n = m;

        for (j = 0;  j < input_length;  ++j) {
          /* Punycode does not need to check whether input[j] is basic: */
          if (input[j] < n /* || basic(input[j]) */ ) {
            if (++delta == 0) return punycode_overflow;
          }

          if (input[j] == n) {
            /* Represent delta as a generalized variable-length integer: */

            for (q = delta, k = base;  ;  k += base) {
              if (out >= max_out) return punycode_big_output;
              t = k <= bias /* + tmin */ ? tmin :     /* +tmin not needed */
                  k >= bias + tmax ? tmax : k - bias;
              if (q < t) break;
              output[out++] = encode_digit(t + (q - t) % (base - t), 0);
              q = (q - t) / (base - t);
            }

            output[out++] = encode_digit(q, (case_flags !=null) ? case_flags[j] : 0);
            bias = adapt(delta, h + 1, (h == b));
            delta = 0;
            ++h;
          }
        }

        ++delta;
        ++n;
      }

      output_length[0] = out;
      return punycode_success;
    }
    
    public static final StringBuffer encode(StringBuffer input,char[] case_flags)
                               throws StringPrepParseException{
        int[] in = new int[input.length()];
        int inLen = 0;
        int ch;
        StringBuffer result = new StringBuffer();
        UCharacterIterator iter = UCharacterIterator.getInstance(input);
        while((ch=iter.nextCodePoint())!= UCharacterIterator.DONE){
            in[inLen++]=ch;
        }

        int[] outLen =  new int[1];
        outLen[0] = input.length()*4;
        char[] output = new char[outLen[0]];
        int rc = punycode_success;
        for(;;){
            rc = encode(inLen,in,case_flags, outLen, output);
            if(rc==punycode_big_output){
                outLen[0] = outLen[0]*4;
                output = new char[outLen[0]];
                // continue to convert
                continue;
            }
            break;
        }
        if(rc==punycode_success){
            return result.append(output,0,outLen[0]);
        }
        getException(rc);
        return result;
    }

    private static void getException(int rc) 
                   throws StringPrepParseException{
         switch(rc){
             case punycode_big_output:
                throw new StringPrepParseException("The output capacity was not sufficient.",StringPrepParseException.BUFFER_OVERFLOW_ERROR);
             case punycode_bad_input:
                throw new StringPrepParseException("Illegal char found in the input",StringPrepParseException.ILLEGAL_CHAR_FOUND);
             case punycode_overflow:
                throw new StringPrepParseException("Invalid char found in the input",StringPrepParseException.INVALID_CHAR_FOUND);   
         }
        
    }
    private static final int MAX_BUFFER_SIZE = 100;
    
    public static final StringBuffer decode(StringBuffer input,char[] case_flags)
                               throws StringPrepParseException{
        char[] in = input.toString().toCharArray();
        int[] outLen = new int[1];
        outLen[0] = MAX_BUFFER_SIZE;
        int[] output = new int[outLen[0]];
        int rc = punycode_success;
        StringBuffer result = new StringBuffer();
        for(;;){
            rc = decode(input.length(),in, outLen, output,case_flags);
            if(rc==punycode_big_output){
                outLen[0] = output.length * 4;
                output = new int[outLen[0]];
                continue;
            }
            break;
        }
        if(rc==punycode_success){
            for(int i=0; i < outLen[0]; i++ ){
                UTF16.append(result,output[i]);
            }
        }else{
            getException(rc);
        }
        return result;
    }
    
    /*** Main decode function ***/
    public static final int decode(int input_length,
                             char[] input,
                             int[] output_length,
                             int[] output,
                             char[] case_flags ){
      int n, out, i, max_out, bias,
                     b, j, in, oldi, w, k, digit, t;

      /* Initialize the state: */

      n = initial_n;
      out = i = 0;
      max_out = output_length[0];
      bias = initial_bias;

      /* Handle the basic code points:  Let b be the number of input code */
      /* points before the last delimiter, or 0 if there is none, then    */
      /* copy the first b code points to the output.                      */

      for (b = j = 0;  j < input_length;  ++j){
           if (delim(input[j])==true){
                b = j;
           }
      }
      if (b > max_out) return punycode_big_output;

      for (j = 0;  j < b;  ++j) {
        if (case_flags != null) case_flags[out] = (char)(flagged(input[j]) ? 1 : 0);
        if (!basic(input[j])) return punycode_bad_input;
        output[out++] = input[j];
      }

      /* Main decoding loop:  Start just after the last delimiter if any  */
      /* basic code points were copied; start at the beginning otherwise. */

      for (in = b > 0 ? b + 1 : 0;  in < input_length;  ++out) {

        /* in is the index of the next character to be consumed, and */
        /* out is the number of code points in the output array.     */

        /* Decode a generalized variable-length integer into delta,  */
        /* which gets added to i.  The overflow checking is easier   */
        /* if we increase i as we go, then subtract off its starting */
        /* value at the end to obtain delta.                         */

        for (oldi = i, w = 1, k = base;  ;  k += base) {
          if (in >= input_length) return punycode_bad_input;
          digit = decode_digit(input[in++]);
          if (digit >= base) return punycode_bad_input;
          if (digit > (maxint - i) / w) return punycode_overflow;
          i += digit * w;
          t = (k <= bias) /* + tmin */ ? tmin :     /* +tmin not needed */
              (k >= (bias + tmax)) ? tmax : k - bias;
          if (digit < t) break;
          if (w > maxint / (base - t)) return punycode_overflow;
          w *= (base - t);
        }

        bias = adapt(i - oldi, out + 1, (oldi == 0));

        /* i was supposed to wrap around from out+1 to 0,   */
        /* incrementing n each time, so we'll fix that now: */

        if (i / (out + 1) > maxint - n) return punycode_overflow;
        n += i / (out + 1);
        i %= (out + 1);

        /* Insert n at position i of the output: */

        /* not needed for Punycode: */
        /* if (decode_digit(n) <= base) return punycode_invalid_input; */
        if (out >= max_out) return punycode_big_output;

        if (case_flags != null) {
          System.arraycopy(case_flags, i, case_flags,  i + 1, out - i);
          /* Case of last character determines uppercase flag: */
          case_flags[i] = (char)(flagged(input[in - 1]) ? 0 :1);
        }

        System.arraycopy(output, i, output, i + 1,  (out - i));
        output[i++] = n;
      }

      output_length[0] = out;
      return punycode_success;
    }

}
