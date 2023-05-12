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

#include "J2ObjC_source.h"
#include "com/google/j2objc/LibraryNotLinkedError.h"
#include "java/io/BufferedInputStream.h"
#include "java/io/ByteArrayInputStream.h"
#include "java/io/FileInputStream.h"
#include "java/lang/ClassLoader.h"
#include "java/lang/Exception.h"
#include "java/lang/reflect/Method.h"
#include "java/net/URL.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "jni.h"

static IOSByteArray *GetLinkedResource(NSString *name) {
  if ([name characterAtIndex:0] != '/') {
    name = [NSString stringWithFormat:@"/%@", name];
  }
  const char *resourceName =
      [[[[name stringByReplacingOccurrencesOfString:@"/" withString:@"_"]
               stringByReplacingOccurrencesOfString:@"." withString:@"_"]
               stringByReplacingOccurrencesOfString:@"-" withString:@"_"] UTF8String];
  extern J2ObjcResourceDefinition start_resource_section __asm(
      "section$start$__DATA$__j2objcresource");
  extern J2ObjcResourceDefinition end_resource_section __asm(
      "section$end$__DATA$__j2objcresource");
  NSUInteger nResources = (NSUInteger)(&end_resource_section - &start_resource_section);
  for (long i = 0; i < nResources; i++) {
    J2ObjcResourceDefinition *resource = (&start_resource_section) + i;
    if (strcmp(resourceName, resource->full_name) == 0) {
      return [IOSByteArray arrayWithBytes:(const jbyte *)resource->data
                                    count:(NSUInteger)resource->length];
    }
  }
  return nil;
}

static JavaNetURL *CreateResourceURL(NSString *name, IOSByteArray *data) {
  @try {
    IOSClass *resourceClass =
        IOSClass_forName_(@"com.google.j2objc.net.ResourceDataStreamHandler");
    IOSObjectArray *paramTypes =
        [IOSObjectArray arrayWithObjects:(id[]){ NSString_class_(), [data java_getClass]}
                                   count:2
                                    type:IOSClass_class_()];
    JavaLangReflectMethod *m = [resourceClass getMethod:@"createResourceDataURL"
                                         parameterTypes:paramTypes];
    IOSObjectArray *args = [IOSObjectArray arrayWithObjects:(id[]){ name, data }
                                                      count:2
                                                       type:NSObject_class_()];
    return (JavaNetURL *)[m invokeWithId:nil withNSObjectArray:args];
  }
  @catch (JavaLangException *e) {
    @throw create_ComGoogleJ2objcLibraryNotLinkedError_initWithNSString_withNSString_withNSString_(
         @"java.net", @"jre_net ", @"JavaLangSystemClassLoader");
  }
}


JNIEXPORT jclass Java_java_lang_SystemClassLoader_findClass(
      JNIEnv *env, jobject obj, jstring name) {
  return [IOSClass forName:name];
}

// Returns a URL for a specified resource in a bundle, or nil if not found.
static JavaNetURL *getResourceURLFromBundle(NSBundle *bundle, jstring name) {
  NSURL *nativeURL = [bundle URLForResource:name withExtension:nil];
  if (nativeURL) {
    return create_JavaNetURL_initWithNSString_([nativeURL description]);
  }
  return nil;
}

JNIEXPORT jobject Java_java_lang_SystemClassLoader_findResource(
      JNIEnv *env, jobject obj, jstring name) {
  if (!name) {
    return nil;
  }

  JavaNetURL *url = getResourceURLFromBundle([NSBundle mainBundle], name);
  if (url) {
    return url;
  }

  IOSByteArray *data = GetLinkedResource(name);
  if (data) {
    return CreateResourceURL(name, data);
  }

  for (NSBundle *bundle in [NSBundle allFrameworks]) {
    JavaNetURL *url = getResourceURLFromBundle(bundle, name);
    if (url) {
      return url;
    }
  }
  return nil;
}

JNIEXPORT jobject Java_java_lang_SystemClassLoader_findResources(
      JNIEnv *env, jobject obj, jstring name) {
  if (!name) {
    return [JavaUtilCollections emptyEnumeration];
  }
  JavaUtilArrayList *urls = AUTORELEASE([[JavaUtilArrayList alloc] init]);
  for (NSBundle *bundle in [NSBundle allBundles]) {
    NSURL *nativeURL = [bundle URLForResource:name withExtension:nil];
    if (nativeURL) {
      [urls addWithId:create_JavaNetURL_initWithNSString_([nativeURL description])];
    }
  }
  for (NSBundle *bundle in [NSBundle allFrameworks]) {
    NSURL *nativeURL = [bundle URLForResource:name withExtension:nil];
    if (nativeURL) {
      [urls addWithId:create_JavaNetURL_initWithNSString_([nativeURL description])];
    }
  }
  IOSByteArray *data = GetLinkedResource(name);
  if (data) {
    [urls addWithId:CreateResourceURL(name, data)];
  }
  return JavaUtilCollections_enumerationWithJavaUtilCollection_(urls);
}

// Returns an input stream for a specified resource in a bundle, or nil if not found.
static JavaIoBufferedInputStream *getResourceFromBundle(NSBundle *bundle, jstring name) {
  NSString *path = [bundle pathForResource:name ofType:nil];
  if (path) {
    return create_JavaIoBufferedInputStream_initWithJavaIoInputStream_(
        create_JavaIoFileInputStream_initWithNSString_(path));
  }
  return nil;
}

JNIEXPORT jobject Java_java_lang_SystemClassLoader_getResourceAsStream(
      JNIEnv *env, jobject obj, jstring name) {
  if (!name) {
    return nil;
  }
  JavaIoBufferedInputStream *stream = getResourceFromBundle([NSBundle mainBundle], name);
  if (stream) {
    return stream;
  }

  // No iOS resource available, check for linked resource.
  IOSByteArray *data = GetLinkedResource(name);
  if (data) {
    return create_JavaIoByteArrayInputStream_initWithByteArray_(data);
  }
  
  // Check framework bundles for resource.
  for (NSBundle *bundle in [NSBundle allFrameworks]) {
    stream = getResourceFromBundle(bundle, name);
    if (stream) {
      return stream;
    }
  }
  return nil;
}
