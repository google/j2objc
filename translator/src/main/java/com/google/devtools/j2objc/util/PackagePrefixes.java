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
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.file.InputFile;
import com.google.j2objc.annotations.ObjectiveCName;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

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

    private Map<String, String> mappedPrefixes = Maps.newHashMap();

    // A key array is used so that wildcards are checked in declared order.
    // There is one wildcard value for each key, enforced within this class.
    // Too bad there's no available equivalent to android.util.ArrayMap.
    private List<Pattern> wildcardKeys = Lists.newArrayList();
    private List<String> wildcardValues = Lists.newArrayList();

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

    public boolean hasPrefix(String packageName) {
      return getPrefix(packageName) != null;
    }

    /**
     * Return the prefix for a specified package. If a prefix was specified
     * for the package, then that prefix is returned. Otherwise, a camel-cased
     * prefix is created from the package name.
     */
    public String getPrefix(IPackageBinding packageBinding) {
      String packageName = packageBinding.getName();
      if (hasPrefix(packageName)) {
        return getPrefix(packageName);
      }

      for (IAnnotationBinding annotation : packageBinding.getAnnotations()) {
        if (annotation.getName().endsWith("ObjectiveCName")) {
          String prefix = (String) BindingUtil.getAnnotationValue(annotation, "value");
          addPrefix(packageName, prefix);
          // Don't return, as there may be a prefix annotation that overrides this value.
        }
      }

      String prefix = getPrefixFromPackageInfoSource(packageBinding);
      if (prefix == null) {
        prefix = getPrefixFromPackageInfoClass(packageName);
      }
      if (prefix == null) {
        prefix = NameTable.camelCaseQualifiedName(packageName);
      }
      addPrefix(packageName, prefix);
      return prefix;
    }

    /**
     * Check if there is a package-info.java source file with a prefix annotation.
     */
    private String getPrefixFromPackageInfoSource(IPackageBinding packageBinding) {
      try {
        String qualifiedName = "package-info";
        String packageName = packageBinding.getName();
        // Path will be null if this is the empty package.
        if (packageName != null) {
          qualifiedName = packageName + '.' + qualifiedName;
        }
        InputFile file = FileUtil.findOnSourcePath(qualifiedName);
        if (file != null) {
          String pkgInfo = FileUtil.readFile(file);
          int i = pkgInfo.indexOf("@ObjectiveCName");
          if (i == -1) {
            i = pkgInfo.indexOf("@com.google.j2objc.annotations.ObjectiveCName");
          }
          if (i > -1) {
            // Extract annotation's value string.
            i = pkgInfo.indexOf('"', i + 1);
            if (i > -1) {
              int j = pkgInfo.indexOf('"', i + 1);
              if (j > -1) {
                return pkgInfo.substring(i + 1, j);
              }
            }
          }
        }
      } catch (IOException e) {
        // Continue, as there's no package-info to check.
      }
      return null;
    }

    /**
     * Check if there is a package-info class with a prefix annotation.
     */
    private String getPrefixFromPackageInfoClass(String packageName) {
      List<String> paths = Options.getBootClasspath();
      paths.addAll(Options.getClassPathEntries());
      PathClassLoader classLoader = new PathClassLoader(paths);
      try {
        Class<?> clazz = classLoader.loadClass(packageName + ".package-info");
        ObjectiveCName objectiveCName = clazz.getAnnotation(ObjectiveCName.class);
        if (objectiveCName != null) {
          return objectiveCName.value();
        }
      } catch (ClassNotFoundException e) {
        // Class does not exist -- ignore exception.
      } catch (SecurityException e) {
        // Failed fetching a package-info class from a secure package -- ignore exception.
      } finally {
        try {
          classLoader.close();
        } catch (IOException e) {
          // Ignore, any open files will be closed on exit.
        }
      }
      return null;
    }

    /**
     * Add a set of package=prefix properties.
     */
    public void addPrefixProperties(Properties props) {
      for (String pkg : props.stringPropertyNames()) {
        addPrefix(pkg, props.getProperty(pkg).trim());
      }
    }

    @VisibleForTesting
    static String wildcardToRegex(String s) {
      if (s.endsWith(".*")) {
        // Include root package in regex. For example, foo.bar.* needs to match
        // foo.bar, foo.bar.mumble, etc.
        String root = s.substring(0, s.length() - 2).replace(".",  "\\.");
        return String.format("^(%s|%s\\..*)$", root, root);
      }
      return String.format("^%s$", s.replace(".", "\\.").replace("\\*", ".*"));
    }
}
