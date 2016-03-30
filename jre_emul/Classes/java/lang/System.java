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

package java.lang;

/*-[

#ifndef TARGET_OS_SIMULATOR
#define TARGET_OS_SIMULATOR 0
#endif
#if TARGET_OS_IPHONE || TARGET_OS_SIMULATOR
#import <UIKit/UIKit.h>
#endif

#import "IOSArray_PackagePrivate.h"
#import "IOSObjectArray.h"
#import "IOSPrimitiveArray.h"
#import "NSDictionaryMap.h"
#import "java/lang/ArrayIndexOutOfBoundsException.h"
#import "java/lang/ArrayStoreException.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/NullPointerException.h"
#import "java/util/Collections.h"
#include "mach/mach_time.h"
#include "TargetConditionals.h"

#if !TARGET_OS_IPHONE && !TARGET_OS_SIMULATOR
#include <crt_externs.h>
#endif
]-*/

import java.io.BufferedInputStream;
import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple iOS version of java.lang.System.  No code was shared, just its
 * public API.
 *
 * @author Tom Ball
 */
public class System {
  private static Properties props;

  public static final InputStream in;
  public static final PrintStream out;
  public static final PrintStream err;

  /*-[
    static mach_timebase_info_data_t machTimeInfo_;
  ]-*/

  static {
    // Set up standard in, out, and err.
    err = new PrintStream(new FileOutputStream(FileDescriptor.err));
    out = new PrintStream(new FileOutputStream(FileDescriptor.out));
    in = new BufferedInputStream(new FileInputStream(FileDescriptor.in));

    // Set up statics for time unit conversion.
    setTimeInfoConsts();
  }

  private static native void setTimeInfoConsts() /*-[
    // Get the timebase info
    mach_timebase_info(&machTimeInfo_);
  ]-*/;

  public static native void setIn(InputStream newIn) /*-[
#if __has_feature(objc_arc)
    JavaLangSystem_in = newIn;
#else
    JreStrongAssign(&JavaLangSystem_in, newIn);
#endif
  ]-*/;

  public static native void setOut(java.io.PrintStream newOut) /*-[
#if __has_feature(objc_arc)
    JavaLangSystem_out = newOut;
#else
    JreStrongAssign(&JavaLangSystem_out, newOut);
#endif
  ]-*/;

  public static native void setErr(java.io.PrintStream newErr)  /*-[
#if __has_feature(objc_arc)
    JavaLangSystem_err = newErr;
#else
    JreStrongAssign(&JavaLangSystem_err, newErr);
#endif
  ]-*/;

  public static native long currentTimeMillis() /*-[
    return (long long) ((CFAbsoluteTimeGetCurrent() + kCFAbsoluteTimeIntervalSince1970) * 1000);
  ]-*/;

  public static native int identityHashCode(Object anObject) /*-[
    return (int) (intptr_t) anObject;
  ]-*/;

  public static native void arraycopy(Object src, int srcPos, Object dest, int destPos,
      int length) /*-[
    if (!src || !dest) {
      @throw AUTORELEASE([[JavaLangNullPointerException alloc] init]);
    }
    Class srcCls = object_getClass(src);
    Class destCls = object_getClass(dest);
    if (class_getSuperclass(srcCls) != [IOSArray class]) {
      NSString *msg = [NSString stringWithFormat:@"source of type %@ is not an array",
                       [src class]];
      @throw AUTORELEASE([[JavaLangArrayStoreException alloc] initWithNSString:msg]);
    }
    if (destCls != srcCls) {
      NSString *msg =
         [NSString stringWithFormat:@"source type %@ cannot be copied to array of type %@",
          [src class], [dest class]];
      @throw AUTORELEASE([[JavaLangArrayStoreException alloc] initWithNSString:msg]);
    }

    // Range tests are done by array class.
    [(IOSArray *) src arraycopy:srcPos
                    destination:(IOSArray *) dest
                      dstOffset:destPos
                         length:length];
  ]-*/;

  public native static long nanoTime() /*-[
    uint64_t time = mach_absolute_time();

    // Convert to nanoseconds and return,
    return (time * machTimeInfo_.numer) / machTimeInfo_.denom;
  ]-*/;

  public native static void exit(int status) /*-[
    exit(status);
  ]-*/;

  public native static Properties getProperties() /*-[
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
      JreStrongAssignAndConsume(&JavaLangSystem_props, [[JavaUtilProperties alloc] init]);

      [JavaLangSystem_props setPropertyWithNSString:@"java.class.path" withNSString:@""];
      [JavaLangSystem_props setPropertyWithNSString:@"java.class.version" withNSString:@"0"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.compiler" withNSString:@""];
      [JavaLangSystem_props setPropertyWithNSString:@"java.ext.dirs" withNSString:@""];
      [JavaLangSystem_props setPropertyWithNSString:@"java.library.path" withNSString:@""];
      [JavaLangSystem_props setPropertyWithNSString:@"java.specification.name"
                                       withNSString:@"J2ObjC"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.specification.vendor"
                                       withNSString:@"J2ObjC"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.specification.version"
                                       withNSString:@"0"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.vendor" withNSString:@"J2ObjC"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.vendor.url"
                                       withNSString:@"http://j2objc.org/"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.version" withNSString:@"0"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.vm.name" withNSString:@""];
      [JavaLangSystem_props setPropertyWithNSString:@"java.vm.specification.name"
                                       withNSString:@"J2ObjC"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.vm.specification.vendor"
                                       withNSString:@"J2ObjC"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.vm.specification.version"
                                       withNSString:@"0"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.vm.vendor" withNSString:@"J2ObjC"];
      [JavaLangSystem_props setPropertyWithNSString:@"java.vm.version" withNSString:@"0"];

      // Get os.arch from J2OBJC_BUILD_ARCH defined in fat_lib.mk.
      #define J2OBJC_BUILD_ARCH_STRINGIFY(x) #x
      #define J2OBJC_BUILD_ARCH_CSTR(x) J2OBJC_BUILD_ARCH_STRINGIFY(x)
      #define J2OBJC_BUILD_ARCH_NSSTR ([NSString stringWithUTF8String: \
                                        J2OBJC_BUILD_ARCH_CSTR(J2OBJC_BUILD_ARCH)])
      [JavaLangSystem_props setPropertyWithNSString:@"os.arch"
                                       withNSString:J2OBJC_BUILD_ARCH_NSSTR];
      #undef J2OBJC_BUILD_ARCH_NSSTR
      #undef J2OBJC_BUILD_ARCH_CSTR
      #undef J2OBJC_BUILD_ARCH_STRINGIFY

      NSString *versionString;
#if !TARGET_OS_IPHONE && !TARGET_OS_SIMULATOR
      BOOL onSimulator = false;
#endif
      // During compile time, see if [NSProcessInfo processInfo].operatingSystemVersion is available
      // in the SDK.
#if (defined(__MAC_OS_X_VERSION_MAX_ALLOWED) && (__MAC_OS_X_VERSION_MAX_ALLOWED > __MAC_10_9)) \
    || (defined(__IPHONE_OS_VERSION_MAX_ALLOWED) \
        && (__IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_8_0))

      // Then we check if the method is actually available on the running device.
      if ([NSProcessInfo instancesRespondToSelector:@selector(operatingSystemVersion)]) {
        NSOperatingSystemVersion version = [NSProcessInfo processInfo].operatingSystemVersion;

        // This matches the format of [UIDevice currentDevice].systemVersion.
        if (version.patchVersion) {
          versionString = [NSString stringWithFormat:@"%ld.%ld.%ld",
                                                     (long) version.majorVersion,
                                                     (long) version.minorVersion,
                                                     (long) version.patchVersion];
        } else {
          versionString = [NSString stringWithFormat:@"%ld.%ld",
                                                     (long) version.majorVersion,
                                                     (long) version.minorVersion];
        }
      } else {
#else
      {
#endif  // #if (defined(...))

#if (TARGET_OS_IPHONE || TARGET_OS_SIMULATOR)
        // If [NSProcessInfo processInfo].operatingSystemVersion is not available in the SDK and
        // this is iOS SDK, use [UIDevice currentDevice].
    #if TARGET_OS_WATCH
        versionString = [NSProcessInfo processInfo].operatingSystemVersionString;
    #else
        versionString = [UIDevice currentDevice].systemVersion;
    #endif // #if TARGET_OS_WATCH
#else
        // If we arrive here, we want to try again to see if [UIDevice currentDevice] is
        // available. This is because the code may be running in a 64-bit iOS Simulator, but
        // the x86_64 portion of the fat library is built as a Mac library.
        Class uiDeviceClass = NSClassFromString(@"UIDevice");
        SEL currentDeviceSel = NSSelectorFromString(@"currentDevice");
        SEL systemVersionSel = NSSelectorFromString(@"systemVersion");
        id currentDevice = [uiDeviceClass performSelector:currentDeviceSel];
        versionString = (NSString *)[currentDevice performSelector:systemVersionSel];
        if (versionString) {
          onSimulator = true;
        } else {
          // Ok, this is OS X. We use operatingSystemVersionString which gives us a localized
          // version not suitable for parsing. Given the use case of this property, it's not worth
          // doing more than just reporting this back verbatim.
          versionString = [NSProcessInfo processInfo].operatingSystemVersionString;
        }
#endif  // #if TARGET_OS_IPHONE || TARGET_OS_SIMULATOR
      }

      [JavaLangSystem_props setPropertyWithNSString:@"os.version" withNSString:versionString];

      [JavaLangSystem_props setPropertyWithNSString:@"file.separator" withNSString:@"/"];
      [JavaLangSystem_props setPropertyWithNSString:@"line.separator" withNSString:@"\n"];
      [JavaLangSystem_props setPropertyWithNSString:@"path.separator" withNSString:@":"];
      [JavaLangSystem_props setPropertyWithNSString:@"org.xml.sax.driver"
                                       withNSString:@"org.xmlpull.v1.sax2.Driver"];

      NSString *homeDirectory = NSHomeDirectory();
      [JavaLangSystem_props setPropertyWithNSString:@"user.home" withNSString:homeDirectory];

      NSString *userName = NSUserName();
#if TARGET_OS_SIMULATOR
      // Some simulators don't initialize the user name, so try hacking it from the app's path.
      if (!userName || userName.length == 0) {
        NSArray *bundlePathComponents = [NSBundle.mainBundle.bundlePath pathComponents];
        if (bundlePathComponents.count >= 3
            && [bundlePathComponents[0] isEqualToString:@"/"]
            && [bundlePathComponents[1] isEqualToString:@"Users"]) {
          userName = bundlePathComponents[2];
        }
      }
#endif
      [JavaLangSystem_props setPropertyWithNSString:@"user.name" withNSString:userName];

#if TARGET_OS_SIMULATOR
      [JavaLangSystem_props setPropertyWithNSString:@"os.name" withNSString:@"iPhone Simulator"];
      [JavaLangSystem_props
          setPropertyWithNSString:@"user.dir"
                     withNSString:[homeDirectory stringByAppendingString:@"/Documents"]];
#elif TARGET_OS_IPHONE
      [JavaLangSystem_props setPropertyWithNSString:@"os.name" withNSString:@"iPhone"];
      [JavaLangSystem_props
          setPropertyWithNSString:@"user.dir"
                     withNSString:[homeDirectory stringByAppendingString:@"/Documents"]];
#else
      if (onSimulator) {
        [JavaLangSystem_props setPropertyWithNSString:@"os.name" withNSString:@"iPhone Simulator"];
        [JavaLangSystem_props
            setPropertyWithNSString:@"user.dir"
                       withNSString:[homeDirectory stringByAppendingString:@"/Documents"]];
      } else {
        [JavaLangSystem_props setPropertyWithNSString:@"os.name" withNSString:@"Mac OS X"];
        NSString *curDir = [[NSFileManager defaultManager] currentDirectoryPath];
        if ([curDir isEqualToString:@"/"]) {
          // Workaround for simulator bug.
          curDir = [homeDirectory stringByAppendingString:@"/Documents"];
        }
        [JavaLangSystem_props setPropertyWithNSString:@"user.dir" withNSString:curDir];
      }
#endif

      NSString *tmpDir = NSTemporaryDirectory();
      int iLast = (int) [tmpDir length] - 1;
      if (iLast >= 0 && [tmpDir characterAtIndex:iLast] == '/') {
        tmpDir = [tmpDir substringToIndex:iLast];
      }
      [JavaLangSystem_props setPropertyWithNSString:@"java.io.tmpdir" withNSString:tmpDir];
      [JavaLangSystem_props setPropertyWithNSString:@"java.home"
                                        withNSString:[[NSBundle mainBundle] bundlePath]];

      char *fileEncoding = getenv("file_encoding");  // Shell variables cannot have periods.
      if (!fileEncoding) {
        fileEncoding = getenv("file.encoding");
      }
      if (fileEncoding) {
        NSString *enc = [NSString stringWithCString:fileEncoding
                                           encoding:[NSString defaultCStringEncoding]];
        [JavaLangSystem_props setPropertyWithNSString:@"file.encoding" withNSString:enc];
      }

      // These properties are used to define the default Locale.
      NSString *localeId = [[NSLocale currentLocale] localeIdentifier];
      NSDictionary *components = [NSLocale componentsFromLocaleIdentifier:localeId];
      NSString *language = [components objectForKey:NSLocaleLanguageCode];
      if (language) {
        [JavaLangSystem_props setPropertyWithNSString:@"user.language" withNSString:language];
      }
      NSString *country = [components objectForKey:NSLocaleCountryCode];
      if (country) {
        [JavaLangSystem_props setPropertyWithNSString:@"user.region" withNSString:country];
      }
      NSString *variant = [components objectForKey:NSLocaleVariantCode];
      if (variant) {
        [JavaLangSystem_props setPropertyWithNSString:@"user.variant" withNSString:variant];
      }
    });
    return JavaLangSystem_props;
  ]-*/;

  public static String getProperty(String key) {
    return getProperties().getProperty(key);
  }

  public static String getProperty(String key, String defaultValue) {
    String result = getProperties().getProperty(key);
    return result != null ? result : defaultValue;
  }

  public static String setProperty(String key, String value) {
    return (String) getProperties().setProperty(key, value);
  }

  public static void setProperties(Properties properties) {
    props = properties;
  }

  public static String clearProperty(String key) {
    Properties properties = getProperties();
    String result = properties.getProperty(key);
    properties.remove(key);
    return result;
  }

  public static native String getenv(String name) /*-[
    const char *value = getenv([name UTF8String]);
    return value ? [NSString stringWithUTF8String:value] : nil;
  ]-*/;

  public static native Map<String,String> getenv() /*-[
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
#if !TARGET_OS_IPHONE && !TARGET_OS_SIMULATOR
    // TODO(tball): move to libcore.io.Posix.
    char ***nsEnviron = _NSGetEnviron();
    if (nsEnviron && *nsEnviron) {
      char **environ = *nsEnviron;
      for (int i = 0; environ[i]; i++) {
        NSString *var = [NSString stringWithUTF8String:environ[i]];
        NSRange range = [var rangeOfString:@"="];
        if (range.location != NSNotFound) {
          NSString *key = [var substringToIndex:range.location];
          NSString *value = [var substringFromIndex:(range.location + 1)];
          [dict setObject:value forKey:key];
        }
      }
    }
#endif
    return [JavaUtilCollections unmodifiableMapWithJavaUtilMap:
            [NSDictionaryMap mapWithDictionary:dict]];
  ]-*/;

  /**
   * Returns null. Android does not use {@code SecurityManager}. This method
   * is only provided for source compatibility.
   *
   * @return null
   */
  public static SecurityManager getSecurityManager() {
      return null;
  }

  /**
   * Returns the system's line separator.
   * @since 1.7
   */
  public static String lineSeparator() {
      return "\n";   // Always return OSX/iOS value.
  }

  /**
   * See {@link Runtime#load}.
   */
  public static void load(String pathName) {
      Runtime.getRuntime().load(pathName);
  }

  /**
   * See {@link Runtime#loadLibrary}.
   */
  public static void loadLibrary(String libName) {
      Runtime.getRuntime().loadLibrary(libName);
  }

  public static void gc() {
      Runtime.getRuntime().gc();
  }

  /**
   * No-op on iOS, since it doesn't use garbage collection.
   */
  public static void runFinalization() {}

  /**
   * No-op on iOS, since it doesn't use garbage collection.
   */
  public static void runFinalizersOnExit(boolean b) {}

  /**
   * Returns the {@link java.io.Console} associated with this VM, or null.
   * Not all VMs will have an associated console. A console is typically only
   * available for programs run from the command line.
   * @since 1.6
   */
  public static Console console() {
      return Console.getConsole();
  }

  // Android internal logging methods, rewritten to use Logger.

  /**
   * @hide internal use only
   */
  public static void logE(String message) {
      log(Level.SEVERE, message, null);
  }

  /**
   * @hide internal use only
   */
  public static void logE(String message, Throwable th) {
      log(Level.SEVERE, message, th);
  }

  /**
   * @hide internal use only
   */
  public static void logI(String message) {
      log(Level.INFO, message, null);
  }

  /**
   * @hide internal use only
   */
  public static void logI(String message, Throwable th) {
      log(Level.INFO, message, th);
  }

  /**
   * @hide internal use only
   */
  public static void logW(String message) {
      log(Level.WARNING, message, null);
  }

  /**
   * @hide internal use only
   */
  public static void logW(String message, Throwable th) {
      log(Level.WARNING, message, th);
  }

  private static Logger systemLogger;

  private static void log(Level level, String message, Throwable thrown) {
    if (systemLogger == null) {
      systemLogger = Logger.getLogger("java.lang.System");
    }
    systemLogger.log(level, message, thrown);
  }
}
