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
//  jvm.m
//  JreEmulation
//
//  Provides J2ObjC implementation for the JVM functions defined in libcore's
//  jvm.h
//
//  Created by Keith Stanger on Mar. 8, 2016.
//

#include <pthread.h>

#include "TempFailureRetry.h"
#include "java/io/File.h"
#include "jvm.h"

static const char *absolutePath(const char *path) {
  if (!path || !*path) {
    return path;
  }
  NSString *pathStr = [NSString stringWithUTF8String:path];
  if ([pathStr characterAtIndex:0] != '/') {
    JavaIoFile *f = new_JavaIoFile_initWithNSString_(pathStr);
    pathStr = [f getAbsolutePath];
    RELEASE_(f);
  }
  return [pathStr fileSystemRepresentation];
}

jint JVM_GetLastErrorString(char *buf, int len) {
  int ret = strerror_r(errno, buf, len);
  if (ret != 0) {
    return 0;
  }
  return (jint)strlen(buf);
}

char *JVM_NativePath(char *path) {
  NSString *str = [[NSString alloc] initWithUTF8String:path];
  [str getFileSystemRepresentation:path maxLength:strlen(path) + 1];
  RELEASE_(str);
  return path;
}

jint JVM_Open(const char *fname, jint flags, jint mode) {
  fname = absolutePath(fname);
  int fd = TEMP_FAILURE_RETRY(open(fname, flags, mode));
  if (fd < 0) {
    int err = errno;
    if (err == EEXIST) {
      return JVM_EEXIST;
    } else {
      return JVM_IO_ERR;
    }
  }
  return fd;
}

jint JVM_Close(jint fd) {
  return close(fd);
}

jlong JVM_Lseek(jint fd, jlong offset, jint whence) {
  return TEMP_FAILURE_RETRY(lseek(fd, offset, whence));
}

void *JVM_RawMonitorCreate(void) {
  pthread_mutex_t *lock = malloc(sizeof(pthread_mutex_t));
  if (pthread_mutex_init(lock, NULL) == 0) {
    return lock;
  } else {
    free(lock);
    return NULL;
  }
}

void JVM_RawMonitorDestroy(void *mon) {
  pthread_mutex_destroy(mon);
  free(mon);
}

jint JVM_RawMonitorEnter(void *mon) {
  return pthread_mutex_lock(mon);
}

void JVM_RawMonitorExit(void *mon) {
  pthread_mutex_unlock(mon);
}
