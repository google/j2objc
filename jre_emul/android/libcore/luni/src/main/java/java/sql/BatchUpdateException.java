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

import java.io.Serializable;

/**
 * This exception is thrown if a problem occurs during a batch update operation.
 * <p>
 * A {@code BatchUpdateException} provides additional information about the
 * problem that occurred, compared with a standard {@code SQLException}. It
 * supplies update counts for successful commands which were executed before the
 * exception was encountered.
 * <p>
 * The element order in the array of update counts matches the order that the
 * commands were added to the batch operation.
 * <p>
 * Once a batch update command fails and a {@code BatchUpdateException} is
 * thrown, the JDBC driver may continue processing the remaining commands in the
 * batch. If the driver does process more commands after the problem occurs, the
 * array returned by {@code BatchUpdateException.getUpdateCounts} has an element
 * for every command in the batch, not only those that executed successfully. In
 * this case, the array element for any command which encountered a problem is
 * set to {@code Statement.EXECUTE_FAILED}.
 */
public class BatchUpdateException extends SQLException implements Serializable {

    private static final long serialVersionUID = 5977529877145521757L;

    private int[] updateCounts = null;

    /**
     * Creates a default {@code BatchUpdateException} with the parameters
     * <i>reason</i>, <i>SQLState</i>, and <i>update counts</i> set to {@code
     * null} and the <i>vendor code</i> set to 0.
     */
    public BatchUpdateException() {
    }

    /**
     * Creates an BatchUpdateException object. The reason is set to
     * null if cause == null otherwise to cause.toString(), and the cause
     * Throwable object is set to the given cause Throwable object.
     *
     * @param cause the Throwable object for the underlying reason this SQLException
     *
     * @since 1.6
     */
    public BatchUpdateException(Throwable cause) {
        this(null, cause);
    }

    /**
     * Creates an BatchUpdateException object. The Reason string is set to the
     * null if cause == null otherwise to cause.toString(), and the cause
     * Throwable object is set to the given cause Throwable object. SQLState is
     * initialized to null while vendorCode is zero.
     *
     * @param cause the Throwable object for the underlying reason this SQLException
     *
     * @since 1.6
     */
    public BatchUpdateException(int[] updateCounts, Throwable cause) {
        super(cause);
        this.updateCounts = updateCounts;
    }

    /**
     * Creates an BatchUpdateException object. The cause Throwable object is set
     * to the given cause Throwable object. SQLState is initialized to null
     * while vendorCode is zero.
     *
     * @param cause the Throwable object for the underlying reason this SQLException
     *
     * @since 1.6
     */
    public BatchUpdateException(String reason, int[] updateCounts,
            Throwable cause) {
        super(reason, cause);
        this.updateCounts = updateCounts;
    }

    /**
     * Creates an BatchUpdateException object. The cause Throwable object is set
     * to the given cause Throwable object and the updateCounts array set to the
     * int array parameter. SQLState is initialized to null while vendorCode is
     * zero.
     *
     * @param cause the Throwable object for the underlying reason this SQLException
     *
     * @since 1.6
     */
    public BatchUpdateException(String reason, String SQLState,
            int[] updateCounts, Throwable cause) {
        super(reason, SQLState, cause);
        this.updateCounts = updateCounts;
    }

    /**
     * Creates an BatchUpdateException object. The cause Throwable object is set
     * to the given cause Throwable object and the updateCounts array set to the
     * int array parameter. VendorCode is set to the given vendorCode. SQLState
     * is initialized to null while vendorCode is zero.
     *
     * @param cause the Throwable object for the underlying reason this SQLException
     *
     * @since 1.6
     */
    public BatchUpdateException(String reason, String SQLState, int vendorCode,
            int[] updateCounts, Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
        this.updateCounts = updateCounts;
    }

    /**
     * Creates a {@code BatchUpdateException} with the {@code updateCounts} set
     * to the supplied value. All other fields are set to their
     * default values.
     *
     * @param updateCounts
     *            the array of {@code updateCounts} giving the number of
     *            successful updates (or another status code) for each command
     *            in the batch that was attempted.
     */
    public BatchUpdateException(int[] updateCounts) {
        this.updateCounts = updateCounts;
    }

    /**
     * Creates a {@code BatchUpdateException} with the {@code updateCounts} and
     * {@code reason} set to the supplied values. All other fields are set to their
     * default values.
     *
     * @param reason
     *            the message providing information about the source of this
     *            exception.
     * @param updateCounts
     *            the array of {@code updateCounts} giving the number of
     *            successful updates (or another status code) for each command
     *            in the batch that was attempted.
     */
    public BatchUpdateException(String reason, int[] updateCounts) {
        super(reason);
        this.updateCounts = updateCounts;
    }

    /**
     * Creates a {@code BatchUpdateException} with the {@code reason}, {@code
     * SQLState} and {@code updateCounts} set to the supplied values. All other
     * fields are set to their default values.
     *
     * @param reason
     *            the message providing information about the source of this
     *            exception.
     * @param SQLState
     *            the X/OPEN value to use for the {@code SQLState}
     * @param updateCounts
     *            the array of {@code updateCounts} giving the number of
     *            successful updates (or another status code) for each command
     *            in the batch that was attempted.
     */
    public BatchUpdateException(String reason, String SQLState,
            int[] updateCounts) {
        super(reason, SQLState);
        this.updateCounts = updateCounts;
    }

    /**
     * Creates a {@code BatchUpdateException} for the case where all relevant
     * information is provided.
     *
     * @param reason
     *            the message providing information about the source of this
     *            exception.
     * @param SQLState
     *            the X/OPEN value to use for the {@code SQLState}.
     * @param vendorCode
     *            the value to use for the vendor error code.
     * @param updateCounts
     *            the array of {@code updateCounts} giving the number of
     *            successful updates (or another status code) for each command
     *            in the batch that was attempted.
     */
    public BatchUpdateException(String reason, String SQLState, int vendorCode,
            int[] updateCounts) {
        super(reason, SQLState, vendorCode);
        this.updateCounts = updateCounts;
    }

    /**
     * Gets the <i>update count</i> array giving status information for every
     * command that was attempted in the batch.
     * <p>
     * If a batch update command fails and a {@code BatchUpdateException} is
     * thrown, the JDBC driver may continue processing the remaining commands in
     * the batch. If the driver does so, the array returned by {@code
     * BatchUpdateException.getUpdateCounts} has an element for every command in
     * the batch, not only those that executed successfully. In this case, the
     * array element for any command which encountered a problem is set to
     * {@code Statement.EXECUTE_FAILED}.
     *
     * @return an array that contains the successful update counts, before this
     *         exception was thrown. Alternatively, if the driver continues to
     *         process commands following an error, for each successive command
     *         there is a corresponding element in the array giving one of the
     *         following status values:
     *         <ol>
     *         <li>the number of successful updates</li> <li>{@code
     *         Statement.SUCCESS_NO_INFO} indicating that the command completed
     *         successfully, but the amount of altered rows is unknown.</li>
     *         <li>{@code Statement.EXECUTE_FAILED} indicating that the command
     *         was unsuccessful.</li>
     *         </ol>
     */
    public int[] getUpdateCounts() {
        return updateCounts;
    }
}
