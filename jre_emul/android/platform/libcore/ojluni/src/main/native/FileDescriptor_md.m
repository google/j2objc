/*
 * Copyright (c) 1997, Oracle and/or its affiliates. All rights reserved.
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
#include <sys/socket.h>
#include <sys/types.h>

#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "TempFailureRetry.h"


// J2ObjC: unused.
/*******************************************************************/
/*  BEGIN JNI ********* BEGIN JNI *********** BEGIN JNI ************/
/*******************************************************************/

/* field id for jint 'fd' in java.io.FileDescriptor */
//jfieldID IO_fd_fdID;

/**************************************************************
 * static methods to store field ID's in initializers

static void FileDescriptor_initIDs(JNIEnv *env) {
    jclass fdClass = (*env)->FindClass(env, "java/io/FileDescriptor");
    IO_fd_fdID = (*env)->GetFieldID(env, fdClass, "descriptor", "I");
}
*/

/**************************************************************
 * File Descriptor

JNIEXPORT void JNICALL
FileDescriptor_sync(JNIEnv *env, jobject this) {
    int fd = (*env)->GetIntField(env, this, IO_fd_fdID);
    if (JVM_Sync(fd) == -1) {
        JNU_ThrowByName(env, "java/io/SyncFailedException", "sync failed");
    }
}
*/

JNIEXPORT jboolean JNICALL Java_java_io_FileDescriptor_isSocket(JNIEnv *env, jclass ignored, jint fd) {
    int error;
    socklen_t error_length = sizeof(error);
    return TEMP_FAILURE_RETRY(getsockopt(fd, SOL_SOCKET, SO_ERROR, &error, &error_length)) == 0;
}

/* J2ObjC unused.
static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(FileDescriptor, sync, "()V"),
  NATIVE_METHOD(FileDescriptor, isSocket, "(I)Z"),
};

void register_java_io_FileDescriptor(JNIEnv* env) {
    jniRegisterNativeMethods(env, "java/io/FileDescriptor", gMethods, NELEM(gMethods));

    FileDescriptor_initIDs(env);
}
*/
