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

package java.util.concurrent;

/**
 * Handwritten version of TimeUnit for J2ObjC.
 *
 * This class is not translated by J2ObjC, due to incorrect enum
 * translation bug.
 *
 * @author Pankaj Kakkar
 */
public enum TimeUnit {
  // TODO(user): update bug id in comments to public issue numbers when
  // issue tracking is sync'd.

  DAYS        (1000L * 1000L * 1000L * 3600L * 24L),
  HOURS       (1000L * 1000L * 1000L * 3600L),
  MINUTES     (1000L * 1000L * 1000L * 60L),
  SECONDS     (1000L * 1000L * 1000L),
  MILLISECONDS(1000L * 1000L),
  MICROSECONDS(1000L),
  NANOSECONDS (1L);

  // The value of this TimeUnit in nanoseconds
  private long value;

  TimeUnit(long value) {
    this.value = value;
  }

  public long convert(long sourceDuration, TimeUnit unit) {
    return convert(sourceDuration * unit.value);
  }

  // Not implemented: sleep timedJoin timedWait
  // Require java.lang.Thread

  public long toDays(long duration) {
    return DAYS.convert(duration, this);
  }

  public long toHours(long duration) {
    return HOURS.convert(duration, this);
  }

  public long toMinutes(long duration) {
    return MINUTES.convert(duration, this);
  }

  public long toSeconds(long duration) {
    return SECONDS.convert(duration, this);
  }

  public long toMillis(long duration) {
    return MILLISECONDS.convert(duration, this);
  }

  public long toMicros(long duration) {
    return MICROSECONDS.convert(duration, this);
  }

  public long toNanos(long duration) {
    return NANOSECONDS.convert(duration, this);
  }

  private long convert(long sourceDurationNs) {
    return sourceDurationNs / this.value;
  }
}
