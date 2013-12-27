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
 * Defines an interface for classes that want to be serializable, but have their
 * own binary representation.
 */
public interface Externalizable extends Serializable {
    /**
     * Reads the next object from the ObjectInput <code>input</code>.
     *
     * @param input
     *            the ObjectInput from which the next object is read.
     * @throws IOException
     *             if an error occurs attempting to read from {@code input}.
     * @throws ClassNotFoundException
     *             if the class of the instance being loaded cannot be found.
     */
    public void readExternal(ObjectInput input) throws IOException,
            ClassNotFoundException;

    /**
     * Writes the receiver to the ObjectOutput <code>output</code>.
     *
     * @param output
     *            the ObjectOutput to write the object to.
     * @throws IOException
     *             if an error occurs attempting to write to {@code output}.
     */
    public void writeExternal(ObjectOutput output) throws IOException;
}
