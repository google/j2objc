/*
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

package com.google.j2objc.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/*-[
#if TARGET_OS_MAC && __MAC_OS_X_VERSION_MAX_ALLOWED >= 101200
#define OS_LOG_AVAILABLE 1
#elif TARGET_OS_IOS && __IPHONE_OS_VERSION_MAX_ALLOWED >= 100000
#define OS_LOG_AVAILABLE 1
#elif TARGET_OS_TV && __TV_OS_VERSION_MAX_ALLOWED >= 100000
#define OS_LOG_AVAILABLE 1
#elif TARGET_OS_WATCH && __WATCH_OS_X_VERSION_MAX_ALLOWED >= 30000
#define OS_LOG_AVAILABLE 1
#else
#define OS_LOG_AVAILABLE 0
#endif

#if OS_LOG_AVAILABLE
#import <os/log.h>
#endif

@interface NativeLog : NSObject {
#if OS_LOG_AVAILABLE
 @public
  os_log_t _log;
#endif
}
- (instancetype)initWithSubsystem:(NSString *)subsystem category:(NSString *)category;
@end

@implementation NativeLog
- (instancetype)initWithSubsystem:(NSString *)subsystem category:(NSString *)category {
  self = [super init];
  if (self) {
#if OS_LOG_AVAILABLE
    _log = os_log_create(subsystem.UTF8String, category.UTF8String);
#endif
  }
  return self;
}
@end
]-*/

/*-[
// TODO(tball): update ASL use to iOS 10's os_log and remove clang pragmas.
#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"

#import <asl.h>

// Simple value holder, so aslclient is closed when thread dictionary is deallocated.
@interface ASLClientHolder : NSObject {
 @public
  aslclient _client;
}
@end

@implementation ASLClientHolder
- (instancetype)initWithClient:(aslclient)client {
  self = [super init];
  if (self) {
    _client = client;
  }
  return self;
}

- (void)dealloc {
  asl_close(_client);
#if !__has_feature(objc_arc)
  [super dealloc];
#endif
}
@end
#pragma clang diagnostic pop
]-*/

/**
 * Handler implementation that calls iOS asl_log(), or os_log() if supported by the OS.
 *
 * @author Tom Ball
 */
public class IOSLogHandler extends Handler {

  static class IOSLogFormatter extends Formatter {

    /**
     * Very simple formatter, since asl_log adds its own text.
     */
    @Override
    public String format(LogRecord record) {
      return formatMessage(record);
    }
  }

  // TODO(tball): Replace this with a system property that defines a java.util.logging.config.class.
  public static final String IOS_LOG_MANAGER_DEFAULTS =
      ".level=INFO\nhandlers=com.google.j2objc.util.logging.IOSLogHandler\n";

  private static final String ASLCLIENT = "IOSLogHandler-aslclient";

  /**
   * An os_log_t object used to group all os_log() calls from this handler into a category.
   */
  private Object nativeLog;

  public IOSLogHandler() {
    setFormatter(new IOSLogFormatter());
  }

  @Override
  public void close() {
    // No action needed
  }

  @Override
  public void flush() {
    // No action needed
  }

  @Override
  public void publish(LogRecord record) {
    if (!isLoggable(record)) {
      return;
    }
    if (shouldUseOSLog()) {
      publishWithOSLog(record);
    } else {
      publishWithAsl(record);
    }
  }

  private native boolean shouldUseOSLog() /*-[
  #if OS_LOG_AVAILABLE
    return YES;
  #else
    return NO;
  #endif
  ]-*/;

  private String getLogMessage(LogRecord record) {
    StringBuilder sb = new StringBuilder(getFormatter().format(record));
    if (record.getThrown() != null) {
      sb.append('\n');
      StringWriter stringWriter = new StringWriter();
      record.getThrown().printStackTrace(new PrintWriter(stringWriter));
      sb.append(stringWriter.toString());
    }
    return sb.toString();
  }

  private void publishWithAsl(LogRecord record) {
    int aslLevel;
    switch (record.getLevel().intValue()) {
      case 1000:       // Level.SEVERE
        aslLevel = 3;  // ASL_LEVEL_ERR
        break;
      case 900:        // Level.WARNING
        aslLevel = 4;  // ASL_LEVEL_WARNING
        break;
      case 800:        // Level.INFO
      case 700:        // Level.CONFIG
        aslLevel = 5;  // ASL_LEVEL_NOTICE
        break;
      default:
        aslLevel = 6;  // ASL_LEVEL_INFO
    }
    aslLog(getLogMessage(record), aslLevel);
  }

  private native void aslLog(String logMessage, int aslLevel) /*-[
    // TODO(tball): update ASL use to iOS 10's os_log and remove clang pragmas.
    #pragma clang diagnostic push
    #pragma GCC diagnostic ignored "-Wdeprecated-declarations"

    // Add stderr as a log file, so that log messages are seen on the debug log,
    // and not just the device log.
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        asl_add_log_file(NULL, STDERR_FILENO);
    });

    NSThread *currentThread = [NSThread currentThread];
    NSMutableDictionary *threadData = [currentThread threadDictionary];
    ASLClientHolder *logClient =
        [threadData objectForKey:ComGoogleJ2objcUtilLoggingIOSLogHandler_ASLCLIENT];
    if (!logClient) {
      aslclient aslClient = asl_open([[currentThread name] UTF8String],
          [[[NSBundle mainBundle] bundleIdentifier] UTF8String], ASL_OPT_NO_DELAY | ASL_OPT_STDERR);
      logClient = AUTORELEASE([[ASLClientHolder alloc] initWithClient:aslClient]);
      [threadData setObject:logClient forKey:ComGoogleJ2objcUtilLoggingIOSLogHandler_ASLCLIENT];
    }
    asl_log(logClient->_client, NULL, aslLevel, "%s", [logMessage UTF8String]);
    #pragma clang diagnostic pop
  ]-*/;

  private void publishWithOSLog(LogRecord record) {
    osLog(getLogMessage(record), record.getLevel().intValue());
  }

  private native void osLog(String logMessage, int logLevel) /*-[
  #if OS_LOG_AVAILABLE
    os_log_type_t logType = OS_LOG_TYPE_DEBUG;
    switch (logLevel) {
      case 1000:  // Level.SEVERE
        logType = OS_LOG_TYPE_ERROR;
        break;
      case 900:   // Level.WARNING
        logType = OS_LOG_TYPE_DEFAULT;
        break;
      case 800:   // Level.INFO
      case 700:   // Level.CONFIG
        logType = OS_LOG_TYPE_INFO;
        break;
    }
    if (!self->nativeLog_) {
      self->nativeLog_ =
        [[NativeLog alloc] initWithSubsystem:@"com.google.j2objc.util.logging.IOSLogHandler"
                                    category:@"general"];
    }
    NativeLog *nativeLog = (NativeLog *)self->nativeLog_;
    os_log_with_type(nativeLog->_log, logType, "%{public}s", logMessage.UTF8String);
  #else
    JreThrowAssertionError(@"Failure in os_log support. Expected to use os_log only when the\
                             framework is supported by the base SDK.");
  #endif
  ]-*/;
}
