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

/**
 * A savepoint is an instant during the current transaction that can be utilized
 * by a rollback via the {@link Connection#rollback} command. Rolling back to a
 * particular savepoint means that all changes that occurred after that
 * savepoint are undone.
 */
public interface Savepoint {

    /**
     * Returns the constructed ID for this savepoint.
     *
     * @return the ID for this savepoint.
     * @throws SQLException
     *             if an error occurrs accessing the database.
     */
    public int getSavepointId() throws SQLException;

    /**
     * Returns the name for this savepoint.
     *
     * @return the name of this savepoint.
     * @throws SQLException
     *             if an error occurrs accessing the database.
     */
    public String getSavepointName() throws SQLException;
}
