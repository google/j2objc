/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * Exception thrown by an {@link Executor} when a task cannot be
 * accepted for execution.
 *
 * @since 1.5
 * @author Doug Lea
 */
public class RejectedExecutionException extends RuntimeException {
    private static final long serialVersionUID = -375805702767069545L;

    /**
     * Constructs a {@code RejectedExecutionException} with no detail message.
     * The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause(Throwable) initCause}.
     */
    public RejectedExecutionException() { }

    /**
     * Constructs a {@code RejectedExecutionException} with the
     * specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link
     * #initCause(Throwable) initCause}.
     *
     * @param message the detail message
     */
    public RejectedExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code RejectedExecutionException} with the
     * specified detail message and cause.
     *
     * @param  message the detail message
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method)
     */
    public RejectedExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code RejectedExecutionException} with the
     * specified cause.  The detail message is set to {@code (cause ==
     * null ? null : cause.toString())} (which typically contains
     * the class and detail message of {@code cause}).
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method)
     */
    public RejectedExecutionException(Throwable cause) {
        super(cause);
    }
}
