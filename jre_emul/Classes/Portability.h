/*
 * Copyright (C) 2012 The Android Open Source Project
 *
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

#ifndef PORTABILITY_H_included
#define PORTABILITY_H_included

#if defined(__APPLE__)

// Mac OS.
#include <AvailabilityMacros.h> // For MAC_OS_X_VERSION_MAX_ALLOWED

#include <libkern/OSByteOrder.h>
#define bswap_16 OSSwapInt16
#define bswap_32 OSSwapInt32
#define bswap_64 OSSwapInt64

#if !defined(TARGET_OS_IPHONE) && !defined(TARGET_IPHONE_SIMULATOR)
#include <crt_externs.h>
#define environ (*_NSGetEnviron())
#endif

// Mac OS has a 64-bit off_t and no 32-bit compatibility cruft.
#define flock64 flock
#define ftruncate64 ftruncate
#define isnanf __inline_isnanf
#define lseek64 lseek
#define pread64 pread
#define pwrite64 pwrite

// TODO: Darwin appears to have an fdatasync syscall.
static inline int fdatasync(int fd) { return fsync(fd); }

// For Linux-compatible sendfile(3).
#include <sys/socket.h>
#include <sys/types.h>
static inline ssize_t sendfile_(int out_fd, int in_fd, off_t* offset, size_t count) {
  off_t in_out_count = count;
  int result = sendfile(in_fd, out_fd, *offset, &in_out_count, NULL, 0);
  if (result == -1) {
    return -1;
  }
  return (ssize_t) in_out_count;
}

// For mincore(3).
#define _DARWIN_C_SOURCE
#include <sys/mman.h>
#undef _DARWIN_C_SOURCE
static inline int mincore_(void* addr, size_t length, unsigned char* vec) {
#ifdef __cplusplus
  return mincore(addr, length, reinterpret_cast<char*>(vec));
#else
  return mincore(addr, length, (char *) vec);
#endif
}


#else

// Bionic or glibc.

#include <byteswap.h>
#include <sys/sendfile.h>

// The prebuilt toolchains tend to be rather old and don't include the newest
// kernel headers. CAP_BLOCK_SUSPEND was introduced in 3.5, so until all of
// headers update to at least that version, we need this hack.
#ifndef CAP_BLOCK_SUSPEND
#define CAP_BLOCK_SUSPEND 36
#define CAP_LAST_CAP CAP_BLOCK_SUSPEND
#endif // ifndef CAP_BLOCK_SUSPEND

#endif

#include <sys/statvfs.h>

#endif  // PORTABILITY_H_included
