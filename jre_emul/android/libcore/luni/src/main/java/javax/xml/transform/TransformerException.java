/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// $Id: TransformerException.java 569994 2007-08-27 04:28:57Z mrglavas $

package javax.xml.transform;

/**
 * This class specifies an exceptional condition that occurred
 * during the transformation process.
 */
public class TransformerException extends Exception {

    // Added serialVersionUID to preserve binary compatibility
    private static final long serialVersionUID = 975798773772956428L;

    /** Field locator specifies where the error occurred */
    SourceLocator locator;

    /**
     * Method getLocator retrieves an instance of a SourceLocator
     * object that specifies where an error occurred.
     *
     * @return A SourceLocator object, or null if none was specified.
     */
    public SourceLocator getLocator() {
        return locator;
    }

    /**
     * Method setLocator sets an instance of a SourceLocator
     * object that specifies where an error occurred.
     *
     * @param location A SourceLocator object, or null to clear the location.
     */
    public void setLocator(SourceLocator location) {
        locator = location;
    }

    /** Field containedException specifies a wrapped exception.  May be null. */
    Throwable containedException;

    /**
     * This method retrieves an exception that this exception wraps.
     *
     * @return An Throwable object, or null.
     * @see #getCause
     */
    public Throwable getException() {
        return containedException;
    }

    /**
     * Returns the cause of this throwable or <code>null</code> if the
     * cause is nonexistent or unknown.  (The cause is the throwable that
     * caused this throwable to get thrown.)
     */
    public Throwable getCause() {

        return ((containedException == this)
                ? null
                : containedException);
    }

    /**
     * Initializes the <i>cause</i> of this throwable to the specified value.
     * (The cause is the throwable that caused this throwable to get thrown.)
     *
     * <p>This method can be called at most once.  It is generally called from
     * within the constructor, or immediately after creating the
     * throwable.  If this throwable was created
     * with {@link #TransformerException(Throwable)} or
     * {@link #TransformerException(String,Throwable)}, this method cannot be called
     * even once.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @return  a reference to this <code>Throwable</code> instance.
     * @throws IllegalArgumentException if <code>cause</code> is this
     *         throwable.  (A throwable cannot
     *         be its own cause.)
     * @throws IllegalStateException if this throwable was
     *         created with {@link #TransformerException(Throwable)} or
     *         {@link #TransformerException(String,Throwable)}, or this method has already
     *         been called on this throwable.
     */
    public synchronized Throwable initCause(Throwable cause) {

        if (this.containedException != null) {
            throw new IllegalStateException("Can't overwrite cause");
        }

        if (cause == this) {
            throw new IllegalArgumentException(
                "Self-causation not permitted");
        }

        this.containedException = cause;

        return this;
    }

    /**
     * Create a new TransformerException.
     *
     * @param message The error or warning message.
     */
    public TransformerException(String message) {

        super(message);

        this.containedException = null;
        this.locator            = null;
    }

    /**
     * Create a new TransformerException wrapping an existing exception.
     *
     * @param e The exception to be wrapped.
     */
    public TransformerException(Throwable e) {

        super(e.toString());

        this.containedException = e;
        this.locator            = null;
    }

    /**
     * Wrap an existing exception in a TransformerException.
     *
     * <p>This is used for throwing processor exceptions before
     * the processing has started.</p>
     *
     * @param message The error or warning message, or null to
     *                use the message from the embedded exception.
     * @param e Any exception
     */
    public TransformerException(String message, Throwable e) {

        super(((message == null) || (message.length() == 0))
              ? e.toString()
              : message);

        this.containedException = e;
        this.locator            = null;
    }

    /**
     * Create a new TransformerException from a message and a Locator.
     *
     * <p>This constructor is especially useful when an application is
     * creating its own exception from within a DocumentHandler
     * callback.</p>
     *
     * @param message The error or warning message.
     * @param locator The locator object for the error or warning.
     */
    public TransformerException(String message, SourceLocator locator) {

        super(message);

        this.containedException = null;
        this.locator            = locator;
    }

    /**
     * Wrap an existing exception in a TransformerException.
     *
     * @param message The error or warning message, or null to
     *                use the message from the embedded exception.
     * @param locator The locator object for the error or warning.
     * @param e Any exception
     */
    public TransformerException(String message, SourceLocator locator,
                                Throwable e) {

        super(message);

        this.containedException = e;
        this.locator            = locator;
    }

    /**
     * Get the error message with location information
     * appended.
     *
     * @return A <code>String</code> representing the error message with
     *         location information appended.
     */
    public String getMessageAndLocation() {

        StringBuilder sbuffer = new StringBuilder();
        String       message = super.getMessage();

        if (null != message) {
            sbuffer.append(message);
        }

        if (null != locator) {
            String systemID = locator.getSystemId();
            int    line     = locator.getLineNumber();
            int    column   = locator.getColumnNumber();

            if (null != systemID) {
                sbuffer.append("; SystemID: ");
                sbuffer.append(systemID);
            }

            if (0 != line) {
                sbuffer.append("; Line#: ");
                sbuffer.append(line);
            }

            if (0 != column) {
                sbuffer.append("; Column#: ");
                sbuffer.append(column);
            }
        }

        return sbuffer.toString();
    }

    /**
     * Get the location information as a string.
     *
     * @return A string with location info, or null
     * if there is no location information.
     */
    public String getLocationAsString() {

        if (null != locator) {
            StringBuilder sbuffer  = new StringBuilder();
            String       systemID = locator.getSystemId();
            int          line     = locator.getLineNumber();
            int          column   = locator.getColumnNumber();

            if (null != systemID) {
                sbuffer.append("; SystemID: ");
                sbuffer.append(systemID);
            }

            if (0 != line) {
                sbuffer.append("; Line#: ");
                sbuffer.append(line);
            }

            if (0 != column) {
                sbuffer.append("; Column#: ");
                sbuffer.append(column);
            }

            return sbuffer.toString();
        } else {
            return null;
        }
    }

    /**
     * Print the the trace of methods from where the error
     * originated.  This will trace all nested exception
     * objects, as well as this object.
     */
    public void printStackTrace() {
        printStackTrace(new java.io.PrintWriter(System.err, true));
    }

    /**
     * Print the the trace of methods from where the error
     * originated.  This will trace all nested exception
     * objects, as well as this object.
     * @param s The stream where the dump will be sent to.
     */
    public void printStackTrace(java.io.PrintStream s) {
        printStackTrace(new java.io.PrintWriter(s));
    }

    /**
     * Print the the trace of methods from where the error
     * originated.  This will trace all nested exception
     * objects, as well as this object.
     * @param s The writer where the dump will be sent to.
     */
    public void printStackTrace(java.io.PrintWriter s) {

        if (s == null) {
            s = new java.io.PrintWriter(System.err, true);
        }

        try {
            String locInfo = getLocationAsString();

            if (null != locInfo) {
                s.println(locInfo);
            }

            super.printStackTrace(s);
        } catch (Throwable e) {}
    }
}
