/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.util.logging;

/**
 *  An emulation of the java.util.logging.Formatter class. See
 *  <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/Formatter.html">
 *  The Java API doc for details</a>
 */
public abstract class Formatter {
  public abstract String format(LogRecord record);

  public String formatMessage(LogRecord record) {
    return format(record);
  }

  /* Not Implemented */
  // public String getHead(Handler h) {}
  // public String getTail(Handler h) {}

}
