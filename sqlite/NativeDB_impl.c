/*

#include "IOSObjectArray.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "java/io/UnsupportedEncodingException.h"
#include "java/lang/RuntimeException.h"
#include "java/lang/System.h"
#include "java/sql/SQLException.h"
#include "org/sqlite/BusyHandler.h"
#include "org/sqlite/Function.h"
#include "org/sqlite/ProgressHandler.h"
#include "org/sqlite/SQLiteConfig.h"
#include "org/sqlite/SQLiteJDBCLoader.h"
#include "org/sqlite/core/DB.h"
#include "org/sqlite/core/NativeDB.h"

#import <sqlite3.h>
JNIEXPORT void Java_org_sqlite_core_NativeDB__1open_1utf8(JNIEnv *_env_, jobject self, jarray fileUtf8, jint openFlags) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
};

JNIEXPORT void Java_org_sqlite_core_NativeDB__1close(JNIEnv *_env_, jobject self) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB__1exec_1utf8(JNIEnv *_env_, jobject self, jarray sqlUtf8) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_shared_1cache(JNIEnv *_env_, jobject self, jboolean enable) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_enable_1load_1extension(JNIEnv *_env_, jobject self, jboolean enable) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_interrupt(JNIEnv *_env_, jobject self) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_busy_1timeout(JNIEnv *_env_, jobject self, jint ms) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_busy_1handler(JNIEnv *_env_, jobject self, jobject busyHandler) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT jlong Java_org_sqlite_core_NativeDB_prepare_1utf8(JNIEnv *_env_, jobject self, jarray sqlUtf8) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jarray Java_org_sqlite_core_NativeDB_errmsg_1utf8(JNIEnv *_env_, jobject self) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jarray Java_org_sqlite_core_NativeDB_libversion_1utf8(JNIEnv *_env_, jobject self) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_changes(JNIEnv *_env_, jobject self) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_total_1changes(JNIEnv *_env_, jobject self) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_finalize(JNIEnv *_env_, jobject self, jlong stmt) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_step(JNIEnv *_env_, jobject self, jlong stmt) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_reset(JNIEnv *_env_, jobject self, jlong stmt) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_clear_1bindings(JNIEnv *_env_, jobject self, jlong stmt) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_bind_1parameter_1count(JNIEnv *_env_, jobject self, jlong stmt) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_column_1count(JNIEnv *_env_, jobject self, jlong stmt) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_column_1type(JNIEnv *_env_, jobject self, jlong stmt, jint col) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jarray Java_org_sqlite_core_NativeDB_column_1decltype_1utf8(JNIEnv *_env_, jobject self, jlong stmt, jint col) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jarray Java_org_sqlite_core_NativeDB_column_1table_1name_1utf8(JNIEnv *_env_, jobject self, jlong stmt, jint col) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jarray Java_org_sqlite_core_NativeDB_column_1name_1utf8(JNIEnv *_env_, jobject self, jlong stmt, jint col) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jarray Java_org_sqlite_core_NativeDB_column_1text_1utf8(JNIEnv *_env_, jobject self, jlong stmt, jint col) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jarray Java_org_sqlite_core_NativeDB_column_1blob(JNIEnv *_env_, jobject self, jlong stmt, jint col) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jdouble Java_org_sqlite_core_NativeDB_column_1double(JNIEnv *_env_, jobject self, jlong stmt, jint col) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jlong Java_org_sqlite_core_NativeDB_column_1long(JNIEnv *_env_, jobject self, jlong stmt, jint col) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_column_1int(JNIEnv *_env_, jobject self, jlong stmt, jint col) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_bind_1null(JNIEnv *_env_, jobject self, jlong stmt, jint pos) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_bind_1int(JNIEnv *_env_, jobject self, jlong stmt, jint pos, jint v) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_bind_1long(JNIEnv *_env_, jobject self, jlong stmt, jint pos, jlong v) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_bind_1double(JNIEnv *_env_, jobject self, jlong stmt, jint pos, jdouble v) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_bind_1text_1utf8(JNIEnv *_env_, jobject self, jlong stmt, jint pos, jarray vUtf8) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_bind_1blob(JNIEnv *_env_, jobject self, jlong stmt, jint pos, jarray v) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_result_1null(JNIEnv *_env_, jobject self, jlong context) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_result_1text_1utf8(JNIEnv *_env_, jobject self, jlong context, jarray valUtf8) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_result_1blob(JNIEnv *_env_, jobject self, jlong context, jarray val) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_result_1double(JNIEnv *_env_, jobject self, jlong context, jdouble val) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_result_1long(JNIEnv *_env_, jobject self, jlong context, jlong val) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_result_1int(JNIEnv *_env_, jobject self, jlong context, jint val) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT void Java_org_sqlite_core_NativeDB_result_1error_1utf8(JNIEnv *_env_, jobject self, jlong context, jarray errUtf8) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT jarray Java_org_sqlite_core_NativeDB_value_1text_1utf8(JNIEnv *_env_, jobject self, jobject f, jint argUtf8) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jarray Java_org_sqlite_core_NativeDB_value_1blob(JNIEnv *_env_, jobject self, jobject f, jint arg) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jdouble Java_org_sqlite_core_NativeDB_value_1double(JNIEnv *_env_, jobject self, jobject f, jint arg) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jlong Java_org_sqlite_core_NativeDB_value_1long(JNIEnv *_env_, jobject self, jobject f, jint arg) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_value_1int(JNIEnv *_env_, jobject self, jobject f, jint arg) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_value_1type(JNIEnv *_env_, jobject self, jobject f, jint arg) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_create_1function_1utf8(JNIEnv *_env_, jobject self, jarray nameUtf8, jobject func, jint flags) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_destroy_1function_1utf8(JNIEnv *_env_, jobject self, jarray nameUtf8) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT void Java_org_sqlite_core_NativeDB_free_1functions(JNIEnv *_env_, jobject self) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

JNIEXPORT jint Java_org_sqlite_core_NativeDB_backup(JNIEnv *_env_, jobject self, jarray dbNameUtf8, jarray destFileNameUtf8, jobject observer) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jint Java_org_sqlite_core_NativeDB_restore(JNIEnv *_env_, jobject self, jarray dbNameUtf8, jarray sourceFileName, jobject observer) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT jarray Java_org_sqlite_core_NativeDB_column_1metadata(JNIEnv *_env_, jobject self, jlong stmt) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}


JNIEXPORT void Java_org_sqlite_core_NativeDB_register_1progress_1handler(JNIEnv *_env_, jobject self, jint vmCalls, jobject progressHandler) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}


JNIEXPORT void Java_org_sqlite_core_NativeDB_clear_1progress_1handler(JNIEnv *_env_, jobject self) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
}

*/


#include <stdlib.h>
#include <string.h>
#include <assert.h>
//#include "NativeDB.h"
#include "sqlite3.h"
#include "jni.h"

static jclass dbclass = 0;
static jclass  fclass = 0;
static jclass  aclass = 0;
static jclass  wclass = 0;
static jclass pclass = 0;
static jclass phandleclass = 0;

static void * toref(jlong value)
{
    jvalue ret;
    ret.j = value;
    return (void *) ret.l;
}

static jlong fromref(void * value)
{
    jvalue ret;
    ret.l = value;
    return ret.j;
}

static void throwex(JNIEnv *env, jobject this)
{
    static jmethodID mth_throwex = 0;

    if (!mth_throwex)
        mth_throwex = (*env)->GetMethodID(env, dbclass, "throwex", "()V");

    (*env)->CallVoidMethod(env, this, mth_throwex);
}

static void throwex_errorcode(JNIEnv *env, jobject this, int errorCode)
{
    static jmethodID mth_throwex = 0;

    if (!mth_throwex)
        mth_throwex = (*env)->GetMethodID(env, dbclass, "throwex", "(I)V");

    (*env)->CallVoidMethod(env, this, mth_throwex, (jint) errorCode);
}

static void throwex_msg(JNIEnv *env, const char *str)
{
    static jmethodID mth_throwexmsg = 0;

    if (!mth_throwexmsg) mth_throwexmsg = (*env)->GetStaticMethodID(
            env, dbclass, "throwex", "(Ljava/lang/String;)V");

    (*env)->CallStaticVoidMethod(env, dbclass, mth_throwexmsg,
                                (*env)->NewStringUTF(env, str));
}

static void throwex_outofmemory(JNIEnv *env)
{
    throwex_msg(env, "Out of memory");
}

static void throwex_stmt_finalized(JNIEnv *env)
{
    throwex_msg(env, "The prepared statement has been finalized");
}

static void throwex_db_closed(JNIEnv *env)
{
    throwex_msg(env, "The database has been closed");
}

static jbyteArray utf8BytesToJavaByteArray(JNIEnv *env, const char* bytes, int nbytes)
{
    jbyteArray result;

    if (!bytes)
    {
        return NULL;
    }

    result = (*env)->NewByteArray(env, (jsize) nbytes);
    if (!result)
    {
        throwex_outofmemory(env);
        return NULL;
    }

    (*env)->SetByteArrayRegion(env, result, (jsize) 0, (jsize) nbytes, (const jbyte*) bytes);
 
    return result;
}

static void utf8JavaByteArrayToUtf8Bytes(JNIEnv *env, jbyteArray utf8bytes, char** bytes, int* nbytes)
{
    jsize utf8bytes_length;
    char* buf;

    *bytes = NULL;
    if (nbytes) *nbytes = 0;

    if (!utf8bytes)
    {
        return;
    }

    utf8bytes_length = (*env)->GetArrayLength(env, (jarray) utf8bytes);

    buf = (char*) malloc(utf8bytes_length + 1);
    if (!buf)
    {
        throwex_outofmemory(env);
        return;
    }

    (*env)->GetByteArrayRegion(env, utf8bytes, 0, utf8bytes_length, (jbyte*)buf);

    buf[utf8bytes_length] = '\0';

    *bytes = buf;
    if (nbytes) *nbytes = (int) utf8bytes_length;
}

static jbyteArray stringToUtf8ByteArray(JNIEnv *env, jstring str)
{
    static jmethodID mth_stringToUtf8ByteArray = 0;

    jobject result;

    if (!mth_stringToUtf8ByteArray) mth_stringToUtf8ByteArray = (*env)->GetStaticMethodID(
            env, dbclass, "stringToUtf8ByteArray", "(Ljava/lang/String;)[B");

    result = (*env)->CallStaticObjectMethod(env, dbclass, mth_stringToUtf8ByteArray, str);

    return (jbyteArray) result;
}

static void stringToUtf8Bytes(JNIEnv *env, jstring str, char** bytes, int* nbytes)
{
    jbyteArray utf8bytes;
    jsize utf8bytes_length;
    char* buf;

    *bytes = NULL;
    if (nbytes) *nbytes = 0;

    if (!str)
    {
        return;
    }

    utf8bytes = stringToUtf8ByteArray(env, str);
    if (!utf8bytes)
     {
        return;
    }

    utf8bytes_length = (*env)->GetArrayLength(env, (jarray) utf8bytes);

    buf = (char*) malloc(utf8bytes_length + 1);
    if (!buf)
    {
        throwex_outofmemory(env);
        return;
    }

    (*env)->GetByteArrayRegion(env, utf8bytes, 0, utf8bytes_length, (jbyte*)buf);

    buf[utf8bytes_length] = '\0';

    *bytes = buf;
    if (nbytes) *nbytes = (int) utf8bytes_length;
}

static void freeUtf8Bytes(char* bytes)
{
    if (bytes)
    {
        free(bytes);
    }
}

static sqlite3 * gethandle(JNIEnv *env, jobject this)
{
    static jfieldID pointer = 0;
    if (!pointer) pointer = (*env)->GetFieldID(env, dbclass, "pointer", "J");

    return (sqlite3 *)toref((*env)->GetLongField(env, this, pointer));
}

static void sethandle(JNIEnv *env, jobject this, sqlite3 * ref)
{
    static jfieldID pointer = 0;
    if (!pointer) pointer = (*env)->GetFieldID(env, dbclass, "pointer", "J");

    (*env)->SetLongField(env, this, pointer, fromref(ref));
}


// User Defined Function SUPPORT ////////////////////////////////////

struct UDFData {
    JavaVM *vm;
    jobject func;
    struct UDFData *next;  // linked list of all UDFData instances
};

/* Returns the sqlite3_value for the given arg of the given function.
 * If 0 is returned, an exception has been thrown to report the reason. */
static sqlite3_value * tovalue(JNIEnv *env, jobject function, jint arg)
{
    jlong value_pntr = 0;
    jint numArgs = 0;
    static jfieldID func_value = 0,
                    func_args = 0;

    if (!func_value || !func_args) {
        func_value = (*env)->GetFieldID(env, fclass, "value", "J");
        func_args  = (*env)->GetFieldID(env, fclass, "args", "I");
    }

    // check we have any business being here
    if (arg  < 0) { throwex_msg(env, "negative arg out of range"); return 0; }
    if (!function) { throwex_msg(env, "inconstent function"); return 0; }

    value_pntr = (*env)->GetLongField(env, function, func_value);
    numArgs = (*env)->GetIntField(env, function, func_args);

    if (value_pntr == 0) { throwex_msg(env, "no current value"); return 0; }
    if (arg >= numArgs) { throwex_msg(env, "arg out of range"); return 0; }

    return ((sqlite3_value**)toref(value_pntr))[arg];
}

/* called if an exception occured processing xFunc */
static void xFunc_error(sqlite3_context *context, JNIEnv *env)
{
    jstring msg = 0;
    char *msg_bytes;
    int msg_nbytes;

    jclass exclass = 0;
    static jmethodID exp_msg = 0;
    jthrowable ex = (*env)->ExceptionOccurred(env);

    (*env)->ExceptionClear(env);

    if (!exp_msg) {
        exclass = (*env)->FindClass(env, "java/lang/Throwable");
        exp_msg = (*env)->GetMethodID(
                env, exclass, "toString", "()Ljava/lang/String;");
    }

    msg = (jstring)(*env)->CallObjectMethod(env, ex, exp_msg);
    if (!msg) { sqlite3_result_error(context, "unknown error", 13); return; }

    stringToUtf8Bytes(env, msg, &msg_bytes, &msg_nbytes);
    if (!msg_bytes) { sqlite3_result_error_nomem(context); return; }

    sqlite3_result_error(context, msg_bytes, msg_nbytes);
    freeUtf8Bytes(msg_bytes);
}

/* used to call xFunc, xStep and xFinal */
static void xCall(
    sqlite3_context *context,
    int args,
    sqlite3_value** value,
    jobject func,
    jmethodID method)
{
    static jfieldID fld_context = 0,
                     fld_value = 0,
                     fld_args = 0;
    JNIEnv *env = 0;
    struct UDFData *udf = 0;

    udf = (struct UDFData*)sqlite3_user_data(context);
    assert(udf);
    (*udf->vm)->AttachCurrentThread(udf->vm, (void **)&env, 0);
    if (!func) func = udf->func;

    if (!fld_context || !fld_value || !fld_args) {
        fld_context = (*env)->GetFieldID(env, fclass, "context", "J");
        fld_value   = (*env)->GetFieldID(env, fclass, "value", "J");
        fld_args    = (*env)->GetFieldID(env, fclass, "args", "I");
    }

    (*env)->SetLongField(env, func, fld_context, fromref(context));
    (*env)->SetLongField(env, func, fld_value, value ? fromref(value) : 0);
    (*env)->SetIntField(env, func, fld_args, args);

    (*env)->CallVoidMethod(env, func, method);

    // check if xFunc threw an Exception
    if ((*env)->ExceptionCheck(env)) {
        xFunc_error(context, env);
    }

    (*env)->SetLongField(env, func, fld_context, 0);
    (*env)->SetLongField(env, func, fld_value, 0);
    (*env)->SetIntField(env, func, fld_args, 0);
}


void xFunc(sqlite3_context *context, int args, sqlite3_value** value)
{
    static jmethodID mth = 0;
    if (!mth) {
        JNIEnv *env;
        struct UDFData *udf = (struct UDFData*)sqlite3_user_data(context);
        (*udf->vm)->AttachCurrentThread(udf->vm, (void **)&env, 0);
        mth = (*env)->GetMethodID(env, fclass, "xFunc", "()V");
    }
    xCall(context, args, value, 0, mth);
}

void xStep(sqlite3_context *context, int args, sqlite3_value** value)
{
    JNIEnv *env;
    struct UDFData *udf;
    jobject *func = 0;
    static jmethodID mth = 0;
    static jmethodID clone = 0;

    if (!mth || !clone) {
        udf = (struct UDFData*)sqlite3_user_data(context);
        (*udf->vm)->AttachCurrentThread(udf->vm, (void **)&env, 0);

        mth = (*env)->GetMethodID(env, aclass, "xStep", "()V");
        clone = (*env)->GetMethodID(env, aclass, "clone",
            "()Ljava/lang/Object;");
    }

    // clone the Function.Aggregate instance and store a pointer
    // in SQLite's aggregate_context (clean up in xFinal)
    func = sqlite3_aggregate_context(context, sizeof(jobject));
    if (!*func) {
        udf = (struct UDFData*)sqlite3_user_data(context);
        (*udf->vm)->AttachCurrentThread(udf->vm, (void **)&env, 0);

        *func = (*env)->CallObjectMethod(env, udf->func, clone);
        *func = (*env)->NewGlobalRef(env, *func);
    }

    xCall(context, args, value, *func, mth);
}

void xInverse(sqlite3_context *context, int args, sqlite3_value** value)
{
    JNIEnv *env = 0;
    struct UDFData *udf = 0;
    jobject *func = 0;
    static jmethodID mth = 0;

    udf = (struct UDFData*)sqlite3_user_data(context);
    (*udf->vm)->AttachCurrentThread(udf->vm, (void **)&env, 0);

    if (!mth) mth = (*env)->GetMethodID(env, wclass, "xInverse", "()V");

    func = sqlite3_aggregate_context(context, sizeof(jobject));
    assert(*func); // disaster

    xCall(context, args, value, *func, mth);
}

void xValue(sqlite3_context *context)
{
    JNIEnv *env = 0;
    struct UDFData *udf = 0;
    jobject *func = 0;
    static jmethodID mth = 0;

    udf = (struct UDFData*)sqlite3_user_data(context);
    (*udf->vm)->AttachCurrentThread(udf->vm, (void **)&env, 0);

    if (!mth) mth = (*env)->GetMethodID(env, wclass, "xValue", "()V");

    func = sqlite3_aggregate_context(context, sizeof(jobject));
    assert(*func); // disaster

    xCall(context, 0, 0, *func, mth);
}

void xFinal(sqlite3_context *context)
{
    JNIEnv *env = 0;
    struct UDFData *udf = 0;
    jobject *func = 0;
    static jmethodID mth = 0;
    static jmethodID clone = 0;

    udf = (struct UDFData*)sqlite3_user_data(context);
    (*udf->vm)->AttachCurrentThread(udf->vm, (void **)&env, 0);

    if (!mth) mth = (*env)->GetMethodID(env, aclass, "xFinal", "()V");

    func = sqlite3_aggregate_context(context, sizeof(jobject));
    // func may not have been allocated if xStep never ran
    if (!*func) {
        udf = (struct UDFData*)sqlite3_user_data(context);
        (*udf->vm)->AttachCurrentThread(udf->vm, (void **)&env, 0);

        clone = (*env)->GetMethodID(env, aclass, "clone",
            "()Ljava/lang/Object;");

        *func = (*env)->CallObjectMethod(env, udf->func, clone);
        *func = (*env)->NewGlobalRef(env, *func);
    }

    xCall(context, 0, 0, *func, mth);

    // clean up Function.Aggregate instance
    (*env)->DeleteGlobalRef(env, *func);
}


// INITIALISATION ///////////////////////////////////////////////////

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env = 0;

    if (JNI_OK != (*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_2))
        return JNI_ERR;

    dbclass = (*env)->FindClass(env, "org/sqlite/core/NativeDB");
    if (!dbclass) return JNI_ERR;
    dbclass = (*env)->NewWeakGlobalRef(env, dbclass);

    fclass = (*env)->FindClass(env, "org/sqlite/Function");
    if (!fclass) return JNI_ERR;
    fclass = (*env)->NewWeakGlobalRef(env, fclass);

    aclass = (*env)->FindClass(env, "org/sqlite/Function$Aggregate");
    if (!aclass) return JNI_ERR;
    aclass = (*env)->NewWeakGlobalRef(env, aclass);

    wclass = (*env)->FindClass(env, "org/sqlite/Function$Window");
    if (!wclass) return JNI_ERR;
    wclass = (*env)->NewWeakGlobalRef(env, wclass);

    pclass = (*env)->FindClass(env, "org/sqlite/core/DB$ProgressObserver");
    if(!pclass) return JNI_ERR;
    pclass = (*env)->NewWeakGlobalRef(env, pclass);

    phandleclass = (*env)->FindClass(env, "org/sqlite/ProgressHandler");
    if(!phandleclass) return JNI_ERR;
    phandleclass = (*env)->NewWeakGlobalRef(env, phandleclass);

    return JNI_VERSION_1_2;
}

// FINALIZATION

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv* env = 0;

    if (JNI_OK != (*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_2))
        return;

    if (dbclass) (*env)->DeleteWeakGlobalRef(env, dbclass);

    if (fclass) (*env)->DeleteWeakGlobalRef(env, fclass);

    if (aclass) (*env)->DeleteWeakGlobalRef(env, aclass);

    if (wclass) (*env)->DeleteWeakGlobalRef(env, wclass);

    if (pclass) (*env)->DeleteWeakGlobalRef(env, pclass);

    if (phandleclass) (*env)->DeleteWeakGlobalRef(env, phandleclass);
}


// WRAPPERS for sqlite_* functions //////////////////////////////////

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_shared_1cache(
        JNIEnv *env, jobject this, jboolean enable)
{
    return sqlite3_enable_shared_cache(enable ? 1 : 0);
}


JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_enable_1load_1extension(
        JNIEnv *env, jobject this, jboolean enable)
{
    sqlite3 *db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return SQLITE_MISUSE;
    }

#ifdef SQLITE_EXTENSION
    return sqlite3_enable_load_extension(db, enable ? 1 : 0);
#else
    //[NSException raise:@"NSException" format:@"SQLite extension not supported"];
    return SQLITE_MISUSE;
#endif
}


JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB__1open_1utf8(
        JNIEnv *env, jobject this, jbyteArray file, jint flags)
{
    sqlite3 *db;
    int ret;
    char *file_bytes;

    db = gethandle(env, this);
    if (db) {
        throwex_msg(env, "DB already open");
        sqlite3_close(db);
        return;
    }

    utf8JavaByteArrayToUtf8Bytes(env, file, &file_bytes, NULL);
    if (!file_bytes) return;

    ret = sqlite3_open_v2(file_bytes, &db, flags, NULL);
    freeUtf8Bytes(file_bytes);

    sethandle(env, this, db);
    if (ret != SQLITE_OK) {
        ret = sqlite3_extended_errcode(db);
        throwex_errorcode(env, this, ret);
        sethandle(env, this, 0); // The handle is needed for throwex_errorcode
        sqlite3_close(db);
        return;
    }

    // Ignore failures, as we can tolerate regular result codes.
    (void) sqlite3_extended_result_codes(db, 1);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB__1close(
        JNIEnv *env, jobject this)
{
    sqlite3 *db = gethandle(env, this);
    if (db)
    {
        if (sqlite3_close(db) != SQLITE_OK)
        {
            throwex(env, this);
        }
        sethandle(env, this, 0);
    }
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_interrupt(JNIEnv *env, jobject this)
{
    sqlite3 *db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return;
    }

    sqlite3_interrupt(db);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_busy_1timeout(
    JNIEnv *env, jobject this, jint ms)
{
    sqlite3 *db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return;
    }

    sqlite3_busy_timeout(db, ms);
}

struct BusyHandlerContext {
    JavaVM * vm;
    jmethodID methodId;
    jobject obj;
};

static struct BusyHandlerContext busyHandlerContext;

int busyHandlerCallBack(void * ctx, int nbPrevInvok) {
    
    JNIEnv *env = 0;
    (*busyHandlerContext.vm)->AttachCurrentThread(busyHandlerContext.vm, (void **)&env, 0);

    return (*env)->CallIntMethod(   env,
                                    busyHandlerContext.obj,
                                    busyHandlerContext.methodId,
                                    nbPrevInvok);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_busy_1handler(
    JNIEnv *env, jobject this, jobject busyHandler)
{
    sqlite3 *db;

    (*env)->GetJavaVM(env, &busyHandlerContext.vm);
    
    if (busyHandler != NULL) {
        busyHandlerContext.obj = (*env)->NewGlobalRef(env, busyHandler);
        busyHandlerContext.methodId = (*env)->GetMethodID(  env,
                                                            (*env)->GetObjectClass(env, busyHandlerContext.obj),
                                                            "callback",
                                                            "(I)I");
    }

    db = gethandle(env, this);
    if (!db){
        throwex_db_closed(env);
        return;
    }
    
    if (busyHandler != NULL) {
        sqlite3_busy_handler(db, &busyHandlerCallBack, NULL);
    } else {
        sqlite3_busy_handler(db, NULL, NULL);
    }
}

JNIEXPORT jlong JNICALL Java_org_sqlite_core_NativeDB_prepare_1utf8(
        JNIEnv *env, jobject this, jbyteArray sql)
{
    sqlite3* db;
    sqlite3_stmt* stmt;
    char* sql_bytes;
    int sql_nbytes;
    int status;

    db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return 0;
    }

    utf8JavaByteArrayToUtf8Bytes(env, sql, &sql_bytes, &sql_nbytes);
    if (!sql_bytes) return fromref(0);

    status = sqlite3_prepare_v2(db, sql_bytes, sql_nbytes, &stmt, 0);
    freeUtf8Bytes(sql_bytes);

    if (status != SQLITE_OK) {
        throwex_errorcode(env, this, status);
        return fromref(0);
    }
    return fromref(stmt);
}


JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB__1exec_1utf8(
        JNIEnv *env, jobject this, jbyteArray sql)
{
    sqlite3* db;
    char* sql_bytes;
    int status;

    db = gethandle(env, this);
    if (!db)
    {
        throwex_errorcode(env, this, SQLITE_MISUSE);
        return SQLITE_MISUSE;
    }

    utf8JavaByteArrayToUtf8Bytes(env, sql, &sql_bytes, NULL);
    if (!sql_bytes)
    {
        return SQLITE_ERROR;
    }

    status = sqlite3_exec(db, sql_bytes, 0, 0, NULL);
    freeUtf8Bytes(sql_bytes);

    if (status != SQLITE_OK) {
        throwex_errorcode(env, this, status);
    }

    return status;
}


JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_errmsg_1utf8(JNIEnv *env, jobject this)
{
    sqlite3 *db;
    const char *str;

    db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return NULL;
    }
    
    str = (const char*) sqlite3_errmsg(db);
    if (!str) return NULL;
    return utf8BytesToJavaByteArray(env, str, strlen(str));
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_libversion_1utf8(
        JNIEnv *env, jobject this)
{
    const char* version = sqlite3_libversion();
    return utf8BytesToJavaByteArray(env, version, strlen(version));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_changes(
        JNIEnv *env, jobject this)
{
    sqlite3 *db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return 0;
    }

    return sqlite3_changes(db);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_total_1changes(
        JNIEnv *env, jobject this)
{
    sqlite3 *db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return 0;
    }

    return sqlite3_total_changes(db);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_finalize(
        JNIEnv *env, jobject this, jlong stmt)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_finalize(toref(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_step(
        JNIEnv *env, jobject this, jlong stmt)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_step(toref(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_reset(
        JNIEnv *env, jobject this, jlong stmt)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_reset(toref(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_clear_1bindings(
        JNIEnv *env, jobject this, jlong stmt)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_clear_bindings(toref(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1parameter_1count(
        JNIEnv *env, jobject this, jlong stmt)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_bind_parameter_count(toref(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_column_1count(
        JNIEnv *env, jobject this, jlong stmt)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_column_count(toref(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_column_1type(
        JNIEnv *env, jobject this, jlong stmt, jint col)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_column_type(toref(stmt), col);
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1decltype_1utf8(
        JNIEnv *env, jobject this, jlong stmt, jint col)
{
    const char *str;

    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return NULL;
    }

    str = (const char*) sqlite3_column_decltype(toref(stmt), col);
    if (!str) return NULL;
    return utf8BytesToJavaByteArray(env, str, strlen(str));
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1table_1name_1utf8(
        JNIEnv *env, jobject this, jlong stmt, jint col)
{
    const char *str;

    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return NULL;
    }

    str = sqlite3_column_table_name(toref(stmt), col);
    if (!str) return NULL;
    return utf8BytesToJavaByteArray(env, str, strlen(str));
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1name_1utf8(
        JNIEnv *env, jobject this, jlong stmt, jint col)
{
    const char *str;

    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return NULL;
    }

    str = sqlite3_column_name(toref(stmt), col);
    if (!str) return NULL;

    return utf8BytesToJavaByteArray(env, str, strlen(str));
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1text_1utf8(
        JNIEnv *env, jobject this, jlong stmt, jint col)
{
    sqlite3 *db;
    const char *bytes;
    int nbytes;

    db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return NULL;
    }

    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return NULL;
    }

    bytes = (const char*) sqlite3_column_text(toref(stmt), col);
    nbytes = sqlite3_column_bytes(toref(stmt), col);

    if (!bytes && sqlite3_errcode(db) == SQLITE_NOMEM)
    {
        throwex_outofmemory(env);
        return NULL;
    }

    return utf8BytesToJavaByteArray(env, bytes, nbytes);
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1blob(
        JNIEnv *env, jobject this, jlong stmt, jint col)
{
    sqlite3 *db;
    int type;
    int length;
    jbyteArray jBlob;
    const void *blob;

    db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return NULL;
    }

    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return NULL;
    }

    // The value returned by sqlite3_column_type() is only meaningful if no type conversions have occurred
    type = sqlite3_column_type(toref(stmt), col);
    blob = sqlite3_column_blob(toref(stmt), col);
    if (!blob && sqlite3_errcode(db) == SQLITE_NOMEM)
    {
        throwex_outofmemory(env);
        return NULL;
    }
    if (!blob) {
        if (type == SQLITE_NULL) {
            return NULL;
        }
        else {
            // The return value from sqlite3_column_blob() for a zero-length BLOB is a NULL pointer.
            jBlob = (*env)->NewByteArray(env, 0);
            if (!jBlob) { throwex_outofmemory(env); return NULL; }
            return jBlob;
        }
    }

    length = sqlite3_column_bytes(toref(stmt), col);
    jBlob = (*env)->NewByteArray(env, length);
    if (!jBlob) { throwex_outofmemory(env); return NULL; }

    (*env)->SetByteArrayRegion(env, jBlob, (jsize) 0, (jsize) length, (const jbyte*) blob);

    return jBlob;
}

JNIEXPORT jdouble JNICALL Java_org_sqlite_core_NativeDB_column_1double(
        JNIEnv *env, jobject this, jlong stmt, jint col)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return 0;
    }

    return sqlite3_column_double(toref(stmt), col);
}

JNIEXPORT jlong JNICALL Java_org_sqlite_core_NativeDB_column_1long(
        JNIEnv *env, jobject this, jlong stmt, jint col)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return 0;
    }

    return sqlite3_column_int64(toref(stmt), col);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_column_1int(
        JNIEnv *env, jobject this, jlong stmt, jint col)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return 0;
    }

    return sqlite3_column_int(toref(stmt), col);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1null(
        JNIEnv *env, jobject this, jlong stmt, jint pos)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_bind_null(toref(stmt), pos);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1int(
        JNIEnv *env, jobject this, jlong stmt, jint pos, jint v)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_bind_int(toref(stmt), pos, v);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1long(
        JNIEnv *env, jobject this, jlong stmt, jint pos, jlong v)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_bind_int64(toref(stmt), pos, v);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1double(
        JNIEnv *env, jobject this, jlong stmt, jint pos, jdouble v)
{
    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    return sqlite3_bind_double(toref(stmt), pos, v);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1text_1utf8(
        JNIEnv *env, jobject this, jlong stmt, jint pos, jbyteArray v)
{
    int rc;
    char* v_bytes;
    int v_nbytes;

    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    utf8JavaByteArrayToUtf8Bytes(env, v, &v_bytes, &v_nbytes);
    if (!v_bytes) return SQLITE_ERROR;

    rc = sqlite3_bind_text(toref(stmt), pos, v_bytes, v_nbytes, SQLITE_TRANSIENT);
    freeUtf8Bytes(v_bytes);

    return rc;
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1blob(
        JNIEnv *env, jobject this, jlong stmt, jint pos, jbyteArray v)
{
    jint rc;
    void *a;
    jsize size;

    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return SQLITE_MISUSE;
    }

    size = (*env)->GetArrayLength(env, v);
    a = (*env)->GetPrimitiveArrayCritical(env, v, 0);
    if (!a) { throwex_outofmemory(env); return 0; }
    rc = sqlite3_bind_blob(toref(stmt), pos, a, size, SQLITE_TRANSIENT);
    (*env)->ReleasePrimitiveArrayCritical(env, v, a, JNI_ABORT);
    return rc;
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1null(
        JNIEnv *env, jobject this, jlong context)
{
    if (!context) return;
    sqlite3_result_null(toref(context));
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1text_1utf8(
        JNIEnv *env, jobject this, jlong context, jbyteArray value)
{
    char* value_bytes;
    int value_nbytes;

    if (!context) return;
    if (value == NULL) { sqlite3_result_null(toref(context)); return; }

    utf8JavaByteArrayToUtf8Bytes(env, value, &value_bytes, &value_nbytes);
    if (!value_bytes)
    {
        sqlite3_result_error_nomem(toref(context));
        return;
    }

    sqlite3_result_text(toref(context), value_bytes, value_nbytes, SQLITE_TRANSIENT);
    freeUtf8Bytes(value_bytes);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1blob(
        JNIEnv *env, jobject this, jlong context, jobject value)
{
    jbyte *bytes;
    jsize size;

    if (!context) return;
    if (value == NULL) { sqlite3_result_null(toref(context)); return; }

    size = (*env)->GetArrayLength(env, value);
    bytes = (*env)->GetPrimitiveArrayCritical(env, value, 0);
    if (!bytes) { throwex_outofmemory(env); return; }
    sqlite3_result_blob(toref(context), bytes, size, SQLITE_TRANSIENT);
    (*env)->ReleasePrimitiveArrayCritical(env, value, bytes, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1double(
        JNIEnv *env, jobject this, jlong context, jdouble value)
{
    if (!context) return;
    sqlite3_result_double(toref(context), value);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1long(
        JNIEnv *env, jobject this, jlong context, jlong value)
{
    if (!context) return;
    sqlite3_result_int64(toref(context), value);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1int(
        JNIEnv *env, jobject this, jlong context, jint value)
{
    if (!context) return;
    sqlite3_result_int(toref(context), value);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1error_1utf8(
        JNIEnv *env, jobject this, jlong context, jbyteArray err)
{
    char* err_bytes;
    int err_nbytes;

    if (!context) return;

    utf8JavaByteArrayToUtf8Bytes(env, err, &err_bytes, &err_nbytes);
    if (!err_bytes)
    {
        sqlite3_result_error_nomem(toref(context));
        return;
    }

    sqlite3_result_error(toref(context), err_bytes, err_nbytes);
    freeUtf8Bytes(err_bytes);
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_value_1text_1utf8(
        JNIEnv *env, jobject this, jobject f, jint arg)
{
    const char* bytes;
    int nbytes;

    sqlite3_value *value = tovalue(env, f, arg);
    if (!value) return NULL;

    bytes = (const char*) sqlite3_value_text(value);
    nbytes = sqlite3_value_bytes(value);

    return utf8BytesToJavaByteArray(env, bytes, nbytes);
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_value_1blob(
        JNIEnv *env, jobject this, jobject f, jint arg)
{
    int length;
    jbyteArray jBlob;
    const void *blob;
    sqlite3_value *value = tovalue(env, f, arg);
    if (!value) return NULL;

    blob = sqlite3_value_blob(value);
    if (!blob) return NULL;

    length = sqlite3_value_bytes(value);
    jBlob = (*env)->NewByteArray(env, length);
    if (!jBlob) { throwex_outofmemory(env); return NULL; }

    (*env)->SetByteArrayRegion(env, jBlob, (jsize) 0, (jsize) length, (const jbyte*) blob);

    return jBlob;
}

JNIEXPORT jdouble JNICALL Java_org_sqlite_core_NativeDB_value_1double(
        JNIEnv *env, jobject this, jobject f, jint arg)
{
    sqlite3_value *value = tovalue(env, f, arg);
    return value ? sqlite3_value_double(value) : 0;
}

JNIEXPORT jlong JNICALL Java_org_sqlite_core_NativeDB_value_1long(
        JNIEnv *env, jobject this, jobject f, jint arg)
{
    sqlite3_value *value = tovalue(env, f, arg);
    return value ? sqlite3_value_int64(value) : 0;
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_value_1int(
        JNIEnv *env, jobject this, jobject f, jint arg)
{
    sqlite3_value *value = tovalue(env, f, arg);
    return value ? sqlite3_value_int(value) : 0;
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_value_1type(
        JNIEnv *env, jobject this, jobject func, jint arg)
{
    return sqlite3_value_type(tovalue(env, func, arg));
}


JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_create_1function_1utf8(
        JNIEnv *env, jobject this, jbyteArray name, jobject func, jint nArgs, jint flags)
{
    jint ret = 0;
    char *name_bytes;
    int isAgg = 0, isWindow = 0;

    static jfieldID udfdatalist = 0;
    struct UDFData *udf = malloc(sizeof(struct UDFData));

    if (!udf) { throwex_outofmemory(env); return 0; }

    if (!udfdatalist)
        udfdatalist = (*env)->GetFieldID(env, dbclass, "udfdatalist", "J");

    isAgg = (*env)->IsInstanceOf(env, func, aclass);
    isWindow = (*env)->IsInstanceOf(env, func, wclass);
    udf->func = (*env)->NewGlobalRef(env, func);
    (*env)->GetJavaVM(env, &udf->vm);

    // add new function def to linked list
    udf->next = toref((*env)->GetLongField(env, this, udfdatalist));
    (*env)->SetLongField(env, this, udfdatalist, fromref(udf));

    utf8JavaByteArrayToUtf8Bytes(env, name, &name_bytes, NULL);
    if (!name_bytes) { throwex_outofmemory(env); return 0; }

    if (isAgg) {
        ret = sqlite3_create_window_function(
                gethandle(env, this),
                name_bytes,            // function name
                nArgs,                 // number of args
                SQLITE_UTF16 | flags,  // preferred chars
                udf,
                &xStep,
                &xFinal,
                isWindow ? &xValue : 0,
                isWindow ? &xInverse : 0,
                0
        );
    } else {
        ret = sqlite3_create_function(
                gethandle(env, this),
                name_bytes,            // function name
                nArgs,                 // number of args
                SQLITE_UTF16 | flags,  // preferred chars
                udf,
                &xFunc,
                0,
                0
        );
    }

    freeUtf8Bytes(name_bytes);

    return ret;
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_destroy_1function_1utf8(
        JNIEnv *env, jobject this, jbyteArray name, jint nArgs)
{
    jint ret = 0;
    char* name_bytes;

    utf8JavaByteArrayToUtf8Bytes(env, name, &name_bytes, NULL);
    if (!name_bytes) { throwex_outofmemory(env); return 0; }
    
    ret = sqlite3_create_function(
        gethandle(env, this), name_bytes, nArgs, SQLITE_UTF16, 0, 0, 0, 0
    );
    freeUtf8Bytes(name_bytes);

    return ret;
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_free_1functions(
        JNIEnv *env, jobject this)
{
    // clean up all the malloc()ed UDFData instances using the
    // linked list stored in DB.udfdatalist
    jfieldID udfdatalist;
    struct UDFData *udf, *udfpass;

    udfdatalist = (*env)->GetFieldID(env, dbclass, "udfdatalist", "J");
    udf = toref((*env)->GetLongField(env, this, udfdatalist));
    (*env)->SetLongField(env, this, udfdatalist, 0);

    while (udf) {
        udfpass = udf->next;
        (*env)->DeleteGlobalRef(env, udf->func);
        free(udf);
        udf = udfpass;
    }
}


// COMPOUND FUNCTIONS ///////////////////////////////////////////////

JNIEXPORT jobjectArray JNICALL Java_org_sqlite_core_NativeDB_column_1metadata(
        JNIEnv *env, jobject this, jlong stmt)
{
    const char *zTableName, *zColumnName;
    int pNotNull, pPrimaryKey, pAutoinc, i, colCount;
    jobjectArray array;
    jbooleanArray colData;
    jboolean* colDataRaw;
    sqlite3 *db;
    sqlite3_stmt *dbstmt;

    db = gethandle(env, this);
    if (!db)
    {
        throwex_db_closed(env);
        return NULL;
    }

    if (!stmt)
    {
        throwex_stmt_finalized(env);
        return NULL;
    }

    dbstmt = toref(stmt);

    colCount = sqlite3_column_count(dbstmt);
    array = (*env)->NewObjectArray(
        env, colCount, (*env)->FindClass(env, "[Z"), NULL) ;
    if (!array) { throwex_outofmemory(env); return 0; }

    colDataRaw = (jboolean*)malloc(3 * sizeof(jboolean));
    if (!colDataRaw) { throwex_outofmemory(env); return 0; }

    for (i = 0; i < colCount; i++) {
        // load passed column name and table name
        zColumnName = sqlite3_column_name(dbstmt, i);
        zTableName  = sqlite3_column_table_name(dbstmt, i);

        pNotNull = 0;
        pPrimaryKey = 0;
        pAutoinc = 0;

        // request metadata for column and load into output variables
        if (zTableName && zColumnName) {
            sqlite3_table_column_metadata(
                db, 0, zTableName, zColumnName,
                0, 0, &pNotNull, &pPrimaryKey, &pAutoinc
            );
        }

        // load relevant metadata into 2nd dimension of return results
        colDataRaw[0] = pNotNull;
        colDataRaw[1] = pPrimaryKey;
        colDataRaw[2] = pAutoinc;

        colData = (*env)->NewBooleanArray(env, 3);
        if (!colData) { throwex_outofmemory(env); return 0; }

        (*env)->SetBooleanArrayRegion(env, colData, 0, 3, colDataRaw);
        (*env)->SetObjectArrayElement(env, array, i, colData);
    }

    free(colDataRaw);

    return array;
}

// backup function

void reportProgress(JNIEnv* env, jobject func, int remaining, int pageCount) {

  static jmethodID mth = 0;
  if (!mth) {
      mth = (*env)->GetMethodID(env, pclass, "progress", "(II)V");
  }

  if(!func)
    return;

  (*env)->CallVoidMethod(env, func, mth, remaining, pageCount);
}


/*
** Perform an online backup of database pDb to the database file named
** by zFilename. This function copies 5 database pages from pDb to
** zFilename, then unlocks pDb and sleeps for 250 ms, then repeats the
** process until the entire database is backed up.
**
** The third argument passed to this function must be a pointer to a progress
** function. After each set of 5 pages is backed up, the progress function
** is invoked with two integer parameters: the number of pages left to
** copy, and the total number of pages in the source file. This information
** may be used, for example, to update a GUI progress bar.
**
** While this function is running, another thread may use the database pDb, or
** another process may access the underlying database file via a separate
** connection.
**
** If the backup process is successfully completed, SQLITE_OK is returned.
** Otherwise, if an error occurs, an SQLite error code is returned.
*/

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_backup(
  JNIEnv *env, jobject this,
  jbyteArray zDBName,
  jbyteArray zFilename,       /* Name of file to back up to */
  jobject observer            /* Progress function to invoke */
)
{
#if SQLITE_VERSION_NUMBER >= 3006011
  int rc;                     /* Function return code */
  sqlite3* pDb;               /* Database to back up */
  sqlite3* pFile;             /* Database connection opened on zFilename */
  sqlite3_backup *pBackup;    /* Backup handle used to copy data */
  char *dFileName;
  char *dDBName;

  pDb = gethandle(env, this);
  if (!pDb)
  {
    throwex_db_closed(env);
    return SQLITE_MISUSE;
  }

  utf8JavaByteArrayToUtf8Bytes(env, zFilename, &dFileName, NULL);
  if (!dFileName)
  {
    return SQLITE_NOMEM;
  }

  utf8JavaByteArrayToUtf8Bytes(env, zDBName, &dDBName, NULL);
  if (!dDBName)
  {
    freeUtf8Bytes(dFileName);
    return SQLITE_NOMEM;
  }

  /* Open the database file identified by dFileName. */
  rc = sqlite3_open(dFileName, &pFile);
  if( rc==SQLITE_OK ){

    /* Open the sqlite3_backup object used to accomplish the transfer */
    pBackup = sqlite3_backup_init(pFile, "main", pDb, dDBName);
    if( pBackup ){
      while((rc = sqlite3_backup_step(pBackup,100))==SQLITE_OK ){}

      /* Release resources allocated by backup_init(). */
      (void)sqlite3_backup_finish(pBackup);
    }
    rc = sqlite3_errcode(pFile);
  }

  /* Close the database connection opened on database file zFilename
  ** and return the result of this function. */
  (void)sqlite3_close(pFile);

  freeUtf8Bytes(dDBName);
  freeUtf8Bytes(dFileName);

  return rc;
#else
  return SQLITE_INTERNAL;
#endif
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_restore(
  JNIEnv *env, jobject this,
  jbyteArray zDBName,
  jbyteArray zFilename,         /* Name of file to back up to */
  jobject observer              /* Progress function to invoke */
)
{
#if SQLITE_VERSION_NUMBER >= 3006011
  int rc;                     /* Function return code */
  sqlite3* pDb;               /* Database to back up */
  sqlite3* pFile;             /* Database connection opened on zFilename */
  sqlite3_backup *pBackup;    /* Backup handle used to copy data */
  char *dFileName;
  char *dDBName;
  int nTimeout = 0;

  pDb = gethandle(env, this);
  if (!pDb)
  {
    throwex_db_closed(env);
    return SQLITE_MISUSE;
  }

  utf8JavaByteArrayToUtf8Bytes(env, zFilename, &dFileName, NULL);
  if (!dFileName)
  {
    return SQLITE_NOMEM;
  }

  utf8JavaByteArrayToUtf8Bytes(env, zDBName, &dDBName, NULL);
  if (!dDBName)
  {
    freeUtf8Bytes(dFileName);
    return SQLITE_NOMEM;
  }

  /* Open the database file identified by dFileName. */
  rc = sqlite3_open(dFileName, &pFile);
  if( rc==SQLITE_OK ){

    /* Open the sqlite3_backup object used to accomplish the transfer */
    pBackup = sqlite3_backup_init(pDb, dDBName, pFile, "main");
    if( pBackup ){
        while( (rc = sqlite3_backup_step(pBackup,100))==SQLITE_OK
              || rc==SQLITE_BUSY  ){
              if( rc==SQLITE_BUSY ){
                if( nTimeout++ >= 3 ) break;
                sqlite3_sleep(100);
            }
        }
      /* Release resources allocated by backup_init(). */
      (void)sqlite3_backup_finish(pBackup);
    }
    rc = sqlite3_errcode(pFile);
  }

  /* Close the database connection opened on database file zFilename
  ** and return the result of this function. */
  (void)sqlite3_close(pFile);

  freeUtf8Bytes(dDBName);
  freeUtf8Bytes(dFileName);

  return rc;
#else
  return SQLITE_INTERNAL;
#endif
}


// Progress handler

struct ProgressHandlerContext {
    JavaVM *vm;
    jmethodID mth;
    jobject phandler;
};

static struct ProgressHandlerContext progress_handler_context;

int progress_handler_function(void *ctx) {
    JNIEnv *env = 0;
    jint rv;
    (*progress_handler_context.vm)->AttachCurrentThread(progress_handler_context.vm, (void **)&env, 0);
    rv = (*env)->CallIntMethod(env, progress_handler_context.phandler, progress_handler_context.mth);
    return rv;
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_register_1progress_1handler(
  JNIEnv *env,
  jobject this,
  jint vmCalls,
  jobject progressHandler
)
{
    progress_handler_context.mth = (*env)->GetMethodID(env, phandleclass, "progress", "()I");
    progress_handler_context.phandler = (*env)->NewGlobalRef(env, progressHandler);
    (*env)->GetJavaVM(env, &progress_handler_context.vm);
    sqlite3_progress_handler(gethandle(env, this), vmCalls, &progress_handler_function, NULL);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_clear_1progress_1handler(
  JNIEnv *env,
  jobject this
)
{
    sqlite3_progress_handler(gethandle(env, this), 0, NULL, NULL);
    (*env)->DeleteGlobalRef(env, progress_handler_context.phandler);
}
