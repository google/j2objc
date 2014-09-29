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
#import "java/lang/ClassLoader.h"
#import "java/lang/ClassNotFoundException.h"
#import "java/lang/Enum.h"
#import "java/lang/InstantiationException.h"
#import "java/lang/NoSuchFieldException.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/Package.h"
#import "java/lang/annotation/Annotation.h"
#import "java/lang/annotation/Inherited.h"
#import "java/lang/reflect/Constructor.h"
#import "java/lang/reflect/Field.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "java/util/Properties.h"
#import "IOSArrayClass.h"
#import "IOSConcreteClass.h"
#import "IOSMappedClass.h"
#import "IOSObjectArray.h"
#import "IOSPrimitiveArray.h"
#import "IOSPrimitiveClass.h"
#import "IOSProtocolClass.h"
#import "JavaMetadata.h"
#import "NSNumber+JavaNumber.h"
#import "NSObject+JavaObject.h"
#import "NSString+JavaString.h"
#import "objc/message.h"
#import "objc/runtime.h"

@implementation IOSClass

static NSDictionary *IOSClass_mappedClasses;

// Primitive class instances.
static IOSPrimitiveClass *IOSClass_byteClass;
static IOSPrimitiveClass *IOSClass_charClass;
static IOSPrimitiveClass *IOSClass_doubleClass;
static IOSPrimitiveClass *IOSClass_floatClass;
static IOSPrimitiveClass *IOSClass_intClass;
static IOSPrimitiveClass *IOSClass_longClass;
static IOSPrimitiveClass *IOSClass_shortClass;
static IOSPrimitiveClass *IOSClass_booleanClass;
static IOSPrimitiveClass *IOSClass_voidClass;

// Other commonly used instances.
static IOSClass *IOSClass_objectClass;
static IOSClass *IOSClass_stringClass;

// Function forwards.
static IOSClass *FetchClass(Class cls);
static IOSClass *FetchProtocol(Protocol *protocol);
static IOSClass *FetchArray(IOSClass *componentType);

#define PREFIX_MAPPING_RESOURCE @"prefixes.properties"

// Package to prefix mappings, initialized in FindMappedClass().
static JavaUtilProperties *prefixMapping;

- (instancetype)init {
  if ((self = [super init])) {
    JreMemDebugAdd(self);
  }
  return self;
}

- (Class)objcClass {
  return nil;
}

- (Protocol *)objcProtocol {
  return nil;
}

+ (IOSClass *)classWithClass:(Class)cls {
  return FetchClass(cls);
}

+ (IOSClass *)classWithProtocol:(Protocol *)protocol {
  return FetchProtocol(protocol);
}

+ (IOSClass *)arrayClassWithComponentType:(IOSClass *)componentType {
  return FetchArray(componentType);
}

+ (IOSClass *)byteClass {
  return IOSClass_byteClass;
}

+ (IOSClass *)charClass {
  return IOSClass_charClass;
}

+ (IOSClass *)doubleClass {
  return IOSClass_doubleClass;
}

+ (IOSClass *)floatClass {
  return IOSClass_floatClass;
}

+ (IOSClass *)intClass {
  return IOSClass_intClass;
}

+ (IOSClass *)longClass {
  return IOSClass_longClass;
}

+ (IOSClass *)shortClass {
  return IOSClass_shortClass;
}

+ (IOSClass *)booleanClass {
  return IOSClass_booleanClass;
}

+ (IOSClass *)voidClass {
  return IOSClass_voidClass;
}

+ (IOSClass *)objectClass {
  return IOSClass_objectClass;
}

+ (IOSClass *)stringClass {
  return IOSClass_stringClass;
}

- (id)newInstance {
  // Per the JLS spec, throw an InstantiationException if the type is an
  // interface (no class_), array or primitive type (IOSClass types), or void.
  @throw AUTORELEASE([[JavaLangInstantiationException alloc] init]);
}

- (IOSClass *)getSuperclass {
  return nil;
}

- (id<JavaLangReflectType>)getGenericSuperclass {
  return nil;
}

// Returns true if an object is an instance of this class.
- (BOOL)isInstance:(id)object {
  return NO;
}

- (NSString *)getName {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:
      @"abstract method not overridden"]);
}

- (NSString *)getSimpleName {
  return [self getName];
}

- (NSString *)getCanonicalName {
  return [self getName];
}

- (NSString *)objcName {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:
      @"abstract method not overridden"]);
}

- (int)getModifiers {
  JavaClassMetadata *metadata = [self getMetadata];
  if (metadata) {
    return metadata.modifiers & [JavaLangReflectModifier classModifiers];
  } else {
    // All Objective-C classes and protocols are public by default.
    return JavaLangReflectModifier_PUBLIC;
  }
}

// Returns all methods defined in a class. Methods that aren't defined by the
// original Java class are ignored.
- (void)collectMethods:(NSMutableDictionary *)methodMap
            publicOnly:(BOOL)publicOnly {
  // Overridden by subclasses.
}

// Return the class and instance methods declared by the Java class.  Superclass
// methods are not included.
- (IOSObjectArray *)getDeclaredMethods {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  [self collectMethods:methodMap publicOnly:NO];
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues] type:
      FetchClass([JavaLangReflectMethod class])];
}

// Return the constructors declared by this class.  Superclass constructors
// are not included.
- (IOSObjectArray *)getDeclaredConstructors {
  return [IOSObjectArray arrayWithLength:0 type:
      FetchClass([JavaLangReflectConstructor class])];
}

// Return the methods for this class, including inherited methods.
- (IOSObjectArray *)getMethods {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  IOSClass *cls = self;
  while (cls) {
    [cls collectMethods:methodMap publicOnly:YES];
    cls = [cls getSuperclass];
  }
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues] type:
      FetchClass([JavaLangReflectMethod class])];
}

// Return the constructors for this class, including inherited ones.
- (IOSObjectArray *)getConstructors {
  return [IOSObjectArray arrayWithLength:0 type:
      FetchClass([JavaLangReflectConstructor class])];
}

// Return a method instance described by a name and an array of
// parameter types.  If the named method isn't a member of the specified
// class, return a superclass method if available.
- (JavaLangReflectMethod *)getMethod:(NSString *)name
                      parameterTypes:(IOSObjectArray *)types {
  NSString *translatedName = IOSClass_GetTranslatedMethodName(name, types);
  JavaLangReflectMethod *method = [self findMethodWithTranslatedName:translatedName];
  if (method != nil) {
    return method;
  }
  IOSClass *cls = self;
  while ((cls = [cls getSuperclass]) != nil) {
    method = [cls findMethodWithTranslatedName:translatedName];
    if (method != nil) {
      return method;
    }
  }
  for (IOSProtocolClass *p in [self getInterfacesWithArrayType:FetchClass([IOSClass class])]) {
    method = [p findMethodWithTranslatedName:translatedName];
    if (method != nil) {
      return method;
    }
  }
  @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] initWithNSString:name]);
}

// Return a method instance described by a name and an array of parameter
// types.  Return nil if the named method is not a member of this class.
- (JavaLangReflectMethod *)getDeclaredMethod:(NSString *)name
                              parameterTypes:(IOSObjectArray *)types {
  JavaLangReflectMethod *result =
      [self findMethodWithTranslatedName:IOSClass_GetTranslatedMethodName(name, types)];
  if (!result) {
    @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] initWithNSString:name]);
  }
  return result;
}

- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName {
  return nil; // Overriden by subclasses.
}

static NSString *Capitalize(NSString *s) {
  if ([s length] == 0) {
    return s;
  }
  // Only capitalize the first character, as NSString.capitalizedString
  // will make all other characters lowercase.
  NSString *firstChar = [[s substringToIndex:1] capitalizedString];
  return [s stringByReplacingCharactersInRange:NSMakeRange(0, 1)
                                    withString:firstChar];
}

static NSString *GetParameterKeyword(IOSClass *paramType) {
  if (paramType == IOSClass_objectClass) {
    return @"Id";
  }
  return Capitalize([paramType objcName]);
}

// Return a method name as it would be modified during j2objc translation.
// The format is "name" with no parameters, "nameWithType:" for one parameter,
// and "nameWithType:withType:..." for multiple parameters.
NSString *IOSClass_GetTranslatedMethodName(NSString *name, IOSObjectArray *parameterTypes) {
  nil_chk(name);
  jint nParameters = parameterTypes ? parameterTypes->size_ : 0;
  if (nParameters == 0) {
    return name;
  }
  IOSClass *firstParameterType = parameterTypes->buffer_[0];
  NSMutableString *translatedName = [NSMutableString stringWithCapacity:128];
  [translatedName appendFormat:@"%@With%@:", name, GetParameterKeyword(firstParameterType)];
  for (jint i = 1; i < nParameters; i++) {
    IOSClass *parameterType = parameterTypes->buffer_[i];
    [translatedName appendFormat:@"with%@:", GetParameterKeyword(parameterType)];
  }
  return translatedName;
}

- (IOSClass *)getComponentType {
  return nil;
}

- (JavaLangReflectConstructor *)getConstructor:(IOSObjectArray *)parameterTypes {
  // Java's getConstructor() only returns the constructor if it's public.
  // However, all constructors in Objective-C are public, so this method
  // is identical to getDeclaredConstructor().
  @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] init]);
}

- (JavaLangReflectConstructor *)getDeclaredConstructor:(IOSObjectArray *)parameterTypes {
  @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] init]);
}

- (BOOL)isAssignableFrom:(IOSClass *)cls {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:
      @"abstract method not overridden"]);
}

- (IOSClass *)asSubclass:(IOSClass *)cls {
  @throw AUTORELEASE([[JavaLangClassCastException alloc] init]);
}

- (NSString *)description {
  // matches java.lang.Class.toString() output
  return [NSString stringWithFormat:@"class %@", [self getName]];
}

- (NSString *)binaryName {
  NSString *name = [self getName];
  return [NSString stringWithFormat:@"L%@;", name];
}

// Convert Java class name to camelcased iOS name.
static NSString *IOSClass_JavaToIOSName(NSString *javaName) {
  NSString *mappedName = [IOSClass_mappedClasses objectForKey:javaName];
  if (mappedName) {
    return mappedName;
  }
  NSArray *parts = [javaName componentsSeparatedByString:@"."];
  NSMutableString *iosName = [NSMutableString string];
  if ([parts count] == 1) {
    [iosName appendString:[parts objectAtIndex:0]];
  } else {
    id lastPart = [parts lastObject];
    for (NSString *part in parts) {
      if (part != lastPart) {
        part = Capitalize(part);
      }
      [iosName appendString:part];
    }
  }
  return iosName;
}

static IOSClass *ClassForIosName(NSString *iosName) {
  nil_chk(iosName);
  // Some protocols have a sibling class that contains the metadata and any
  // constants that are defined. We must look for the protocol before the class
  // to ensure we create a IOSProtocolClass for such cases. NSObject must be
  // special-cased because it also has a protocol but we want to return an
  // IOSConcreteClass instance for it.
  if ([iosName isEqualToString:@"NSObject"]) {
    return [IOSClass objectClass];
  }
  Protocol *protocol = NSProtocolFromString(iosName);
  if (protocol) {
    return FetchProtocol(protocol);
  }
  Class clazz = NSClassFromString(iosName);
  if (clazz) {
    return FetchClass(clazz);
  }
  return nil;
}

static IOSClass *FindMappedClass(NSString *name) {
  static dispatch_once_t once;
  dispatch_once(&once, ^{
    prefixMapping = [[JavaUtilProperties alloc] init];
    JavaIoInputStream *prefixesResource =
        [IOSClass_objectClass getResourceAsStream:PREFIX_MAPPING_RESOURCE];
    if (prefixesResource) {
      [prefixMapping load__WithJavaIoInputStream:prefixesResource];
    }
  });
  NSRange lastDot = [name rangeOfString:@"." options:NSBackwardsSearch];
  if (lastDot.location == NSNotFound) {
    return nil;   // No package in class name.
  }
  NSString *package = [name substringToIndex:lastDot.location];
  NSString *prefix = [prefixMapping getPropertyWithNSString:package];
  if (!prefix) {
    return nil;   // No prefix for package.
  }
  NSString *mappedName =
      [prefix stringByAppendingString:[name substringFromIndex:lastDot.location + 1]];
  mappedName = [mappedName stringByReplacingOccurrencesOfString:@"$" withString:@"_"];
  return ClassForIosName(mappedName);
}

+ (IOSClass *)classForIosName:(NSString *)iosName {
  return ClassForIosName(iosName);
}

static IOSClass *ClassForJavaName(NSString *name) {
  IOSClass *cls = ClassForIosName(IOSClass_JavaToIOSName(name));
  if (!cls && [name indexOf:'$'] >= 0) {
    name = [name stringByReplacingOccurrencesOfString:@"$" withString:@"_"];
    cls = ClassForIosName(IOSClass_JavaToIOSName(name));
  }
  if (!cls) {
    cls = FindMappedClass(name);
  }
  return cls;
}

static IOSClass *IOSClass_PrimitiveClassForChar(unichar c) {
  switch (c) {
    case 'B': return IOSClass_byteClass;
    case 'C': return IOSClass_charClass;
    case 'D': return IOSClass_doubleClass;
    case 'F': return IOSClass_floatClass;
    case 'I': return IOSClass_intClass;
    case 'J': return IOSClass_longClass;
    case 'S': return IOSClass_shortClass;
    case 'Z': return IOSClass_booleanClass;
    case 'V': return IOSClass_voidClass;
    default: return nil;
  }
}

+ (IOSClass *)primitiveClassForChar:(unichar)c {
  return IOSClass_PrimitiveClassForChar(c);
}

static IOSClass *IOSClass_ArrayClassForName(NSString *name, NSUInteger index) {
  IOSClass *componentType = nil;
  unichar c = [name characterAtIndex:index];
  switch (c) {
    case 'L':
      {
        NSUInteger length = [name length];
        if ([name characterAtIndex:length - 1] == ';') {
          componentType = ClassForJavaName(
              [name substringWithRange:NSMakeRange(index + 1, length - index - 2)]);
        }
        break;
      }
    case '[':
      componentType = IOSClass_ArrayClassForName(name, index + 1);
      break;
    default:
      if ([name length] == index + 1) {
        componentType = IOSClass_PrimitiveClassForChar(c);
      }
      break;
  }
  if (componentType) {
    return FetchArray(componentType);
  }
  return nil;
}

IOSClass *IOSClass_forNameWithNSString_(NSString *className) {
  nil_chk(className);
  IOSClass *iosClass = nil;
  if ([className length] > 0) {
    if ([className characterAtIndex:0] == '[') {
      iosClass = IOSClass_ArrayClassForName(className, 1);
    } else {
      iosClass = ClassForJavaName(className);
    }
  }
  if (!iosClass) {
    // See if it's an enum.
    iosClass = ClassForJavaName([className stringByAppendingString:@"Enum"]);
  }
  if (iosClass) {
    return iosClass;
  }
  @throw AUTORELEASE([[JavaLangClassNotFoundException alloc] initWithNSString:className]);
}

+ (IOSClass *)forName:(NSString *)className {
  return IOSClass_forNameWithNSString_(className);
}

IOSClass *IOSClass_forNameWithNSString_withBoolean_withJavaLangClassLoader_(
    NSString *className, BOOL load, JavaLangClassLoader *loader) {
  return IOSClass_forNameWithNSString_(className);
}

+ (IOSClass *)forName:(NSString *)className
           initialize:(BOOL)load
          classLoader:(JavaLangClassLoader *)loader {
  return IOSClass_forNameWithNSString_withBoolean_withJavaLangClassLoader_(className, load, loader);
}

- (id)cast:(id)throwable {
  // There's no need to actually cast this here, as the translator will add
  // a C cast since the return type is a type variable.
  return throwable;
}

- (IOSClass *)getEnclosingClass {
  JavaClassMetadata *metadata = [self getMetadata];
  if (!metadata || !metadata.enclosingName) {
    return nil;
  }
  NSMutableString *qName = [NSMutableString string];
  if (metadata.packageName) {
    [qName appendString:metadata.packageName];
    [qName appendString:@"."];
  }
  [qName appendString:metadata.enclosingName];
  return ClassForJavaName(qName);
}

- (BOOL)isArray {
  return NO;
}

- (BOOL)isEnum {
  return NO;
}

- (BOOL)isInterface {
  return NO;
}

- (BOOL)isPrimitive {
  return NO;  // Overridden by IOSPrimitiveClass.
}

static BOOL hasModifier(IOSClass *cls, int flag) {
  JavaClassMetadata *metadata = [cls getMetadata];
  return metadata ? (metadata.modifiers & flag) > 0 : NO;
}

- (BOOL)isAnnotation {
  return hasModifier(self, JavaLangReflectModifier_ANNOTATION);
}

- (BOOL)isMemberClass {
  JavaClassMetadata *metadata = [self getMetadata];
  return metadata && metadata.enclosingName && ![self isAnonymousClass];
}

- (BOOL)isLocalClass {
  return [self getEnclosingMethod] && ![self isAnonymousClass];
}

- (BOOL)isSynthetic {
  return hasModifier(self, JavaLangReflectModifier_SYNTHETIC);
}

- (IOSObjectArray *)getInterfacesWithArrayType:(IOSClass *)arrayType {
  return [IOSObjectArray arrayWithLength:0 type:arrayType];
}

- (IOSObjectArray *)getInterfaces {
  return [self getInterfacesWithArrayType:FetchClass([IOSClass class])];
}

- (IOSObjectArray *)getGenericInterfaces {
  return [self getInterfacesWithArrayType:FetchProtocol(@protocol(JavaLangReflectType))];
}

- (IOSObjectArray *)getTypeParameters {
  IOSClass *typeVariableClass = [IOSClass
      classWithProtocol:objc_getProtocol("JavaLangReflectTypeVariable")];
  return [IOSObjectArray arrayWithLength:0 type:typeVariableClass];
}

- (id)getAnnotationWithIOSClass:(IOSClass *)annotationClass {
  nil_chk(annotationClass);
  IOSObjectArray *annotations = [self getAnnotations];
  jint n = annotations->size_;
  for (jint i = 0; i < n; i++) {
    id annotation = annotations->buffer_[i];
    if ([annotationClass isInstance:annotation]) {
      return annotation;
    }
  }
  return nil;
}

- (BOOL)isAnnotationPresentWithIOSClass:(IOSClass *)annotationClass {
  return [self getAnnotationWithIOSClass:annotationClass] != nil;
}

- (IOSObjectArray *)getAnnotations {
  NSMutableArray *array = [[NSMutableArray alloc] init];
  IOSObjectArray *declared = [self getDeclaredAnnotations];
  for (jint i = 0; i < declared->size_; i++) {
    [array addObject:declared->buffer_[i]];
  }

  // Check for any inherited annotations.
  IOSClass *cls = [self getSuperclass];
  IOSClass *inheritedAnnotation = [JavaLangAnnotationInherited getClass];
  while (cls) {
    IOSObjectArray *declared = [cls getDeclaredAnnotations];
    for (jint i = 0; i < declared->size_; i++) {
      id<JavaLangAnnotationAnnotation> annotation = declared->buffer_[i];
      IOSObjectArray *attributes = [[annotation getClass] getDeclaredAnnotations];
      for (jint j = 0; j < attributes->size_; j++) {
        id<JavaLangAnnotationAnnotation> attribute = attributes->buffer_[j];
        if (inheritedAnnotation == [attribute getClass]) {
          [array addObject:annotation];
        }
      }
    }
    cls = [cls getSuperclass];
  }
  IOSClass *annotationType = [IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)];
  IOSObjectArray *result = [IOSObjectArray arrayWithNSArray:array type:annotationType];
#if ! __has_feature(objc_arc)
  [array release];
#endif
  return result;
}

- (IOSObjectArray *)getDeclaredAnnotations {
  Class cls = self.objcClass;
  if (cls) {
    Method annotationsMethod = JreFindClassMethod(cls, "__annotations");
    if (annotationsMethod) {
      return method_invoke(cls, annotationsMethod);
    }
  }
  IOSClass *annotationType = [IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)];
  return [IOSObjectArray arrayWithLength:0 type:annotationType];
}

// Returns the metadata structure defined by this class, if it exists.
- (JavaClassMetadata *)getMetadata {
  Class cls = [self objcClass];
  if (cls) {
    // Can't use respondsToSelector here because that will search superclasses.
    Method metadataMethod = JreFindClassMethod(cls, "__metadata");
    if (metadataMethod) {
      J2ObjcClassInfo *rawData = (ARCBRIDGE J2ObjcClassInfo *) method_invoke(cls, metadataMethod);
      return AUTORELEASE([[JavaClassMetadata alloc] initWithMetadata:rawData]);
    }
  }
  return nil;
}

- (id)getPackage {
  JavaClassMetadata *metadata = [self getMetadata];
  if (metadata) {
    return AUTORELEASE([[JavaLangPackage alloc] initWithNSString:metadata.packageName
                                                    withNSString:nil
                                                    withNSString:nil
                                                    withNSString:nil
                                                    withNSString:nil
                                                    withNSString:nil
                                                    withNSString:nil
                                                  withJavaNetURL:nil]);
  }
  return nil;
}

- (id)getClassLoader {
  return [JavaLangClassLoader getSystemClassLoader];
}

static const char* GetFieldName(NSString *name, IOSClass *clazz) {
  const char *cname = [name UTF8String];
  JavaFieldMetadata *fieldMetadata = [[clazz getMetadata] findFieldMetadata:cname];
  if (fieldMetadata) {
    return [[fieldMetadata name] UTF8String];
  }
  name = [JavaLangReflectField variableName:name];
  return [name cStringUsingEncoding:[NSString defaultCStringEncoding]];
}

static JavaLangReflectField *FieldFromIvar(IOSClass *iosClass, Ivar ivar) {
  JavaClassMetadata *metadata = [iosClass getMetadata];
  JavaFieldMetadata *fieldMetadata = [metadata findFieldMetadata:ivar_getName(ivar)];
  return [JavaLangReflectField fieldWithIvar:ivar
                                   withClass:iosClass
                                withMetadata:fieldMetadata];
}

// Adds all the fields for a specified class to a specified dictionary.
static void GetFieldsFromClass(IOSClass *iosClass, NSMutableDictionary *fields) {
  JavaClassMetadata *metadata = [iosClass getMetadata];
  if (metadata) {
    IOSObjectArray *infos = [metadata allFields];
    for (jint i = 0; i < infos->size_; i++) {
      JavaFieldMetadata *fieldMeta = [infos objectAtIndex:i];
      Ivar ivar = class_getInstanceVariable(iosClass.objcClass, [[fieldMeta iosName] UTF8String]);
      JavaLangReflectField *field = [JavaLangReflectField fieldWithIvar:ivar
                                                              withClass:iosClass
                                                           withMetadata:fieldMeta];
      NSString *name = [field getName];
      if (![fields valueForKey:name]) {
        [fields setObject:field forKey:name];
      }
    };
  } else {
    unsigned int count;
    Ivar *ivars = class_copyIvarList(iosClass.objcClass, &count);
    for (unsigned int i = 0; i < count; i++) {
      JavaLangReflectField *field = FieldFromIvar(iosClass, ivars[i]);
      NSString *name = [field getName];
      if (![fields valueForKey:name]) { // Don't add shadowed fields.
        [fields setObject:field forKey:name];
      }
    }
    free(ivars);
  }
}

- (JavaLangReflectField *)getDeclaredField:(NSString *)name {
  nil_chk(name);
  Class cls = self.objcClass;
  if (cls) {
    Ivar ivar = class_getInstanceVariable(cls, GetFieldName(name, self));
    if (ivar) {
      return FieldFromIvar(self, ivar);
    }

    JavaClassMetadata *metadata = [self getMetadata];
    if (metadata) {
      JavaFieldMetadata *fieldMeta = [metadata findFieldMetadata:[name UTF8String]];
      if (fieldMeta) {
        ivar = class_getInstanceVariable(cls, [[fieldMeta iosName] UTF8String]);
        return [JavaLangReflectField fieldWithIvar:ivar
                                         withClass:self
                                      withMetadata:fieldMeta];
      }
    }
  }
  @throw AUTORELEASE([[JavaLangNoSuchFieldException alloc] initWithNSString:name]);
}

- (JavaLangReflectField *)getField:(NSString *)name {
  nil_chk(name);
  const char *objcName = GetFieldName(name, self);
  IOSClass *iosClass = self;
  Class cls = nil;
  while (iosClass && (cls = iosClass.objcClass)) {
    Ivar ivar = class_getInstanceVariable(cls, objcName);
    if (ivar) {
      return FieldFromIvar(self, ivar);
    }

    JavaClassMetadata *metadata = [self getMetadata];
    if (metadata) {
      JavaFieldMetadata *fieldMeta = [metadata findFieldMetadata:[name UTF8String]];
      if (fieldMeta) {
        ivar = class_getInstanceVariable(cls, [[fieldMeta iosName] UTF8String]);
        return [JavaLangReflectField fieldWithIvar:ivar
                                         withClass:self
                                      withMetadata:fieldMeta];
      }
    }
    iosClass = [iosClass getSuperclass];
  }
  @throw AUTORELEASE([[JavaLangNoSuchFieldException alloc] initWithNSString:name]);
}

IOSObjectArray *copyFieldsToObjectArray(NSArray *fields) {
  jint count = (jint)[fields count];
  IOSClass *fieldType = [IOSClass classWithClass:[JavaLangReflectField class]];
  IOSObjectArray *results = [IOSObjectArray arrayWithLength:count
                                                       type:fieldType];
  for (jint i = 0; i < count; i++) {
    [results replaceObjectAtIndex:i withObject:[fields objectAtIndex:i]];
  }
  return results;
}

- (IOSObjectArray *)getDeclaredFields {
  NSMutableDictionary *fieldDictionary = [NSMutableDictionary dictionary];
  GetFieldsFromClass(self, fieldDictionary);
  return copyFieldsToObjectArray([fieldDictionary allValues]);
}

- (IOSObjectArray *)getFields {
  NSMutableDictionary *fieldDictionary = [NSMutableDictionary dictionary];
  IOSClass *iosClass = self;
  Class cls = nil;
  while (iosClass && (cls = iosClass.objcClass)) {
    GetFieldsFromClass(iosClass, fieldDictionary);
    iosClass = [iosClass getSuperclass];
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
  return NO;
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

- (JavaNetURL *)getResource:(NSString *)name {
  return [[self getClassLoader] getResourceWithNSString:name];
}

- (JavaIoInputStream *)getResourceAsStream:(NSString *)name {
  return [[self getClassLoader] getResourceAsStreamWithNSString:name];
}

- (id)__boxValue:(J2ObjcRawValue *)rawValue {
  return rawValue->asId;
}

- (BOOL)__unboxValue:(id)value toRawValue:(J2ObjcRawValue *)rawValue {
  rawValue->asId = value;
  return true;
}

- (void)__readRawValue:(J2ObjcRawValue *)rawValue fromAddress:(const void *)addr {
  rawValue->asId = *(id *)addr;
}

- (void)__writeRawValue:(J2ObjcRawValue *)rawValue toAddress:(const void *)addr {
  *(id *)addr = rawValue->asId;
}

- (BOOL)__convertRawValue:(J2ObjcRawValue *)rawValue toType:(IOSClass *)type {
  // No conversion necessary if both types are ids.
  return ![type isPrimitive];
}

// Implementing NSCopying allows IOSClass objects to be used as keys in the
// class cache.
- (instancetype)copyWithZone:(NSZone *)zone {
  return self;
}

- (void)dealloc {
#if ! __has_feature(objc_arc)
  JreMemDebugRemove(self);
  [super dealloc];
#endif
}

IOSClass *FetchClass(Class cls) {
  static int key;
  IOSClass *iosClass = objc_getAssociatedObject(cls, &key);
  if (!iosClass) {
    @synchronized (cls) {
      iosClass = objc_getAssociatedObject(cls, &key);
      if (!iosClass) {
        if (cls == [NSObject class]) {
          iosClass = AUTORELEASE([[IOSMappedClass alloc] initWithClass:[NSObject class]
                                                               package:@"java.lang"
                                                                  name:@"Object"]);
        } else if ([cls isSubclassOfClass:[NSString class]]) {
          // NSString is implemented by several subclasses.
          if (!IOSClass_stringClass) {
            IOSClass_stringClass =
                AUTORELEASE([[IOSMappedClass alloc] initWithClass:[NSString class]
                                                          package:@"java.lang"
                                                             name:@"String"]);
          }
          iosClass = IOSClass_stringClass;
        } else {
          iosClass = AUTORELEASE([[IOSConcreteClass alloc] initWithClass:cls]);
        }
        objc_setAssociatedObject(cls, &key, iosClass, OBJC_ASSOCIATION_RETAIN);
      }
    }
  }
  return iosClass;
}

IOSClass *FetchProtocol(Protocol *protocol) {
  static int key;
  IOSClass *iosClass = objc_getAssociatedObject(protocol, &key);
  if (!iosClass) {
    @synchronized (protocol) {
      iosClass = objc_getAssociatedObject(protocol, &key);
      if (!iosClass) {
        iosClass = AUTORELEASE([[IOSProtocolClass alloc] initWithProtocol:protocol]);
        objc_setAssociatedObject(protocol, &key, iosClass, OBJC_ASSOCIATION_RETAIN);
      }
    }
  }
  return iosClass;
}

IOSClass *FetchArray(IOSClass *componentType) {
  static int key;
  IOSClass *iosClass = objc_getAssociatedObject(componentType, &key);
  if (!iosClass) {
    @synchronized (componentType) {
      iosClass = objc_getAssociatedObject(componentType, &key);
      if (!iosClass) {
        iosClass = AUTORELEASE([[IOSArrayClass alloc] initWithComponentType:componentType]);
        objc_setAssociatedObject(componentType, &key, iosClass, OBJC_ASSOCIATION_RETAIN);
      }
    }
  }
  return iosClass;
}

+ (void)initialize {
  if (self == [IOSClass class]) {
    // Explicitly mapped classes are defined in Types.initializeTypeMap().
    // If types are added to that method (it's rare) they need to be added here.
    IOSClass_mappedClasses = [[NSDictionary alloc] initWithObjectsAndKeys:
         @"NSObject",  @"java.lang.Object",
         @"IOSClass",  @"java.lang.Class",
         @"NSNumber",  @"java.lang.Number",
         @"NSString",  @"java.lang.String",
         @"NSString",  @"java.lang.CharSequence",
         @"NSCopying", @"java.lang.Cloneable", nil];

    IOSClass_byteClass = [[IOSPrimitiveClass alloc] initWithName:@"byte" type:@"B"];
    IOSClass_charClass = [[IOSPrimitiveClass alloc] initWithName:@"char" type:@"C"];
    IOSClass_doubleClass = [[IOSPrimitiveClass alloc] initWithName:@"double" type:@"D"];
    IOSClass_floatClass = [[IOSPrimitiveClass alloc] initWithName:@"float" type:@"F"];
    IOSClass_intClass = [[IOSPrimitiveClass alloc] initWithName:@"int" type:@"I"];
    IOSClass_longClass = [[IOSPrimitiveClass alloc] initWithName:@"long" type:@"J"];
    IOSClass_shortClass = [[IOSPrimitiveClass alloc] initWithName:@"short" type:@"S"];
    IOSClass_booleanClass = [[IOSPrimitiveClass alloc] initWithName:@"boolean" type:@"Z"];
    IOSClass_voidClass = [[IOSPrimitiveClass alloc] initWithName:@"void" type:@"V"];

    IOSClass_objectClass = FetchClass([NSObject class]);
    IOSClass_stringClass = FetchClass([NSString class]);

    // Load and initialize JRE categories, using their dummy classes.
    AUTORELEASE([[JreObjectCategoryDummy alloc] init]);
    AUTORELEASE([[JreStringCategoryDummy alloc] init]);
    AUTORELEASE([[JreNumberCategoryDummy alloc] init]);

    // Verify that these categories successfully loaded.
    if ([[NSObject class] instanceMethodSignatureForSelector:@selector(compareToWithId:)] == NULL ||
        [[NSString class] instanceMethodSignatureForSelector:@selector(trim)] == NULL ||
        ![NSNumber conformsToProtocol:@protocol(JavaIoSerializable)]) {
      [NSException raise:@"J2ObjCLinkError"
                  format:@"Your project is not configured to load categories from the JRE "
                          "emulation library. Try adding the -force_load linker flag."];
    }
  }
}

+ (long long int)serialVersionUID {
  return 3206093459760846163L;
}

// Generated by running the translator over the java.lang.Class stub file.
+ (J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { "getClasses", NULL, "[Ljava/lang/Class;", 0x1, NULL },
    { "getDeclaredClasses", NULL, "[Ljava/lang/Class;", 0x1, "Ljava/lang/SecurityException;" },
    { "getDeclaringClass", NULL, "Ljava/lang/Class;", 0x1, NULL },
    { "getGenericSuperclass", NULL, "Ljava/lang/reflect.Type;", 0x1, NULL },
    { "getSigners", NULL, "Ljava/lang/Object;", 0x1, NULL },
    { "isAnnotation", NULL, "Z", 0x1, NULL },
    { "isAnnotationPresent", NULL, "Z", 0x1, NULL },
    { "isLocalClass", NULL, "Z", 0x1, NULL },
    { "isMemberClass", NULL, "Z", 0x1, NULL },
    { "isSynthetic", NULL, "Z", 0x1, NULL },
  };
  static J2ObjcFieldInfo fields[] = {
    { "serialVersionUID_", NULL, 0x1a, "J" },
  };
static J2ObjcClassInfo _IOSClass = { "Class", "java.lang", NULL, 0x11, 10, methods, 1, fields, 0, NULL};
  return &_IOSClass;
}

@end
