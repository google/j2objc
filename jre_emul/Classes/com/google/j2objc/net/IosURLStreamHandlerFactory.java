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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import libcore.net.url.FileHandler;

/**
 * Dynamically load iOS stream handling classes. This class is public so that
 * apps can add an explicit dependency to force load this class.
 */
public class IosURLStreamHandlerFactory implements URLStreamHandlerFactory {

  @Override
  public URLStreamHandler createURLStreamHandler(String protocol) {
    if (protocol.equals("http")) {
      return new IosHttpHandler();
    }
    if (protocol.equals("https")) {
      return new IosHttpsHandler();
    }
    if (protocol.equals("file")) {
      return new FileHandler();
    }
    return null;
  }

}
