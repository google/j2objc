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

#if !defined(cbigint_h)
#define cbigint_h

#include <sys/types.h>
#include <sys/param.h>
#include <stdint.h>

/* IEEE floats consist of: sign bit, exponent field, significand field
    single:  31 = sign bit, 30..23 = exponent (8 bits), 22..0 = significand (23 bits)
    double:  63 = sign bit, 62..52 = exponent (11 bits), 51..0 = significand (52 bits)
    inf                ==    (all exponent bits set) and (all mantissa bits clear)
    nan                ==    (all exponent bits set) and (at least one mantissa bit set)
    finite             ==    (at least one exponent bit clear)
    zero               ==    (all exponent bits clear) and (all mantissa bits clear)
    denormal           ==    (all exponent bits clear) and (at least one mantissa bit set)
    positive           ==    sign bit clear
    negative           ==    sign bit set
*/
#if __BYTE_ORDER == __LITTLE_ENDIAN
#define DOUBLE_LO_OFFSET        0
#define DOUBLE_HI_OFFSET        1
#define LONG_LO_OFFSET          0
#define LONG_HI_OFFSET          1
#else
#define DOUBLE_LO_OFFSET        1
#define DOUBLE_HI_OFFSET        0
#define LONG_LO_OFFSET          1
#define LONG_HI_OFFSET          0
#endif

#define DOUBLE_EXPONENT_MASK_HI 0x7FF00000
#define DOUBLE_MANTISSA_MASK_HI 0x000FFFFF

typedef union U64U32DBL {
    uint64_t    u64val;
    uint32_t    u32val[2];
    int32_t    i32val[2];
    double  dval;
} U64U32DBL;

#define DOUBLE_TO_LONGBITS(dbl) (*((uint64_t *)&dbl))
#define FLOAT_TO_INTBITS(flt) (*((uint32_t *)&flt))
#define INTBITS_TO_FLOAT(bits) (*((float *)&bits))

/* Replace P_FLOAT_HI and P_FLOAT_LOW */
/* These macros are used to access the high and low 32-bit parts of a double (64-bit) value. */
#define LOW_U32_FROM_DBL_PTR(dblptr) (((U64U32DBL *)(dblptr))->u32val[DOUBLE_LO_OFFSET])
#define HIGH_U32_FROM_DBL_PTR(dblptr) (((U64U32DBL *)(dblptr))->u32val[DOUBLE_HI_OFFSET])
#define LOW_I32_FROM_DBL_PTR(dblptr) (((U64U32DBL *)(dblptr))->i32val[DOUBLE_LO_OFFSET])
#define HIGH_I32_FROM_DBL_PTR(dblptr) (((U64U32DBL *)(dblptr))->i32val[DOUBLE_HI_OFFSET])
#define LOW_U32_FROM_DBL(dbl) LOW_U32_FROM_DBL_PTR(&(dbl))
#define HIGH_U32_FROM_DBL(dbl) HIGH_U32_FROM_DBL_PTR(&(dbl))
#define LOW_U32_FROM_LONG64_PTR(long64ptr) (((U64U32DBL *)(long64ptr))->u32val[LONG_LO_OFFSET])
#define HIGH_U32_FROM_LONG64_PTR(long64ptr) (((U64U32DBL *)(long64ptr))->u32val[LONG_HI_OFFSET])
#define LOW_I32_FROM_LONG64_PTR(long64ptr) (((U64U32DBL *)(long64ptr))->i32val[LONG_LO_OFFSET])
#define HIGH_I32_FROM_LONG64_PTR(long64ptr) (((U64U32DBL *)(long64ptr))->i32val[LONG_HI_OFFSET])
#define LOW_U32_FROM_LONG64(long64) LOW_U32_FROM_LONG64_PTR(&(long64))
#define HIGH_U32_FROM_LONG64(long64) HIGH_U32_FROM_LONG64_PTR(&(long64))
#define LOW_I32_FROM_LONG64(long64) LOW_I32_FROM_LONG64_PTR(&(long64))
#define HIGH_I32_FROM_LONG64(long64) HIGH_I32_FROM_LONG64_PTR(&(long64))
#define IS_DENORMAL_DBL_PTR(dblptr) (((HIGH_U32_FROM_DBL_PTR(dblptr) & DOUBLE_EXPONENT_MASK_HI) == 0) && ((HIGH_U32_FROM_DBL_PTR(dblptr) & DOUBLE_MANTISSA_MASK_HI) != 0 || (LOW_U32_FROM_DBL_PTR(dblptr) != 0)))
#define IS_DENORMAL_DBL(dbl) IS_DENORMAL_DBL_PTR(&(dbl))

#define LOW_U32_FROM_VAR(u64)     LOW_U32_FROM_LONG64(u64)
#define LOW_U32_FROM_PTR(u64ptr)  LOW_U32_FROM_LONG64_PTR(u64ptr)
#define HIGH_U32_FROM_VAR(u64)    HIGH_U32_FROM_LONG64(u64)
#define HIGH_U32_FROM_PTR(u64ptr) HIGH_U32_FROM_LONG64_PTR(u64ptr)

void multiplyHighPrecision(uint64_t* arg1, int32_t length1, uint64_t* arg2, int32_t length2,
        uint64_t* result, int32_t length);
uint32_t simpleAppendDecimalDigitHighPrecision(uint64_t* arg1, int32_t length, uint64_t digit);
double toDoubleHighPrecision(uint64_t* arg, int32_t length);
uint64_t doubleMantissa(double z);
int32_t compareHighPrecision(uint64_t* arg1, int32_t length1, uint64_t* arg2, int32_t length2);
int32_t highestSetBitHighPrecision(uint64_t* arg, int32_t length);
void subtractHighPrecision(uint64_t* arg1, int32_t length1, uint64_t* arg2, int32_t length2);
int32_t doubleExponent(double z);
int32_t addHighPrecision(uint64_t* arg1, int32_t length1, uint64_t* arg2, int32_t length2);
int32_t lowestSetBit(uint64_t* y);
int32_t timesTenToTheEHighPrecision(uint64_t* result, int32_t length, int e);
int32_t highestSetBit(uint64_t* y);
int32_t lowestSetBitHighPrecision(uint64_t* arg, int32_t length);
void simpleShiftLeftHighPrecision(uint64_t* arg1, int32_t length, int32_t arg2);
uint32_t floatMantissa(float z);
int32_t simpleAddHighPrecision(uint64_t* arg1, int32_t length, uint64_t arg2);
int32_t floatExponent(float z);

#endif                          /* cbigint_h */
