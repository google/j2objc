/*
 * Copyright (c) 1994, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#include <stdlib.h>
#include <string.h>
#include <stddef.h>
#include <stdio.h>

#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "io_util.h"
#include "io_util_md.h"

#include "java/io/FileNotFoundException.h"

// BEGIN Android-added: Fuchsia: Alias *64 functions on Fuchsia. http://b/119496969
#if defined(_ALLBSD_SOURCE) || defined(__Fuchsia__)
#define stat64 stat
#define fstat64 fstat
#define open64 open
#endif
// END Android-added: Fuchsia: Alias *64 functions on Fuchsia. http://b/119496969

/* IO helper functions */

// J2ObjC: unused.
//jint
//readSingle(JNIEnv *env, jobject this, jfieldID fid) {
//    jint nread;
//    char ret;
//    FD fd = GET_FD(this, fid);
//    if (fd == -1) {
//        JNU_ThrowIOException(env, "Stream Closed");
//        return -1;
//    }
//    nread = (jint)IO_Read(fd, &ret, 1);
//    if (nread == 0) { /* EOF */
//        return -1;
//    } else if (nread == JVM_IO_ERR) { /* error */
//        JNU_ThrowIOExceptionWithLastError(env, "Read error");
//    } else if (nread == JVM_IO_INTR) {
//        JNU_ThrowByName(env, "java/io/InterruptedIOException", NULL);
//    }
//    return ret & 0xFF;
//}

/* The maximum size of a stack-allocated buffer.
 */
#define BUF_SIZE 8192

/*
 * Returns true if the array slice defined by the given offset and length
 * is out of bounds.
 */
/* J2ObjC: unused.
static int
outOfBounds(JNIEnv *env, jint off, jint len, jbyteArray array) {
    return ((off < 0) ||
            (len < 0) ||
            // We are very careful to avoid signed integer overflow,
            // the result of which is undefined in C.
            ((*env)->GetArrayLength(env, array) - off < len));
}*/

// J2ObjC: unused.
//jint
//readBytes(JNIEnv *env, jobject this, jbyteArray bytes,
//          jint off, jint len, jfieldID fid)
//{
//    jint nread;
//    char stackBuf[BUF_SIZE];
//    char *buf = NULL;
//    FD fd;
//
//    if (IS_NULL(bytes)) {
//        JNU_ThrowNullPointerException(env, NULL);
//        return -1;
//    }
//
//    if (outOfBounds(env, off, len, bytes)) {
//        JNU_ThrowByName(env, "java/lang/IndexOutOfBoundsException", NULL);
//        return -1;
//    }
//
//    if (len == 0) {
//        return 0;
//    } else if (len > BUF_SIZE) {
//        buf = malloc(len);
//        if (buf == NULL) {
//            JNU_ThrowOutOfMemoryError(env, NULL);
//            return 0;
//        }
//    } else {
//        buf = stackBuf;
//    }
//
//    fd = GET_FD(this, fid);
//    if (fd == -1) {
//        JNU_ThrowIOException(env, "Stream Closed");
//        nread = -1;
//    } else {
//        nread = (jint)IO_Read(fd, buf, len);
//        if (nread > 0) {
//            (*env)->SetByteArrayRegion(env, bytes, off, nread, (jbyte *)buf);
//        } else if (nread == JVM_IO_ERR) {
//            JNU_ThrowIOExceptionWithLastError(env, "Read error");
//        } else if (nread == JVM_IO_INTR) {
//            JNU_ThrowByName(env, "java/io/InterruptedIOException", NULL);
//        } else { /* EOF */
//            nread = -1;
//        }
//    }
//
//    if (buf != stackBuf) {
//        free(buf);
//    }
//    return nread;
//}

/* J2ObjC: unused.
void
writeSingle(JNIEnv *env, jobject this, jint byte, jboolean append, jfieldID fid) {
    // Discard the 24 high-order bits of byte. See OutputStream#write(int)
    char c = (char) byte;
    jint n;
    FD fd = GET_FD(this, fid);
    if (fd == -1) {
        JNU_ThrowIOException(env, "Stream Closed");
        return;
    }
    if (append == JNI_TRUE) {
        n = (jint)IO_Append(fd, &c, 1);
    } else {
        n = (jint)IO_Write(fd, &c, 1);
    }
    if (n == JVM_IO_ERR) {
        JNU_ThrowIOExceptionWithLastError(env, "Write error");
    } else if (n == JVM_IO_INTR) {
        JNU_ThrowByName(env, "java/io/InterruptedIOException", NULL);
    }
}*/

/* J2ObjC: unused.
void
writeBytes(JNIEnv *env, jobject this, jbyteArray bytes,
           jint off, jint len, jboolean append, jfieldID fid)
{
    jint n;
    char stackBuf[BUF_SIZE];
    char *buf = NULL;
    FD fd;

    if (IS_NULL(bytes)) {
        JNU_ThrowNullPointerException(env, NULL);
        return;
    }

    if (outOfBounds(env, off, len, bytes)) {
        JNU_ThrowByName(env, "java/lang/IndexOutOfBoundsException", NULL);
        return;
    }

    if (len == 0) {
        return;
    } else if (len > BUF_SIZE) {
        buf = malloc(len);
        if (buf == NULL) {
            JNU_ThrowOutOfMemoryError(env, NULL);
            return;
        }
    } else {
        buf = stackBuf;
    }

    (*env)->GetByteArrayRegion(env, bytes, off, len, (jbyte *)buf);

    if (!(*env)->ExceptionOccurred(env)) {
        off = 0;
        while (len > 0) {
            fd = GET_FD(this, fid);
            if (fd == -1) {
                JNU_ThrowIOException(env, "Stream Closed");
                break;
            }
            if (append == JNI_TRUE) {
                n = (jint)IO_Append(fd, buf+off, len);
            } else {
                n = (jint)IO_Write(fd, buf+off, len);
            }
            if (n == JVM_IO_ERR) {
                JNU_ThrowIOExceptionWithLastError(env, "Write error");
                break;
            } else if (n == JVM_IO_INTR) {
                JNU_ThrowByName(env, "java/io/InterruptedIOException", NULL);
                break;
            }
            off += n;
            len -= n;
        }
    }
    if (buf != stackBuf) {
        free(buf);
    }
}*/

void
throwFileNotFoundException(JNIEnv *env, jstring path)
{
    char buf[256];
    jint n;
    jobject x;
    jstring why = NULL;

    n = JVM_GetLastErrorString(buf, sizeof(buf));
    if (n > 0) {
        why = JNU_NewStringPlatform(env, buf);
    }
    /* J2ObjC: Call the constructor directly.
    x = JNU_NewObjectByName(env,
                            "java/io/FileNotFoundException",
                            "(Ljava/lang/String;Ljava/lang/String;)V",
                            path, why);
    if (x != NULL) {
        (*env)->Throw(env, x);
    }*/
    @throw create_JavaIoFileNotFoundException_initWithNSString_withNSString_(path, why);
}

// From android/platform/libcore/ojluni/src/main/native/io_util_md.c

FD
handleOpen(const char *path, int oflag, int mode) {
    FD fd;
    RESTARTABLE(open64(path, oflag, mode), fd);
    if (fd != -1) {
        struct stat64 buf64;
        int result;
        RESTARTABLE(fstat64(fd, &buf64), result);
        if (result != -1) {
            if (S_ISDIR(buf64.st_mode)) {
                close(fd);
                errno = EISDIR;
                fd = -1;
            }
        } else {
            close(fd);
            fd = -1;
        }
    }
    return fd;
}

