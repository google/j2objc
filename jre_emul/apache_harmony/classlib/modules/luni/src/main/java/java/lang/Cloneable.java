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

package java.lang;


/**
 * This (empty) interface must be implemented by all classes that wish to
 * support cloning. The implementation of {@code clone()} in {@code Object}
 * checks if the object being cloned implements this interface and throws
 * {@code CloneNotSupportedException} if it does not.
 * 
 * @see Object#clone
 * @see CloneNotSupportedException
 */
public interface Cloneable {
    // Marker interface
}
