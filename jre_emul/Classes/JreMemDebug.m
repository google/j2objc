// Copyright 2011 Google Inc. All Rights Reserved.
//
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

// Set minimum iOS requirement if compiled on Xcode5.
#ifdef SET_MIN_IOS_VERSION
#ifndef __IPHONE_5_0
  #define __IPHONE_5_0 50000
#endif

#if (!defined(__IPHONE_OS_VERSION_MIN_REQUIRED) || \
    __IPHONE_OS_VERSION_MIN_REQUIRED < __IPHONE_5_0)
#undef __IPHONE_OS_VERSION_MIN_REQUIRED
#define __IPHONE_OS_VERSION_MIN_REQUIRED __IPHONE_5_0
#endif
#endif

#import "JreMemDebug.h"

#import "NSObject+JavaObject.h"

#include <execinfo.h>
#include <pthread.h>
#include <sys/time.h>
#include <objc/runtime.h>

BOOL JreMemDebugEnabled = NO;

#if __has_feature(objc_arc)

// We don't implement memory usage debug when ARC is enabled because we rely
// on JreStrongAssign() to lock properly to avoid some memory inconsistency
// while running JreMemDebugGenerateAllocationsReport().

FOUNDATION_EXPORT id JreMemDebugAddInternal(id obj) {
  return obj;
}
FOUNDATION_EXPORT void JreMemDebugRemoveInternal(id obj) {
}
FOUNDATION_EXPORT void JreMemDebugLockInternal(void) {
}
FOUNDATION_EXPORT void JreMemDebugMarkAllocations(void) {
  NSLog(@"J2Objc MemDebug: Not implemented when ARC is enabled.");
}
FOUNDATION_EXPORT void JreMemDebugShowAllocations(void) {
  NSLog(@"J2Objc MemDebug: Not implemented when ARC is enabled.");
}
FOUNDATION_EXPORT void JreMemDebugGenerateAllocationsReport(void) {
  NSLog(@"J2Objc MemDebug: Not implemented when ARC is enabled.");
}
#else

// We use [NSNumber numberWithUnsignedLong:] to store pointer values in
// an object we can store in standard Objective-C containers because
// unsigned long is the same size as a pointer and [NSValue valueWithPointer:]
// seems to be broken.

// The actual global lock for memory debugging.
static pthread_mutex_t memDebugLock = PTHREAD_RECURSIVE_MUTEX_INITIALIZER;

// We store all allocations in that set.
static NSMutableSet *memDebugAllocations = nil;

// Stacktrace of allocations. Key are the allocation pointers in NSNumber and
// values are NSData that contains stack trace.
static NSMutableDictionary *memDebugAllocationsStacktrace = nil;

// It's the set of allocations to ignore in the analysis.
static NSMutableSet *memDebugMarkedAllocations = nil;

// It's the folder where to save the reports.
static NSString *memDebugSessionFolder = nil;

// This function initializes the memory debugging mode.
static void memDebugInit(void) {
  static dispatch_once_t once;
  dispatch_once(&once,
      ^{
           NSString *memDebugBaseFolder = [@"~/Library/Logs/J2Objc"
               stringByExpandingTildeInPath];

           unsigned int count =  1;
           NSString *path = nil;
           while (1) {
             NSString *name = [NSString stringWithFormat:@"%u", count];
             path = [memDebugBaseFolder stringByAppendingPathComponent:name];
             if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
               break;
             }
             count ++;
           }
           memDebugSessionFolder = [path retain];
           memDebugAllocations = [[NSMutableSet alloc] init];
           memDebugAllocationsStacktrace = [[NSMutableDictionary alloc] init];
       });
}

// Maximum number of pointers to collect when getting stacktrace.
#define kMaxAddresses 512

static NSData* currentStackTraceData(void) {
  void *addresses[kMaxAddresses];
  int count = backtrace(addresses, sizeof(addresses));
  return [NSData dataWithBytes:addresses length:count * sizeof(addresses[0])];
}

FOUNDATION_EXPORT id JreMemDebugAddInternal(id obj) {
  // Avoid tracking an overwhelming number of objects.
  // They don't have any links. Therefore, it's not interesting to track
  // them and leaks of those objects can be tracked easily in Instruments.
  if ([obj class] == [NSObject class])
    return obj;

  memDebugInit();
  NSNumber *value = [NSNumber numberWithUnsignedLong:(unsigned long) obj];
  JreMemDebugLock();
  [memDebugAllocations addObject:value];
  [memDebugAllocationsStacktrace setObject:currentStackTraceData()
                                    forKey:value];
  JreMemDebugUnlock();

  return obj;
}

FOUNDATION_EXPORT void JreMemDebugRemoveInternal(id obj) {
  memDebugInit();
  NSNumber *value = [NSNumber numberWithUnsignedLong:(unsigned long) obj];
  JreMemDebugLock();
  [memDebugAllocations removeObject:value];
  [memDebugAllocationsStacktrace removeObjectForKey:value];
  [memDebugMarkedAllocations removeObject:value];
  JreMemDebugUnlock();
}

FOUNDATION_EXPORT void JreMemDebugLockInternal(void) {
  pthread_mutex_lock(&memDebugLock);
}

FOUNDATION_EXPORT void JreMemDebugUnlockInternal(void) {
  pthread_mutex_unlock(&memDebugLock);
}

static NSInteger sortByCount(id obj1, id obj2, void *context) {
  NSString * str1 = obj1;
  NSString * str2 = obj2;

  if (context == NULL) {
    return [str1 compare:str2];
  } else {
    NSDictionary * counts = (id) context;
    return [[counts objectForKey:str2] compare:[counts objectForKey:str1]];
  }
}

FOUNDATION_EXPORT void JreMemDebugMarkAllocations(void) {
  JreMemDebugLock();
  [memDebugMarkedAllocations release];
  memDebugMarkedAllocations = [memDebugAllocations mutableCopy];
  JreMemDebugUnlock();
}

// This function will simplify the name of the class to something simpler.
static NSString * displayNameForIdentifier(NSString *identifier) {
  if ([identifier hasPrefix:@"ComGoogle"]) {
    identifier =
        [identifier stringByReplacingOccurrencesOfString:@"ComGoogle"
                                              withString:@"CG"];
  } else if ([identifier hasPrefix:@"JavaUtil"]) {
    identifier =
        [identifier stringByReplacingOccurrencesOfString:@"JavaUtil"
                                              withString:@""];
  }

  return identifier;
}

FOUNDATION_EXPORT void JreMemDebugGenerateAllocationsReport(void) {
  if (!JreMemDebugEnabled)
    return;

  JreMemDebugLock();

  [[NSFileManager defaultManager] createDirectoryAtPath:memDebugSessionFolder
      withIntermediateDirectories:YES attributes:nil error:NULL];

  // We generate the name of the .log file to write.
  struct timeval tv;
  struct tm tm_value;
  gettimeofday(&tv, NULL);
  localtime_r(&tv.tv_sec, &tm_value);
  NSString *dateString = [NSString stringWithFormat:@"%02u-%02u-%02u",
      tm_value.tm_hour, tm_value.tm_min, tm_value.tm_sec];
  NSString *filename = [NSString stringWithFormat:@"%@/j2objc-memdebug-%@.log",
      memDebugSessionFolder, dateString];

  NSLog(@"write file to %@", filename);
  FILE *f = fopen([filename fileSystemRepresentation], "w");
  if (f == NULL) {
    NSLog(@"failed to write log file");
    JreMemDebugUnlock();
    return;
  }

  // We count all the used classes since the last mark.
  fprintf(f, "-- classes --\n");
  NSMutableSet *resultSet = [memDebugAllocations mutableCopy];
  [resultSet minusSet:memDebugMarkedAllocations];
  fprintf(f, "%i objects\n", (int)[resultSet count]);

  fprintf(f, "----\n");

  NSMutableSet *resultClasses = [[NSMutableSet alloc] init];
  NSMutableDictionary *classesCount = [[NSMutableDictionary alloc] init];

  for (NSNumber *value in [resultSet allObjects]) {
    id obj = (id) [value unsignedLongValue];
    NSString *className =
        [NSString stringWithUTF8String:object_getClassName(obj)];
    [resultClasses addObject:className];

    NSNumber *nb = [classesCount objectForKey:className];
    int value = [nb intValue] + 1;
    [classesCount setObject:[NSNumber numberWithInt:value] forKey:className];
  }

  // Show result.
  for (NSString *className in [[resultClasses allObjects]
      sortedArrayUsingFunction:sortByCount context:classesCount]) {
    int value = [[classesCount objectForKey:className] intValue];
    fprintf(f, "%s: %i\n", [className UTF8String], value);
  }

  fprintf(f, "----\n");

  // Group objects per stack trace.

  // For each different stack trace, we assign a number from 1 to n.
  // identifierHelper will help generate it.
  // And we generate an identifier which is formed with the class name and
  // this number. It will look like "JavaUtilHashmap:534".
  int identifierHelper = 1;

  // stacktraceToIdentifier will store an identifier for a given
  // stacktrace NSData.
  NSMutableDictionary *stacktraceToIdentifier =
      [[NSMutableDictionary alloc] init];
  // identifierToStacktrace will store a stacktrace NSDate for a given
  // identifier.
  NSMutableDictionary *identifierToStacktrace =
      [[NSMutableDictionary alloc] init];
  // identifierCount will store the number of corresponding objects allocated
  // from the given stacktrace.
  NSMutableDictionary *identifierCount = [[NSMutableDictionary alloc] init];

  // We won't report some basic objects.
  NSMutableSet *filterClasses = [[NSMutableSet alloc] init];
  [filterClasses addObject:@"JavaLangBoolean"];
  [filterClasses addObject:@"JavaLangByte"];
  [filterClasses addObject:@"JavaLangDouble"];
  [filterClasses addObject:@"JavaLangFloat"];
  [filterClasses addObject:@"JavaLangInteger"];
  [filterClasses addObject:@"JavaLangLong"];
  [filterClasses addObject:@"JavaLangShort"];
  [filterClasses addObject:@"JavaLangVoid"];
  [filterClasses addObject:@"JavaLangEnum"];
  [filterClasses addObject:@"IOSClass"];

  // Fill the content of stacktraceToIdentifier, identifierCount and
  // identifierToStacktrace.
  for (NSNumber *value in [resultSet allObjects]) {
    id obj = (id) [value unsignedLongValue];

    NSData *stacktrace = [memDebugAllocationsStacktrace objectForKey:value];
    NSString *className =
        [NSString stringWithUTF8String:object_getClassName(obj)];
    if ([filterClasses containsObject:className]) {
      continue;
    }

    NSString *identifier = [stacktraceToIdentifier objectForKey:stacktrace];
    if (identifier == nil) {
      identifier =
          [NSString stringWithFormat:@"%@:%i", className, identifierHelper];
      identifierHelper ++;
      [stacktraceToIdentifier setObject:identifier forKey:stacktrace];
      [identifierCount setObject:[NSNumber numberWithInt:1]
                          forKey:identifier];
      [identifierToStacktrace setObject:stacktrace forKey:identifier];
    }
    else {
      NSNumber *nb = [identifierCount objectForKey:identifier];
      int value = [nb intValue] + 1;
      [identifierCount setObject:[NSNumber numberWithInt:value]
                          forKey:identifier];
    }
  }

  // Write the result to the .log file.
  for (NSString *identifier in [identifierCount allKeys]) {
    int value = [[identifierCount objectForKey:identifier] intValue];
    fprintf(f, "%s: %i\n", [identifier UTF8String], value);
  }

  // graphLinks will store informations about a link for a given link
  // identifier. The link identifier will look like
  // "JavaUtilHashmap:534-String:1223-key"
  // Information about a like will be
  // {source: "JavaUtilHashmap:534", target: "String:1223", name: "key"}
  // Information returned by -memDebugStongReferences will be used for
  // the name of the link.
  NSMutableDictionary *graphLinks = [[NSMutableDictionary alloc] init];
  // includedInGraph will mark identifier that have been used in the graph.
  NSMutableSet *includedInGraph = [[NSMutableSet alloc] init];

  // We collect information about the graph of objects using
  // -memDebugStrongReferences.

  // For each object since the last mark ...
  for (NSNumber *value in [resultSet allObjects]) {
    id obj = (id) [value unsignedLongValue];
    NSData *stacktrace = [memDebugAllocationsStacktrace objectForKey:value];
    NSString *identifier = [stacktraceToIdentifier objectForKey:stacktrace];

    NSObject *javaObj = obj;
    NSArray *referencesInfos = [javaObj memDebugStrongReferences];

    // We run through all strong references ...
    for (unsigned int i = 0 ; i < [referencesInfos count] ; i ++) {
      JreMemDebugStrongReference *referenceInfo =
          [referencesInfos objectAtIndex:i];
      NSString *name = [referenceInfo name];
      id targetObj = [referenceInfo object];
      if (targetObj == nil) {
        // Occurs when the strong reference is not set.
        continue;
      }

      NSData *targetStacktrace =
          [memDebugAllocationsStacktrace objectForKey:
              [NSNumber numberWithUnsignedLong:(unsigned long)targetObj]];
      NSString *targetIdentifier =
          [stacktraceToIdentifier objectForKey:targetStacktrace];
      if (targetIdentifier == nil) {
        // Occurs in case the target object is in the marked allocations set.
        continue;
      }

      NSString *linkIdentifier = [NSString stringWithFormat:@"%@-%@-%@",
          identifier, targetIdentifier, name];
      if (![graphLinks objectForKey:linkIdentifier]) {
        // And store them in a graph.
        NSMutableDictionary *info = [[NSMutableDictionary alloc] init];
        [info setObject:identifier forKey:@"source"];
        [info setObject:targetIdentifier forKey:@"target"];
        [info setObject:name forKey:@"name"];
        [graphLinks setObject:info forKey:linkIdentifier];
        [info release];

        [includedInGraph addObject:identifier];
        [includedInGraph addObject:targetIdentifier];
      }
    }
  }

  // staticVariables contains objects that are pointed by a static variable.
  NSMutableSet *staticVariables = [[NSMutableSet alloc] init];

  // Collects the static variables.
  for (NSString *className in [resultClasses allObjects]) {
    NSArray *referencesInfos =
        [NSClassFromString(className) memDebugStaticReferences];
    for(unsigned int i = 0 ; i < [referencesInfos count] ; i ++) {
      JreMemDebugStrongReference *referenceInfo = [referencesInfos objectAtIndex:i];
      NSString *name = [referenceInfo name];
      id targetObj = [referenceInfo object];
      if (targetObj == nil) {
        continue;
      }
      NSData *stacktrace = [memDebugAllocationsStacktrace objectForKey:
          [NSNumber numberWithUnsignedLong:(unsigned long)targetObj]];
      if (stacktrace == nil) {
        NSLog(@"could not find stack trace for %@ %p", name, targetObj);
        continue;
      }
      NSString *targetIdentifier =
          [stacktraceToIdentifier objectForKey:stacktrace];
      if (targetIdentifier == nil) {
        NSLog(@"could not find identifier for stacktrace %@ %p", name, targetObj);
        continue;
      }

      [staticVariables addObject:targetIdentifier];
    }
  }

  // We write .dot file (graphviz).
  filename = [NSString stringWithFormat:@"%@/j2objc-memdebug-%@.dot",
      memDebugSessionFolder, dateString];
  FILE *dotF = fopen([filename fileSystemRepresentation], "w");

  fprintf(dotF, "digraph G {\n");

  // We'll generate the colors depending on the count of objects for
  // each identifier.
  int maxCount = 10;
  for(NSString *identifier in [identifierCount allKeys]) {
    int value = [[identifierCount objectForKey:identifier] intValue];
    if (value > maxCount) {
      maxCount = value;
    }
  }

  // Generates the color for each identifier.
  for (NSString *identifier in [[identifierCount allKeys]
      sortedArrayUsingSelector:@selector(compare:)]) {
    int value = [[identifierCount objectForKey:identifier] intValue];
    NSString *displayName = displayNameForIdentifier(identifier);
    float factor = (float) value / (float) maxCount;
    int minValue[3] = {255, 255, 255};
    int maxValue[3] = {255, 128, 128};
    int color[3];
    for (unsigned int i = 0 ; i < 3 ; i ++) {
      color[i] = (int) (((float) minValue[i]) +
          ((float) (maxValue[i] - minValue[i])) * factor);
    }
    if ([staticVariables containsObject:identifier]) {
      int blueColor[3] = {128, 128, 255};
      memcpy(color, blueColor, sizeof(color));

      if (![includedInGraph containsObject:identifier]) {
        // ignore static variables with no graph
        continue;
      }
    }
    fprintf(dotF, "\"%s (%i)\" [fillcolor = \"#%02x%02x%02x\"];\n",
            [displayName UTF8String], value, color[0], color[1], color[2]);
  }

  // Generates the link of the graph.
  for (NSString *linkIdentifier in [[graphLinks allKeys]
      sortedArrayUsingSelector:@selector(compare:)]) {
    NSDictionary *info = [graphLinks objectForKey:linkIdentifier];
    NSString *sourceIdentifier = [info objectForKey:@"source"];
    NSString *targetIdentifier = [info objectForKey:@"target"];
    NSString *name = [info objectForKey:@"name"];
    NSString *sourceDisplayName = displayNameForIdentifier(sourceIdentifier);
    NSString *targetDisplayName = displayNameForIdentifier(targetIdentifier);
    NSNumber *sourceCount = [identifierCount objectForKey:sourceIdentifier];
    NSNumber *targetCount = [identifierCount objectForKey:targetIdentifier];
    fprintf(dotF, "\"%s (%i)\" -> \"%s (%i)\" [label = \"%s\"];\n",
      [sourceDisplayName UTF8String], [sourceCount intValue],
      [targetDisplayName UTF8String], [targetCount intValue],
      [name UTF8String]);
  }
  fprintf(dotF, "}\n");
  fclose(dotF);

  // We write the stack trace in the .log file for reference.
  fprintf(f, "-- stack trace --\n");

  for (NSString *identifier in [[identifierToStacktrace allKeys]
      sortedArrayUsingSelector:@selector(compare:)]) {
    NSData *stacktrace = [identifierToStacktrace objectForKey:identifier];
    void *addresses[512];
    size_t count;
    memcpy(addresses, [stacktrace bytes], [stacktrace length]);
    count = [stacktrace length] / sizeof(addresses[0]);
    fprintf(f, "%s:", [identifier UTF8String]);
    for (size_t i = 0 ; i < count ; i ++) {
      fprintf(f, " %p", addresses[i]);
    }
    fprintf(f, "\n");
  }

  [staticVariables release];
  [includedInGraph release];
  [graphLinks release];

  [identifierToStacktrace release];
  [identifierCount release];
  [stacktraceToIdentifier release];

  [classesCount release];
  [resultClasses release];

  [resultSet release];

  fprintf(f, "-- classes end --\n");

  JreMemDebugUnlock();
}
#endif

