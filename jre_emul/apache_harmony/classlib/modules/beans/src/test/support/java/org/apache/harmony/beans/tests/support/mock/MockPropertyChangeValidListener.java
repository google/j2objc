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

import java.util.EventListener;

public interface MockPropertyChangeValidListener extends EventListener {
    /*
     * fire MockPropertyChange event.
     */

    public void mockPropertyChange(MockPropertyChangeEvent e);

    public void mockPropertyChange2(MockPropertyChangeEvent e);

    public void mockPropertyChange3(MockPropertyChangeEvent e);

    public void mockNotAEventObject(MockFakeEvent event);

    public void mockPropertyChange_Valid(MockEvent event); 
    
    public void mockPropertyChange_Valid(Mock2Event event);
    
    public void mockPropertyChange_Valid(MockPropertyChangeEvent event);
   

}

class MockEvent 
{
}

class Mock2Event
{
}
