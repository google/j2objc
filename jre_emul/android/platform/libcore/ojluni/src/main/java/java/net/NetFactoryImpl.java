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

public class NetFactoryImpl implements NetFactory.FactoryInterface {

  public URL newURL(String spec) throws MalformedURLException {
    return new URL(spec);
  }

  public URL newURL(String protocol, String host, int port, String file, URLStreamHandler handler)
      throws MalformedURLException {
    return new URL(protocol, host, port, file, handler);
  }

  public URI newURI(String scheme, String host, String path, String fragment) {
    try {
      return new URI(scheme, host, path, fragment);
    } catch (URISyntaxException e) {
      // this should never happen (only invocation is from java.net.File)
      return null;
    }
  }

  public URI newURI(String scheme, String authority, String path, String query, String fragment) {
    try {
      return new URI(scheme, authority, path, query, fragment);
    } catch (URISyntaxException e) {
      // this should never happen (only invocation is from java.net.File)
      return null;
    }
  }
}
