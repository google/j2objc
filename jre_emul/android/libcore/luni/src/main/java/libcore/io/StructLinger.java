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
 * Corresponds to C's {@code struct linger} from
 * <a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_socket.h.html">&lt;sys/socket.h&gt;</a>
 */
public final class StructLinger {
    /** Whether or not linger is enabled. Non-zero is on. */
    public final int l_onoff;

    /** Linger time in seconds. */
    public final int l_linger;

    public StructLinger(int l_onoff, int l_linger) {
        this.l_onoff = l_onoff;
        this.l_linger = l_linger;
    }

    public boolean isOn() {
        return l_onoff != 0;
    }

    @Override public String toString() {
        return "StructLinger[l_onoff=" + l_onoff + ",l_linger=" + l_linger + "]";
    }
}
