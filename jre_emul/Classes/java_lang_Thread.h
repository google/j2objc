/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef java_lang_Thread_H
#define java_lang_Thread_H

#import <pthread.h>

@class JavaLangThread;

CF_EXTERN_C_BEGIN

pthread_key_t java_thread_key;
pthread_once_t java_thread_key_init_once;

void initJavaThreadKeyOnce();
JavaLangThread *getCurrentJavaThreadOrNull();

CF_EXTERN_C_END

#endif  // java_lang_Thread_H
