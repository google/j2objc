package tests.support;

import java.io.IOException;
import java.io.Writer;

/**
 * An implementation of {@code OutputStream} that should serve as the
 * underlying stream for classes to be tested.
 * In particular this implementation allows to have IOExecptions thrown on demand.
 * For simplicity of use and understanding all fields are public.
 */
public class Support_ASimpleWriter extends Writer {

    public static final int DEFAULT_BUFFER_SIZE = 32;

    public char[] buf;

    public int pos;

    public int size;

    // Set to true when exception is wanted:
    public boolean throwExceptionOnNextUse = false;

    public Support_ASimpleWriter() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public Support_ASimpleWriter(boolean throwException) {
        this(DEFAULT_BUFFER_SIZE);
        throwExceptionOnNextUse = throwException;
    }

    public Support_ASimpleWriter(int bufferSize) {
        buf = new char[bufferSize];
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

    @Override
    public void write(char[] src, int offset, int count) throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
        if (offset < 0 || count < 0 || (offset + count) > buf.length) {
            throw new IndexOutOfBoundsException();
        }
        try {
            System.arraycopy(src, offset, buf, pos, count);
            pos += count;
        } catch (IndexOutOfBoundsException e) {
            pos = size;
            throw new IOException("Internal Buffer Overflow");
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
