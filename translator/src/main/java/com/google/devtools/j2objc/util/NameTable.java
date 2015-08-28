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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.PointerTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.j2objc.annotations.ObjectiveCName;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Singleton service for type/method/variable name support.
 *
 * @author Tom Ball
 */
public class NameTable {

  private final Types typeEnv;
  private final Map<IVariableBinding, String> variableNames = Maps.newHashMap();

  public static final String INIT_NAME = "init";
  public static final String ALLOC_METHOD = "alloc";
  public static final String RETAIN_METHOD = "retain";
  public static final String RELEASE_METHOD = "release";
  public static final String AUTORELEASE_METHOD = "autorelease";
  public static final String DEALLOC_METHOD = "dealloc";
  public static final String FINALIZE_METHOD = "finalize";

  // The JDT compiler requires package-info files be named as "package-info",
  // but that's an illegal type to generate.
  public static final String PACKAGE_INFO_FILE_NAME = "package-info";
  public static final String PACKAGE_INFO_MAIN_TYPE = "package_info";

  // The self name in Java is reserved in Objective-C, but functionized methods
  // actually want the first parameter to be self. This is an internal name,
  // converted to self during generation.
  public static final String SELF_NAME = "$$self$$";

  public static final String ID_TYPE = "id";
  // This is syntactic sugar for blocks. All block are typed as ids, but we add a block_type typedef
  // for source clarity.
  public static final String BLOCK_TYPE = "block_type";

  private static final Logger logger = Logger.getLogger(NameTable.class.getName());

  /**
   * The list of predefined types, common primitive typedefs, constants and
   * variables.
   */
  public static final Set<String> reservedNames = Sets.newHashSet(
      // types
      "id", "bool", "BOOL", "SEL", "IMP", "unichar",

      // constants
      "nil", "Nil", "YES", "NO", "TRUE", "FALSE",

      // C99 keywords
      "auto", "const", "entry", "extern", "goto", "inline", "register", "restrict", "signed",
      "sizeof", "struct", "typedef", "union", "unsigned", "volatile",

      // C++ keywords
      "and", "and_eq", "asm", "bitand", "bitor", "compl", "const_cast", "delete", "dynamic_cast",
      "explicit", "export", "friend", "mutable", "namespace", "not", "not_eq", "operator", "or",
      "or_eq", "reinterpret_cast", "static_cast", "template", "typeid", "typename", "using",
      "virtual", "wchar_t", "xor", "xor_eq",

      // variables
      "self", "isa",

      // Definitions from standard C and Objective-C headers, not including
      // typedefs and #defines that start with "_", nor #defines for
      // functions.  Some of these may seem very unlikely to be used in
      // Java source, but if a name is legal some Java developer might very
      // well use it.

      // Definitions from stddef.h
      "ptrdiff_t", "size_t", "wchar_t", "wint_t",

      // Definitions from stdint.h
      "int8_t", "int16_t", "int32_t", "int64_t", "uint8_t", "uint16_t", "uint32_t", "uint64_t",
      "int_least8_t", "int_least16_t", "int_least32_t", "int_least64_t",
      "uint_least8_t", "uint_least16_t", "uint_least32_t", "uint_least64_t",
      "int_fast8_t", "int_fast16_t", "int_fast32_t", "int_fast64_t",
      "uint_fast8_t", "uint_fast16_t", "uint_fast32_t", "uint_fast64_t",
      "intptr_t", "uintptr_t", "intmax_t", "uintmax_t",
      "INT8_MAX", "INT16_MAX", "INT32_MAX", "INT64_MAX", "INT8_MIN", "INT16_MIN", "INT32_MIN",
      "INT64_MIN", "UINT8_MAX", "UINT16_MAX", "UINT32_MAX", "UINT64_MAX", "INT_LEAST8_MIN",
      "INT_LEAST16_MIN", "INT_LEAST32_MIN", "INT_LEAST64_MIN", "INT_LEAST8_MAX", "INT_LEAST16_MAX",
      "INT_LEAST32_MAX", "INT_LEAST64_MAX", "INT_FAST8_MIN", "INT_FAST16_MIN", "INT_FAST32_MIN",
      "INT_FAST64_MIN", "INT_FAST8_MAX", "INT_FAST16_MAX", "INT_FAST32_MAX", "INT_FAST64_MAX",
      "UINT_FAST8_MAX", "UINT_FAST16_MAX", "UINT_FAST32_MAX", "UINT_FAST64_MAX", "INTPTR_MIN",
      "INTPTR_MAX", "UINTPTR_MAX", "INTMAX_MIN", "INTMAX_MAX", "UINTMAX_MAX", "PTRDIFF_MIN",
      "PTRDIFF_MAX", "SIZE_MAX", "WCHAR_MAX", "WCHAR_MIN", "WINT_MIN", "WINT_MAX",
      "SIG_ATOMIC_MIN", "SIG_ATOMIC_MAX", "INT8_MAX", "INT16_MAX", "INT32_MAX", "INT64_MAX",
      "UINT8_C", "UINT16_C", "UINT32_C", "UINT64_C", "INTMAX_C", "UINTMAX_C",

      // Definitions from stdio.h
      "va_list", "fpos_t", "FILE", "off_t", "ssize_t", "BUFSIZ", "EOF", "FOPEN_MAX",
      "FILENAME_MAX", "R_OK", "SEEK_SET", "SEEK_CUR", "SEEK_END", "stdin", "STDIN_FILENO",
      "stdout", "STDOUT_FILENO", "stderr", "STDERR_FILENO", "TMP_MAX", "W_OK", "X_OK",

      // Definitions from stdlib.h
      "ct_rune_t", "rune_t", "div_t", "ldiv_t", "lldiv_t", "dev_t", "mode_t",
      "NULL", "EXIT_FAILURE", "EXIT_SUCCESS", "RAND_MAX", "MB_CUR_MAX", "MB_CUR_MAX_L",

      // Definitions from errno.h
      "errno", "EPERM", "ENOENT", "ESRCH", "EINTR", "EIO", "ENXIO", "E2BIG", "ENOEXEC",
      "EBADF", "ECHILD", "EDEADLK", "ENOMEM", "EACCES", "EFAULT", "ENOTBLK", "EBUSY",
      "EEXIST", "EXDEV", "ENODEV", "ENOTDIR", "EISDIR", "EINVAL", "ENFILE", "EMFILE",
      "ENOTTY", "ETXTBSY", "EFBIG", "ENOSPC", "ESPIPE", "EROFS", "EMLINK", "EPIPE",
      "EDOM", "ERANGE", "EAGAIN", "EWOULDBLOCK", "EINPROGRESS", "EALREADY", "ENOTSOCK",
      "EDESTADDRREQ", "EMSGSIZE", "EPROTOTYPE", "ENOPROTOOPT", "EPROTONOSUPPORT",
      "ESOCKTNOSUPPORT", "ENOTSUP", "ENOTSUPP", "EPFNOSUPPORT", "EAFNOSUPPORT", "EADDRINUSE",
      "EADDRNOTAVAIL", "ENETDOWN", "ENETUNREACH", "ENETRESET", "ECONNABORTED", "ECONNRESET",
      "ENOBUFS", "EISCONN", "ENOTCONN", "ESHUTDOWN", "ETOOMANYREFS", "ETIMEDOUT", "ECONNREFUSED",
      "ELOOP", "ENAMETOOLONG", "EHOSTDOWN", "EHOSTUNREACH", "ENOTEMPTY", "EPROCLIM", "EUSERS",
      "EDQUOT", "ESTALE", "EREMOTE", "EBADRPC", "ERPCMISMATCH", "EPROGUNAVAIL", "EPROGMISMATCH",
      "EPROCUNAVAIL", "ENOLCK", "ENOSYS", "EFTYPE", "EAUTH", "ENEEDAUTH", "EPWROFF", "EDEVERR",
      "EOVERFLOW", "EBADEXEC", "EBADARCH", "ESHLIBVERS", "EBADMACHO", "ECANCELED", "EIDRM",
      "ENOMSG", "ENOATTR", "EBADMSG", "EMULTIHOP", "ENODATA", "ENOLINK", "ENOSR", "ENOSTR",
      "EPROTO", "ETIME", "ENOPOLICY", "ENOTRECOVERABLE", "EOWNERDEAD", "EQFULL", "EILSEQ",
      "EOPNOTSUPP", "ELAST",

      // Definitions from fcntl.h
      "F_DUPFD", "F_GETFD", "F_SETFD", "F_GETFL", "F_SETFL", "F_GETOWN", "F_SETOWN",
      "F_GETLK", "F_SETLK", "F_SETLKW", "FD_CLOEXEC", "F_RDLCK", "F_UNLCK", "F_WRLCK",
      "SEEK_SET", "SEEK_CUR", "SEEK_END",
      "O_RDONLY", "O_WRONLY", "O_RDWR", "O_ACCMODE", "O_NONBLOCK", "O_APPEND", "O_SYNC", "O_CREAT",
      "O_TRUNC", "O_EXCL", "O_NOCTTY", "O_NOFOLLOW",

      // Definitions from math.h
      "DOMAIN", "HUGE", "INFINITY", "NAN", "OVERFLOW", "SING", "UNDERFLOW",

      // Definitions from mman.h
      "MAP_FIXED", "MAP_PRIVATE", "MAP_SHARED", "MCL_CURRENT", "MCL_FUTURE", "MS_ASYNC",
      "MS_INVALIDATE", "MS_SYNC", "PROT_EXEC", "PROT_NONE", "PROT_READ", "PROT_WRITE",

      // Definitions from netdb.h
      "AI_ADDRCONFIG", "AI_ALL", "AI_CANONNAME", "AI_NUMERICHOST", "AI_NUMERICSERV",
      "AI_PASSIVE", "AI_V4MAPPED", "EAI_AGAIN", "EAI_BADFLAGS", "EAI_FAIL", "EAI_FAMILY",
      "EAI_MEMORY", "EAI_NODATA", "EAI_NONAME", "EAI_OVERFLOW", "EAI_SERVICE", "EAI_SOCKTYPE",
      "EAI_SYSTEM", "NI_NAMEREQD", "NI_NUMERICHOST",

      // Definitions from net/if.h
      "IFF_LOOPBACK", "IFF_MULTICAST", "IFF_POINTTOPOINT", "IFF_UP", "SIOCGIFADDR",
      "SIOCGIFBRDADDR", "SIOCGIFNETMASK", "SIOCGIFDSTADDR",

      // Definitions from netinet/in.h, in6.h
      "IPPROTO_IP", "IPPROTO_IPV6", "IPPROTO_TCP", "IPV6_MULTICAST_HOPS", "IPV6_MULTICAST_IF",
      "IP_MULTICAST_LOOP", "IPV6_TCLASS", "MCAST_JOIN_GROUP", "MCAST_JOIN_GROUP",

      // Definitions from socket.h
      "AF_INET", "AF_INET6", "AF_UNIX", "AF_UNSPEC", "MSG_OOB", "MSG_PEEK", "SHUT_RD", "SHUT_RDWR",
      "SHUT_WR", "SOCK_DGRAM", "SOCK_STREAM", "SOL_SOCKET", "SO_BINDTODEVICE", "SO_BROADCAST",
      "SO_ERROR", "SO_KEEPALIVE", "SO_LINGER", "SOOOBINLINE", "SO_REUSEADDR", "SO_RCVBUF",
      "SO_RCVTIMEO", "SO_SNDBUF", "TCP_NODELAY",

      // Definitions from stat.h
      "S_IFBLK", "S_IFCHR", "S_IFDIR", "S_IFIFO", "S_IFLNK", "S_IFMT", "S_IFREG", "S_IFSOCK",

      // Definitions from sys/poll.h
      "POLLERR", "POLLHUP", "POLLIN", "POLLOUT",

      // Definitions from sys/syslimits.h
      "ARG_MAX", "LINE_MAX", "MAX_INPUT", "NAME_MAX", "NZERO", "PATH_MAX",

      // Definitions from limits.h, machine/limits.h
      "CHAR_BIT", "CHAR_MAX", "CHAR_MIN", "INT_MAX", "INT_MIN", "LLONG_MAX", "LLONG_MIN",
      "LONG_BIT", "LONG_MAX", "LONG_MIN", "MB_LEN_MAX", "OFF_MIN", "OFF_MAX",
      "PTHREAD_DESTRUCTOR_ITERATIONS", "PTHREAD_KEYS_MAX", "PTHREAD_STACK_MIN", "QUAD_MAX",
      "QUAD_MIN", "SCHAR_MAX", "SCHAR_MIN", "SHRT_MAX", "SHRT_MIN", "SIZE_T_MAX", "SSIZE_MAX",
      "UCHAR_MAX", "UINT_MAX", "ULONG_MAX", "UQUAD_MAX", "USHRT_MAX", "UULONG_MAX", "WORD_BIT",

      // Definitions from types.h
      "S_IRGRP", "S_IROTH", "S_IRUSR", "S_IRWXG", "S_IRWXO", "S_IRWXU", "S_IWGRP", "S_IWOTH",
      "S_IWUSR", "S_IXGRP", "S_IXOTH", "S_IXUSR",

      // Definitions from unistd.h
      "F_OK", "R_OK", "STDERR_FILENO", "STDIN_FILENO", "STDOUT_FILENO", "W_OK", "X_OK",
      "_SC_PAGESIZE", "_SC_PAGE_SIZE",

      // Cocoa definitions from ConditionalMacros.h
      "CFMSYSTEMCALLS", "CGLUESUPPORTED", "FUNCTION_PASCAL", "FUNCTION_DECLSPEC",
      "FUNCTION_WIN32CC", "GENERATING68881", "GENERATING68K", "GENERATINGCFM", "GENERATINGPOWERPC",
      "OLDROUTINELOCATIONS", "PRAGMA_ALIGN_SUPPORTED", "PRAGMA_ENUM_PACK", "PRAGMA_ENUM_ALWAYSINT",
      "PRAGMA_ENUM_OPTIONS", "PRAGMA_IMPORT", "PRAGMA_IMPORT_SUPPORTED", "PRAGMA_ONCE",
      "PRAGMA_STRUCT_ALIGN", "PRAGMA_STRUCT_PACK", "PRAGMA_STRUCT_PACKPUSH",
      "TARGET_API_MAC_CARBON", "TARGET_API_MAC_OS8", "TARGET_API_MAC_OSX", "TARGET_CARBON",
      "TYPE_BOOL", "TYPE_EXTENDED", "TYPE_LONGDOUBLE_IS_DOUBLE", "TYPE_LONGLONG",
      "UNIVERSAL_INTERFACES_VERSION",

      // Core Foundation definitions
      "BIG_ENDIAN", "BYTE_ORDER", "LITTLE_ENDIAN", "PDP_ENDIAN",

      // CoreServices definitions
      "positiveInfinity", "negativeInfinity",

      // Common preprocessor definitions.
      "DEBUG", "NDEBUG",

      // Foundation methods with conflicting return types
      "scale",

      // Syntactic sugar for Objective-C block types
      "block_type");

  private static final Set<String> badParameterNames = Sets.newHashSet(
      // Objective-C type qualifier keywords.
      "in", "out", "inout", "oneway", "bycopy", "byref");

  /**
   * List of NSObject message names.  Java methods with one of these names are
   * renamed to avoid unintentional overriding.  Message names with trailing
   * colons are not included since they can't be overridden.  For example,
   * "public boolean isEqual(Object o)" would be translated as
   * "- (BOOL)isEqualWithObject:(NSObject *)o", not NSObject's "isEqual:".
   */
  public static final List<String> nsObjectMessages = Lists.newArrayList(
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

  private final Map<String, String> methodMappings;

  /**
   * Factory class for creating new NameTable instances.
   */
  public static class Factory {

    // Currently a shared map.
    // TODO(kstanger): For thread safety this will either need to be a
    // concurrent map, or make a copy for each NameTable.
    private PackagePrefixes prefixMap = Options.getPackagePrefixes();

    private final Map<String, String> methodMappings = ImmutableMap.copyOf(
        Maps.transformValues(Options.getMethodMappings(), EXTRACT_SELECTOR_FUNC));

    public NameTable newNameTable(Types typeEnv) {
      return new NameTable(typeEnv, prefixMap, methodMappings);
    }
  }

  public static Factory newFactory() {
    return new Factory();
  }

  private static final Function<String, String> EXTRACT_SELECTOR_FUNC =
      new Function<String, String>() {
    public String apply(String value) {
      return extractMethodSelector(value);
    }
  };

  private NameTable(
      Types typeEnv, PackagePrefixes prefixMap, Map<String, String> methodMappings) {
    this.typeEnv = typeEnv;
    this.prefixMap = prefixMap;
    this.methodMappings = methodMappings;
  }

  public void setVariableName(IVariableBinding var, String name) {
    var = var.getVariableDeclaration();
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
  public String getVariableBaseName(IVariableBinding var) {
    return getVarBaseName(var, BindingUtil.isPrimitiveConstant(var) || var.isEnumConstant());
  }

  /**
   * Gets the name of the accessor method for a static variable.
   */
  public String getStaticAccessorName(IVariableBinding var) {
    return getVarBaseName(var, false);
  }

  private String getVarBaseName(IVariableBinding var, boolean allowReservedName) {
    var = var.getVariableDeclaration();
    String name = variableNames.get(var);
    if (name != null) {
      return name;
    }
    name = var.getName();
    if (allowReservedName) {
      return name;
    }
    if (isReservedName(name)) {
      name += '_';
    } else if (var.isParameter() && badParameterNames.contains(name)) {
      name += "Arg";
    }
    return name.equals(SELF_NAME) ? "self" : name;
  }

  /**
   * Gets the non-qualified variable name, with underscore suffix.
   */
  public String getVariableShortName(IVariableBinding var) {
    String baseName = getVariableBaseName(var);
    if (var.isField() && !BindingUtil.isPrimitiveConstant(var) && !var.isEnumConstant()) {
      return baseName + '_';
    }
    return baseName;
  }

  /**
   * Gets the name of the variable as it is declared in ObjC, fully qualified.
   */
  public String getVariableQualifiedName(IVariableBinding var) {
    String shortName = getVariableShortName(var);
    if (BindingUtil.isGlobalVar(var)) {
      return getFullName(var.getDeclaringClass()) + '_' + shortName;
    }
    return shortName;
  }

  /**
   * Returns the name of an annotation property variable, extracted from its accessor binding.
   */
  public static String getAnnotationPropertyVariableName(IMethodBinding binding) {
    return getAnnotationPropertyName(binding) + '_';
  }

  /**
   * Returns the name of an annotation property variable, extracted from its accessor binding.
   */
  public static String getAnnotationPropertyName(IMethodBinding binding) {
    return getMethodName(binding);
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

  // TODO(kstanger): See whether the logic in this method can be simplified.
  //     Also, what about type variables?
  private String getArrayTypeParameterKeyword(ITypeBinding elementType, int dimensions) {
    if (elementType.isParameterizedType()) {
      elementType = elementType.getErasure();
    }
    if (elementType.isCapture()) {
      elementType = elementType.getWildcard();
    }
    if (elementType.isWildcardType()) {
      ITypeBinding bound = elementType.getBound();
      if (bound != null) {
        elementType = bound;
      }
    }
    String name = getFullName(elementType) + "Array";
    if (dimensions > 1) {
      name += dimensions;
    }
    return name;
  }

  private boolean isIdType(ITypeBinding type) {
    return type == typeEnv.resolveIOSType("id") || type == typeEnv.resolveIOSType("NSObject")
        || typeEnv.isJavaObjectType(type);
  }

  private String getParameterTypeKeyword(ITypeBinding type) {
    if (isIdType(type) || type.isTypeVariable()) {
      ITypeBinding[] bounds = type.getTypeBounds();
      if (bounds.length > 0) {
        return getParameterTypeKeyword(bounds[0]);
      }
      return ID_TYPE;
    } else if (type.isPrimitive()) {
      return type.getName();
    } else if (type.isArray()) {
      return getArrayTypeParameterKeyword(type.getElementType(), type.getDimensions());
    }
    return getFullName(type);
  }

  public String parameterKeyword(ITypeBinding type) {
    return "with" + capitalize(getParameterTypeKeyword(type));
  }

  // Matches the class name prefix or a parameter declarations of a method
  // signature. After removing these parts, the selector remains.
  private static final Pattern SIGNATURE_STRIPPER =
      Pattern.compile("^\\w* |\\s*\\([^)]*\\)\\s*\\w+\\s*");

  // TODO(kstanger): Phase out usage of full method signatures when renaming methods.
  private static String parseSelectorFromSignature(String s) {
    if (s.endsWith(";")) {
      s = s.substring(0, s.length() - 1);
    }
    Matcher matcher = SIGNATURE_STRIPPER.matcher(s);
    return matcher.replaceAll("");
  }

  private static final Pattern SELECTOR_VALIDATOR = Pattern.compile("\\w+|(\\w+\\:)+");

  private static boolean validateMethodSelector(String selector) {
    if (!SELECTOR_VALIDATOR.matcher(selector).matches()) {
      ErrorUtil.error("Invalid method selector: " + selector);
      return false;
    }
    return true;
  }

  // Be nice and only print the warning once per method.
  private static Set<String> renamingWarned = Sets.newHashSet();

  private static String extractMethodSelector(String value) {
    String selector = value;
    if (value.contains(" ") || value.contains("(")) {
      selector = parseSelectorFromSignature(value);
      if (validateMethodSelector(selector) && !renamingWarned.contains(value)) {
        ErrorUtil.warning("Method renaming with full signature is being phased out. "
            + "Please replace \"" + value + "\" with \"" + selector + "\".");
        renamingWarned.add(value);
      }
    } else {
      validateMethodSelector(selector);
    }
    return selector;
  }

  public static String getMethodName(IMethodBinding method) {
    if (method.isConstructor()) {
      return "init";
    }
    String name = method.getName();
    if (isReservedName(name)) {
      name += "__";
    }
    return name;
  }

  private String addParamNames(IMethodBinding method, String name, char delim) {
    method = method.getMethodDeclaration();
    StringBuilder sb = new StringBuilder(name);
    ITypeBinding[] paramTypes = method.getParameterTypes();
    for (int i = 0; i < paramTypes.length; i++) {
      String keyword = parameterKeyword(paramTypes[i]);
      if (i == 0) {
        keyword = capitalize(keyword);
      }
      sb.append(keyword).append(delim);
    }
    return sb.toString();
  }

  public String getMethodSelector(IMethodBinding method) {
    if (method instanceof IOSMethodBinding) {
      return ((IOSMethodBinding) method).getSelector();
    }
    if (BindingUtil.isDestructor(method)) {
      return DEALLOC_METHOD;
    }
    if (method.isConstructor() || BindingUtil.isStatic(method)) {
      return selectorForOriginalBinding(method);
    }
    return selectorForOriginalBinding(getOriginalMethodBindings(method).get(0));
  }

  private String getRenamedMethodName(IMethodBinding method) {
    method = method.getMethodDeclaration();
    String selector = methodMappings.get(BindingUtil.getMethodKey(method));
    if (selector != null) {
      return selector;
    }
    selector = getMethodNameFromAnnotation(method);
    if (selector != null) {
      return selector;
    }
    return null;
  }

  public String selectorForMethodName(IMethodBinding method, String name) {
    if (name.contains(":")) {
      return name;
    }
    return addParamNames(method, name, ':');
  }

  private String selectorForOriginalBinding(IMethodBinding method) {
    String selector = getRenamedMethodName(method);
    return selectorForMethodName(method, selector != null ? selector : getMethodName(method));
  }

  /**
   * In rare edge cases a single method will override two or more methods that
   * have different selectors. This returns the additional selectors that are
   * not returned by getMethodSelector().
   */
  public List<String> getExtraSelectors(IMethodBinding method) {
    if (method instanceof IOSMethodBinding || method.isConstructor() || BindingUtil.isStatic(method)
        || BindingUtil.isDestructor(method)) {
      return Collections.emptyList();
    }
    List<IMethodBinding> originalMethods = getOriginalMethodBindings(method);
    List<String> extraSelectors = Lists.newArrayList();
    String actualSelector = selectorForOriginalBinding(originalMethods.get(0));
    for (int i = 1; i < originalMethods.size(); i++) {
      String selector = selectorForOriginalBinding(originalMethods.get(i));
      if (!selector.equals(actualSelector)) {
        extraSelectors.add(selector);
      }
    }
    return extraSelectors;
  }

  /**
   * Returns a "Type_method" function name for static methods, such as from
   * enum types. A combination of classname plus modified selector is
   * guaranteed to be unique within the app.
   */
  public String getFullFunctionName(IMethodBinding method) {
    return getFullName(method.getDeclaringClass()) + '_' + getFunctionName(method);
  }

  /**
   * If a referenced method isn't a varargs method, then we can use the selector of the method
   * binding to uniquely identify method references. If it is a varargs method, then we need to
   * identify the reference by functional interface selector, as it contains the actual argument
   * layout for this instance.
   */
  public String getMethodReferenceName(IMethodBinding method, IMethodBinding functionalInterface) {
    if (method.isVarargs()) {
      return getFullName(method.getDeclaringClass()) + '_'
          + addParamNames(functionalInterface.getMethodDeclaration(), getMethodName(method), '_');
    } else {
      return getFullFunctionName(method);
    }
  }

  /**
   * Increments and returns a generic argument, as needed by lambda wrapper blocks. Possible
   * variable names range from 'a' to 'zz'. This only supports 676 arguments, but this more than the
   * java limit of 255 / 254 parameters for static / non-static parameters, respectively.
   */
  public char[] incrementVariable(char[] var) {
    if (var == null) {
      return new char[] { 'a' };
    }
    if (var[var.length - 1]++ == 'z') {
      if (var.length == 1) {
        var = new char[2];
        var[0] = 'a';
      } else {
        var[0]++;
      }
      var[1] = 'a';
    }
    return var;
  }

  /**
   * Similar to getFullFunctionName, but doesn't add the selector to the name, as lambda expressions
   * cannot be overloaded.
   */
  public String getFullLambdaName(IMethodBinding method) {
    return getFullName(method.getDeclaringClass()) + '_' + method.getName();
  }

  /**
   * Returns the name of the allocating constructor wrapper. The name will take
   * the form of "new_TypeName_ConstructorName".
   */
  public String getAllocatingConstructorName(IMethodBinding method) {
    return "new_" + getFullFunctionName(method);
  }

  /**
   * Returns an appropriate name to use for this method as a function. This name
   * is guaranteed to be unique within the declaring class, if no methods in the
   * class have a renaming. The returned name should be given an appropriate
   * prefix to avoid collisions with methods from other classes.
   */
  public String getFunctionName(IMethodBinding method) {
    method = method.getMethodDeclaration();
    String name = getRenamedMethodName(method);
    if (name != null) {
      return name.replaceAll(":", "_");
    } else {
      return addParamNames(method, getMethodName(method), '_');
    }
  }

  public static String getMethodNameFromAnnotation(IMethodBinding method) {
    IAnnotationBinding annotation = BindingUtil.getAnnotation(method, ObjectiveCName.class);
    if (annotation != null) {
      String value = (String) BindingUtil.getAnnotationValue(annotation, "value");
      return extractMethodSelector(value);
    }
    return null;
  }

  /**
   * Finds all the original overridden method bindings. If the the method is
   * overridden multiple times in the hierarchy, only the original is included.
   * Multiple results are still possible if the the given method overrides
   * methods from multiple interfaces or classes that do not share the same
   * hierarchy.
   */
  private List<IMethodBinding> getOriginalMethodBindings(IMethodBinding method) {
    method = method.getMethodDeclaration();
    if (method.isConstructor() || BindingUtil.isStatic(method)) {
      return Lists.newArrayList(method);
    }
    ITypeBinding declaringClass = method.getDeclaringClass();
    List<IMethodBinding> originalBindings = Lists.newArrayList();
    originalBindings.add(method);

    // Collect all the inherited types.
    // Predictable ordering is important, so we use a LinkedHashSet.
    Set<ITypeBinding> inheritedTypes = Sets.newLinkedHashSet();
    BindingUtil.collectAllInheritedTypes(declaringClass, inheritedTypes);
    if (declaringClass.isInterface()) {
      inheritedTypes.add(typeEnv.resolveJavaType("java.lang.Object"));
    }

    // Find all overridden methods.
    for (ITypeBinding inheritedType : inheritedTypes) {
      for (IMethodBinding interfaceMethod : inheritedType.getDeclaredMethods()) {
        if (method.overrides(interfaceMethod)) {
          originalBindings.add(interfaceMethod);
        }
      }
    }

    // Remove any overridden method that overrides another overriden method,
    // leaving only the original overridden methods. Usually there is just one
    // but not always.
    Iterator<IMethodBinding> iter = originalBindings.iterator();
    while (iter.hasNext()) {
      IMethodBinding inheritedMethod = iter.next();
      for (IMethodBinding otherInheritedMethod : originalBindings) {
        if (inheritedMethod != otherInheritedMethod
            && inheritedMethod.overrides(otherInheritedMethod)) {
          iter.remove();
          break;
        }
      }
    }

    return originalBindings;
  }

  /**
   * Converts a Java type to an equivalent Objective-C type, returning "id" for
   * an object type.
   */
  public static String getPrimitiveObjCType(ITypeBinding type) {
    return type.isPrimitive() ? (BindingUtil.isVoid(type) ? "void" : "j" + type.getName()) : "id";
  }

  /**
   * Convert a Java type to an equivalent Objective-C type with type variables
   * resolved to their bounds.
   */
  public String getSpecificObjCType(ITypeBinding type) {
    return getObjCTypeInner(type, null, true);
  }

  public String getSpecificObjCType(IVariableBinding var) {
    String qualifiers = null;
    if (var instanceof GeneratedVariableBinding) {
      qualifiers = ((GeneratedVariableBinding) var).getTypeQualifiers();
    }
    return getObjCTypeInner(var.getType(), qualifiers, true);
  }

  /**
   * Convert a Java type into the equivalent JNI type.
   */
  public String getJniType(ITypeBinding type) {
    if (type.isPrimitive()) {
      return getObjCType(type);
    }
    if (type.isArray()) {
      return "jarray";
    }
    if (type.getQualifiedName().equals("java.lang.String")) {
      return "jstring";
    }
    if (type.getQualifiedName().equals("java.lang.Class")) {
      return "jclass";
    }
    return "jobject";
  }

  /**
   * Convert a Java type to an equivalent Objective-C type with type variables
   * converted to "id" regardless of their bounds.
   */
  public String getObjCType(ITypeBinding type) {
    return getObjCTypeInner(type, null, false);
  }

  private String getObjCTypeInner(ITypeBinding type, String qualifiers, boolean expandBounds) {
    String objCType;
    if (type instanceof PointerTypeBinding) {
      String pointeeQualifiers = null;
      if (qualifiers != null) {
        int idx = qualifiers.indexOf('*');
        if (idx != -1) {
          pointeeQualifiers = qualifiers.substring(0, idx);
          qualifiers = qualifiers.substring(idx + 1);
        }
      }
      objCType = getObjCTypeInner(
          ((PointerTypeBinding) type).getPointeeType(), pointeeQualifiers, expandBounds);
      objCType = objCType.endsWith("*") ? objCType + "*" : objCType + " *";
    } else if (type.isTypeVariable() || type.isCapture() || type.isWildcardType()) {
      if (expandBounds) {
        List<ITypeBinding> bounds = Lists.newArrayList();
        collectBounds(type, bounds);
        objCType = constructObjCType(bounds);
      } else {
        objCType = ID_TYPE;
      }
    } else if (type.isPrimitive()) {
      objCType = getPrimitiveObjCType(type);
    } else {
      objCType = constructObjCType(Collections.singletonList(type));
    }
    if (qualifiers != null) {
      qualifiers = qualifiers.trim();
      if (!qualifiers.isEmpty()) {
        objCType += " " + qualifiers;
      }
    }
    return objCType;
  }

  private boolean collectBounds(ITypeBinding type, Collection<ITypeBinding> bounds) {
    ITypeBinding[] boundsArr = type.getTypeBounds();
    if (boundsArr.length == 0) {
      if (type.isWildcardType()) {
        bounds.addAll(Arrays.asList(type.getInterfaces()));
      }
      bounds.add(type.getErasure());
    } else {
      for (ITypeBinding bound : boundsArr) {
        collectBounds(bound, bounds);
      }
    }
    return true;
  }

  private String constructObjCType(Iterable<ITypeBinding> types) {
    String classType = null;
    List<String> interfaces = Lists.newArrayList();
    for (ITypeBinding type : types) {
      type = type.getErasure();
      if (isIdType(type) || typeEnv.isJavaVoidType(type)) {
        continue;
      }
      if (type.isInterface()) {
        interfaces.add(getFullName(type));
      } else {
        assert classType == null;  // Can only have one class type.
        classType = getFullName(type);
      }
    }
    String protocols = interfaces.isEmpty() ? "" : "<" + Joiner.on(", ").join(interfaces) + ">";
    return classType == null ? ID_TYPE + protocols : classType + protocols + " *";
  }

  /**
   * Return a comma-separated list of field names from a fragments list.
   *
   * @param fragments a list of VariableDeclarationFragment instances
   */
  public static String fieldNames(List<?> fragments) {
    if (fragments.isEmpty()) {
      return "";
    }
    StringBuffer sb = new StringBuffer();
    for (Iterator<?> iterator = fragments.iterator(); iterator.hasNext();) {
      Object o = iterator.next();
      if (o instanceof VariableDeclarationFragment) {
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;
        sb.append(fragment.getName().getIdentifier());
        if (iterator.hasNext()) {
          sb.append(", ");
        }
      } else {
        throw new AssertionError("unknown fragment type: " + o.getClass());
      }
    }
    return sb.toString();
  }

  /**
   * Return the full name of a type, including its package.  For outer types,
   * is the type's full name; for example, java.lang.Object's full name is
   * "JavaLangObject".  For inner classes, the full name is their outer class'
   * name plus the inner class name; for example, java.util.ArrayList.ListItr's
   * name is "JavaUtilArrayList_ListItr".
   */
  public String getFullName(ITypeBinding binding) {
    String name = getFullNameInner(binding);
    return binding.isEnum() ? (name + "Enum") : name;
  }

  private String getFullNameInner(ITypeBinding binding) {
    binding = typeEnv.mapType(binding.getErasure());  // Make sure type variables aren't included.

    // Use ObjectiveCType annotation, if it exists.
    IAnnotationBinding annotation = BindingUtil.getAnnotation(binding, ObjectiveCName.class);
    if (annotation != null) {
      return (String) BindingUtil.getAnnotationValue(annotation, "value");
    }

    ITypeBinding outerBinding = binding.getDeclaringClass();
    if (outerBinding != null) {
      return getFullNameInner(outerBinding) + '_' + getTypeSubName(binding);
    }
    String name = binding.getQualifiedName();

    // Use mapping file entry, if it exists.
    if (Options.getClassMappings().containsKey(name)) {
      return Options.getClassMappings().get(name);
    }

    // Use camel-cased package+class name.
    IPackageBinding pkg = binding.getPackage();
    String pkgName = pkg != null ? getPrefix(pkg) : "";
    return pkgName + binding.getName();
  }

  private static String getTypeSubName(ITypeBinding binding) {
    if (binding.isAnonymous()) {
      String binaryName = binding.getBinaryName();
      return binaryName.substring(binaryName.lastIndexOf("$"));
    } else if (binding.isLocal()) {
      String binaryName = binding.getBinaryName();
      int innerClassIndex = binaryName.lastIndexOf(binding.getName());
      while (innerClassIndex > 0 && binaryName.charAt(innerClassIndex - 1) != '$') {
        --innerClassIndex;
      }
      return binaryName.substring(innerClassIndex);
    }
    return binding.getName();
  }

  private static boolean isReservedName(String name) {
    return reservedNames.contains(name) || nsObjectMessages.contains(name);
  }

  public static String getMainTypeFullName(CompilationUnit unit) {
    PackageDeclaration pkg = unit.getPackage();
    if (pkg.isDefaultPackage()) {
      return unit.getMainTypeName();
    } else {
      return unit.getNameTable().getPrefix(pkg.getPackageBinding()) + unit.getMainTypeName();
    }
  }

  public String getPrefix(IPackageBinding packageBinding) {
    return prefixMap.getPrefix(packageBinding);
  }

  public boolean hasPrefix(String packageName) {
    return prefixMap.hasPrefix(packageName);
  }
}
