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
//
//  Created by Keith Stanger on 9/24/13.
//
//  Hooks into malloc to record memory allocation and deallocation.

#include "my_malloc.h"

#include "java/io/PrintStream.h"
#include "java/lang/System.h"
#include <execinfo.h>
#include <malloc/malloc.h>
#include <objc/runtime.h>
#include <sys/mman.h>

//#define REPORT_BACKTRACES
#define REPORT_EACH_BLOCK
#define ALLOCATED_BLOCKS_SIZE 1024
#define BACKTRACE_SIZE 16

static void *(*system_malloc)(malloc_zone_t *zone, size_t size);
static void *(*system_calloc)(malloc_zone_t *zone, size_t num_items, size_t size);
static void *(*system_valloc)(malloc_zone_t *zone, size_t size);
static void *(*system_realloc)(malloc_zone_t *zone, void *ptr, size_t size);
static void *(*system_memalign)(malloc_zone_t *zone, size_t alignment, size_t size);
static void (*system_free)(malloc_zone_t *zone, void *ptr);
static void (*system_free_definite_size)(malloc_zone_t *zone, void *ptr, size_t size);

static void *allocated_blocks[ALLOCATED_BLOCKS_SIZE];
static unsigned int allocated_blocks_idx = 0;
#ifdef REPORT_BACKTRACES
static void *backtraces[ALLOCATED_BLOCKS_SIZE][BACKTRACE_SIZE];
static int backtrace_frames[ALLOCATED_BLOCKS_SIZE];
#endif

static inline void *track_alloc(void *ptr) {
#ifdef REPORT_BACKTRACES
  backtrace_frames[allocated_blocks_idx] =
      backtrace(backtraces[allocated_blocks_idx], BACKTRACE_SIZE);
#endif
  allocated_blocks[allocated_blocks_idx++] = ptr;
  if (allocated_blocks_idx == ALLOCATED_BLOCKS_SIZE) {
    malloc_printf("ERROR: Out of space for tracking allocated blocks.\n");
  }
  return ptr;
}

static inline void clear_from_allocated_blocks(void *ptr) {
  for (int i = 0; i < allocated_blocks_idx; i++) {
    if (allocated_blocks[i] == ptr) {
      allocated_blocks[i] = NULL;
      break;
    }
  }
}

static void *my_malloc(malloc_zone_t *zone, size_t size) {
  return track_alloc(system_malloc(zone, size));
}

static void *my_calloc(malloc_zone_t *zone, size_t num_items, size_t size) {
  return track_alloc(system_calloc(zone, num_items, size));
}

static void *my_valloc(malloc_zone_t *zone, size_t size) {
  return track_alloc(system_valloc(zone, size));
}

static void *my_realloc(malloc_zone_t *zone, void *ptr, size_t size) {
  clear_from_allocated_blocks(ptr);
  return track_alloc(system_realloc(zone, ptr, size));
}

static void *my_memalign(malloc_zone_t *zone, size_t alignment, size_t size) {
  return track_alloc(system_memalign(zone, alignment, size));
}

static void my_free(malloc_zone_t *zone, void *ptr) {
  clear_from_allocated_blocks(ptr);
  system_free(zone, ptr);
}

static void my_free_definite_size(malloc_zone_t *zone, void *ptr, size_t size) {
  clear_from_allocated_blocks(ptr);
  system_free_definite_size(zone, ptr, size);
}

void my_malloc_install() {
  malloc_zone_t *zone = malloc_default_zone();
  system_malloc = zone->malloc;
  system_calloc = zone->calloc;
  system_valloc = zone->valloc;
  system_realloc = zone->realloc;
  system_memalign = zone->memalign;
  system_free = zone->free;
  system_free_definite_size = zone->free_definite_size;
  // Change the protection of the page containing the default zone.
  mprotect(zone, sizeof(malloc_zone_t), PROT_READ | PROT_WRITE);
  zone->malloc = &my_malloc;
  zone->calloc = &my_calloc;
  zone->valloc = &my_valloc;
  zone->realloc = &my_realloc;
  zone->memalign = &my_memalign;
  zone->free = &my_free;
  zone->free_definite_size = &my_free_definite_size;
}

void my_malloc_clear() {
  for (int i = 0; i < ALLOCATED_BLOCKS_SIZE; i++) {
    allocated_blocks[i] = NULL;
#ifdef REPORT_BACKTRACES
    backtrace_frames[i] = 0;
#endif
  }
  allocated_blocks_idx = 0;
}

void my_malloc_reset() {
  malloc_zone_t *zone = malloc_default_zone();
  zone->malloc = system_malloc;
  zone->calloc = system_calloc;
  zone->valloc = system_valloc;
  zone->realloc = system_realloc;
  zone->memalign = system_memalign;
  zone->free = system_free;
  zone->free_definite_size = system_free_definite_size;
}

void my_malloc_reset_and_report() {
  my_malloc_reset();
  NSUInteger numClasses = objc_getClassList(NULL, 0);
  Class *classesList = malloc(sizeof(Class) * numClasses);
  numClasses = objc_getClassList(classesList, numClasses);
  int total_bytes = 0;
  for (int i = 0; i < allocated_blocks_idx; i++) {
    void *ptr = allocated_blocks[i];
    if (!ptr) {
      continue;
    }
    Class testClass = *((Class *) ptr);
    NSString *className = @"unknown";
    for (int j = 0; j < numClasses; j++) {
      if (classesList[j] == testClass) {
        className = NSStringFromClass(testClass);
        break;
      }
    }
    int size = malloc_size(ptr);
    total_bytes += size;
#ifdef REPORT_EACH_BLOCK
    [JavaLangSystem_get_out() printlnWithNSString:[NSString stringWithFormat:
        @"Class: %@, size: %d", className, size]];
#endif
#ifdef REPORT_BACKTRACES
    if (backtrace_frames[i] > 0) {
      char **bt_syms = backtrace_symbols(backtraces[i], backtrace_frames[i]);
      for (int j = 0; j < backtrace_frames[i]; j++) {
        [[JavaLangSystem out] printlnWithNSString:[NSString stringWithUTF8String:bt_syms[j]]];
      }
    }
#endif
  }
  [JavaLangSystem_get_out() printlnWithNSString:[NSString stringWithFormat:
      @"Total bytes: %d", total_bytes]];
}
