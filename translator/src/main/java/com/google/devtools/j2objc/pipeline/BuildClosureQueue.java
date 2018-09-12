/*
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

package com.google.devtools.j2objc.pipeline;

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Looks up and queues dependencies for --build-closure.
 */
public class BuildClosureQueue {

  private static final Logger logger = Logger.getLogger(BuildClosureQueue.class.getName());
  private final Options options;

  private final Set<String> processedNames = Sets.newHashSet();

  private final Set<String> queuedNames = Sets.newLinkedHashSet();

  public BuildClosureQueue(Options options) {
    this.options = options;
    queuedNames.addAll(options.entryClasses());
  }

  /**
   * Returns the next Java source file to be processed. Returns null if the
   * queue is empty.
   */
  public InputFile getNextFile() {
    for (Iterator<String> iter = queuedNames.iterator(); iter.hasNext(); ) {
      String name = iter.next();
      iter.remove();
      processedNames.add(name);
      InputFile file = getFileForName(name);
      if (file != null) {
        return file;
      }
    }
    return null;
  }

  /**
   * Adds a name to the queue. The name must be the fully qualified type name
   * to search for (dot separated).
   */
  public void addName(String name) {
    if (!processedNames.contains(name)) {
      queuedNames.add(name);
    }
  }

  /**
   * Adds the name of a file that has been processed to ensure that this name is
   * not searched for in the future.
   */
  public void addProcessedName(String name) {
    processedNames.add(name);
    queuedNames.remove(name);
  }

  private InputFile getFileForName(String name) {
    InputFile inputFile = null;
    try {
      inputFile = options.fileUtil().findOnSourcePath(name);
    } catch (IOException e) {
      ErrorUtil.warning(e.getMessage());
    }

    if (inputFile == null) {
      // Check if class exists on classpath.
      if (findClassFile(name)) {
        logger.finest("no source for " + name + ", class found");
      }
      return null;
    }

    // Check if the source file is older than the generated header file.
    File headerSource = new File(
        options.fileUtil().getOutputDirectory(), name.replace('.', File.separatorChar) + ".h");
    if (headerSource.exists() && inputFile.lastModified() < headerSource.lastModified()) {
      return null;
    }

    return inputFile;
  }

  private boolean findClassFile(String name) {
    InputFile f = null;
    try {
      f = options.fileUtil().findOnClassPath(name);
    } catch (IOException e) {
      ErrorUtil.warning(e.getMessage());
    }
    if (f != null) {
      return true;
    }
    // See if it's a JRE class.
    try {
      Class.forName(name);
      return true;
    } catch (ClassNotFoundException e) {
      // Fall-through.
    }
    return false;
  }
}
