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

#import "JreEmulation.h"
#import "IOSPrimitiveArray.h"
#import "java/nio/ByteArrayBuffer.h"

static inline char *BytesRW(id object) {
  nil_chk(object);
  if ([object isKindOfClass:[IOSByteArray class]]) {
      return (char *)IOSByteArray_GetRef(object, 0);
  } else if ([object isKindOfClass:[JavaNioBuffer class]]) {
    // All buffer concrete classes have byteBuffer at the same
    // offset by explicit design.
    JavaNioBuffer *buffer = (JavaNioBuffer *) object;
    return (char *)buffer->effectiveDirectAddress_;
  }
  return NULL;  // Unknown type.
}

static inline const char *BytesRO(id object) {
  return (const char *) BytesRW(object);
}


#endif
