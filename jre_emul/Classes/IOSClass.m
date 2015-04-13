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

#import "FastPointerLookup.h"
#import "IOSArrayClass.h"
#import "IOSConcreteClass.h"
#import "IOSMappedClass.h"
#import "IOSObjectArray.h"
#import "IOSPrimitiveArray.h"
#import "IOSPrimitiveClass.h"
#import "IOSProtocolClass.h"
#import "IOSReflection.h"
#import "JavaMetadata.h"
#import "NSCopying+JavaCloneable.h"
#import "NSNumber+JavaNumber.h"
#import "NSObject+JavaObject.h"
#import "NSString+JavaString.h"
#import "com/google/j2objc/annotations/ObjectiveCName.h"
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
#import "java/lang/reflect/TypeVariable.h"
#import "java/util/Properties.h"
#import "libcore/reflect/GenericSignatureParser.h"
#import "libcore/reflect/Types.h"
#import "objc/message.h"
#import "objc/runtime.h"
#import "unicode/uregex.h"

J2OBJC_INITIALIZED_DEFN(IOSClass)

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

static IOSObjectArray *IOSClass_emptyClassArray;

#define PREFIX_MAPPING_RESOURCE @"prefixes.properties"

// Package to prefix mappings, initialized in FindMappedClass().
static JavaUtilProperties *prefixMapping;

- (Class)objcClass {
  return nil;
}

- (Protocol *)objcProtocol {
  return nil;
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

- (id)newInstance {
  // Per the JLS spec, throw an InstantiationException if the type is an
  // interface (no class_), array or primitive type (IOSClass types), or void.
  @throw AUTORELEASE([[JavaLangInstantiationException alloc] init]);
}

- (IOSClass *)getSuperclass {
  return nil;
}

- (id<JavaLangReflectType>)getGenericSuperclass {
  id<JavaLangReflectType> result = [self getSuperclass];
  if (!result) {
    return nil;
  }
  NSString *genericSignature = [[self getMetadata] genericSignature];
  if (!genericSignature) {
    return result;
  }
  LibcoreReflectGenericSignatureParser *parser =
      [[LibcoreReflectGenericSignatureParser alloc]
       initWithJavaLangClassLoader:JavaLangClassLoader_getSystemClassLoader()];
  [parser parseForClassWithJavaLangReflectGenericDeclaration:self
                                                withNSString:genericSignature];
  result = [LibcoreReflectTypes getType:parser->superclassType_];
  [parser release];
  return result;
}

- (IOSClass *)getDeclaringClass {
  if ([self isPrimitive] || [self isArray] || [self isAnonymousClass] || [self isLocalClass]) {
    // Class.getDeclaringClass() javadoc, JVM spec 4.7.6.
    return nil;
  }
  IOSClass *enclosingClass = [self getEnclosingClass];
  while ([enclosingClass isAnonymousClass]) {
    enclosingClass = [enclosingClass getEnclosingClass];
  }
  return enclosingClass;
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
  return [[self getName] stringByReplacingOccurrencesOfString:@"$" withString:@"."];
}

- (NSString *)objcName {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:
      @"abstract method not overridden"]);
}

- (int)getModifiers {
  JavaClassMetadata *metadata = [self getMetadata];
  if (metadata) {
    return metadata.modifiers & JavaLangReflectModifier_classModifiers();
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
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues]
      type:JavaLangReflectMethod_class_()];
}

// Return the constructors declared by this class.  Superclass constructors
// are not included.
- (IOSObjectArray *)getDeclaredConstructors {
  return [IOSObjectArray arrayWithLength:0 type:JavaLangReflectConstructor_class_()];
}

static void GetAllMethods(IOSClass *cls, NSMutableDictionary *methodMap) {
  [cls collectMethods:methodMap publicOnly:YES];

  // getMethods() returns unimplemented interface methods if the class is abstract.
  if (([cls getModifiers] & JavaLangReflectModifier_ABSTRACT) > 0) {
    for (IOSClass *p in [cls getInterfacesInternal]) {
      GetAllMethods(p, methodMap);
    }
  }

  while ((cls = [cls getSuperclass])) {
    GetAllMethods(cls, methodMap);
  }
}

// Return the methods for this class, including inherited methods.
- (IOSObjectArray *)getMethods {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  GetAllMethods(self, methodMap);
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues]
                                     type:JavaLangReflectMethod_class_()];
}

// Return the constructors for this class, including inherited ones.
- (IOSObjectArray *)getConstructors {
  return [IOSObjectArray arrayWithLength:0 type:JavaLangReflectConstructor_class_()];
}

// Return a method instance described by a name and an array of
// parameter types.  If the named method isn't a member of the specified
// class, return a superclass method if available.
- (JavaLangReflectMethod *)getMethod:(NSString *)name
                      parameterTypes:(IOSObjectArray *)types {
  NSString *translatedName = IOSClass_GetTranslatedMethodName(self, name, types);
  IOSClass *cls = self;
  do {
    JavaLangReflectMethod *method = [cls findMethodWithTranslatedName:translatedName
                                                      checkSupertypes:YES];
    if (method != nil) {
      if (([method getModifiers] & JavaLangReflectModifier_PUBLIC) == 0) {
        break;
      }
      return method;
    }
    for (IOSClass *p in [cls getInterfacesInternal]) {
      method = [p findMethodWithTranslatedName:translatedName checkSupertypes:YES];
      if (method != nil) {
        return method;
      }
    }
  } while ((cls = [cls getSuperclass]) != nil);
  @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] initWithNSString:name]);
}

// Return a method instance described by a name and an array of parameter
// types.  Return nil if the named method is not a member of this class.
- (JavaLangReflectMethod *)getDeclaredMethod:(NSString *)name
                              parameterTypes:(IOSObjectArray *)types {
  JavaLangReflectMethod *result =
      [self findMethodWithTranslatedName:IOSClass_GetTranslatedMethodName(self, name, types)
                         checkSupertypes:NO];
  if (!result) {
    @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] initWithNSString:name]);
  }
  return result;
}

- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName
                                        checkSupertypes:(BOOL)includePublic {
  return nil; // Overriden by subclasses.
}

- (JavaLangReflectConstructor *)findConstructorWithTranslatedName:(NSString *)objcName {
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

// Return a method's selector as it would be created during j2objc translation.
// The format is "name" with no parameters, "nameWithType:" for one parameter,
// and "nameWithType:withType:..." for multiple parameters.
NSString *IOSClass_GetTranslatedMethodName(IOSClass *cls, NSString *name,
                                           IOSObjectArray *parameterTypes) {
  nil_chk(name);
  IOSClass *metaCls = cls;
  while (metaCls) {
    JavaClassMetadata *metadata = [metaCls getMetadata];
    if (metadata) {
      JavaMethodMetadata *methodData =
          [metadata findMethodMetadataWithJavaName:name
                                          argCount:parameterTypes ? parameterTypes->size_ : 0];
      if (methodData) {
        return [methodData objcName];
      }
    }
    metaCls = [metaCls getSuperclass];
  }
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
    return IOSClass_objectClass;
  }
  Protocol *protocol = NSProtocolFromString(iosName);
  if (protocol) {
    return IOSClass_fromProtocol(protocol);
  }
  Class clazz = NSClassFromString(iosName);
  if (clazz) {
    return IOSClass_fromClass(clazz);
  }
  return nil;
}

static IOSClass *FindMappedClass(NSString *name) {
  NSRange lastDot = [name rangeOfString:@"." options:NSBackwardsSearch];
  if (lastDot.location == NSNotFound) {
    return nil;   // No package in class name.
  }
  NSString *package = [name substringToIndex:lastDot.location];
  NSString *prefix = nil;

  // Check for a package-info class that has an ObjectiveCName annotation.
  NSString *pkgInfoName =
      IOSClass_JavaToIOSName([package stringByAppendingString:@".package_info"]);
  IOSClass *pkgInfo = ClassForIosName(pkgInfoName);
  if (pkgInfo) {
    IOSClass *objcNameClass = IOSClass_fromClass([ComGoogleJ2objcAnnotationsObjectiveCName class]);
    ComGoogleJ2objcAnnotationsObjectiveCName *ann =
        [pkgInfo getAnnotationWithIOSClass:objcNameClass];
    if (ann) {
      prefix = ann.value;
    }
  }
  if (!prefix) {
    // Check whether package has a mapped prefix property.
    static dispatch_once_t once;
    dispatch_once(&once, ^{
      prefixMapping = [[JavaUtilProperties alloc] init];
      JavaIoInputStream *prefixesResource =
          [IOSClass_objectClass getResourceAsStream:PREFIX_MAPPING_RESOURCE];
      if (prefixesResource) {
        [prefixMapping load__WithJavaIoInputStream:prefixesResource];
      }
    });
    prefix = [prefixMapping getPropertyWithNSString:package];
  }
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
    return IOSClass_arrayOf(componentType);
  }
  return nil;
}

IOSClass *IOSClass_forName_(NSString *className) {
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
  if ([className rangeOfString:@"$"].location != NSNotFound) {
    // Scan classes to see if a class exists with a mixture of '_' and '$' characters.
    // This can happen with inner classes that have '$' in their names.
    NSString *iosName = IOSClass_JavaToIOSName(className);
    NSRange range = NSMakeRange(0, [iosName length]);
    NSString *s = [iosName stringByReplacingOccurrencesOfString:@"[_$]"
                                                       withString:@"[_$]+"
                                                          options:NSRegularExpressionSearch
                                                            range:range];
    NSString *regex = [NSString stringWithFormat:@"^%@$", s];
    UErrorCode status = U_ZERO_ERROR;
    UParseError error;
    error.offset = -1;
    jint patLen = (jint)[regex length];
    jchar *patternBuf = (jchar *)malloc(patLen * sizeof(unichar));
    [regex getCharacters:patternBuf range:NSMakeRange(0, patLen)];
    URegularExpression *pattern =
        uregex_open(patternBuf, patLen, UREGEX_ERROR_ON_UNKNOWN_ESCAPES, &error, &status);
    if (!U_SUCCESS(status)) {
      @throw [[[JavaLangAssertionError alloc] init] autorelease];
    }

    int classCount = objc_getClassList(NULL, 0);
    Class *classes = (Class *)malloc(classCount * sizeof(Class));
    objc_getClassList(classes, classCount);
    size_t bufsize = 256; // Expands below if necessary.
    unichar *nameBuf = (unichar *)malloc(bufsize * sizeof(unichar));
    for (int i = 0; i < classCount; i++) {
      Class cls = classes[i];
      NSString *cls_name = [[NSString alloc] initWithUTF8String:class_getName(cls)];
      if (cls_name.length > bufsize) {
        nameBuf = (unichar *)realloc(nameBuf, cls_name.length);
        bufsize = cls_name.length;
      }
      [cls_name getCharacters:nameBuf range:NSMakeRange(0, cls_name.length)];

      UErrorCode status = U_ZERO_ERROR;
      uregex_setText(pattern, nameBuf, (int32_t)cls_name.length, &status);
      if (!U_SUCCESS(status)) {
        continue;
      }
      uregex_setRegion(pattern, 0, (int32_t)cls_name.length, &status);
      if (!U_SUCCESS(status)) {
        continue;
      }
      jboolean matches = uregex_matches(pattern, -1, &status);
      if (matches && U_SUCCESS(status)) {
        iosClass = IOSClass_fromClass(cls);
        break;
      }
    }
    free(classes);
    free(nameBuf);
    free(patternBuf);
    uregex_close(pattern);
    if (iosClass) {
      return iosClass;
    }
  }
  @throw AUTORELEASE([[JavaLangClassNotFoundException alloc] initWithNSString:className]);
}

+ (IOSClass *)forName:(NSString *)className {
  return IOSClass_forName_(className);
}

IOSClass *IOSClass_forName_initialize_classLoader_(
    NSString *className, BOOL load, JavaLangClassLoader *loader) {
  return IOSClass_forName_(className);
}

+ (IOSClass *)forName:(NSString *)className
           initialize:(BOOL)load
          classLoader:(JavaLangClassLoader *)loader {
  return IOSClass_forName_initialize_classLoader_(className, load, loader);
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

- (IOSObjectArray *)getInterfacesInternal {
  return IOSClass_emptyClassArray;
}

- (IOSObjectArray *)getInterfaces {
  return [IOSObjectArray arrayWithArray:[self getInterfacesInternal]];
}

- (IOSObjectArray *)getGenericInterfaces {
  if ([self isPrimitive]) {
    return [IOSObjectArray arrayWithLength:0 type:JavaLangReflectType_class_()];
  }
  NSString *genericSignature = [[self getMetadata] genericSignature];
  if (!genericSignature) {
    // Just return regular interfaces list.
    IOSObjectArray *interfaces = [self getInterfacesInternal];
    return [IOSObjectArray arrayWithObjects:interfaces->buffer_
                                      count:interfaces->size_
                                       type:JavaLangReflectType_class_()];
  }
  LibcoreReflectGenericSignatureParser *parser =
      [[LibcoreReflectGenericSignatureParser alloc]
       initWithJavaLangClassLoader:JavaLangClassLoader_getSystemClassLoader()];
  [parser parseForClassWithJavaLangReflectGenericDeclaration:self
                                                withNSString:genericSignature];
  IOSObjectArray *result =
      [LibcoreReflectTypes getTypeArray:parser->interfaceTypes_ clone:false];
  [parser release];
  return result;
}

IOSObjectArray *IOSClass_NewInterfacesFromProtocolList(Protocol **list, unsigned int count) {
  IOSClass *buffer[count];
  unsigned int actualCount = 0;
  for (unsigned int i = 0; i < count; i++) {
    Protocol *protocol = list[i];
    if (protocol != @protocol(NSObject) && protocol != @protocol(JavaObject)) {
      buffer[actualCount++] = IOSClass_fromProtocol(list[i]);
    }
  }
  return [IOSObjectArray newArrayWithObjects:buffer
                                       count:actualCount
                                        type:IOSClass_class_()];
}

- (IOSObjectArray *)getTypeParameters {
  NSString *genericSignature = [[self getMetadata] genericSignature];
  if (!genericSignature) {
    return [IOSObjectArray arrayWithLength:0 type:JavaLangReflectTypeVariable_class_()];
  }
  LibcoreReflectGenericSignatureParser *parser =
      [[LibcoreReflectGenericSignatureParser alloc]
       initWithJavaLangClassLoader:JavaLangClassLoader_getSystemClassLoader()];
  [parser parseForClassWithJavaLangReflectGenericDeclaration:self
                                                withNSString:genericSignature];
  IOSObjectArray *result = [[parser->formalTypeParameters_ retain] autorelease];
  [parser release];
  return result;
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
  IOSClass *inheritedAnnotation = JavaLangAnnotationInherited_class_();
  while (cls) {
    IOSObjectArray *declared = [cls getDeclaredAnnotations];
    for (jint i = 0; i < declared->size_; i++) {
      id<JavaLangAnnotationAnnotation> annotation = declared->buffer_[i];
      IOSObjectArray *attributes = [[annotation getClass] getDeclaredAnnotations];
      for (jint j = 0; j < attributes->size_; j++) {
        id<JavaLangAnnotationAnnotation> attribute = attributes->buffer_[j];
        if (inheritedAnnotation == [attribute annotationType]) {
          [array addObject:annotation];
        }
      }
    }
    cls = [cls getSuperclass];
  }
  IOSObjectArray *result =
      [IOSObjectArray arrayWithNSArray:array type:JavaLangAnnotationAnnotation_class_()];
  [array release];
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
  return [IOSObjectArray arrayWithLength:0 type:JavaLangAnnotationAnnotation_class_()];
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
  return JavaLangClassLoader_getSystemClassLoader();
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

// Adds all the fields for a specified class to a specified dictionary.
static void GetFieldsFromClass(IOSClass *iosClass, NSMutableDictionary *fields, BOOL publicOnly) {
  JavaClassMetadata *metadata = [iosClass getMetadata];
  if (metadata) {
    IOSObjectArray *infos = [metadata allFields];
    for (jint i = 0; i < infos->size_; i++) {
      JavaFieldMetadata *fieldMeta = [infos objectAtIndex:i];
      if (publicOnly && ([fieldMeta modifiers] & JavaLangReflectModifier_PUBLIC) == 0) {
        continue;
      }
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
      JavaLangReflectField *field = [JavaLangReflectField fieldWithIvar:ivars[i]
                                                              withClass:iosClass
                                                           withMetadata:nil];
      NSString *name = [field getName];
      if (![fields valueForKey:name]) { // Don't add shadowed fields.
        [fields setObject:field forKey:name];
      }
    }
    free(ivars);
  }
}

// Try to locate a native variable for a given class and name. Since the
// translator may append underscores to differentiate reserved and method
// names, try all possible names.
Ivar FindIvar(IOSClass *cls, NSString *name) {
  Ivar ivar = class_getInstanceVariable(cls.objcClass, GetFieldName(name, cls));
  if (ivar) {
    return ivar;
  }
  for (int i = 0; i < 3; i++) {  // Translator never appends more the 3 underscores.
    name = [name stringByAppendingString:@"_"];
    ivar = class_getInstanceVariable(cls.objcClass, GetFieldName(name, cls));
    if (ivar) {
      return ivar;
    }
  }
  return NULL;
}

- (JavaLangReflectField *)getDeclaredField:(NSString *)name {
  nil_chk(name);
  Class cls = self.objcClass;
  if (cls) {
    JavaClassMetadata *metadata = [self getMetadata];
    if (metadata) {
      JavaFieldMetadata *fieldMeta = [metadata findFieldMetadata:[name UTF8String]];
      if (fieldMeta) {
        Ivar ivar = class_getInstanceVariable(cls, [[fieldMeta iosName] UTF8String]);
        return [JavaLangReflectField fieldWithIvar:ivar
                                         withClass:self
                                      withMetadata:fieldMeta];
      }
    } else {
      Ivar ivar = FindIvar(self, name);
      if (ivar) {
        return [JavaLangReflectField fieldWithIvar:ivar withClass:self withMetadata:nil];
      }
    }
  }
  @throw AUTORELEASE([[JavaLangNoSuchFieldException alloc] initWithNSString:name]);
}

JavaLangReflectField *findField(IOSClass *iosClass, NSString *name) {
  while (iosClass) {
    Class cls = iosClass.objcClass;
    JavaClassMetadata *metadata = [iosClass getMetadata];
    if (metadata) {
      JavaFieldMetadata *fieldMeta = [metadata findFieldMetadata:[name UTF8String]];
      if (([fieldMeta modifiers] & JavaLangReflectModifier_PUBLIC) > 0) {
        Ivar ivar = class_getInstanceVariable(cls, [[fieldMeta iosName] UTF8String]);
        return [JavaLangReflectField fieldWithIvar:ivar
                                         withClass:iosClass
                                      withMetadata:fieldMeta];
      }
    } else {
      Ivar ivar = FindIvar(iosClass, name);
      if (ivar) {
        return [JavaLangReflectField fieldWithIvar:ivar withClass:iosClass withMetadata:nil];
      }
    }
    for (IOSClass *p in [iosClass getInterfacesInternal]) {
      JavaLangReflectField *field = findField(p, name);
      if (field) {
        return field;
      }
    }
    iosClass = [iosClass getSuperclass];
  }
  return nil;
}

- (JavaLangReflectField *)getField:(NSString *)name {
  nil_chk(name);
  JavaLangReflectField *field = findField(self, name);
  if (field) {
    return field;
  }
  @throw AUTORELEASE([[JavaLangNoSuchFieldException alloc] initWithNSString:name]);
}

IOSObjectArray *copyFieldsToObjectArray(NSArray *fields) {
  jint count = (jint)[fields count];
  IOSObjectArray *results =
      [IOSObjectArray arrayWithLength:count type:JavaLangReflectField_class_()];
  for (jint i = 0; i < count; i++) {
    [results replaceObjectAtIndex:i withObject:[fields objectAtIndex:i]];
  }
  return results;
}

- (IOSObjectArray *)getDeclaredFields {
  NSMutableDictionary *fieldDictionary = [NSMutableDictionary dictionary];
  GetFieldsFromClass(self, fieldDictionary, NO);
  return copyFieldsToObjectArray([fieldDictionary allValues]);
}

static void getAllFields(IOSClass *cls, NSMutableDictionary *fieldMap) {
  GetFieldsFromClass(cls, fieldMap, YES);
  for (IOSClass *p in [cls getInterfacesInternal]) {
    getAllFields(p, fieldMap);
  }
  while ((cls = [cls getSuperclass])) {
    getAllFields(cls, fieldMap);
  }
}

- (IOSObjectArray *)getFields {
  NSMutableDictionary *fieldDictionary = [NSMutableDictionary dictionary];
  getAllFields(self, fieldDictionary);
  return copyFieldsToObjectArray([fieldDictionary allValues]);
}

static BOOL IsConstructorSelector(NSString *selector) {
  return [selector isEqualToString:@"init"] || [selector hasPrefix:@"initWith"];
}

- (JavaLangReflectMethod *)getEnclosingMethod {
  JavaEnclosingMethodMetadata *metadata = [[self getMetadata] getEnclosingMethod];
  if (metadata) {
    if (IsConstructorSelector(metadata.selector)) {
      return nil;
    }
    IOSClass *type = ClassForIosName(metadata.typeName);
    if (!type) {
      // Should always succeed, since the method's class should be defined in
      // the same object file as the enclosed class.
      @throw AUTORELEASE([[JavaLangAssertionError alloc] init]);
    }
    return [type findMethodWithTranslatedName:metadata.selector checkSupertypes:NO];
  }
  return nil;
}

- (JavaLangReflectConstructor *)getEnclosingConstructor {
  JavaEnclosingMethodMetadata *metadata = [[self getMetadata] getEnclosingMethod];
  if (metadata) {
    if (!IsConstructorSelector(metadata.selector)) {
      return nil;
    }
    IOSClass *type = ClassForIosName(metadata.typeName);
    if (!type) {
      // Should always succeed, since the method's class should be defined in
      // the same object file as the enclosed class.
      @throw AUTORELEASE([[JavaLangAssertionError alloc] init]);
    }
    return [type findConstructorWithTranslatedName:metadata.selector];
  }
  return nil;
}


// Adds all the inner classes for a specified class to a specified dictionary.
static void GetInnerClasses(IOSClass *iosClass, NSMutableArray *classes,
    BOOL publicOnly, BOOL includeInterfaces) {
  JavaClassMetadata *metadata = [iosClass getMetadata];
  if (metadata) {
    IOSObjectArray *innerClasses = [metadata getInnerClasses];
    if (!innerClasses) {
      return;
    }
    for (jint i = 0; i < innerClasses->size_; i++) {
      IOSClass *c = IOSObjectArray_Get(innerClasses, i);
      if (![c isAnonymousClass] && ![c isSynthetic]) {
        if (publicOnly && ([c getModifiers] & JavaLangReflectModifier_PUBLIC) == 0) {
          continue;
        }
        if ([c isInterface] && !includeInterfaces) {
          continue;
        }
        [classes addObject:c];
      }
    }
  }
}

- (IOSObjectArray *)getClasses {
  NSMutableArray *innerClasses = [[NSMutableArray alloc] init];
  IOSClass *cls = self;
  GetInnerClasses(cls, innerClasses, YES, YES);
  while ((cls = [cls getSuperclass])) {
    // Class.getClasses() shouldn't include interfaces from superclasses.
    GetInnerClasses(cls, innerClasses, YES, NO);
  }
  IOSObjectArray *result = [IOSObjectArray arrayWithNSArray:innerClasses
                                                       type:IOSClass_class_()];
  [innerClasses release];
  return result;
}

- (IOSObjectArray *)getDeclaredClasses {
  NSMutableArray *declaredClasses = [[NSMutableArray alloc] init];
  GetInnerClasses(self, declaredClasses, NO, YES);
  IOSObjectArray *result = [IOSObjectArray arrayWithNSArray:declaredClasses
                                                       type:IOSClass_class_()];
  [declaredClasses release];
  return result;
}

- (BOOL)isAnonymousClass {
  return NO;
}

- (BOOL)desiredAssertionStatus {
  return false;
}

- (IOSObjectArray *)getEnumConstants {
  if ([self isEnum]) {
    return JavaLangEnum_getSharedConstantsWithIOSClass_(self);
  }
  return nil;
}

- (Class)objcArrayClass {
  return [IOSObjectArray class];
}

- (size_t)getSizeof {
  return sizeof(id);
}

- (JavaNetURL *)getResource:(NSString *)name {
  return [[self getClassLoader] getResourceWithNSString:name];
}

- (JavaIoInputStream *)getResourceAsStream:(NSString *)name {
  return [[self getClassLoader] getResourceAsStreamWithNSString:name];
}

// These java.security methods don't have an iOS equivalent, so always return nil.
- (id)getProtectionDomain {
  return nil;
}

- (id)getSigners {
  return nil;
}


- (id)__boxValue:(J2ObjcRawValue *)rawValue {
  return (id)rawValue->asId;
}

- (BOOL)__unboxValue:(id)value toRawValue:(J2ObjcRawValue *)rawValue {
  rawValue->asId = value;
  return true;
}

- (void)__readRawValue:(J2ObjcRawValue *)rawValue fromAddress:(const void *)addr {
  rawValue->asId = *(id *)addr;
}

- (void)__writeRawValue:(J2ObjcRawValue *)rawValue toAddress:(const void *)addr {
  *(id *)addr = (id)rawValue->asId;
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

- (IOSClass *)getClass {
  return IOSClass_class_();
}

static BOOL IsStringType(Class cls) {
  // We can't trigger class initialization because that might recursively enter
  // FetchClass and result in deadlock within the FastPointerLookup. Therefore,
  // we can't use [cls isSubclassOfClass:[NSString class]].
  Class stringCls = [NSString class];
  while (cls) {
    if (cls == stringCls) {
      return YES;
    }
    cls = class_getSuperclass(cls);
  }
  return NO;
}

static void *ClassLookup(void *clsPtr) {
  Class cls = (Class)clsPtr;
  if (cls == [NSObject class]) {
    return [[IOSMappedClass alloc] initWithClass:[NSObject class]
                                         package:@"java.lang"
                                            name:@"Object"];
  } else if (IsStringType(cls)) {
    // NSString is implemented by several subclasses.
    // Thread safety is guaranteed by the FastPointerLookup that calls this.
    static IOSClass *stringClass;
    if (!stringClass) {
      stringClass = [[IOSMappedClass alloc] initWithClass:[NSString class]
                                                  package:@"java.lang"
                                                     name:@"String"];
    }
    return stringClass;
  } else {
    IOSClass *result = [[IOSConcreteClass alloc] initWithClass:cls];
    return result;
  }
  return NULL;
}

static FastPointerLookup_t classLookup = FAST_POINTER_LOOKUP_INIT(&ClassLookup);

IOSClass *IOSClass_fromClass(Class cls) {
  // We get deadlock if IOSClass is not initialized before entering the fast
  // lookup because +initialize makes calls into IOSClass_fromClass().
  IOSClass_initialize();
  return (IOSClass *)FastPointerLookup(&classLookup, cls);
}

static void *ProtocolLookup(void *protocol) {
  return [[IOSProtocolClass alloc] initWithProtocol:(Protocol *)protocol];
}

static FastPointerLookup_t protocolLookup = FAST_POINTER_LOOKUP_INIT(&ProtocolLookup);

IOSClass *IOSClass_fromProtocol(Protocol *protocol) {
  return (IOSClass *)FastPointerLookup(&protocolLookup, protocol);
}

static void *ArrayLookup(void *componentType) {
  return [[IOSArrayClass alloc] initWithComponentType:(IOSClass *)componentType];
}

static FastPointerLookup_t arrayLookup = FAST_POINTER_LOOKUP_INIT(&ArrayLookup);

IOSClass *IOSClass_arrayOf(IOSClass *componentType) {
  return (IOSClass *)FastPointerLookup(&arrayLookup, componentType);
}

IOSClass *IOSClass_arrayType(IOSClass *componentType, jint dimensions) {
  IOSClass *result = (IOSClass *)FastPointerLookup(&arrayLookup, componentType);
  while (--dimensions > 0) {
    result = (IOSClass *)FastPointerLookup(&arrayLookup, result);
  }
  return result;
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

    IOSClass_objectClass = IOSClass_fromClass([NSObject class]);

    IOSClass_emptyClassArray = [IOSObjectArray newArrayWithLength:0 type:IOSClass_class_()];

    // Load and initialize JRE categories, using their dummy classes.
    [JreObjectCategoryDummy class];
    [JreStringCategoryDummy class];
    [JreNumberCategoryDummy class];
    [NSCopying class];

    // Verify that these categories successfully loaded.
    if ([[NSObject class] instanceMethodSignatureForSelector:@selector(compareToWithId:)] == NULL ||
        [[NSString class] instanceMethodSignatureForSelector:@selector(trim)] == NULL ||
        ![NSNumber conformsToProtocol:@protocol(JavaIoSerializable)]) {
      [NSException raise:@"J2ObjCLinkError"
                  format:@"Your project is not configured to load categories from the JRE "
                          "emulation library. Try adding the -force_load linker flag."];
    }

    J2OBJC_SET_INITIALIZED(IOSClass)
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
  static J2ObjcClassInfo _IOSClass = {
    1, "Class", "java.lang", NULL, 0x11, 10, methods, 1, fields, 0, NULL
  };
  return &_IOSClass;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(IOSClass)
