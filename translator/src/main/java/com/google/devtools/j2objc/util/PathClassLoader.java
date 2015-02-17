/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.logging.Logger;

/**
 * A ClassLoader that understands file system paths.
 *
 * @author Mike Thvedt
 */
public class PathClassLoader extends URLClassLoader {
  private static final Logger logger = Logger.getLogger(PathClassLoader.class.getName());

  public PathClassLoader() {
    super(new URL[0]);
  }

  public PathClassLoader(List<String> paths) {
    this();
    addPaths(paths);
  }

  public void addPaths(List<String> paths) {
    for (String path: paths) {
      addPath(path);
    }
  }

  public void addPath(String path) {
    URL url;
    try {
      url = new File(path).toURI().toURL();
      addURL(url);
    } catch (MalformedURLException e) {
      logger.warning("Don't understand path: " + path);
    }
  }
}
