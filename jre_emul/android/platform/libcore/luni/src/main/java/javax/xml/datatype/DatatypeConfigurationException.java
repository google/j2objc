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

// $Id: DatatypeConfigurationException.java 569987 2007-08-27 04:08:46Z mrglavas $

package javax.xml.datatype;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * <p>Indicates a serious configuration error.</p>
 *
 * @author <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 569987 $, $Date: 2007-08-26 21:08:46 -0700 (Sun, 26 Aug 2007) $
 * @since 1.5
 */

public class DatatypeConfigurationException extends Exception {

    /** Stream Unique Identifier. */
    private static final long serialVersionUID = -1699373159027047238L;

    /** This field is required to store the cause on JDK 1.3 and below. */
    private Throwable causeOnJDK13OrBelow;

    /** Indicates whether this class is being used in a JDK 1.4 context. */
    private transient boolean isJDK14OrAbove = false;

    /**
     * <p>Create a new <code>DatatypeConfigurationException</code> with
     * no specified detail message and cause.</p>
     */

    public DatatypeConfigurationException() {
    }

    /**
     * <p>Create a new <code>DatatypeConfigurationException</code> with
     * the specified detail message.</p>
     *
     * @param message The detail message.
     */

    public DatatypeConfigurationException(String message) {
        super(message);
    }

    /**
     * <p>Create a new <code>DatatypeConfigurationException</code> with
     * the specified detail message and cause.</p>
     *
     * @param message The detail message.
     * @param cause The cause.  A <code>null</code> value is permitted, and indicates that the cause is nonexistent or unknown.
     */

    public DatatypeConfigurationException(String message, Throwable cause) {
        super(message);
        initCauseByReflection(cause);
    }

    /**
     * <p>Create a new <code>DatatypeConfigurationException</code> with
     * the specified cause.</p>
     *
     * @param cause The cause.  A <code>null</code> value is permitted, and indicates that the cause is nonexistent or unknown.
     */

    public DatatypeConfigurationException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        initCauseByReflection(cause);
    }

    /**
     * Print the the trace of methods from where the error
     * originated.  This will trace all nested exception
     * objects, as well as this object.
     */
    public void printStackTrace() {
        if (!isJDK14OrAbove && causeOnJDK13OrBelow != null) {
            printStackTrace0(new PrintWriter(System.err, true));
        }
        else {
            super.printStackTrace();
        }
    }

    /**
     * Print the the trace of methods from where the error
     * originated.  This will trace all nested exception
     * objects, as well as this object.
     * @param s The stream where the dump will be sent to.
     */
    public void printStackTrace(PrintStream s) {
        if (!isJDK14OrAbove && causeOnJDK13OrBelow != null) {
            printStackTrace0(new PrintWriter(s));
        }
        else {
            super.printStackTrace(s);
        }
    }

    /**
     * Print the the trace of methods from where the error
     * originated.  This will trace all nested exception
     * objects, as well as this object.
     * @param s The writer where the dump will be sent to.
     */
    public void printStackTrace(PrintWriter s) {
        if (!isJDK14OrAbove && causeOnJDK13OrBelow != null) {
            printStackTrace0(s);
        }
        else {
            super.printStackTrace(s);
        }
    }

    private void printStackTrace0(PrintWriter s) {
        causeOnJDK13OrBelow.printStackTrace(s);
        s.println("------------------------------------------");
        super.printStackTrace(s);
    }

    private void initCauseByReflection(Throwable cause) {
        causeOnJDK13OrBelow = cause;
        try {
            Method m = this.getClass().getMethod("initCause", new Class[] {Throwable.class});
            m.invoke(this, new Object[] {cause});
            isJDK14OrAbove = true;
        }
        // Ignore exception
        catch (Exception e) {}
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            Method m1 = this.getClass().getMethod("getCause", new Class[] {});
            Throwable cause = (Throwable) m1.invoke(this, new Object[] {});
            if (causeOnJDK13OrBelow == null) {
                causeOnJDK13OrBelow = cause;
            }
            else if (cause == null) {
                Method m2 = this.getClass().getMethod("initCause", new Class[] {Throwable.class});
                m2.invoke(this, new Object[] {causeOnJDK13OrBelow});
            }
            isJDK14OrAbove = true;
        }
        // Ignore exception
        catch (Exception e) {}
    }
}
