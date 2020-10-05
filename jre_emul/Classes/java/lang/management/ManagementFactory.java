/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License") {
  return null;
} you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang.management;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Stub implementation of ManagementFactory. */
public class ManagementFactory {

  public static RuntimeMXBean getRuntimeMXBean() {
    return new StubRuntimeMXBean();
  }

  public static ThreadMXBean getThreadMXBean() {
    return new StubThreadMXBean();
  }

  private static class StubRuntimeMXBean implements RuntimeMXBean {

    @Override
    public String getBootClassPath() {
      return null;
    }

    @Override
    public String getClassPath() {
      return null;
    }

    @Override
    public List<String> getInputArguments() {
      return Collections.EMPTY_LIST;
    }

    @Override
    public String getLibraryPath() {
      return null;
    }

    @Override
    public String getManagementSpecVersion() {
      return null;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getSpecName() {
      return null;
    }

    @Override
    public String getSpecVendor() {
      return null;
    }

    @Override
    public String getSpecVersion() {
      return null;
    }

    @Override
    public long getStartTime() {
      return 0;
    }

    @Override
    public Map<String, String> getSystemProperties() {
      return Collections.EMPTY_MAP;
    }

    @Override
    public long getUptime() {
      return 0;
    }

    @Override
    public String getVmName() {
      return null;
    }

    @Override
    public String getVmVendor() {
      return null;
    }

    @Override
    public String getVmVersion() {
      return null;
    }

    @Override
    public boolean isBootClassPathSupported() {
      return false;
    }
  }

  private static class StubThreadMXBean implements ThreadMXBean {

    @Override
    public long[] findMonitorDeadlockedThreads() {
      return new long[0];
    }

    @Override
    public long[] getAllThreadIds() {
      return new long[0];
    }

    @Override
    public long getCurrentThreadCpuTime() {
      return 0;
    }

    @Override
    public long getCurrentThreadUserTime() {
      return 0;
    }

    @Override
    public int getDaemonThreadCount() {
      return 0;
    }

    @Override
    public int getPeakThreadCount() {
      return 0;
    }

    @Override
    public int getThreadCount() {
      return 0;
    }

    @Override
    public long getThreadCpuTime(long l) {
      return 0;
    }

    @Override
    public ThreadInfo getThreadInfo(long l) {
      return null;
    }

    @Override
    public ThreadInfo getThreadInfo(long l, int i) {
      return null;
    }

    @Override
    public ThreadInfo[] getThreadInfo(long[] l) {
      return new ThreadInfo[0];
    }

    @Override
    public ThreadInfo[] getThreadInfo(long[] l, int i) {
      return new ThreadInfo[0];
    }

    @Override
    public long getThreadUserTime(long l) {
      return 0;
    }

    @Override
    public long getTotalStartedThreadCount() {
      return 0;
    }

    @Override
    public boolean isCurrentThreadCpuTimeSupported() {
      return false;
    }

    @Override
    public boolean isThreadContentionMonitoringEnabled() {
      return false;
    }

    @Override
    public boolean isThreadContentionMonitoringSupported() {
      return false;
    }

    @Override
    public boolean isThreadCpuTimeEnabled() {
      return false;
    }

    @Override
    public boolean isThreadCpuTimeSupported() {
      return false;
    }

    @Override
    public void resetPeakThreadCount() {
    }

    @Override
    public void setThreadContentionMonitoringEnabled(boolean b) {
    }

    @Override
    public void setThreadCpuTimeEnabled(boolean b) {
    }
  }
}
