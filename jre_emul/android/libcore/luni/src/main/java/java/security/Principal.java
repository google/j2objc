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

package java.security;


/**
 * {@code Principal}s are objects which have identities. These can be
 * individuals, groups, corporations, unique program executions, etc.
 */
public interface Principal {
    /**
     * Compares the specified object with this {@code Principal} for equality
     * and returns {@code true} if the specified object is equal, {@code false}
     * otherwise.
     *
     * @param obj
     *            object to be compared for equality with this {@code
     *            Principal}.
     * @return {@code true} if the specified object is equal to this {@code
     *         Principal}, otherwise {@code false}.
     */
    public boolean equals( Object obj );

    /**
     * Returns the name of this {@code Principal}.
     *
     * @return the name of this {@code Principal}.
     */
    public String getName();

    /**
     * Returns the hash code value for this {@code Principal}. Returns the same
     * hash code for {@code Principal}s that are equal to each other as
     * required by the general contract of {@link Object#hashCode}.
     *
     * @return the hash code value for this {@code Principal}.
     * @see Object#equals(Object)
     * @see Principal#equals(Object)
     */
    public int hashCode();

    /**
     * Returns a string containing a concise, human-readable description of
     * this {@code Principal}.
     *
     * @return a printable representation for this {@code Principal}.
     */
    public String toString();
}
