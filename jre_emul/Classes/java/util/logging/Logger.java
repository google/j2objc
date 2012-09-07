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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

/**
 * Stub of java.util.logging.Logger, simplified for iOS use. It doesn't
 * support filters, formatters, or multiple loggers, but instead directly
 * calls NSLog().
 *
 * @author Tom Ball
 */
public class Logger {
  private List<Handler> handlers;
  private String name;
  private Level level;
  private volatile int levelValue;
  private Logger parent;
  private boolean useParentHandlers;

  public static final String GLOBAL_LOGGER_NAME = "global";

  public static synchronized Logger getLogger(String name) {
    LogManager manager = LogManager.getLogManager();
    Logger logger = manager.getLogger(name);
    if (logger == null) {
      Logger newLogger = new Logger(name, "");
      manager.addLogger(newLogger);
      return newLogger;
    }
    return logger;
  }

  protected Logger(String name, String resourceName) {
    //TODO(user): check OS X user defaults, so level can be changed from command-line
    level = null;
    this.useParentHandlers = true;
    handlers = new ArrayList<Handler>();
    this.name = name;
  }

  /**
   * All the other logging methods in this class call through this method to
   * actually perform any logging.
   */
  public void log(LogRecord record) {
    if (isLoggable(record.getLevel())) {
      for (Handler h : handlers) {
        h.publish(record);
      }
      if (useParentHandlers && parent != null) {
        parent.log(record);
      }
    }
  }

  public void log(Level level, String msg) {
    log(level, msg, null);
  }

  public void log(Level level, String msg, Throwable thrown) {
    if (isLoggable(level)) {
      LogRecord lr = new LogRecord(level, msg);
      lr.setThrown(thrown);
      lr.setLoggerName(getName());
      log(lr);
    }
  }

  public void severe(String msg) {
    if (Level.SEVERE.intValue() < levelValue) {
      return;
    }
    log(Level.SEVERE, msg);
  }

  public void warning(String msg) {
    if (Level.WARNING.intValue() < levelValue) {
      return;
    }
    log(Level.WARNING, msg);
  }

  public void info(String msg) {
    if (Level.INFO.intValue() < levelValue) {
      return;
    }
    log(Level.INFO, msg);
  }

  public void config(String msg) {
    if (Level.CONFIG.intValue() < levelValue) {
      return;
    }
    log(Level.CONFIG, msg);
  }

  public void fine(String msg) {
    if (Level.FINE.intValue() < levelValue) {
      return;
    }
    log(Level.FINE, msg);
  }

  public void finer(String msg) {
    if (Level.FINER.intValue() < levelValue) {
      return;
    }
    log(Level.FINER, msg);
  }

  public void finest(String msg) {
    if (Level.FINEST.intValue() < levelValue) {
      return;
    }
    log(Level.FINEST, msg);
  }

  public void addHandler(Handler handler) {
    handlers.add(handler);
  }

  public Handler[] getHandlers() {
    if (handlers.size() > 0) {
      return handlers.toArray(new Handler[handlers.size()]);
    }
    return null;
  }

  public void removeHandler(Handler handler) {
    handlers.remove(handler);
  }

  public Level getLevel() {
    return level != null ? level : getParent().getLevel();
  }

  public void setLevel(Level newLevel) {
    level = newLevel;
    levelValue = newLevel.intValue();
    for (Handler handler : handlers) {
      handler.setLevel(newLevel);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String newName) {
    name = newName;
  }

  public Logger getParent() {
    return parent;
  }

  public void setParent(Logger newParent) {
    if (newParent != null) {
      parent = newParent;
    }
  }

  public boolean getUseParentHandlers() {
    return useParentHandlers;
  }

  public void setUseParentHandlers(boolean newUseParentHandlers) {
    useParentHandlers = newUseParentHandlers;
  }

  public boolean isLoggable(Level messageLevel) {
    return getLevel().intValue() <= messageLevel.intValue();
  }

  /* Not Implemented */
  // public static Logger getAnonymousLogger() {}
  // public static Logger getAnonymousLogger(String resourceBundleName) {}
  // public Filter getFilter() {}
  // public static Logger getLogger(String name, String resourceBundleName) {}
  // public ResourceBundle getResourceBundle() {}
  // public String getResourceBundleName() {}
  // public void setFilter(Filter newFilter) {}
  // public void entering(String sourceClass, String sourceMethod) {}
  // public void entering(String sourceClass, String sourceMethod, Object param1) {}
  // public void entering(String sourceClass, String sourceMethod, Object[] params) {}
  // public void exiting(String sourceClass, String sourceMethod, Object result) {}
  // public void exiting(String sourceClass, String sourceMethod) {}
  // public void log(Level level, String msg, Object param1) {}
  // public void log(Level level, String msg, Object[] params) {}
  // public void logp(Level level, String sourceClass, String sourceMethod, String msg) {}
  // public void logp(Level level, String sourceClass, String sourceMethod, String msg,
  //     Object param1) {}
  // public void logp(Level level, String sourceClass, String sourceMethod, String msg,
  //     Object[] params) {}
  // public void logp(Level level, String sourceClass, String sourceMethod, String msg,
  //     Throwable thrown) {}
  // public void logrb(Level level, String sourceClass, String sourceMethod,
  //     String bundleName, String msg) {}
  // public void logrb(Level level, String sourceClass, String sourceMethod,
  //     String bundleName, String msg, Object param1) {}
  // public void logrb(Level level, String sourceClass, String sourceMethod,
  //     String bundleName, String msg, Object[] params) {}
  // public void logrb(Level level, String sourceClass, String sourceMethod,
  //     String bundleName, String msg, Throwable thrown) {}
  // public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {}
}
