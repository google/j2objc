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

package java.util.logging;

/*-[
#import "java/lang/Throwable.h"
]-*/

/**
 * Handler implementation that calls iOS NSLog().
 *
 * @author Tom Ball
 */
class NSLogHandler extends Handler {

  class NSLogFormatter extends Formatter {

    /**
     * Very simple formatter, since NSLog adds its own text.
     */
    @Override
    public String format(LogRecord record) {
      StringBuffer sb = new StringBuffer();
      sb.append(record.getLevel().getLocalizedName());
      sb.append(": ");
      sb.append(record.getMessage());
      return sb.toString();
    }
  }

  public NSLogHandler() {
    setFormatter(new NSLogFormatter());
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
    log(record);
  }

  private native void log(LogRecord record) /*-{
    NSLog(@"%@: %@", [NSThread currentThread],
       [[self getFormatter] formatWithJavaUtilLoggingLogRecord:record]);
    [[record getThrown] printStackTrace];

    // TODO(user): replace NSLog above with stderr below, once desired format
    // is decided.
//    NSString *msg =
//        [[self getFormatter] formatWithJavaUtilLoggingLogRecord:record];
//    const char *cmsg = [msg cStringUsingEncoding:NSASCIIStringEncoding];
//    fprintf(stderr, "%s\n", cmsg);
  }-*/;

}
