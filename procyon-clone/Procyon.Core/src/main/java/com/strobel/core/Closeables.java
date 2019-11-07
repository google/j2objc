package com.strobel.core;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Mike Strobel
 */
public final class Closeables {
    private final static SafeCloseable EMPTY = new SafeCloseable() {
        @Override
        public void close() {
        }
    };

    public static SafeCloseable empty() {
        return EMPTY;
    }

    public static SafeCloseable create(final Runnable delegate) {
        return new AnonymousCloseable(VerifyArgument.notNull(delegate, "delegate"));
    }

    public static void close(final AutoCloseable closeable) {
        try {
            closeable.close();
        }
        catch (java.lang.Error | RuntimeException e) {
            throw e;
        }
        catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }

    public static void close(final AutoCloseable... closeables) {
        for (final AutoCloseable closeable : closeables) {
            close(closeable);
        }
    }

    public static void tryClose(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (Exception ignored) {
            }
        }
    }

    public static void tryClose(final AutoCloseable... closeables) {
        if (closeables != null) {
            for (final AutoCloseable closeable : closeables) {
                tryClose(closeable);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="AnonymousCloseable Class">

    private final static class AnonymousCloseable implements SafeCloseable {
        private final static AtomicIntegerFieldUpdater<AnonymousCloseable> CLOSED_UPDATER = AtomicIntegerFieldUpdater.newUpdater(
            AnonymousCloseable.class,
            "_closed"
        );

        private final Runnable _delegate;
        @SuppressWarnings("UnusedDeclaration")
        private volatile int _closed;

        private AnonymousCloseable(final Runnable delegate) {
            _delegate = VerifyArgument.notNull(delegate, "delegate");
        }

        @Override
        public void close() {
            if (CLOSED_UPDATER.getAndSet(this, 1) == 0) {
                _delegate.run();
            }
        }
    }

    // </editor-fold>
}
