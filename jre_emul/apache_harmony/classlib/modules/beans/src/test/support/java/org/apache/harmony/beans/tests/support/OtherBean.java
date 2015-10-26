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

package org.apache.harmony.beans.tests.support;

import java.beans.PropertyChangeListener;

public class OtherBean {

    public void addSampleListener(SampleListener listener) {
    }

    public void removeSampleListener(SampleListener listener) {
    }

    // no corresponding add method
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
   
    public void setNumber(int a) {
    }

    public void set(int a) {
    }
}
