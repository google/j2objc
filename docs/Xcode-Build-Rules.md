---
layout: default
---

# Xcode Build Rules

Most Xcode project types support build rules, which allow developers to modify
how existing file types are built, and to define how new file types are built.
The J2ObjC scripts are intentionally designed to plug into build systems like Xcode.

>The [j2objc-sample-reversi project](https://github.com/tomball/j2objc-sample-reversi) is
>an example of how to add Java sources to a simple iOS game.

## Adding a J2ObjC Build Rule

1. Determine the root directory of your Java source files, which we'll call 
"$*source-root*". The root directory is the directory that contains the top
package of your source files.  For example, if you have a Java package `foo.bar`
in a directory called "~/myproject/src", that package's Java files should be in
`~/myproject/src/foo/bar/**.java` -- that means `~/myproject/src` is the root
directory for your project.
- If they are in a group or directory in your Xcode project, the source-root
is `${PROJECT_DIR}/`*<group or directory name>*.
- If in doubt, right click on that group or directory and select Show in Finder
to see the directory.
- If the Java source files are external to the Xcode project, enter the path used
when listing them in a Terminal window.
1. In Xcode's Project Editor, select the project target and click on the Build Rules tab.
1. Click the Add Build Rule button at the bottom right of the panel.
1. In the new rule's Process option, select "Java source files".
1. In the Custom script text box, add the following (substitute your J2ObjC's
distribution directory for $*distribution-path*):
`$distribution-path/j2objc -d ${DERIVED_FILES_DIR} -sourcepath ${PROJECT_DIR}/$source-root \`
`--no-package-directories ${INPUT_FILE_PATH};`
1. In the Output Files panel, click the **+** button, and add 
`${DERIVED_FILES_DIR}/${INPUT_FILE_BASE}.h`.
1. Click the **+* button again, and add `${DERIVED_FILES_DIR}/${INPUT_FILE_BASE}.m`.
(cut&paste works well here :-)

When you are finished, the settings panel should look something like this:

![Xcode build rules setting](https://raw.github.com/google/j2objc/master/doc/wiki_images/xcode-java-setup.png)

## Update the Build Settings

Select the Build Settings tab, and make the following changes (again, 
substitute your J2ObjC's distribution directory for *$distribution-path*):

1. In User Header Search Paths, add `$distribution-path/include`,
then add `${DERIVED_FILES_DIR}`.
1. In Library Search Paths, add `$distribution-path/lib`.
1. In Other Linker Flags, add `-ljre_emul`.  (jre_emul is J2ObjC's 
JRE emulation library).

Next, add the Java source files to the project.  When the project is built, 
these files will be converted to Objective-C files and built with the rest of the project.

Finally, select the Build Phases tab, open the Link phase and add:

1. The libz.dylib library, needed to support java.util.zip.
1. The Security Framework, to support secure hash generation.
1. The icucore library, needed to support java.text.

## Debugging Build Problems

If Xcode reports a build failure with these steps, open the Build report and
click on the expand icon at the right of the error.  This shows the equivalent
command-line statement that was executed.  Double-check that the settings above are correct.

If there's an error like "'JreEmulation.h' file not found", the problem is
likely a bad header search path.  Search the build command for the -I option
with the path; if it looks okay, copy the path (not the -I) and in a terminal
window run ls with that path to verify there isn't a typo.

If there's a link error where a symbol isn't found that begins with 
`_OBJC_CLASS_$_Java`, the library search path is wrong, as the linker cannot
find the "libjre_emul.a" file. Expand the link step in the log file to see
the path created after the environment variables are expanded, and adjust as needed.

If your app fails with runtime errors like `NSString [...](__NSCFConstantString)`,
the `-ObjC`, and/or `-force_load $distribution-path/lib/libjre_emul.a`
linker flags need to be set. This is a common issue with Objective-C static
libraries that have categories.

If you still have problems, ask the
[j2objc-discuss group](https://groups.google.com/forum/?hl=en#!forum/j2objc-discuss).