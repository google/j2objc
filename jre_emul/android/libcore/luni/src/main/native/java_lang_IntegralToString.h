/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@class JavaLangAbstractStringBuilder;
@class JavaLangStringBuilder;

NSString *IntegralToString_intToString(int i, int radix);
NSString *IntegralToString_convertInt(JavaLangAbstractStringBuilder *sb, int i);
NSString *IntegralToString_longToString(long long v, int radix);
NSString *IntegralToString_convertLong(JavaLangAbstractStringBuilder *sb, long long n);
NSString *IntegralToString_intToBinaryString(int i);
NSString *IntegralToString_longToBinaryString(long long v);
JavaLangStringBuilder *IntegralToString_appendByteAsHex(
    JavaLangStringBuilder *sb, char b, BOOL upperCase);
NSString *IntegralToString_byteToHexString(char b, BOOL upperCase);
NSString *IntegralToString_bytesToHexString(IOSByteArray *bytes, BOOL upperCase);
NSString *IntegralToString_intToHexString(int i, BOOL upperCase, int minWidth);
NSString *IntegralToString_longToHexString(long long v);
NSString *IntegralToString_intToOctalString(int i);
NSString *IntegralToString_longToOctalString(long long v);
