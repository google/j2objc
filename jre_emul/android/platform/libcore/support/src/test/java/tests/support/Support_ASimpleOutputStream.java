package tests.support;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An implementation of {@code OutputStream} that should serve as the
 * underlying stream for classes to be tested.
 * In particular this implementation allows to have IOExecptions thrown on demand.
 * For simplicity of use and understanding all fields are public.
 */
public class Support_ASimpleOutputStream extends OutputStream {

    public static final int DEFAULT_BUFFER_SIZE = 32;

    public byte[] buf;

    public int pos;

    public int size;

    // Set to true when exception is wanted:
    public boolean throwExceptionOnNextUse = false;

    public Support_ASimpleOutputStream() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public Support_ASimpleOutputStream(boolean throwException) {
        this(DEFAULT_BUFFER_SIZE);
        throwExceptionOnNextUse = throwException;
    }

    public Support_ASimpleOutputStream(int bufferSize) {
        buf = new byte[bufferSize];
        pos = 0;
        size = bufferSize;
    }

    @Override
    public void close() throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
    }

    @Override
    public void flush() throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
    }

//    @Override
//    public void write(byte buffer[]) throws IOException {
//        if (throwExceptionOnNextUse) {
//            throw new IOException("Exception thrown for testing purposes.");
//        }
//        for (int i = 0; i < buffer.length; i++) {
//            write(buffer[i]);
//        }
//    }
//
//    @Override
//    public void write(byte buffer[], int offset, int count) throws IOException {
//        if (throwExceptionOnNextUse) {
//            throw new IOException("Exception thrown for testing purposes.");
//        }
//        if (offset < 0 || count < 0 || (offset + count) > buffer.length) {
//            throw new IndexOutOfBoundsException();
//        }
//        for (int i = offset; i < offset + count; i++) {
//            write(buffer[i]);
//        }
//    }

    @Override
    public void write(int oneByte) throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
        if (pos < size) {
            buf[pos] = (byte)(oneByte & 255);
            pos++;
        } else {
            throw new IOException("Internal buffer overflow.");
        }
    }

    public byte[] toByteArray() {
        byte[] toReturn = new byte[pos];
        System.arraycopy(buf, 0, toReturn, 0, pos);
        return toReturn;
    }

    public String toString() {
        return new String(buf, 0, pos);
    }
}
