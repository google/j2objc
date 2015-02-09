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

#ifndef _java_lang_IntegralToString_H_
#define _java_lang_IntegralToString_H_

#import <Foundation/Foundation.h>

#include "java/lang/AbstractStringBuilder.h"

CF_EXTERN_C_BEGIN

NSString *IntegralToString_convertInt(JreStringBuilder *sb, int i);
NSString *IntegralToString_convertLong(JreStringBuilder *sb, long long n);

CF_EXTERN_C_END

#endif // _java_lang_IntegralToString_H_
