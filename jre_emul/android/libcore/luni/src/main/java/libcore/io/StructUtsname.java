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
 * Information returned by uname(2). Corresponds to C's
 * {@code struct utsname} from
 * <a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_utsname.h.html">&lt;sys/utsname.h&gt;</a>
 */
public final class StructUtsname {
    /** The OS name, such as "Linux". */
    public final String sysname;

    /** The machine's unqualified name on some implementation-defined network. */
    public final String nodename;

    /** The OS release, such as "2.6.35-27-generic". */
    public final String release;

    /** The OS version, such as "#48-Ubuntu SMP Tue Feb 22 20:25:29 UTC 2011". */
    public final String version;

    /** The machine architecture, such as "armv7l" or "x86_64". */
    public final String machine;

    public StructUtsname(String sysname, String nodename, String release, String version, String machine) {
        this.sysname = sysname;
        this.nodename = nodename;
        this.release = release;
        this.version = version;
        this.machine = machine;
    }
}
