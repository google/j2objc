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

/**
 * Simplified Level class for use with iOS apps.  See 
 *  <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/Formatter.html"> 
 *  The Java API doc for details</a>.
 *
 * @author Tom Ball
 */
public class Level {
  private static ArrayList<Level> known = new ArrayList<Level>();
  private final String name;
  private final int value;

  public static final Level OFF = new Level("OFF", Integer.MAX_VALUE);
  public static final Level SEVERE = new Level("SEVERE", 1000);
  public static final Level WARNING = new Level("WARNING", 900);
  public static final Level INFO = new Level("INFO", 800);
  public static final Level CONFIG = new Level("CONFIG", 700);
  public static final Level FINE = new Level("FINE", 500);
  public static final Level FINER = new Level("FINER", 400);
  public static final Level FINEST = new Level("FINEST", 300);
  public static final Level ALL = new Level("ALL", Integer.MIN_VALUE);

  private Level(String name, int value) {
    this.name = name;
    this.value = value;
    known.add(this);
  }

  public String getName() {
    return name;
  }

  public String getLocalizedName() {
    return name;
  }

  public final String toString() {
    return name;
  }

  public final int intValue() {
    return value;
  }

  public static synchronized Level parse(String name) throws IllegalArgumentException {
    for (int i = 0; i < known.size(); i++) {
      Level l = (Level) known.get(i);
      if (name.equals(l.name)) {
        return l;
      }
    }
    throw new IllegalArgumentException("Bad level \"" + name + "\"");
  }

  public boolean equals(Object ox) {
    try {
      Level lx = (Level) ox;
      return (lx.value == this.value);
    } catch (Exception ex) {
      return false;
    }
  }

  public int hashCode() {
    return this.value;
  }
}
