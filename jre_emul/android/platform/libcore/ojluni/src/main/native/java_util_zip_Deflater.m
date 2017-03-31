/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
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
 * Native method support for java.util.zip.Deflater
 */

#include <stdio.h>
#include <stdlib.h>
#include "jlong.h"
#include "jni.h"
#include "jni_util.h"
#include <zlib.h>

#include "IOSPrimitiveArray.h"
#include "java/util/zip/Deflater.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

#define DEF_MEM_LEVEL 8

JNIEXPORT jlong JNICALL
Java_java_util_zip_Deflater_init(JNIEnv *env, jclass cls, jint level,
                                 jint strategy, jboolean nowrap)
{
    z_stream *strm = calloc(1, sizeof(z_stream));

    if (strm == 0) {
        JNU_ThrowOutOfMemoryError(env, 0);
        return jlong_zero;
    } else {
        char *msg;
        switch (deflateInit2(strm, level, Z_DEFLATED,
                             nowrap ? -MAX_WBITS : MAX_WBITS,
                             DEF_MEM_LEVEL, strategy)) {
          case Z_OK:
            return ptr_to_jlong(strm);
          case Z_MEM_ERROR:
            free(strm);
            JNU_ThrowOutOfMemoryError(env, 0);
            return jlong_zero;
          case Z_STREAM_ERROR:
            free(strm);
            JNU_ThrowIllegalArgumentException(env, 0);
            return jlong_zero;
          default:
            msg = strm->msg;
            free(strm);
            JNU_ThrowInternalError(env, msg);
            return jlong_zero;
        }
    }
}

JNIEXPORT void JNICALL
Java_java_util_zip_Deflater_setDictionary(JNIEnv *env, jclass cls, jlong addr,
                                          IOSByteArray *b, jint off, jint len)
{
    Bytef *buf = (Bytef *)b->buffer_;
    int res;
    if (buf == 0) {/* out of memory */
        return;
    }
    res = deflateSetDictionary((z_stream *)jlong_to_ptr(addr), buf + off, len);
    switch (res) {
    case Z_OK:
        break;
    case Z_STREAM_ERROR:
        JNU_ThrowIllegalArgumentException(env, 0);
        break;
    default:
        JNU_ThrowInternalError(env, ((z_stream *)jlong_to_ptr(addr))->msg);
        break;
    }
}

JNIEXPORT jint JNICALL
Java_java_util_zip_Deflater_deflateBytes(JNIEnv *env, JavaUtilZipDeflater *this, jlong addr,
                                         IOSByteArray *b, jint off, jint len, jint flush)
{
    z_stream *strm = jlong_to_ptr(addr);

    IOSByteArray *this_buf = this->buf_;
    jint this_off = this->off_;
    jint this_len = this->len_;
    jbyte *in_buf;
    jbyte *out_buf;
    int res;
    if (this->setParams_) {
        int level = this->level_;
        int strategy = this->strategy_;
        in_buf = this_buf->buffer_;
        if (in_buf == NULL) {
            // Throw OOME only when length is not zero
            if (this_len != 0)
                JNU_ThrowOutOfMemoryError(env, 0);
            return 0;
        }
        out_buf = b->buffer_;
        if (out_buf == NULL) {
            if (len != 0)
                JNU_ThrowOutOfMemoryError(env, 0);
            return 0;
        }

        strm->next_in = (Bytef *) (in_buf + this_off);
        strm->next_out = (Bytef *) (out_buf + off);
        strm->avail_in = this_len;
        strm->avail_out = len;
        res = deflateParams(strm, level, strategy);

        switch (res) {
        case Z_OK:
            this->setParams_ = JNI_FALSE;
            this_off += this_len - strm->avail_in;
            this->off_ = this_off;
            this->len_ = strm->avail_in;
            return len - strm->avail_out;
        case Z_BUF_ERROR:
            this->setParams_ = JNI_FALSE;
            return 0;
        default:
            JNU_ThrowInternalError(env, strm->msg);
            return 0;
        }
    } else {
        jboolean finish = this->finish_;
        in_buf = this_buf->buffer_;
        if (in_buf == NULL) {
            if (this_len != 0)
                JNU_ThrowOutOfMemoryError(env, 0);
            return 0;
        }
        out_buf = b->buffer_;
        if (out_buf == NULL) {
            if (len != 0)
                JNU_ThrowOutOfMemoryError(env, 0);

            return 0;
        }

        strm->next_in = (Bytef *) (in_buf + this_off);
        strm->next_out = (Bytef *) (out_buf + off);
        strm->avail_in = this_len;
        strm->avail_out = len;
        res = deflate(strm, finish ? Z_FINISH : flush);

        switch (res) {
        case Z_STREAM_END:
            this->finished_ = JNI_TRUE;
            /* fall through */
        case Z_OK:
            this_off += this_len - strm->avail_in;
            this->off_ = this_off;
            this->len_ = strm->avail_in;
            return len - strm->avail_out;
        case Z_BUF_ERROR:
            return 0;
            default:
            JNU_ThrowInternalError(env, strm->msg);
            return 0;
        }
    }
}

JNIEXPORT jint JNICALL
Java_java_util_zip_Deflater_getAdler(JNIEnv *env, jclass cls, jlong addr)
{
    return ((z_stream *)jlong_to_ptr(addr))->adler & 0xffffffff;
}

JNIEXPORT void JNICALL
Java_java_util_zip_Deflater_reset(JNIEnv *env, jclass cls, jlong addr)
{
    if (deflateReset((z_stream *)jlong_to_ptr(addr)) != Z_OK) {
        JNU_ThrowInternalError(env, 0);
    }
}

JNIEXPORT void JNICALL
Java_java_util_zip_Deflater_end(JNIEnv *env, jclass cls, jlong addr)
{
    if (deflateEnd((z_stream *)jlong_to_ptr(addr)) == Z_STREAM_ERROR) {
        JNU_ThrowInternalError(env, 0);
    } else {
        free((z_stream *)jlong_to_ptr(addr));
    }
}
