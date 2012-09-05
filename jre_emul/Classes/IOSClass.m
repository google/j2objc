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

//
//  IOSClass.m
//  JreEmulation
//
//  Created by Tom Ball on 10/18/11.
//

#import "IOSClass.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ClassCastException.h"
#import "java/lang/Enum.h"
#import "java/lang/InstantiationException.h"
#import "java/lang/NoSuchFieldException.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/Void.h"
#import "java/lang/annotation/Annotation.h"
#import "java/lang/reflect/Constructor.h"
#import "java/lang/reflect/Field.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "IOSArray.h"
#import "IOSObjectArray.h"
#import "objc/runtime.h"

@implementation IOSClass

@synthesize objcClass = class_;
@synthesize objcProtocol = protocol_;

// Function forwards.
static JavaLangReflectMethod *getClassMethod(NSString *name,
                                             IOSObjectArray *parameterTypes,
                                             IOSClass *cls);
static JavaLangReflectMethod *findClassMethod(NSString *name,
                                              IOSObjectArray *parameterTypes,
                                              IOSClass *cls,
                                              Method *methodList,
                                              unsigned int nMethods);
static BOOL methodMatches(NSString *name, IOSObjectArray *parameterTypes,
                          JavaLangReflectMethod *method);
static NSString *getTranslatedMethodName(NSString *name,
                                         IOSObjectArray *parameterTypes);

+ (IOSClass *)classWithClass:(Class)cls {
  IOSClass *clazz = [[IOSClass alloc] initWithClass:cls];
#if __has_feature(objc_arc)
  return clazz;
#else
  return [clazz autorelease];
#endif
}

- (id)initWithClass:(Class)cls {
  if ((self = [super init])) {
    class_ = cls;
#if ! __has_feature(objc_arc)
    [class_ retain];
#endif
    protocol_ = nil;
  }
  return self;
}

- (id)initWithProtocol:(Protocol *)protocol {
  if ((self = [super init])) {
    protocol_ = protocol;
#if ! __has_feature(objc_arc)
    [protocol_ retain];
#endif
    class_ = nil;
  }
  return self;
}

+ (IOSClass *)classWithProtocol:(Protocol *)protocol {
  IOSClass *clazz = [[IOSClass alloc] initWithProtocol:protocol];
#if __has_feature(objc_arc)
  return clazz;
#else
  return [clazz autorelease];
#endif
}

- (id)newInstance {
  // Per the JLS spec, throw an InstantiationException if the type is an
  // interface (no class_), array or primitive type (IOSClass types), or void.
  if (!class_ || [class_ isKindOfClass:[IOSClass class]]
      || [class_ isMemberOfClass:[JavaLangVoid class]]) {
    id exception = [[JavaLangInstantiationException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return [[class_ alloc] init];
}

- (IOSClass *)getSuperclass {
  if (class_ != nil) {
    Class superclass = [class_ superclass];
    if (superclass != nil) {
      IOSClass *result = [[IOSClass alloc] initWithClass:[class_ superclass]];
#if ! __has_feature(objc_arc)
      [result autorelease];
#endif
      return result;
    }
  }
  return nil;
}

// Returns true if an object is an instance of this class.
- (BOOL)isInstance:(id)object {
  if (class_ != nil) {
    return [object isKindOfClass:class_];
  } else {
    return [object conformsToProtocol:protocol_];
  }
}

- (NSString *)getName {
  const char *name;
  if (class_ != nil) {
    name = class_getName(class_);
  } else {
    name = protocol_getName(protocol_);
  }
  NSString *result =
      [[NSString alloc] initWithCString:name
                               encoding:[NSString defaultCStringEncoding]];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (NSString *)getSimpleName {
  return NSStringFromClass(class_);
}

- (NSString *)getCanonicalName {
  return [self getName];
}

- (int)getModifiers {
  int mods = protocol_ != nil ? JavaLangReflectModifier_INTERFACE : 0;
  // All Objective-C classes and protocols are public.
  return mods | JavaLangReflectModifier_PUBLIC;
}

// Create reflection wrappers for an Objective-C Method list, updating a map
// keyed by those methods' signatures.  The map is necessary to skip methods
// in superclasses that are overridden.
void createMethodWrappers(Method *methods,
                          unsigned count,
                          IOSClass* clazz,
                          NSMutableDictionary *map,
                          BOOL fetchConstructors) {

  // Copy first the instance, then the class methods into a combined
  // array of IOSMethod instances.  Method ordering is not defined or
  // important.
  for (NSUInteger i = 0; i < count; i++) {
    SEL sel = method_getName(methods[i]);
    NSString *key = NSStringFromSelector(sel);
    BOOL isConstructor =
        [key hasPrefix:@"init"] && ![key isEqualToString:@"initialize"];
    if (isConstructor == fetchConstructors && ![map objectForKey:key]) {
      JavaLangReflectMethod *method =
          [JavaLangReflectMethod methodWithSelector:sel withClass:clazz];
      [map setObject:method forKey:key];
    }
  }
}

// Adds all the methods or constructors for a specified class to
// a specified dictionary.
void getMethodsFromClass(IOSClass *clazz, NSMutableDictionary *methods,
                         BOOL fetchConstructors) {
  unsigned int nInstanceMethods, nClassMethods;
  Method *instanceMethods =
      class_copyMethodList(clazz->class_, &nInstanceMethods);
  createMethodWrappers(instanceMethods, nInstanceMethods, clazz,
                       methods, fetchConstructors);

  Method *classMethods =
      class_copyMethodList(object_getClass(clazz->class_), &nClassMethods);
  createMethodWrappers(classMethods, nClassMethods, clazz,
                       methods, fetchConstructors);

  free(instanceMethods);
  free(classMethods);
}

IOSObjectArray *getDeclaredMethods(IOSClass *clazz, BOOL fetchConstructors) {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  getMethodsFromClass(clazz, methodMap, fetchConstructors);
  IOSClass *methodType =
  [IOSClass classWithClass:[JavaLangReflectMethod class]];
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues]
                                     type:methodType];
}

// Return the class and instance methods declared by this class.  Superclass
// methods are not included.
- (IOSObjectArray *)getDeclaredMethods {
  if (class_ == nil) {
    id exception =
        [[JavaLangAssertionError alloc] initWithNSString:@"not implemented"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }

  return getDeclaredMethods(self, NO);
}

// Return the constructors declared by this class.  Superclass constructors
// are not included.
- (IOSObjectArray *)getDeclaredConstructors {
  if (class_ == nil) {
    id exception =
    [[JavaLangAssertionError alloc] initWithNSString:@"not implemented"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }

  return getDeclaredMethods(self, YES);
}

IOSObjectArray *getMethods(IOSClass *clazz, BOOL fetchConstructors) {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  IOSClass *cls = clazz;
  while (cls) {
    getMethodsFromClass(cls, methodMap, fetchConstructors);
    cls = [cls getSuperclass];
  }
  IOSClass *methodType =
      [IOSClass classWithClass:[JavaLangReflectMethod class]];
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues]
                                     type:methodType];
}

// Return the methods for this class, including inherited methods.
- (IOSObjectArray *)getMethods {
  if (class_ == nil) {
    id exception =
    [[JavaLangAssertionError alloc] initWithNSString:@"not implemented"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return getMethods(self, NO);
}

// Return the constructors for this class, including inherited ones.
- (IOSObjectArray *)getConstructors {
  if (class_ == nil) {
    id exception =
    [[JavaLangAssertionError alloc] initWithNSString:@"not implemented"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return getMethods(self, YES);
}

// Return a method instance described by a name and an array of
// parameter types.  If the named method isn't a member of the specified
// class, return a superclass method if available.
- (JavaLangReflectMethod *)getMethod:(NSString *)name
                      parameterTypes:(IOSObjectArray *)types {
  if (class_ == nil) {
    id exception =
    [[JavaLangAssertionError alloc] initWithNSString:@"not implemented"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }

  JavaLangReflectMethod *method = getClassMethod(name, types, self);
  if (method != nil) {
    return method;
  }
  IOSClass *cls = self;
  while ((cls = [cls getSuperclass]) != nil) {
    method = getClassMethod(name, types, cls);
    if (method != nil) {
      return method;
    }
  }
  return nil;
}

// Return a method instance described by a name and an array of parameter
// types.  Return nil if the named method is not a member of this class.
- (JavaLangReflectMethod *)getDeclaredMethod:(NSString *)name
                              parameterTypes:(IOSObjectArray *)types {
  if (class_ == nil) {
    id exception =
    [[JavaLangAssertionError alloc] initWithNSString:@"not implemented"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  
  return getClassMethod(name, types, self);
}

// Look up a method in a specific class.
JavaLangReflectMethod *getClassMethod(NSString *name,
                                      IOSObjectArray *parameterTypes,
                                      IOSClass *cls) {
  unsigned int n;
  Method *instanceMethods = class_copyMethodList(cls->class_, &n);
  JavaLangReflectMethod *method = findClassMethod(name, parameterTypes,
                                                  cls, instanceMethods, n);
  free(instanceMethods);
  if (method != nil) {
    return method;
  }
  Method *classMethods = class_copyMethodList(object_getClass(cls->class_), &n);
  method = findClassMethod(name, parameterTypes, cls, classMethods, n);
  free(classMethods);
  return method;
}

// Look up a method in a list of Method references.
JavaLangReflectMethod *findClassMethod(NSString *name,
                                       IOSObjectArray *parameterTypes,
                                       IOSClass *cls,
                                       Method *methodList,
                                       unsigned int nMethods) {
  for (NSUInteger i = 0; i < nMethods; i++) {
    SEL sel = method_getName(methodList[i]);
    JavaLangReflectMethod *method =
        [JavaLangReflectMethod methodWithSelector:sel withClass:cls];
    if (methodMatches(name, parameterTypes, method)) {
      return method;
    }
  }
  return nil;
}

// Returns true if a name and set of parameter types matches a method.
BOOL methodMatches(NSString *name, IOSObjectArray *parameterTypes,
                   JavaLangReflectMethod *method) {
  NSString *methodName = [method getName];
  if ([name isEqualToString:methodName]) {
    return YES;
  }
  NSString *translatedName = getTranslatedMethodName(name, parameterTypes);
  return [translatedName isEqualToString:methodName];
}

// Return a method name as it would be modified during j2objc translation.
// The format is "name" with no parameters, "nameWithType:" for one parameter,
// and "nameWithType:withType:..." for multiple parameters.
NSString *getTranslatedMethodName(NSString *name,
                                  IOSObjectArray *parameterTypes) {
  NSUInteger nParameters = [parameterTypes count];
  if (nParameters == 0) {
    return name;
  }
  IOSClass *firstParameterType = (IOSClass *)[parameterTypes objectAtIndex:0];
  NSString *translatedName = [NSString stringWithFormat:@"%@With%@:", name,
                              [firstParameterType getName]];
  for (NSUInteger i = 1; i < nParameters; i++) {
    IOSClass *parameterType = (IOSClass *) [parameterTypes objectAtIndex:i];
    [translatedName stringByAppendingFormat:@"with%@:",
        [parameterType getName]];
  }
  return translatedName;
}

- (IOSClass *)getComponentType {
  return nil;
}

JavaLangReflectConstructor *getConstructorImpl(IOSClass *cls,
                                               IOSObjectArray *classes,
                                               BOOL searchSuperclasses) {
  NSMutableString *name = [@"init" mutableCopy];
#if ! __has_feature(objc_arc)
  [name autorelease];
#endif
  BOOL first = YES;
  for (int i = 0; i < [classes count]; i++) {
    IOSClass *cls = [classes objectAtIndex:i];
    if (first) {
      [name appendString:@"With"];
      first = NO;
    } else {
      [name appendString:@"with"];
    }
    [name appendFormat:@"%@:", [cls getSimpleName]];
  }
  
  SEL selector = NSSelectorFromString(name);
  if (cls != nil && ![cls respondsToSelector:selector] && searchSuperclasses) {
    while (cls != nil) {
      cls = [cls getSuperclass];
      if ([cls respondsToSelector:selector]) {
        break;
      }
    }
  }
  if (cls != nil && ![cls respondsToSelector:selector]) {
    // Either a protocol (Java interface - no constructors) or doesn't have
    // the required constructor.
#if __has_feature(objc_arc)
    @throw [[JavaLangNoSuchMethodException alloc] init];
#else
    @throw [[[JavaLangNoSuchMethodException alloc] init] autorelease];
#endif
  }
  
  return [JavaLangReflectConstructor constructorWithSelector:selector
                                                   withClass:cls];
}

- (JavaLangReflectConstructor *)getConstructor:(IOSObjectArray *)classes {
  return getConstructorImpl(self, classes, YES);
}

- (JavaLangReflectConstructor *)getDeclaredConstructor:
    (IOSObjectArray *)classes {
  return getConstructorImpl(self, classes, NO);
}

- (BOOL) isAssignableFrom:(IOSClass *)cls {
  if (class_ != nil) {
    return [cls->class_ isSubclassOfClass:class_];
  } else {
    return cls->class_ != nil
        ? [cls->class_ conformsToProtocol:protocol_]
        : [cls->protocol_ conformsToProtocol:protocol_];
  }
}

- (IOSClass *)asSubclass:(IOSClass *)cls {
  if (class_ == nil || cls->class_ == nil ||
      ![class_ isSubclassOfClass:cls->class_]) {
#if __has_feature(objc_arc)
    @throw [[JavaLangClassCastException alloc] init];
#else
    @throw [[[JavaLangClassCastException alloc] init] autorelease];
#endif
  }
  return self;
}

- (NSString *)description {
  // matches java.lang.Class.toString() output
  return [NSString stringWithFormat:@"class %@", [self getSimpleName]];
}

- (BOOL)isEqual:(id)anObject {
  if (![anObject isKindOfClass:[IOSClass class]]) {
    return NO;
  }
  IOSClass *other = (IOSClass *)anObject;
  if (class_ != nil) {
    return [class_ isEqual:other.objcClass];
  } else {
    return [protocol_ isEqual:other.objcProtocol];
  }
}

- (NSUInteger)hash {
  if (class_ != nil) {
    return [class_ hash];
  } else {
    return (NSUInteger) &protocol_;
  }
}

- (NSString *)binaryName {
  return [self getName];
}

static NSDictionary *IOSClass_mappedClasses;

// Convert Java class name to camelcased iOS name.
+ (NSString *)camelcaseClassName:(NSString *)className {
  NSString *mappedName = [IOSClass_mappedClasses objectForKey:className];
  if (mappedName) {
    return mappedName;
  }
  NSArray *parts = [className componentsSeparatedByString:@"."];
  NSString *iosName = [NSString string];
  for (NSString *part in parts) {
    // Only capitalize the first character of the class name segment.
    // NSString.capitalizedString will make all other characters lower case.
    NSString *firstChar = [[part substringToIndex:1] capitalizedString];
    NSString *capitalizedPart =
    [part stringByReplacingCharactersInRange:NSMakeRange(0, 1)
                                  withString:firstChar];
    iosName = [iosName stringByAppendingString:capitalizedPart];
  }
  return iosName;
}

+ (IOSClass *)forName:(NSString *)className {
  NSString *iosName = [IOSClass camelcaseClassName:className];
  Class class = NSClassFromString(iosName);
  if (class) {
    return [IOSClass classWithClass:class];
  }
  Protocol *protocol = NSProtocolFromString(iosName);
  if (protocol) {
    return [IOSClass classWithProtocol:protocol];
  }
  return nil;
}

+ (IOSClass *)forName:(NSString *)className
           initialize:(BOOL)load
          classLoader:(id)loader {
  return [IOSClass forName:className];
}

- (id)cast:(id)throwable {
  // There's no need to actually cast this here, as the translator will add
  // a C cast since the return type is a type variable.
  return throwable;
}

- (IOSClass *)getEnclosingClass {
  NSString *className = NSStringFromClass(class_);
  NSRange r = [className rangeOfString:@"_" options:NSBackwardsSearch];
  if (r.location == NSNotFound) {
    return nil;
  }
  NSString *enclosingName = [className substringToIndex:r.location];
  Class class = NSClassFromString(enclosingName);
  return class ? [IOSClass classWithClass:class] : nil;
}

- (BOOL)isArray {
  return [class_ isSubclassOfClass:[IOSArray class]];
}

- (BOOL)isEnum {
  return class_ != nil && [NSStringFromClass(class_) hasSuffix:@"Enum"];
}

- (BOOL)isInterface {
  return protocol_ != nil;
}

- (BOOL)isPrimitive {
  //TODO(user): implement when getComponentType is implemented.
  return NO;
}

static IOSObjectArray *getClassInterfaces(IOSClass *cls, IOSClass *arrayType) {
  IOSObjectArray *result;
  if (!cls) {
    return[IOSObjectArray arrayWithLength:0 type:arrayType];
  }
  unsigned int outCount;
  Protocol **interfaces = class_copyProtocolList(cls.class, &outCount);
  result = [IOSObjectArray arrayWithLength:outCount type:arrayType];
  for (unsigned i = 0; i < outCount; i++) {
    [result replaceObjectAtIndex:i
                      withObject:[IOSClass classWithProtocol:interfaces[i]]];
  }
  return result;
}

- (IOSObjectArray *)getInterfaces {
  return getClassInterfaces(self, [IOSClass classWithClass:[IOSClass class]]);
}

- (IOSObjectArray *)getGenericInterfaces {
  return getClassInterfaces(self,
      [IOSClass classWithProtocol:objc_getProtocol("JavaLangReflectType")]);
}

- (IOSObjectArray *)getTypeParameters {
  IOSClass *typeVariableClass = [IOSClass
      classWithProtocol:objc_getProtocol("JavaLangReflectTypeVariable")];
  return [IOSObjectArray arrayWithLength:0 type:typeVariableClass];
}

// Annotations aren't available, so stub out annotation-related methods.
- (JavaLangAnnotationAnnotation *)getAnnotation:(IOSClass *)annotationClass {
  return nil;
}

- (BOOL)isAnnotationPresent:(IOSClass *)annotationClass {
  return NO;
}

- (IOSObjectArray *)getAnnotations {
  IOSClass *arrayType = [IOSClass classWithProtocol:objc_getProtocol(
      "JavaLangAnnotationAnnotation")];
  return[IOSObjectArray arrayWithLength:0 type:arrayType];
}

- (IOSObjectArray *)getDeclaredAnnotations {
  return [self getAnnotations];
}

- (id)getPackage {
  // No packages in Objective-C, but are included in class names.
  return nil;
}

- (id)getClassLoader {
  //TODO(user): enable when there is classloader support.
  return nil;
}

JavaLangReflectField *getFieldFromClass(NSString *name, Class clazz) {
  const char* cname =
      [name cStringUsingEncoding:[NSString defaultCStringEncoding]];
  Ivar ivar = class_getInstanceVariable(clazz, cname);
  if (ivar) {
    return [JavaLangReflectField fieldWithName:name
                                     withClass:[IOSClass classWithClass:clazz]];
  }
  return nil;
}

JavaLangReflectField *getFieldFromIvar(Ivar ivar, Class clazz) {
  return [JavaLangReflectField fieldWithIvar:ivar
                                   withClass:[IOSClass classWithClass:clazz]];
}

// Adds all the fields for a specified class to a specified dictionary.
void getFieldsFromClass(Class clazz, NSMutableDictionary *fields) {
  unsigned int count;
  Ivar *ivars = class_copyIvarList(clazz, &count);
  for (unsigned int i = 0; i < count; i++) {
    JavaLangReflectField *field = getFieldFromIvar(ivars[i], clazz);
    NSString *name = [field getName];
    if (![fields valueForKey:name]) { // Don't adding shadowed fields.
      [fields setObject:field forKey:name];
    }
  }
  free(ivars);
}

- (JavaLangReflectField *)getDeclaredField:(NSString *)name {
  NIL_CHK(name);
  JavaLangReflectField *field = nil;
  id exception = nil;
  if (class_) {
    field = getFieldFromClass(name, class_);
  } else {
    // TODO(user): add support for interface constants.
    exception = [[JavaLangAssertionError alloc]
                 initWithNSString:@"interface constants not implemented"];
  }
  if (!field) {
    exception = [[JavaLangNoSuchFieldException alloc] initWithNSString:name];
  }
  if (exception) {
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return field;
}

- (JavaLangReflectField *)getField:(NSString *)name {
  NIL_CHK(name);
  JavaLangReflectField *field = nil;
  Class cls = class_;
  while (cls) {
    field = getFieldFromClass(name, cls);
    if (field) {
      return field;
    }
    cls = class_getSuperclass(cls);
  }
  if (!field) {
    id exception = [[JavaLangNoSuchFieldException alloc] initWithNSString:name];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return field;
}

IOSObjectArray *copyFieldsToObjectArray(NSArray *fields) {
  NSUInteger count = [fields count];
  IOSClass *fieldType = [IOSClass classWithClass:[JavaLangReflectField class]];
  IOSObjectArray *results = [IOSObjectArray arrayWithLength:count
                                                       type:fieldType];
  for (NSUInteger i = 0; i < count; i++) {
    [results replaceObjectAtIndex:i withObject:[fields objectAtIndex:i]];
  }
  return results;
}

- (IOSObjectArray *)getDeclaredFields {
  NSMutableDictionary *fieldDictionary = [NSMutableDictionary dictionary];
  getFieldsFromClass(class_, fieldDictionary);
  return copyFieldsToObjectArray([fieldDictionary allValues]);
}

- (IOSObjectArray *)getFields {
  NSMutableDictionary *fieldDictionary = [NSMutableDictionary dictionary];
  Class cls = class_;
  while (cls) {
    getFieldsFromClass(cls, fieldDictionary);
    cls = class_getSuperclass(cls);
  }
  return copyFieldsToObjectArray([fieldDictionary allValues]);
}

- (JavaLangReflectMethod *)getEnclosingMethod {
  return nil;  // Classes aren't enclosed in Objective-C.
}

- (JavaLangReflectMethod *)getEnclosingConstructor {
  return nil;  // Classes aren't enclosed in Objective-C.
}

- (BOOL)isAnonymousClass {
  return class_ ? strchr(class_getName(class_), '?') != NULL : NO;
}

- (BOOL)desiredAssertionStatus {
  return false;
}

- (IOSObjectArray *)getEnumConstants {
  if ([self isEnum]) {
    return [JavaLangEnum getValuesWithIOSClass:self];
  }
  return nil;
}

- (void)dealloc {
#if ! __has_feature(objc_arc)
  [class_ release];
  [protocol_ release];
  class_ = nil;
  protocol_ = nil;
  [super dealloc];
#endif
}

+ (void)initialize {
  // Explicitly mapped classes are defined in Types.initializeTypeMap().
  // If types are added to that method (it's rare) they need to be added here.
  IOSClass_mappedClasses =
      [NSDictionary dictionaryWithObjectsAndKeys:
       @"NSObject",  @"java.lang.Object",
       @"IOSClass",  @"java.lang.Class",
       @"NSNumber",  @"java.lang.Number",
       @"NSString",  @"java.lang.String",
       @"NSString",  @"java.lang.CharSequence",
       @"NSCopying", @"java.lang.Cloneable", nil];
}

@end
