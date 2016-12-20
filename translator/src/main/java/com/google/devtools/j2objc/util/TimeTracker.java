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

package com.google.devtools.j2objc.util;

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options.TimingLevel;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for logging time slices of an operation. Supports slicing at multiple
 * levels so that one slice can be divided into sub-slices.
 *
 * @author Keith Stanger
 */
public class TimeTracker {

  public static TimeTracker getTicker(String name, TimingLevel timingLevel) {
    if (timingLevel == TimingLevel.ALL) {
      return TimeTracker.start(name);
    } else {
      return TimeTracker.noop();
    }
  }

  public static TimeTracker noop() {
    return new TimeTracker();
  }

  public static TimeTracker start(String name) {
    return new TimeTrackerImpl(name);
  }

  public void tick(String event) {
  }

  public void push() {
  }

  public void pop() {
  }

  public void printResults(PrintStream out) {
  }

  private static class TimeTrackerImpl extends TimeTracker {

    private static final int MAX_LEVELS = 8;
    private static final String[] INDENTS = createIndents();

    private static String[] createIndents() {
      String[] indents = new String[MAX_LEVELS];
      for (int i = 0; i < MAX_LEVELS; i++) {
        char[] chars = new char[i * 2];
        Arrays.fill(chars, ' ');
        indents[i] = new String(chars);
      }
      return indents;
    }

    long[] lastTicks = new long[16];
    int currentLevel = 0;
    List<String> entries = Lists.newArrayList();

    private TimeTrackerImpl(String name) {
      entries.add("Timings for " + name);
      lastTicks[currentLevel] = System.currentTimeMillis();
    }

    @Override
    public void tick(String event) {
      long now = System.currentTimeMillis();
      long time = now - lastTicks[currentLevel];
      lastTicks[currentLevel] = now;
      entries.add(String.format("%s%5d ms - %s", INDENTS[currentLevel], time, event));
    }

    @Override
    public void push() {
      currentLevel++;
      lastTicks[currentLevel] = System.currentTimeMillis();
    }

    @Override
    public void pop() {
      currentLevel--;
    }

    @Override
    public void printResults(PrintStream out) {
      for (String entry : entries) {
        out.println(entry);
      }
    }
  }
}
