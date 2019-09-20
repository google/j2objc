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

#import "JavaObject.h"

#import <mach/mach.h>
#import <objc/NSObject.h>
#import <objc/objc.h>
#import <objc/runtime.h>
#import <stdio.h>
#import <stdlib.h>

mach_vm_size_t residentSize() {
  mach_task_basic_info_data_t info;
  mach_msg_type_number_t size = MACH_TASK_BASIC_INFO_COUNT;
  kern_return_t kerr = task_info(mach_task_self(),
                                 MACH_TASK_BASIC_INFO,
                                 (task_info_t)&info,
                                 &size);
  if( kerr == KERN_SUCCESS ) {
    return info.resident_size;
  } else {
    fprintf(stderr, "Error with task_info(): %s\n", mach_error_string(kerr));
    return 0;
  }
}

BOOL IsJavaObjectClass(Class cls) {
  while (cls != nil) {
    if (class_conformsToProtocol(cls, @protocol(JavaObject))) {
      return YES;
    }
     cls = class_getSuperclass(cls);
  }
  return NO;
}

int main(int argc, char * argv[]) {
  BOOL verbose = argc > 1 && strcmp(argv[1], "-v") == 0;
  mach_vm_size_t start = residentSize();
  mach_vm_size_t end = start;
  @autoreleasepool {
    int classCount = objc_getClassList(NULL, 0);
    Class *classes = (Class *)malloc(classCount * sizeof(Class));
    objc_getClassList(classes, classCount);
    for (int i = 0; i < classCount; i++) {
      Class cls = classes[i];
      if (IsJavaObjectClass(cls)) {
        mach_vm_size_t prev = end;
        // All JavaObject classes are NSObject subclasses.
        [(NSObject *)cls class];
        if (verbose) {
          end = residentSize();
          if ((end - prev) >= 100) {  // If at least .1k.
            printf("%5.1f K   %s\n", ((end - prev) / 1000.0), class_getName(cls));
          }
        }
      }
    }
    free(classes);
  }
  end = residentSize();
  double megs = ((end - start) / 1000000.0);
  if (verbose) {
    printf("\n");
  }
  printf("Total initialization memory: %0.1fM\n", megs);
  return 0;
}
