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
 *  An emulation of the java.util.logging.Handler class. See
 *  <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/Handler.html">
 *  The Java API doc for details</a>
 */
public abstract class Handler {
  private Formatter formatter;
  private Level level;

  public abstract void close();
  public abstract void flush();

  public Formatter getFormatter() {
    return formatter;
  }

  public Level getLevel() {
    if (level != null) {
      return level;
    }
    return Level.ALL;
  }

  public boolean isLoggable(LogRecord record) {
    return getLevel().intValue() <= record.getLevel().intValue();
  }

  public abstract void publish(LogRecord record);

  public void setFormatter(Formatter newFormatter) {
    formatter = newFormatter;
  }

  public void setLevel(Level newLevel) {
    level = newLevel;
  }

  /* Not Implemented */
  // public String getEncoding() {}
  // public ErrorManager getErrorManager() {}
  // public Filter getFilter() {}
  // public protected void reportError(String msg, Exception ex, int code) {}
  // public void setEncoding(String encoding) {}
  // public void setErrorManager(ErrorManager em) {}
  // public void setFilter(Filter newFilter) {}
}
