/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.luni.tests.java.lang;

import java.io.Serializable;

class MockEnum2 implements Serializable {

    private static final long serialVersionUID = -4812214670022262730L;

    enum Sample {
        LARRY, MOE, CURLY
    }

    enum Sample2 {
        RED, BLUE, YELLO
    }

    String str;

    int i;

    Sample samEnum;

    Sample larry = Sample.LARRY;

    String myStr = "LARRY";

    MockEnum2() {
        str = "test";
        i = 99;
        samEnum = larry;
    }

    public boolean equals(Object arg0) {
        if (!(arg0 instanceof MockEnum2)) {
            return false;
        }
        MockEnum2 test = (MockEnum2) arg0;
        if (str.equals(test.str) && i == test.i && samEnum == test.samEnum
                && myStr.equals(test.myStr)) {
            return true;
        }
        return false;
    }

}
