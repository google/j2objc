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

package java.beans;

import com.google.j2objc.LibraryNotLinkedError;

public class BeansFactory {

  public static PropertyChangeSupport newPropertyChangeSupportSafe(Object sourceBean) {
    FactoryInterface impl = IMPL;
    if (impl != null) {
      return impl.newPropertyChangeSupport(sourceBean);
    } else {
      return null;
    }
  }

  public static void throwNotLoadedError() {
    throw new LibraryNotLinkedError("java.beans support", "jre_beans",
        "JavaBeansPropertyChangeSupport");
  }

  private static final FactoryInterface IMPL = findImplementation();

  // Uses a native implementation to avoid the exception thrown by Class.forName().
  private static native FactoryInterface findImplementation() /*-[
    Class cls = objc_getClass("JavaBeansBeansFactoryImpl");
    if (cls) {
      return [[[cls alloc] init] autorelease];
    } else {
      return nil;
    }
  ]-*/;

  interface FactoryInterface {

    public PropertyChangeSupport newPropertyChangeSupport(Object sourceBean);
  }
}
