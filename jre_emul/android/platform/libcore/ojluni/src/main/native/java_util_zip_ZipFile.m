/*
 * Copyright (c) 1998, 2010, Oracle and/or its affiliates. All rights reserved.
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

/*
 * Native method support for java.util.zip.ZipFile
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <ctype.h>
#include <assert.h>
//#include "JNIHelp.h"
#include "jlong.h"
#include "jvm.h"
#include "jni.h"
#include "jni_util.h"
#include "zip_util.h"
#ifdef WIN32
#include "io_util_md.h"
#else
#include "io_util.h"
#endif

#include "java_util_zip_ZipFile.h"

#include "IOSPrimitiveArray.h"
#include "NSString+JavaString.h"
#include "java/io/FileNotFoundException.h"
#include "java/lang/IllegalStateException.h"
#include "java/util/zip/ZipException.h"
#include "java/util/zip/ZipFile.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

#define DEFLATED 8
#define STORED 0

static int OPEN_READ = java_util_zip_ZipFile_OPEN_READ;
static int OPEN_DELETE = java_util_zip_ZipFile_OPEN_DELETE;

static void
ThrowZipException(JNIEnv *env, const char *msg)
{
    jstring s = NULL;
    jobject x;

    if (msg != NULL) {
        s = JNU_NewStringPlatform(env, msg);
    }
    @throw create_JavaUtilZipZipException_initWithNSString_(s);
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_ZipFile_open(JNIEnv *env, jclass cls, jstring name,
                                        jint mode, jlong lastModified,
                                        jboolean usemmap)
{
    const char *path = JNU_GetStringPlatformChars(env, name, 0);
    char *msg = 0;
    jlong result = 0;
    int flag = 0;
    jzfile *zip = 0;

    if (mode & OPEN_READ) flag |= O_RDONLY;
    // Android changed, JVM_O_DELETE/unlink is problematic, see b/28901232.
    //if (mode & OPEN_DELETE) flag |= JVM_O_DELETE;

    if (path != 0) {
        zip = ZIP_Get_From_Cache(path, &msg, lastModified);
        if (zip == 0 && msg == 0) {
            ZFILE zfd = 0;
#ifdef WIN32
            zfd = winFileHandleOpen(env, name, flag);
            if (zfd == -1) {
                /* Exception already pending. */
                goto finally;
            }
#else
            zfd = JVM_Open(path, flag, 0);
            if (zfd < 0) {
                throwFileNotFoundException(env, name);
                goto finally;
            }
#endif
            zip = ZIP_Put_In_Cache0(path, zfd, &msg, lastModified, usemmap);
        }

        if (zip != 0) {
            result = ptr_to_jlong(zip);
        } else if (msg != 0) {
            ThrowZipException(env, msg);
            free(msg);
        } else if (errno == ENOMEM) {
            JNU_ThrowOutOfMemoryError(env, 0);
        } else {
            ThrowZipException(env, "error in opening zip file");
        }
finally:
        JNU_ReleaseStringPlatformChars(env, name, path);
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_java_util_zip_ZipFile_getFileDescriptor(JNIEnv *env, jclass cls, jlong zfile)
{
    jzfile *zip = jlong_to_ptr(zfile);
    
    return zip->zfd;
}

JNIEXPORT jint JNICALL
Java_java_util_zip_ZipFile_getTotal(JNIEnv *env, jclass cls, jlong zfile)
{
    jzfile *zip = jlong_to_ptr(zfile);

    return zip->total;
}

JNIEXPORT jboolean JNICALL
Java_java_util_zip_ZipFile_startsWithLOC(JNIEnv *env, jclass cls, jlong zfile)
{
    jzfile *zip = jlong_to_ptr(zfile);

    return zip->locsig;
}

JNIEXPORT void JNICALL
Java_java_util_zip_ZipFile_close(JNIEnv *env, jclass cls, jlong zfile)
{
    ZIP_Close(jlong_to_ptr(zfile));
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_ZipFile_getEntry(JNIEnv *env, jclass cls, jlong zfile,
                 IOSByteArray *name, jboolean addSlash)
{
#define MAXNAME 1024
    jzfile *zip = jlong_to_ptr(zfile);
    jsize ulen = name->size_;
    char buf[MAXNAME+2], *path;
    jzentry *ze;

    if (ulen > MAXNAME) {
        path = malloc(ulen + 2);
        if (path == 0) {
            JNU_ThrowOutOfMemoryError(env, 0);
            return 0;
        }
    } else {
        path = buf;
    }
    (*env)->GetByteArrayRegion(env, name, 0, ulen, (jbyte *)path);
    path[ulen] = '\0';
    if (addSlash == JNI_FALSE) {
        ze = ZIP_GetEntry(zip, path, 0);
    } else {
        ze = ZIP_GetEntry(zip, path, (jint)ulen);
    }
    if (path != buf) {
        free(path);
    }
    return ptr_to_jlong(ze);
}

JNIEXPORT void JNICALL
Java_java_util_zip_ZipFile_freeEntry(JNIEnv *env, jclass cls, jlong zfile,
                                    jlong zentry)
{
    jzfile *zip = jlong_to_ptr(zfile);
    jzentry *ze = jlong_to_ptr(zentry);
    ZIP_FreeEntry(zip, ze);
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_ZipFile_getNextEntry(JNIEnv *env, jclass cls, jlong zfile,
                                        jint n)
{
    jzentry *ze = ZIP_GetNextEntry(jlong_to_ptr(zfile), n);
    return ptr_to_jlong(ze);
}

JNIEXPORT jint JNICALL
Java_java_util_zip_ZipFile_getEntryMethod(JNIEnv *env, jclass cls, jlong zentry)
{
    jzentry *ze = jlong_to_ptr(zentry);
    return ze->csize != 0 ? DEFLATED : STORED;
}

JNIEXPORT jint JNICALL
Java_java_util_zip_ZipFile_getEntryFlag(JNIEnv *env, jclass cls, jlong zentry)
{
    jzentry *ze = jlong_to_ptr(zentry);
    return ze->flag;
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_ZipFile_getEntryCSize(JNIEnv *env, jclass cls, jlong zentry)
{
    jzentry *ze = jlong_to_ptr(zentry);
    return ze->csize != 0 ? ze->csize : ze->size;
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_ZipFile_getEntrySize(JNIEnv *env, jclass cls, jlong zentry)
{
    jzentry *ze = jlong_to_ptr(zentry);
    return ze->size;
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_ZipFile_getEntryTime(JNIEnv *env, jclass cls, jlong zentry)
{
    jzentry *ze = jlong_to_ptr(zentry);
    return (jlong)ze->time & 0xffffffffUL;
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_ZipFile_getEntryCrc(JNIEnv *env, jclass cls, jlong zentry)
{
    jzentry *ze = jlong_to_ptr(zentry);
    return (jlong)ze->crc & 0xffffffffUL;
}

JNIEXPORT jbyteArray JNICALL
Java_java_util_zip_ZipFile_getCommentBytes(JNIEnv *env, jclass cls, jlong zfile)
{
    jzfile *zip = jlong_to_ptr(zfile);
    jbyteArray jba = NULL;

    if (zip->comment != NULL) {
        if ((jba = (*env)->NewByteArray(env, zip->clen)) == NULL)
            return NULL;
        (*env)->SetByteArrayRegion(env, jba, 0, zip->clen, (jbyte*)zip->comment);
    }
    return jba;
}

JNIEXPORT jbyteArray JNICALL
Java_java_util_zip_ZipFile_getEntryBytes(JNIEnv *env, jclass cls, jlong zentry, jint type)
{
    jzentry *ze = jlong_to_ptr(zentry);
    int len = 0;
    jbyteArray jba = NULL;
    switch (type) {
    case java_util_zip_ZipFile_JZENTRY_NAME:
        if (ze->name != 0) {
            len = (int)strlen(ze->name);
            if (len == 0 || (jba = (*env)->NewByteArray(env, len)) == NULL)
                break;
            (*env)->SetByteArrayRegion(env, jba, 0, len, (jbyte *)ze->name);
        }
        break;
    case java_util_zip_ZipFile_JZENTRY_EXTRA:
        if (ze->extra != 0) {
            unsigned char *bp = (unsigned char *)&ze->extra[0];
            len = (bp[0] | (bp[1] << 8));
            if (len <= 0 || (jba = (*env)->NewByteArray(env, len)) == NULL)
                break;
            (*env)->SetByteArrayRegion(env, jba, 0, len, &ze->extra[2]);
        }
        break;
    case java_util_zip_ZipFile_JZENTRY_COMMENT:
        if (ze->comment != 0) {
            len = (int)strlen(ze->comment);
            if (len == 0 || (jba = (*env)->NewByteArray(env, len)) == NULL)
                break;
            (*env)->SetByteArrayRegion(env, jba, 0, len, (jbyte*)ze->comment);
        }
        break;
    }
    return jba;
}

JNIEXPORT jint JNICALL
Java_java_util_zip_ZipFile_read(JNIEnv *env, jclass cls, jlong zfile,
             jlong zentry, jlong pos, jbyteArray bytes,
             jint off, jint len)
{
    jzfile *zip = jlong_to_ptr(zfile);
    char *msg;

    // BEGIN Android-changed: Removed tmp stack buffer.
    long long length = (long long)(*env)->GetArrayLength(env, bytes);
    if (off < 0 || len < 0 || off + len > length) {
        char errmsg[128];
        snprintf(errmsg, sizeof(errmsg), "len: %d, off: %d are not valid for array sized %lld\n",
                 len, off, length);
        JNU_ThrowArrayIndexOutOfBoundsException(env, errmsg);
        return -1;
    }

    jbyte *buf = (*env)->GetByteArrayElements(env, bytes, NULL);
    ZIP_Lock(zip);
    len = ZIP_Read(zip, jlong_to_ptr(zentry), pos, buf + off, len);
    msg = zip->msg;
    ZIP_Unlock(zip);
    (*env)->ReleaseByteArrayElements(env, bytes, buf, 0);


    if (len == -1) {
        if (msg != 0) {
            ThrowZipException(env, msg);
        } else {
            char errmsg[128];
            snprintf(errmsg, sizeof(errmsg), "errno: %d, error: %s\n", errno,
                     "Error reading ZIP file");
            JNU_ThrowIOExceptionWithLastError(env, errmsg);
        }
    }

    return len;
}

JNIEXPORT jstring JNICALL
Java_java_util_zip_ZipFile_getZipMessage(JNIEnv *env, jclass cls, jlong zfile)
{
    jzfile *zip = jlong_to_ptr(zfile);
    char *msg = zip->msg;
    if (msg == NULL) {
        return NULL;
    }
    return JNU_NewStringPlatform(env, msg);
}

JNIEXPORT jobjectArray JNICALL
Java_java_util_jar_JarFile_getMetaInfEntryNames(JNIEnv *env, JavaUtilZipZipFile *obj)
{
    jlong zfile = obj->jzfile_;
    jzfile *zip;
    int i, count;
    jobjectArray result = 0;

    if (zfile == 0) {
        @throw create_JavaLangIllegalStateException_initWithNSString_(@"zip file closed");
        return NULL;
    }
    zip = jlong_to_ptr(zfile);

    /* count the number of valid ZIP metanames */
    count = 0;
    if (zip->metanames != 0) {
        for (i = 0; i < zip->metacount; i++) {
            if (zip->metanames[i] != 0) {
                count++;
            }
        }
    }

    /* If some names were found then build array of java strings */
    if (count > 0) {
        result = [IOSObjectArray arrayWithLength:count type:NSString_class_()];
        if (result != 0) {
            for (i = 0; i < count; i++) {
                IOSObjectArray_Set(result, i, [NSString stringWithUTF8String:zip->metanames[i]]);
            }
        }
    }
    return result;
}
