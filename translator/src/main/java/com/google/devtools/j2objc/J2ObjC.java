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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.DeadCodeMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.PathClassLoader;
import com.google.devtools.j2objc.util.ProGuardUsageParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
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

  public static String getFileHeader(String sourceFileName) {
    return String.format(Options.getFileHeader(), sourceFileName);
  }

  private static void initPlugins(String[] pluginPaths, String pluginOptionString)
      throws IOException {
    @SuppressWarnings("resource")
    PathClassLoader classLoader = new PathClassLoader();
    for (String path : pluginPaths) {
      if (path.endsWith(".jar")) {
        JarInputStream jarStream = null;
        try {
          jarStream = new JarInputStream(new FileInputStream(path));
          classLoader.addPath(path);

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

  private static void checkErrors() {
    int errors = ErrorUtil.errorCount();
    if (Options.treatWarningsAsErrors()) {
      errors += ErrorUtil.warningCount();
    }
    if (errors > 0) {
      System.exit(errors);
    }
  }

  @VisibleForTesting
  static JdtParser createParser() {
    JdtParser parser = new JdtParser();
    parser.addClasspathEntries(Options.getClassPathEntries());
    parser.addClasspathEntries(Options.getBootClasspath());
    parser.addSourcepathEntries(Options.getSourcePathEntries());
    parser.setIncludeRunningVMBootclasspath(false);
    parser.setEncoding(Options.fileEncoding());
    parser.setEnableDocComments(Options.docCommentsEnabled());
    return parser;
  }

  private static DeadCodeMap loadDeadCodeMap() {
    File file = Options.getProGuardUsageFile();
    if (file != null) {
      try {
        return ProGuardUsageParser.parse(Files.asCharSource(file, Charset.defaultCharset()));
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }
    return null;
  }

  /**
   * Runs the entire J2ObjC pipeline.
   * @param fileArgs the files to process, same format as command-line args to {@link #main}.
   */
  public static void run(List<String> fileArgs) {
    File preProcessorTempDir = null;
    try {
      JdtParser parser = createParser();

      GenerationBatch batch = new GenerationBatch();
      batch.processFileArgs(fileArgs);
      if (ErrorUtil.errorCount() > 0) {
        return;
      }

      AnnotationPreProcessor preProcessor = new AnnotationPreProcessor(batch);
      preProcessor.process(fileArgs);
      preProcessorTempDir = preProcessor.getTemporaryDirectory();
      if (ErrorUtil.errorCount() > 0) {
        return;
      }
      if (preProcessorTempDir != null) {
        parser.addSourcepathEntry(preProcessorTempDir.getAbsolutePath());
      }

      PackageInfoPreProcessor packageInfoPreProcessor = new PackageInfoPreProcessor(parser);
      packageInfoPreProcessor.processBatch(batch);
      if (ErrorUtil.errorCount() > 0) {
        return;
      }

      if (Options.shouldPreProcess()) {
        HeaderMappingPreProcessor headerMappingPreProcessor = new HeaderMappingPreProcessor(parser);
        headerMappingPreProcessor.processBatch(batch);
        if (ErrorUtil.errorCount() > 0) {
          return;
        }
      }

      TranslationProcessor translationProcessor
          = new TranslationProcessor(parser, loadDeadCodeMap());
      translationProcessor.processBatch(batch);
      if (ErrorUtil.errorCount() > 0) {
        return;
      }
      translationProcessor.postProcess();
    } finally {
      NameTable.cleanup();
      Types.cleanup();
      if (preProcessorTempDir != null) {
        FileUtil.deleteTempDir(preProcessorTempDir);
      }
      Options.deleteTemporaryDirectory();
    }
  }

  /**
   * Entry point for tool.
   * Initializes {@link Options}, calls {@link #run}, and exits.
   *
   * @param args command-line arguments: flags and source file names
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
      ErrorUtil.error(e.getMessage());
      System.exit(1);
    }

    run(Arrays.asList(files));

    checkErrors();
  }
}
