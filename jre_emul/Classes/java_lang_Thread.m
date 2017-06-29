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

#import "java/lang/AssertionError.h"
#import "java/lang/Thread.h"
#import "java_lang_Thread.h"
#import <pthread.h>

pthread_key_t java_thread_key;
pthread_once_t java_thread_key_init_once = PTHREAD_ONCE_INIT;

static void javaThreadDestructor(void *javaThread) {
  JavaLangThread *thread = (JavaLangThread *)javaThread;
  [thread exit];
  [thread release];
}

static void createJavaThreadKey() {
  if (pthread_key_create(&java_thread_key, &javaThreadDestructor)) {
    @throw create_JavaLangAssertionError_initWithId_(@"Failed to create pthread key.");
  }
}

// Initialize java_thread_key once.
void initJavaThreadKeyOnce() {
  pthread_once(&java_thread_key_init_once, &createJavaThreadKey);
}

// Returns the current Java thread or NULL if the calling thread is not a Java thread.
JavaLangThread *getCurrentJavaThreadOrNull() {
  initJavaThreadKeyOnce();
  return pthread_getspecific(java_thread_key);
}
