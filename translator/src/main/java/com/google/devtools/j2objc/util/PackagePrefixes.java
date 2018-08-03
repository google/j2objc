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

package com.google.devtools.j2objc.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.lang.model.element.PackageElement;

/**
 * Class that creates and stores the prefixes associated with Java packages.
 * Prefixes can be defined in several ways:
 * <ul>
 * <li>Using a --prefix command-line flag,</li>
 * <li>In a properties file specified by a --prefixes command-line flag,</li>
 * <li>By an ObjectiveCName annotation in a package-info.java source file, or</li>
 * <li>By camel-casing the package name (default).
 * </ul>
 *
 * Command-line wildcard flags (either separately or in a properties file) are
 * also supported, which map multiple packages to a single prefix. For example,
 * 'com.google.devtools.j2objc.*=J2C' specifies that all translator classes have
 * a J2C prefix, but not the com.google.j2objc.annotations classes. Wildcard
 * declarations are matched in the order they are declared.
 *
 * @author Tom Ball
 */
public final class PackagePrefixes {

  private final PackageInfoLookup packageLookup;
  private final Map<String, String> mappedPrefixes = Maps.newHashMap();

  // A key array is used so that wildcards are checked in declared order.
  // There is one wildcard value for each key, enforced within this class.
  // Too bad there's no available equivalent to android.util.ArrayMap.
  private List<Pattern> wildcardKeys = Lists.newArrayList();
  private List<String> wildcardValues = Lists.newArrayList();

  public PackagePrefixes(PackageInfoLookup packageLookup) {
    this.packageLookup = packageLookup;
  }

  @VisibleForTesting
  String getPrefix(String pkg) {
    String value = mappedPrefixes.get(pkg);
    if (value != null) {
      return value;
    }

    for (int i = 0; i < wildcardKeys.size(); i++) {
      Pattern p = wildcardKeys.get(i);
      if (p.matcher(pkg).matches()) {
        value = wildcardValues.get(i);
        mappedPrefixes.put(pkg, value);
        return value;
      }
    }

    return null;
  }

  public void addPrefix(String pkg, String prefix) {
    if (pkg == null || prefix == null) {
      throw new IllegalArgumentException("null package or prefix specified");
    }
    if (pkg.contains("*")) {
      String regex = wildcardToRegex(pkg);
      for (int i = 0; i < wildcardKeys.size(); i++) {
        if (regex.equals(wildcardKeys.get(i).toString())) {
          String oldPrefix = wildcardValues.get(i);
          if (!prefix.equals(oldPrefix)) {
            ErrorUtil.error("package prefix redefined; was \"" + oldPrefix + ", now " + prefix);
          }
          return;
        }
      }
      wildcardKeys.add(Pattern.compile(regex));
      wildcardValues.add(prefix);
    } else {
      mappedPrefixes.put(pkg, prefix);
    }
  }

  /**
   * Return the prefix for a specified package. If a prefix was specified
   * for the package, then that prefix is returned. Otherwise, a camel-cased
   * prefix is created from the package name.
   */
  public String getPrefix(PackageElement packageElement) {
    if (packageElement == null) {
      return "";
    }
    String packageName = packageElement.getQualifiedName().toString();
    String prefix = getPrefix(packageName);
    if (prefix != null) {
      return prefix;
    }

    prefix = packageLookup.getObjectiveCName(packageName);
    if (prefix == null) {
      prefix = NameTable.camelCaseQualifiedName(packageName);
    }
    addPrefix(packageName, prefix);
    return prefix;
  }

  /**
   * Add a file map of packages to their respective prefixes, using the Properties file format.
   */
  public void addPrefixesFile(String filename) throws IOException {
    try (Reader r = Files.newBufferedReader(Paths.get(filename), StandardCharsets.UTF_8)) {
      addPrefixProperties(r);
    }
  }

  @VisibleForTesting
  void addPrefixProperties(Reader r) throws IOException {
    // To avoid Properties using key hash ordering, parse each line as a separate Properties.
    try (BufferedReader br = new BufferedReader(r)) {
      String line;
      while ((line = br.readLine()) != null) {
        Properties props = new Properties();
        props.load(new StringReader(line));
        Enumeration<?> names = props.propertyNames();
        while (names.hasMoreElements()) {
          String name = (String) names.nextElement();
          addPrefix(name.trim(), props.getProperty(name).trim());
        }
      }
    }
  }

  @VisibleForTesting
  static String wildcardToRegex(String s) {
    if (s.endsWith(".*")) {
      // Include root package in regex. For example, foo.bar.* needs to match
      // foo.bar, foo.bar.mumble, etc.
      String root = s.substring(0, s.length() - 2).replace(".",  "\\.");
      return UnicodeUtils.format("^(%s|%s\\..*)$", root, root);
    }
    return UnicodeUtils.format("^%s$", s.replace(".", "\\.").replace("\\*", ".*"));
  }
}
