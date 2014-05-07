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

package java.sql;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Provides facilities for managing JDBC drivers. The <code>android.database</code> and
 * <code>android.database.sqlite</code> packages offer a higher-performance alternative for new
 * code.
 *
 * <p>Note that Android does not include any JDBC drivers by default; you must provide your own.
 *
 * <p>The {@code DriverManager} class loads JDBC drivers during its initialization,
 * from the list of drivers referenced by the system property {@code
 * "jdbc.drivers"}.
 */
public class DriverManager {

    /*
     * Facilities for logging. The Print Stream is deprecated but is maintained
     * here for compatibility.
     */
    private static PrintStream thePrintStream;

    private static PrintWriter thePrintWriter;

    // Login timeout value - by default set to 0 -> "wait forever"
    private static int loginTimeout = 0;

    /*
     * Set to hold Registered Drivers - initial capacity 10 drivers (will expand
     * automatically if necessary.
     */
    private static final List<Driver> theDrivers = new ArrayList<Driver>(10);

    // Permission for setting log
    private static final SQLPermission logPermission = new SQLPermission("setLog");

    /*
     * Load drivers on initialization
     */
    static {
        loadInitialDrivers();
    }

    /*
     * Loads the set of JDBC drivers defined by the Property "jdbc.drivers" if
     * it is defined.
     */
    private static void loadInitialDrivers() {
        String theDriverList = System.getProperty("jdbc.drivers", null);
        if (theDriverList == null) {
            return;
        }

        /*
         * Get the names of the drivers as an array of Strings from the system
         * property by splitting the property at the separator character ':'
         */
        String[] theDriverNames = theDriverList.split(":");

        for (String element : theDriverNames) {
            try {
                // Load the driver class
                Class
                        .forName(element, true, ClassLoader
                                .getSystemClassLoader());
            } catch (Throwable t) {
                // Ignored
            }
        }
    }

    /*
     * A private constructor to prevent allocation
     */
    private DriverManager() {
    }

    /**
     * Removes a driver from the {@code DriverManager}'s registered driver list.
     * This will only succeed when the caller's class loader loaded the driver
     * that is to be removed. If the driver was loaded by a different class
     * loader, the removal of the driver fails silently.
     * <p>
     * If the removal succeeds, the {@code DriverManager} will not use this
     * driver in the future when asked to get a {@code Connection}.
     *
     * @param driver
     *            the JDBC driver to remove.
     * @throws SQLException
     *             if there is a problem interfering with accessing the
     *             database.
     */
    public static void deregisterDriver(Driver driver) throws SQLException {
        if (driver == null) {
            return;
        }
        synchronized (theDrivers) {
            theDrivers.remove(driver);
        }
    }

    /**
     * Attempts to establish a connection to the given database URL.
     *
     * @param url
     *            a URL string representing the database target to connect with.
     * @return a {@code Connection} to the database identified by the URL.
     *         {@code null} if no connection can be established.
     * @throws SQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL.
     */
    public static Connection getConnection(String url) throws SQLException {
        return getConnection(url, new Properties());
    }

    /**
     * Attempts to establish a connection to the given database URL.
     *
     * @param url
     *            a URL string representing the database target to connect with
     * @param info
     *            a set of properties to use as arguments to set up the
     *            connection. Properties are arbitrary string/value pairs.
     *            Normally, at least the properties {@code "user"} and {@code
     *            "password"} should be passed, with appropriate settings for
     *            the user ID and its corresponding password to get access to
     *            the corresponding database.
     * @return a {@code Connection} to the database identified by the URL.
     *         {@code null} if no connection can be established.
     * @throws SQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL.
     */
    public static Connection getConnection(String url, Properties info) throws SQLException {
        // 08 - connection exception
        // 001 - SQL-client unable to establish SQL-connection
        String sqlState = "08001";
        if (url == null) {
            throw new SQLException("The url cannot be null", sqlState);
        }
        synchronized (theDrivers) {
            /*
             * Loop over the drivers in the DriverSet checking to see if one can
             * open a connection to the supplied URL - return the first
             * connection which is returned
             */
            for (Driver theDriver : theDrivers) {
                Connection theConnection = theDriver.connect(url, info);
                if (theConnection != null) {
                    return theConnection;
                }
            }
        }
        // If we get here, none of the drivers are able to resolve the URL
        throw new SQLException("No suitable driver", sqlState);
    }

    /**
     * Attempts to establish a connection to the given database URL.
     *
     * @param url
     *            a URL string representing the database target to connect with.
     * @param user
     *            a user ID used to login to the database.
     * @param password
     *            a password for the user ID to login to the database.
     * @return a {@code Connection} to the database identified by the URL.
     *         {@code null} if no connection can be established.
     * @throws SQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL.
     */
    public static Connection getConnection(String url, String user, String password)
            throws SQLException {
        Properties theProperties = new Properties();
        if (user != null) {
            theProperties.setProperty("user", user);
        }
        if (password != null) {
            theProperties.setProperty("password", password);
        }
        return getConnection(url, theProperties);
    }

    /**
     * Tries to find a driver that can interpret the supplied URL.
     *
     * @param url
     *            the URL of a database.
     * @return a {@code Driver} that matches the provided URL. {@code null} if
     *         no {@code Driver} understands the URL
     * @throws SQLException
     *             if there is any kind of problem accessing the database.
     */
    public static Driver getDriver(String url) throws SQLException {
        synchronized (theDrivers) {
            /*
             * Loop over the drivers in the DriverSet checking to see if one
             * does understand the supplied URL - return the first driver which
             * does understand the URL
             */
            for (Driver driver : theDrivers) {
                if (driver.acceptsURL(url)) {
                    return driver;
                }
            }
        }
        // If no drivers understand the URL, throw an SQLException
        // SQLState: 08 - connection exception
        // 001 - SQL-client unable to establish SQL-connection
        throw new SQLException("No suitable driver", "08001");
    }

    /**
     * Returns an {@code Enumeration} that contains all of the loaded JDBC
     * drivers that the current caller can access.
     *
     * @return An {@code Enumeration} containing all the currently loaded JDBC
     *         {@code Drivers}.
     */
    public static Enumeration<Driver> getDrivers() {
        /*
         * Synchronize to avoid clashes with additions and removals of drivers
         * in the DriverSet
         */
        synchronized (theDrivers) {
            ArrayList<Driver> result = new ArrayList<Driver>();
            for (Driver driver : theDrivers) {
                result.add(driver);
            }
            return Collections.enumeration(result);
        }
    }

    /**
     * Returns the login timeout when connecting to a database in seconds.
     *
     * @return the login timeout in seconds.
     */
    public static int getLoginTimeout() {
        return loginTimeout;
    }

    /**
     * Gets the log {@code PrintStream} used by the {@code DriverManager} and
     * all the JDBC Drivers.
     *
     * @deprecated Use {@link #getLogWriter()} instead.
     * @return the {@code PrintStream} used for logging activities.
     */
    @Deprecated
    public static PrintStream getLogStream() {
        return thePrintStream;
    }

    /**
     * Retrieves the log writer.
     *
     * @return A {@code PrintWriter} object used as the log writer. {@code null}
     *         if no log writer is set.
     */
    public static PrintWriter getLogWriter() {
        return thePrintWriter;
    }

    /**
     * Prints a message to the current JDBC log stream. This is either the
     * {@code PrintWriter} or (deprecated) the {@code PrintStream}, if set.
     *
     * @param message
     *            the message to print to the JDBC log stream.
     */
    public static void println(String message) {
        if (thePrintWriter != null) {
            thePrintWriter.println(message);
            thePrintWriter.flush();
        } else if (thePrintStream != null) {
            thePrintStream.println(message);
            thePrintStream.flush();
        }
        /*
         * If neither the PrintWriter not the PrintStream are set, then silently
         * do nothing the message is not recorded and no exception is generated.
         */
    }

    /**
     * Registers a given JDBC driver with the {@code DriverManager}.
     * <p>
     * A newly loaded JDBC driver class should register itself with the
     * {@code DriverManager} by calling this method.
     *
     * @param driver
     *            the {@code Driver} to register with the {@code DriverManager}.
     * @throws SQLException
     *             if a database access error occurs.
     */
    public static void registerDriver(Driver driver) throws SQLException {
        if (driver == null) {
            throw new NullPointerException("driver == null");
        }
        synchronized (theDrivers) {
            theDrivers.add(driver);
        }
    }

    /**
     * Sets the login timeout when connecting to a database in seconds.
     *
     * @param seconds
     *            seconds until timeout. 0 indicates wait forever.
     */
    public static void setLoginTimeout(int seconds) {
        loginTimeout = seconds;
    }

    /**
     * Sets the print stream to use for logging data from the {@code
     * DriverManager} and the JDBC drivers.
     *
     * @deprecated Use {@link #setLogWriter} instead.
     * @param out
     *            the {@code PrintStream} to use for logging.
     */
    @Deprecated
    public static void setLogStream(PrintStream out) {
        thePrintStream = out;
    }

    /**
     * Sets the {@code PrintWriter} that is used by all loaded drivers, and also
     * the {@code DriverManager}.
     *
     * @param out
     *            the {@code PrintWriter} to be used.
     */
    public static void setLogWriter(PrintWriter out) {
        thePrintWriter = out;
    }

    /**
     * Determines whether the supplied object was loaded by the given {@code ClassLoader}.
     *
     * @param theObject
     *            the object to check.
     * @param theClassLoader
     *            the {@code ClassLoader}.
     * @return {@code true} if the Object does belong to the {@code ClassLoader}
     *         , {@code false} otherwise
     */
    private static boolean isClassFromClassLoader(Object theObject,
            ClassLoader theClassLoader) {

        if ((theObject == null) || (theClassLoader == null)) {
            return false;
        }

        Class<?> objectClass = theObject.getClass();

        try {
            Class<?> checkClass = Class.forName(objectClass.getName(), true,
                    theClassLoader);
            if (checkClass == objectClass) {
                return true;
            }
        } catch (Throwable t) {
            // Empty
        }
        return false;
    }
}
