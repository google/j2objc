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

package java.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

/*-[
#import "java/lang/Throwable.h"
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
]-*/

/**
 * Handler implementation that calls iOS asl_log().
 *
 * @author Tom Ball
 */
class IOSLogHandler extends Handler {

  static class IOSLogFormatter extends Formatter {

    /**
     * Very simple formatter, since asl_log adds its own text.
     */
    @Override
    public String format(LogRecord record) {
      return formatMessage(record);
    }
  }

  static final String IOS_LOG_MANAGER_DEFAULTS =
      ".level=INFO\nhandlers=java.util.logging.IOSLogHandler\n";

  private static final String ASLCLIENT = "IOSLogHandler-aslclient";

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
    StringBuilder sb = new StringBuilder(getFormatter().format(record));
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

    if (record.getThrown() != null) {
      sb.append('\n');
      StringWriter stringWriter = new StringWriter();
      record.getThrown().printStackTrace(new PrintWriter(stringWriter));
      sb.append(stringWriter.toString());
    }
    log(sb.toString(), aslLevel);
  }

  private native void log(String logMessage, int aslLevel) /*-[
    // Add stderr as a log file, so that log messages are seen on the debug log,
    // and not just the device log.
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        asl_add_log_file(NULL, STDERR_FILENO);
    });

    NSThread *currentThread = [NSThread currentThread];
    NSMutableDictionary *threadData = [currentThread threadDictionary];
    ASLClientHolder *logClient = [threadData objectForKey:JavaUtilLoggingIOSLogHandler_ASLCLIENT];
    if (!logClient) {
      aslclient aslClient = asl_open([[currentThread name] UTF8String],
          [[[NSBundle mainBundle] bundleIdentifier] UTF8String], ASL_OPT_NO_DELAY | ASL_OPT_STDERR);
      logClient = AUTORELEASE([[ASLClientHolder alloc] initWithClient:aslClient]);
      [threadData setObject:logClient forKey:JavaUtilLoggingIOSLogHandler_ASLCLIENT];
    }
    asl_log(logClient->_client, NULL, aslLevel, "%s", [logMessage UTF8String]);
  ]-*/;
}
