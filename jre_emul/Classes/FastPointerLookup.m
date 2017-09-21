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

#include "FastPointerLookup.h"

#include <stdlib.h>

#define INITIAL_CAPACITY 64

// Entry objects are immutable once initialized so their fields don't need to be
// atomic.
typedef struct Entry {
  void *key;
  void *value;
  struct Entry *next;
} Entry;

typedef struct FastPointerLookupStore {
  // Non-atomic. Read by the lock-free lookup but is never mutated once the
  // store becomes visible to the lock-free lookup.
  size_t size;
  // Non-atomic. nextEntry and lastEntry are only used under mutex.
  Entry *nextEntry;
  Entry *lastEntry;
  // Atomic. Read by the lock-free lookup and written concurrently by Put().
  _Atomic(Entry *) table[0];
} Store;

// Copied from Collections.secondaryHash() which HashMap uses.
static uint32_t Hash(void *key) {
  uint32_t h = (uint32_t)(uintptr_t)key;
  h += (h << 15) ^ 0xffffcd7d;
  h ^= (h >> 10);
  h += (h << 3);
  h ^= (h >> 6);
  h += (h << 2) + (h << 14);
  return h ^ (h >> 16);
}

static Store *NewStore(FastPointerLookup_t *lookup, size_t newSize) {
  // Use a load factor of .75, same as HashMap.
  size_t numEntries = (newSize >> 2) + (newSize >> 1);
  // The table array needs to be cleared.
  size_t storeSize = sizeof(Store) + sizeof(Entry *) * newSize;
  size_t allocSize = storeSize + sizeof(Entry) * numEntries;
  Store *store = (Store *)calloc(allocSize, 1);

  store->size = newSize;

  // Initialize nextEntry, making sure it is aligned.
  uintptr_t entriesBuf = (uintptr_t)store + storeSize;
  if (entriesBuf % __alignof__(Entry) != 0) {
    entriesBuf += __alignof__(Entry) - entriesBuf % __alignof__(Entry);
  }
  store->nextEntry = (Entry *)entriesBuf;

  // Initialize lastEntry as the greatest allowable Entry pointer in the
  // allocation.
  char *allocEnd = (char *)store + allocSize;
  store->lastEntry = ((Entry *)allocEnd) - 1;

  return store;
}

// Gets the Store, ensuring that it is created first. Caller must hold the
// mutex.
static Store *GetInitializedStore(FastPointerLookup_t *lookup) {
  Store *store = __c11_atomic_load(&lookup->store, __ATOMIC_RELAXED);
  if (!store) {
    store = NewStore(lookup, INITIAL_CAPACITY);
    // Atomic store with a barrier.
    __c11_atomic_store(&lookup->store, store, __ATOMIC_RELEASE);
  }
  return store;
}

// Creates a new store with double the capacity as oldStore, copies all entry
// data then swaps in the new store and frees oldStore when it is safe to do so.
static Store *Resize(FastPointerLookup_t *lookup, Store *oldStore) {
  size_t oldSize = oldStore->size;
  size_t newSize = oldSize << 1;
  Store *newStore = NewStore(lookup, newSize);

  for (size_t i = 0; i < oldSize; i++) {
    // Relaxed ordering because we hold the mutex.
    Entry *entry = __c11_atomic_load(&oldStore->table[i], __ATOMIC_RELAXED);
    while (entry) {
      size_t newIdx = Hash(entry->key) & (newSize - 1);
      Entry *newEntry = newStore->nextEntry++;
      newEntry->key = entry->key;
      newEntry->value = entry->value;
      newEntry->next = __c11_atomic_load(&newStore->table[newIdx], __ATOMIC_RELAXED);
      __c11_atomic_store(&newStore->table[newIdx], newEntry, __ATOMIC_RELEASE);
      entry = entry->next;
    }
  }

  // Once the new store is fully initialized, we can swap it with the old store
  // using an atomic store with a barrier.
  __c11_atomic_store(&lookup->store, newStore, __ATOMIC_RELEASE);

  // Synchronize with the fetch-add of "readers" to ensure we read the updated
  // value here.
  __c11_atomic_thread_fence(__ATOMIC_SEQ_CST);
  // We need to busy wait until there are no lock-free readers before it is safe
  // to free the old store.
  while (__c11_atomic_load(&lookup->readers, __ATOMIC_ACQUIRE) > 0);
  free(oldStore);

  return newStore;
}

// Adds a new entry to the store by calling the provided factory, resizing if
// necessary.
static Entry *Put(
    FastPointerLookup_t *lookup, Store *store, void *key, uint32_t hash, void *value) {
  if (store->nextEntry > store->lastEntry) {
    store = Resize(lookup, store);
  }
  size_t idx = hash & (store->size - 1);
  Entry *entry = store->nextEntry++;
  entry->key = key;
  entry->value = value;
  entry->next = __c11_atomic_load(&store->table[idx], __ATOMIC_RELAXED);
  // Must be an atomic store with a barrier here so that the lock-free lookup
  // will read consistent data.
  __c11_atomic_store(&store->table[idx], entry, __ATOMIC_RELEASE);
  return entry;
}

// Returns the Entry for the key and hash, or NULL. Caller must hold the mutex.
static Entry *FindEntryRelaxed(Store *store, void *key, uint32_t hash) {
  size_t idx = hash & (store->size - 1);
  Entry *entry = __c11_atomic_load(&store->table[idx], __ATOMIC_RELAXED);
  while (entry) {
    if (entry->key == key) {
      break;
    }
    entry = entry->next;
  }
  return entry;
}

// Does a lookup under mutual exclusion. Lazily creates the initial store. Calls
// Put() to add an entry if the key is not found.
static void *LockedLookup(FastPointerLookup_t *lookup, void *key, uint32_t hash) {
  pthread_mutex_lock(&lookup->mutex);
  Store *store = GetInitializedStore(lookup);
  Entry *entry = FindEntryRelaxed(store, key, hash);
  if (!entry) {
    entry = Put(lookup, store, key, hash, lookup->create_func(key));
  }
  void *result = entry->value;
  pthread_mutex_unlock(&lookup->mutex);
  return result;
}

// Attempts a fast lock-free lookup before grabbing any locks.
void *FastPointerLookup(FastPointerLookup_t *lookup, void *key) {
  uint32_t hash = Hash(key);
  __c11_atomic_fetch_add(&lookup->readers, 1, __ATOMIC_RELAXED);
  // Synchronize with "Resize()" above to ensure it reads the increment of
  // "readers" and doesn't deallocate the store while we read from it.
  __c11_atomic_thread_fence(__ATOMIC_SEQ_CST);
  // Atomic load with barrier.
  Store *store = __c11_atomic_load(&lookup->store, __ATOMIC_ACQUIRE);

  void *result = NULL;
  if (store) {
    size_t idx = hash & (store->size - 1);
    Entry *entry = __c11_atomic_load(&store->table[idx], __ATOMIC_ACQUIRE);
    while (entry) {
      if (entry->key == key) {
        result = entry->value;
        break;
      }
      entry = entry->next;
    }
  }

  // Exit protected read-only section. (Safe to delete store now)
  __c11_atomic_fetch_sub(&lookup->readers, 1, __ATOMIC_RELEASE);

  if (result) {
    return result;
  }
  return LockedLookup(lookup, key, hash);
}

bool FastPointerLookupAddMapping(FastPointerLookup_t *lookup, void *key, void *value) {
  bool result = false;
  pthread_mutex_lock(&lookup->mutex);
  Store *store = GetInitializedStore(lookup);
  uint32_t hash = Hash(key);
  Entry *entry = FindEntryRelaxed(store, key, hash);
  if (!entry) {
    Put(lookup, store, key, hash, value);
    result = true;
  }
  pthread_mutex_unlock(&lookup->mutex);
  return result;
}
