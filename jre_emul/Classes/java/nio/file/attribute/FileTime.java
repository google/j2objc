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

package java.nio.file.attribute;

import java.util.concurrent.TimeUnit;

/**
 * Convert FileTime units.
 *
 * This is a complete j2objc rewrite, designed to avoid Android's
 * FileTime java.time dependencies. If an app only needs jre_channels (which many do),
 * adding java.nio.file.FileTime to jre_channels would increase its size by ~23M.
 *
 * @author Mary Qin
 */
public class FileTime implements Comparable<FileTime>{

  private final long millis;

  FileTime(long millis) {
    this.millis = millis;
  }

  public static FileTime from(long value, TimeUnit unit) {
    return new FileTime(TimeUnit.MILLISECONDS.convert(value, unit));
  }

  public long to(TimeUnit unit) {
    return unit.convert(millis, TimeUnit.MILLISECONDS);
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

