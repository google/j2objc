/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

package com.google.devtools.j2objc.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.types.PointerType;
import com.google.j2objc.annotations.ObjectiveCName;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;

/**
 * Singleton service for type/method/variable name support.
 *
 * @author Tom Ball
 */
public class NameTable {

  private final TypeUtil typeUtil;
  private final ElementUtil elementUtil;
  private final CaptureInfo captureInfo;
  private final Map<VariableElement, String> variableNames = new HashMap<>();
  private final Map<ExecutableElement, String> methodSelectorCache = new HashMap<>();
  private final Map<TypeElement, String> fullNameCache = new HashMap<>();

  public static final String INIT_NAME = "init";
  public static final String RETAIN_METHOD = "retain";
  public static final String RELEASE_METHOD = "release";
  public static final String DEALLOC_METHOD = "dealloc";
  public static final String FINALIZE_METHOD = "finalize";

  // The JDT compiler requires package-info files be named as "package-info",
  // but that's an illegal type to generate.
  public static final String PACKAGE_INFO_CLASS_NAME = "package-info";
  private static final String PACKAGE_INFO_OBJC_NAME = "package_info";

  // The self name in Java is reserved in Objective-C, but functionized methods
  // actually want the first parameter to be self. This is an internal name,
  // converted to self during generation.
  public static final String SELF_NAME = "$$self$$";
  public static final String ID_TYPE = "id";

  private static final Logger logger = Logger.getLogger(NameTable.class.getName());

  private static final String RESERVED_NAMES_FILE = "reserved_names.txt";
  private static final Splitter RESERVED_NAMES_SPLITTER =
      Splitter.on(CharMatcher.whitespace()).omitEmptyStrings();

  /**
   * The list of predefined types, common primitive typedefs, constants and
   * variables. Loaded from a resource file.
   */
  private static final ImmutableSet<String> reservedNames = loadReservedNames();

  // Regex pattern for fully-qualified Java class or package names.
  private static final String JAVA_CLASS_NAME_REGEX
      = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*"
          + "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
  private static final Pattern JAVA_CLASS_NAME_PATTERN = Pattern.compile(JAVA_CLASS_NAME_REGEX);


  private static ImmutableSet<String> loadReservedNames() {
    try (InputStream stream = J2ObjC.class.getResourceAsStream(RESERVED_NAMES_FILE);
        BufferedReader lines = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
      ImmutableSet.Builder<String> builder = ImmutableSet.builder();
      String line;
      while ((line = lines.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }
        builder.addAll(RESERVED_NAMES_SPLITTER.split(line));
      }
      return builder.build();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private static final ImmutableSet<String> badParameterNames = ImmutableSet.of(
      // Objective-C type qualifier keywords.
      "in", "out", "inout", "oneway", "bycopy", "byref");

  /**
   * List of NSObject message names.  Java methods with one of these names are
   * renamed to avoid unintentional overriding.  Message names with trailing
   * colons are not included since they can't be overridden.  For example,
   * "public boolean isEqual(Object o)" would be translated as
   * "- (BOOL)isEqualWithObject:(NSObject *)o", not NSObject's "isEqual:".
   */
  private static final ImmutableSet<String> nsObjectMessages = ImmutableSet.of(
      "alloc", "attributeKeys", "autoContentAccessingProxy", "autorelease",
      "classCode", "classDescription", "classForArchiver",
      "classForKeyedArchiver", "classFallbacksForKeyedArchiver",
      "classForPortCoder", "className", "copy", "dealloc", "description",
      "hash", "init", "initialize", "isProxy", "load", "mutableCopy", "new",
      "release", "retain", "retainCount", "scriptingProperties", "self",
      "superclass", "toManyRelationshipKeys", "toOneRelationshipKeys",
      "version");

  /**
   * Map of package names to their specified prefixes.  Multiple packages
   * can share a prefix; for example, the com.google.common packages in
   * Guava could share a "GG" (Google Guava) or simply "Guava" prefix.
   */
  private final PackagePrefixes prefixMap;

  private final ImmutableMap<String, String> classMappings;
  private final ImmutableMap<String, String> methodMappings;

  public NameTable(TypeUtil typeUtil, CaptureInfo captureInfo, Options options) {
    this.typeUtil = typeUtil;
    this.elementUtil = typeUtil.elementUtil();
    this.captureInfo = captureInfo;
    prefixMap = options.getPackagePrefixes();
    classMappings = options.getMappings().getClassMappings();
    methodMappings = options.getMappings().getMethodMappings();
  }

  public void setVariableName(VariableElement var, String name) {
    String previousName = variableNames.get(var);
    if (previousName != null && !previousName.equals(name)) {
      logger.fine(String.format("Changing previous rename for variable: %s. Was: %s, now: %s",
          var.toString(), previousName, name));
    }
    variableNames.put(var, name);
  }

  /**
   * Gets the variable name without any qualifying class name or other prefix
   * or suffix attached.
   */
  public String getVariableBaseName(VariableElement var) {
    return getVarBaseName(var, ElementUtil.isGlobalVar(var));
  }

  /**
   * Gets the name of the accessor method for a static variable.
   */
  public String getStaticAccessorName(VariableElement var) {
    return getVarBaseName(var, false);
  }

  private String getVarBaseName(VariableElement var, boolean allowReservedName) {
    String name = variableNames.get(var);
    if (name != null) {
      return name;
    }
    name = ElementUtil.getName(var);
    if (allowReservedName) {
      return name;
    }
    name = maybeRenameVar(var, name);
    return name.equals(SELF_NAME) ? "self" : name;
  }

  private static String maybeRenameVar(VariableElement var, String name) {
    if (isReservedName(name)) {
      name += '_';
    } else if (ElementUtil.isParameter(var) && badParameterNames.contains(name)) {
      name += "Arg";
    }
    return name;
  }

  /**
   * Gets the variable or parameter name that should be used in a doc-comment.
   * This may be wrong if a variable is renamed by a translation phase, but will
   * handle all the reserved and bad parameter renamings correctly.
   */
  public static String getDocCommentVariableName(VariableElement var) {
    return maybeRenameVar(var, ElementUtil.getName(var));
  }

  /**
   * Gets the non-qualified variable name, with underscore suffix.
   */
  public String getVariableShortName(VariableElement var) {
    String baseName = getVariableBaseName(var);
    if (var.getKind().isField() && !ElementUtil.isGlobalVar(var)) {
      return baseName + '_';
    }
    return baseName;
  }

  /**
   * Gets the name of the variable as it is declared in ObjC, fully qualified.
   */
  public String getVariableQualifiedName(VariableElement var) {
    String shortName = getVariableShortName(var);
    if (ElementUtil.isGlobalVar(var)) {
      String className = getFullName(ElementUtil.getDeclaringClass(var));
      if (ElementUtil.isEnumConstant(var)) {
        // Enums are declared in an array, so we use a macro to shorten the
        // array access expression.
        return "JreEnum(" + className + ", " + shortName + ")";
      }
      return className + '_' + shortName;
    }
    return shortName;
  }

  /**
   * Returns the name of an annotation property variable, extracted from its accessor element.
   */
  public static String getAnnotationPropertyName(ExecutableElement element) {
    return getMethodName(element);
  }

  /**
   * Capitalize the first letter of a string.
   */
  public static String capitalize(String s) {
    return s.length() > 0 ? Character.toUpperCase(s.charAt(0)) + s.substring(1) : s;
  }

  /**
   * Given a period-separated name, return as a camel-cased type name.  For
   * example, java.util.logging.Level is returned as JavaUtilLoggingLevel.
   */
  public static String camelCaseQualifiedName(String fqn) {
    StringBuilder sb = new StringBuilder();
    for (String part : fqn.split("\\.")) {
      sb.append(capitalize(part));
    }
    return sb.toString();
  }

  /**
   * Given a path, return as a camel-cased name. Used, for example, in header guards.
   */
  public static String camelCasePath(String fqn) {
    StringBuilder sb = new StringBuilder();
    for (String part : fqn.split(Pattern.quote(File.separator))) {
      sb.append(capitalize(part));
    }
    return sb.toString();
  }

  private static final Pattern FAMILY_METHOD_REGEX =
      Pattern.compile("^[_]*(new|copy|alloc|init|mutableCopy).*");

  public static boolean needsObjcMethodFamilyNoneAttribute(String name) {
     return FAMILY_METHOD_REGEX.matcher(name).matches();
  }

  private String getParameterTypeKeyword(TypeMirror type) {
    int arrayDimensions = 0;
    while (TypeUtil.isArray(type)) {
      type = ((ArrayType) type).getComponentType();
      arrayDimensions++;
    }
    String name;
    if (type.getKind().isPrimitive()) {
      name = TypeUtil.getName(type);
    } else {
      // For type variables, use the first bound for the parameter keyword.
      List<? extends TypeMirror> bounds = typeUtil.getUpperBounds(type);
      TypeElement elem = bounds.isEmpty()
          ? TypeUtil.NS_OBJECT : typeUtil.getObjcClass(bounds.get(0));
      assert elem != null;
      if (arrayDimensions == 0 && elem.equals(TypeUtil.NS_OBJECT)) {
        // Special case: Non-array object types become "id".
        return ID_TYPE;
      }
      name = getFullName(elem);
    }
    if (arrayDimensions > 0) {
      name += "Array";
      if (arrayDimensions > 1) {
        name += arrayDimensions;
      }
    }
    return name;
  }

  private String parameterKeyword(TypeMirror type) {
    return "with" + capitalize(getParameterTypeKeyword(type));
  }

  private static final Pattern SELECTOR_VALIDATOR = Pattern.compile("\\w+|(\\w+:)+");

  private static void validateMethodSelector(String selector) {
    if (!SELECTOR_VALIDATOR.matcher(selector).matches()) {
      ErrorUtil.error("Invalid method selector: " + selector);
    }
  }

  private static String getMethodName(ExecutableElement method) {
    if (ElementUtil.isConstructor(method)) {
      return "init";
    }
    String name = ElementUtil.getName(method);
    if (isReservedName(name)) {
      name += "__";
    }
    return name;
  }

  private boolean appendParamKeyword(
      StringBuilder sb, TypeMirror paramType, char delim, boolean first) {
    String keyword = parameterKeyword(paramType);
    if (first) {
      keyword = capitalize(keyword);
    }
    sb.append(keyword).append(delim);
    return false;
  }

  private String addParamNames(ExecutableElement method, String name, char delim) {
    StringBuilder sb = new StringBuilder(name);
    boolean first = true;
    TypeElement declaringClass = ElementUtil.getDeclaringClass(method);
    if (ElementUtil.isConstructor(method)) {
      for (VariableElement param : captureInfo.getImplicitPrefixParams(declaringClass)) {
        first = appendParamKeyword(sb, param.asType(), delim, first);
      }
    }
    for (VariableElement param : method.getParameters()) {
      first = appendParamKeyword(sb, param.asType(), delim, first);
    }
    if (ElementUtil.isConstructor(method)) {
      for (VariableElement param : captureInfo.getImplicitPostfixParams(declaringClass)) {
        first = appendParamKeyword(sb, param.asType(), delim, first);
      }
    }
    return sb.toString();
  }

  public String getMethodSelector(ExecutableElement method) {
    String selector = methodSelectorCache.get(method);
    if (selector != null) {
      return selector;
    }
    selector = getMethodSelectorInner(method);
    methodSelectorCache.put(method, selector);
    return selector;
  }

  private String getMethodSelectorInner(ExecutableElement method) {
    String selector = ElementUtil.getSelector(method);
    if (selector != null) {
      return selector;
    }
    if (ElementUtil.isInstanceMethod(method)) {
      method = getOriginalMethod(method);
    }
    selector = getRenamedMethodName(method);
    return selectorForMethodName(method, selector != null ? selector : getMethodName(method));
  }

  private String getRenamedMethodName(ExecutableElement method) {
    String selector = methodMappings.get(Mappings.getMethodKey(method, typeUtil));
    if (selector != null) {
      validateMethodSelector(selector);
      return selector;
    }
    selector = getMethodNameFromAnnotation(method);
    if (selector != null) {
      return selector;
    }
    return null;
  }

  public String selectorForMethodName(ExecutableElement method, String name) {
    if (name.contains(":")) {
      return name;
    }
    return addParamNames(method, name, ':');
  }

  /**
   * Returns a "Type_method" function name for static methods, such as from
   * enum types. A combination of classname plus modified selector is
   * guaranteed to be unique within the app.
   */
  public String getFullFunctionName(ExecutableElement method) {
    return getFullName(ElementUtil.getDeclaringClass(method)) + '_' + getFunctionName(method);
  }

  /**
   * Returns the name of the allocating constructor that returns a retained
   * object. The name will take the form of "new_TypeName_ConstructorName".
   */
  public String getAllocatingConstructorName(ExecutableElement method) {
    return "new_" + getFullFunctionName(method);
  }

  /**
   * Returns the name of the allocating constructor that returns a released
   * object. The name will take the form of "create_TypeName_ConstructorName".
   */
  public String getReleasingConstructorName(ExecutableElement method) {
    return "create_" + getFullFunctionName(method);
  }

  /**
   * Returns an appropriate name to use for this method as a function. This name
   * is guaranteed to be unique within the declaring class, if no methods in the
   * class have a renaming. The returned name should be given an appropriate
   * prefix to avoid collisions with methods from other classes.
   */
  public String getFunctionName(ExecutableElement method) {
    String name = ElementUtil.getSelector(method);
    if (name == null) {
      name = getRenamedMethodName(method);
    }
    if (name != null) {
      return name.replaceAll(":", "_");
    } else {
      return addParamNames(method, getMethodName(method), '_');
    }
  }

  public static String getMethodNameFromAnnotation(ExecutableElement method) {
    AnnotationMirror annotation = ElementUtil.getAnnotation(method, ObjectiveCName.class);
    if (annotation != null) {
      String value = (String) ElementUtil.getAnnotationValue(annotation, "value");
      validateMethodSelector(value);
      return value;
    }
    return null;
  }

  private ExecutableElement getOriginalMethod(ExecutableElement method) {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(method);
    return getOriginalMethod(method, declaringClass, declaringClass);
  }

  /**
   * Finds the original method element to use for generating a selector. The method returned is the
   * first method found in the hierarchy while traversing in order of declared inheritance that
   * doesn't override a method from a supertype. (ie. it is the first leaf node found in the tree of
   * overriding methods)
   */
  private ExecutableElement getOriginalMethod(
      ExecutableElement topMethod, TypeElement declaringClass, TypeElement currentType) {
    if (currentType == null) {
      return null;
    }
    // TODO(tball): simplify to ElementUtil.getSuperclass() when javac update is complete.
    TypeElement superclass = currentType.getKind().isInterface()
        ? typeUtil.getJavaObject() : ElementUtil.getSuperclass(currentType);
    ExecutableElement original = getOriginalMethod(topMethod, declaringClass, superclass);
    if (original != null) {
      return original;
    }
    for (TypeMirror supertype : currentType.getInterfaces()) {
      original = getOriginalMethod(topMethod, declaringClass, TypeUtil.asTypeElement(supertype));
      if (original != null) {
        return original;
      }
    }
    if (declaringClass == currentType) {
      return topMethod;
    }
    for (ExecutableElement candidate : ElementUtil.getMethods(currentType)) {
      if (ElementUtil.isInstanceMethod(candidate)
          && elementUtil.overrides(topMethod, candidate, declaringClass)) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Converts a Java type to an equivalent Objective-C type, returning "id" for an object type.
   */
  public static String getPrimitiveObjCType(TypeMirror type) {
    return TypeUtil.isVoid(type) ? "void"
        : type.getKind().isPrimitive() ? "j" + TypeUtil.getName(type) : "id";
  }

  /**
   * Convert a Java type to an equivalent Objective-C type with type variables
   * resolved to their bounds.
   */
  public String getObjCType(TypeMirror type) {
    return getObjcTypeInner(type, null);
  }

  public String getObjCType(VariableElement var) {
    return getObjcTypeInner(var.asType(), ElementUtil.getTypeQualifiers(var));
  }

  /**
   * Convert a Java type into the equivalent JNI type.
   */
  public String getJniType(TypeMirror type) {
    if (TypeUtil.isPrimitiveOrVoid(type)) {
      return getPrimitiveObjCType(type);
    } else if (TypeUtil.isArray(type)) {
      return "jarray";
    } else if (typeUtil.isString(type)) {
      return "jstring";
    } else if (typeUtil.isClassType(type)) {
      return "jclass";
    }
    return "jobject";
  }

  private String getObjcTypeInner(TypeMirror type, String qualifiers) {
    String objcType;
    if (type instanceof NativeType) {
      objcType = ((NativeType) type).getName();
    } else if (type instanceof PointerType) {
      String pointeeQualifiers = null;
      if (qualifiers != null) {
        int idx = qualifiers.indexOf('*');
        if (idx != -1) {
          pointeeQualifiers = qualifiers.substring(0, idx);
          qualifiers = qualifiers.substring(idx + 1);
        }
      }
      objcType = getObjcTypeInner(((PointerType) type).getPointeeType(), pointeeQualifiers);
      objcType = objcType.endsWith("*") ? objcType + "*" : objcType + " *";
    } else if (TypeUtil.isPrimitiveOrVoid(type)) {
      objcType = getPrimitiveObjCType(type);
    } else {
      objcType = constructObjcTypeFromBounds(type);
    }
    if (qualifiers != null) {
      qualifiers = qualifiers.trim();
      if (!qualifiers.isEmpty()) {
        objcType += " " + qualifiers;
      }
    }
    return objcType;
  }

  private String constructObjcTypeFromBounds(TypeMirror type) {
    String classType = null;
    List<String> interfaces = new ArrayList<>();
    for (TypeElement bound : typeUtil.getObjcUpperBounds(type)) {
      if (bound.getKind().isInterface()) {
        interfaces.add(getFullName(bound));
      } else {
        assert classType == null : "Cannot have multiple class bounds";
        classType = getFullName(bound);
      }
    }
    String protocols = interfaces.isEmpty() ? "" : "<" + Joiner.on(", ").join(interfaces) + ">";
    return classType == null ? ID_TYPE + protocols : classType + protocols + " *";
  }

  public static String getNativeEnumName(String typeName) {
    return typeName + "_Enum";
  }

  /**
   * Return the full name of a type, including its package.  For outer types,
   * is the type's full name; for example, java.lang.Object's full name is
   * "JavaLangObject".  For inner classes, the full name is their outer class'
   * name plus the inner class name; for example, java.util.ArrayList.ListItr's
   * name is "JavaUtilArrayList_ListItr".
   */
  public String getFullName(TypeElement element) {
    element = typeUtil.getObjcClass(element);
    String fullName = fullNameCache.get(element);
    if (fullName == null) {
      fullName = getFullNameImpl(element);
      fullNameCache.put(element, fullName);
    }
    return fullName;
  }

  private String getFullNameImpl(TypeElement element) {
    // Avoid package prefix renaming for package-info types, and use a valid ObjC name that doesn't
    // have a dash character.
    if (ElementUtil.isPackageInfo(element)) {
      return camelCaseQualifiedName(ElementUtil.getName(ElementUtil.getPackage(element)))
          + PACKAGE_INFO_OBJC_NAME;
    }

    // Use ObjectiveCName annotation, if it exists.
    AnnotationMirror annotation = ElementUtil.getAnnotation(element, ObjectiveCName.class);
    if (annotation != null) {
      return (String) ElementUtil.getAnnotationValue(annotation, "value");
    }

    TypeElement outerClass = ElementUtil.getDeclaringClass(element);
    if (outerClass != null) {
      return getFullName(outerClass) + '_' + getTypeSubName(element);
    }

    // Use mapping file entry, if it exists.
    String mappedName = classMappings.get(ElementUtil.getQualifiedName(element));
    if (mappedName != null) {
      return mappedName;
    }

    // Use camel-cased package+class name.
    return getPrefix(ElementUtil.getPackage(element)) + getTypeSubName(element);
  }

  private String getTypeSubName(TypeElement element) {
    if (ElementUtil.isLambda(element)) {
      return ElementUtil.getName(element);
    } else if (ElementUtil.isLocal(element)) {
      String binaryName = elementUtil.getBinaryName(element);
      int innerClassIndex = ElementUtil.isAnonymous(element)
          ? binaryName.length() : binaryName.lastIndexOf(ElementUtil.getName(element));
      while (innerClassIndex > 0 && binaryName.charAt(innerClassIndex - 1) != '$') {
        --innerClassIndex;
      }
      return binaryName.substring(innerClassIndex);
    }
    return ElementUtil.getName(element).replace('$', '_');
  }

  private static boolean isReservedName(String name) {
    return reservedNames.contains(name) || nsObjectMessages.contains(name);
  }

  private String getPrefix(PackageElement packageElement) {
    return prefixMap.getPrefix(packageElement);
  }

  /** Ignores the ObjectiveCName annotation. */
  private String getDefaultObjectiveCName(TypeElement element) {
    String binaryName = elementUtil.getBinaryName(element);
    return camelCaseQualifiedName(binaryName).replace('$', '_');
  }

  public Optional<String> getNameMapping(TypeElement typeElement, String typeName) {
    final String mappingFormat = "J2OBJC_NAME_MAPPING(%s, \"%s\", \"%s\")\n";
    // No mapping is needed if the default Objective-C name was not modified.
    if (typeName.equals(getDefaultObjectiveCName(typeElement))) {
      return Optional.empty();
    }

    // Return a class mapping only if there is a explicit rename.
    AnnotationMirror annotation = ElementUtil.getAnnotation(typeElement, ObjectiveCName.class);
    String mappedName = classMappings.get(ElementUtil.getQualifiedName(typeElement));
    if (annotation != null || mappedName != null) {
      return Optional.of(
          String.format(mappingFormat, typeName, elementUtil.getBinaryName(typeElement), typeName));
    }

    // Otherwise, there was a package rename. Because only one package mapping is needed per
    // generation unit, it is safe to generate it together with a public class.
    if (ElementUtil.isTopLevel(typeElement) && ElementUtil.isPublic(typeElement)) {
      PackageElement packageElement = ElementUtil.getPackage(typeElement);
      String packageName = packageElement.getQualifiedName().toString();
      String mappedPackageName = getPrefix(packageElement);
      return Optional.of(
          String.format(mappingFormat, typeName, packageName, mappedPackageName));
    }

    return Optional.empty();
  }

  /**
   * Verifies that a fully-qualified class name is lexically correct. This method does
   * not check whether the class actually exists, however. It also will return true for
   * valid package names, since they cannot be distinguished except by parsing context.
   */
  public static boolean isValidClassName(String className) {
    return JAVA_CLASS_NAME_PATTERN.matcher(className).matches();
  }
}
