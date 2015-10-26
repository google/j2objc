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

package org.apache.harmony.beans.tests.support.mock;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

public class FakeFox041 extends FakeFox04 {

    public int[] getTwoProp() {
        return null;
    }

    // throwing PropertyVetoException makes this property constrained.
    public void setTwoProp(int[] i) throws PropertyVetoException {
    }

    // being able to add/remove listeners makes this classes properties bound.
    // but it does not bind properties in any superclasses.
    // both add and remove methods are required.
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

}
