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

extern char **environ;
]-*/

import java.io.BufferedInputStream;
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
    JavaLangSystem_in_ = newIn;
#else
    JreStrongAssign(&JavaLangSystem_in_, nil, newIn);
#endif
  ]-*/;

  public static native void setOut(java.io.PrintStream newOut) /*-[
#if __has_feature(objc_arc)
    JavaLangSystem_out_ = newOut;
#else
    JreStrongAssign(&JavaLangSystem_out_, nil, newOut);
#endif
  ]-*/;

  public static native void setErr(java.io.PrintStream newErr)  /*-[
#if __has_feature(objc_arc)
    JavaLangSystem_err_ = newErr;
#else
    JreStrongAssign(&JavaLangSystem_err_, nil, newErr);
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
      JreStrongAssignAndConsume(&JavaLangSystem_props_, nil, [[JavaUtilProperties alloc] init]);

      [JavaLangSystem_props_ setPropertyWithNSString:@"file.separator" withNSString:@"/"];
      [JavaLangSystem_props_ setPropertyWithNSString:@"line.separator" withNSString:@"\n"];
      [JavaLangSystem_props_ setPropertyWithNSString:@"path.separator" withNSString:@":"];
      [JavaLangSystem_props_ setPropertyWithNSString:@"org.xml.sax.driver"
                                        withNSString:@"org.xmlpull.v1.sax2.Driver"];

      NSString *homeDirectory = NSHomeDirectory();
      [JavaLangSystem_props_ setPropertyWithNSString:@"user.home" withNSString:homeDirectory];
      [JavaLangSystem_props_ setPropertyWithNSString:@"user.name" withNSString:NSUserName()];

#if TARGET_OS_IPHONE
      [JavaLangSystem_props_ setPropertyWithNSString:@"os.name" withNSString:@"iPhone"];
      [JavaLangSystem_props_
          setPropertyWithNSString:@"user.dir"
                     withNSString:[homeDirectory stringByAppendingString:@"/Documents"]];
#elif TARGET_IPHONE_SIMULATOR
      [JavaLangSystem_props_ setPropertyWithNSString:@"os.name" withNSString:@"iPhone Simulator"];
      [JavaLangSystem_props_
          setPropertyWithNSString:@"user.dir"
                     withNSString:[homeDirectory stringByAppendingString:@"/Documents"]];
#else
      [JavaLangSystem_props_ setPropertyWithNSString:@"os.name" withNSString:@"Mac OS X"];
      NSString *curDir = [[NSFileManager defaultManager] currentDirectoryPath];
      [JavaLangSystem_props_ setPropertyWithNSString:@"user.dir" withNSString:curDir];
#endif

      NSString *tmpDir = NSTemporaryDirectory();
      int iLast = (int) [tmpDir length] - 1;
      if (iLast >= 0 && [tmpDir characterAtIndex:iLast] == '/') {
        tmpDir = [tmpDir substringToIndex:iLast];
      }
      [JavaLangSystem_props_ setPropertyWithNSString:@"java.io.tmpdir" withNSString:tmpDir];
      [JavaLangSystem_props_ setPropertyWithNSString:@"java.home"
                                        withNSString:[[NSBundle mainBundle] bundlePath]];

      char *fileEncoding = getenv("file_encoding");  // Shell variables cannot have periods.
      if (!fileEncoding) {
        fileEncoding = getenv("file.encoding");
      }
      if (fileEncoding) {
        NSString *enc = [NSString stringWithCString:fileEncoding
                                           encoding:[NSString defaultCStringEncoding]];
        [JavaLangSystem_props_ setPropertyWithNSString:@"file.encoding" withNSString:enc];
      }

      // These properties are used to define the default Locale.
      NSString *localeId = [[NSLocale currentLocale] localeIdentifier];
      NSDictionary *components = [NSLocale componentsFromLocaleIdentifier:localeId];
      NSString *language = [components objectForKey:NSLocaleLanguageCode];
      if (language) {
        [JavaLangSystem_props_ setPropertyWithNSString:@"user.language" withNSString:language];
      }
      NSString *country = [components objectForKey:NSLocaleCountryCode];
      if (country) {
        [JavaLangSystem_props_ setPropertyWithNSString:@"user.region" withNSString:country];
      }
      NSString *variant = [components objectForKey:NSLocaleVariantCode];
      if (variant) {
        [JavaLangSystem_props_ setPropertyWithNSString:@"user.variant" withNSString:variant];
      }
    });
    return JavaLangSystem_props_;
  ]-*/;

  public static String getProperty(String key) {
    return getProperties().getProperty(key);
  }

  public static String getProperty(String key, String defaultValue) {
    String result = getProperties().getProperty(key);
    return result != null ? result : defaultValue;
  }

  public static void setProperty(String key, String value) {
    getProperties().setProperty(key, value);
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
    for (int i = 0; environ[i]; i++) {
      NSString *var = [NSString stringWithUTF8String:environ[i]];
      NSRange range = [var rangeOfString:@"="];
      if (range.location != NSNotFound) {
        NSString *key = [var substringToIndex:range.location];
        NSString *value = [var substringFromIndex:(range.location + 1)];
        [dict setObject:value forKey:key];
      }
    }
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
