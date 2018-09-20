/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.java.io;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

/** 
 * A PrintStream that throws away its output.
 */
public final class NullPrintStream extends PrintStream {
    public NullPrintStream() {
        // super class complains if argument is null
        super((OutputStream) new ByteArrayOutputStream());
    }
    public boolean checkError() { return false; }
    protected void clearError() {}
    public void close() {}
    public void flush() {}
    public PrintStream format(String format, Object... args) { return this; }
    public PrintStream format(Locale l, String format, Object... args) { return this; }
    public PrintStream printf(String format, Object... args) { return this; }
    public PrintStream printf(Locale l, String format, Object... args) { return this; }
    public void print(char[] charArray) {}
    public void print(char ch) {}
    public void print(double dnum) {}
    public void print(float fnum) {}
    public void print(int inum) {}
    public void print(long lnum) {}
    public void print(Object obj) {}
    public void print(String str) {}
    public void print(boolean bool) {}
    public void println() {}
    public void println(char[] charArray) {}
    public void println(char ch) {}
    public void println(double dnum) {}
    public void println(float fnum) {}
    public void println(int inum) {}
    public void println(long lnum) {}
    public void println(Object obj) {}
    public void println(String str) {}
    public void println(boolean bool) {}
    protected void setError() {}
    public void write(byte[] buffer, int offset, int length) {}
    public void write(int oneByte) {}
    public PrintStream append(char c) { return this; }
    public PrintStream append(CharSequence csq) { return this; }
    public PrintStream append(CharSequence csq, int start, int end) { return this; }
}
