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

import java.util.HashMap;

/**
 *  An emulation of the java.util.logging.LogManager class. See the
 *  <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/LogManger.html">
 *  Java API doc</a> for details.
 */
public class LogManager {
  /**
   * Since the Logger constructor is protected, the LogManager cannot create
   * one directly, so we create a RootLogger which has an exposed constructor.
   */
  private class RootLogger extends Logger {
    public RootLogger() {
      super("", null);
      addHandler(new NSLogHandler());
      setLevel(Level.WARNING);
    }
  }

  private static LogManager singleton;

  public static LogManager getLogManager() {
    if (singleton == null) {
      singleton = new LogManager();
    }
    return singleton;
  }

  private HashMap<String, Logger> loggerList;
  private Logger rootLogger;

  protected LogManager() {
    loggerList = new HashMap<String, Logger>();
    rootLogger = new RootLogger();
    loggerList.put("", rootLogger);
  }

  public boolean addLogger(Logger logger) {
    if (getLogger(logger.getName()) != null) {
      return false;
    }
    addLoggerWithoutDuplicationChecking(logger);
    return true;
  }

  public Logger getLogger(String name) {
    return loggerList.get(name);
  }

  /**
   *  Helper function to add a logger when we have already determined that it
   *  does not exist.  When we add a logger, we recursively add all of its
   *  ancestors. Since loggers do not get removed, logger creation is cheap,
   *  and there are not usually too many loggers in an ancestry chain,
   *  this is a simple way to ensure that the parent/child relationships are
   *  always correctly set up.
   */
  private void addLoggerWithoutDuplicationChecking(Logger logger) {
    String name = logger.getName();
    String parentName = name.substring(0, Math.max(0, name.lastIndexOf('.')));
    Logger parent = getOrAddLogger(parentName);
    loggerList.put(logger.getName(), logger);
    logger.setParent(parent);
  }

  /**
   *  Helper function to create a logger if it does not exist since the public
   *  APIs for getLogger and addLogger make it difficult to use those functions
   *  for this.
   */
  private Logger getOrAddLogger(String name) {
    Logger logger = getLogger(name);
    if (logger == null) {
      Logger newLogger = new Logger(name, null);
      addLoggerWithoutDuplicationChecking(newLogger);
      return newLogger;
    }
    return logger;
  }

  /* Not Implemented */
  // public void addPropertyChangeListener(PropertyChangeListener l) {}
  // public void checkAccess() {}
  // public Enumeration getLoggerNames() {}
  // public String getProperty(String name) {}
  // public void readConfiguration() {}
  // public void readConfiguration(InputStream ins) {}
  // public void removePropertyChangeListener(PropertyChangeListener l) {}
  // public void reset() {}
}
