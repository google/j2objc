/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
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

package com.google.j2objc.nio.file.attribute;

import java.util.concurrent.TimeUnit;

/**
 * Convert FileTime units.
 *
 * @author Mary Qin
 */
public class FileTime implements Comparable<FileTime>{

  private final long millis;

  FileTime(long millis) {
    this.millis = millis;
  }

  public static FileTime from(long value, TimeUnit unit) {
    switch (unit) {
      case NANOSECONDS:
        return new FileTime(value / 1000000);
      case MICROSECONDS:
        return new FileTime( value / 1000);
      case MILLISECONDS:
        return new FileTime(value);
      case SECONDS:
        return new FileTime(value * 1000);
      case MINUTES:
        return new FileTime(value * 60000);
      case HOURS:
        return new FileTime(value * 3600000);
      case DAYS:
        return new FileTime(value * 86400000);
      default:
        throw new IllegalArgumentException("unknown TimeUnit type: " + unit);
    }
  }

  public long to(TimeUnit unit) {
    switch (unit) {
      case NANOSECONDS:
        return millis * 1000000;
      case MICROSECONDS:
        return millis * 1000;
      case MILLISECONDS:
        return millis;
      case SECONDS:
        return millis / 1000;
      case MINUTES:
        return millis / 60000;
      case HOURS:
        return millis / 3600000;
      case DAYS:
        return millis / 86400000;
      default:
        throw new IllegalArgumentException("unknown TimeUnit type: " + unit);
    }
  }

  public long toMillis() {
    return millis;
  }

  public static FileTime fromMillis(long value) {
    return new FileTime(value);
  }

  @Override
  public int compareTo(FileTime o) {
    if (o.millis < millis) {
      return -1;
    } else if (o.millis == millis) {
      return 0;
    }
    return 1;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof FileTime) ? compareTo((FileTime) o) == 0 : false;
  }
}

