/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

/**
 * An empty marker interface for classes that want to support serialization and
 * deserialization based on the {@code ObjectOutputStream} and {@code
 * ObjectInputStream} classes. Implementing this interface is enough to make
 * most classes serializable. If a class needs more fine-grained control over
 * the serialization process (for example to implement compatibility with older
 * versions of the class), it can achieve this by providing the following two
 * methods (signatures must match exactly):
 * <p>
 * {@code private void writeObject(java.io.ObjectOutputStream out) throws
 * IOException}
 * <p>
 * {@code private void readObject(java.io.ObjectInputStream in) throws
 * IOException, ClassNotFoundException}
 */
public interface Serializable {
    /* empty */
}
