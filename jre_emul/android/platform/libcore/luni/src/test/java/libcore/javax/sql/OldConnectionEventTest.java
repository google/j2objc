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

package libcore.javax.sql;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;
import junit.framework.TestCase;

public class OldConnectionEventTest extends TestCase {

    public void testConstructorConnection() {
        Impl_PooledConnection ipc = new Impl_PooledConnection();
        ConnectionEvent ce = new ConnectionEvent(ipc);
        ConnectionEvent ce2 = new ConnectionEvent(ipc,null);
        assertSame(ce2.getSource(),ce.getSource());
    }

    public void testGetSQLException() {
        Impl_PooledConnection ipc = new Impl_PooledConnection();
        ConnectionEvent ce = new ConnectionEvent(ipc);

        ConnectionEvent ce2 = new ConnectionEvent(ipc, null);
        assertNull(ce.getSQLException());
        assertEquals(ce2.getSQLException(), ce.getSQLException());

        SQLException e = new SQLException();
        ConnectionEvent ce3 = new ConnectionEvent(ipc, e);
        assertNotNull(ce3.getSQLException());
        assertNotSame(ce3.getSQLException(), ce2.getSQLException());
    }

    static class Impl_PooledConnection implements PooledConnection {
        public void addConnectionEventListener(ConnectionEventListener theListener) {}
        public void close() throws SQLException {}
        public Connection getConnection() throws SQLException {
            return null;
        }
        public void removeConnectionEventListener(ConnectionEventListener listener) {}
        public void addStatementEventListener(StatementEventListener listener) {}
        public void removeStatementEventListener(StatementEventListener listener) {}
    }
}
