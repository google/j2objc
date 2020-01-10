/*
 * Copyright (c) 2007 David Crawshaw <david@zentus.com>
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.sqlite.core;

import org.sqlite.BusyHandler;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.sqlite.Function;
import org.sqlite.ProgressHandler;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteJDBCLoader;

/** This class provides a thin JNI layer over the SQLite3 C API. */
public final class NativeDB extends DB
{
    /** SQLite connection handle. */
    long                   pointer       = 0;

    private static boolean isLoaded;
    private static boolean loadSucceeded;

    static {
        if ("The Android Project".equals(System.getProperty("java.vm.vendor"))) {
            System.loadLibrary("sqlitejdbc");
            isLoaded = true;
            loadSucceeded = true;
        } else {
            // continue with non Android execution path
            isLoaded = false;
            loadSucceeded = false;
        }
    }

    public NativeDB(String url, String fileName, SQLiteConfig config)
            throws SQLException
    {
        super(url, fileName, config);
    }

    /**
     * Loads the SQLite interface backend.
     * @return True if the SQLite JDBC driver is successfully loaded; false otherwise.
     */
    public static boolean load() throws Exception {
        if (isLoaded)
            return loadSucceeded == true;

        loadSucceeded = SQLiteJDBCLoader.initialize();
        isLoaded = true;
        return loadSucceeded;
    }

    /** linked list of all instanced UDFDatas */
    private final long udfdatalist = 0;

    // WRAPPER FUNCTIONS ////////////////////////////////////////////

    /**
     * @see org.sqlite.core.DB#_open(java.lang.String, int)
     */
    @Override
    protected synchronized void _open(String file, int openFlags) throws SQLException {
        _open_utf8(stringToUtf8ByteArray(file), openFlags);
    }

    native synchronized void _open_utf8(byte[] fileUtf8, int openFlags) throws SQLException;

    /**
     * @see org.sqlite.core.DB#_close()
     */
    @Override
    protected native synchronized void _close() throws SQLException;

    /**
     * @see org.sqlite.core.DB#_exec(java.lang.String)
     */
    @Override
    public synchronized int _exec(String sql) throws SQLException {
        return _exec_utf8(stringToUtf8ByteArray(sql));
    }

    native synchronized int _exec_utf8(byte[] sqlUtf8) throws SQLException;

    /**
     * @see org.sqlite.core.DB#shared_cache(boolean)
     */
    @Override
    public native synchronized int shared_cache(boolean enable);

    /**
     * @see org.sqlite.core.DB#enable_load_extension(boolean)
     */
    @Override
    public native synchronized int enable_load_extension(boolean enable);

    /**
     * @see org.sqlite.core.DB#interrupt()
     */
    @Override
    public native void interrupt();

    /**
     * @see org.sqlite.core.DB#busy_timeout(int)
     */
    @Override
    public native synchronized void busy_timeout(int ms);
    
    /**
     * @see org.sqlite.core.DB#busy_handler(BusyHandler)
     */
    @Override
    public native synchronized void busy_handler(BusyHandler busyHandler);

    /**
     * @see org.sqlite.core.DB#prepare(java.lang.String)
     */
    @Override
    protected synchronized long prepare(String sql) throws SQLException {
        return prepare_utf8(stringToUtf8ByteArray(sql));
    }

    native synchronized long prepare_utf8(byte[] sqlUtf8) throws SQLException;

    /**
     * @see org.sqlite.core.DB#errmsg()
     */
    @Override
    synchronized String errmsg() {
        return utf8ByteArrayToString(errmsg_utf8());
    }

    native synchronized byte[] errmsg_utf8();

    /**
     * @see org.sqlite.core.DB#libversion()
     */
    @Override
    public synchronized String libversion() {
        return utf8ByteArrayToString(libversion_utf8());
    }

    native byte[] libversion_utf8();

    /**
     * @see org.sqlite.core.DB#changes()
     */
    @Override
    public native synchronized int changes();

    /**
     * @see org.sqlite.core.DB#total_changes()
     */
    @Override
    public native synchronized int total_changes();

    /**
     * @see org.sqlite.core.DB#finalize(long)
     */
    @Override
    protected native synchronized int finalize(long stmt);

    /**
     * @see org.sqlite.core.DB#step(long)
     */
    @Override
    public native synchronized int step(long stmt);

    /**
     * @see org.sqlite.core.DB#reset(long)
     */
    @Override
    public native synchronized int reset(long stmt);

    /**
     * @see org.sqlite.core.DB#clear_bindings(long)
     */
    @Override
    public native synchronized int clear_bindings(long stmt);

    /**
     * @see org.sqlite.core.DB#bind_parameter_count(long)
     */
    @Override
    native synchronized int bind_parameter_count(long stmt);

    /**
     * @see org.sqlite.core.DB#column_count(long)
     */
    @Override
    public native synchronized int column_count(long stmt);

    /**
     * @see org.sqlite.core.DB#column_type(long, int)
     */
    @Override
    public native synchronized int column_type(long stmt, int col);

    /**
     * @see org.sqlite.core.DB#column_decltype(long, int)
     */
    @Override
    public synchronized String column_decltype(long stmt, int col) {
        return utf8ByteArrayToString(column_decltype_utf8(stmt, col));
    }

    native synchronized byte[] column_decltype_utf8(long stmt, int col);

    /**
     * @see org.sqlite.core.DB#column_table_name(long, int)
     */
    @Override
    public synchronized String column_table_name(long stmt, int col) {
        return utf8ByteArrayToString(column_table_name_utf8(stmt, col));
    }

    native synchronized byte[] column_table_name_utf8(long stmt, int col);

    /**
     * @see org.sqlite.core.DB#column_name(long, int)
     */
    @Override
    public synchronized String column_name(long stmt, int col)
    {
        return utf8ByteArrayToString(column_name_utf8(stmt, col));
    }

    native synchronized byte[] column_name_utf8(long stmt, int col);

    /**
     * @see org.sqlite.core.DB#column_text(long, int)
     */
    @Override
    public synchronized String column_text(long stmt, int col) {
        return utf8ByteArrayToString(column_text_utf8(stmt, col));
    }

    native synchronized byte[] column_text_utf8(long stmt, int col);

    /**
     * @see org.sqlite.core.DB#column_blob(long, int)
     */
    @Override
    public native synchronized byte[] column_blob(long stmt, int col);

    /**
     * @see org.sqlite.core.DB#column_double(long, int)
     */
    @Override
    public native synchronized double column_double(long stmt, int col);

    /**
     * @see org.sqlite.core.DB#column_long(long, int)
     */
    @Override
    public native synchronized long column_long(long stmt, int col);

    /**
     * @see org.sqlite.core.DB#column_int(long, int)
     */
    @Override
    public native synchronized int column_int(long stmt, int col);

    /**
     * @see org.sqlite.core.DB#bind_null(long, int)
     */
    @Override
    native synchronized int bind_null(long stmt, int pos);

    /**
     * @see org.sqlite.core.DB#bind_int(long, int, int)
     */
    @Override
    native synchronized int bind_int(long stmt, int pos, int v);

    /**
     * @see org.sqlite.core.DB#bind_long(long, int, long)
     */
    @Override
    native synchronized int bind_long(long stmt, int pos, long v);

    /**
     * @see org.sqlite.core.DB#bind_double(long, int, double)
     */
    @Override
    native synchronized int bind_double(long stmt, int pos, double v);

    /**
     * @see org.sqlite.core.DB#bind_text(long, int, java.lang.String)
     */
    @Override
    synchronized int bind_text(long stmt, int pos, String v) {
        return bind_text_utf8(stmt, pos, stringToUtf8ByteArray(v));
    }

    native synchronized int bind_text_utf8(long stmt, int pos, byte[] vUtf8);

    /**
     * @see org.sqlite.core.DB#bind_blob(long, int, byte[])
     */
    @Override
    native synchronized int bind_blob(long stmt, int pos, byte[] v);

    /**
     * @see org.sqlite.core.DB#result_null(long)
     */
    @Override
    public native synchronized void result_null(long context);

    /**
     * @see org.sqlite.core.DB#result_text(long, java.lang.String)
     */
    @Override
    public synchronized void result_text(long context, String val) {
        result_text_utf8(context, stringToUtf8ByteArray(val));
    }

    native synchronized void result_text_utf8(long context, byte[] valUtf8);

    /**
     * @see org.sqlite.core.DB#result_blob(long, byte[])
     */
    @Override
    public native synchronized void result_blob(long context, byte[] val);

    /**
     * @see org.sqlite.core.DB#result_double(long, double)
     */
    @Override
    public native synchronized void result_double(long context, double val);

    /**
     * @see org.sqlite.core.DB#result_long(long, long)
     */
    @Override
    public native synchronized void result_long(long context, long val);

    /**
     * @see org.sqlite.core.DB#result_int(long, int)
     */
    @Override
    public native synchronized void result_int(long context, int val);

    /**
     * @see org.sqlite.core.DB#result_error(long, java.lang.String)
     */
    @Override
    public synchronized void result_error(long context, String err) {
        result_error_utf8(context, stringToUtf8ByteArray(err));
    }

    native synchronized void result_error_utf8(long context, byte[] errUtf8);

    /**
     * @see org.sqlite.core.DB#value_text(org.sqlite.Function, int)
     */
    @Override
    public synchronized String value_text(Function f, int arg) {
        return utf8ByteArrayToString(value_text_utf8(f, arg));
    }

    native synchronized byte[] value_text_utf8(Function f, int argUtf8);

    /**
     * @see org.sqlite.core.DB#value_blob(org.sqlite.Function, int)
     */
    @Override
    public native synchronized byte[] value_blob(Function f, int arg);

    /**
     * @see org.sqlite.core.DB#value_double(org.sqlite.Function, int)
     */
    @Override
    public native synchronized double value_double(Function f, int arg);

    /**
     * @see org.sqlite.core.DB#value_long(org.sqlite.Function, int)
     */
    @Override
    public native synchronized long value_long(Function f, int arg);

    /**
     * @see org.sqlite.core.DB#value_int(org.sqlite.Function, int)
     */
    @Override
    public native synchronized int value_int(Function f, int arg);

    /**
     * @see org.sqlite.core.DB#value_type(org.sqlite.Function, int)
     */
    @Override
    public native synchronized int value_type(Function f, int arg);

    /**
     * @see org.sqlite.core.DB#create_function(java.lang.String, org.sqlite.Function, int)
     */
    @Override
    public synchronized int create_function(String name, Function func, int flags) {
        return create_function_utf8(stringToUtf8ByteArray(name), func, flags);
    }

    native synchronized int create_function_utf8(byte[] nameUtf8, Function func, int flags);

    /**
     * @see org.sqlite.core.DB#destroy_function(java.lang.String)
     */
    @Override
    public synchronized int destroy_function(String name) {
        return destroy_function_utf8(stringToUtf8ByteArray(name));
    }

    native synchronized int destroy_function_utf8(byte[] nameUtf8);

    /**
     * @see org.sqlite.core.DB#free_functions()
     */
    @Override
    native synchronized void free_functions();

    /**
     * @see org.sqlite.core.DB#backup(java.lang.String, java.lang.String, org.sqlite.core.DB.ProgressObserver)
     */
    @Override
    public int backup(String dbName, String destFileName, ProgressObserver observer) throws SQLException {
        return backup(stringToUtf8ByteArray(dbName), stringToUtf8ByteArray(destFileName), observer);
    }

    native synchronized int backup(byte[] dbNameUtf8, byte[] destFileNameUtf8,
            ProgressObserver observer) throws SQLException;

    /**
     * @see org.sqlite.core.DB#restore(java.lang.String, java.lang.String,
     *      org.sqlite.core.DB.ProgressObserver)
     */
    @Override
    public synchronized int restore(String dbName, String sourceFileName, ProgressObserver observer)
            throws SQLException {

        return restore(stringToUtf8ByteArray(dbName), stringToUtf8ByteArray(sourceFileName), observer);
    }

    native synchronized int restore(byte[] dbNameUtf8, byte[] sourceFileName,
            ProgressObserver observer) throws SQLException;

    // COMPOUND FUNCTIONS (for optimisation) /////////////////////////

    /**
     * Provides metadata for table columns.
     * @returns For each column returns: <br/>
     * res[col][0] = true if column constrained NOT NULL<br/>
     * res[col][1] = true if column is part of the primary key<br/>
     * res[col][2] = true if column is auto-increment.
     * @see org.sqlite.core.DB#column_metadata(long)
     */
    @Override
    native synchronized boolean[][] column_metadata(long stmt);

    /**
     * Throws an SQLException
     * @param msg Message for the SQLException.
     * @throws SQLException
     */
    static void throwex(String msg) throws SQLException {
        throw new SQLException(msg);
    }

    static byte[] stringToUtf8ByteArray(String str) {
        if (str == null) {
            return null;
        }
        try {
            return str.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is not supported", e);
        }
    }

    static String utf8ByteArrayToString(byte[] utf8bytes) {
        if (utf8bytes == null) {
            return null;
        }
        try {
            return new String(utf8bytes, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is not supported", e);
        }
    }

    public native synchronized void register_progress_handler(int vmCalls, ProgressHandler progressHandler) throws SQLException;

    public native synchronized void clear_progress_handler() throws SQLException;
}
