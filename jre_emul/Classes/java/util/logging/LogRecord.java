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

import java.io.Serializable;
import java.util.Date;

/**
 *  An emulation of the java.util.logging.LogRecord class. See 
 *  <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/LogRecord.html"> 
 *  The Java API doc for details</a>
 */
public class LogRecord implements Serializable {
  private Level level;
  private String loggerName = "";
  private String msg;
  private Throwable thrown = null;
  private long millis;
  
  public LogRecord(Level level, String msg) {
    this.level = level;
    this.msg = msg;
    millis = new Date().getTime();
  }
  
  protected LogRecord() {
    // for serialization
  }
  
  public Level getLevel() {
    return level;
  }
  
  public String getLoggerName() {
    return loggerName;
  }
  
  public String getMessage() {
    return msg;
  }
  
  public long getMillis() {
    return millis;
  }
  
  public Throwable getThrown() {
    return thrown;
  } 
  
  public void setLevel(Level newLevel) {
    level = newLevel;
  } 
  
  public void setLoggerName(String newName) {
    loggerName = newName;
  }
  
  public void setMessage(String newMessage) {
    msg = newMessage;
  }
  
  public void setMillis(long newMillis) {
    millis = newMillis;
  }

  public void setThrown(Throwable newThrown) {
    thrown = newThrown;
  }

  /* Not Implemented */
  // public Object[] getParameters() {} 
  // public ResourceBundle getResourceBundle() {} 
  // public String getResourceBundleName() {}
  // public long getSequenceNumber() {}
  // public String getSourceClassName() {}
  // public String getSourceMethodName() {}
  // public int getThreadID() {}
  // public void setParameters(Object[] parameters) {} 
  // public void setResourceBundle(ResourceBundle bundle) {} 
  // public void setResourceBundleName(String name) {}
  // public void setSequenceNumber(long seq) {}
  // public void setSourceClassName(String sourceClassName) {} 
  // public void setSourceMethodName(String sourceMethodName) {}
  // public void setThreadID(int threadID) {}
}
