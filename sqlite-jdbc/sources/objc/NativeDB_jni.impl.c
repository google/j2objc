/**
 SQLite JNI Implementation for J2ObjC.
 Author: DaeHoon Zee
 */
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "org/sqlite/Function.h"
#include "org/sqlite/core/NativeDB.h"
#include "org/sqlite/core/DB.h"
#include "org/sqlite/ProgressHandler.h"
#include "org/sqlite/BusyHandler.h"

#include <stdlib.h>
#include <string.h>
#include <assert.h>
#import <sqlite3.h>
#include "jni.h"

typedef OrgSqliteCoreNativeDB NativeDB;
typedef OrgSqliteFunction Function;
typedef OrgSqliteFunction_Aggregate Aggregate;
typedef OrgSqliteFunction_Window Window;
typedef OrgSqliteBusyHandler BusyHandler;
typedef OrgSqliteProgressHandler ProgressHandler;

static inline sqlite3_stmt* to_dbstmt(jlong value) { return (sqlite3_stmt*) value; }
static inline sqlite3_context* to_dbcotext(jlong value) { return (sqlite3_context*) value; }
static inline void * toref(jlong value) { return (void*) value; }
static inline jlong fromref(void * value) { return (jlong) value; }
static char dummy = 0;

static void throwex(JNIEnv *env, jobject self)
{
  [self throwex];
}

static void throwex_errorcode(JNIEnv *env, jobject self, int errorCode)
{
  [self throwexWithInt:errorCode];
}

static void throwex_msg(JNIEnv *env, NSString *str)
{
  [NativeDB throwexWithNSString:str];
}

static void throwex_outofmemory(JNIEnv *env)
{
  throwex_msg(env, @"Out of memory");
}

static void throwex_stmt_finalized(JNIEnv *env)
{
  throwex_msg(env, @"The prepared statement has been finalized");
}

static void throwex_db_closed(JNIEnv *env)
{
  throwex_msg(env, @"The database has been closed");
}

static IOSByteArray* getEmptyBLOB()
{
  static IOSByteArray* empty_blob = [IOSByteArray arrayWithLength:0];
  return empty_blob;
}

static jbyteArray utf8BytesToJavaByteArray(JNIEnv *env, const char* bytes, int nbytes)
{
  if (!bytes) {
    return NULL;
  }
  
  jbyteArray result = [IOSByteArray newArrayWithBytes:(jbyte*)bytes count: nbytes];
  
  return result;
}

static char* utf8JavaByteArrayToUtf8Bytes(JNIEnv *env, jbyteArray utf8bytes, int* nbytes)
{
  if (!utf8bytes) {
    if (nbytes) *nbytes = 0;
    return NULL;
  }
  int len = [utf8bytes length];
  if (nbytes) *nbytes = len;
  if (len == 0) {
    return &dummy;
  }
  char* bytes = (char*)[utf8bytes byteRefAtIndex:0];
  // @zee ByteArray allocation size must be greater than its length.
  bytes[len] = 0;
  return bytes;
}

static void freeUtf8Bytes(char* bytes) {
  // IGNORE;
}

static sqlite3 * gethandle(JNIEnv *env, jobject self)
{
  return (sqlite3*)((NativeDB*)self)->pointer_;
}

static void sethandle(JNIEnv *env, jobject self, sqlite3 * ref)
{
  ((NativeDB*)self)->pointer_ = (jlong)ref;
}


// User Defined Function SUPPORT ////////////////////////////////////

struct UDFData {
  Function* func;
  struct UDFData *next;  // linked list of all UDFData instances
};

/* Returns the sqlite3_value for the given arg of the given function.
 * If 0 is returned, an exception has been thrown to report the reason. */
static sqlite3_value * tovalue(JNIEnv *env, jobject func0, jint arg)
{
  Function* func = (Function*)func0;
  // check we have any business being here
  if (arg  < 0) { throwex_msg(env, @"negative arg out of range"); return 0; }
  if (!func) { throwex_msg(env, @"inconstent function"); return 0; }
  
  jlong value_pntr = func->value_;
  jint numArgs = func->args_;
  
  if (value_pntr == 0) { throwex_msg(env, @"no current value"); return 0; }
  if (arg >= numArgs) { throwex_msg(env, @"arg out of range"); return 0; }
  
  return ((sqlite3_value**)toref(value_pntr))[arg];
}


#define XFUNC 0
#define XSTEP 1
#define XFINAL 2
#define XINVERSE 3
#define XVALUE 4

/* used to call xFunc, xStep and xFinal */
static void xCall(
                  sqlite3_context *context,
                  int args,
                  sqlite3_value** value,
                  jobject func0,
                  int method)
{
  struct UDFData *udf = 0;
  
  udf = (struct UDFData*)sqlite3_user_data(context);
  assert(udf);
  Function* func = (func0) ? (Function*)func0 : udf->func;
  
  
  func->context_ = fromref(context);
  func->value_ = value ? fromref(value) : 0;
  func->args_ = args;
  
  switch (method) {
    case XFUNC:
      [func xFunc];
      break;
    case XSTEP:
      [(Aggregate*)func xStep];
      break;
    case XFINAL:
      [(Aggregate*)func xFinal];
      break;
    case XINVERSE:
      [(Window*)func xInverse];
      break;
    case XVALUE:
      [(Window*)func xValue];
      break;
    default:
      assert(false);
  }
  
  func->context_ = 0;
  func->value_ = 0;
  func->args_ = 0;
}


void xFunc(sqlite3_context *context, int args, sqlite3_value** value)
{
  xCall(context, args, value, 0, XFUNC);
}

void xStep(sqlite3_context *context, int args, sqlite3_value** value)
{
  jobject *func = (jobject*)sqlite3_aggregate_context(context, sizeof(jobject));
  if (!*func) {
    // clone the Function.Aggregate instance and store a pointer
    // in SQLite's aggregate_context (clean up in xFinal)
    UDFData* udf = (struct UDFData*)sqlite3_user_data(context);
    
    *func = [(Aggregate*)udf->func java_clone];
    (void)RETAIN_(*func);
  }
  
  xCall(context, args, value, *func, XSTEP);
}

void xInverse(sqlite3_context *context, int args, sqlite3_value** value)
{
  jobject *func = (jobject*)sqlite3_aggregate_context(context, sizeof(jobject));
  assert(*func); // disaster
  
  xCall(context, args, value, *func, XINVERSE);
}

void xValue(sqlite3_context *context)
{
  jobject *func = (jobject*)sqlite3_aggregate_context(context, sizeof(jobject));
  assert(*func); // disaster
  
  xCall(context, 0, 0, *func, XVALUE);
}

void xFinal(sqlite3_context *context)
{
  jobject *func = (jobject*)sqlite3_aggregate_context(context, sizeof(jobject));
  // func may not have been allocated if xStep never ran
  if (!*func) {
    struct UDFData *udf = (struct UDFData*)sqlite3_user_data(context);
    *func = [(Aggregate*)udf->func java_clone];
  }
  
  xCall(context, 0, 0, *func, XFINAL);
  
  RELEASE_(*func);
}





// WRAPPERS for sqlite_* functions //////////////////////////////////

#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_shared_1cache(
                                                                   JNIEnv *env, jobject self, jboolean enable)
{
  return sqlite3_enable_shared_cache(enable ? 1 : 0);
}
#pragma clang diagnostic pop


JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_enable_1load_1extension(
                                                                             JNIEnv *env, jobject self, jboolean enable)
{
  sqlite3 *db = gethandle(env, self);
  if (!db)
  {
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
                                                                  JNIEnv *env, jobject self, jbyteArray file, jint flags)
{
  sqlite3 *db;
  int ret;
  char *file_bytes;
  
  db = gethandle(env, self);
  if (db) {
    throwex_msg(env, @"DB already open");
    sqlite3_close(db);
    return;
  }
  
  file_bytes = utf8JavaByteArrayToUtf8Bytes(env, file, NULL);
  if (!file_bytes) return;
  
  ret = sqlite3_open_v2((char*)file_bytes, &db, flags, NULL);
  freeUtf8Bytes(file_bytes);
  
  sethandle(env, self, db);
  if (ret != SQLITE_OK) {
    ret = sqlite3_extended_errcode(db);
    throwex_errorcode(env, self, ret);
    sethandle(env, self, 0); // The handle is needed for throwex_errorcode
    sqlite3_close(db);
    return;
  }
  
  // Ignore failures, as we can tolerate regular result codes.
  (void) sqlite3_extended_result_codes(db, 1);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB__1close(
                                                             JNIEnv *env, jobject self)
{
  sqlite3 *db = gethandle(env, self);
  if (db)
  {
    if (sqlite3_close(db) != SQLITE_OK)
    {
      throwex(env, self);
    }
    sethandle(env, self, 0);
  }
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_interrupt(JNIEnv *env, jobject self)
{
  sqlite3 *db = gethandle(env, self);
  if (!db)
  {
    throwex_db_closed(env);
    return;
  }
  
  sqlite3_interrupt(db);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_busy_1timeout(
                                                                   JNIEnv *env, jobject self, jint ms)
{
  sqlite3 *db = gethandle(env, self);
  if (!db)
  {
    throwex_db_closed(env);
    return;
  }
  
  sqlite3_busy_timeout(db, ms);
}

struct BusyHandlerContext {
  jobject obj;
};

static struct BusyHandlerContext busyHandlerContext;

int busyHandlerCallBack(void * ctx, int nbPrevInvok) {
  
//  (*busyHandlerContext.vm)->AttachCurrentThread(busyHandlerContext.vm, (void **)&env, 0);
  
  return [(BusyHandler*)busyHandlerContext.obj callbackWithInt:nbPrevInvok];
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_busy_1handler(
                                                                   JNIEnv *env, jobject self, jobject busyHandler)
{
  sqlite3 *db;
  
  if (busyHandler != NULL) {
    busyHandlerContext.obj = RETAIN_(busyHandler);
  }
  
  db = gethandle(env, self);
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
                                                                    JNIEnv *env, jobject self, jbyteArray sql)
{
  sqlite3* db;
  sqlite3_stmt* stmt;
  char* sql_bytes;
  int sql_nbytes;
  int status;
  
  db = gethandle(env, self);
  if (!db)
  {
    throwex_db_closed(env);
    return 0;
  }
  
  sql_bytes= utf8JavaByteArrayToUtf8Bytes(env, sql, &sql_nbytes);
  if (!sql_bytes) return fromref(0);
  
  status = sqlite3_prepare_v2(db, sql_bytes, sql_nbytes, &stmt, 0);
  freeUtf8Bytes(sql_bytes);
  
  if (status != SQLITE_OK) {
    throwex_errorcode(env, self, status);
    return fromref(0);
  }
  return fromref(stmt);
}


JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB__1exec_1utf8(
                                                                  JNIEnv *env, jobject self, jbyteArray sql)
{
  sqlite3* db;
  char* sql_bytes;
  int status;
  
  db = gethandle(env, self);
  if (!db)
  {
    throwex_errorcode(env, self, SQLITE_MISUSE);
    return SQLITE_MISUSE;
  }
  
  sql_bytes = utf8JavaByteArrayToUtf8Bytes(env, sql, NULL);
  if (!sql_bytes)
  {
    return SQLITE_ERROR;
  }
  
  status = sqlite3_exec(db, sql_bytes, 0, 0, NULL);
  freeUtf8Bytes(sql_bytes);
  
  if (status != SQLITE_OK) {
    throwex_errorcode(env, self, status);
  }
  
  return status;
}


JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_errmsg_1utf8(JNIEnv *env, jobject self)
{
  sqlite3 *db;
  const char *str;
  
  db = gethandle(env, self);
  if (!db)
  {
    throwex_db_closed(env);
    return NULL;
  }
  
  str = (const char*) sqlite3_errmsg(db);
  if (!str) return NULL;
  return utf8BytesToJavaByteArray(env, str, (int)strlen(str));
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_libversion_1utf8(
                                                                            JNIEnv *env, jobject self)
{
  const char* version = sqlite3_libversion();
  return utf8BytesToJavaByteArray(env, version, (int)strlen(version));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_changes(
                                                             JNIEnv *env, jobject self)
{
  sqlite3 *db = gethandle(env, self);
  if (!db)
  {
    throwex_db_closed(env);
    return 0;
  }
  
  return sqlite3_changes(db);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_total_1changes(
                                                                    JNIEnv *env, jobject self)
{
  sqlite3 *db = gethandle(env, self);
  if (!db)
  {
    throwex_db_closed(env);
    return 0;
  }
  
  return sqlite3_total_changes(db);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_finalize(
                                                              JNIEnv *env, jobject self, jlong stmt)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_finalize(to_dbstmt(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_step(
                                                          JNIEnv *env, jobject self, jlong stmt)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_step(to_dbstmt(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_reset(
                                                           JNIEnv *env, jobject self, jlong stmt)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_reset(to_dbstmt(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_clear_1bindings(
                                                                     JNIEnv *env, jobject self, jlong stmt)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_clear_bindings(to_dbstmt(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1parameter_1count(
                                                                            JNIEnv *env, jobject self, jlong stmt)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_bind_parameter_count(to_dbstmt(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_column_1count(
                                                                   JNIEnv *env, jobject self, jlong stmt)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_column_count(to_dbstmt(stmt));
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_column_1type(
                                                                  JNIEnv *env, jobject self, jlong stmt, jint col)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_column_type(to_dbstmt(stmt), col);
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1decltype_1utf8(
                                                                                  JNIEnv *env, jobject self, jlong stmt, jint col)
{
  const char *str;
  
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return NULL;
  }
  
  str = (const char*) sqlite3_column_decltype(to_dbstmt(stmt), col);
  if (!str) return NULL;
  return utf8BytesToJavaByteArray(env, str, (int)strlen(str));
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1table_1name_1utf8(
                                                                                     JNIEnv *env, jobject self, jlong stmt, jint col)
{
  const char *str;
  
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return NULL;
  }
  
  str = sqlite3_column_table_name(to_dbstmt(stmt), col);
  if (!str) return NULL;
  return utf8BytesToJavaByteArray(env, str, (int)strlen(str));
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1name_1utf8(
                                                                              JNIEnv *env, jobject self, jlong stmt, jint col)
{
  const char *str;
  
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return NULL;
  }
  
  str = sqlite3_column_name(to_dbstmt(stmt), col);
  if (!str) return NULL;
  
  return utf8BytesToJavaByteArray(env, str, (int)strlen(str));
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1text_1utf8(
                                                                              JNIEnv *env, jobject self, jlong stmt, jint col)
{
  sqlite3 *db;
  const char *bytes;
  int nbytes;
  
  db = gethandle(env, self);
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
  
  bytes = (const char*) sqlite3_column_text(to_dbstmt(stmt), col);
  nbytes = sqlite3_column_bytes(to_dbstmt(stmt), col);
  
  if (!bytes && sqlite3_errcode(db) == SQLITE_NOMEM)
  {
    throwex_outofmemory(env);
    return NULL;
  }
  
  return utf8BytesToJavaByteArray(env, bytes, nbytes);
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_column_1blob(
                                                                        JNIEnv *env, jobject self, jlong stmt, jint col)
{
  sqlite3 *db;
  int type;
  int length;
  jbyteArray jBlob;
  const void *blob;
  
  db = gethandle(env, self);
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
  type = sqlite3_column_type(to_dbstmt(stmt), col);
  blob = sqlite3_column_blob(to_dbstmt(stmt), col);
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
      jBlob = getEmptyBLOB();
      return jBlob;
    }
  }
  
  length = sqlite3_column_bytes(to_dbstmt(stmt), col);
  jBlob = [IOSByteArray newArrayWithBytes:(jbyte*)blob count:length];
  return jBlob;
}

JNIEXPORT jdouble JNICALL Java_org_sqlite_core_NativeDB_column_1double(
                                                                       JNIEnv *env, jobject self, jlong stmt, jint col)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return 0;
  }
  
  return sqlite3_column_double(to_dbstmt(stmt), col);
}

JNIEXPORT jlong JNICALL Java_org_sqlite_core_NativeDB_column_1long(
                                                                   JNIEnv *env, jobject self, jlong stmt, jint col)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return 0;
  }
  
  return sqlite3_column_int64(to_dbstmt(stmt), col);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_column_1int(
                                                                 JNIEnv *env, jobject self, jlong stmt, jint col)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return 0;
  }
  
  return sqlite3_column_int(to_dbstmt(stmt), col);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1null(
                                                                JNIEnv *env, jobject self, jlong stmt, jint pos)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_bind_null(to_dbstmt(stmt), pos);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1int(
                                                               JNIEnv *env, jobject self, jlong stmt, jint pos, jint v)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_bind_int(to_dbstmt(stmt), pos, v);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1long(
                                                                JNIEnv *env, jobject self, jlong stmt, jint pos, jlong v)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_bind_int64(to_dbstmt(stmt), pos, v);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1double(
                                                                  JNIEnv *env, jobject self, jlong stmt, jint pos, jdouble v)
{
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  return sqlite3_bind_double(to_dbstmt(stmt), pos, v);
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1text_1utf8(
                                                                      JNIEnv *env, jobject self, jlong stmt, jint pos, jbyteArray v)
{
  int rc;
  char* v_bytes;
  int v_nbytes;
  
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  v_bytes = utf8JavaByteArrayToUtf8Bytes(env, v, &v_nbytes);
  if (!v_bytes) return SQLITE_ERROR;
  
  rc = sqlite3_bind_text(to_dbstmt(stmt), pos, v_bytes, v_nbytes, SQLITE_TRANSIENT);
  freeUtf8Bytes(v_bytes);
  
  return rc;
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_bind_1blob(
                                                                JNIEnv *env, jobject self, jlong stmt, jint pos, jbyteArray v)
{
  jint rc;
  void *a;
  jsize size;
  
  if (!stmt)
  {
    throwex_stmt_finalized(env);
    return SQLITE_MISUSE;
  }
  
  size = v->size_;
  a = size == 0 ? &dummy : (char*)IOSByteArray_GetRef(v, 0);
  rc = sqlite3_bind_blob(to_dbstmt(stmt), pos, a, size, SQLITE_TRANSIENT);
  return rc;
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1null(
                                                                  JNIEnv *env, jobject self, jlong context)
{
  if (!context) return;
  sqlite3_result_null(to_dbcotext(context));
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1text_1utf8(
                                                                        JNIEnv *env, jobject self, jlong context, jbyteArray value)
{
  char* value_bytes;
  int value_nbytes;
  
  if (!context) return;
  if (value == NULL) { sqlite3_result_null(to_dbcotext(context)); return; }
  
  value_bytes = utf8JavaByteArrayToUtf8Bytes(env, value, &value_nbytes);
  if (!value_bytes)
  {
    sqlite3_result_error_nomem(to_dbcotext(context));
    return;
  }
  
  sqlite3_result_text(to_dbcotext(context), value_bytes, value_nbytes, SQLITE_TRANSIENT);
  freeUtf8Bytes(value_bytes);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1blob(
                                                                  JNIEnv *env, jobject self, jlong context, jarray v)
{
  char *bytes;
  jsize size;
  
  if (!context) return;
  if (v == NULL) { sqlite3_result_null(to_dbcotext(context)); return; }
  
  size = v->size_;
  bytes = size == 0 ? &dummy : (char*)IOSByteArray_GetRef((IOSByteArray*)v, 0);
  sqlite3_result_blob(to_dbcotext(context), bytes, size, SQLITE_TRANSIENT);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1double(
                                                                    JNIEnv *env, jobject self, jlong context, jdouble value)
{
  if (!context) return;
  sqlite3_result_double(to_dbcotext(context), value);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1long(
                                                                  JNIEnv *env, jobject self, jlong context, jlong value)
{
  if (!context) return;
  sqlite3_result_int64(to_dbcotext(context), value);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1int(
                                                                 JNIEnv *env, jobject self, jlong context, jint value)
{
  if (!context) return;
  sqlite3_result_int(to_dbcotext(context), value);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_result_1error_1utf8(
                                                                         JNIEnv *env, jobject self, jlong context, jbyteArray err)
{
  char* err_bytes;
  int err_nbytes;
  
  if (!context) return;
  
  err_bytes = utf8JavaByteArrayToUtf8Bytes(env, err, &err_nbytes);
  if (!err_bytes)
  {
    sqlite3_result_error_nomem(to_dbcotext(context));
    return;
  }
  
  sqlite3_result_error(to_dbcotext(context), err_bytes, err_nbytes);
  freeUtf8Bytes(err_bytes);
}

JNIEXPORT jbyteArray JNICALL Java_org_sqlite_core_NativeDB_value_1text_1utf8(
                                                                             JNIEnv *env, jobject self, jobject f, jint arg)
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
                                                                       JNIEnv *env, jobject self, jobject f, jint arg)
{
  int length;
  jbyteArray jBlob;
  const void *blob;
  sqlite3_value *value = tovalue(env, f, arg);
  if (!value) return NULL;
  
  blob = sqlite3_value_blob(value);
  if (!blob) return NULL;
  
  length = sqlite3_value_bytes(value);
  jBlob = [IOSByteArray newArrayWithBytes:(jbyte*)blob count:length];
  
  return jBlob;
}

JNIEXPORT jdouble JNICALL Java_org_sqlite_core_NativeDB_value_1double(
                                                                      JNIEnv *env, jobject self, jobject f, jint arg)
{
  sqlite3_value *value = tovalue(env, f, arg);
  return value ? sqlite3_value_double(value) : 0;
}

JNIEXPORT jlong JNICALL Java_org_sqlite_core_NativeDB_value_1long(
                                                                  JNIEnv *env, jobject self, jobject f, jint arg)
{
  sqlite3_value *value = tovalue(env, f, arg);
  return value ? sqlite3_value_int64(value) : 0;
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_value_1int(
                                                                JNIEnv *env, jobject self, jobject f, jint arg)
{
  sqlite3_value *value = tovalue(env, f, arg);
  return value ? sqlite3_value_int(value) : 0;
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_value_1type(
                                                                 JNIEnv *env, jobject self, jobject func, jint arg)
{
  return sqlite3_value_type(tovalue(env, func, arg));
}

#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wunguarded-availability-new"
int is_iOS_13x_available();

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_create_1function_1utf8(
                                                                            JNIEnv *env, jobject self, jbyteArray name, jobject func, jint nArgs, jint flags)
{
  jint ret = 0;
  char *name_bytes;
  
  struct UDFData *udf = (UDFData*)malloc(sizeof(struct UDFData));
  
  if (!udf) { throwex_outofmemory(env); return 0; }
  
  udf->func = RETAIN_(func);
  
  // add new function def to linked list
  udf->next = (UDFData*)((NativeDB*)self)->udfdatalist_;
  ((NativeDB*)self)->udfdatalist_ = fromref(udf);
  
  name_bytes = utf8JavaByteArrayToUtf8Bytes(env, name, NULL);
  if (!name_bytes) { throwex_outofmemory(env); return 0; }
  
  if ([func isKindOfClass:[Aggregate class]] && is_iOS_13x_available()) {
    bool isWindow = [func isKindOfClass:[Window class]];
    ret = sqlite3_create_window_function(
                                         gethandle(env, self),
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
                                  gethandle(env, self),
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
#pragma clang diagnostic pop

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_destroy_1function_1utf8(
                                                                             JNIEnv *env, jobject self, jbyteArray name, jint nArgs)
{
  jint ret = 0;
  char* name_bytes;
  
  name_bytes = utf8JavaByteArrayToUtf8Bytes(env, name, NULL);
  if (!name_bytes) { throwex_outofmemory(env); return 0; }
  
  ret = sqlite3_create_function(
                                gethandle(env, self), name_bytes, nArgs, SQLITE_UTF16, 0, 0, 0, 0
                                );
  freeUtf8Bytes(name_bytes);
  
  return ret;
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_free_1functions(
                                                                     JNIEnv *env, jobject self0)
{
  // clean up all the malloc()ed UDFData instances using the
  // linked list stored in DB.udfdatalist
  struct UDFData *udf, *udfpass;
  NativeDB* self = (NativeDB*)self0;
  
  udf = (UDFData*)toref(self->udfdatalist_);
  self->udfdatalist_ = 0;
  
  while (udf) {
    udfpass = udf->next;
    RELEASE_(udf->func);
    free(udf);
    udf = udfpass;
  }
}


// COMPOUND FUNCTIONS ///////////////////////////////////////////////

JNIEXPORT jobjectArray JNICALL Java_org_sqlite_core_NativeDB_column_1metadata(
                                                                              JNIEnv *env, jobject self, jlong stmt)
{
  const char *zTableName, *zColumnName;
  int pNotNull, pPrimaryKey, pAutoinc, i, colCount;
  jobjectArray array;
  jbooleanArray colData;
  sqlite3 *db;
  sqlite3_stmt *dbstmt;
  
  db = gethandle(env, self);
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
  
  dbstmt = to_dbstmt(stmt);
  
  colCount = sqlite3_column_count(dbstmt);
  array = [IOSObjectArray arrayWithLength:colCount type:[IOSBooleanArray class]];
  
  for (i = 0; i < colCount; i++) {
    // load passed column name and table name
    zColumnName = sqlite3_column_name(dbstmt, i);
    zTableName  = sqlite3_column_table_name(dbstmt, i);
    
    pNotNull = 0;
    pPrimaryKey = 0;
    pAutoinc = 0;
    
    colData = [IOSBooleanArray arrayWithLength:3];
    // request metadata for column and load into output variables
    if (zTableName && zColumnName) {
      sqlite3_table_column_metadata(
                                    db, 0, zTableName, zColumnName,
                                    0, 0, &pNotNull, &pPrimaryKey, &pAutoinc
                                    );
    }
    
    // load relevant metadata into 2nd dimension of return results
    *IOSBooleanArray_GetRef(colData, 0) = (jboolean)pNotNull;
    *IOSBooleanArray_GetRef(colData, 1) = (jboolean)pPrimaryKey;
    *IOSBooleanArray_GetRef(colData, 2) = (jboolean)pAutoinc;
    
    [array replaceObjectAtIndex:i withObject:colData];
  }
  
  return array;
}

// backup function

void reportProgress(JNIEnv* env, jobject observer, int remaining, int pageCount) {
  
  if(!observer)
    return;
  
  [((id<OrgSqliteCoreDB_ProgressObserver>)observer) progressWithInt:remaining withInt:pageCount];
  
}


/*
 ** Perform an online backup of database pDb to the database file named
 ** by zFilename. This function copies 5 database pages from pDb to
 ** zFilename, then unlocks pDb and sleeps for 250 ms, then repeats the
 ** process until the entire database is backed up.
 **
 ** The third argument passed to self function must be a pointer to a progress
 ** function. After each set of 5 pages is backed up, the progress function
 ** is invoked with two integer parameters: the number of pages left to
 ** copy, and the total number of pages in the source file. This information
 ** may be used, for example, to update a GUI progress bar.
 **
 ** While self function is running, another thread may use the database pDb, or
 ** another process may access the underlying database file via a separate
 ** connection.
 **
 ** If the backup process is successfully completed, SQLITE_OK is returned.
 ** Otherwise, if an error occurs, an SQLite error code is returned.
 */

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_backup(
    JNIEnv *env, jobject self,
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
  
  pDb = gethandle(env, self);
  if (!pDb)
  {
    throwex_db_closed(env);
    return SQLITE_MISUSE;
  }
  
  dFileName = utf8JavaByteArrayToUtf8Bytes(env, zFilename, NULL);
  if (!dFileName)
  {
    return SQLITE_NOMEM;
  }
  
  dDBName = utf8JavaByteArrayToUtf8Bytes(env, zDBName, NULL);
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
   ** and return the result of self function. */
  (void)sqlite3_close(pFile);
  
  freeUtf8Bytes(dDBName);
  freeUtf8Bytes(dFileName);
  
  return rc;
#else
  return SQLITE_INTERNAL;
#endif
}

JNIEXPORT jint JNICALL Java_org_sqlite_core_NativeDB_restore(
                                                             JNIEnv *env, jobject self,
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
  
  pDb = gethandle(env, self);
  if (!pDb)
  {
    throwex_db_closed(env);
    return SQLITE_MISUSE;
  }
  
  dFileName = utf8JavaByteArrayToUtf8Bytes(env, zFilename, NULL);
  if (!dFileName)
  {
    return SQLITE_NOMEM;
  }
  
  dDBName = utf8JavaByteArrayToUtf8Bytes(env, zDBName, NULL);
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
   ** and return the result of self function. */
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
  ProgressHandler* phandler;
};

static struct ProgressHandlerContext progress_handler_context;

int progress_handler_function(void *ctx) {
  jint rv;
  //    (*env)->AttachCurrentThread(progress_handler_context.vm, (void **)&env, 0);
  rv = [progress_handler_context.phandler progress];
  return rv;
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_register_1progress_1handler(
                                                                                 JNIEnv *env,
                                                                                 jobject self,
                                                                                 jint vmCalls,
                                                                                 jobject progressHandler
                                                                                 )
{
  progress_handler_context.phandler = (ProgressHandler*)RETAIN_(progressHandler);
  sqlite3_progress_handler(gethandle(env, self), vmCalls, &progress_handler_function, NULL);
}

JNIEXPORT void JNICALL Java_org_sqlite_core_NativeDB_clear_1progress_1handler(
                                                                              JNIEnv *env,
                                                                              jobject self
                                                                              )
{
  sqlite3_progress_handler(gethandle(env, self), 0, NULL, NULL);
  RELEASE_(progress_handler_context.phandler);
}
