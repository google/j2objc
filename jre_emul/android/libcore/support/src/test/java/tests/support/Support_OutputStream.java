package tests.support;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An implementation of {@code OutputStream} that stores the written data in a
 * byte array of fixed size. As a special feature, instances of this class can
 * be instructed to throw an {@code IOException} whenever a method is called.
 * This is used to test the {@code IOException} handling of classes that write
 * to an {@code OutputStream}.
 */
public class Support_OutputStream extends OutputStream {

    private static final int DEFAULT_BUFFER_SIZE = 32;

    private byte[] buffer;

    private int position;

    private int size;

    private boolean throwsException;

    public Support_OutputStream() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public Support_OutputStream(boolean throwException) {
        this(DEFAULT_BUFFER_SIZE);
        throwsException = throwException;
    }

    public Support_OutputStream(int bufferSize) {
        buffer = new byte[bufferSize];
        position = 0;
        size = bufferSize;
        throwsException = false;
    }

    @Override
    public void close() throws IOException {
        if (throwsException) {
            throw new IOException("Exception thrown for testing purposes.");
        }
        super.close();
    }

    @Override
    public void flush() throws IOException {
        if (throwsException) {
            throw new IOException("Exception thrown for testing purposes.");
        }
        super.flush();
    }

    @Override
    public void write(byte buffer[]) throws IOException {
        if (throwsException) {
            throw new IOException("Exception thrown for testing purposes.");
        }
        for (int i = 0; i < buffer.length; i++) {
            write(buffer[i]);
        }
    }

    @Override
    public void write(byte buffer[], int offset, int count) throws IOException {
        if (throwsException) {
            throw new IOException("Exception thrown for testing purposes.");
        }
        if (offset < 0 || count < 0 || (offset + count) > buffer.length) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = offset; i < offset + count; i++) {
            write(buffer[i]);
        }
    }

    @Override
    public void write(int oneByte) throws IOException {
        if (throwsException) {
            throw new IOException("Exception thrown for testing purposes.");
        }
        if (position < size) {
            buffer[position] = (byte)(oneByte & 255);
            position++;
        } else {
            throw new IOException("Internal buffer overflow.");
        }
    }

    public byte[] toByteArray() {
        byte[] toReturn = new byte[position];
        System.arraycopy(buffer, 0, toReturn, 0, position);
        return toReturn;
    }

    public String toString() {
        return new String(buffer, 0, position);
    }

    public int size() {
        return position;
    }

    public void setThrowsException(boolean newValue) {
        throwsException = newValue;
    }
}
