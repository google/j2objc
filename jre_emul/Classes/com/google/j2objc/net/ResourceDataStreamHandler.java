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

package com.google.j2objc.net;

import com.google.j2objc.annotations.ReflectionSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Defines a stream handler for a resource data URL. This is used to
 * return linked in data resources as URLs, such as from Class.getResource()
 * and ClassLoader.findResources().
 */
@ReflectionSupport(value = ReflectionSupport.Level.FULL)
public class ResourceDataStreamHandler extends URLStreamHandler {
  private final String path;
  private final byte[] data;

  private static final String PROTOCOL_NAME = "resourcedata";

  public static URL createResourceDataURL(String path, byte[] data) {
    ResourceDataStreamHandler handler = new ResourceDataStreamHandler(path, data);
    try {
      return new URL(PROTOCOL_NAME, "", -1, path, handler);
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  private ResourceDataStreamHandler(String path, byte[] data) {
    this.path = path;
    this.data = data;
  }

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    if (!u.getProtocol().equals(PROTOCOL_NAME)) {
        throw new IOException("Cannot handle protocol: " + u.getProtocol());
    }
    return new URLConnection(u) {

      @Override
      public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
      }

      @Override
      public int getContentLength() {
        return data.length;
      }

      @Override
      public void connect() throws IOException {
        connected = true;
      }

      @Override
      public boolean equals(Object other) {
        return other instanceof ResourceDataStreamHandler
            && ((ResourceDataStreamHandler) other).path.equals(path);
      }

      @Override
      public int hashCode() {
        return path.hashCode();
      }
    };
  }
}
