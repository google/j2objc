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

package java.net;

import com.google.j2objc.LibraryNotLinkedError;

public class NetFactory {

  public static URL newURL(String spec) throws MalformedURLException {
    return getImpl().newURL(spec);
  }

  public static URL newURL(
      String protocol, String host, int port, String file, URLStreamHandler handler)
      throws MalformedURLException {
    return getImpl().newURL(protocol, host, port, file, handler);
  }

  public static URI newURI(String scheme, String host, String path, String fragment) {
    return getImpl().newURI(scheme, host, path, fragment);
  }

  public static URI newURI(
      String scheme, String authority, String path, String query, String fragment) {
    return getImpl().newURI(scheme, authority, path, query, fragment);
  }

  private static final FactoryInterface IMPL = findImplementation();

  private static FactoryInterface getImpl() {
    FactoryInterface impl = IMPL;
    if (impl == null) {
      throw new LibraryNotLinkedError("java.net", "jre_net ", "JavaNetURL");
    }
    return impl;
  }

  private static FactoryInterface findImplementation() {
    try {
      Class<?> implClass = Class.forName("java.net.NetFactoryImpl");
      return (FactoryInterface) implClass.newInstance();
    } catch (Exception e) {
      return null;
    }
  }

  interface FactoryInterface {

    public URL newURL(String spec) throws MalformedURLException;

    public URL newURL(String protocol, String host, int port, String file, URLStreamHandler handler)
        throws MalformedURLException;

    public URI newURI(String scheme, String host, String path, String fragment);

    public URI newURI(String scheme, String authority, String path, String query, String fragment);
  }
}
