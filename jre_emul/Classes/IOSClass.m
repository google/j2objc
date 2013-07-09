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
#import "java/lang/ClassNotFoundException.h"
#import "java/lang/Enum.h"
#import "java/lang/InstantiationException.h"
#import "java/lang/NoSuchFieldException.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/Void.h"
#import "java/lang/annotation/Annotation.h"
#import "java/lang/reflect/Constructor.h"
#import "java/lang/reflect/Field.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "IOSArray.h"
#import "IOSArrayClass.h"
#import "IOSBooleanArray.h"
#import "IOSByteArray.h"
#import "IOSCharArray.h"
#import "IOSDoubleArray.h"
#import "IOSFloatArray.h"
#import "IOSIntArray.h"
#import "IOSLongArray.h"
#import "IOSObjectArray.h"
#import "IOSPrimitiveClass.h"
#import "IOSShortArray.h"
#import "objc/runtime.h"

@implementation IOSClass

@synthesize objcClass = class_;
@synthesize objcProtocol = protocol_;

static NSMutableDictionary *IOSClass_classCache;

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
static NSString *capitalize(NSString *s);

+ (IOSClass *)classWithClass:(Class)cls {
  return [self fetchClass:cls];
}

- (id)initWithClass:(Class)cls {
  if ((self = [super init])) {
    JreMemDebugAdd(self);
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
  return [IOSClass fetchProtocol:protocol];
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
    if ([class_ isSubclassOfClass:[IOSArrayClass class]]) {
      return [IOSClass fetchCachedClass:@"NSObject"];
    }
    Class superclass = [class_ superclass];
    if (superclass != nil) {
      NSString *classKey = NSStringFromClass(superclass);
      IOSClass *clazz = [IOSClass_classCache objectForKey:classKey];
      if (!clazz) {
        clazz = [[IOSClass alloc] initWithClass:[class_ superclass]];
  #if ! __has_feature(objc_arc)
        [clazz autorelease];
  #endif
        [IOSClass_classCache setObject:clazz forKey:classKey];
      }
      return clazz;
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
  return [self getName];
}

- (NSString *)getCanonicalName {
  return [self getName];
}

- (int)getModifiers {
  int mods = protocol_ != nil ? JavaLangReflectModifier_INTERFACE : 0;
  // All Objective-C classes and protocols are public.
  return mods | JavaLangReflectModifier_PUBLIC;
}

// Create a reflection wrapper for an Objective-C selector, updating a map
// keyed by the selector's signature.  The map is necessary to skip methods
// that are overridden (subtypes are added first).
void addMethod(SEL sel, IOSClass *clazz, NSMutableDictionary *map,
               BOOL fetchConstructors) {
  NSString *key = NSStringFromSelector(sel);
  BOOL isConstructor =
      [key isEqualToString:@"init"] || [key hasPrefix:@"initWith"];
  if (isConstructor == fetchConstructors && ![map objectForKey:key]) {
    JavaLangReflectMethod *method =
    [JavaLangReflectMethod methodWithSelector:sel withClass:clazz];
    [map setObject:method forKey:key];
  }
}

void createMethodWrappers(Method *methods,
                          unsigned count,
                          IOSClass* clazz,
                          NSMutableDictionary *map,
                          BOOL fetchConstructors) {
  for (NSUInteger i = 0; i < count; i++) {
    SEL sel = method_getName(methods[i]);
    addMethod(sel, clazz, map, fetchConstructors);
  }
}

// Adds all the methods or constructors for a specified class to
// a specified dictionary.
void getMethodsFromClass(IOSClass *clazz, NSMutableDictionary *methods,
                         BOOL fetchConstructors) {
  unsigned int nInstanceMethods, nClassMethods;
  if (clazz->class_) {
    // Copy first the instance, then the class methods into a combined
    // array of IOSMethod instances.  Method ordering is not defined or
    // important.
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
  } else {
    assert(clazz->protocol_);
    unsigned count;
    struct objc_method_description *descriptions =
        protocol_copyMethodDescriptionList(clazz->protocol_, YES, YES, &count);
    for (unsigned i = 0; i < count; i++) {
      SEL sel = descriptions[i].name;
      addMethod(sel, clazz, methods, fetchConstructors);
    }
    free (descriptions);
  }
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
  return getDeclaredMethods(self, NO);
}

// Return the constructors declared by this class.  Superclass constructors
// are not included.
- (IOSObjectArray *)getDeclaredConstructors {
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
  return getMethods(self, NO);
}

// Return the constructors for this class, including inherited ones.
- (IOSObjectArray *)getConstructors {
  return getMethods(self, YES);
}

// Return a method instance described by a name and an array of
// parameter types.  If the named method isn't a member of the specified
// class, return a superclass method if available.
- (JavaLangReflectMethod *)getMethod:(NSString *)name
                      parameterTypes:(IOSObjectArray *)types {
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
  JavaLangNoSuchMethodException *e = AUTORELEASE(
      [[JavaLangNoSuchMethodException alloc] initWithNSString:name]);
  @throw e;
  return nil;
}

// Return a method instance described by a name and an array of parameter
// types.  Return nil if the named method is not a member of this class.
- (JavaLangReflectMethod *)getDeclaredMethod:(NSString *)name
                              parameterTypes:(IOSObjectArray *)types {
  JavaLangReflectMethod *result = getClassMethod(name, types, self);
  if (!result) {
    @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc]
                        initWithNSString:name]);
  }
  return result;
}

// Look up a method in a specific class.
JavaLangReflectMethod *getClassMethod(NSString *name,
                                      IOSObjectArray *parameterTypes,
                                      IOSClass *cls) {
  nil_chk(name);
  JavaLangReflectMethod *result = nil;
  if (cls->class_) {
    unsigned int n;
    Method *instanceMethods = class_copyMethodList(cls->class_, &n);
    JavaLangReflectMethod *method = findClassMethod(name, parameterTypes,
                                                    cls, instanceMethods, n);
    free(instanceMethods);
    if (!method) {
      Method *classMethods = class_copyMethodList(object_getClass(cls->class_), &n);
      method = findClassMethod(name, parameterTypes, cls, classMethods, n);
      free(classMethods);
    }
    result = method;
  } else {
    assert(cls->protocol_);
    unsigned count;
    struct objc_method_description *descriptions =
        protocol_copyMethodDescriptionList(cls->protocol_, YES, YES, &count);
    for (unsigned i = 0; i < count; i++) {
      SEL sel = descriptions[i].name;
      JavaLangReflectMethod *method =
          [JavaLangReflectMethod methodWithSelector:sel withClass:cls];
      if (methodMatches(name, parameterTypes, method)) {
        result = method;
        break;
      }
    }
    free(descriptions);
  }
  return result;
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

NSString *capitalize(NSString *s) {
  if ([s length] == 0) {
    return s;
  }
  // Only capitalize the first character, as NSString.capitalizedString
  // will make all other characters lowercase.
  NSString *firstChar = [[s substringToIndex:1] capitalizedString];
  return [s stringByReplacingCharactersInRange:NSMakeRange(0, 1)
                                    withString:firstChar];
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
                              capitalize([firstParameterType getName])];
  for (NSUInteger i = 1; i < nParameters; i++) {
    IOSClass *parameterType = (IOSClass *) [parameterTypes objectAtIndex:i];
    translatedName = [translatedName stringByAppendingFormat:@"with%@:",
        capitalize([parameterType getName])];
  }
  return translatedName;
}

- (IOSClass *)getComponentType {
  return nil;
}

JavaLangReflectConstructor *getConstructorImpl(IOSClass *cls,
                                               IOSObjectArray *classes) {
  NSMutableString *name = [@"init" mutableCopy];
#if ! __has_feature(objc_arc)
  [name autorelease];
#endif
  BOOL first = YES;
  for (int i = 0; i < [classes count]; i++) {
    IOSClass *type = [classes objectAtIndex:i];
    if (first) {
      [name appendString:@"With"];
      first = NO;
    } else {
      [name appendString:@"with"];
    }
    NSString *clsName = [type getSimpleName];
    if ([clsName isEqualToString:@"NSObject"]) {
      clsName = @"Id";
    }
    [name appendFormat:@"%@:", clsName];
  }
  
  SEL selector = NSSelectorFromString(name);
  BOOL hasConstructor = NO;
  if (cls->class_) {
    unsigned count;
    Method *instanceMethods = class_copyMethodList(cls->class_, &count);
    for (unsigned i = 0; i < count; i++) {
      SEL signature = method_getName(instanceMethods[i]);
      if (sel_isEqual(selector, signature)) {
        hasConstructor = YES;
        break;
      }
    }
  }
  if (!hasConstructor) {
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
  // Java's getConstructor() only returns the constructor if it's public.
  // However, all constructors in Objective-C are public, so this method
  // is identical to getDeclaredConstructor().
  return getConstructorImpl(self, classes);
}

- (JavaLangReflectConstructor *)getDeclaredConstructor:
    (IOSObjectArray *)classes {
  return getConstructorImpl(self, classes);
}

- (BOOL) isAssignableFrom:(IOSClass *)cls {
  if (class_ != nil) {
    return [cls->class_ isSubclassOfClass:class_];
  } else {
    return cls->class_ != nil
        ? [cls->class_ conformsToProtocol:protocol_]
        : [(id) cls->protocol_ conformsToProtocol:protocol_];
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
  return [NSString stringWithFormat:@"class %@",
          [IOSClass javaToIOSName:[self getSimpleName]]];
}

- (BOOL)isEqual:(id)anObject {
  if (![anObject isKindOfClass:[IOSClass class]]) {
    return NO;
  }
  IOSClass *other = (IOSClass *)anObject;
  if (class_ != nil) {
    return [class_ isEqual:other.objcClass];
  } else {
    return [(id) protocol_ isEqual:other.objcProtocol];
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
static NSArray *IOSClass_primitiveClassNames;

// Convert Java class name to camelcased iOS name.
+ (NSString *)javaToIOSName:(NSString *)className {
  if (!className) {
    @throw AUTORELEASE([[JavaLangNullPointerException alloc] init]);
  }
  if ([IOSClass_primitiveClassNames containsObject:className]) {
    return className;
  }
  if ([className length] >= 2 && [className characterAtIndex:0] == '[') {
    switch ([className characterAtIndex:1]) {
      case 'B': return @"IOSByteArray";
      case 'C': return @"IOSCharArray";
      case 'D': return @"IOSDoubleArray";
      case 'F': return @"IOSFloatArray";
      case 'I': return @"IOSIntArray";
      case 'J': return @"IOSLongArray";
      case 'S': return @"IOSShortArray";
      case 'Z': return @"IOSBooleanArray";
      case 'L': return @"IOSObjectArray";
      case '[': return @"IOSObjectArray";
    }
  }
  NSString *mappedName = [IOSClass_mappedClasses objectForKey:className];
  if (mappedName) {
    return mappedName;
  }
  NSArray *parts = [className componentsSeparatedByString:@"."];
  NSString *iosName = [NSString string];
  for (NSString *part in parts) {
    iosName = [iosName stringByAppendingString:capitalize(part)];
  }
  return iosName;
}

+ (IOSClass *)forName:(NSString *)className {
  if ([IOSClass_primitiveClassNames containsObject:className]) {
    // Primitive types are found using their associated wrapper class's TYPE.
    @throw AUTORELEASE([[JavaLangClassNotFoundException alloc]
                        initWithNSString:className]);
  }
  IOSClass *cls = [self fetchCachedClass:className];
  if (cls) {
    return cls;
  }
  NSString *iosName = [IOSClass javaToIOSName:className];
  if ([iosName isEqualToString:@"IOSObjectArray"]) {
    // Strip leading [ (may be multiple if multi-dimensional,
    NSString *componentName = [className substringFromIndex:1];
    // and leading L if single-dimensional,
    if ([componentName characterAtIndex:0] == 'L') {
      componentName = [componentName substringFromIndex:1];
    }
    // and optional trailing semi-colon.
    int lastChar = [componentName length] - 1;
    if ([componentName characterAtIndex:lastChar] == ';') {
      componentName = [componentName substringToIndex:lastChar];
    }
    IOSClass *componentType = [self forName:componentName];
    cls = AUTORELEASE([[IOSArrayClass alloc]
                       initWithComponentType:componentType]);
    [self addToCache:cls withSignature:className];
    return cls;
  }
  cls = [self fetchCachedClass:iosName];
  if (cls) {
    return cls;
  }
  Class clazz = NSClassFromString(iosName);
  if (clazz) {
    return [self fetchClass:clazz];
  }
  Protocol *protocol = NSProtocolFromString(iosName);
  if (protocol) {
    return [self fetchProtocol:protocol];
  }
  @throw AUTORELEASE([[JavaLangClassNotFoundException alloc] init]);
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
  return NO;
}

- (BOOL)isEnum {
  return class_ != nil && [NSStringFromClass(class_) hasSuffix:@"Enum"];
}

- (BOOL)isInterface {
  return protocol_ != nil;
}

- (BOOL)isPrimitive {
  return NO;  // Overridden by IOSPrimitiveClass.
}

static IOSObjectArray *getClassInterfaces(IOSClass *cls, IOSClass *arrayType) {
  IOSObjectArray *result;
  if (!cls) {
    return[IOSObjectArray arrayWithLength:0 type:arrayType];
  }
  unsigned int outCount;
  Protocol *__unsafe_unretained *interfaces =
      class_copyProtocolList(cls->class_, &outCount);
  result = [IOSObjectArray arrayWithLength:outCount type:arrayType];
  for (unsigned i = 0; i < outCount; i++) {
    [result replaceObjectAtIndex:i
                      withObject:[IOSClass classWithProtocol:interfaces[i]]];
  }
  free(interfaces);
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
  name = [JavaLangReflectField variableName:name];
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
  nil_chk(name);
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
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
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
  nil_chk(name);
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
  JreMemDebugRemove(self);
  [class_ release];
  [protocol_ release];
  class_ = nil;
  protocol_ = nil;
  [super dealloc];
#endif
}

+ (IOSClass *)fetchClass:(Class)cls {
  NSString *classKey = [IOSClass javaToIOSName:NSStringFromClass(cls)];
  IOSClass *clazz = [IOSClass_classCache objectForKey:classKey];
  if (!clazz) {
    clazz = AUTORELEASE([[IOSClass alloc] initWithClass:cls]);
    [IOSClass_classCache setObject:clazz forKey:classKey];
  }
  return clazz;
}

+ (IOSClass *)fetchCachedClass:(NSString *)signature {
  return [IOSClass_classCache objectForKey:signature];
}

+ (IOSClass *)fetchProtocol:(Protocol *)protocol {
  NSString *protocolKey = NSStringFromProtocol(protocol);
  IOSClass *clazz = [IOSClass_classCache objectForKey:protocolKey];
  if (!clazz) {
    clazz = AUTORELEASE([[IOSClass alloc] initWithProtocol:protocol]);
    [IOSClass_classCache setObject:clazz forKey:protocolKey];
  }
  return clazz;
}

+ (void)addToCache:(IOSClass *)clazz withSignature:(NSString *)signature {
  [IOSClass_classCache setObject:clazz forKey:signature];
}

+ (void)initialize {
  // Explicitly mapped classes are defined in Types.initializeTypeMap().
  // If types are added to that method (it's rare) they need to be added here.
  IOSClass_mappedClasses = [[NSDictionary alloc] initWithObjectsAndKeys:
       @"NSObject",  @"java.lang.Object",
       @"IOSClass",  @"java.lang.Class",
       @"NSNumber",  @"java.lang.Number",
       @"NSString",  @"java.lang.String",
       @"NSString",  @"java.lang.CharSequence",
       @"NSCopying", @"java.lang.Cloneable", nil];
  IOSClass_primitiveClassNames = [[NSArray alloc] initWithObjects:
       @"boolean", @"byte", @"char", @"double", @"float",
       @"int", @"long", @"short", @"void", nil];

  // Populate class cache with primitive and primitive array types.
  IOSClass_classCache = [[NSMutableDictionary alloc] init];
  IOSClass *clazz =
      [[IOSPrimitiveClass alloc] initWithName:@"boolean" type:@"Z"];
  [IOSClass_classCache setObject:clazz forKey:@"Z"];
  [IOSClass_classCache setObject:clazz forKey:@"boolean"];
  IOSClass *arrayClazz = [[IOSArrayClass alloc] initWithComponentType:clazz];
  [IOSClass_classCache setObject:arrayClazz forKey:@"[Z"];
  [IOSClass_classCache setObject:arrayClazz forKey:@"IOSBooleanArray"];

  clazz = [[IOSPrimitiveClass alloc] initWithName:@"byte" type:@"B"];
  [IOSClass_classCache setObject:clazz forKey:@"B"];
  [IOSClass_classCache setObject:clazz forKey:@"byte"];
  arrayClazz = [[IOSArrayClass alloc] initWithComponentType:clazz];
  [IOSClass_classCache setObject:arrayClazz forKey:@"[B"];
  [IOSClass_classCache setObject:arrayClazz forKey:@"IOSByteArray"];

  clazz = [[IOSPrimitiveClass alloc] initWithName:@"char" type:@"C"];
  [IOSClass_classCache setObject:clazz forKey:@"C"];
  [IOSClass_classCache setObject:clazz forKey:@"char"];
  arrayClazz = [[IOSArrayClass alloc] initWithComponentType:clazz];
  [IOSClass_classCache setObject:arrayClazz forKey:@"[C"];
  [IOSClass_classCache setObject:arrayClazz forKey:@"IOSCharArray"];

  clazz = [[IOSPrimitiveClass alloc] initWithName:@"double" type:@"D"];
  [IOSClass_classCache setObject:clazz forKey:@"D"];
  [IOSClass_classCache setObject:clazz forKey:@"double"];
  arrayClazz = [[IOSArrayClass alloc] initWithComponentType:clazz];
  [IOSClass_classCache setObject:arrayClazz forKey:@"[D"];
  [IOSClass_classCache setObject:arrayClazz forKey:@"IOSDoubleArray"];

  clazz = [[IOSPrimitiveClass alloc] initWithName:@"float" type:@"F"];
  [IOSClass_classCache setObject:clazz forKey:@"F"];
  [IOSClass_classCache setObject:clazz forKey:@"float"];
  arrayClazz = [[IOSArrayClass alloc] initWithComponentType:clazz];
  [IOSClass_classCache setObject:arrayClazz forKey:@"[F"];
  [IOSClass_classCache setObject:arrayClazz forKey:@"IOSFloatArray"];

  clazz = [[IOSPrimitiveClass alloc] initWithName:@"int" type:@"I"];
  [IOSClass_classCache setObject:clazz forKey:@"int"];
  [IOSClass_classCache setObject:clazz forKey:@"I"];
  arrayClazz = [[IOSArrayClass alloc] initWithComponentType:clazz];
  [IOSClass_classCache setObject:arrayClazz forKey:@"[I"];
  [IOSClass_classCache setObject:arrayClazz forKey:@"IOSIntArray"];

  clazz = [[IOSPrimitiveClass alloc] initWithName:@"long" type:@"J"];
  [IOSClass_classCache setObject:clazz forKey:@"long"];
  [IOSClass_classCache setObject:clazz forKey:@"J"];
  arrayClazz = [[IOSArrayClass alloc] initWithComponentType:clazz];
  [IOSClass_classCache setObject:arrayClazz forKey:@"[J"];
  [IOSClass_classCache setObject:arrayClazz forKey:@"IOSLongArray"];

  clazz = [[IOSPrimitiveClass alloc] initWithName:@"short" type:@"S"];
  [IOSClass_classCache setObject:clazz forKey:@"S"];
  [IOSClass_classCache setObject:clazz forKey:@"short"];
  arrayClazz = [[IOSArrayClass alloc] initWithComponentType:clazz];
  [IOSClass_classCache setObject:arrayClazz forKey:@"[S"];
  [IOSClass_classCache setObject:arrayClazz forKey:@"IOSShortArray"];

  clazz = [[self alloc] initWithClass:[NSObject class]];
  [IOSClass_classCache setObject:clazz forKey:@"java.lang.Object"];
  [IOSClass_classCache setObject:clazz forKey:@"NSObject"];
  arrayClazz = [[IOSArrayClass alloc] initWithComponentType:clazz];
  [IOSClass_classCache setObject:arrayClazz forKey:@"[NSObject"];
  [IOSClass_classCache setObject:arrayClazz forKey:@"IOSObjectArray"];
}

@end
