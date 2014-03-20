/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

import dalvik.system.CloseGuard;

import java.nio.ByteOrder;
import java.nio.NioUtils;
import java.nio.channels.FileChannel;
import java.nio.charset.ModifiedUtf8;
import java.util.Arrays;

import libcore.io.ErrnoException;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import libcore.io.Memory;
import libcore.io.SizeOf;
import static libcore.io.OsConstants.*;

/**
 * Allows reading from and writing to a file in a random-access manner. This is
 * different from the uni-directional sequential access that a
 * {@link FileInputStream} or {@link FileOutputStream} provides. If the file is
 * opened in read/write mode, write operations are available as well. The
 * position of the next read or write operation can be moved forwards and
 * backwards after every operation.
 */
public class RandomAccessFile implements DataInput, DataOutput, Closeable {
    /**
     * The FileDescriptor representing this RandomAccessFile.
     */
    private FileDescriptor fd;

    private boolean syncMetadata = false;

    // The unique file channel associated with this FileInputStream (lazily
    // initialized).
    private FileChannel channel;

    private int mode;

    private final CloseGuard guard = CloseGuard.get();

    private final byte[] scratch = new byte[8];

    /**
     * Constructs a new {@code RandomAccessFile} based on {@code file} and opens
     * it according to the access string in {@code mode}.
     * <p><a id="accessmode"/>
     * {@code mode} may have one of following values:
     * <table border="0">
     * <tr>
     * <td>{@code "r"}</td>
     * <td>The file is opened in read-only mode. An {@code IOException} is
     * thrown if any of the {@code write} methods is called.</td>
     * </tr>
     * <tr>
     * <td>{@code "rw"}</td>
     * <td>The file is opened for reading and writing. If the file does not
     * exist, it will be created.</td>
     * </tr>
     * <tr>
     * <td>{@code "rws"}</td>
     * <td>The file is opened for reading and writing. Every change of the
     * file's content or metadata must be written synchronously to the target
     * device.</td>
     * </tr>
     * <tr>
     * <td>{@code "rwd"}</td>
     * <td>The file is opened for reading and writing. Every change of the
     * file's content must be written synchronously to the target device.</td>
     * </tr>
     * </table>
     *
     * @param file
     *            the file to open.
     * @param mode
     *            the file access <a href="#accessmode">mode</a>, either {@code
     *            "r"}, {@code "rw"}, {@code "rws"} or {@code "rwd"}.
     * @throws FileNotFoundException
     *             if the file cannot be opened or created according to {@code
     *             mode}.
     * @throws IllegalArgumentException
     *             if {@code mode} is not {@code "r"}, {@code "rw"}, {@code
     *             "rws"} or {@code "rwd"}.
     */
    public RandomAccessFile(File file, String mode) throws FileNotFoundException {
        int flags;
        if (mode.equals("r")) {
            flags = O_RDONLY;
        } else if (mode.equals("rw") || mode.equals("rws") || mode.equals("rwd")) {
            flags = O_RDWR | O_CREAT;
            if (mode.equals("rws")) {
                // Sync file and metadata with every write
                syncMetadata = true;
            } else if (mode.equals("rwd")) {
                // Sync file, but not necessarily metadata
                flags |= O_SYNC;
            }
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        this.mode = flags;
        this.fd = IoBridge.open(file.getAbsolutePath(), flags);

        // if we are in "rws" mode, attempt to sync file+metadata
        if (syncMetadata) {
            try {
                fd.sync();
            } catch (IOException e) {
                // Ignored
            }
        }
        guard.open("close");
    }

    /**
     * Constructs a new {@code RandomAccessFile} based on the file named {@code
     * fileName} and opens it according to the access string in {@code mode}.
     * The file path may be specified absolutely or relative to the system
     * property {@code "user.dir"}.
     *
     * @param fileName
     *            the name of the file to open.
     * @param mode
     *            the file access <a href="#accessmode">mode</a>, either {@code
     *            "r"}, {@code "rw"}, {@code "rws"} or {@code "rwd"}.
     * @throws FileNotFoundException
     *             if the file cannot be opened or created according to {@code
     *             mode}.
     * @throws IllegalArgumentException
     *             if {@code mode} is not {@code "r"}, {@code "rw"}, {@code
     *             "rws"} or {@code "rwd"}.
     */
    public RandomAccessFile(String fileName, String mode) throws FileNotFoundException {
        this(new File(fileName), mode);
    }

    /**
     * Closes this file.
     *
     * @throws IOException
     *             if an error occurs while closing this file.
     */
    public void close() throws IOException {
        guard.close();
        synchronized (this) {
            if (channel != null && channel.isOpen()) {
                channel.close();
                channel = null;
            }
            IoUtils.close(fd);
        }
    }

    @Override protected void finalize() throws Throwable {
        try {
            if (guard != null) {
                guard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Gets this file's {@link FileChannel} object.
     * <p>
     * The file channel's {@link FileChannel#position() position} is the same
     * as this file's file pointer offset (see {@link #getFilePointer()}). Any
     * changes made to this file's file pointer offset are also visible in the
     * file channel's position and vice versa.
     *
     * @return this file's file channel instance.
     */
    public final synchronized FileChannel getChannel() {
        if(channel == null) {
            channel = NioUtils.newFileChannel(this, fd, mode);
        }
        return channel;
    }

    /**
     * Gets this file's {@link FileDescriptor}. This represents the operating
     * system resource for this random access file.
     *
     * @return this file's file descriptor object.
     * @throws IOException
     *             if an error occurs while getting the file descriptor of this
     *             file.
     */
    public final FileDescriptor getFD() throws IOException {
        return fd;
    }

    /**
     * Gets the current position within this file. All reads and
     * writes take place at the current file pointer position.
     *
     * @return the current offset in bytes from the beginning of the file.
     *
     * @throws IOException
     *             if an error occurs while getting the file pointer of this
     *             file.
     */
    public long getFilePointer() throws IOException {
        try {
            return Libcore.os.lseek(fd, 0L, SEEK_CUR);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    /**
     * Returns the length of this file in bytes.
     *
     * @return the file's length in bytes.
     * @throws IOException
     *             if this file is closed or some other I/O error occurs.
     */
    public long length() throws IOException {
        try {
            return Libcore.os.fstat(fd).st_size;
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    /**
     * Reads a single byte from the current position in this file and returns it
     * as an integer in the range from 0 to 255. Returns -1 if the end of the
     * file has been reached. Blocks until one byte has been read, the end of
     * the file is detected, or an exception is thrown.
     *
     * @return the byte read or -1 if the end of the file has been reached.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     */
    public int read() throws IOException {
        return (read(scratch, 0, 1) != -1) ? scratch[0] & 0xff : -1;
    }

    /**
     * Reads bytes from the current position in this file and stores them in the
     * byte array {@code buffer}. The maximum number of bytes read corresponds
     * to the size of {@code buffer}. Blocks until at least one byte has been
     * read, the end of the file is detected, or an exception is thrown.
     * Returns the number of bytes actually read or -1 if the end of the file
     * has been reached. See also {@link #readFully}.
     *
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     */
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    /**
     * Reads up to {@code byteCount} bytes from the current position in this file
     * and stores them in the byte array {@code buffer} starting at {@code
     * byteOffset}. Blocks until at least one byte has been
     * read, the end of the file is detected, or an exception is thrown.
     * Returns the number of bytes actually read or -1 if the end of the stream has been reached.
     * See also {@link #readFully}.
     *
     * @throws IndexOutOfBoundsException
     *     if {@code byteOffset < 0 || byteCount < 0 || byteOffset + byteCount > buffer.length}.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     */
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        return IoBridge.read(fd, buffer, byteOffset, byteCount);
    }

    /**
     * Reads a boolean from the current position in this file. Blocks until one
     * byte has been read, the end of the file is reached or an exception is
     * thrown.
     *
     * @return the next boolean value from this file.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeBoolean(boolean)
     */
    public final boolean readBoolean() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp != 0;
    }

    /**
     * Reads an 8-bit byte from the current position in this file. Blocks until
     * one byte has been read, the end of the file is reached or an exception is
     * thrown.
     *
     * @return the next signed 8-bit byte value from this file.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeBoolean(boolean)
     */
    public final byte readByte() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return (byte) temp;
    }

    /**
     * Reads a big-endian 16-bit character from the current position in this file. Blocks until
     * two bytes have been read, the end of the file is reached or an exception is
     * thrown.
     *
     * @return the next char value from this file.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeChar(int)
     */
    public final char readChar() throws IOException {
        return (char) readShort();
    }

    /**
     * Reads a big-endian 64-bit double from the current position in this file. Blocks
     * until eight bytes have been read, the end of the file is reached or an
     * exception is thrown.
     *
     * @return the next double value from this file.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeDouble(double)
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Reads a big-endian 32-bit float from the current position in this file. Blocks
     * until four bytes have been read, the end of the file is reached or an
     * exception is thrown.
     *
     * @return the next float value from this file.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeFloat(float)
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Equivalent to {@code readFully(dst, 0, dst.length);}.
     */
    public final void readFully(byte[] dst) throws IOException {
        readFully(dst, 0, dst.length);
    }

    /**
     * Reads {@code byteCount} bytes from this stream and stores them in the byte
     * array {@code dst} starting at {@code offset}. If {@code byteCount} is zero, then this
     * method returns without reading any bytes. Otherwise, this method blocks until
     * {@code byteCount} bytes have been read. If insufficient bytes are available,
     * {@code EOFException} is thrown. If an I/O error occurs, {@code IOException} is
     * thrown. When an exception is thrown, some bytes may have been consumed from the stream
     * and written into the array.
     *
     * @param dst
     *            the byte array into which the data is read.
     * @param offset
     *            the offset in {@code dst} at which to store the bytes.
     * @param byteCount
     *            the number of bytes to read.
     * @throws EOFException
     *             if the end of the source stream is reached before enough
     *             bytes have been read.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code byteCount < 0}, or
     *             {@code offset + byteCount > dst.length}.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @throws NullPointerException
     *             if {@code dst} is null.
     */
    public final void readFully(byte[] dst, int offset, int byteCount) throws IOException {
        Arrays.checkOffsetAndCount(dst.length, offset, byteCount);
        while (byteCount > 0) {
            int result = read(dst, offset, byteCount);
            if (result < 0) {
                throw new EOFException();
            }
            offset += result;
            byteCount -= result;
        }
    }

    /**
     * Reads a big-endian 32-bit integer from the current position in this file. Blocks
     * until four bytes have been read, the end of the file is reached or an
     * exception is thrown.
     *
     * @return the next int value from this file.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeInt(int)
     */
    public final int readInt() throws IOException {
        readFully(scratch, 0, SizeOf.INT);
        return Memory.peekInt(scratch, 0, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Reads a line of text form the current position in this file. A line is
     * represented by zero or more characters followed by {@code '\n'}, {@code
     * '\r'}, {@code "\r\n"} or the end of file marker. The string does not
     * include the line terminating sequence.
     * <p>
     * Blocks until a line terminating sequence has been read, the end of the
     * file is reached or an exception is thrown.
     *
     * @return the contents of the line or {@code null} if no characters have
     *         been read before the end of the file has been reached.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     */
    public final String readLine() throws IOException {
        StringBuilder line = new StringBuilder(80); // Typical line length
        boolean foundTerminator = false;
        long unreadPosition = 0;
        while (true) {
            int nextByte = read();
            switch (nextByte) {
                case -1:
                    return line.length() != 0 ? line.toString() : null;
                case (byte) '\r':
                    if (foundTerminator) {
                        seek(unreadPosition);
                        return line.toString();
                    }
                    foundTerminator = true;
                    /* Have to be able to peek ahead one byte */
                    unreadPosition = getFilePointer();
                    break;
                case (byte) '\n':
                    return line.toString();
                default:
                    if (foundTerminator) {
                        seek(unreadPosition);
                        return line.toString();
                    }
                    line.append((char) nextByte);
            }
        }
    }

    /**
     * Reads a big-endian 64-bit long from the current position in this file. Blocks until
     * eight bytes have been read, the end of the file is reached or an
     * exception is thrown.
     *
     * @return the next long value from this file.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeLong(long)
     */
    public final long readLong() throws IOException {
        readFully(scratch, 0, SizeOf.LONG);
        return Memory.peekLong(scratch, 0, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Reads a big-endian 16-bit short from the current position in this file. Blocks until
     * two bytes have been read, the end of the file is reached or an exception
     * is thrown.
     *
     * @return the next short value from this file.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeShort(int)
     */
    public final short readShort() throws IOException {
        readFully(scratch, 0, SizeOf.SHORT);
        return Memory.peekShort(scratch, 0, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Reads an unsigned 8-bit byte from the current position in this file and
     * returns it as an integer. Blocks until one byte has been read, the end of
     * the file is reached or an exception is thrown.
     *
     * @return the next unsigned byte value from this file as an int.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeByte(int)
     */
    public final int readUnsignedByte() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp;
    }

    /**
     * Reads an unsigned big-endian 16-bit short from the current position in this file and
     * returns it as an integer. Blocks until two bytes have been read, the end of
     * the file is reached or an exception is thrown.
     *
     * @return the next unsigned short value from this file as an int.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #writeShort(int)
     */
    public final int readUnsignedShort() throws IOException {
        return ((int) readShort()) & 0xffff;
    }

    /**
     * Reads a string that is encoded in {@link DataInput modified UTF-8} from
     * this file. The number of bytes that must be read for the complete string
     * is determined by the first two bytes read from the file. Blocks until all
     * required bytes have been read, the end of the file is reached or an
     * exception is thrown.
     *
     * @return the next string encoded in {@link DataInput modified UTF-8} from
     *         this file.
     * @throws EOFException
     *             if the end of this file is detected.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @throws UTFDataFormatException
     *             if the bytes read cannot be decoded into a character string.
     * @see #writeUTF(String)
     */
    public final String readUTF() throws IOException {
        int utfSize = readUnsignedShort();
        if (utfSize == 0) {
            return "";
        }
        byte[] buf = new byte[utfSize];
        if (read(buf, 0, buf.length) != buf.length) {
            throw new EOFException();
        }
        return ModifiedUtf8.decode(buf, new char[utfSize], 0, utfSize);
    }

    /**
     * Moves this file's file pointer to a new position, from where following
     * {@code read}, {@code write} or {@code skip} operations are done. The
     * position may be greater than the current length of the file, but the
     * file's length will only change if the moving of the pointer is followed
     * by a {@code write} operation.
     *
     * @param offset
     *            the new file pointer position.
     * @throws IOException
     *             if this file is closed, {@code pos < 0} or another I/O error
     *             occurs.
     */
    public void seek(long offset) throws IOException {
        if (offset < 0) {
            throw new IOException("offset < 0: " + offset);
        }
        try {
            Libcore.os.lseek(fd, offset, SEEK_SET);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    /**
     * Sets the length of this file to {@code newLength}. If the current file is
     * smaller, it is expanded but the contents from the previous end of the
     * file to the new end are undefined. The file is truncated if its current
     * size is bigger than {@code newLength}. If the current file pointer
     * position is in the truncated part, it is set to the end of the file.
     *
     * @param newLength
     *            the new file length in bytes.
     * @throws IllegalArgumentException
     *             if {@code newLength < 0}.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     */
    public void setLength(long newLength) throws IOException {
        if (newLength < 0) {
            throw new IllegalArgumentException("newLength < 0");
        }
        try {
            Libcore.os.ftruncate(fd, newLength);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }

        long filePointer = getFilePointer();
        if (filePointer > newLength) {
            seek(newLength);
        }

        // if we are in "rws" mode, attempt to sync file+metadata
        if (syncMetadata) {
            fd.sync();
        }
    }

    /**
     * Skips over {@code count} bytes in this file. Less than {@code count}
     * bytes are skipped if the end of the file is reached or an exception is
     * thrown during the operation. Nothing is done if {@code count} is
     * negative.
     *
     * @param count
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     */
    public int skipBytes(int count) throws IOException {
        if (count > 0) {
            long currentPos = getFilePointer(), eof = length();
            int newCount = (int) ((currentPos + count > eof) ? eof - currentPos : count);
            seek(currentPos + newCount);
            return newCount;
        }
        return 0;
    }

    /**
     * Writes the entire contents of the byte array {@code buffer} to this file,
     * starting at the current file pointer.
     *
     * @param buffer
     *            the buffer to write.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     */
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes {@code byteCount} bytes from the byte array {@code buffer} to this
     * file, starting at the current file pointer and using {@code byteOffset} as
     * the first position within {@code buffer} to get bytes.
     *
     * @throws IndexOutOfBoundsException
     *             if {@code byteCount < 0}, {@code byteOffset < 0} or {@code byteCount +
     *             byteOffset} is greater than the size of {@code buffer}.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     */
    public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        IoBridge.write(fd, buffer, byteOffset, byteCount);
        // if we are in "rws" mode, attempt to sync file+metadata
        if (syncMetadata) {
            fd.sync();
        }
    }

    /**
     * Writes a byte to this file, starting at the current file pointer. Only
     * the least significant byte of the integer {@code oneByte} is written.
     *
     * @param oneByte
     *            the byte to write to this file.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #read()
     */
    public void write(int oneByte) throws IOException {
        scratch[0] = (byte) (oneByte & 0xff);
        write(scratch, 0, 1);
    }

    /**
     * Writes a boolean to this file as a single byte (1 for true, 0 for false), starting at the
     * current file pointer.
     *
     * @param val
     *            the boolean to write to this file.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #readBoolean()
     */
    public final void writeBoolean(boolean val) throws IOException {
        write(val ? 1 : 0);
    }

    /**
     * Writes an 8-bit byte to this file, starting at the current file pointer.
     * Only the least significant byte of the integer {@code val} is written.
     *
     * @param val
     *            the byte to write to this file.
     * @throws IOException
     *             if this file is closed or another I/O error occurs.
     * @see #readByte()
     * @see #readUnsignedByte()
     */
    public final void writeByte(int val) throws IOException {
        write(val & 0xFF);
    }

    /**
     * Writes the low order 8-bit bytes from a string to this file, starting at
     * the current file pointer.
     *
     * @param str
     *            the string containing the bytes to write to this file
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     */
    public final void writeBytes(String str) throws IOException {
        byte[] bytes = new byte[str.length()];
        for (int index = 0; index < str.length(); index++) {
            bytes[index] = (byte) (str.charAt(index) & 0xFF);
        }
        write(bytes);
    }

    /**
     * Writes a big-endian 16-bit character to this file, starting at the current file
     * pointer. Only the two least significant bytes of the integer {@code val}
     * are written, with the high byte first.
     *
     * @param val
     *            the char to write to this file.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     * @see #readChar()
     */
    public final void writeChar(int val) throws IOException {
        writeShort(val);
    }

    /**
     * Writes big-endian 16-bit characters from {@code str} to this file, starting at the
     * current file pointer.
     *
     * @param str
     *            the string to write to this file.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     * @see #readChar()
     */
    public final void writeChars(String str) throws IOException {
        write(str.getBytes("UTF-16BE"));
    }

    /**
     * Writes a big-endian 64-bit double to this file, starting at the current file
     * pointer. The bytes are those returned by
     * {@link Double#doubleToLongBits(double)}, meaning a canonical NaN is used.
     *
     * @param val
     *            the double to write to this file.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     * @see #readDouble()
     */
    public final void writeDouble(double val) throws IOException {
        writeLong(Double.doubleToLongBits(val));
    }

    /**
     * Writes a big-endian 32-bit float to this file, starting at the current file pointer.
     * The bytes are those returned by {@link Float#floatToIntBits(float)}, meaning a canonical NaN
     * is used.
     *
     * @param val
     *            the float to write to this file.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     * @see #readFloat()
     */
    public final void writeFloat(float val) throws IOException {
        writeInt(Float.floatToIntBits(val));
    }

    /**
     * Writes a big-endian 32-bit integer to this file, starting at the current file
     * pointer.
     *
     * @param val
     *            the int to write to this file.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     * @see #readInt()
     */
    public final void writeInt(int val) throws IOException {
        Memory.pokeInt(scratch, 0, val, ByteOrder.BIG_ENDIAN);
        write(scratch, 0, SizeOf.INT);
    }

    /**
     * Writes a big-endian 64-bit long to this file, starting at the current file
     * pointer.
     *
     * @param val
     *            the long to write to this file.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     * @see #readLong()
     */
    public final void writeLong(long val) throws IOException {
        Memory.pokeLong(scratch, 0, val, ByteOrder.BIG_ENDIAN);
        write(scratch, 0, SizeOf.LONG);
    }

    /**
     * Writes a big-endian 16-bit short to this file, starting at the current file
     * pointer. Only the two least significant bytes of the integer {@code val}
     * are written.
     *
     * @param val
     *            the short to write to this file.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     * @see #readShort()
     * @see DataInput#readUnsignedShort()
     */
    public final void writeShort(int val) throws IOException {
        Memory.pokeShort(scratch, 0, (short) val, ByteOrder.BIG_ENDIAN);
        write(scratch, 0, SizeOf.SHORT);
    }

    /**
     * Writes a string encoded with {@link DataInput modified UTF-8} to this
     * file, starting at the current file pointer.
     *
     * @param str
     *            the string to write in {@link DataInput modified UTF-8}
     *            format.
     * @throws IOException
     *             if an I/O error occurs while writing to this file.
     * @throws UTFDataFormatException
     *             if the encoded string is longer than 65535 bytes.
     * @see #readUTF()
     */
    public final void writeUTF(String str) throws IOException {
        write(ModifiedUtf8.encode(str));
    }
}
