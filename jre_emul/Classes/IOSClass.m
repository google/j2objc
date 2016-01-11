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
#import "J2ObjC_icu.h"
#import "JavaMetadata.h"
#import "NSCopying+JavaCloneable.h"
#import "NSNumber+JavaNumber.h"
#import "NSObject+JavaObject.h"
#import "NSString+JavaString.h"
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
#import "java/util/Enumeration.h"
#import "java/util/Properties.h"
#import "libcore/reflect/GenericSignatureParser.h"
#import "libcore/reflect/Types.h"
#import "objc/message.h"
#import "objc/runtime.h"

#define IOSClass_serialVersionUID 3206093459760846163LL

@interface IOSClass () {
  JavaClassMetadata *metadata_;
}
@end

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

#define PREFIX_MAPPING_RESOURCE @"/prefixes.properties"

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

- (instancetype)initWithClass:(Class)cls {
  if ((self = [super init])) {
    if (cls) {
      // Can't use respondsToSelector here because that will search superclasses.
      Method metadataMethod = JreFindClassMethod(cls, "__metadata");
      if (metadataMethod) {
        J2ObjcClassInfo *rawData = (ARCBRIDGE J2ObjcClassInfo *) method_invoke(cls, metadataMethod);
        metadata_ = [[JavaClassMetadata alloc] initWithMetadata:rawData];
      }
    }
  }
  return self;
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
  NSString *genericSignature = [metadata_ genericSignature];
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
- (jboolean)isInstance:(id)object {
  return false;
}

- (NSString *)getName {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithId:@"abstract method not overridden"]);
}

- (NSString *)getSimpleName {
  return [self getName];
}

- (NSString *)getCanonicalName {
  return [[self getName] stringByReplacingOccurrencesOfString:@"$" withString:@"."];
}

- (NSString *)objcName {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithId:@"abstract method not overridden"]);
}

- (int)getModifiers {
  if (metadata_) {
    return metadata_.modifiers & JavaLangReflectModifier_classModifiers();
  } else {
    // All Objective-C classes and protocols are public by default.
    return JavaLangReflectModifier_PUBLIC;
  }
}

// Returns all methods defined in a class. Methods that aren't defined by the
// original Java class are ignored.
- (void)collectMethods:(NSMutableDictionary *)methodMap
            publicOnly:(jboolean)publicOnly {
  // Overridden by subclasses.
}

// Return the class and instance methods declared by the Java class.  Superclass
// methods are not included.
- (IOSObjectArray *)getDeclaredMethods {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  [self collectMethods:methodMap publicOnly:false];
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues]
      type:JavaLangReflectMethod_class_()];
}

// Return the constructors declared by this class.  Superclass constructors
// are not included.
- (IOSObjectArray *)getDeclaredConstructors {
  return [IOSObjectArray arrayWithLength:0 type:JavaLangReflectConstructor_class_()];
}

static void GetAllMethods(IOSClass *cls, NSMutableDictionary *methodMap) {
  [cls collectMethods:methodMap publicOnly:true];

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
                                                      checkSupertypes:true];
    if (method != nil) {
      if (([method getModifiers] & JavaLangReflectModifier_PUBLIC) == 0) {
        break;
      }
      return method;
    }
    for (IOSClass *p in [cls getInterfacesInternal]) {
      method = [p findMethodWithTranslatedName:translatedName checkSupertypes:true];
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
                         checkSupertypes:false];
  if (!result) {
    @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] initWithNSString:name]);
  }
  return result;
}

- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName
                                        checkSupertypes:(jboolean)includePublic {
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
  jint nParameters = parameterTypes ? parameterTypes->size_ : 0;
  while (metaCls) {
    JavaMethodMetadata *methodData =
        [metaCls->metadata_ findMethodMetadataWithJavaName:name argCount:nParameters];
    if (methodData) {
      return [methodData objcName];
    }
    metaCls = [metaCls getSuperclass];
  }
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

- (jboolean)isAssignableFrom:(IOSClass *)cls {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithId:@"abstract method not overridden"]);
}

- (IOSClass *)asSubclass:(IOSClass *)cls {
  if ([cls isAssignableFrom:self]) {
    return self;
  }

  @throw AUTORELEASE([[JavaLangClassCastException alloc] initWithNSString:[self description]]);
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

  // Check for a package-info class that has a __prefix method.
  NSString *pkgInfoName =
      IOSClass_JavaToIOSName([package stringByAppendingString:@".package_info"]);
  Class pkgInfoCls = NSClassFromString(pkgInfoName);
  Method prefixMethod = JreFindClassMethod(pkgInfoCls, "__prefix");
  if (prefixMethod) {
    prefix = method_invoke(pkgInfoCls, prefixMethod);
  }
  if (!prefix) {
    // Check whether package has a mapped prefix property.
    static dispatch_once_t once;
    dispatch_once(&once, ^{
      JavaIoInputStream *prefixesResource =
          [IOSClass_objectClass getResourceAsStream:PREFIX_MAPPING_RESOURCE];
      if (prefixesResource) {
        prefixMapping = [[JavaUtilProperties alloc] init];
        [prefixMapping load__WithJavaIoInputStream:prefixesResource];
      }
    });
    prefix = [prefixMapping getPropertyWithNSString:package];
  }
  if (!prefix && prefixMapping) {
    // Check each prefix mapping to see if it's a matching wildcard.
    id<JavaUtilEnumeration> names = [prefixMapping propertyNames];
    while ([names hasMoreElements]) {
      NSString *key = (NSString *) [names nextElement];
      // Same translation as j2objc's PackagePrefixes.wildcardToRegex().
      NSString *regex;
      if ([key hasSuffix:@".*"]) {
        NSString *root = [[key substring:0 endIndex:((jint) [key length]) - 2]
                          replace:@"." withSequence:@"\\."];
        regex = [NSString stringWithFormat:@"^(%@|%@\\..*)$", root, root];
      } else {
        regex = [NSString stringWithFormat:@"^%@$",
                 [[key replace:@"." withSequence:@"\\."]replace:@"\\*" withSequence:@".*"]];
      }
      if ([package matches:regex]) {
        prefix = [prefixMapping getPropertyWithNSString:key];
        break;
      }
    }
  }
  if (!prefix) {
    return nil;   // No prefix for package.
  }
  NSString *mappedName =
      [prefix stringByAppendingString:[name substringFromIndex:lastDot.location + 1]];
  IOSClass *result = ClassForIosName(mappedName);
  if (result) {
    return result;
  }
  mappedName = [mappedName stringByReplacingOccurrencesOfString:@"$" withString:@"_"];
  return ClassForIosName(mappedName);
}

+ (IOSClass *)classForIosName:(NSString *)iosName {
  return ClassForIosName(iosName);
}

static IOSClass *ClassForJavaName(NSString *name) {
  IOSClass *cls = ClassForIosName(IOSClass_JavaToIOSName(name));
  if (!cls && [name indexOf:'$'] >= 0) {
    NSString *fixedName = [name stringByReplacingOccurrencesOfString:@"$" withString:@"_"];
    cls = ClassForIosName(IOSClass_JavaToIOSName(fixedName));
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
      NSUInteger name_len = cls_name.length;
      if (name_len > bufsize) {
        nameBuf = (unichar *)realloc(nameBuf, name_len);
        bufsize = name_len;
      }
      [cls_name getCharacters:nameBuf range:NSMakeRange(0, name_len)];
      [cls_name release];

      UErrorCode status = U_ZERO_ERROR;
      uregex_setText(pattern, nameBuf, (int32_t)name_len, &status);
      if (!U_SUCCESS(status)) {
        continue;
      }
      uregex_setRegion(pattern, 0, (int32_t)name_len, &status);
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
    NSString *className, jboolean load, JavaLangClassLoader *loader) {
  return IOSClass_forName_(className);
}

+ (IOSClass *)forName:(NSString *)className
           initialize:(jboolean)load
          classLoader:(JavaLangClassLoader *)loader {
  return IOSClass_forName_initialize_classLoader_(className, load, loader);
}

- (id)cast:(id)throwable {
  // There's no need to actually cast this here, as the translator will add
  // a C cast since the return type is a type variable.
  return throwable;
}

- (IOSClass *)getEnclosingClass {
  if (!metadata_ || !metadata_.enclosingName) {
    return nil;
  }
  NSMutableString *qName = [NSMutableString string];
  if (metadata_.packageName) {
    [qName appendString:metadata_.packageName];
    [qName appendString:@"."];
  }
  [qName appendString:metadata_.enclosingName];
  return ClassForJavaName(qName);
}

- (jboolean)isArray {
  return false;
}

- (jboolean)isEnum {
  return false;
}

- (jboolean)isInterface {
  return false;
}

- (jboolean)isPrimitive {
  return false;  // Overridden by IOSPrimitiveClass.
}

static jboolean hasModifier(IOSClass *cls, int flag) {
  return cls->metadata_ ? (cls->metadata_.modifiers & flag) > 0 : false;
}

- (jboolean)isAnnotation {
  return hasModifier(self, JavaLangReflectModifier_ANNOTATION);
}

- (jboolean)isMemberClass {
  return metadata_ && metadata_.enclosingName && ![self isAnonymousClass];
}

- (jboolean)isLocalClass {
  return [self getEnclosingMethod] && ![self isAnonymousClass];
}

- (jboolean)isSynthetic {
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
  NSString *genericSignature = [metadata_ genericSignature];
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
  NSString *genericSignature = [metadata_ genericSignature];
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

- (jboolean)isAnnotationPresentWithIOSClass:(IOSClass *)annotationClass {
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

- (JavaClassMetadata *)getMetadata {
  return metadata_;
}

- (id)getPackage {
  if (metadata_ && metadata_.packageName) {
    return AUTORELEASE([[JavaLangPackage alloc] initWithNSString:metadata_.packageName
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
  JavaFieldMetadata *fieldMetadata = [clazz->metadata_ findFieldMetadata:cname];
  if (fieldMetadata) {
    return [[fieldMetadata name] UTF8String];
  }
  name = [JavaLangReflectField variableName:name];
  return [name cStringUsingEncoding:[NSString defaultCStringEncoding]];
}

// Adds all the fields for a specified class to a specified dictionary.
static void GetFieldsFromClass(IOSClass *iosClass, NSMutableDictionary *fields,
    jboolean publicOnly) {
  JavaClassMetadata *metadata = iosClass->metadata_;
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

JavaLangReflectField *findDeclaredField(IOSClass *iosClass, NSString *name, jboolean publicOnly) {
  Class cls = iosClass.objcClass;
  if (cls) {
    JavaClassMetadata *metadata = iosClass->metadata_;
    if (metadata) {
      JavaFieldMetadata *fieldMeta = [metadata findFieldMetadata:[name UTF8String]];
      if (fieldMeta &&
          (!publicOnly || ([fieldMeta modifiers] & JavaLangReflectModifier_PUBLIC) != 0)) {
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
  }
  return nil;
}

JavaLangReflectField *findField(IOSClass *iosClass, NSString *name, jboolean publicOnly) {
  while (iosClass) {
    JavaLangReflectField *field = findDeclaredField(iosClass, name, publicOnly);
    if (field) {
      return field;
    }
    for (IOSClass *p in [iosClass getInterfacesInternal]) {
      JavaLangReflectField *field = findField(p, name, publicOnly);
      if (field) {
        return field;
      }
    }
    iosClass = [iosClass getSuperclass];
  }
  return nil;
}

- (JavaLangReflectField *)getDeclaredField:(NSString *)name {
  nil_chk(name);
  JavaLangReflectField *field = findDeclaredField(self, name, false);
  if (field) {
    return field;
  }
  @throw AUTORELEASE([[JavaLangNoSuchFieldException alloc] initWithNSString:name]);
}

- (JavaLangReflectField *)getField:(NSString *)name {
  nil_chk(name);
  JavaLangReflectField *field = findField(self, name, true);
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
  GetFieldsFromClass(self, fieldDictionary, false);
  return copyFieldsToObjectArray([fieldDictionary allValues]);
}

static void getAllFields(IOSClass *cls, NSMutableDictionary *fieldMap) {
  GetFieldsFromClass(cls, fieldMap, true);
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

static jboolean IsConstructorSelector(NSString *selector) {
  return [selector isEqualToString:@"init"] || [selector hasPrefix:@"initWith"];
}

- (JavaLangReflectMethod *)getEnclosingMethod {
  JavaEnclosingMethodMetadata *metadata = [metadata_ getEnclosingMethod];
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
    return [type findMethodWithTranslatedName:metadata.selector checkSupertypes:false];
  }
  return nil;
}

- (JavaLangReflectConstructor *)getEnclosingConstructor {
  JavaEnclosingMethodMetadata *metadata = [metadata_ getEnclosingMethod];
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
    jboolean publicOnly, jboolean includeInterfaces) {
  JavaClassMetadata *metadata = iosClass->metadata_;
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
  GetInnerClasses(cls, innerClasses, true, true);
  while ((cls = [cls getSuperclass])) {
    // Class.getClasses() shouldn't include interfaces from superclasses.
    GetInnerClasses(cls, innerClasses, true, false);
  }
  IOSObjectArray *result = [IOSObjectArray arrayWithNSArray:innerClasses
                                                       type:IOSClass_class_()];
  [innerClasses release];
  return result;
}

- (IOSObjectArray *)getDeclaredClasses {
  NSMutableArray *declaredClasses = [[NSMutableArray alloc] init];
  GetInnerClasses(self, declaredClasses, false, true);
  IOSObjectArray *result = [IOSObjectArray arrayWithNSArray:declaredClasses
                                                       type:IOSClass_class_()];
  [declaredClasses release];
  return result;
}

- (jboolean)isAnonymousClass {
  return false;
}

- (jboolean)desiredAssertionStatus {
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

NSString *resolveResourceName(IOSClass *cls, NSString *resourceName) {
  if (!resourceName || [resourceName length] == 0) {
    return resourceName;
  }
  if ([resourceName characterAtIndex:0] == '/') {
    return resourceName;
  }
  NSString *relativePath = [[[cls getPackage] getName] stringByReplacingOccurrencesOfString:@"."
                                                                                 withString:@"/"];
  return [NSString stringWithFormat:@"/%@/%@", relativePath, resourceName];
}

- (JavaNetURL *)getResource:(NSString *)name {
  return [[self getClassLoader] getResourceWithNSString:resolveResourceName(self, name)];
}

- (JavaIoInputStream *)getResourceAsStream:(NSString *)name {
  return [[self getClassLoader] getResourceAsStreamWithNSString:resolveResourceName(self, name)];
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

- (jboolean)__unboxValue:(id)value toRawValue:(J2ObjcRawValue *)rawValue {
  rawValue->asId = value;
  return true;
}

- (void)__readRawValue:(J2ObjcRawValue *)rawValue fromAddress:(const void *)addr {
  rawValue->asId = *(id *)addr;
}

- (void)__writeRawValue:(J2ObjcRawValue *)rawValue toAddress:(const void *)addr {
  *(id *)addr = (id)rawValue->asId;
}

- (jboolean)__convertRawValue:(J2ObjcRawValue *)rawValue toType:(IOSClass *)type {
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

static jboolean IsStringType(Class cls) {
  // We can't trigger class initialization because that might recursively enter
  // FetchClass and result in deadlock within the FastPointerLookup. Therefore,
  // we can't use [cls isSubclassOfClass:[NSString class]].
  Class stringCls = [NSString class];
  while (cls) {
    if (cls == stringCls) {
      return true;
    }
    cls = class_getSuperclass(cls);
  }
  return false;
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

+ (void)load {
  // Initialize ICU function pointers.
  J2ObjC_icu_init();
}

// Generated by running the translator over the java.lang.Class stub file.
+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "forName:", "forName", "Ljava.lang.Class;", 0x9, "Ljava.lang.ClassNotFoundException;",
      "(Ljava/lang/String;)Ljava/lang/Class<*>;" },
    { "forName:initialize:classLoader:", "forName", "Ljava.lang.Class;", 0x9,
      "Ljava.lang.ClassNotFoundException;",
      "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class<*>;" },
    { "asSubclass:", "asSubclass", "Ljava.lang.Class;", 0x1, NULL,
      "<U:Ljava/lang/Object;>(Ljava/lang/Class<TU;>;)Ljava/lang/Class<+TU;>;" },
    { "cast:", "cast", "TT;", 0x1, NULL, "(Ljava/lang/Object;)TT;" },
    { "desiredAssertionStatus", NULL, "Z", 0x1, NULL, NULL },
    { "getAnnotationWithIOSClass:", "getAnnotation", "TA;", 0x1, NULL,
      "<A::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TA;>;)TA;" },
    { "getAnnotations", NULL, "[Ljava.lang.annotation.Annotation;", 0x1, NULL, NULL },
    { "getCanonicalName", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "getClasses", NULL, "[Ljava.lang.Class;", 0x1, NULL, NULL },
    { "getClassLoader", NULL, "Ljava.lang.ClassLoader;", 0x1, NULL, NULL },
    { "getComponentType", NULL, "Ljava.lang.Class;", 0x1, NULL, "()Ljava/lang/Class<*>;" },
    { "getConstructor:", "getConstructor", "Ljava.lang.reflect.Constructor;", 0x81,
      "Ljava.lang.NoSuchMethodException;Ljava.lang.SecurityException;",
      "([Ljava/lang/Class<*>;)Ljava/lang/reflect/Constructor<TT;>;" },
    { "getConstructors", NULL, "[Ljava.lang.reflect.Constructor;", 0x1,
      "Ljava.lang.SecurityException;", NULL },
    { "getDeclaredAnnotations", NULL, "[Ljava.lang.annotation.Annotation;", 0x1, NULL, NULL },
    { "getDeclaredClasses", NULL, "[Ljava.lang.Class;", 0x1, "Ljava.lang.SecurityException;",
      NULL },
    { "getDeclaredConstructor:", "getDeclaredConstructor", "Ljava.lang.reflect.Constructor;", 0x81,
      "Ljava.lang.NoSuchMethodException;Ljava.lang.SecurityException;",
      "([Ljava/lang/Class<*>;)Ljava/lang/reflect/Constructor<TT;>;" },
    { "getDeclaredConstructors", NULL, "[Ljava.lang.reflect.Constructor;", 0x1,
      "Ljava.lang.SecurityException;", NULL },
    { "getDeclaredField:", "getDeclaredField", "Ljava.lang.reflect.Field;", 0x1,
      "Ljava.lang.NoSuchFieldException;Ljava.lang.SecurityException;", NULL },
    { "getDeclaredFields", NULL, "[Ljava.lang.reflect.Field;", 0x1,
      "Ljava.lang.SecurityException;", NULL },
    { "getDeclaredMethod:parameterTypes:", "getDeclaredMethod", "Ljava.lang.reflect.Method;", 0x81,
      "Ljava.lang.NoSuchMethodException;Ljava.lang.SecurityException;", NULL },
    { "getDeclaredMethods", NULL, "[Ljava.lang.reflect.Method;", 0x1,
      "Ljava.lang.SecurityException;", NULL },
    { "getDeclaringClass", NULL, "Ljava.lang.Class;", 0x1, NULL, "()Ljava/lang/Class<*>;" },
    { "getEnclosingClass", NULL, "Ljava.lang.Class;", 0x1, NULL, "()Ljava/lang/Class<*>;" },
    { "getEnclosingConstructor", NULL, "Ljava.lang.reflect.Constructor;", 0x1, NULL,
      "()Ljava/lang/reflect/Constructor<*>;" },
    { "getEnclosingMethod", NULL, "Ljava.lang.reflect.Method;", 0x1, NULL, NULL },
    { "getEnumConstants", NULL, "[Ljava.lang.Object;", 0x1, NULL, NULL },
    { "getField:", "getField", "Ljava.lang.reflect.Field;", 0x1,
      "Ljava.lang.NoSuchFieldException;Ljava.lang.SecurityException;", NULL },
    { "getFields", NULL, "[Ljava.lang.reflect.Field;", 0x1, "Ljava.lang.SecurityException;", NULL },
    { "getGenericInterfaces", NULL, "[Ljava.lang.reflect.Type;", 0x1, NULL, NULL },
    { "getGenericSuperclass", NULL, "Ljava.lang.reflect.Type;", 0x1, NULL, NULL },
    { "getInterfaces", NULL, "[Ljava.lang.Class;", 0x1, NULL, NULL },
    { "getMethod:parameterTypes:", "getMethod", "Ljava.lang.reflect.Method;", 0x81,
      "Ljava.lang.NoSuchMethodException;Ljava.lang.SecurityException;", NULL },
    { "getMethods", NULL, "[Ljava.lang.reflect.Method;", 0x1, "Ljava.lang.SecurityException;",
      NULL },
    { "getModifiers", NULL, "I", 0x1, NULL, NULL },
    { "getName", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "getPackage", NULL, "Ljava.lang.Package;", 0x1, NULL, NULL },
    { "getProtectionDomain", NULL, "Ljava.security.ProtectionDomain;", 0x1, NULL, NULL },
    { "getResource:", "getResource", "Ljava.net.URL;", 0x1, NULL, NULL },
    { "getResourceAsStream:", "getResourceAsStream", "Ljava.io.InputStream;", 0x1, NULL, NULL },
    { "getSigners", NULL, "[Ljava.lang.Object;", 0x1, NULL, NULL },
    { "getSimpleName", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "getSuperclass", NULL, "Ljava.lang.Class;", 0x1, NULL, "()Ljava/lang/Class<-TT;>;" },
    { "getTypeParameters", NULL, "[Ljava.lang.reflect.TypeVariable;", 0x1, NULL, NULL },
    { "isAnnotation", NULL, "Z", 0x1, NULL, NULL },
    { "isAnnotationPresentWithIOSClass:", "isAnnotationPresent", "Z", 0x1, NULL,
      "(Ljava/lang/Class<+Ljava/lang/annotation/Annotation;>;)Z" },
    { "isAnonymousClass", NULL, "Z", 0x1, NULL, NULL },
    { "isArray", NULL, "Z", 0x1, NULL, NULL },
    { "isAssignableFrom:", "isAssignableFrom", "Z", 0x1, NULL, "(Ljava/lang/Class<*>;)Z" },
    { "isEnum", NULL, "Z", 0x1, NULL, NULL },
    { "isInstance:", "isInstance", "Z", 0x1, NULL, NULL },
    { "isInterface", NULL, "Z", 0x1, NULL, NULL },
    { "isLocalClass", NULL, "Z", 0x1, NULL, NULL },
    { "isMemberClass", NULL, "Z", 0x1, NULL, NULL },
    { "isPrimitive", NULL, "Z", 0x1, NULL, NULL },
    { "isSynthetic", NULL, "Z", 0x1, NULL, NULL },
    { "newInstance", NULL, "TT;", 0x1,
      "Ljava.lang.InstantiationException;Ljava.lang.IllegalAccessException;", "()TT;" },
    { "description", "toString", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "init", NULL, NULL, 0x1, NULL, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "serialVersionUID", "serialVersionUID", 0x1a, "J", NULL, NULL,
      .constantValue.asLong = IOSClass_serialVersionUID },
  };
  static const J2ObjcClassInfo _IOSClass = {
    2, "Class", "java.lang", NULL, 0x11, 58, methods, 1, fields, 0, NULL, 0, NULL, NULL,
    "<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/lang/reflect/AnnotatedElement;"
    "Ljava/lang/reflect/GenericDeclaration;Ljava/io/Serializable;Ljava/lang/reflect/Type;" };
  return &_IOSClass;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(IOSClass)
