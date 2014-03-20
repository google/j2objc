/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.io;

/**
 * Corresponds to C's {@code struct timeval} from
 * <a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_time.h.html">&lt;sys/time.h&gt;</a>
 */
public final class StructTimeval {
    /** Seconds. */
    public final long tv_sec;

    /** Microseconds. */
    public final long tv_usec;

    private StructTimeval(long tv_sec, long tv_usec) {
        this.tv_sec = tv_sec;
        this.tv_usec = tv_usec;
    }

    public static StructTimeval fromMillis(long millis) {
        long tv_sec = millis / 1000;
        long tv_usec = (millis - (tv_sec * 1000)) * 1000;
        return new StructTimeval(tv_sec, tv_usec);
    }

    public long toMillis() {
        return (tv_sec * 1000) + (tv_usec / 1000);
    }

    @Override public String toString() {
        return "StructTimeval[tv_sec=" + tv_sec + ",tv_usec=" + tv_usec + "]";
    }
}
