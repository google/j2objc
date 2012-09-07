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

/*-{
#import "IOSBooleanArray.h"
#import "IOSByteArray.h"
#import "IOSCharArray.h"
#import "IOSDoubleArray.h"
#import "IOSFloatArray.h"
#import "IOSIntArray.h"
#import "IOSLongArray.h"
#import "IOSObjectArray.h"
#import "IOSShortArray.h"
#import "java/lang/ArrayIndexOutOfBoundsException.h"
#import "java/lang/ArrayStoreException.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/NullPointerException.h"
#include "mach/mach_time.h"
}-*/

import java.io.PrintStream;
import java.util.Properties;

/**
 * Simple iOS version of java.lang.System.  No code was shared, just its
 * public API.
 * 
 * @author Tom Ball
 */
public class System {
  private static Properties props;

  // Currently, calls to these print streams are replaced with NSLog messages,
  // so are only declared to resolve references during translation.
  public static final PrintStream out = null;
  public static final PrintStream err = null;

  public static native long currentTimeMillis() /*-{
    return (long long) ([[NSDate date] timeIntervalSince1970] * 1000);
  }-*/;

  public static native int identityHashCode(Object anObject) /*-{
    return (int) (intptr_t) anObject;
  }-*/;
  
  public static native void arraycopy(Object src, int srcPos, Object dest, int destPos,
      int length) /*-{
    id exception = nil;
    if (!src || !dest) {
      exception = [[JavaLangNullPointerException alloc] init];
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
    }
    if (srcPos < 0 || destPos < 0) {
      exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
    }
    if (![src isMemberOfClass:[IOSArray class]] && ![dest isMemberOfClass:[src class]]) {
      exception = [[JavaLangArrayStoreException alloc] init];
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
    }
    if (exception) {
      @throw exception;
    }
    [(IOSArray *) src arraycopy:NSMakeRange(srcPos, length)
                    destination:(IOSArray *) dest
                         offset:destPos];
  }-*/;

  public native static long nanoTime() /*-{
    // Get the timebase info
    mach_timebase_info_data_t info;
    mach_timebase_info(&info);

    uint64_t time = mach_absolute_time();

    // Convert to nanoseconds and return,
    return (time * info.numer) / info.denom;
  }-*/;

  public native static void exit(int status) /*-{
    exit(status);
  }-*/;

  public static Properties getProperties() {
    if (props == null) {
      props = new Properties();
      props.setProperty("os.name", "Mac OS X");
      props.setProperty("file.separator", "/");
      props.setProperty("line.separator", "\n");
      props.setProperty("path.separator", ":");
      setSystemProperties(props);
    }
    return props;
  }

  private static native void setSystemProperties(Properties props) /*-{
    [props setPropertyWithNSString:@"user.home" withNSString:NSHomeDirectory()];
    [props setPropertyWithNSString:@"user.name" withNSString:NSUserName()];
    NSString *curDir = [[NSFileManager defaultManager] currentDirectoryPath];
    [props setPropertyWithNSString:@"user.dir" withNSString:curDir];

    NSString *tmpDir = NSTemporaryDirectory();
    int iLast = [tmpDir length] - 1;
    if (iLast >= 0 && [tmpDir characterAtIndex:iLast] == '/') {
      tmpDir = [tmpDir substringToIndex:iLast];
    }
    [props setPropertyWithNSString:@"java.io.tmpdir" withNSString:tmpDir];
  }-*/;

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
}
