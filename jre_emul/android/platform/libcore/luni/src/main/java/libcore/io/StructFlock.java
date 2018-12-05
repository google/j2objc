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
 * Information returned/taken by fcntl(2) F_GETFL and F_SETFL. Corresponds to C's
 * {@code struct flock} from
 * <a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fcntl.h.html">&lt;fcntl.h&gt;</a>
 */
public final class StructFlock {
    /** The operation type, one of F_RDLCK, F_WRLCK, or F_UNLCK. */
    public short l_type;

    /** How to interpret l_start, one of SEEK_CUR, SEEK_END, SEEK_SET. */
    public short l_whence;

    /** Start offset. */
    public long l_start; /*off_t*/

    /** Byte count to operate on. */
    public long l_len; /*off_t*/

    /** Process blocking our lock (filled in by F_GETLK, otherwise unused). */
    public int l_pid; /*pid_t*/
}
