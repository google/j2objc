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

package com.google.j2objc.util;

/* J2ObjC Removed: avoid using Java FileTime.
import java.nio.file.attribute.FileTime; */
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;

public class ZipUtils {

	/**
	 * J2ObjC added: avoids using java.nio.file.attribute.FileTime,
	 * instead implements FileTime class with relevant methods.
	 */
	public static class FileTime implements Comparable<FileTime>{

		private final long millis;

		FileTime(long millis) {
			this.millis = millis;
		}

		public static FileTime from(long value, TimeUnit unit) {
			if (unit.equals(TimeUnit.MICROSECONDS)) {
				return new FileTime( value / 1000);
			}
			if (unit.equals(TimeUnit.MILLISECONDS)) {
				return new FileTime(value);
			}
			if (unit.equals(TimeUnit.SECONDS)) {
				return new FileTime(1000 * value);
			}
			return new FileTime(0);
		}

		public long to(TimeUnit unit) {
			if (unit.equals(TimeUnit.MICROSECONDS)) {
				return 1000 * millis;
			}
			if (unit.equals(TimeUnit.MILLISECONDS)) {
				return millis;
			}
			if (unit.equals(TimeUnit.SECONDS)) {
				return millis / 1000;
			}
			return 0;
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

	// used to adjust values between Windows and java epoch
	private static final long WINDOWS_EPOCH_IN_MICROSECONDS = -11644473600000000L;

	/**
	 * Converts Windows time (in microseconds, UTC/GMT) time to FileTime.
	 */
	public static final FileTime winTimeToFileTime(long wtime) {
		return FileTime.from(wtime / 10 + WINDOWS_EPOCH_IN_MICROSECONDS,
							 TimeUnit.MICROSECONDS);
	}

	/**
	 * Converts FileTime to Windows time.
	 */
	public static final long fileTimeToWinTime(FileTime ftime) {
		return (ftime.to(TimeUnit.MICROSECONDS) - WINDOWS_EPOCH_IN_MICROSECONDS) * 10;
	}

	/**
	 * Converts "standard Unix time"(in seconds, UTC/GMT) to FileTime
	 */
	public static final FileTime unixTimeToFileTime(long utime) {
		return FileTime.from(utime, TimeUnit.SECONDS);
	}

	/**
	 * Converts FileTime to "standard Unix time".
	 */
	public static final long fileTimeToUnixTime(FileTime ftime) {
		return ftime.to(TimeUnit.SECONDS);
	}

	/**
	 * Converts DOS time to Java time (number of milliseconds since epoch).
	 */
	private static long dosToJavaTime(long dtime) {
		@SuppressWarnings("deprecation") // Use of date constructor.
		Date d = new Date((int)(((dtime >> 25) & 0x7f) + 80),
						  (int)(((dtime >> 21) & 0x0f) - 1),
						  (int)((dtime >> 16) & 0x1f),
						  (int)((dtime >> 11) & 0x1f),
						  (int)((dtime >> 5) & 0x3f),
						  (int)((dtime << 1) & 0x3e));
		return d.getTime();
	}

	/**
	 * Converts extended DOS time to Java time, where up to 1999 milliseconds
	 * might be encoded into the upper half of the returned long.
	 *
	 * @param xdostime the extended DOS time value
	 * @return milliseconds since epoch
	 */
	public static long extendedDosToJavaTime(long xdostime) {
		long time = dosToJavaTime(xdostime);
		return time + (xdostime >> 32);
	}

	/**
	 * Converts Java time to DOS time.
	 */
	@SuppressWarnings("deprecation") // Use of date methods
	private static long javaToDosTime(long time) {
		Date d = new Date(time);
		int year = d.getYear() + 1900;
		if (year < 1980) {
			return ZipEntry.DOSTIME_BEFORE_1980;
		}
		// Android-changed: backport of JDK-8130914 fix
		return ((year - 1980) << 25 | (d.getMonth() + 1) << 21 |
				d.getDate() << 16 | d.getHours() << 11 | d.getMinutes() << 5 |
				d.getSeconds() >> 1) & 0xffffffffL;
	}

	/**
	 * Converts Java time to DOS time, encoding any milliseconds lost
	 * in the conversion into the upper half of the returned long.
	 *
	 * @param time milliseconds since epoch
	 * @return DOS time with 2s remainder encoded into upper half
	 */
	public static long javaToExtendedDosTime(long time) {
		if (time < 0) {
			return ZipEntry.DOSTIME_BEFORE_1980;
		}
		long dostime = javaToDosTime(time);
		return (dostime != ZipEntry.DOSTIME_BEFORE_1980)
				? dostime + ((time % 2000) << 32)
				: ZipEntry.DOSTIME_BEFORE_1980;
	}

	/**
	 * Fetches unsigned 16-bit value from byte array at specified offset.
	 * The bytes are assumed to be in Intel (little-endian) byte order.
	 */
	public static final int get16(byte b[], int off) {
		return Byte.toUnsignedInt(b[off]) | (Byte.toUnsignedInt(b[off+1]) << 8);
	}

	/**
	 * Fetches unsigned 32-bit value from byte array at specified offset.
	 * The bytes are assumed to be in Intel (little-endian) byte order.
	 */
	public static final long get32(byte b[], int off) {
		return (get16(b, off) | ((long)get16(b, off+2) << 16)) & 0xffffffffL;
	}

	/**
	 * Fetches signed 64-bit value from byte array at specified offset.
	 * The bytes are assumed to be in Intel (little-endian) byte order.
	 */
	public static final long get64(byte b[], int off) {
		return get32(b, off) | (get32(b, off+4) << 32);
	}
}
