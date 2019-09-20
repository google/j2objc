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
 * File information returned by fstatvfs(2) and statvfs(2).
 */
public final class StructStatVfs {
  /** File system block size (used for block counts). */
  public final long f_bsize; /*unsigned long*/

  /** Fundamental file system block size. */
  public final long f_frsize; /*unsigned long*/

  /** Total block count. */
  public final long f_blocks; /*fsblkcnt_t*/

  /** Free block count. */
  public final long f_bfree; /*fsblkcnt_t*/

  /** Free block count available to non-root. */
  public final long f_bavail; /*fsblkcnt_t*/

  /** Total file (inode) count. */
  public final long f_files; /*fsfilcnt_t*/

  /** Free file (inode) count. */
  public final long f_ffree; /*fsfilcnt_t*/

  /** Free file (inode) count available to non-root. */
  public final long f_favail; /*fsfilcnt_t*/

  /** File system id. */
  public final long f_fsid; /*unsigned long*/

  /** Bit mask of ST_* flags. */
  public final long f_flag; /*unsigned long*/

  /** Maximum filename length. */
  public final long f_namemax; /*unsigned long*/

  StructStatVfs(long f_bsize, long f_frsize, long f_blocks, long f_bfree, long f_bavail,
                long f_files, long f_ffree, long f_favail,
                long f_fsid, long f_flag, long f_namemax) {
    this.f_bsize = f_bsize;
    this.f_frsize = f_frsize;
    this.f_blocks = f_blocks;
    this.f_bfree = f_bfree;
    this.f_bavail = f_bavail;
    this.f_files = f_files;
    this.f_ffree = f_ffree;
    this.f_favail = f_favail;
    this.f_fsid = f_fsid;
    this.f_flag = f_flag;
    this.f_namemax = f_namemax;
  }
}
