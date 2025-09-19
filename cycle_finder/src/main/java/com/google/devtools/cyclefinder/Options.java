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

package com.google.devtools.cyclefinder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.Version;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

class Options extends com.google.devtools.j2objc.Options {

  private static String usageMessage;
  private static String helpMessage;

  static {
    // Load string resources.
    URL propertiesUrl = Resources.getResource(CycleFinder.class, "CycleFinder.properties");
    Properties properties = new Properties();
    try {
      properties.load(propertiesUrl.openStream());
    } catch (IOException e) {
      System.err.println("unable to access tool properties: " + e);
      System.exit(1);
    }
    usageMessage = properties.getProperty("usage-message");
    Preconditions.checkNotNull(usageMessage);
    helpMessage = properties.getProperty("help-message");
    Preconditions.checkNotNull(helpMessage);
  }

  private final List<String> suppressListFiles = Lists.newArrayList();
  private final List<String> restrictToListFiles = Lists.newArrayList();
  private List<String> sourceFiles = Lists.newArrayList();
  private boolean printReferenceGraph = false;

  public List<String> getSourceFiles() {
    return sourceFiles;
  }

  public void setSourceFiles(List<String> files) {
    this.sourceFiles = files;
  }

  public List<String> getSuppressListFiles() {
    return suppressListFiles;
  }

  public void addSuppressListFile(String fileName) {
    suppressListFiles.add(fileName);
  }

  public List<String> getRestrictToFiles() {
    return restrictToListFiles;
  }

  public void addRestrictToFile(String fileName) {
    restrictToListFiles.add(fileName);
  }

  private void addManifest(String manifestFile) throws IOException {
    try (BufferedReader in =
        Files.newReader(new File(manifestFile), fileUtil().getCharset())) {
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        if (!Strings.isNullOrEmpty(line)) {
          sourceFiles.add(line.trim());
        }
      }
    }
  }

  public boolean printReferenceGraph() {
    return printReferenceGraph;
  }

  @VisibleForTesting
  public void setPrintReferenceGraph() {
     printReferenceGraph = true;
  }

  @VisibleForTesting
  public void setClasspath(String classpath) {
    fileUtil().getClassPathEntries().addAll(Arrays.asList(classpath.split(":")));
  }

  public static void usage(String invalidUseMsg) {
    System.err.println("cycle_finder: " + invalidUseMsg);
    System.err.println(usageMessage);
    System.exit(1);
  }

  public static void help(boolean errorExit) {
    System.err.println(helpMessage);
    // javac exits with 2, but any non-zero value works.
    System.exit(errorExit ? 2 : 0);
  }

  public static void version() {
    System.err.println("cycle_finder " + Version.jarVersion(Options.class));
    System.exit(0);
  }

  public static Options parse(String[] args) throws IOException {
    Options options = new Options();
    // pre-scan for encoding
    for (int i = 0; i < args.length - 1; i++) {
      if (args[i].equals("-encoding")) {
        try {
          options.fileUtil().setFileEncoding(args[i + 1]);
        } catch (UnsupportedCharsetException e) {
          ErrorUtil.error(e.getMessage());
        }
        break;
      }
    }

    List<String> j2objcArgs = new ArrayList<>();
    Iterator<String> iter = Arrays.asList(args).iterator();
    while (iter.hasNext()) {
      String arg = iter.next();
      if (arg.equals("--suppress-list")
          // Deprecated flag names.
          || arg.equals("--whitelist")
          || arg.equals("-w")) {
        if (!iter.hasNext()) {
          usage("--suppress-list requires an argument");
        }
        options.suppressListFiles.add(iter.next());
      } else if (arg.equals("--restrict-to")
          // Deprecated flag name.
          || arg.equals("--blacklist")) {
        if (!iter.hasNext()) {
          usage("--restrict-to requires an argument");
        }
        options.restrictToListFiles.add(iter.next());
      } else if (arg.equals("--sourcefilelist") || arg.equals("-s")) {
        if (!iter.hasNext()) {
          usage("--sourcefilelist requires an argument");
        }
        options.addManifest(iter.next());
      } else if (arg.equals("--print-reference-graph")) {
        options.printReferenceGraph = true;
      } else if (arg.equals("-version")) {
        version();
      } else if (arg.startsWith("-h") || arg.equals("--help")) {
        help(false);
      } else {
        j2objcArgs.add(arg);
      }
    }

    options.sourceFiles.addAll(options.load(j2objcArgs.toArray(new String[0])));

    if (options.sourceFiles.isEmpty()) {
      usage("no source files");
    }

    return options;
  }
}
