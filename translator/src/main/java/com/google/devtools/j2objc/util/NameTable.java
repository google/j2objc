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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSParameter;
import com.google.devtools.j2objc.types.PointerTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.j2objc.annotations.ObjectiveCName;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Singleton service for type/method/variable name support.
 *
 * @author Tom Ball
 */
public class NameTable {

  private static NameTable instance;
  private final Map<IBinding, String> renamings = Maps.newHashMap();

  public static final String INIT_NAME = "init";
  public static final String CLINIT_NAME = "initialize";

  public static final String ID_TYPE = "id";

  private static final Logger logger = Logger.getLogger(NameTable.class.getName());

  /**
   * The list of predefined types, common primitive typedefs, constants and
   * variables.
   */
  public static final List<String> reservedNames = Lists.newArrayList(
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
      "scale");

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
  private final Map<String, String> prefixMap;

  private NameTable(Map<String, String> prefixMap) {
    this.prefixMap = prefixMap;
  }

  /**
   * Initialize this service using the AST returned by the parser.
   */
  public static void initialize(CompilationUnit unit) {
    instance = new NameTable(Options.getPackagePrefixes());
  }

  public static void cleanup() {
    instance = null;
  }

  /**
   * Returns a bound name that may have been renamed by a translation phase.
   *
   * @return the new name, or the old name if no renaming exists
   */
  public static String getName(IBinding binding) {
    assert binding != null;
    binding = getBindingDeclaration(binding);
    String newName = instance.renamings.get(binding);
    if (newName != null) {
      return newName;
    }
    String name = binding.getName();
    if (binding instanceof IVariableBinding) {
      IVariableBinding var = (IVariableBinding) binding;
      if (isReservedName(name)) {
        name += "_";
      }
      if (var.isField()) {
        // Check if field has the same name as a method.
        ITypeBinding superclass = ((IVariableBinding) binding).getDeclaringClass();
        for (IMethodBinding method : superclass.getDeclaredMethods()) {
          if (method.getName().equals(name)) {
            name = name + '_';
            break;
          }
        }
      }
    }
    return name;
  }

  private static IBinding getBindingDeclaration(IBinding binding) {
    if (binding instanceof IVariableBinding) {
      return ((IVariableBinding) binding).getVariableDeclaration();
    }
    if (binding instanceof IMethodBinding) {
      return ((IMethodBinding) binding).getMethodDeclaration();
    }
    if (binding instanceof ITypeBinding) {
      return ((ITypeBinding) binding).getTypeDeclaration();
    }
    return binding;
  }

  /**
   * Returns a name for a SimpleName that may have been renamed by a
   * translation phase.
   *
   * @return the new name, or the old name if no renaming exists
   */
  public static String getName(SimpleName node) {
    return getName(Types.getBinding(node));
  }

  public static boolean isRenamed(IBinding binding) {
    return instance.renamings.containsKey(binding);
  }

  public static boolean isRenamed(SimpleName node) {
    return isRenamed(Types.getBinding(node));
  }

  /**
   * Adds a name to the renamings map, used by getName().
   */
  public static void rename(IBinding oldName, String newName) {
    oldName = getBindingDeclaration(oldName);
    String previousName = instance.renamings.get(oldName);
    if (previousName != null && !previousName.equals(newName)) {
      logger.fine(String.format("Changing previous rename: %s => %s, now: %s => %s",
          oldName.toString(), previousName, oldName, newName));
    }
    instance.renamings.put(oldName, newName);
  }

  /**
   * Adds a SimpleName to the renamings map.
   */
  public static void rename(SimpleName node, String newName) {
    rename(Types.getBinding(node), newName);
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
   * Return the Objective-C equivalent name for a Java primitive type.
   */
  public static String primitiveTypeToObjC(PrimitiveType type) {
    PrimitiveType.Code code = type.getPrimitiveTypeCode();
    return primitiveTypeToObjC(code.toString());
  }

  private static final ImmutableMap<String, String> PRIMITIVE_TYPE_MAP =
      ImmutableMap.<String, String>builder()
      .put("boolean", "BOOL")
      .put("byte", "char")
      .put("char", "unichar")
      .put("short", "short int")
      .put("long", "long long int")
      .build();

  public static String primitiveTypeToObjC(String javaName) {
    String result = PRIMITIVE_TYPE_MAP.get(javaName);
    return result != null ? result : javaName;
  }

  // TODO(kstanger): See whether the logic in this method can be simplified.
  //     Also, what about type variables?
  private static String getArrayTypeParameterKeyword(ITypeBinding elementType, int dimensions) {
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

  private static boolean isIdType(ITypeBinding type) {
    return type == Types.resolveIOSType("id") || type == Types.resolveIOSType("NSObject")
        || Types.isJavaObjectType(type);
  }

  private static String getParameterTypeKeyword(ITypeBinding type) {
    if (isIdType(type) || type.isTypeVariable()) {
      return ID_TYPE;
    } else if (type.isPrimitive()) {
      return type.getName();
    } else if (type.isArray()) {
      return getArrayTypeParameterKeyword(type.getElementType(), type.getDimensions());
    }
    return getFullName(type);
  }

  public static String parameterKeyword(ITypeBinding type) {
    return "with" + capitalize(getParameterTypeKeyword(type));
  }

  public static String getMethodSelector(IMethodBinding method) {
    StringBuilder sb = new StringBuilder();
    if (method.isConstructor()) {
      sb.append("init");
    } else {
      sb.append(getName(method));
    }
    IOSMethod iosMethod = IOSMethodBinding.getIOSMethod(method);
    if (iosMethod != null) {
      List<IOSParameter> params = iosMethod.getParameters();
      for (int i = 0; i < params.size(); i++) {
        if (params.get(i).isVarArgs()) {
          break;
        }
        if (i != 0) {
          sb.append(params.get(i).getParameterName());
        }
        sb.append(":");
      }
    } else {
      ITypeBinding[] paramTypes = method.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++) {
        String keyword = NameTable.parameterKeyword(paramTypes[i]);
        if (i == 0) {
          keyword = NameTable.capitalize(keyword);
        }
        sb.append(keyword).append(":");
      }
    }
    return sb.toString();
  }

  /**
   * Convert a Java type to an equivalent Objective-C type with type variables
   * resolved to their bounds.
   */
  public static String getSpecificObjCType(ITypeBinding type) {
    return getObjCTypeInner(type, null, true);
  }

  public static String getSpecificObjCType(IVariableBinding var) {
    String qualifiers = null;
    if (var instanceof GeneratedVariableBinding) {
      qualifiers = ((GeneratedVariableBinding) var).getTypeQualifiers();
    }
    return getObjCTypeInner(var.getType(), qualifiers, true);
  }

  /**
   * Convert a Java type to an equivalent Objective-C type with type variables
   * converted to "id" regardless of their bounds.
   */
  public static String getObjCType(ITypeBinding type) {
    return getObjCTypeInner(type, null, false);
  }

  private static String getObjCTypeInner(
      ITypeBinding type, String qualifiers, boolean expandTypeVariables) {
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
          ((PointerTypeBinding) type).getPointeeType(), pointeeQualifiers, expandTypeVariables);
      objCType = objCType.endsWith("*") ? objCType + "*" : objCType + " *";
    } else if (type.isTypeVariable()) {
      if (expandTypeVariables) {
        ITypeBinding[] bounds = type.getTypeBounds();
        while (bounds.length > 0 && bounds[0].isTypeVariable()) {
          type = bounds[0];
          bounds = type.getTypeBounds();
        }
        objCType = constructObjCType(bounds);
      } else {
        objCType = ID_TYPE;
      }
    } else if (type.isPrimitive()) {
      objCType = primitiveTypeToObjC(type.getName());
    } else {
      objCType = constructObjCType(type.getErasure());
    }
    if (qualifiers != null) {
      qualifiers = qualifiers.trim();
      if (!qualifiers.isEmpty()) {
        objCType += " " + qualifiers;
      }
    }
    return objCType;
  }

  private static String constructObjCType(ITypeBinding... types) {
    String classType = null;
    List<String> interfaces = Lists.newArrayListWithCapacity(types.length);
    for (ITypeBinding type : types) {
      if (type == null || isIdType(type) || Types.isJavaVoidType(type)) {
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
  public static String getFullName(AbstractTypeDeclaration typeDecl) {
    return getFullName(Types.getTypeBinding(typeDecl));
  }

  public static String getFullName(ITypeBinding binding) {
    binding = Types.mapType(binding.getErasure());  // Make sure type variables aren't included.
    String suffix = binding.isEnum() ? "Enum" : "";
    String prefix = "";
    IMethodBinding outerMethod = binding.getDeclaringMethod();
    if (outerMethod != null && !binding.isAnonymous()) {
      prefix += "_" + outerMethod.getName();
    }
    ITypeBinding outerBinding = binding.getDeclaringClass();
    if (outerBinding != null) {
      String baseName = getFullName(outerBinding) + prefix + '_' + getName(binding);
      return (outerBinding.isEnum() && binding.isAnonymous()) ? baseName : baseName + suffix;
    }
    String name = binding.getQualifiedName();

    // Use ObjectiveCType annotation, if it exists.
    IAnnotationBinding annotation = BindingUtil.getAnnotation(binding, ObjectiveCName.class);
    if (annotation != null) {
      name = (String) BindingUtil.getAnnotationValue(annotation, "value");
      return name + suffix;
    }

    // Use mapping file entry, if it exists.
    if (Options.getClassMappings().containsKey(name)) {
      name = Options.getClassMappings().get(name);
      return name + suffix;
    }

    // Use camel-cased package+class name.
    IPackageBinding pkg = binding.getPackage();
    String pkgName = pkg != null ? getPrefix(pkg.getName()) : "";
    return pkgName + binding.getName() + suffix;
  }

  /**
   * Returns a "Type_method" function name for static methods, such as from
   * enum types.
   */
  public static String makeFunctionName(AbstractTypeDeclaration cls, MethodDeclaration method) {
    return makeFunctionName(Types.getTypeBinding(cls), Types.getMethodBinding(method));
  }

  public static String makeFunctionName(ITypeBinding classBinding, IMethodBinding methodBinding) {
    String className = getFullName(classBinding);
    String methodName = methodBinding.getName();

    // If method name is overloaded, get its declaration index. For example if a class has
    // private foo(int) and foo(long) methods, the function names would be foo1(int) and foo2(long).
    int index = 0;
    for (IMethodBinding m : classBinding.getDeclaredMethods()) {
      if (m.getName().equals(methodName)) {
        index++;
        if (m.isEqualTo(methodBinding)) {
          break;
        }
      }
    }

    return String.format("%s_%s_%s", className, methodName,
        index > 1 ? Integer.toString(index) : "");
  }

  public static boolean isReservedName(String name) {
    return reservedNames.contains(name) || nsObjectMessages.contains(name);
  }

  /**
   * Returns the fully-qualified main class for a given compilation unit.
   */
  public static String getMainJavaName(CompilationUnit node, String sourceFileName) {
    String className = getClassNameFromSourceFileName(sourceFileName);
    PackageDeclaration pkgDecl = node.getPackage();
    if (pkgDecl != null) {
      className = pkgDecl.getName().getFullyQualifiedName()  + '.' + className;
    }
    return className;
  }

  private static String getClassNameFromSourceFileName(String sourceFileName) {
    int begin = sourceFileName.lastIndexOf(File.separatorChar) + 1;
    int end = sourceFileName.lastIndexOf(".java");
    String className = sourceFileName.substring(begin, end);
    return className;
  }

  public static String getMainTypeName(CompilationUnit node, String sourceFileName) {
    String className = getClassNameFromSourceFileName(sourceFileName);
    PackageDeclaration pkgDecl = node.getPackage();
    if (pkgDecl != null) {
      String pkgName = getPrefix(pkgDecl.getName().getFullyQualifiedName());
      return pkgName + className;
    } else {
      return className;
    }
  }

  public static String getStaticAccessorName(String varName) {
    // follow the Obj-C style guide for reader names, unless it's an illegal name
    return isReservedName(varName) ? "get" + capitalize(varName) : varName;
  }

  public static String getStaticVarQualifiedName(IVariableBinding var) {
    ITypeBinding declaringType = var.getDeclaringClass().getTypeDeclaration();
    return getFullName(declaringType) + "_" + getName(var) + (var.isEnumConstant() ? "" : "_");
  }

  public static String getStaticVarName(IVariableBinding var) {
    return getName(var) + (var.isEnumConstant() ? "" : "_");
  }

  public static String getPrimitiveConstantName(IVariableBinding constant) {
    return String.format("%s_%s", getFullName(constant.getDeclaringClass()), constant.getName());
  }

  public static String javaFieldToObjC(String fieldName) {
    return fieldName + "_";
  }

  public static void mapPackageToPrefix(String packageName, String prefix) {
    instance.prefixMap.put(packageName, prefix);
  }

  /**
   * Return the prefix for a specified package.  If a prefix was specified
   * for the package on the command-line, then that prefix is returned.
   * Otherwise, a camel-cased prefix is created from the package name.
   */
  public static String getPrefix(String packageName) {
    if (hasPrefix(packageName)) {
      return instance.prefixMap.get(packageName);
    }
    StringBuilder sb = new StringBuilder();
    for (String part : packageName.split("\\.")) {
      sb.append(capitalize(part));
    }
    return sb.toString();
  }

  public static boolean hasPrefix(String packageName) {
    return instance.prefixMap.containsKey(packageName);
  }
}
