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
 * File information returned by fstat(2), lstat(2), and stat(2). Corresponds to C's
 * {@code struct stat} from
 * <a href="http://www.opengroup.org/onlinepubs/000095399/basedefs/sys/stat.h.html">&lt;stat.h&gt;</a>
 */
public final class StructStat {
    /** Device ID of device containing file. */
    public final long st_dev; /*dev_t*/

    /** File serial number (inode). */
    public final long st_ino; /*ino_t*/

    /** Mode (permissions) of file. */
    public final int st_mode; /*mode_t*/

    /** Number of hard links to the file. */
    public final long st_nlink; /*nlink_t*/

    /** User ID of file. */
    public final int st_uid; /*uid_t*/

    /** Group ID of file. */
    public final int st_gid; /*gid_t*/

    /** Device ID (if file is character or block special). */
    public final long st_rdev; /*dev_t*/

    /**
     * For regular files, the file size in bytes.
     * For symbolic links, the length in bytes of the pathname contained in the symbolic link.
     * For a shared memory object, the length in bytes.
     * For a typed memory object, the length in bytes.
     * For other file types, the use of this field is unspecified.
     */
    public final long st_size; /*off_t*/

    /** Time of last access. */
    public final long st_atime; /*time_t*/

    /** Time of last data modification. */
    public final long st_mtime; /*time_t*/

    /** Time of last status change. */
    public final long st_ctime; /*time_t*/

    /**
     * A file system-specific preferred I/O block size for this object.
     * For some file system types, this may vary from file to file.
     */
    public final long st_blksize; /*blksize_t*/

    /** Number of blocks allocated for this object. */
    public final long st_blocks; /*blkcnt_t*/

    StructStat(long st_dev, long st_ino, int st_mode, long st_nlink, int st_uid, int st_gid,
            long st_rdev, long st_size, long st_atime, long st_mtime, long st_ctime,
            long st_blksize, long st_blocks) {
        this.st_dev = st_dev;
        this.st_ino = st_ino;
        this.st_mode = st_mode;
        this.st_nlink = st_nlink;
        this.st_uid = st_uid;
        this.st_gid = st_gid;
        this.st_rdev = st_rdev;
        this.st_size = st_size;
        this.st_atime = st_atime;
        this.st_mtime = st_mtime;
        this.st_ctime = st_ctime;
        this.st_blksize = st_blksize;
        this.st_blocks = st_blocks;
    }
}
