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
//  BufferUtils.h
//  JreEmulation
//
//  Created by Tom Ball on 2/13/14.
//

#ifndef JreEmulation_BufferUtils_h
#define JreEmulation_BufferUtils_h

#import <objc/runtime.h> // For object_getClass

#import "IOSPrimitiveArray.h"
#import "java/nio/Buffer.h"

static inline char *BytesRW(id object) {
  (void)nil_chk(object);
  Class cls = object_getClass(object);
  if (cls == [IOSByteArray class]) {
    return (char *)((IOSByteArray *)object)->buffer_;
  } else if (cls == [IOSCharArray class]) {
    return (char *)((IOSCharArray *)object)->buffer_;
  } else if (cls == [IOSShortArray class]) {
    return (char *)((IOSShortArray *)object)->buffer_;
  } else if (cls == [IOSIntArray class]) {
    return (char *)((IOSIntArray *)object)->buffer_;
  } else if (cls == [IOSLongArray class]) {
    return (char *)((IOSLongArray *)object)->buffer_;
  } else if (cls == [IOSFloatArray class]) {
    return (char *)((IOSFloatArray *)object)->buffer_;
  } else if (cls == [IOSDoubleArray class]) {
    return (char *)((IOSDoubleArray *)object)->buffer_;
  } else if ([object isKindOfClass:[JavaNioBuffer class]]) {
    // All buffer concrete classes have byteBuffer at the same
    // offset by explicit design.
    JavaNioBuffer *buffer = (JavaNioBuffer *) object;
    return (char *)buffer->address_;
  }
  return NULL;  // Unknown type.
}

static inline const char *BytesRO(id object) {
  return (const char *) BytesRW(object);
}


#endif
