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
 * An exception which is thrown when a JDBC driver unexpectedly truncates a data
 * value either when reading (resulting in warning), or when writing data
 * (resulting in an error). The {@code SQLState} error code for truncated data
 * is {@code 01004}.
 */
public class DataTruncation extends SQLWarning implements Serializable {

    private static final long serialVersionUID = 6464298989504059473L;

    private int index = 0;

    private boolean parameter = false;

    private boolean read = false;

    private int dataSize = 0;

    private int transferSize = 0;

    private static final String THE_REASON = "Data truncation";

    private static final String THE_SQLSTATE_READ = "01004";

    private static final String THE_SQLSTATE_WRITE = "22001";

    private static final int THE_ERROR_CODE = 0;

    /**
     * Creates the {@code DataTruncation} object. The reason is set to {@code
     * "Data truncation"}, the error code is set to the {@code
     * SQLException} default value, and the other fields are set to the values
     * supplied as arguments.
     *
     * @param index
     *            the Index value of the column value or parameter that was
     *            truncated.
     * @param parameter
     *            {@code true} if it was a parameter value that was truncated,
     *            {@code false} otherwise.
     * @param read
     *            {@code true} if the truncation occurred on a read operation,
     *            {@code false} otherwise.
     * @param dataSize
     *            the original size of the truncated data.
     * @param transferSize
     *            the size of the data after truncation.
     */
    public DataTruncation(int index, boolean parameter, boolean read,
            int dataSize, int transferSize) {
        super(THE_REASON, THE_SQLSTATE_READ, THE_ERROR_CODE);
        this.index = index;
        this.parameter = parameter;
        this.read = read;
        this.dataSize = dataSize;
        this.transferSize = transferSize;
    }

    /**
     * Creates a DataTruncation. The Reason is set to "Data truncation", the
     * error code is set to the SQLException default value and other fields are
     * set to the values supplied on this method.
     *
     * @param index
     *            the Index value of the column value or parameter that was
     *            truncated
     * @param parameter
     *            true if it was a Parameter value that was truncated, false
     *            otherwise
     * @param read
     *            true if the truncation occurred on a read operation, false
     *            otherwise
     * @param dataSize
     *            the original size of the truncated data
     * @param transferSize
     *            the size of the data after truncation
     * @param cause
     *            the root reason for this DataTruncation
     *
     * @since 1.6
     */
    public DataTruncation(int index, boolean parameter, boolean read,
            int dataSize, int transferSize, Throwable cause) {
        super(THE_REASON, read ? THE_SQLSTATE_READ : THE_SQLSTATE_WRITE,
                THE_ERROR_CODE, cause);
        this.index = index;
        this.parameter = parameter;
        this.read = read;
        this.dataSize = dataSize;
        this.transferSize = transferSize;
    }

    /**
     * Gets the number of bytes of data that should have been read/written.
     *
     * @return the number of bytes that should have been read or written. The
     *         value is set to {@code -1} if the size is unknown.
     */
    public int getDataSize() {
        return dataSize;
    }

    /**
     * Gets the index of the column or of the parameter that was truncated.
     *
     * @return the index number of the column or of the parameter.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets whether the value truncated was a parameter value or a column value.
     *
     * @return {@code true} if the value truncated was a parameter value,
     *         {@code false} if it was a column value.
     */
    public boolean getParameter() {
        return parameter;
    }

    /**
     * Gets whether the value was truncated on a read operation or a write
     * operation
     *
     * @return {@code true} if the value was truncated on a read operation,
     *         {@code false} otherwise.
     */
    public boolean getRead() {
        return read;
    }

    /**
     * Gets the number of bytes of data that was actually read or written.
     *
     * @return the number of bytes actually read/written. The value may be set
     *         to {@code -1} if the size is unknown.
     */
    public int getTransferSize() {
        return transferSize;
    }
}
