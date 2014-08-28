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

#ifndef _java_lang_RealToString_H_
#define _java_lang_RealToString_H_

#import <Foundation/Foundation.h>

@class JavaLangAbstractStringBuilder;

NSString *RealToString_convertDouble(JavaLangAbstractStringBuilder *sb, double inputNumber);
NSString *RealToString_convertFloat(JavaLangAbstractStringBuilder *sb, float inputNumber);

__attribute__((always_inline)) inline NSString *RealToString_doubleToString(double d) {
  return RealToString_convertDouble(nil, d);
}

__attribute__((always_inline)) inline void RealToString_appendDouble(
    JavaLangAbstractStringBuilder *sb, double d) {
  RealToString_convertDouble(sb, d);
}

__attribute__((always_inline)) inline NSString *RealToString_floatToString(float f) {
  return RealToString_convertFloat(nil, f);
}

__attribute__((always_inline)) inline void RealToString_appendFloat(
    JavaLangAbstractStringBuilder *sb, float f) {
  RealToString_convertFloat(sb, f);
}

#endif // _java_lang_RealToString_H_
