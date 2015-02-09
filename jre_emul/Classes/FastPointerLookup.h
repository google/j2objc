// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#import <pthread.h>

struct FastPointerLookupStore;

typedef struct FastPointerLookup_t {
  pthread_mutex_t mutex;  // For mutual exclusion while editing the store.
  void *(*create_func)(void *);  // Creates the result for as yet unmapped keys.
  // Atomic. Incremented on entry of a lock-free lookup and decremented on exit.
  _Atomic(size_t) readers;
  _Atomic(struct FastPointerLookupStore *) store;
} FastPointerLookup_t;

/**
 * Static initializer to use with a FastPointerLookup_t declaration.
 *
 * @define FAST_POINTER_LOOKUP_INIT
 * @param create_func A pointer to the function that generates values from keys.
 *   Execution of this function is synchronized and it will only be called once
 *   for each key.
 */
#define FAST_POINTER_LOOKUP_INIT(create_func) \
  { PTHREAD_MUTEX_INITIALIZER, create_func, 0, NULL }

// Looks up the value for a key. This is the only operation.
void *FastPointerLookup(FastPointerLookup_t *lookup, void *key);
