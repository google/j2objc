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

package java.math;

/**
 * The library implements some logical operations over {@code BigInteger}. The
 * operations provided are listed below.
 * <ul type="circle">
 * <li>not</li>
 * <li>and</li>
 * <li>andNot</li>
 * <li>or</li>
 * <li>xor</li>
 * </ul>
 */
class Logical {

    /** Just to denote that this class can't be instantiated. */
    
    private Logical() {}


    /** @see BigInteger#not() */
    static BigInteger not(BigInteger val) {
        if (val.sign == 0) {
            return BigInteger.MINUS_ONE;
        }
        if (val.equals(BigInteger.MINUS_ONE)) {
            return BigInteger.ZERO;
        }
        int resDigits[] = new int[val.numberLength + 1];
        int i;

        if (val.sign > 0) {
            // ~val = -val + 1
            if (val.digits[val.numberLength - 1] != -1) {
                for (i = 0; val.digits[i] == -1; i++) {
                    ;
                }
            } else {
                for (i = 0; (i < val.numberLength) && (val.digits[i] == -1); i++) {
                    ;
                }
                if (i == val.numberLength) {
                    resDigits[i] = 1;
                    return new BigInteger(-val.sign, i + 1, resDigits);
                }
            }
            // Here a carry 1 was generated
        } else {// (val.sign < 0)
            // ~val = -val - 1
            for (i = 0; val.digits[i] == 0; i++) {
                resDigits[i] = -1;
            }
            // Here a borrow -1 was generated
        }
        // Now, the carry/borrow can be absorbed
        resDigits[i] = val.digits[i] + val.sign;
        // Copying the remaining unchanged digit
        for (i++; i < val.numberLength; i++) {
            resDigits[i] = val.digits[i];
        }
        return new BigInteger(-val.sign, i, resDigits);
    }

    /** @see BigInteger#and(BigInteger) */
    static BigInteger and(BigInteger val, BigInteger that) {
        if (that.sign == 0 || val.sign == 0) {
            return BigInteger.ZERO;
        }
        if (that.equals(BigInteger.MINUS_ONE)){
            return val;
        }
        if (val.equals(BigInteger.MINUS_ONE)) {
            return that;
        }
        
        if (val.sign > 0) {
            if (that.sign > 0) {
                return andPositive(val, that);
            } else {
                return andDiffSigns(val, that);
            }
        } else {
            if (that.sign > 0) {
                return andDiffSigns(that, val);
            } else if (val.numberLength > that.numberLength) {
                return andNegative(val, that);
            } else {
                return andNegative(that, val);
            }
        }
    }
    
    /** @return sign = 1, magnitude = val.magnitude & that.magnitude*/
    static BigInteger andPositive(BigInteger val, BigInteger that) {
        // PRE: both arguments are positive
        int resLength = Math.min(val.numberLength, that.numberLength);
        int i = Math.max(val.getFirstNonzeroDigit(), that.getFirstNonzeroDigit());
        
        if (i >= resLength) {
            return BigInteger.ZERO;
        }
        
        int resDigits[] = new int[resLength];
        for ( ; i < resLength; i++) {
            resDigits[i] = val.digits[i] & that.digits[i];
        }
        
        BigInteger result = new BigInteger(1, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }

    /** @return sign = positive.magnitude & magnitude = -negative.magnitude */
    static BigInteger andDiffSigns(BigInteger positive, BigInteger negative) {
        // PRE: positive is positive and negative is negative
        int iPos = positive.getFirstNonzeroDigit();
        int iNeg = negative.getFirstNonzeroDigit();
        
        // Look if the trailing zeros of the negative will "blank" all
        // the positive digits
        if (iNeg >= positive.numberLength) {
            return BigInteger.ZERO;
        }
        int resLength = positive.numberLength;
        int resDigits[] = new int[resLength];
        
        // Must start from max(iPos, iNeg)
        int i = Math.max(iPos, iNeg);
        if (i == iNeg) {
            resDigits[i] = -negative.digits[i] & positive.digits[i];
            i++;
        }
        int limit = Math.min(negative.numberLength, positive.numberLength);
        for ( ; i < limit; i++) {
            resDigits[i] = ~negative.digits[i] & positive.digits[i];
        }
        // if the negative was shorter must copy the remaining digits
        // from positive
        if (i >= negative.numberLength) {
            for ( ; i < positive.numberLength; i++) {
                resDigits[i] = positive.digits[i];
            }
        } // else positive ended and must "copy" virtual 0's, do nothing then
        
        BigInteger result = new BigInteger(1, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
    
    /** @return sign = -1, magnitude = -(-longer.magnitude & -shorter.magnitude)*/
    static BigInteger andNegative(BigInteger longer, BigInteger shorter) {
        // PRE: longer and shorter are negative
        // PRE: longer has at least as many digits as shorter
        int iLonger = longer.getFirstNonzeroDigit();
        int iShorter = shorter.getFirstNonzeroDigit();
        
        // Does shorter matter?
        if (iLonger >= shorter.numberLength) {
            return longer;
        }
        
        int resLength;
        int resDigits[];
        int i = Math.max(iShorter, iLonger);
        int digit;
        if (iShorter > iLonger) {
            digit = -shorter.digits[i] & ~longer.digits[i];
        } else if (iShorter < iLonger) {
            digit = ~shorter.digits[i] & -longer.digits[i];
        } else {
            digit = -shorter.digits[i] & -longer.digits[i];
        }
        if (digit == 0) {
            for (i++; i < shorter.numberLength && (digit = ~(longer.digits[i] | shorter.digits[i])) == 0; i++)
                ;  // digit = ~longer.digits[i] & ~shorter.digits[i]
            if (digit == 0) {
                // shorter has only the remaining virtual sign bits
                for ( ; i < longer.numberLength && (digit = ~longer.digits[i]) == 0; i++)
                    ;
                if (digit == 0) {
                    resLength = longer.numberLength + 1;
                    resDigits = new int[resLength];
                    resDigits[resLength - 1] = 1;

                    BigInteger result = new BigInteger(-1, resLength, resDigits);
                    return result;
                }
            }
        }
        resLength = longer.numberLength;
                resDigits = new int[resLength];
        resDigits[i] = -digit;
        for (i++; i < shorter.numberLength; i++){
            // resDigits[i] = ~(~longer.digits[i] & ~shorter.digits[i];)
            resDigits[i] = longer.digits[i] | shorter.digits[i];
        }
        // shorter has only the remaining virtual sign bits
        for( ; i < longer.numberLength; i++){
            resDigits[i] = longer.digits[i];
        }
        
        BigInteger result = new BigInteger(-1, resLength, resDigits);
        return result;
    }
    
    /** @see BigInteger#andNot(BigInteger) */
    static BigInteger andNot(BigInteger val, BigInteger that) {
        if (that.sign == 0 ) {
            return val;
        }
        if (val.sign == 0) {
            return BigInteger.ZERO;
        }
        if (val.equals(BigInteger.MINUS_ONE)) {
            return that.not();
        }
        if (that.equals(BigInteger.MINUS_ONE)){
            return BigInteger.ZERO;
        }
        
        //if val == that, return 0
        
       if (val.sign > 0) {
            if (that.sign > 0) {
                return andNotPositive(val, that);
            } else {
                return andNotPositiveNegative(val, that);
                    }
                } else {
            if (that.sign > 0) {
                return andNotNegativePositive(val, that);
            } else  {
                return andNotNegative(val, that);
            }
        }
    }
    
    /** @return sign = 1, magnitude = val.magnitude & ~that.magnitude*/
    static BigInteger andNotPositive(BigInteger val, BigInteger that) {
        // PRE: both arguments are positive
        int resDigits[] = new int[val.numberLength];
        
        int limit = Math.min(val.numberLength, that.numberLength);
        int i;
        for (i = val.getFirstNonzeroDigit(); i < limit; i++) {
            resDigits[i] = val.digits[i] & ~that.digits[i];
        }
        for ( ; i < val.numberLength; i++) {
            resDigits[i] = val.digits[i];
        }
        
        BigInteger result = new BigInteger(1, val.numberLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
    
    /** @return sign = 1, magnitude = positive.magnitude & ~(-negative.magnitude)*/
    static BigInteger andNotPositiveNegative(BigInteger positive, BigInteger negative) {
        // PRE: positive > 0 && negative < 0
        int iNeg = negative.getFirstNonzeroDigit();
        int iPos = positive.getFirstNonzeroDigit();
        
        if (iNeg >= positive.numberLength) {
            return positive;
        }
        
        int resLength = Math.min(positive.numberLength, negative.numberLength);
        int resDigits[] = new int[resLength];
        
        // Always start from first non zero of positive
        int i = iPos;
        for ( ; i < iNeg; i++) {
            // resDigits[i] = positive.digits[i] & -1 (~0)
            resDigits[i] = positive.digits[i];
        }
        if (i == iNeg) {
            resDigits[i] = positive.digits[i] & (negative.digits[i] - 1);
            i++;
        }
        for ( ; i < resLength; i++) {
            // resDigits[i] = positive.digits[i] & ~(~negative.digits[i]);
            resDigits[i] = positive.digits[i] & negative.digits[i];
        }
        
        BigInteger result = new BigInteger(1, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
    
    /** @return sign = -1, magnitude = -(-negative.magnitude & ~positive.magnitude)*/
    static BigInteger andNotNegativePositive(BigInteger negative, BigInteger positive) {
        // PRE: negative < 0 && positive > 0
        int resLength;
        int resDigits[];
        int limit;
        int digit;
        
        int iNeg = negative.getFirstNonzeroDigit();
        int iPos = positive.getFirstNonzeroDigit();
        
        if (iNeg >= positive.numberLength) {
            return negative;
        }
        
        resLength = Math.max(negative.numberLength, positive.numberLength);
        int i = iNeg;
        if (iPos > iNeg) {
            resDigits = new int[resLength];
            limit = Math.min(negative.numberLength, iPos);
            for ( ; i < limit; i++) {
                // 1st case:  resDigits [i] = -(-negative.digits[i] & (~0))
                // otherwise: resDigits[i] = ~(~negative.digits[i] & ~0)  ;
                resDigits[i] = negative.digits[i];
            }
            if (i == negative.numberLength) {
                for (i = iPos; i < positive.numberLength; i++) {
                    // resDigits[i] = ~(~positive.digits[i] & -1);
                    resDigits[i] = positive.digits[i];
                }
            }
        } else {
            digit = -negative.digits[i] & ~positive.digits[i];
            if (digit == 0) {
                limit = Math.min(positive.numberLength, negative.numberLength);
                for (i++; i < limit && (digit = ~(negative.digits[i] | positive.digits[i])) == 0; i++)
                    ; // digit = ~negative.digits[i] & ~positive.digits[i]
                if (digit == 0) {
                    // the shorter has only the remaining virtual sign bits
                    for ( ; i < positive.numberLength && (digit = ~positive.digits[i]) == 0; i++)
                        ; // digit = -1 & ~positive.digits[i]
                    for ( ; i < negative.numberLength && (digit = ~negative.digits[i]) == 0; i++)
                        ; // digit = ~negative.digits[i] & ~0
                    if (digit == 0) {
                        resLength++;
                        resDigits = new int[resLength];
                        resDigits[resLength - 1] = 1;
                        
                        BigInteger result = new BigInteger(-1, resLength, resDigits);
                        return result;
                    }
                }
            }
                        resDigits = new int[resLength];
            resDigits[i] = -digit;
            i++;
                    }
        
        limit = Math.min(positive.numberLength, negative.numberLength);
        for ( ; i < limit; i++) {
            //resDigits[i] = ~(~negative.digits[i] & ~positive.digits[i]);
            resDigits[i] = negative.digits[i] | positive.digits[i];
        }
        // Actually one of the next two cycles will be executed
        for ( ; i < negative.numberLength; i++) {
            resDigits[i] = negative.digits[i];
                }
        for ( ; i < positive.numberLength; i++) {
            resDigits[i] = positive.digits[i];
        }
        
        BigInteger result = new BigInteger(-1, resLength, resDigits);
        return result;
            }
    
    /** @return sign = 1, magnitude = -val.magnitude & ~(-that.magnitude)*/
    static BigInteger andNotNegative(BigInteger val, BigInteger that) {
        // PRE: val < 0 && that < 0
        int iVal = val.getFirstNonzeroDigit();
        int iThat = that.getFirstNonzeroDigit();
        
        if (iVal >= that.numberLength) {
            return BigInteger.ZERO;
        }
        
        int resLength = that.numberLength;
        int resDigits[] = new int[resLength];
        int limit;
        int i = iVal;
        if (iVal < iThat) {
            // resDigits[i] = -val.digits[i] & -1;
            resDigits[i] = -val.digits[i];
            limit = Math.min(val.numberLength, iThat);
            for (i++; i < limit; i++) {
                // resDigits[i] = ~val.digits[i] & -1;
                resDigits[i] = ~val.digits[i];
            }
            if (i == val.numberLength) {
                for ( ; i < iThat; i++) {
                    // resDigits[i] = -1 & -1;
                    resDigits[i] = -1;
                }
                // resDigits[i] = -1 & ~-that.digits[i];
                resDigits[i] = that.digits[i] - 1;
        } else {
                // resDigits[i] = ~val.digits[i] & ~-that.digits[i];
                resDigits[i] = ~val.digits[i] & (that.digits[i] - 1);
            }
        } else if (iThat < iVal ) {
            // resDigits[i] = -val.digits[i] & ~~that.digits[i];
            resDigits[i] = -val.digits[i] & that.digits[i];
        } else {
            // resDigits[i] = -val.digits[i] & ~-that.digits[i];
            resDigits[i] = -val.digits[i] & (that.digits[i] - 1);
            }
        
        limit = Math.min(val.numberLength, that.numberLength);
        for (i++; i < limit; i++) {
            // resDigits[i] = ~val.digits[i] & ~~that.digits[i];
            resDigits[i] = ~val.digits[i] & that.digits[i];
        }
        for ( ; i < that.numberLength; i++) {
            // resDigits[i] = -1 & ~~that.digits[i];
            resDigits[i] = that.digits[i];
        }
        
        BigInteger result = new BigInteger(1, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }

    /** @see BigInteger#or(BigInteger) */
    static BigInteger or(BigInteger val, BigInteger that) {
        if (that.equals(BigInteger.MINUS_ONE) || val.equals(BigInteger.MINUS_ONE)) {
            return BigInteger.MINUS_ONE;
        }
        if (that.sign == 0) {
            return val;
        }
        if (val.sign == 0) {
            return that;
        }

                    if (val.sign > 0) {
            if (that.sign > 0) {
                if (val.numberLength > that.numberLength) {
                    return orPositive(val, that);
                    } else {
                    return orPositive(that, val);
                    }
                } else {
                return orDiffSigns(val, that);
            }
                    } else {
            if (that.sign > 0) {
                return orDiffSigns(that, val);
            } else if (that.getFirstNonzeroDigit() > val.getFirstNonzeroDigit()) {
                return orNegative(that, val);
            } else {
                return orNegative(val, that);
                    }
                }
            }
    
    /** @return sign = 1, magnitude = longer.magnitude | shorter.magnitude*/
    static BigInteger orPositive(BigInteger longer, BigInteger shorter) {
        // PRE: longer and shorter are positive;
        // PRE: longer has at least as many digits as shorter
        int resLength = longer.numberLength;
        int resDigits[] = new int[resLength];
        
        int i = Math.min(longer.getFirstNonzeroDigit(), shorter.getFirstNonzeroDigit());
        for (i = 0; i < shorter.numberLength; i++) {
            resDigits[i] = longer.digits[i] | shorter.digits[i];
        }
        for ( ; i < resLength; i++) {
            resDigits[i] = longer.digits[i];
        }
        
        BigInteger result = new BigInteger(1, resLength, resDigits);
        return result;
    }
    
    /** @return sign = -1, magnitude = -(-val.magnitude | -that.magnitude) */
    static BigInteger orNegative(BigInteger val, BigInteger that){
        // PRE: val and that are negative;
        // PRE: val has at least as many trailing zeros digits as that
        int iThat = that.getFirstNonzeroDigit();
        int iVal = val.getFirstNonzeroDigit();
        int i;
        
        if (iVal >= that.numberLength) {
            return that;
        }else if (iThat >= val.numberLength) {
            return val;
        }
        
        int resLength = Math.min(val.numberLength, that.numberLength);
        int resDigits[] = new int[resLength];
        
        //Looking for the first non-zero digit of the result
        if (iThat == iVal) {
            resDigits[iVal] = -(-val.digits[iVal] | -that.digits[iVal]);
            i = iVal;
        } else {
            for (i = iThat; i < iVal; i++) {
                resDigits[i] = that.digits[i];
            }
            resDigits[i] = that.digits[i] & (val.digits[i] - 1);
        }
        
        for (i++; i < resLength; i++) {
            resDigits[i] = val.digits[i] & that.digits[i];
        }
        
        BigInteger result = new BigInteger(-1, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
    
    /** @return sign = -1, magnitude = -(positive.magnitude | -negative.magnitude) */
    static BigInteger orDiffSigns(BigInteger positive, BigInteger negative){
        // Jumping over the least significant zero bits
        int iNeg = negative.getFirstNonzeroDigit();
        int iPos = positive.getFirstNonzeroDigit();
        int i;
        int limit;
        
        // Look if the trailing zeros of the positive will "copy" all
        // the negative digits
        if (iPos >= negative.numberLength) {
            return negative;
        }
        int resLength = negative.numberLength;
        int resDigits[] = new int[resLength];
        
        if (iNeg < iPos ) {
            // We know for sure that this will
            // be the first non zero digit in the result
            for (i = iNeg; i < iPos; i++) {
            resDigits[i] = negative.digits[i];
            }
        } else if (iPos < iNeg) {
            i = iPos;
            resDigits[i] = -positive.digits[i];
            limit = Math.min(positive.numberLength, iNeg);
            for(i++; i < limit; i++ ) {
                resDigits[i] = ~positive.digits[i];
            }
            if (i != positive.numberLength) {               
                resDigits[i] = ~(-negative.digits[i] | positive.digits[i]);                
            } else{
                  for (; i<iNeg; i++) {
                      resDigits[i] = -1;
                  }
                  // resDigits[i] = ~(-negative.digits[i] | 0);
                  resDigits[i] = negative.digits[i] - 1;
            }
            i++;
        } else {// iNeg == iPos
            // Applying two complement to negative and to result
            i = iPos;
            resDigits[i] = -(-negative.digits[i] | positive.digits[i]);
            i++;
        }
        limit = Math.min(negative.numberLength, positive.numberLength);
        for (; i < limit; i++) {
            // Applying two complement to negative and to result
            // resDigits[i] = ~(~negative.digits[i] | positive.digits[i] );
            resDigits[i] = negative.digits[i] & ~positive.digits[i];
        }
        for( ; i < negative.numberLength; i++) {
            resDigits[i] = negative.digits[i];
        }
        
        BigInteger result = new BigInteger(-1, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }

    /** @see BigInteger#xor(BigInteger) */
    static BigInteger xor(BigInteger val, BigInteger that) {
        if (that.sign == 0) {
            return val;
        }
        if (val.sign == 0) {
            return that;
        }
        if (that.equals(BigInteger.MINUS_ONE)) {
            return val.not();
        }
        if (val.equals(BigInteger.MINUS_ONE)) {
            return that.not();
        }
        
        if (val.sign > 0) {
            if (that.sign > 0) {
                if (val.numberLength > that.numberLength) {
                    return xorPositive(val, that);
                } else {
                    return xorPositive(that, val);
                }
            } else {
                return xorDiffSigns(val, that);
            }
        } else {
            if (that.sign > 0) {
                return xorDiffSigns(that, val);
            } else if (that.getFirstNonzeroDigit() > val.getFirstNonzeroDigit()) {
                return xorNegative(that, val);
            } else {
                return xorNegative(val, that);
            }
        }
    }
    
    /** @return sign = 0, magnitude = longer.magnitude | shorter.magnitude */
    static BigInteger xorPositive(BigInteger longer, BigInteger shorter) {
        // PRE: longer and shorter are positive;
        // PRE: longer has at least as many digits as shorter
        int resLength = longer.numberLength;
        int resDigits[] = new int[resLength];
        int i = Math.min(longer.getFirstNonzeroDigit(), shorter.getFirstNonzeroDigit());
        for ( ; i < shorter.numberLength; i++) {
            resDigits[i] = longer.digits[i] ^ shorter.digits[i];
        }
        for( ; i < longer.numberLength; i++ ){
            resDigits[i] = longer.digits[i];
        }
        
        BigInteger result = new BigInteger(1, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
    
    /** @return sign = 0, magnitude = -val.magnitude ^ -that.magnitude */
    static BigInteger xorNegative(BigInteger val, BigInteger that){
        // PRE: val and that are negative
        // PRE: val has at least as many trailing zero digits as that
        int resLength = Math.max(val.numberLength, that.numberLength);
        int resDigits[] = new int[resLength];
        int iVal = val.getFirstNonzeroDigit();
        int iThat = that.getFirstNonzeroDigit();
        int i = iThat;
        int limit;
        
        
        if (iVal == iThat) {
            resDigits[i] = -val.digits[i] ^ -that.digits[i];
        } else {
            resDigits[i] = -that.digits[i];
            limit = Math.min(that.numberLength, iVal);
            for (i++; i < limit; i++) {
                resDigits[i] = ~that.digits[i];
            }
            // Remains digits in that?
            if (i == that.numberLength) {
                //Jumping over the remaining zero to the first non one
                for ( ;i < iVal; i++) {
                    //resDigits[i] = 0 ^ -1;
                    resDigits[i] = -1;
                }
                //resDigits[i] = -val.digits[i] ^ -1;
                resDigits[i] = val.digits[i] - 1;
            } else {
                resDigits[i] = -val.digits[i] ^ ~that.digits[i];
            }
        }
        
        limit = Math.min(val.numberLength, that.numberLength);
        //Perform ^ between that al val until that ends
        for (i++; i < limit; i++) {
            //resDigits[i] = ~val.digits[i] ^ ~that.digits[i];
            resDigits[i] = val.digits[i] ^ that.digits[i];
        }
        //Perform ^ between val digits and -1 until val ends
        for ( ; i < val.numberLength; i++) {
            //resDigits[i] = ~val.digits[i] ^ -1  ;
            resDigits[i] = val.digits[i] ;
        }
        for ( ; i < that.numberLength; i++) {
            //resDigits[i] = -1 ^ ~that.digits[i] ;
            resDigits[i] = that.digits[i];
        }
        
        BigInteger result = new BigInteger(1, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
    
    /** @return sign = 1, magnitude = -(positive.magnitude ^ -negative.magnitude)*/
    static BigInteger xorDiffSigns(BigInteger positive, BigInteger negative){
        int resLength = Math.max(negative.numberLength, positive.numberLength);
        int resDigits[];
        int iNeg = negative.getFirstNonzeroDigit();
        int iPos = positive.getFirstNonzeroDigit();
            int i;
        int limit;
        
        //The first
        if (iNeg < iPos) {
            resDigits = new int[resLength];
            i = iNeg;
            //resDigits[i] = -(-negative.digits[i]);
            resDigits[i] = negative.digits[i];
            limit = Math.min(negative.numberLength, iPos);
            //Skip the positive digits while they are zeros
            for (i++; i < limit; i++) {
                //resDigits[i] = ~(~negative.digits[i]);
                resDigits[i] = negative.digits[i];
            }
            //if the negative has no more elements, must fill the
            //result with the remaining digits of the positive
            if (i == negative.numberLength) {
                for ( ; i < positive.numberLength; i++) {
                    //resDigits[i] = ~(positive.digits[i] ^ -1) -> ~(~positive.digits[i])
                    resDigits[i] = positive.digits[i];
                }
            }
        } else if (iPos < iNeg) {
            resDigits = new int[resLength];
            i = iPos;
            //Applying two complement to the first non-zero digit of the result
            resDigits[i] = -positive.digits[i];
            limit = Math.min(positive.numberLength, iNeg);
            for (i++; i < limit; i++) {
                //Continue applying two complement the result
                resDigits[i] = ~positive.digits[i];
            }
            //When the first non-zero digit of the negative is reached, must apply
            //two complement (arithmetic negation) to it, and then operate
            if (i == iNeg) {
                resDigits[i] = ~(positive.digits[i] ^ -negative.digits[i]);
                i++;
            } else {
                //if the positive has no more elements must fill the remaining digits with
                //the negative ones
                for ( ; i < iNeg; i++) {
                    // resDigits[i] = ~(0 ^ 0)
                    resDigits[i] = -1;
                }
                for ( ; i < negative.numberLength; i++) {
                    //resDigits[i] = ~(~negative.digits[i] ^ 0)
                    resDigits[i] = negative.digits[i];
                }
            }
                } else {
            int digit;
            //The first non-zero digit of the positive and negative are the same
            i = iNeg;
            digit = positive.digits[i] ^ -negative.digits[i];
            if (digit == 0) {
                limit = Math.min(positive.numberLength, negative.numberLength);
                for (i++; i < limit && (digit = positive.digits[i] ^ ~negative.digits[i]) == 0; i++)
                    ;
                if (digit == 0) {
                    // shorter has only the remaining virtual sign bits
                    for ( ; i < positive.numberLength && (digit = ~positive.digits[i]) == 0; i++)
                        ;
                    for ( ; i < negative.numberLength && (digit = ~negative.digits[i]) == 0; i++)
                        ;
                    if (digit == 0) {
                        resLength = resLength + 1;
                        resDigits = new int[resLength];
                        resDigits[resLength - 1] = 1;
                        
                        BigInteger result = new BigInteger(-1, resLength, resDigits);
                        return result;
                }
            }
        }
            resDigits = new int[resLength];
            resDigits[i] = -digit;
            i++;
        }
        
        limit = Math.min(negative.numberLength, positive.numberLength);
        for ( ; i < limit; i++) {
            resDigits[i] = ~(~negative.digits[i] ^ positive.digits[i]);
        }
        for ( ; i < positive.numberLength; i++) {
            // resDigits[i] = ~(positive.digits[i] ^ -1)
            resDigits[i] = positive.digits[i];
        }
        for ( ; i < negative.numberLength; i++) { 
            // resDigits[i] = ~(0 ^ ~negative.digits[i])
            resDigits[i] = negative.digits[i];
        }
        
        BigInteger result = new BigInteger(-1, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
}
