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

import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.JdtParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Translation tool for generating Objective C source files from Java sources.
 * This tool is not intended to be a general purpose converter, but instead is
 * focused on what is needed for business logic libraries written in Java to
 * run natively on iOS.  In particular, no attempt is made to translate Java
 * UI framework code to any iOS frameworks.
 *
 * @author Tom Ball
 */
public class J2ObjC {

  static {
    // Always enable assertions in translator.
    ClassLoader loader = J2ObjC.class.getClassLoader();
    if (loader != null) {
      loader.setPackageAssertionStatus(J2ObjC.class.getPackage().getName(), true);
    }
  }

  private static final Logger logger = Logger.getLogger(J2ObjC.class.getName());

  private static void exit() {
    Options.deleteTemporaryDirectory();
    System.exit(ErrorUtil.errorCount());
  }

  public static String getFileHeader(String sourceFileName) {
    // Template parameters are: source file, user name, date.
    String username = System.getProperty("user.name");
    Date now = new Date();
    String generationDate = DateFormat.getDateInstance(DateFormat.SHORT).format(now);
    return String.format(Options.getFileHeader(), sourceFileName, username, generationDate);
  }

  private static class JarFileLoader extends URLClassLoader {
    public JarFileLoader() {
      super(new URL[]{});
    }

    public void addJarFile(String path) throws MalformedURLException {
      String urlPath = "jar:file://" + path + "!/";
      addURL(new URL(urlPath));
    }
  }

  private static void initPlugins(String[] pluginPaths, String pluginOptionString)
      throws IOException {
    @SuppressWarnings("resource")
    JarFileLoader classLoader = new JarFileLoader();
    for (String path : pluginPaths) {
      if (path.endsWith(".jar")) {
        JarInputStream jarStream = null;
        try {
          jarStream = new JarInputStream(new FileInputStream(path));
          classLoader.addJarFile(new File(path).getAbsolutePath());

          JarEntry entry;
          while ((entry = jarStream.getNextJarEntry()) != null) {
            String entryName = entry.getName();
            if (!entryName.endsWith(".class")) {
              continue;
            }

            String className = entryName.replaceAll("/", "\\.").substring(
                0, entryName.length() - ".class".length());

            try {
              Class<?> clazz = classLoader.loadClass(className);
              if (Plugin.class.isAssignableFrom(clazz)) {
                Constructor<?> cons = clazz.getDeclaredConstructor();
                Plugin plugin = (Plugin) cons.newInstance();
                plugin.initPlugin(pluginOptionString);
                Options.getPlugins().add(plugin);
              }
            } catch (Exception e) {
              throw new IOException("plugin exception: ", e);
            }
          }
        } finally {
          if (jarStream != null) {
            jarStream.close();
          }
        }
      } else {
        logger.warning("Don't understand plugin path entry: " + path);
      }
    }
  }

  public static void error(Exception e) {
    logger.log(Level.SEVERE, "Exiting due to exception", e);
    System.exit(1);
  }

  private static void checkErrors() {
    int errors = ErrorUtil.errorCount();
    if (Options.treatWarningsAsErrors()) {
      errors += ErrorUtil.warningCount();
    }
    if (errors > 0) {
      System.exit(1);
    }
  }

  private static JdtParser createParser() {
    JdtParser parser = new JdtParser();
    parser.addClasspathEntries(Options.getClassPathEntries());
    parser.addClasspathEntries(Options.getBootClasspath());
    parser.addSourcepathEntries(Options.getSourcePathEntries());
    parser.setIncludeRunningVMBootclasspath(false);
    parser.setEncoding(Options.fileEncoding());
    parser.setIgnoreMissingImports(Options.ignoreMissingImports());
    parser.setEnableDocComments(Options.docCommentsEnabled());
    return parser;
  }

  /**
   * Entry point for tool.
   *
   * @param args command-line arguments: flags and source file names
   * @throws IOException
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      Options.help(true);
    }
    String[] files = null;
    try {
      files = Options.load(args);
      if (files.length == 0) {
        Options.usage("no source files");
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
      System.exit(1);
    }

    try {
      initPlugins(Options.getPluginPathEntries(), Options.getPluginOptionString());
    } catch (IOException e) {
      error(e);
    }

    JdtParser parser = createParser();

    // Remove dead-code first, so modified file paths are replaced in the
    // translation list.
    DeadCodeProcessor deadCodeProcessor = DeadCodeProcessor.create(parser);
    if (deadCodeProcessor != null) {
      deadCodeProcessor.processFiles(Arrays.asList(files));
      checkErrors();
      files = deadCodeProcessor.postProcess().toArray(new String[0]);
    }

    TranslationProcessor translationProcessor = new TranslationProcessor(parser);
    translationProcessor.processFiles(Arrays.asList(files));
    translationProcessor.postProcess();
    checkErrors();

    exit();
  }
}
