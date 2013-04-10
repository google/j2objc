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

package com.google.devtools.j2objc;

import com.google.common.base.Preconditions;

import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A plugin invoked to do custom processing not part of J2ObjC.
 *
 * @author Pankaj Kakkar
 */
public abstract class Plugin {

  private final Map<String, String> options = new HashMap<String, String>();

  /**
   * Initialize the plugin with the given options. Interpreting the options
   * is completely up to the plugin. Different plugins may be passed the same
   * options and interpret them differently. Called before any processing is
   * started.
   */
  void initPlugin(String pluginOptions) throws IOException {
    String[] optionComponents = pluginOptions.split(",");

    for (String option : optionComponents) {
      if (option.isEmpty()) {
        continue;
      }

      String[] keyValuePair = option.split("=");
      Preconditions.checkState(keyValuePair.length == 2);

      options.put(keyValuePair[0], keyValuePair[1]);
    }

    beginProcessing();
  }

  protected final String getOption(String key) {
    return options.get(key);
  }

  /**
   * Called after options have been initialized and can be accessed via
   * getOption. Subclasses may override this to process any needed options.
   */
  protected void beginProcessing() throws IOException { }

  /**
   * Called to process the given CompilationUnit after J2ObjC has processed
   * it.
   */
  public abstract void processUnit(CompilationUnit unit);

  /**
   * Called after all input files have been translated.
   */
  public abstract void endProcessing(File outputDirectory);
}
